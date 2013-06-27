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
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import nl.esciencecenter.octopus.adaptors.local.LocalAdaptor;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.files.AbsolutePathImplementation;
import nl.esciencecenter.octopus.engine.files.CopyImplementation;
import nl.esciencecenter.octopus.engine.files.FileSystemImplementation;
import nl.esciencecenter.octopus.engine.files.FilesEngine;
import nl.esciencecenter.octopus.engine.util.CopyEngine;
import nl.esciencecenter.octopus.engine.util.CopyInfo;
import nl.esciencecenter.octopus.engine.util.OpenOptions;
import nl.esciencecenter.octopus.exceptions.DirectoryNotEmptyException;
import nl.esciencecenter.octopus.exceptions.FileAlreadyExistsException;
import nl.esciencecenter.octopus.exceptions.IllegalSourcePathException;
import nl.esciencecenter.octopus.exceptions.InvalidOpenOptionsException;
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
import com.jcraft.jsch.JSchException;
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

    private Map<String, FileSystemInfo> fileSystems = Collections.synchronizedMap(new HashMap<String, FileSystemInfo>());

    public SshFiles(OctopusProperties properties, SshAdaptor sshAdaptor, OctopusEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
        this.adaptor = sshAdaptor;
        this.properties = properties;

        if (logger.isDebugEnabled()) {
            Set<String> attributeViews = FileSystems.getDefault().supportedFileAttributeViews();

            logger.debug(Arrays.toString(attributeViews.toArray()));
        }
    }
    
    public FileSystem newFileSystem(Session session, URI location, Credential credential, OctopusProperties properties) 
            throws OctopusException, OctopusIOException {
        
        String uniqueID = getNewUniqueID();

        ChannelSftp channel = getSftpChannel(session);

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
                new FileSystemImplementation(SshAdaptor.ADAPTOR_NAME, uniqueID, location, entryPath, credential, properties);
      
        fileSystems.put(uniqueID, new FileSystemInfo(result, session));
        
        return result;        
    }
    
    @Override
    public FileSystem newFileSystem(URI location, Credential credential, Properties properties) throws OctopusException,
            OctopusIOException {
     
        adaptor.checkPath(location, "filesystem");
        
        OctopusProperties octopusProperties = new OctopusProperties(properties);
        
        Session session = adaptor.createNewSession(location, credential, octopusProperties);

        return newFileSystem(session, location, credential, octopusProperties);
    }

    private ChannelSftp getChannel(AbsolutePath path) throws OctopusIOException {
 
        FileSystem fileSystem = path.getFileSystem();
        
        if (!fileSystem.getAdaptorName().equals(SshAdaptor.ADAPTOR_NAME)) {
            throw new OctopusRuntimeException(SshAdaptor.ADAPTOR_NAME, "Illegal Filesystem type: " + fileSystem.getAdaptorName());
        }
        
        FileSystemImplementation fs = (FileSystemImplementation) fileSystem;
        FileSystemInfo info = fileSystems.get(fs.getUniqueID());
        
        if (info == null) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "File system is already closed");
        }

        return getSftpChannel(info.getSession());
    }
    
    /**
     * Get a connected channel for doing sftp operations.
     * 
     * @param session
     *            The authenticated session.
     * @return the channel
     * @throws OctopusIOException
     */
    private ChannelSftp getSftpChannel(Session session) throws OctopusIOException {
        try {
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            return channel;
        } catch (JSchException e) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }
    }

    @Override
    public AbsolutePath newPath(FileSystem filesystem, RelativePath location) throws OctopusException, OctopusIOException {
        return new AbsolutePathImplementation(filesystem, location);
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
//    public AbsolutePath copy(AbsolutePath source, AbsolutePath target) throws OctopusIOException {
//
//        if (!octopusEngine.files().exists(source)) {
//            throw new NoSuchFileException(SshAdaptor.ADAPTOR_NAME, "Source " + source.getPath() + " does not exist!");
//        }
//
//        if (octopusEngine.files().isDirectory(source)) {
//            throw new IllegalSourcePathException(SshAdaptor.ADAPTOR_NAME, "Source " + source.getPath() + " is a directory");
//        }
//
//        if (octopusEngine.files().exists(target)) {
//            throw new FileAlreadyExistsException(SshAdaptor.ADAPTOR_NAME, "Target " + target.getPath() + " already exists!");
//        }
//
//        if (!octopusEngine.files().exists(target.getParent())) {
//            throw new NoSuchFileException(SshAdaptor.ADAPTOR_NAME, "Target directory " + target.getParent().getPath()
//                    + " does not exist!");
//        }
//
//        if (source.normalize().equals(target.normalize())) {
//            return target;
//        }
//
//        logger.debug("ssh copy");
//        // two cases: remote -> local and local -> remote
//
//        FileSystem sourcefs = source.getFileSystem();
//        FileSystem targetfs = target.getFileSystem();
//
//        if (source.isLocal()) {
//            if (target.isLocal()) {
//                throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Cannot copy local files.");
//            }
//
//            if (!targetfs.getAdaptorName().equals("ssh")) {
//                throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME,
//                        "Can only copy between local and an ssh locations (or vice-versa).");
//            }
//
//            // Ok, here, source is local, target is ssh.
//
//            ChannelSftp channel = getChannel(target);
//
//            try {
//                logger.debug("copy from " + source.getPath() + " to " + target.getPath());
//                channel.put(source.getPath(), target.getPath());
//            } catch (SftpException e) {
//                throw adaptor.sftpExceptionToOctopusException(e);
//            } finally {
//                channel.disconnect();
//            }
//        } else if (target.isLocal()) {
//            if (source.isLocal()) {
//                throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Cannot copy local files.");
//            }
//
//            if (!sourcefs.getAdaptorName().equals("ssh")) {
//                throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME,
//                        "Can only copy between local and an ssh locations (or vice-versa).");
//            }
//
//            // Ok, here, target is local, source is ssh.
//
//            ChannelSftp channel = getChannel(source);
//
//            try {
//                logger.debug("copy from " + source.getPath() + " to " + target.getPath());
//                channel.get(source.getPath(), target.getPath());
//            } catch (SftpException e) {
//                throw adaptor.sftpExceptionToOctopusException(e);
//            } finally {
//                channel.disconnect();
//            }
//        } else {
//            // both remote
//            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Cannot copy files between two different remote filesystems!");
//        }
//
//        return target;
//    }

    @Override
    public AbsolutePath createDirectory(AbsolutePath dir) throws OctopusIOException {
        
        if (exists(dir)) {
            throw new FileAlreadyExistsException(SshAdaptor.ADAPTOR_NAME, "Directory " + dir.getPath() + " already exists!");
        }

        if (!exists(dir.getParent())) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Parent directory " + dir.getParent() + " does not exist!");
        }
        
//        if (exists(dir)) {
//            throw new FileAlreadyExistsException(getClass().getName(), "Cannot create directory, as it already exists.");
//        }

        ChannelSftp channel = getChannel(dir);

        try {
            channel.mkdir(dir.getPath());
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            channel.disconnect();
        }

        return dir;
    }

    @Override
    public AbsolutePath createDirectories(AbsolutePath dir) throws OctopusIOException {

        if (exists(dir)) {
            throw new FileAlreadyExistsException(SshAdaptor.ADAPTOR_NAME, "Directory " + dir.getPath() + " already exists!");
        }

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

        if (exists(path)) {
            throw new FileAlreadyExistsException(SshAdaptor.ADAPTOR_NAME, "File " + path.getPath() + " already exists!");
        }

        if (!exists(path.getParent())) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Parent directory " + path.getParent() + " does not exist!");
        }
        
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
    public void delete(AbsolutePath path) throws OctopusIOException {
        
        if (!exists(path)) {
            throw new NoSuchFileException(getClass().getName(), "Cannot delete file, as it does not exist");
        }

        ChannelSftp channel = getChannel(path);

        try {
            if (isDirectory(path)) {
                if (newDirectoryStream(path, FilesEngine.ACCEPT_ALL_FILTER).iterator().hasNext()) {
                    throw new DirectoryNotEmptyException(SshAdaptor.ADAPTOR_NAME, "cannot delete dir " + path + " as it is not empty");
                }

                channel.rmdir(path.getPath());
            } else {
                channel.rm(path.getPath());
            }
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            channel.disconnect();
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
                throw new NoSuchFileException(SshAdaptor.ADAPTOR_NAME, "Source " + source.getPath() + " does not exist!");
            }
            
            if (exists(target)) { 
                throw new FileAlreadyExistsException(SshAdaptor.ADAPTOR_NAME, "Target " + target.getPath() + " already exists!");
            }
            
            if (!exists(target.getParent())) { 
                throw new NoSuchFileException(SshAdaptor.ADAPTOR_NAME, "Target directory " + target.getParent().getPath() + 
                        " does not exist!");
            }
            
            if (source.normalize().equals(target.normalize())) {
                return target;
            }

            try {
                Files.move(LocalUtils.javaPath(source), LocalUtils.javaPath(target)); 
            } catch (IOException e) {
                throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Failed to move " + source.getPath() + " to " + 
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
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Can only move within one remote ssh location.");
        }

        if (!sourcefs.getUri().getHost().equals(targetfs.getUri().getHost())) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Cannot move between different sites: " 
                    + sourcefs.getUri().getHost() + " and " + targetfs.getUri().getHost());
        }

        if (!exists(source)) {
            throw new NoSuchFileException(SshAdaptor.ADAPTOR_NAME, "Source " + source.getPath() + " does not exist!");
        }

        if (source.normalize().equals(target.normalize())) {
            return target;
        }

        if (exists(target)) {
            throw new FileAlreadyExistsException(SshAdaptor.ADAPTOR_NAME, "Target " + target.getPath() + " already exists!");
        }

        if (!exists(target.getParent())) {
            throw new NoSuchFileException(SshAdaptor.ADAPTOR_NAME, "Target directory " + target.getParent().getPath()
                    + " does not exist!");
        }
        
        // Ok, here, we just have a local move

        ChannelSftp channel = getChannel(target);

        try {
            logger.debug("move from " + source.getPath() + " to " + target.getPath());
            channel.rename(source.getPath(), target.getPath());
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            channel.disconnect();
        }
        return target;
    }
    
    @SuppressWarnings("unchecked")
    private Vector<LsEntry> listDirectory(AbsolutePath path, Filter filter) throws OctopusIOException { 

        if (!isDirectory(path)) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "File is not a directory.");
        }

        if (filter == null) { 
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Filter is null.");
        }

        ChannelSftp channel = getChannel(path);

        try {
            return channel.ls(path.getPath());
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            channel.disconnect();
        }
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(AbsolutePath path, Filter filter)
            throws OctopusIOException {
        return new SshDirectoryAttributeStream(path, filter, listDirectory(path, filter));
    }
    
    @Override
    public DirectoryStream<AbsolutePath> newDirectoryStream(AbsolutePath path, Filter filter) throws OctopusIOException {
        return new SshDirectoryStream(path, filter, listDirectory(path, filter));
    }

    @Override
    public DirectoryStream<AbsolutePath> newDirectoryStream(AbsolutePath dir) throws OctopusIOException {
        return newDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER);
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(AbsolutePath dir) throws OctopusIOException {
        return newAttributesDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER);
    }
    
    @Override
    public InputStream newInputStream(AbsolutePath path) throws OctopusIOException {
       
        if (!exists(path)) {
            throw new NoSuchFileException(SshAdaptor.ADAPTOR_NAME, "File " + path.getPath() + " does not exist!");
        }

        if (isDirectory(path)) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "Path " + path.getPath() + " is a directory!");
        }

        ChannelSftp channel = getChannel(path);

        try {
            InputStream in = channel.get(path.getPath());
            return new SshInputStream(in, channel);
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        }
    }

    @Override
    public OutputStream newOutputStream(AbsolutePath path, OpenOption... options) throws OctopusIOException {
        
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
                throw new FileAlreadyExistsException(SshAdaptor.ADAPTOR_NAME, "File already exists: " + path.getPath());
            }
        } else if (tmp.getOpenMode() == OpenOption.OPEN) { 
            if (!exists(path)) { 
                throw new NoSuchFileException(SshAdaptor.ADAPTOR_NAME, "File does not exist: " + path.getPath());
            }
        }
