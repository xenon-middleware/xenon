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
package nl.esciencecenter.octopus.adaptors.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.files.AbsolutePathImplementation;
import nl.esciencecenter.octopus.engine.files.FileSystemImplementation;
import nl.esciencecenter.octopus.engine.files.FilesEngine;
import nl.esciencecenter.octopus.exceptions.DirectoryNotEmptyException;
import nl.esciencecenter.octopus.exceptions.FileAlreadyExistsException;
import nl.esciencecenter.octopus.exceptions.IllegalSourcePathException;
import nl.esciencecenter.octopus.exceptions.NoSuchFileException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.OctopusRuntimeException;
import nl.esciencecenter.octopus.exceptions.UnsupportedOperationException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.Copy;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.CopyStatus;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.DirectoryStream.Filter;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.PathAttributesPair;
import nl.esciencecenter.octopus.files.PosixFilePermission;
import nl.esciencecenter.octopus.files.RelativePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

public class SshFiles implements Files {
    private static final Logger logger = LoggerFactory.getLogger(SshFiles.class);
    private static int currentID = 1;

    private static synchronized String getNewUniqueID() {
        String res = "ssh" + currentID;
        currentID++;
        return res;
    }

    /**
     * Used to store all state attached to a filesystem. This way, FileSystemImplementation is immutable.
     * 
     */
    class FileSystemInfo {
        FileSystemImplementation impl;
        Session session;

        public FileSystemInfo(FileSystemImplementation impl, Session session) {
            super();
            this.impl = impl;
            this.session = session;
        }

        public FileSystemImplementation getImpl() {
            return impl;
        }

        public Session getSession() {
            return session;
        }
    }

    private final OctopusEngine octopusEngine;
    private final SshAdaptor adaptor;
    @SuppressWarnings("unused")
    // no properties yet
    private final Properties properties;

    private HashMap<String, FileSystemInfo> fileSystems = new HashMap<String, FileSystemInfo>();

    public SshFiles(OctopusProperties properties, SshAdaptor sshAdaptor, OctopusEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
        this.adaptor = sshAdaptor;
        this.properties = properties;

        if (logger.isDebugEnabled()) {
            Set<String> attributeViews = FileSystems.getDefault().supportedFileAttributeViews();

            logger.debug(Arrays.toString(attributeViews.toArray()));
        }
    }

    @Override
    public FileSystem newFileSystem(URI location, Credential credential, Properties properties) throws OctopusException,
            OctopusIOException {

        if (!location.getPath().equals("") && !location.getPath().equals("/")) {
            throw new OctopusException(adaptor.getName(),
                    "Adaptor does not support a specific entry point. The location URI should not contain a path, or only '/'. URI = "
                            + location);
        }

        String uniqueID = getNewUniqueID();

        OctopusProperties octopusProperties = new OctopusProperties(
                properties);
        
        Session session = adaptor.createNewSession(uniqueID, location, credential, octopusProperties);

        ChannelSftp channel;
        channel = adaptor.getSftpChannel(session);

        String wd = null;
        try {
            wd = channel.pwd();
        } catch (SftpException e) {
            channel.disconnect();
            session.disconnect();
            throw adaptor.sftpExceptionToOctopusException(e);
        }
        channel.disconnect();

        RelativePath entryPath = new RelativePath(wd);

        logger.debug("remote cwd = " + wd + ", entryPath = " + entryPath);

        FileSystemImplementation result =
                new FileSystemImplementation(adaptor.getName(), uniqueID, location, entryPath, credential, octopusProperties);
        fileSystems.put(uniqueID, new FileSystemInfo(result, session));
        return result;
    }

    protected ChannelSftp getChannel(AbsolutePath path) throws OctopusIOException {
        return getChannel(path.getFileSystem());
    }

    protected ChannelSftp getChannel(FileSystem fileSystem) throws OctopusIOException {
        if (!fileSystem.getAdaptorName().equals(adaptor.getName())) {
            throw new OctopusRuntimeException(adaptor.getName(), "Illegal Filesystem type: " + fileSystem.getAdaptorName());
        }
        FileSystemImplementation fs = (FileSystemImplementation) fileSystem;

        FileSystemInfo info;
        synchronized (this) {
            info = fileSystems.get(fs.getUniqueID());
        }
        if (info == null) {
            throw new OctopusIOException(adaptor.getName(), "file system is already closed");
        }

        Session session = info.getSession();
        return adaptor.getSftpChannel(session);
    }

