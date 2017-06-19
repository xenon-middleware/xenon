/**
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
package nl.esciencecenter.xenon.adaptors.file.sftp;

import static nl.esciencecenter.xenon.adaptors.file.sftp.SftpProperties.ADAPTOR_DESCRIPTION;
import static nl.esciencecenter.xenon.adaptors.file.sftp.SftpProperties.ADAPTOR_LOCATIONS;
import static nl.esciencecenter.xenon.adaptors.file.sftp.SftpProperties.ADAPTOR_NAME;
import static nl.esciencecenter.xenon.adaptors.file.sftp.SftpProperties.ADAPTOR_SCHEME;
import static nl.esciencecenter.xenon.adaptors.file.sftp.SftpProperties.VALID_PROPERTIES;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.client.subsystem.sftp.SftpClient;
import org.apache.sshd.common.subsystem.sftp.SftpConstants;
import org.apache.sshd.common.subsystem.sftp.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.ConnectionLostException;
import nl.esciencecenter.xenon.NotConnectedException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.files.FileAdaptor;
import nl.esciencecenter.xenon.engine.files.FileSystemImplementation;
import nl.esciencecenter.xenon.engine.files.FilesEngine;
import nl.esciencecenter.xenon.engine.files.PathImplementation;
import nl.esciencecenter.xenon.engine.util.OpenOptions;
import nl.esciencecenter.xenon.engine.util.PosixFileUtils;
import nl.esciencecenter.xenon.files.DirectoryNotEmptyException;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.EndOfFileException;
import nl.esciencecenter.xenon.files.DirectoryStream.Filter;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.InvalidAttributeException;
import nl.esciencecenter.xenon.files.InvalidOptionsException;
import nl.esciencecenter.xenon.files.InvalidPathException;
import nl.esciencecenter.xenon.files.NoSpaceException;
import nl.esciencecenter.xenon.files.NoSuchPathException;
import nl.esciencecenter.xenon.files.OpenOption;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PathAlreadyExistsException;
import nl.esciencecenter.xenon.files.PathAttributesPair;
import nl.esciencecenter.xenon.files.PermissionDeniedException;
import nl.esciencecenter.xenon.files.PosixFilePermission;
import nl.esciencecenter.xenon.files.RelativePath;
import nl.esciencecenter.xenon.files.UnsupportedIOOperationException;


//import com.jcraft.jsch.ChannelSftp;
//import com.jcraft.jsch.ChannelSftp.LsEntry;
//import com.jcraft.jsch.SftpATTRS;
//import com.jcraft.jsch.SftpException;

public class SftpFiles extends FileAdaptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SftpFiles.class);

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
        private final SftpClient client;

        public FileSystemInfo(FileSystemImplementation impl, SftpClient client) {
        	super();
        	this.impl = impl;
        	this.client = client;
        }

        public FileSystemImplementation getImpl() {
        	return impl;
        }

        public SftpClient getClient() {
        	return client;
        }

//      private final SshMultiplexedSession session;
//      
//      public FileSystemInfo(FileSystemImplementation impl, SshMultiplexedSession session) {
//          super();
//          this.impl = impl;
//          this.session = session;
//      }
//
//        public SshMultiplexedSession getSession() {
//            return session;
//        }
    }

//    static void close(Closeable stream, SshMultiplexedSession session, ChannelSftp channel, String name) throws IOException {
//        
//        IOException tmp = null;
//        
//        try {
//            // First attempt to close the in stream.
//            stream.close();
//        } catch (IOException e) {
//            tmp = new IOException("Failed to close the SSH " + name + " stream!", e);
//        } 
//        
//        try { 
//            // Next, attempt to release the channel, even if in failed to close. 
//            session.releaseSftpChannel(channel);
//        } catch (XenonException e) { 
//            if (tmp == null) {  
//                tmp = new IOException("Failed to release SSH channel!", e);
//            }
//        }
//        
//        if (tmp != null) { 
//            // throw the first exception we encountered, if any
//            throw tmp;
//        }
//    }
    
    private final Map<String, FileSystemInfo> fileSystems = Collections.synchronizedMap(new HashMap<String, FileSystemInfo>());
    
    public SftpFiles(FilesEngine engine, Map<String, String> properties) throws XenonException { 
    		
    	super(engine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME, ADAPTOR_LOCATIONS, VALID_PROPERTIES,
                    new XenonProperties(VALID_PROPERTIES, properties));
    	
    	
    	
    	
    
    }

    private void checkParent(Path path) throws XenonException {
        
        RelativePath parentName = path.getRelativePath().getParent();
        
        if (parentName == null) { 
            throw new XenonException(ADAPTOR_NAME, "Parent directory does not exist!");
        }
        
        Path parent = newPath(path.getFileSystem(), parentName);
            
        if (!exists(parent)) {
            throw new XenonException(ADAPTOR_NAME, "Parent directory " + parent + " does not exist!");
        }
    }
    
    /*
    protected FileSystem newFileSystem(SshMultiplexedSession session, String scheme, String location, Credential credential,
            XenonProperties properties) throws XenonException {       
        
    	
    	
    	
    	
    	String uniqueID = getNewUniqueID();

        LOGGER.debug("* newFileSystem scheme = {} location = {} credential = {} properties = {}", scheme, location, credential, 
                properties);
        
        ChannelSftp channel = session.getSftpChannel();

        String wd;

        try {
            wd = channel.pwd();
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            session.disconnect();
            throw SshUtil.sftpExceptionToXenonException(e);
        }

        session.releaseSftpChannel(channel);

        RelativePath entryPath = new RelativePath(wd);
        
        FileSystemImplementation result = new FileSystemImplementation(ADAPTOR_NAME, uniqueID, scheme, location, 
                entryPath, credential, properties);

        fileSystems.put(uniqueID, new FileSystemInfo(result, session));

        LOGGER.debug("* newFileSystem OK remote cwd = {} entryPath = {} uniqueID = {}", wd, entryPath, uniqueID);
        
        return result;
    }
	*/

    // TODO: generalize again??
    /*
    private URI parse(String location) throws InvalidLocationException { 
    	
    	URI uri; 
    		
    	if (location == null) {
    		throw new InvalidLocationException(ADAPTOR_NAME, "location may not be null.");
        }
    	  
    	try {
    		uri = new URI(location);
    	} catch (URISyntaxException e) {
            throw new InvalidLocationException(ADAPTOR_NAME, "Could not parse location " + location, e);
        }
      
    	if (!"sftp".equals(uri.getScheme())) { 
    	    throw new InvalidLocationException(ADAPTOR_NAME, "Location has invalid scheme " + uri.getScheme());
		}
    
    	String host = uri.getHost();
    	
    	if (host == null) {
    		throw new InvalidLocationException(ADAPTOR_NAME, "Could not extract host from location");
    	}
    	
    	return uri;	
    }
    */
    
    
    @Override
    public FileSystem newFileSystem(String location, Credential credential, Map<String, String> properties) throws XenonException {

        LOGGER.debug("newFileSystem scheme = SFTP location = {} credential = {} properties = {}", location, credential, properties);
        
        XenonProperties xp = new XenonProperties(SftpProperties.VALID_PROPERTIES, properties);
        
        boolean loadKnownHosts = xp.getBooleanProperty(SftpProperties.LOAD_STANDARD_KNOWN_HOSTS);
        boolean loadSSHConfig = xp.getBooleanProperty(SftpProperties.LOAD_SSH_CONFIG);
        boolean useSSHAgent = xp.getBooleanProperty(SftpProperties.AGENT);
        boolean useAgentForwarding = xp.getBooleanProperty(SftpProperties.AGENT_FORWARDING);
        
        SshClient client = SSHUtil.createSSHClient(loadKnownHosts, loadSSHConfig, useSSHAgent, useAgentForwarding);
     
        long timeout = xp.getLongProperty(SftpProperties.CONNECTION_TIMEOUT);
        
        ClientSession session = SSHUtil.connect(ADAPTOR_NAME, client, location, credential, timeout);
        
//        SftpFileSystem fs = (SftpFileSystem) session.createSftpFileSystem(); 
//        
        SftpClient sftpClient = null;
        
        try { 
        	sftpClient = session.createSftpClient();
        } catch (IOException e) {
        	client.close(true);
        	throw new XenonException(ADAPTOR_NAME, "Failed to create SFTP session", e);
        }
        
        String wd = null;
        
        try {
        	wd = sftpClient.canonicalPath(".");
		} catch (IOException e) {
			client.close(true);
        	throw new XenonException(ADAPTOR_NAME, "Failed to create retrieve working directory", e);
        }
        
        RelativePath entryPath = new RelativePath(wd);
        
        String uniqueID = getNewUniqueID();
        
        // TODO: fix hardcoded scheme!
        FileSystemImplementation result = new FileSystemImplementation(ADAPTOR_NAME, uniqueID, "sftp", location, 
                entryPath, credential, xp);

        fileSystems.put(uniqueID, new FileSystemInfo(result, sftpClient));

        LOGGER.debug("* newFileSystem OK remote cwd = {} entryPath = {} uniqueID = {}", wd, entryPath, uniqueID);
        
        return result;
        
//        
//        
//        
//        SshLocation sshLocation = SshLocation.parse(location, adaptor.getSshConfig());
//        
//        
//        
//        
//        
//        SshMultiplexedSession session = adaptor.createNewSession(sshLocation, credential, xenonProperties);
//
//        return newFileSystem(session, scheme, location, credential, xenonProperties);
    }

