package nl.esciencecenter.octopus.adaptors.ssh;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.files.AbsolutePathImplementation;
import nl.esciencecenter.octopus.engine.files.FileSystemImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.DirectoryStream.Filter;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.PathAttributes;
import nl.esciencecenter.octopus.files.PosixFilePermission;
import nl.esciencecenter.octopus.files.RelativePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpException;

public class SshFiles implements Files {
    private static final Logger logger = LoggerFactory.getLogger(SshFiles.class);

    private final OctopusEngine octopusEngine;
    private final SshAdaptor adaptor;
    private final Properties properties;
    private static int currentID = 1;
    
    private static synchronized String getNewUniqueID() {
        String res = "ssh" + currentID; 
        currentID++;
        return res;
    }
    
    public SshFiles(OctopusProperties properties, SshAdaptor sshAdaptor, OctopusEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
        this.adaptor = sshAdaptor;
        this.properties = properties;
        
        if (logger.isDebugEnabled()) {
            Set<String> attributeViews = FileSystems.getDefault().supportedFileAttributeViews();

            logger.debug(Arrays.toString(attributeViews.toArray()));
        }
    }

    protected static PathAttributes convertAttributes(LsEntry entry) {
        return null; // TODO
    }
    
    @Override
    public FileSystem newFileSystem(URI location, Credential credential, Properties properties) throws OctopusException,
            OctopusIOException {
        return new FileSystemImplementation(adaptor.getName(), getNewUniqueID(), location, credential, new OctopusProperties(properties));
    }

    @Override
    public AbsolutePath newPath(FileSystem filesystem, RelativePath location) throws OctopusException, OctopusIOException {
        return new AbsolutePathImplementation(filesystem, location);
    }

    @Override
    public AbsolutePath newPath(FileSystem filesystem, RelativePath... locations) throws OctopusException, OctopusIOException {
        // TODO
        return null;
    }

    @Override
    public void close(FileSystem filesystem) throws OctopusException, OctopusIOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isOpen(FileSystem filesystem) throws OctopusException, OctopusIOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public AbsolutePath copy(AbsolutePath source, AbsolutePath target, CopyOption... options) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbsolutePath createDirectories(AbsolutePath dir) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbsolutePath createDirectory(AbsolutePath dir) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbsolutePath createFile(AbsolutePath path) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbsolutePath createSymbolicLink(AbsolutePath link, AbsolutePath target) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delete(AbsolutePath path) throws OctopusIOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean exists(AbsolutePath path) throws OctopusIOException { // TODO more specific exception, octopus really is couldnotinitcredential
        ChannelSftp channel;
        try {
            channel = adaptor.getSftpChannel((FileSystemImplementation)path.getFileSystem());
        } catch (OctopusException e) { // TODO more specific exception types
            throw new OctopusIOException(adaptor.getName(), e.getMessage(), e);
        }

        try {
            channel.lstat(path.getPath());
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return false;
            }

            throw adaptor.sftpExceptionToOctopusException(e);
        }