    void putChannel(ChannelSftp channel) {
        adaptor.putSftpChannel(channel);
    }

    @Override
    public AbsolutePath newPath(FileSystem filesystem, RelativePath location) throws OctopusException, OctopusIOException {
        return new AbsolutePathImplementation(filesystem, location);
    }

    @Override
    public void close(FileSystem filesystem) throws OctopusException, OctopusIOException {
        FileSystemImplementation fs = (FileSystemImplementation) filesystem;

        FileSystemInfo info;
        synchronized (this) {
            info = fileSystems.remove(fs.getUniqueID());
        }
        if (info == null) {
            throw new OctopusIOException(adaptor.getName(), "file system is already closed");
        }

        info.getSession().disconnect();
    }

    @Override
    public boolean isOpen(FileSystem filesystem) throws OctopusException, OctopusIOException {
        FileSystemImplementation fs = (FileSystemImplementation) filesystem;

        synchronized (this) {
            FileSystemInfo info = fileSystems.get(fs.getUniqueID());
            if (info == null) {
                return false;
            }
            return true;
        }
    }

    /**
     * Copy an existing source file or link to a non-existing target path.
     * 
     * The source must NOT be a directory.
     * 
     * The parent of the target path (e.g. <code>target.getParent</code>) must exist.
     * 
     * If the source is a link, the link itself will be copied, not the path to which it refers.
     * 
     * @param source
     *            the existing source file or link.
     * @param target
     *            the non existing target path.
     * @return the target path.
     * 
     * @throws NoSuchFileException
     *             If the source file does not exist or the target parent directory does not exist.
     * @throws FileAlreadyExistsException
     *             If the target file already exists.
     * @throws IllegalSourcePathException
     *             If the source is a directory.
     * @throws OctopusIOException
     *             If the copy failed.
     */
  //  @Override
    public AbsolutePath copy(AbsolutePath source, AbsolutePath target) throws OctopusIOException {

        if (!octopusEngine.files().exists(source)) {
            throw new NoSuchFileException(adaptor.getName(), "Source " + source.getPath() + " does not exist!");
        }

        if (octopusEngine.files().isDirectory(source)) {
            throw new IllegalSourcePathException(adaptor.getName(), "Source " + source.getPath() + " is a directory");
        }

        if (octopusEngine.files().exists(target)) {
            throw new FileAlreadyExistsException(adaptor.getName(), "Target " + target.getPath() + " already exists!");
        }

        if (!octopusEngine.files().exists(target.getParent())) {
            throw new NoSuchFileException(adaptor.getName(), "Target directory " + target.getParent().getPath()
                    + " does not exist!");
        }

        if (source.normalize().equals(target.normalize())) {
            return target;
        }

        logger.debug("ssh copy");
        // two cases: remote -> local and local -> remote

        FileSystem sourcefs = source.getFileSystem();
        FileSystem targetfs = target.getFileSystem();

        if (source.isLocal()) {
            if (target.isLocal()) {
                throw new OctopusIOException(adaptor.getName(), "Cannot copy local files.");
            }

            if (!targetfs.getAdaptorName().equals("ssh")) {
                throw new OctopusIOException(adaptor.getName(),
                        "Can only copy between local and an ssh locations (or vice-versa).");
            }

            // Ok, here, source is local, target is ssh.

            ChannelSftp channel = getChannel(target);

            try {
                logger.debug("copy from " + source.getPath() + " to " + target.getPath());
                channel.put(source.getPath(), target.getPath());
            } catch (SftpException e) {
                throw adaptor.sftpExceptionToOctopusException(e);
            } finally {
                putChannel(channel);
            }
        } else if (target.isLocal()) {
            if (source.isLocal()) {
                throw new OctopusIOException(adaptor.getName(), "Cannot copy local files.");
            }

            if (!sourcefs.getAdaptorName().equals("ssh")) {
                throw new OctopusIOException(adaptor.getName(),
                        "Can only copy between local and an ssh locations (or vice-versa).");
            }

            // Ok, here, target is local, source is ssh.

            ChannelSftp channel = getChannel(source);

            try {
                logger.debug("copy from " + source.getPath() + " to " + target.getPath());
                channel.get(source.getPath(), target.getPath());
            } catch (SftpException e) {
                throw adaptor.sftpExceptionToOctopusException(e);
            } finally {
                putChannel(channel);
            }
        } else {
            // both remote
            throw new OctopusIOException(adaptor.getName(), "Cannot copy files between two different remote filesystems!");
        }

        return target;
    }