//    private SshMultiplexedSession getSession(Path path) throws XenonException {
//
//        FileSystemImplementation fs = (FileSystemImplementation) path.getFileSystem();
//        FileSystemInfo info = fileSystems.get(fs.getUniqueID());
//
//        if (info == null) {
//            throw new XenonException(ADAPTOR_NAME, "File system is already closed");
//        }
//
//        return info.getSession();
//    }

    private SftpClient getSftpClient(Path path) throws XenonException { 

    	FileSystemImplementation fs = (FileSystemImplementation) path.getFileSystem();
        FileSystemInfo info = fileSystems.get(fs.getUniqueID());

        if (info == null) {
            throw new XenonException(ADAPTOR_NAME, "File system is already closed");
        }

        return info.getClient();
    }    

        
    @Override
    public Path newPath(FileSystem filesystem, RelativePath location) {
        return new PathImplementation(filesystem, location);
    }

    @Override
    public void close(FileSystem filesystem) throws XenonException {
        
        LOGGER.debug("close fileSystem = {}", filesystem);
        
        FileSystemImplementation fs = (FileSystemImplementation) filesystem;

        FileSystemInfo info = fileSystems.remove(fs.getUniqueID());

        if (info == null) {
            throw new XenonException(ADAPTOR_NAME, "file system is already closed");
        }

        try {
			info.getClient().close();
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to close sftp client", e);
		}
        
        //info.getSession().disconnect();
        
        LOGGER.debug("close OK");        
    }

    @Override
    public boolean isOpen(FileSystem filesystem) throws XenonException {
        
        LOGGER.debug("isOpen fileSystem = {}", filesystem);
        
        FileSystemImplementation fs = (FileSystemImplementation) filesystem;
        boolean result = (fileSystems.get(fs.getUniqueID()) != null);
        
        LOGGER.debug("isOpen OK result = {}", result);
        
        return result;        
    }

    @Override
    public void createDirectory(Path dir) throws XenonException {

        LOGGER.debug("createDirectory dir = {}", dir);
        
        if (exists(dir)) {
            throw new PathAlreadyExistsException(ADAPTOR_NAME, "Directory " + dir + " already exists!");
        }

        checkParent(dir);

        //SshMultiplexedSession session = getSession(dir);
        //ChannelSftp channel = session.getSftpChannel();
        
        SftpClient client = getSftpClient(dir);
        
        try {
        	client.mkdir(dir.getRelativePath().getAbsolutePath());
        	// channel.mkdir(dir.getRelativePath().getAbsolutePath());
        } catch (IOException e) {
//            session.failedSftpChannel(channel);
//            throw SshUtil.sftpExceptionToXenonException(e);
//        }
        	throw new XenonException(ADAPTOR_NAME, "Failed to mkdir", e);
        }

        //session.releaseSftpChannel(channel);
        
        LOGGER.debug("createDirectory OK");        
    }

    