        return true;
    }

    
    @Override
    public boolean isDirectory(AbsolutePath path) throws OctopusIOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public AbsolutePath move(AbsolutePath source, AbsolutePath target, CopyOption... options) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DirectoryStream<AbsolutePath> newDirectoryStream(AbsolutePath dir) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DirectoryStream<AbsolutePath> newDirectoryStream(AbsolutePath dir, Filter filter) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DirectoryStream<PathAttributes> newAttributesDirectoryStream(AbsolutePath dir) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DirectoryStream<PathAttributes> newAttributesDirectoryStream(AbsolutePath dir, Filter filter)
            throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream newInputStream(AbsolutePath path) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OutputStream newOutputStream(AbsolutePath path, OpenOption... options) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SeekableByteChannel newByteChannel(AbsolutePath path, Set<PosixFilePermission> permissions, OpenOption... options)
            throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SeekableByteChannel newByteChannel(AbsolutePath path, OpenOption... options) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileAttributes getAttributes(AbsolutePath path) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbsolutePath readSymbolicLink(AbsolutePath link) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
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
        
    }
        
    /*
    
    // TODO close channels
    

    @Override
    public Path createDirectory(Path dir, Set<PosixFilePermission> permissions) throws OctopusIOException {
        if (exists(dir)) {
            throw new FileAlreadyExistsException(getClass().getName(), "Cannot create directory, as it already exists.");
        }

        ChannelSftp channel;
        try {
            channel = adaptor.getSftpChannel(dir.toUri());
        } catch (OctopusException e) { // TODO more specific exception types
            throw new OctopusIOException(adaptor.getName(), e.getMessage(), e);
        }

        try {
            channel.mkdir(dir.getPath());
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        }

        return dir;
    }


    @Override
    public void delete(Path path, DeleteOption... options) throws OctopusIOException {
        if (!exists(path)) {
            throw new NoSuchFileException(getClass().getName(), "cannot delete file, as it does not exist");
        }

        // recursion step
        if (isDirectory(path) && DeleteOption.contains(options, DeleteOption.RECURSIVE)) {
            for (Path child : newDirectoryStream(path, FilesEngine.ACCEPT_ALL_FILTER)) {
                delete(child, options);
            }
        }

        ChannelSftp channel;
        try {
            channel = adaptor.getSftpChannel(path.toUri());
        } catch (OctopusException e) { // TODO more specific exception types
            throw new OctopusIOException(adaptor.getName(), e.getMessage(), e);
        }

        try {
            if (isDirectory(path)) {
                channel.rmdir(path.getPath());
            } else {
                channel.rm(path.getPath());
            }
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        }
    }



    @Override
    public boolean isDirectory(Path path) throws OctopusIOException {
        ChannelSftp channel;
        try {
            channel = adaptor.getSftpChannel(path.toUri());
        } catch (OctopusException e) { // TODO more specific exception types
            throw new OctopusIOException(adaptor.getName(), e.getMessage(), e);
        }

        try {
            SftpATTRS attributes = channel.lstat(path.getPath());
            return attributes.isDir();
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter filter) throws OctopusIOException {
        if (!isDirectory(dir)) {
            throw new OctopusIOException(getClass().getName(), "Cannot create directorystream, file is not a directory");
        }
        
        ChannelSftp channel;
        try {
            channel = adaptor.getSftpChannel(dir.toUri());
        } catch (OctopusException e) { // TODO more specific exception types
            throw new OctopusIOException(adaptor.getName(), e.getMessage(), e);
        }

        Vector<LsEntry> listing = null;
        try {
            listing = channel.ls(dir.getPath());
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        }
        
        return new SshDirectoryStream(dir, filter, listing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirectoryStream<PathAttributes> newAttributesDirectoryStream(Path dir, Filter filter) throws OctopusIOException {
        if (!isDirectory(dir)) {
            throw new OctopusIOException(getClass().getName(), "Cannot create directorystream, file is not a directory");
        }
        
        ChannelSftp channel;
        try {
            channel = adaptor.getSftpChannel(dir.toUri());
        } catch (OctopusException e) { // TODO more specific exception types
            throw new OctopusIOException(adaptor.getName(), e.getMessage(), e);
        }

        Vector<LsEntry> listing = null;
        try {
            listing = channel.ls(dir.getPath());
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        }
        
        return new SshDirectoryAttributeStream(dir, filter, listing);
    }

    @Override
    public InputStream newInputStream(Path path) throws OctopusIOException {
        if (isDirectory(path)) {
            throw new OctopusIOException(getClass().getName(), "Cannot create input stream, path is a directory");
        }
        
        ChannelSftp channel;
        try {
            channel = adaptor.getSftpChannel(path.toUri());
        } catch (OctopusException e) { // TODO more specific exception types
            throw new OctopusIOException(adaptor.getName(), e.getMessage(), e);
        }

        try {
            InputStream in = channel.get(path.getPath());
            return in;
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        }
    }


    
    */
}