    @Override
    public AbsolutePath createDirectory(AbsolutePath dir) throws OctopusIOException {
        if (exists(dir)) {
            throw new FileAlreadyExistsException(getClass().getName(), "Cannot create directory, as it already exists.");
        }

        ChannelSftp channel = getChannel(dir);

        try {
            channel.mkdir(dir.getPath());
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            putChannel(channel);
        }

        return dir;
    }

    @Override
    public AbsolutePath createDirectories(AbsolutePath dir) throws OctopusIOException {
        Iterator<AbsolutePath> itt = dir.iterator();

        while (itt.hasNext()) {
            AbsolutePath path = itt.next();

            if (!exists(path)) {
                createDirectory(path);
            }
        }

        return dir;
    }

    @Override
    public AbsolutePath createFile(AbsolutePath path) throws OctopusIOException {
        OutputStream out = null;
        try {
            out = newOutputStream(path);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return path;
    }

    @Override
    public AbsolutePath createSymbolicLink(AbsolutePath link, AbsolutePath target) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delete(AbsolutePath path) throws OctopusIOException {
        if (!exists(path)) {
            throw new NoSuchFileException(getClass().getName(), "cannot delete file, as it does not exist");
        }

        ChannelSftp channel = getChannel(path);

        try {
            if (isDirectory(path)) {
                if (newDirectoryStream(path, FilesEngine.ACCEPT_ALL_FILTER).iterator().hasNext()) {
                    throw new DirectoryNotEmptyException(adaptor.getName(), "cannot delete dir " + path + " as it is not empty");
                }

                channel.rmdir(path.getPath());
            } else {
                channel.rm(path.getPath());
            }
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            putChannel(channel);
        }
    }

    @Override
    public boolean exists(AbsolutePath path) throws OctopusIOException {
        ChannelSftp channel = getChannel(path);

        try {
            channel.lstat(path.getPath());
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return false;
            }

            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            putChannel(channel);
        }

        return true;
    }

    @Override
    public boolean isDirectory(AbsolutePath path) throws OctopusIOException {
        ChannelSftp channel = getChannel(path);

        try {
            SftpATTRS attributes = channel.lstat(path.getPath());
            return attributes.isDir();
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            putChannel(channel);
        }
    }

    /**
     * Move or rename an existing source path to a non-existing target path.
     * 
     * The parent of the target path (e.g. <code>target.getParent</code>) must exist.
     * 
     * If the source is a link, the link itself will be moved, not the path to which it refers. If the source is a directory, it
     * will be renamed to the target. This implies that a moving a directory between physical locations may fail.
     * 
     * @param source
     *            the existing source path.
     * @param target
     *            the non existing target path.
     * @return the target path.
     * 
     * @throws NoSuchFileException
     *             If the source file does not exist or the target parent directory does not exist.
     * @throws FileAlreadyExistsException
     *             If the target file already exists.
     * @throws OctopusIOException
     *             If the move failed.
     */
    /*    @Override
        public AbsolutePath move(AbsolutePath source, AbsolutePath target) throws OctopusIOException {

            if (!exists(source)) { 
                throw new NoSuchFileException(adaptor.getName(), "Source " + source.getPath() + " does not exist!");
            }
            
            if (exists(target)) { 
                throw new FileAlreadyExistsException(adaptor.getName(), "Target " + target.getPath() + " already exists!");
            }
            
            if (!exists(target.getParent())) { 
                throw new NoSuchFileException(adaptor.getName(), "Target directory " + target.getParent().getPath() + 
                        " does not exist!");
            }
            
            if (source.normalize().equals(target.normalize())) {
                return target;
            }

            try {
                Files.move(LocalUtils.javaPath(source), LocalUtils.javaPath(target)); 
            } catch (IOException e) {
                throw new OctopusIOException(adaptor.getName(), "Failed to move " + source.getPath() + " to " + 
                        target.getPath(), e);
            }

            return target;
        }
         */

