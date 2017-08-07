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
package nl.esciencecenter.xenon.adaptors.schedulers;

import java.io.IOException;
import java.io.OutputStream;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.shared.local.LocalUtil;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.filesystems.PathAlreadyExistsException;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.Streams;
import nl.esciencecenter.xenon.utils.LocalFileSystemUtils;
import nl.esciencecenter.xenon.utils.StreamForwarder;

/**
 * BatchProcess wraps an {@link InteractiveProcess} to emulate a batch process.
 * 
 * @version 1.0
 * @since 1.0
 */
class BatchProcess implements Process {

    /** Time to wait for a StreamForwarder to terminate (in ms.) */ 
    private static final long TERMINATION_DELAY = 1000L;
    
    private final InteractiveProcess process;

    private StreamForwarder stdinForwarder;
    private StreamForwarder stdoutForwarder;
    private StreamForwarder stderrForwarder;
   
    public BatchProcess(FileSystem filesystem, Path workingDirectory, JobDescription description, String jobIdentifier, InteractiveProcessFactory factory)
            throws XenonException, IOException {

        // Retrieve the filesystem that goes with the scheduler.
        Path workdir = processPath(filesystem, workingDirectory, description.getWorkingDirectory());

        if (!filesystem.exists(workdir)) {
            throw new IOException("Working directory " + workdir + " does not exist!");
        }

        // If needed create a file for stdin, and make sure it exists!
        Path stdin = null;

        if (description.getStdin() != null) {
            stdin = processPath(filesystem, workdir, description.getStdin());

            if (!filesystem.exists(stdin)) {
                throw new IOException("Stdin cannot be redirected from " + stdin + " (file does not exist!)");
            }
        }

        OutputStream out = createOutputStream(filesystem, workdir, description.getStdout());
        OutputStream err = createOutputStream(filesystem, workdir, description.getStderr());

        process = factory.createInteractiveProcess(description, jobIdentifier);
        Streams streams = process.getStreams();

        stdoutForwarder = new StreamForwarder(streams.getStdout(), out);
        stderrForwarder = new StreamForwarder(streams.getStderr(), err);

        if (stdin == null) {
            stdinForwarder = null;
            streams.getStdin().close();
        } else {
            stdinForwarder = new StreamForwarder(filesystem.readFromFile(stdin), streams.getStdin());
        }
    }

    private Path processPath(FileSystem filesystem, Path root, String path) throws XenonException {
        Path result;

        if (path == null) {
            result = root;
        } else if (LocalFileSystemUtils.startWithRoot(path)) { 
            result = new Path(path);
        } else {
            result = root.resolve(path);
        }

        return result;
    }

    private OutputStream createOutputStream(FileSystem filesystem, Path workdir, String filename) throws XenonException {

        if (filename == null) {
            return null;
        }

        Path file = processPath(filesystem, workdir, filename);

        // Create the files for the output stream. Will fail if the files already exist!
        if (filesystem.exists(file)) { 
        	throw new PathAlreadyExistsException(filesystem.getAdaptorName(), "File already exists: " + file);	
        }
        
        // Create the output stream and return it. 
        return filesystem.writeToFile(file);
    }

    private synchronized void closeStreams() {

        if (stdinForwarder != null) {
            stdinForwarder.terminate(TERMINATION_DELAY);
            stdinForwarder = null;
        }

        if (stdoutForwarder != null) {
            stdoutForwarder.terminate(TERMINATION_DELAY);
            stdoutForwarder = null;
        }

        if (stderrForwarder != null) {
            stderrForwarder.terminate(TERMINATION_DELAY);
            stderrForwarder = null;
        }
    }

    public boolean isDone()  {

        if (process.isDone()) {
            closeStreams();
            return true;
        }

        return false;
    }

    public int getExitStatus() {
        return process.getExitStatus();
    }

    public void destroy() {
        process.destroy();
        closeStreams();
    }
}
