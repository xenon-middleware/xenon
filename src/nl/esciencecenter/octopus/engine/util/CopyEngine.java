/*
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.esciencecenter.octopus.engine.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.octopus.engine.files.CopyImplementation;
import nl.esciencecenter.octopus.engine.files.CopyStatusImplementation;
import nl.esciencecenter.octopus.exceptions.FileAlreadyExistsException;
import nl.esciencecenter.octopus.exceptions.IllegalSourcePathException;
import nl.esciencecenter.octopus.exceptions.IllegalTargetPathException;
import nl.esciencecenter.octopus.exceptions.InvalidDataException;
import nl.esciencecenter.octopus.exceptions.NoSuchCopyException;
import nl.esciencecenter.octopus.exceptions.NoSuchFileException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.Copy;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.CopyStatus;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.OpenOption;

/**
 * A CopyEngine is responsible for performing the asynchronous copy operations.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class CopyEngine {

    class CopyThread extends Thread {
        public void run() {
            CopyInfo ac = dequeue();

            while (ac != null) {
                copy(ac);
                ac = dequeue();
            }
        }
    }

    /** A logger for this class */
    private static final Logger LOGGER = LoggerFactory.getLogger(CopyEngine.class);

    /** The polling delay */
    private static final int POLLING_DELAY = 1000;
    
    /** The default buffer size */
    private static final int BUFFER_SIZE = 4 * 1024;

    private final Files owner;
    private final CopyThread thread;

    private Deque<CopyInfo> pending = new LinkedList<>();
    private Map<String, CopyInfo> finished = new LinkedHashMap<>();
    private CopyInfo running;

    private long nextID = 0;

    private boolean done = false;

    public CopyEngine(Files owner) {
        this.owner = owner;
        this.thread = new CopyThread();
        thread.start();
    }

    private void close(Closeable c) {

        try {
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            // ignored
        }
    }

    private void streamCopy(InputStream in, OutputStream out, CopyInfo ac) throws IOException {

        long total = 0;

        byte[] buffer = new byte[BUFFER_SIZE];

        if (ac.isCancelled()) {
            LOGGER.debug("Copy killed by user!");
            ac.setException(new IOException("Copy killed by user"));
            return;
        }

        int size = in.read(buffer);

        while (size > 0) {
            out.write(buffer, 0, size);
            total += size;

            ac.setBytesCopied(total);

            if (ac.isCancelled()) {
                LOGGER.debug("Copy killed by user!");
                ac.setException(new IOException("Copy killed by user"));
                return;
            }

            size = in.read(buffer);
        }
    }

    private void append(AbsolutePath source, long fromOffset, AbsolutePath target, CopyInfo ac) throws OctopusIOException {

        // We need to append some bytes from source to target. 
        LOGGER.debug("Appending from {} to {} starting at {}", source.getPath(), target.getPath(), fromOffset);

        InputStream in = owner.newInputStream(source);
        OutputStream out = owner.newOutputStream(target, OpenOption.OPEN, OpenOption.APPEND);

        long skipped = 0;
       
        try {
            
            while (skipped < fromOffset) { 
                long tmp = in.skip(fromOffset);
            
                if (tmp <= 0) { 
                    throw new OctopusIOException("CopyEngine", "Failed to seek file " + source.getPath() + " to " + fromOffset);
                }
                
                skipped += tmp;
            }
            
            streamCopy(in, out, ac);
        } catch (IOException e) {
            throw new OctopusIOException("CopyEngine", "Failed to copy " + source.getPath() + ":" + fromOffset + " to target "
                    + target.getPath(), e);
        } finally {
            close(in);
            close(out);
        }
    }

    private int readFully(InputStream in, byte [] buffer) throws IOException { 
        
        int offset = 0;
        
        while (offset < buffer.length) { 
            int tmp = in.read(buffer, offset, buffer.length-offset);
        
            if (tmp < 0) { 
                break;
            }
            
            offset += tmp;
        }
        
        return offset;
    }
    
    
    private boolean compareHead(CopyInfo ac, AbsolutePath target, AbsolutePath source) throws OctopusIOException, IOException {

        LOGGER.debug("Compare head of {} to {}", target.getPath(), source.getPath());

        byte[] buf1 = new byte[BUFFER_SIZE];
        byte[] buf2 = new byte[BUFFER_SIZE];

        InputStream in1 = owner.newInputStream(target);
        InputStream in2 = owner.newInputStream(source);

        try {
            while (true) {

                if (ac.isCancelled()) {
                    throw new IOException("Copy killed by user");
                }

                int size1 = readFully(in1, buf1);
                int size2 = readFully(in2, buf2);

                if (size1 > size2) {
                    return false;
                }

                if (size1 < 0) {
                    return true;
                }

                for (int i = 0; i < size1; i++) {
                    if (buf1[i] != buf2[i]) {
                        return false;
                    }
                }
            }
        } finally {
            close(in1);
            close(in2);
        }
    }

    private void doResume(CopyInfo ac) throws OctopusIOException {

        if (ac.isCancelled()) {
            ac.setException(new IOException("Copy killed by user"));
            return;
        }

        AbsolutePath source = ac.copy.getSource();
        AbsolutePath target = ac.copy.getTarget();

        LOGGER.debug("Resume copy from {} to {} verify={}", source.getPath(), target.getPath(), ac.verify);

        if (!owner.exists(source)) {
            throw new NoSuchFileException("CopyEngine", "Source " + source.getPath() + " does not exist!");
        }

        FileAttributes sourceAtt = owner.getAttributes(source);
        
        if (sourceAtt.isDirectory()) {
            throw new IllegalSourcePathException("CopyEngine", "Source " + source.getPath() + " is a directory");
        }

        if (sourceAtt.isSymbolicLink()) {
            throw new IllegalSourcePathException("CopyEngine", "Source " + source.getPath() + " is a link");
        }

        if (!owner.exists(target)) {
            throw new NoSuchFileException("CopyEngine", "Target " + target.getPath() + " does not exist!");
        }

        FileAttributes targetAtt = owner.getAttributes(target);
        
        if (targetAtt.isDirectory()) {
            throw new IllegalSourcePathException("CopyEngine", "Target " + target.getPath() + " is a directory");
        }

        if (targetAtt.isSymbolicLink()) {
            throw new IllegalSourcePathException("CopyEngine", "Target " + target.getPath() + " is a link");
        }

        if (source.normalize().equals(target.normalize())) {
            return;
        }

        if (ac.verify) {
            if (ac.isCancelled()) {
                ac.setException(new IOException("Copy killed by user"));
                return;
            }

            // check if the data in target corresponds to the head of source.
            try {
                if (!compareHead(ac, target, source)) {
                    throw new InvalidDataException("CopyEngine", "Data in target " + target.getPath() + " does not match source "
                            + source.getPath());
                }
            } catch (IOException e) {
                throw new OctopusIOException("CopyEngine", "Failed to compare " + source.getPath() + " to " + target.getPath(), e);
            }
        }

        long targetSize = targetAtt.size();
        long sourceSize = sourceAtt.size();

        LOGGER.debug("Resuming copy from {} to {} ? ", source.getPath(), target.getPath(), (sourceSize < targetSize));

        // If target is larger than source, they cannot be the same file.
        if (targetSize > sourceSize) {
            throw new InvalidDataException("CopyEngine", "Data in target " + target.getPath() + " does not match " + source + " "
                    + source.getPath());
        }

        // If target is the same size as source we are done.
        if (targetSize == sourceSize) {
            ac.setBytesToCopy(0);
            ac.setBytesCopied(0);
            return;
        }

        ac.setBytesToCopy(sourceSize - targetSize);

        // Now append source (from index targetSize) to target.
        append(source, targetSize, target, ac);
    }

    private void doAppend(CopyInfo ac) throws OctopusIOException {

        if (ac.isCancelled()) {
            ac.setException(new IOException("Copy killed by user"));
            return;
        }

        AbsolutePath source = ac.copy.getSource();
        AbsolutePath target = ac.copy.getTarget();

        LOGGER.debug("Append from {} to {} verify={}", source.getPath(), target.getPath());

        if (!owner.exists(source)) {
            throw new NoSuchFileException("CopyEngine", "Source " + source.getPath() + " does not exist!");
        }

        FileAttributes sourceAtt = owner.getAttributes(source);
        
        if (sourceAtt.isDirectory()) {
            throw new IllegalSourcePathException("CopyEngine", "Source " + source.getPath() + " is a directory");
        }

        if (!owner.exists(target)) {
            throw new NoSuchFileException("CopyEngine", "Target " + target.getPath() + " does not exist!");
        }
        
        FileAttributes targetAtt = owner.getAttributes(target);
        
        if (targetAtt.isDirectory()) {
            throw new IllegalSourcePathException("CopyEngine", "Target " + target.getPath() + " is a directory");
        }
        
        if (source.normalize().equals(target.normalize())) {
            throw new IllegalTargetPathException("CopyEngine", "Can not append a file to itself (source " + source.getPath()
                    + " equals target " + target.getPath() + ")");
        }

        ac.setBytesToCopy(sourceAtt.size());
        append(source, 0, target, ac);
    }

    private void doCopy(CopyInfo ac) throws OctopusIOException {

        if (ac.isCancelled()) {
            ac.setException(new IOException("Copy killed by user"));
            return;
        }

        AbsolutePath source = ac.copy.getSource();
        AbsolutePath target = ac.copy.getTarget();

        boolean replace = (ac.mode == CopyOption.REPLACE);
        boolean ignore = (ac.mode == CopyOption.IGNORE);

        LOGGER.debug("Copy from {} to {} replace={} ignore={}", source.getPath(), target.getPath(), replace, ignore);

        if (!owner.exists(source)) {
            throw new NoSuchFileException("CopyEngine", "Source " + source.getPath() + " does not exist!");
        }
        
        FileAttributes sourceAtt = owner.getAttributes(source);
        
        if (sourceAtt.isDirectory()) {
            throw new IllegalSourcePathException("CopyEngine", "Source " + source.getPath() + " is a directory");
        }

        if (source.normalize().equals(target.normalize())) {
            return;
        }

        if (owner.exists(target)) {
            if (ignore) {
                return;
            }
            if (!replace) {
                throw new FileAlreadyExistsException("CopyEngine", "Target " + target.getPath() + " already exists!");
            }
        }

        if (!owner.exists(target.getParent())) {
            throw new NoSuchFileException("CopyEngine", "Target directory " + target.getParent().getPath() + " does not exist!");
        }

        ac.setBytesToCopy(sourceAtt.size());

        InputStream in = null;
        OutputStream out = null;

        try {
            in = owner.newInputStream(source);

            if (replace) {
                out = owner.newOutputStream(target, OpenOption.OPEN_OR_CREATE, OpenOption.TRUNCATE);
            } else {
                out = owner.newOutputStream(target, OpenOption.CREATE, OpenOption.APPEND);
            }

            streamCopy(in, out, ac);

        } catch (IOException e) {
            throw new OctopusIOException("CopyEngine", "Failed to copy " + source.getPath() + " to " + target.getPath(), e);
        } finally {
            close(in);
            close(out);
        }
    }

    private void copy(CopyInfo info) {

        LOGGER.debug("Start copy: {}", info);

        try {
            switch (info.mode) {
            case CREATE:
            case REPLACE:
            case IGNORE:
                doCopy(info);
                break;
            case APPEND:
                doAppend(info);
                break;
            case RESUME:
                doResume(info);
                break;
            default:
                throw new OctopusIOException("CopyEngine", "INTERNAL ERROR: Failed to recognise copy mode! (" + info.mode + " "
                        + info.verify + ")");
            }
        } catch (Exception e) {
            info.setException(e);
        }

        LOGGER.debug("Finished copy: {}", info);
    }

    public void copy(CopyInfo info, boolean asynchronous) {
        if (asynchronous) {
            enqueue(info);
        } else {
            copy(info);
        }
    }

    public synchronized void done() {
        LOGGER.debug("Sending CopyEngine termination signal");
        done = true;
        notifyAll();
    }

    private synchronized void enqueue(CopyInfo info) {

        LOGGER.debug("CopyEngine queueing copy: {}", info);

        pending.addLast(info);
        notifyAll();
    }

    private synchronized CopyInfo dequeue() {

        LOGGER.debug("CopyEngine dequeueing copy");

        if (running != null) {
            finished.put(running.copy.getUniqueID(), running);
            running = null;
            notifyAll();
        }

        while (!done && pending.size() == 0) {
            try {
                wait(POLLING_DELAY);
            } catch (InterruptedException e) {
                // ignore ?
            }
        }

        if (done) {
            LOGGER.debug("CopyEngine received termination signal with {} pending copies.", pending.size());
            return null;
        }

        running = pending.removeFirst();
        return running;
    }

    private synchronized void waitUntilCancelled(String ID) {

        LOGGER.debug("Waiting until copy {} is cancelled.", ID);

        while (running != null && ID.equals(running.copy.getUniqueID())) {
            try {
                wait(POLLING_DELAY);
            } catch (InterruptedException e) {
                // ignored.
            }
        }
    }

    public synchronized CopyStatus cancel(Copy copy) throws NoSuchCopyException {

        if (!(copy instanceof CopyImplementation)) {
            throw new NoSuchCopyException("CopyEngine", "No such copy!");
        }

        CopyImplementation tmp = (CopyImplementation) copy;

        String ID = tmp.getUniqueID();

        LOGGER.debug("Attempting to cancel copy {}.", ID);

        if (running != null && ID.equals(running.copy.getUniqueID())) {
            LOGGER.debug("Canceled copy {} is running,", ID);
            running.cancel();

            // We should now wait until the running copy is indeed cancelled. Otherwise, we are in an inconsistent state when 
            // we return.            
            waitUntilCancelled(ID);
        }

        CopyInfo ac = finished.remove(ID);

        if (ac != null) {
            LOGGER.debug("Canceled copy {} was already finished.", ID);
            // Already finished
            return new CopyStatusImplementation(copy, "DONE", false, true, ac.getBytesToCopy(), ac.getBytesCopied(),
                    ac.getException());
        }

        Iterator<CopyInfo> it = pending.iterator();

        while (it.hasNext()) {

            CopyInfo c = it.next();

            if (c.copy.getUniqueID().equals(ID)) {
                it.remove();
                LOGGER.debug("Canceled copy {} was queued.", ID);
                return new CopyStatusImplementation(copy, "KILLED", false, false, c.getBytesToCopy(), 0, new IOException(
                        "Copy killed by user"));
            }
        }

        throw new NoSuchCopyException("CopyEngine", "No such copy " + ID);
    }

    public synchronized CopyStatus getStatus(Copy copy) throws NoSuchCopyException {

        if (!(copy instanceof CopyImplementation)) {
            throw new NoSuchCopyException("CopyEngine", "No such copy!");
        }

        CopyImplementation tmp = (CopyImplementation) copy;

        String ID = tmp.getUniqueID();

        LOGGER.debug("Retrieving status of copy {}.", ID);

        String state = null;
        CopyInfo ac = null;
        boolean isRunning = false;
        boolean isDone = false;

        if (running != null && ID.equals(running.copy.getUniqueID())) {
            state = "RUNNING";
            ac = running;
            isRunning = true;
        }

        if (ac == null) {
            ac = finished.remove(ID);

            if (ac != null) {
                state = "DONE";
                isDone = true;
            }
        }

        if (ac == null) {
            for (CopyInfo c : pending) {
                if (c.copy.getUniqueID().equals(ID)) {
                    ac = c;
                    state = "PENDING";
                    break;
                }
            }
        }

        if (ac == null) {
            throw new NoSuchCopyException("CopyEngine", "No such copy " + ID);
        }

        return new CopyStatusImplementation(copy, state, isRunning, isDone, ac.getBytesToCopy(), ac.getBytesCopied(),
                ac.getException());
    }

    public synchronized String getNextID(String prefix) {
        return prefix + nextID++;
    }
}
