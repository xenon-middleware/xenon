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
package nl.esciencecenter.cobalt.adaptors.ssh;

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

import nl.esciencecenter.cobalt.CobaltException;
import nl.esciencecenter.cobalt.CobaltPropertyDescription.Component;
import nl.esciencecenter.cobalt.adaptors.local.LocalAdaptor;
import nl.esciencecenter.cobalt.credentials.Credential;
import nl.esciencecenter.cobalt.engine.CobaltEngine;
import nl.esciencecenter.cobalt.engine.CobaltProperties;
import nl.esciencecenter.cobalt.engine.files.FileSystemImplementation;
import nl.esciencecenter.cobalt.engine.files.FilesEngine;
import nl.esciencecenter.cobalt.engine.files.PathImplementation;
import nl.esciencecenter.cobalt.engine.util.CopyEngine;
import nl.esciencecenter.cobalt.engine.util.CopyInfo;
import nl.esciencecenter.cobalt.engine.util.OpenOptions;
import nl.esciencecenter.cobalt.files.Copy;
import nl.esciencecenter.cobalt.files.CopyOption;
import nl.esciencecenter.cobalt.files.CopyStatus;
import nl.esciencecenter.cobalt.files.DirectoryNotEmptyException;
import nl.esciencecenter.cobalt.files.DirectoryStream;
import nl.esciencecenter.cobalt.files.FileAttributes;
import nl.esciencecenter.cobalt.files.FileSystem;
import nl.esciencecenter.cobalt.files.Files;
import nl.esciencecenter.cobalt.files.InvalidOpenOptionsException;
import nl.esciencecenter.cobalt.files.NoSuchPathException;
import nl.esciencecenter.cobalt.files.OpenOption;
import nl.esciencecenter.cobalt.files.Path;
import nl.esciencecenter.cobalt.files.PathAlreadyExistsException;
import nl.esciencecenter.cobalt.files.PathAttributesPair;
import nl.esciencecenter.cobalt.files.PosixFilePermission;
import nl.esciencecenter.cobalt.files.RelativePath;
import nl.esciencecenter.cobalt.files.DirectoryStream.Filter;

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

    private final CobaltEngine cobaltEngine;
    private final SshAdaptor adaptor;

    private Map<String, FileSystemInfo> fileSystems = Collections.synchronizedMap(new HashMap<String, FileSystemInfo>());

    public SshFiles(SshAdaptor sshAdaptor, CobaltEngine cobaltEngine) {
        this.cobaltEngine = cobaltEngine;
        this.adaptor = sshAdaptor;
    }

    private void checkParent(Path path) throws CobaltException {
        
        RelativePath parentName = path.getRelativePath().getParent();
        
        if (parentName == null) { 
            throw new CobaltException(LocalAdaptor.ADAPTOR_NAME, "Parent directory does not exist!");
        }
        
        Path parent = newPath(path.getFileSystem(), parentName);
            
        if (!exists(parent)) {
            throw new CobaltException(LocalAdaptor.ADAPTOR_NAME, "Parent directory " + parent + " does not exist!");
        }
    }
    
    
    protected FileSystem newFileSystem(SshMultiplexedSession session, String scheme, String location, Credential credential,
            CobaltProperties properties) throws CobaltException {

        String uniqueID = getNewUniqueID();

        ChannelSftp channel = session.getSftpChannel();

        String wd = null;

        try {
            wd = channel.pwd();
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            session.disconnect();
            throw adaptor.sftpExceptionToCobaltException(e);
        }

        session.releaseSftpChannel(channel);

        RelativePath entryPath = new RelativePath(wd);

        LOGGER.debug("remote cwd = " + wd + ", entryPath = " + entryPath);

        FileSystemImplementation result = new FileSystemImplementation(SshAdaptor.ADAPTOR_NAME, uniqueID, scheme, location, 
                entryPath, credential, properties);

        fileSystems.put(uniqueID, new FileSystemInfo(result, session));

        return result;
    }

    @Override
    public FileSystem newFileSystem(String scheme, String location, Credential credential, Map<String, String> properties) 
            throws CobaltException {

        SshLocation sshLocation = SshLocation.parse(location);
        
        CobaltProperties cobaltProperties = new CobaltProperties(adaptor.getSupportedProperties(Component.FILESYSTEM), 
                properties);

        SshMultiplexedSession session = adaptor.createNewSession(sshLocation, credential, cobaltProperties);

        return newFileSystem(session, scheme, location, credential, cobaltProperties);
    }

    private SshMultiplexedSession getSession(Path path) throws CobaltException {

        FileSystemImplementation fs = (FileSystemImplementation) path.getFileSystem();
        FileSystemInfo info = fileSystems.get(fs.getUniqueID());

        if (info == null) {
            throw new CobaltException(SshAdaptor.ADAPTOR_NAME, "File system is already closed");
        }

        return info.getSession();
    }

    @Override
    public Path newPath(FileSystem filesystem, RelativePath location) {
        return new PathImplementation(filesystem, location);
    }

    @Override
    public void close(FileSystem filesystem) throws CobaltException {
        FileSystemImplementation fs = (FileSystemImplementation) filesystem;

        FileSystemInfo info = fileSystems.remove(fs.getUniqueID());

        if (info == null) {
            throw new CobaltException(SshAdaptor.ADAPTOR_NAME, "file system is already closed");
        }

        info.getSession().disconnect();
    }

    @Override
    public boolean isOpen(FileSystem filesystem) throws CobaltException {
        FileSystemImplementation fs = (FileSystemImplementation) filesystem;
        return (fileSystems.get(fs.getUniqueID()) != null);
    }

    @Override
    public void createDirectory(Path dir) throws CobaltException {

        if (exists(dir)) {
            throw new PathAlreadyExistsException(SshAdaptor.ADAPTOR_NAME, "Directory " + dir + " already exists!");
        }

        checkParent(dir);

        SshMultiplexedSession session = getSession(dir);
        ChannelSftp channel = session.getSftpChannel();

        try {
            channel.mkdir(dir.getRelativePath().getAbsolutePath());
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            throw adaptor.sftpExceptionToCobaltException(e);
        }

        session.releaseSftpChannel(channel);
    }

    @Override
    public void createDirectories(Path dir) throws CobaltException {

        if (exists(dir)) {
            throw new PathAlreadyExistsException(SshAdaptor.ADAPTOR_NAME, "Directory " + dir + " already exists!");
        }

        Iterator<RelativePath> itt = dir.getRelativePath().iterator();

        while (itt.hasNext()) {
            Path tmp = newPath(dir.getFileSystem(), itt.next());
            
            if (!exists(tmp)) {
                createDirectory(tmp);
            }
        }
    }

    @Override
    public void createFile(Path path) throws CobaltException {

        if (exists(path)) {
            throw new PathAlreadyExistsException(SshAdaptor.ADAPTOR_NAME, "File " + path + " already exists!");
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
    }

    @Override
    public void delete(Path path) throws CobaltException {

        if (!exists(path)) {
            throw new NoSuchPathException(getClass().getName(), "Cannot delete file, as it does not exist");
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

                channel.rmdir(path.getRelativePath().getAbsolutePath());
            } else {
                channel.rm(path.getRelativePath().getAbsolutePath());
            }
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            throw adaptor.sftpExceptionToCobaltException(e);
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
     * @throws NoSuchPathException
     *             If the source file does not exist or the target parent directory does not exist.
     * @throws PathAlreadyExistsException
     *             If the target file already exists.
     * @throws CobaltException
     *             If the move failed.
     */
    @Override
    public void move(Path source, Path target) throws CobaltException {

        FileSystem sourcefs = source.getFileSystem();
        FileSystem targetfs = target.getFileSystem();

        if (!sourcefs.getLocation().equals(targetfs.getLocation())) {
            throw new CobaltException(SshAdaptor.ADAPTOR_NAME, "Cannot move between different FileSystems: "
                    + sourcefs.getLocation() + " and " + targetfs.getLocation());
        }

        if (!exists(source)) {
            throw new NoSuchPathException(SshAdaptor.ADAPTOR_NAME, "Source " + source + " does not exist!");
        }

        RelativePath sourceName = source.getRelativePath().normalize();
        RelativePath targetName = target.getRelativePath().normalize();
        
        if (sourceName.equals(targetName)) {
            return;
        }

        if (exists(target)) {
            throw new PathAlreadyExistsException(SshAdaptor.ADAPTOR_NAME, "Target " + target + " already exists!");
        }

        checkParent(target);

        // Ok, here, we just have a local move
        SshMultiplexedSession session = getSession(target);
        ChannelSftp channel = session.getSftpChannel();

        try {
            LOGGER.debug("move from " + source + " to " + target);
            channel.rename(source.getRelativePath().getAbsolutePath(), target.getRelativePath().getAbsolutePath());
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            throw adaptor.sftpExceptionToCobaltException(e);
        }

        session.releaseSftpChannel(channel);
    }

    @SuppressWarnings("unchecked")
    private List<LsEntry> listDirectory(Path path, Filter filter) throws CobaltException {

        FileAttributes att = getAttributes(path);

        if (!att.isDirectory()) {
            throw new CobaltException(SshAdaptor.ADAPTOR_NAME, "File is not a directory.");
        }

        if (filter == null) {
            throw new CobaltException(SshAdaptor.ADAPTOR_NAME, "Filter is null.");
        }

        SshMultiplexedSession session = getSession(path);
        ChannelSftp channel = session.getSftpChannel();

        List<LsEntry> result = null;

        try {
            result = channel.ls(path.getRelativePath().getAbsolutePath());
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            throw adaptor.sftpExceptionToCobaltException(e);
        }

        session.releaseSftpChannel(channel);
        return result;
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path path, Filter filter)
            throws CobaltException {
        return new SshDirectoryAttributeStream(path, filter, listDirectory(path, filter));
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path path, Filter filter) throws CobaltException {
        return new SshDirectoryStream(path, filter, listDirectory(path, filter));
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir) throws CobaltException {
        return newDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER);
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir) throws CobaltException {
        return newAttributesDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER);
    }

    @Override
    public InputStream newInputStream(Path path) throws CobaltException {

        if (!exists(path)) {
            throw new NoSuchPathException(SshAdaptor.ADAPTOR_NAME, "File " + path + " does not exist!");
        }

        FileAttributes att = getAttributes(path);

        if (att.isDirectory()) {
            throw new CobaltException(SshAdaptor.ADAPTOR_NAME, "Path " + path + " is a directory!");
        }

        SshMultiplexedSession session = getSession(path);
        ChannelSftp channel = session.getSftpChannel();

        try {
            InputStream in = channel.get(path.getRelativePath().getAbsolutePath());
            return new SshInputStream(in, session, channel);
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            throw adaptor.sftpExceptionToCobaltException(e);
        }
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws CobaltException {

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
                throw new PathAlreadyExistsException(SshAdaptor.ADAPTOR_NAME, "File already exists: " + path);
            }
        } else if (tmp.getOpenMode() == OpenOption.OPEN) {
            if (!exists(path)) {
                throw new NoSuchPathException(SshAdaptor.ADAPTOR_NAME, "File does not exist: " + path);
            }
        }

        int mode = ChannelSftp.OVERWRITE;

        if (OpenOption.contains(OpenOption.APPEND, options)) {
            mode = ChannelSftp.APPEND;
        }

        SshMultiplexedSession session = getSession(path);
        ChannelSftp channel = session.getSftpChannel();

        try {
            OutputStream out = channel.put(path.getRelativePath().getAbsolutePath(), mode);
            return new SshOutputStream(out, session, channel);
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            throw adaptor.sftpExceptionToCobaltException(e);
        }
    }

    @Override
    public Path readSymbolicLink(Path path) throws CobaltException {

        SshMultiplexedSession session = getSession(path);
        ChannelSftp channel = session.getSftpChannel();

        Path result = null;

        try {
            String target = channel.readlink(path.getRelativePath().getAbsolutePath());

            if (!target.startsWith(File.separator)) {                
                RelativePath parent = path.getRelativePath().getParent();
                result = new PathImplementation(path.getFileSystem(), parent.resolve(target));
            } else {
                result = new PathImplementation(path.getFileSystem(), new RelativePath(target));
            }
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            throw adaptor.sftpExceptionToCobaltException(e);
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
            } catch (CobaltException e) {
                // ignore for now
            }
        }
    }
    
    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws CobaltException {

        SshMultiplexedSession session = getSession(path);
        ChannelSftp channel = session.getSftpChannel();

        try {
            channel.chmod(SshUtil.permissionsToBits(permissions), path.getRelativePath().getAbsolutePath());
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            throw adaptor.sftpExceptionToCobaltException(e);
        }

        session.releaseSftpChannel(channel);
    }

    private SftpATTRS stat(Path path) throws CobaltException {

        SshMultiplexedSession session = getSession(path);
        ChannelSftp channel = session.getSftpChannel();

        SftpATTRS result = null;

        try {
            result = channel.lstat(path.getRelativePath().getAbsolutePath());
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            throw adaptor.sftpExceptionToCobaltException(e);
        }

        session.releaseSftpChannel(channel);
        return result;
    }

    @Override
    public FileAttributes getAttributes(Path path) throws CobaltException {
        return new SshFileAttributes(stat(path), path);
    }

    @Override
    public boolean exists(Path path) throws CobaltException {
        try {
            stat(path);
            return true;
        } catch (NoSuchPathException e) {
            return false;
        }
    }
    
    @Override
    public Copy copy(Path source, Path target, CopyOption... options) throws CobaltException {

        CopyEngine ce = cobaltEngine.getCopyEngine();
        
        CopyInfo info = CopyInfo.createCopyInfo(SshAdaptor.ADAPTOR_NAME, ce.getNextID("SSH_COPY_"), source, target, options);
         
        ce.copy(info);

        if (info.isAsync()) {
            return info.getCopy();
        } else {

            Exception e = info.getException();

            if (e != null) {
                throw new CobaltException(SshAdaptor.ADAPTOR_NAME, "Copy failed!", e);
            }

            return null;
        }
    }

    @Override
    public CopyStatus getCopyStatus(Copy copy) throws CobaltException {
        return cobaltEngine.getCopyEngine().getStatus(copy);
    }

    @Override
    public CopyStatus cancelCopy(Copy copy) throws CobaltException {
        return cobaltEngine.getCopyEngine().cancel(copy);
    }
}