    /**
     * Move or rename an existing source path to a non-existing target path.
     * 
     * The parent of the target path (e.g. <code>target.getParent</code>) must exist.
     * 
     * If the source is a link, the link itself will be moved, not the path to which it refers. If the source is a directory, it
     * will be renamed to the target. This implies that a moving a directory between physical locations may fail.
     * 
     * @param source
     *            the existing source path.
     * @param target
     *            the non existing target path.
     * @return the target path.
     * 
     * @throws NoSuchFileException
     *             If the source file does not exist or the target parent directory does not exist.
     * @throws FileAlreadyExistsException
     *             If the target file already exists.
     * @throws OctopusIOException
     *             If the move failed.
     */
    @Override
    public AbsolutePath move(AbsolutePath source, AbsolutePath target) throws OctopusIOException {
        FileSystem sourcefs = source.getFileSystem();
        FileSystem targetfs = target.getFileSystem();

        if (!sourcefs.getAdaptorName().equals("ssh") || !targetfs.getAdaptorName().equals("ssh")) {
            throw new OctopusIOException(adaptor.getName(), "Can only move within one remote ssh location.");
        }

        if (!sourcefs.getUri().getHost().equals(targetfs.getUri().getHost())) {
            throw new OctopusIOException(adaptor.getName(), "Cannot move between different sites: " + sourcefs.getUri().getHost()
                    + " and " + targetfs.getUri().getHost());
        }

        if (!exists(source)) {
            throw new NoSuchFileException(adaptor.getName(), "Source " + source.getPath() + " does not exist!");
        }

        if (exists(target)) {
            throw new FileAlreadyExistsException(adaptor.getName(), "Target " + target.getPath() + " already exists!");
        }

        if (!exists(target.getParent())) {
            throw new NoSuchFileException(adaptor.getName(), "Target directory " + target.getParent().getPath()
                    + " does not exist!");
        }

        if (source.normalize().equals(target.normalize())) {
            return target;
        }

        // Ok, here, we just have a local move

        ChannelSftp channel = getChannel(target);

        try {
            logger.debug("move from " + source.getPath() + " to " + target.getPath());
            channel.rename(source.getPath(), target.getPath());
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            putChannel(channel);
        }
        return target;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirectoryStream<AbsolutePath> newDirectoryStream(AbsolutePath path, Filter filter) throws OctopusIOException {
        if (!isDirectory(path)) {
            throw new OctopusIOException(getClass().getName(), "Cannot create directorystream, file is not a directory");
        }

        ChannelSftp channel = getChannel(path);

        Vector<LsEntry> listing = null;
        try {
            listing = channel.ls(path.getPath());
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            putChannel(channel);
        }

        return new SshDirectoryStream(path, filter, listing);
    }

    @Override
    public DirectoryStream<AbsolutePath> newDirectoryStream(AbsolutePath dir) throws OctopusIOException {
        return newDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER);
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(AbsolutePath dir) throws OctopusIOException {
        return newAttributesDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(AbsolutePath path, Filter filter)
            throws OctopusIOException {
        if (!isDirectory(path)) {
            throw new OctopusIOException(getClass().getName(), "Cannot create directorystream, file is not a directory");
        }

        ChannelSftp channel = getChannel(path);

        Vector<LsEntry> listing = null;
        try {
            listing = channel.ls(path.getPath());
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            putChannel(channel);
        }

        return new SshDirectoryAttributeStream(path, filter, listing);
    }

    @Override
    public InputStream newInputStream(AbsolutePath path) throws OctopusIOException {
        if (isDirectory(path)) {
            throw new OctopusIOException(getClass().getName(), "Cannot create input stream, path is a directory");
        }

        ChannelSftp channel = getChannel(path);

        try {
            InputStream in = channel.get(path.getPath());
            return new SshInputStream(in, channel);
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        }
    }

    // FIXME move to OpenOption
    private boolean contains(OpenOption toFind, OpenOption... options) {
        for (OpenOption curr : options) {
            if (curr == toFind) {
                return true;
            }
        }
        return false;
    }

    @Override
    public OutputStream newOutputStream(AbsolutePath path, OpenOption... options) throws OctopusIOException {
        if (exists(path) && isDirectory(path)) {
            throw new OctopusIOException(getClass().getName(), "Cannot create input stream, path is a directory");
        }

        if (contains(OpenOption.READ, options)) {
            throw new IllegalArgumentException("Cannot open an output stream for reading");
        }

        if (contains(OpenOption.CREATE_NEW, options) && exists(path)) {
            throw new FileAlreadyExistsException(getClass().getName(),
                    "Cannot create file, as it already exists, and you specified the CREATE_NEW option.");
        }

        int mode = ChannelSftp.OVERWRITE;

        if (contains(OpenOption.APPEND, options)) {
            mode = ChannelSftp.APPEND;
        }

        ChannelSftp channel = getChannel(path);

        try {
            OutputStream out = channel.put(path.getPath(), mode);
            return new SshOutputStream(out, channel);
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        }
    }

    @Override
    public SeekableByteChannel newByteChannel(AbsolutePath path, OpenOption... options) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileAttributes getAttributes(AbsolutePath path) throws OctopusIOException {
        ChannelSftp channel = getChannel(path);

        try {
            SftpATTRS a = channel.lstat(path.getPath());
            return new SshFileAttributes(a, path);
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            putChannel(channel);
        }
    }

    @Override
    public AbsolutePath readSymbolicLink(AbsolutePath path) throws OctopusIOException {
        ChannelSftp channel = getChannel(path);

        try {
            String target = channel.readlink(path.getPath());
            return new AbsolutePathImplementation(path.getFileSystem(), new RelativePath(target));
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            putChannel(channel);
        }
    }

    @Override
    public void setOwner(AbsolutePath path, String user, String group) throws OctopusIOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPosixFilePermissions(AbsolutePath path, Set<PosixFilePermission> permissions) throws OctopusIOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setFileTimes(AbsolutePath path, long lastModifiedTime, long lastAccessTime, long createTime)
            throws OctopusIOException {
        // TODO Auto-generated method stub

    }

    public void end() {
        logger.debug("end called, closing all file systems");
        while (fileSystems.size() > 0) {
            Set<String> keys = fileSystems.keySet();
            String first = keys.iterator().next();
            FileSystem fs = fileSystems.get(first).getImpl();

            try {
                close(fs);
            } catch (OctopusIOException | OctopusException e) {
                // ignore for now
            }
        }
    }

    @Override
    public FileSystem getLocalCWDFileSystem() throws OctopusException {
        throw new OctopusException(getClass().getName(), "getCWDFileSystem not supported!");
    }

    @Override
    public FileSystem getLocalHomeFileSystem() throws OctopusException {
        throw new OctopusException(getClass().getName(), "getLocalHomeFileSystem not supported!");
    }

    @Override
    public boolean isSymbolicLink(AbsolutePath path) throws OctopusIOException {
        throw new OctopusIOException(getClass().getName(), "isSymbolicLink not implemented!");
    }

    @Override
    public long size(AbsolutePath path) throws OctopusIOException {
        throw new OctopusIOException(getClass().getName(), "size not implemented!");
    }

    @Override
    public Copy copy(AbsolutePath source, AbsolutePath target, CopyOption... options) throws UnsupportedOperationException, OctopusIOException {
        throw new OctopusIOException(getClass().getName(), "copy not implemented!");
    }

    @Override
    public CopyStatus getCopyStatus(Copy copy) throws OctopusException, OctopusIOException {
        throw new OctopusIOException(getClass().getName(), "getCopyStatus not implemented!");
    }

    @Override
    public void cancelCopy(Copy copy) throws OctopusException, OctopusIOException {
        throw new OctopusIOException(getClass().getName(), "cancelCopy not implemented!");
    }
}