//        
//        
//        if (exists(path) && isDirectory(path)) {
//            throw new OctopusIOException(getClass().getName(), "Cannot create input stream, path is a directory");
//        }
//
//        if (OpenOption.contains(OpenOption.READ, options)) {
//            throw new IllegalArgumentException("Cannot open an output stream for reading");
//        }
//
//        if (OpenOption.contains(OpenOption.CREATE, options) && exists(path)) {
//            throw new FileAlreadyExistsException(getClass().getName(),
//                    "Cannot create file, as it already exists, and you specified the CREATE option.");
//        }

        int mode = ChannelSftp.OVERWRITE;

        if (OpenOption.contains(OpenOption.APPEND, options)) {
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
    public AbsolutePath readSymbolicLink(AbsolutePath path) throws OctopusIOException {
        ChannelSftp channel = getChannel(path);

        try {
            String target = channel.readlink(path.getPath());
            
            if (!target.startsWith(File.separator)) { 
                return path.getParent().resolve(new RelativePath(target));
            } else { 
                return new AbsolutePathImplementation(path.getFileSystem(), new RelativePath(target));
            }
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            channel.disconnect();
        }
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
    public void setPosixFilePermissions(AbsolutePath path, Set<PosixFilePermission> permissions) throws OctopusIOException {
        
        ChannelSftp channel = getChannel(path);
        
        try { 
            channel.chmod(SshUtil.permissionsToBits(permissions), path.getPath());
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            channel.disconnect();
        }
    }
        
    @Override
    public SeekableByteChannel newByteChannel(AbsolutePath path, OpenOption... options) throws OctopusIOException {
        throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "newByteChannel not implemented!");
    }
    
    private SftpATTRS stat(AbsolutePath path) throws OctopusIOException {
        
        ChannelSftp channel = getChannel(path);
      
        try { 
            return channel.lstat(path.getPath());
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            channel.disconnect();
        }
    } 
    
    @Override
    public FileAttributes getAttributes(AbsolutePath path) throws OctopusIOException {
        return new SshFileAttributes(stat(path), path);
    }
    
    @Override
    public boolean isSymbolicLink(AbsolutePath path) throws OctopusIOException {
        try { 
            SftpATTRS s = stat(path);
            System.err.println("SSH isLink " + path.getPath() + " " + s.getPermissionsString() + " " + s.isLink());
            return s.isLink();
        } catch (NoSuchFileException e) {
            // We should return false if the operation fails.
            System.err.println("SSH isLink " + path.getPath() + " FAILED");
            return false;
        }
    }

    @Override
    public long size(AbsolutePath path) throws OctopusIOException {
        
        SftpATTRS tmp = stat(path);
        
        if (tmp.isDir() || tmp.isLink()) { 
            return 0;
        } else { 
            return tmp.getSize();
        }
    }

    @Override
    public boolean isDirectory(AbsolutePath path) throws OctopusIOException {
        try { 
            return stat(path).isDir();
        } catch (NoSuchFileException e) {
            // We should return false if the operation fails. 
            return false;
        }
    }
    
    @Override
    public boolean exists(AbsolutePath path) throws OctopusIOException {
        try { 
            stat(path);
            return true;
        } catch (NoSuchFileException e) { 
            return false;
        }
    }
    
    @Override
    public Copy copy(AbsolutePath source, AbsolutePath target, CopyOption... options) 
            throws UnsupportedOperationException, OctopusIOException {

        boolean async = false;
        boolean verify = false;
        
        CopyOption mode = null;
        
        for (CopyOption opt : options) {             
            switch (opt) { 
            case CREATE:
                if (mode != null && mode != opt) { 
                    throw new UnsupportedOperationException(SshAdaptor.ADAPTOR_NAME, "Conflicting copy options: " + mode 
                            + " and CREATE");
                }
                
                mode = opt;
                break;                
            case REPLACE:
                if (mode != null && mode != opt) { 
                    throw new UnsupportedOperationException(SshAdaptor.ADAPTOR_NAME, "Conflicting copy options: " + mode 
                            + " and REPLACE");
                }
                
                mode = opt;
                break;                
            case APPEND:
                if (mode != null && mode != opt) { 
                    throw new UnsupportedOperationException(SshAdaptor.ADAPTOR_NAME, "Conflicting copy options: " + mode 
                            + " and APPEND");
                }
                
                mode = opt;
                break;                
            case RESUME:
                if (mode != null && mode != opt) { 
                    throw new UnsupportedOperationException(SshAdaptor.ADAPTOR_NAME, "Conflicting copy options: " + mode 
                            + " and RESUME");
                }
                
                mode = opt;
                break;     
            case IGNORE:
                if (mode != null && mode != opt) { 
                    throw new UnsupportedOperationException(SshAdaptor.ADAPTOR_NAME, "Conflicting copy options: " + mode 
                            + " and RESUME");
                }
                
                mode = opt;
                break;
            case VERIFY:
                verify = true;
                break;                
            case ASYNCHRONOUS:
                async = true;
                break;                
            }
        }
        
        if (mode == null) { 
            mode = CopyOption.CREATE;
        }
        
        if (verify && mode != CopyOption.RESUME) { 
            throw new UnsupportedOperationException(SshAdaptor.ADAPTOR_NAME, "Conflicting copy options: " + mode 
                            + " and VERIFY");
        }
        
        CopyEngine ce = octopusEngine.getCopyEngine();
        CopyImplementation copy = new CopyImplementation(SshAdaptor.ADAPTOR_NAME, ce.getNextID("SSH_COPY_"), source, target);
        CopyInfo info = new CopyInfo(copy, mode, verify);
        ce.copy(info, async);
        
        if (async) { 
            return copy;
        } else { 
            
            Exception e = info.getException();
            
            if (e != null) { 
                throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Copy failed!", e);
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
