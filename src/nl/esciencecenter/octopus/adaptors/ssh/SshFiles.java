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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.octopus.OctopusPropertyDescription.Component;
import nl.esciencecenter.octopus.adaptors.local.LocalAdaptor;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.files.PathImplementation;
import nl.esciencecenter.octopus.engine.files.FileSystemImplementation;
import nl.esciencecenter.octopus.engine.files.FilesEngine;
import nl.esciencecenter.octopus.engine.util.CopyEngine;
import nl.esciencecenter.octopus.engine.util.CopyInfo;
import nl.esciencecenter.octopus.engine.util.OpenOptions;
import nl.esciencecenter.octopus.exceptions.DirectoryNotEmptyException;
import nl.esciencecenter.octopus.exceptions.FileAlreadyExistsException;
import nl.esciencecenter.octopus.exceptions.InvalidOpenOptionsException;
import nl.esciencecenter.octopus.exceptions.NoSuchFileException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.UnsupportedOperationException;
import nl.esciencecenter.octopus.files.Path;
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
import nl.esciencecenter.octopus.files.Pathname;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

public class SshFiles implements Files {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshFiles.class);

    private static int currentID = 1;

    private static synchronized String getNewUniqueID() {
        String res = "ssh" + currentID;
        currentID++;
        return res;
    }

    /**
     * Used to store all state attached to a filesystem. This way, FileSystemImplementation is immutable.
     */
    static class FileSystemInfo {
        private final FileSystemImplementation impl;
        private final SshMultiplexedSession session;

        public FileSystemInfo(FileSystemImplementation impl, SshMultiplexedSession session) {
            super();
            this.impl = impl;
            this.session = session;
        }

        public FileSystemImplementation getImpl() {
            return impl;
        }

        public SshMultiplexedSession getSession() {
            return session;
        }
    }

    private final OctopusEngine octopusEngine;
    private final SshAdaptor adaptor;

    private Map<String, FileSystemInfo> fileSystems = Collections.synchronizedMap(new HashMap<String, FileSystemInfo>());

    public SshFiles(SshAdaptor sshAdaptor, OctopusEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
        this.adaptor = sshAdaptor;
    }

    private void checkParent(Path path) throws OctopusIOException {
        
        Pathname parentName = path.getPathname().getParent();
        
        if (parentName == null) { 
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Parent directory does not exist!");
        }
        
        Path parent = newPath(path.getFileSystem(), parentName);
            
        if (!exists(parent)) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Parent directory " + parent + " does not exist!");
        }
    }
    
    
    protected FileSystem newFileSystem(SshMultiplexedSession session, String scheme, String location, Credential credential,
            OctopusProperties properties) throws OctopusException, OctopusIOException {

        String uniqueID = getNewUniqueID();

        ChannelSftp channel = session.getSftpChannel();

        String wd = null;

        try {
            wd = channel.pwd();
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            session.disconnect();
            throw adaptor.sftpExceptionToOctopusException(e);
        }

        session.releaseSftpChannel(channel);

        Pathname entryPath = new Pathname(wd);

        LOGGER.debug("remote cwd = " + wd + ", entryPath = " + entryPath);

        FileSystemImplementation result = new FileSystemImplementation(SshAdaptor.ADAPTOR_NAME, uniqueID, scheme, location, 
                entryPath, credential, properties);

        fileSystems.put(uniqueID, new FileSystemInfo(result, session));

        return result;
    }

    @Override
    public FileSystem newFileSystem(String scheme, String location, Credential credential, Map<String, String> properties) 
            throws OctopusException, OctopusIOException {

        SshLocation sshLocation = SshLocation.parse(location);
        
        OctopusProperties octopusProperties = new OctopusProperties(adaptor.getSupportedProperties(Component.FILESYSTEM), 
                properties);

        SshMultiplexedSession session = adaptor.createNewSession(sshLocation, credential, octopusProperties);

        return newFileSystem(session, scheme, location, credential, octopusProperties);
    }

    private SshMultiplexedSession getSession(Path path) throws OctopusIOException {

        FileSystemImplementation fs = (FileSystemImplementation) path.getFileSystem();
        FileSystemInfo info = fileSystems.get(fs.getUniqueID());

        if (info == null) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "File system is already closed");
        }

        return info.getSession();
    }

    @Override
    public Path newPath(FileSystem filesystem, Pathname location) {
        return new PathImplementation(filesystem, location);
    }

    @Override
    public void close(FileSystem filesystem) throws OctopusException, OctopusIOException {
        FileSystemImplementation fs = (FileSystemImplementation) filesystem;

        FileSystemInfo info = fileSystems.remove(fs.getUniqueID());

        if (info == null) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "file system is already closed");
        }

        info.getSession().disconnect();
    }

    @Override
    public boolean isOpen(FileSystem filesystem) throws OctopusException, OctopusIOException {
        FileSystemImplementation fs = (FileSystemImplementation) filesystem;
        return (fileSystems.get(fs.getUniqueID()) != null);
    }

    @Override
    public Path createDirectory(Path dir) throws OctopusIOException {

        if (exists(dir)) {
            throw new FileAlreadyExistsException(SshAdaptor.ADAPTOR_NAME, "Directory " + dir + " already exists!");
        }

        checkParent(dir);

        SshMultiplexedSession session = getSession(dir);
        ChannelSftp channel = session.getSftpChannel();

        try {
            channel.mkdir(dir.getPathname().getAbsolutePath());
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            throw adaptor.sftpExceptionToOctopusException(e);
        }

        session.releaseSftpChannel(channel);
        return dir;
    }

    @Override
    public Path createDirectories(Path dir) throws OctopusIOException {

        if (exists(dir)) {
            throw new FileAlreadyExistsException(SshAdaptor.ADAPTOR_NAME, "Directory " + dir + " already exists!");
        }

        Iterator<Pathname> itt = dir.getPathname().iterator();

        while (itt.hasNext()) {
            Path tmp = newPath(dir.getFileSystem(), itt.next());
            
            if (!exists(tmp)) {
                createDirectory(tmp);
            }
        }

        return dir;
    }

    @Override
    public Path createFile(Path path) throws OctopusIOException {

        if (exists(path)) {
            throw new FileAlreadyExistsException(SshAdaptor.ADAPTOR_NAME, "File " + path + " already exists!");
        }

        checkParent(path);

        OutputStream out = null;

        try {
            out = newOutputStream(path, OpenOption.CREATE, OpenOption.WRITE, OpenOption.APPEND);
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
    public void delete(Path path) throws OctopusIOException {

        if (!exists(path)) {
            throw new NoSuchFileException(getClass().getName(), "Cannot delete file, as it does not exist");
        }

        SshMultiplexedSession session = getSession(path);
        ChannelSftp channel = session.getSftpChannel();

        FileAttributes att = getAttributes(path);

        try {
            if (att.isDirectory()) {
                if (newDirectoryStream(path, FilesEngine.ACCEPT_ALL_FILTER).iterator().hasNext()) {
                    throw new DirectoryNotEmptyException(SshAdaptor.ADAPTOR_NAME, "cannot delete dir " + path
                            + " as it is not empty");
                }

                channel.rmdir(path.getPathname().getAbsolutePath());
            } else {
                channel.rm(path.getPathname().getAbsolutePath());
            }
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            throw adaptor.sftpExceptionToOctopusException(e);
        }

        session.releaseSftpChannel(channel);
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
    @Override
    public Path move(Path source, Path target) throws OctopusIOException {

        FileSystem sourcefs = source.getFileSystem();
        FileSystem targetfs = target.getFileSystem();

        if (!sourcefs.getLocation().equals(targetfs.getLocation())) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Cannot move between different FileSystems: "
                    + sourcefs.getLocation() + " and " + targetfs.getLocation());
        }

        if (!exists(source)) {
            throw new NoSuchFileException(SshAdaptor.ADAPTOR_NAME, "Source " + source + " does not exist!");
        }

        Pathname sourceName = source.getPathname().normalize();
        Pathname targetName = target.getPathname().normalize();
        
        if (sourceName.equals(targetName)) {
            return target;
        }

        if (exists(target)) {
            throw new FileAlreadyExistsException(SshAdaptor.ADAPTOR_NAME, "Target " + target + " already exists!");
        }

        checkParent(target);

        // Ok, here, we just have a local move
        SshMultiplexedSession session = getSession(target);
        ChannelSftp channel = session.getSftpChannel();

        try {
            LOGGER.debug("move from " + source + " to " + target);
            channel.rename(source.getPathname().getAbsolutePath(), target.getPathname().getAbsolutePath());
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            throw adaptor.sftpExceptionToOctopusException(e);
        }

        session.releaseSftpChannel(channel);
        return target;
    }

    @SuppressWarnings("unchecked")
    private List<LsEntry> listDirectory(Path path, Filter filter) throws OctopusIOException {

        FileAttributes att = getAttributes(path);

        if (!att.isDirectory()) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "File is not a directory.");
        }

        if (filter == null) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Filter is null.");
        }

        SshMultiplexedSession session = getSession(path);
        ChannelSftp channel = session.getSftpChannel();

        List<LsEntry> result = null;

        try {
            result = channel.ls(path.getPathname().getAbsolutePath());
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            throw adaptor.sftpExceptionToOctopusException(e);
        }

        session.releaseSftpChannel(channel);
        return result;
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path path, Filter filter)
            throws OctopusIOException {
        return new SshDirectoryAttributeStream(path, filter, listDirectory(path, filter));
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path path, Filter filter) throws OctopusIOException {
        return new SshDirectoryStream(path, filter, listDirectory(path, filter));
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir) throws OctopusIOException {
        return newDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER);
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir) throws OctopusIOException {
        return newAttributesDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER);
    }

    @Override
    public InputStream newInputStream(Path path) throws OctopusIOException {

        if (!exists(path)) {
            throw new NoSuchFileException(SshAdaptor.ADAPTOR_NAME, "File " + path + " does not exist!");
        }

        FileAttributes att = getAttributes(path);

        if (att.isDirectory()) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Path " + path + " is a directory!");
        }

        SshMultiplexedSession session = getSession(path);
        ChannelSftp channel = session.getSftpChannel();

        try {
            InputStream in = channel.get(path.getPathname().getAbsolutePath());
            return new SshInputStream(in, session, channel);
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            throw adaptor.sftpExceptionToOctopusException(e);
        }
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws OctopusIOException {

        OpenOptions tmp = OpenOptions.processOptions(SshAdaptor.ADAPTOR_NAME, options);

        if (tmp.getReadMode() != null) {
            throw new InvalidOpenOptionsException(SshAdaptor.ADAPTOR_NAME, "Disallowed open option: READ");
        }

        if (tmp.getAppendMode() == null) {
            throw new InvalidOpenOptionsException(SshAdaptor.ADAPTOR_NAME, "No append mode provided!");
        }

        if (tmp.getWriteMode() == null) {
            tmp.setWriteMode(OpenOption.WRITE);
        }

        if (tmp.getOpenMode() == OpenOption.CREATE) {
            if (exists(path)) {
                throw new FileAlreadyExistsException(SshAdaptor.ADAPTOR_NAME, "File already exists: " + path);
            }
        } else if (tmp.getOpenMode() == OpenOption.OPEN) {
            if (!exists(path)) {
                throw new NoSuchFileException(SshAdaptor.ADAPTOR_NAME, "File does not exist: " + path);
            }
        }

        int mode = ChannelSftp.OVERWRITE;

        if (OpenOption.contains(OpenOption.APPEND, options)) {
            mode = ChannelSftp.APPEND;
        }

        SshMultiplexedSession session = getSession(path);
        ChannelSftp channel = session.getSftpChannel();

        try {
            OutputStream out = channel.put(path.getPathname().getAbsolutePath(), mode);
            return new SshOutputStream(out, session, channel);
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            throw adaptor.sftpExceptionToOctopusException(e);
        }
    }

    @Override
    public Path readSymbolicLink(Path path) throws OctopusIOException {

        SshMultiplexedSession session = getSession(path);
        ChannelSftp channel = session.getSftpChannel();

        Path result = null;

        try {
            String target = channel.readlink(path.getPathname().getAbsolutePath());

            if (!target.startsWith(File.separator)) {                
                Pathname parent = path.getPathname().getParent();
                result = new PathImplementation(path.getFileSystem(), parent.resolve(target));
            } else {
                result = new PathImplementation(path.getFileSystem(), new Pathname(target));
            }
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            throw adaptor.sftpExceptionToOctopusException(e);
        }

        session.releaseSftpChannel(channel);
        return result;
    }

    public void end() {
        LOGGER.debug("end called, closing all file systems");
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
    public Path getLocalCWD() throws OctopusException { 
        throw new OctopusException(SshAdaptor.ADAPTOR_NAME, "getLocalCWD not supported!");
    }

    @Override
    public Path getLocalHome() throws OctopusException {
        throw new OctopusException(SshAdaptor.ADAPTOR_NAME, "getLocalHome not supported!");
    }
    
    @Override
    public FileSystem [] getLocalFileSystems() throws OctopusException { 
        throw new OctopusException(SshAdaptor.ADAPTOR_NAME, "getLocalFileSystems not supported!");
    }
    
//    @Override
//    public FileSystem getLocalCWDFileSystem() throws OctopusException {
//        throw new OctopusException(getClass().getName(), "getCWDFileSystem not supported!");
//    }
//
//    @Override
//    public FileSystem getLocalHomeFileSystem() throws OctopusException {
//        throw new OctopusException(getClass().getName(), "getLocalHomeFileSystem not supported!");
//    }

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws OctopusIOException {

        SshMultiplexedSession session = getSession(path);
        ChannelSftp channel = session.getSftpChannel();

        try {
            channel.chmod(SshUtil.permissionsToBits(permissions), path.getPathname().getAbsolutePath());
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            throw adaptor.sftpExceptionToOctopusException(e);
        }

        session.releaseSftpChannel(channel);
    }

    private SftpATTRS stat(Path path) throws OctopusIOException {

        SshMultiplexedSession session = getSession(path);
        ChannelSftp channel = session.getSftpChannel();

        SftpATTRS result = null;

        try {
            result = channel.lstat(path.getPathname().getAbsolutePath());
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            throw adaptor.sftpExceptionToOctopusException(e);
        }

        session.releaseSftpChannel(channel);
        return result;
    }

    @Override
    public FileAttributes getAttributes(Path path) throws OctopusIOException {
        return new SshFileAttributes(stat(path), path);
    }

    @Override
    public boolean exists(Path path) throws OctopusIOException {
        try {
            stat(path);
            return true;
        } catch (NoSuchFileException e) {
            return false;
        }
    }
    
    @Override
    public Copy copy(Path source, Path target, CopyOption... options) throws OctopusIOException,
            UnsupportedOperationException {

        CopyEngine ce = octopusEngine.getCopyEngine();
        
        CopyInfo info = CopyInfo.createCopyInfo(SshAdaptor.ADAPTOR_NAME, ce.getNextID("SSH_COPY_"), source, target, options);
         
        ce.copy(info);

        if (info.isAsync()) {
            return info.getCopy();
        } else {

            Exception e = info.getException();

            if (e != null) {
                throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Copy failed!", e);
            }

            return null;
        }
    }

    @Override
    public CopyStatus getCopyStatus(Copy copy) throws OctopusException, OctopusIOException {
        return octopusEngine.getCopyEngine().getStatus(copy);
    }

    @Override
    public CopyStatus cancelCopy(Copy copy) throws OctopusException, OctopusIOException {
        return octopusEngine.getCopyEngine().cancel(copy);
    }
}