//    @Override
//    public void createDirectories(Path dir) throws XenonException {
//        
//        LOGGER.debug("createDirectories dir = {}", dir);
//        
//        if (exists(dir)) {
//            throw new PathAlreadyExistsException(ADAPTOR_NAME, "Directory " + dir + " already exists!");
//        }
//
//        for (RelativePath superDirectory : dir.getRelativePath()) {
//            Path tmp = newPath(dir.getFileSystem(), superDirectory);
//
//            if (!exists(tmp)) {
//                createDirectory(tmp);
//            }
//        }
//        
//        LOGGER.debug("createDirectories OK");
//    }

    @Override
    public void createFile(Path path) throws XenonException {

        LOGGER.debug("createFile path = {}", path);
        
        if (exists(path)) {
            throw new PathAlreadyExistsException(ADAPTOR_NAME, "File " + path + " already exists!");
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
        
        LOGGER.debug("createFile OK");
    }

    @Override
    public void delete(Path path) throws XenonException {

        LOGGER.debug("delete path = {}", path);
        
        if (!exists(path)) {
            throw new NoSuchPathException(getClass().getName(), "Cannot delete file, as it does not exist");
        }

        //SshMultiplexedSession session = getSession(path);
        //ChannelSftp channel = session.getSftpChannel();
        
        SftpClient client = getSftpClient(path);
        
        FileAttributes att = getAttributes(path);

        try {
            if (att.isDirectory()) {
                if (newDirectoryStream(path, FilesEngine.ACCEPT_ALL_FILTER).iterator().hasNext()) {
                    throw new DirectoryNotEmptyException(ADAPTOR_NAME, "cannot delete dir " + path
                            + " as it is not empty");
                }

                client.rmdir(path.getRelativePath().getAbsolutePath());
//                channel.rmdir(path.getRelativePath().getAbsolutePath());
            } else {
            	client.rmdir(path.getRelativePath().getAbsolutePath());
//                channel.rm(path.getRelativePath().getAbsolutePath());
            }
        } catch (IOException e) {
//            session.failedSftpChannel(channel);
//            throw SshUtil.sftpExceptionToXenonException(e);
        	throw new XenonException(ADAPTOR_NAME, "Failed to remove " + path, e);
        }

//        session.releaseSftpChannel(channel);
        
        LOGGER.debug("delete OK");
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
     * 
     * @throws NoSuchPathException
     *             If the source file does not exist or the target parent directory does not exist.
     * @throws PathAlreadyExistsException
     *             If the target file already exists.
     * @throws XenonException
     *             If the move failed.
     */
    @Override
    public void move(Path source, Path target) throws XenonException {

        LOGGER.debug("move source = {} target = {}", source, target);
        
        FileSystem sourcefs = source.getFileSystem();
        FileSystem targetfs = target.getFileSystem();

        if (!sourcefs.getLocation().equals(targetfs.getLocation())) {
            throw new XenonException(ADAPTOR_NAME, "Cannot move between different FileSystems: "
                    + sourcefs.getLocation() + " and " + targetfs.getLocation());
        }

        if (!exists(source)) {
            throw new NoSuchPathException(ADAPTOR_NAME, "Source " + source + " does not exist!");
        }

        RelativePath sourceName = source.getRelativePath().normalize();
        RelativePath targetName = target.getRelativePath().normalize();
        
        if (sourceName.equals(targetName)) {
            return;
        }

        if (exists(target)) {
            throw new PathAlreadyExistsException(ADAPTOR_NAME, "Target " + target + " already exists!");
        }

        checkParent(target);

        // Ok, here, we just have a local move
//        SshMultiplexedSession session = getSession(target);
//        ChannelSftp channel = session.getSftpChannel();
//        
        SftpClient client = getSftpClient(target);
        
        try {
            LOGGER.debug("move from " + source + " to " + target);
            
            client.rename(source.getRelativePath().getAbsolutePath(), target.getRelativePath().getAbsolutePath());
//            channel.rename(source.getRelativePath().getAbsolutePath(), target.getRelativePath().getAbsolutePath());
        } catch (IOException e) {
//            session.failedSftpChannel(channel);
//            throw SshUtil.sftpExceptionToXenonException(e);
        	throw new XenonException(ADAPTOR_NAME, "Failed to rename path", e);
        }

//        session.releaseSftpChannel(channel);
        
        LOGGER.debug("move OK");
    }

    @SuppressWarnings("unchecked")
    private List<SftpClient.DirEntry> listDirectory(Path path, Filter filter) throws XenonException {

        FileAttributes att = getAttributes(path);

        if (!att.isDirectory()) {
            throw new XenonException(ADAPTOR_NAME, "File is not a directory.");
        }

        if (filter == null) {
            throw new XenonException(ADAPTOR_NAME, "Filter is null.");
        }

//        SshMultiplexedSession session = getSession(path);
//        ChannelSftp channel = session.getSftpChannel();
//        
        SftpClient client = getSftpClient(path);
       
        
        
        
        List<SftpClient.DirEntry> result;

        try {

        	SftpClient.Handle handle = client.openDir(path.getRelativePath().getAbsolutePath());
        	result = client.readDir(handle);
        	
        	//result = channel.ls(path.getRelativePath().getAbsolutePath());
        } catch (IOException e) {
//            session.failedSftpChannel(channel);
//            throw SshUtil.sftpExceptionToXenonException(e);
//      
        	throw sftpExceptionToXenonException(e, "Failed to list directory " + path);
        }

        // session.releaseSftpChannel(channel);
        return result;
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path path, Filter filter) throws XenonException {
        LOGGER.debug("newAttributesDirectoryStream path = {} filter = <?>", path);
        return new SftpDirectoryAttributeStream(path, filter, listDirectory(path, filter));
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path path, Filter filter) throws XenonException {
        LOGGER.debug("newDirectoryStream path = {} filter = <?>", path);
        return new SftpDirectoryStream(path, filter, listDirectory(path, filter));
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir) throws XenonException {
        LOGGER.debug("newDirectoryStream path = {}", dir);
        return newDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER);
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir) throws XenonException {
        LOGGER.debug("newAttributesDirectoryStream path = {}", dir);
        return newAttributesDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER);
    }

    @Override
    public InputStream newInputStream(Path path) throws XenonException {

        LOGGER.debug("newInputStream path = {}", path);
        
        if (!exists(path)) {
            throw new NoSuchPathException(ADAPTOR_NAME, "File " + path + " does not exist!");
        }

        FileAttributes att = getAttributes(path);

        if (att.isDirectory()) {
            throw new XenonException(ADAPTOR_NAME, "Path " + path + " is a directory!");
        }

//        SshMultiplexedSession session = getSession(path);
//        ChannelSftp channel = session.getSftpChannel();
        
        SftpClient client = getSftpClient(path);
        
        InputStream in;
        
        try {
        	in = client.read(path.getRelativePath().getAbsolutePath());        	
//            in = channel.get(path.getRelativePath().getAbsolutePath());
        } catch (IOException e) {
//            session.failedSftpChannel(channel);
//            throw SshUtil.sftpExceptionToXenonException(e);
        	throw new XenonException(ADAPTOR_NAME, "Failed to open stream to read from " + path, e);
        }
        
        LOGGER.debug("newInputStream OK");

        return in;
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws XenonException {
        
        LOGGER.debug("newOutputStream path = {} option = {}", path, options);
        
        OpenOptions tmp = OpenOptions.processOptions(ADAPTOR_NAME, options);

        if (tmp.getReadMode() != null) {
            throw new InvalidOptionsException(ADAPTOR_NAME, "Disallowed open option: READ");
        }

        if (tmp.getAppendMode() == null) {
            throw new InvalidOptionsException(ADAPTOR_NAME, "No append mode provided!");
        }

        if (tmp.getWriteMode() == null) {
            tmp.setWriteMode(OpenOption.WRITE);
        }

        if (tmp.getOpenMode() == OpenOption.CREATE && exists(path)) {
            throw new PathAlreadyExistsException(ADAPTOR_NAME, "File already exists: " + path);
        } else if (tmp.getOpenMode() == OpenOption.OPEN && !exists(path)) {
            throw new NoSuchPathException(ADAPTOR_NAME, "File does not exist: " + path);
        }

        SftpClient.OpenMode mode = SftpClient.OpenMode.Truncate;

        if (OpenOption.APPEND.occursIn(options)) {
            mode = SftpClient.OpenMode.Append;
        }
        
//        SshMultiplexedSession session = getSession(path);
//        ChannelSftp channel = session.getSftpChannel();
        
        SftpClient client = getSftpClient(path);
        
        OutputStream out;
        
        try {
        	out = client.write(path.getRelativePath().getAbsolutePath(), mode);      	
            //out = channel.put(path.getRelativePath().getAbsolutePath(), mode);
//        } catch (SftpException e) {
//            session.failedSftpChannel(channel);
//            throw SshUtil.sftpExceptionToXenonException(e);
//        }
        } catch (IOException e) {
        	throw new XenonException(ADAPTOR_NAME, "Failed open stream to write to: " + path, e);
        }
       
        LOGGER.debug("newOutputStream OK");
        
        return out;
    }

    @Override
    public Path readSymbolicLink(Path path) throws XenonException {

        LOGGER.debug("readSymbolicLink path = {}", path);
       
//        SshMultiplexedSession session = getSession(path);
//        ChannelSftp channel = session.getSftpChannel();

        SftpClient client = getSftpClient(path);
                
        Path result;

        try {
        	String target = client.readLink(path.getRelativePath().getAbsolutePath());
            // String target = channel.readlink(path.getRelativePath().getAbsolutePath());

            if (!target.startsWith(File.separator)) {                
                RelativePath parent = path.getRelativePath().getParent();
                result = new PathImplementation(path.getFileSystem(), parent.resolve(target));
            } else {
                result = new PathImplementation(path.getFileSystem(), new RelativePath(target));
            }
//        } catch (SftpException e) {
//            session.failedSftpChannel(channel);
//            throw SshUtil.sftpExceptionToXenonException(e);
//        }
        } catch (IOException e) {
        	throw new XenonException(ADAPTOR_NAME, "Failed to read link: " + path, e);
        }
            
//        session.releaseSftpChannel(channel);
        
        LOGGER.debug("readSymbolicLink OK result = {}", result);

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
            } catch (XenonException e) {
                // ignore for now
            }
        }
        
        LOGGER.debug("end OK");
    }
    
    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {

        LOGGER.debug("setPosixFilePermissions path = {} permissions = {}", path, permissions);
        
//        SshMultiplexedSession session = getSession(path);
//        ChannelSftp channel = session.getSftpChannel();

        SftpClient client = getSftpClient(path);
        
        try {
        	SftpClient.Attributes a = client.stat(path.getRelativePath().getAbsolutePath());
        	a.setPermissions(PosixFileUtils.permissionsToBits(permissions));
        	client.setStat(path.getRelativePath().getAbsolutePath(), a);
        	// channel.chmod(PosixFileUtils.permissionsToBits(permissions), path.getRelativePath().getAbsolutePath());
        } catch (IOException e) {
            //session.failedSftpChannel(channel);
            //throw SshUtil.sftpExceptionToXenonException(e);
        	throw new XenonException(ADAPTOR_NAME, "Failed to set permissions on: " + path, e);
        }

        // session.releaseSftpChannel(channel);

        LOGGER.debug("setPosixFilePermissions OK");
    }

    /*
    private SftpATTRS stat(Path path) throws XenonException {

        LOGGER.debug("* stat path = {}", path);
        
        SshMultiplexedSession session = getSession(path);
        ChannelSftp channel = session.getSftpChannel();

        SftpATTRS result;

        try {
            result = channel.lstat(path.getRelativePath().getAbsolutePath());
        } catch (SftpException e) {
            session.failedSftpChannel(channel);
            throw SshUtil.sftpExceptionToXenonException(e);
        }

        session.releaseSftpChannel(channel);
        
        LOGGER.debug("* stat OK result = {}", result);
        
        return result;
    }
     */

    private static XenonException sftpExceptionToXenonException(IOException e, String message) {
    	
    	if (e instanceof SftpException) { 
    		SftpException x = (SftpException) e;
    		switch (x.getStatus()) { 

    		case SftpConstants.SSH_FX_EOF:
    			return new EndOfFileException(ADAPTOR_NAME, "Unexpected EOF", e);

    		case SftpConstants.SSH_FX_NO_SUCH_FILE:
    		case SftpConstants.SSH_FX_NO_SUCH_PATH:
    			return new NoSuchPathException(ADAPTOR_NAME, "Path does not exists", e);

    		case SftpConstants.SSH_FX_PERMISSION_DENIED:
    			return new PermissionDeniedException(ADAPTOR_NAME, "Permission denied", e);

    		case SftpConstants.SSH_FX_NO_CONNECTION:
    			return new NotConnectedException(ADAPTOR_NAME, "Not connected", e);

    		case SftpConstants.SSH_FX_CONNECTION_LOST:
    			return new ConnectionLostException(ADAPTOR_NAME, "Connection lost", e);

    		case SftpConstants.SSH_FX_OP_UNSUPPORTED:
    			return new UnsupportedIOOperationException(ADAPTOR_NAME, "Unsupported operation", e);

    		case SftpConstants.SSH_FX_FILE_ALREADY_EXISTS:
    			return new PathAlreadyExistsException(ADAPTOR_NAME, "Already exists", e);

    		case SftpConstants.SSH_FX_WRITE_PROTECT:
    			return new PermissionDeniedException(ADAPTOR_NAME, "Write protected", e);

    		case SftpConstants.SSH_FX_CANNOT_DELETE:
    			return new PermissionDeniedException(ADAPTOR_NAME, "Cannot delete", e);

    		case SftpConstants.SSH_FX_DELETE_PENDING:
    			return new PermissionDeniedException(ADAPTOR_NAME, "Delete pending", e);

    		case SftpConstants.SSH_FX_NO_MEDIA:
    		case SftpConstants.SSH_FX_NO_SPACE_ON_FILESYSTEM:
    			return new NoSpaceException(ADAPTOR_NAME, "No space on filesystem", e);

    		case SftpConstants.SSH_FX_QUOTA_EXCEEDED:
    			return new NoSpaceException(ADAPTOR_NAME, "Quota exceeded", e);

    		case SftpConstants.SSH_FX_FILE_CORRUPT:
    			return new InvalidPathException(ADAPTOR_NAME, "File corrupt", e);

    		case SftpConstants.SSH_FX_DIR_NOT_EMPTY:
    			return new InvalidPathException(ADAPTOR_NAME, "Directory not empty", e);

    		case SftpConstants.SSH_FX_NOT_A_DIRECTORY:
    			return new InvalidPathException(ADAPTOR_NAME, "Not a directory", e);

    		case SftpConstants.SSH_FX_INVALID_FILENAME:
    			return new InvalidPathException(ADAPTOR_NAME, "Invalid file name", e);

    		case SftpConstants.SSH_FX_LINK_LOOP:
    			return new InvalidPathException(ADAPTOR_NAME, "Link loop", e);

    		case SftpConstants.SSH_FX_FILE_IS_A_DIRECTORY:
    			return new InvalidPathException(ADAPTOR_NAME, "File is a directory", e);

    		case SftpConstants.SSH_FX_OWNER_INVALID:
    			return new InvalidAttributeException(ADAPTOR_NAME, "Invalid owner", e);

    		case SftpConstants.SSH_FX_GROUP_INVALID:
    			return new InvalidAttributeException(ADAPTOR_NAME, "Invalid group", e);


    		case SftpConstants.SSH_FX_INVALID_HANDLE:
    			return new XenonException(ADAPTOR_NAME, "Invalid handle", e);

    		case SftpConstants.SSH_FX_INVALID_PARAMETER:
    			return new XenonException(ADAPTOR_NAME, "Invalid parameter", e);

    		case SftpConstants.SSH_FX_LOCK_CONFLICT:
    		case SftpConstants.SSH_FX_BYTE_RANGE_LOCK_CONFLICT:
    		case SftpConstants.SSH_FX_BYTE_RANGE_LOCK_REFUSED:
    		case SftpConstants.SSH_FX_NO_MATCHING_BYTE_RANGE_LOCK:
    			return new XenonException(ADAPTOR_NAME, "Locking failed", e);

    		case SftpConstants.SSH_FX_UNKNOWN_PRINCIPAL:
    			return new XenonException(ADAPTOR_NAME, "Unknown principal", e);

    		case SftpConstants.SSH_FX_BAD_MESSAGE:
    			return new XenonException(ADAPTOR_NAME, "Malformed message", e);
    	
    		}
    		
    		// Fall through if we do not know the error
      	} 
 
    	return new XenonException(ADAPTOR_NAME, message, e);
    }
  
    private SftpClient.Attributes stat(Path path) throws XenonException {

        LOGGER.debug("* stat path = {}", path);
        
        SftpClient client = getSftpClient(path);
        
        SftpClient.Attributes result;

        try {
            result = client.lstat(path.getRelativePath().getAbsolutePath());
        } catch (IOException e) {
        	throw sftpExceptionToXenonException(e, "Failed to retrieve attributes from: " + path);
        }
  
        LOGGER.debug("* stat OK result = {}", result);
        
        return result;
    }

    @Override
    public FileAttributes getAttributes(Path path) throws XenonException {
        
        LOGGER.debug("getAttributes path = {}", path);
        return new SftpFileAttributes(stat(path), path);
    }

    @Override
    public boolean exists(Path path) throws XenonException {
        
        LOGGER.debug("exists path = {}", path);

        boolean result;
        
        try {
            stat(path);
            result = true;
        } catch (NoSuchPathException e) {
            result = false;
        }
        
        LOGGER.debug("exists OK result = {}", result);

        return result;
    }

	@Override
	public Map<String, String> getAdaptorSpecificInformation() {
		// TODO Auto-generated method stub
		return null;
	}

    /*
    @Override
    public Copy copy(Path source, Path target, CopyOption... options) throws XenonException {
    	
        LOGGER.debug("copy source = {} target = {} options = {}", source, target, options);
        
        CopyEngine ce = file.getCopyEngine();
        
        CopyInfo info = CopyInfo.createCopyInfo(ADAPTOR_NAME, ce.getNextID("SSH_COPY_"), source, target, options);
         
        ce.copy(info);

        Copy result;
        
        if (info.isAsync()) {
            result = info.getCopy();
        } else {

            Exception e = info.getException();

            if (e != null) {
                throw new XenonException(ADAPTOR_NAME, "Copy failed!", e);
            }

            result = null;
        }
        
        LOGGER.debug("copy OK result = {}", result);

        return result;
    }
    */

    /*
    @Override
    public CopyStatus getCopyStatus(Copy copy) throws XenonException {

        LOGGER.debug("getCopyStatus copy = {}", copy);

        CopyStatus result = xenonEngine.getCopyEngine().getStatus(copy);
        
        LOGGER.debug("getCopyStatus OK result = {}", result);
        
        return result;
    }

    @Override
    public CopyStatus cancelCopy(Copy copy) throws XenonException {

        LOGGER.debug("cancelCopy copy = {}", copy);
        
        CopyStatus result = xenonEngine.getCopyEngine().cancel(copy);
        
        LOGGER.debug("cancelCopy OK result = {}", result);
        
        return result;
    }
    */

}
