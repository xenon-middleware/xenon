package nl.esciencecenter.octopus.adaptors.ssh;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import nl.esciencecenter.octopus.OctopusProperties;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.files.FilesAdaptor;
import nl.esciencecenter.octopus.engine.files.FilesEngine;
import nl.esciencecenter.octopus.engine.files.PathImplementation;
import nl.esciencecenter.octopus.exceptions.FileAlreadyExistsException;
import nl.esciencecenter.octopus.exceptions.NoSuchFileException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AclEntry;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.DeleteOption;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.DirectoryStream.Filter;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.PathAttributes;
import nl.esciencecenter.octopus.files.PosixFilePermission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

public class SshFiles implements FilesAdaptor {
    private static final Logger logger = LoggerFactory.getLogger(SshFiles.class);

    private final OctopusEngine octopusEngine;
    private final SshAdaptor adaptor;

    public SshFiles(OctopusProperties properties, SshAdaptor sshAdaptor, OctopusEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
        this.adaptor = sshAdaptor;

        if (logger.isDebugEnabled()) {
            Set<String> attributeViews = FileSystems.getDefault().supportedFileAttributeViews();

            logger.debug(Arrays.toString(attributeViews.toArray()));
        }
    }
    
    protected static PathAttributes convertAttributes(LsEntry lsEntry) {
        return null; // TODO, PathAttributes must be converted to generic impl first 
    }
    
    @Override
    public Path newPath(OctopusProperties properties, URI location) throws OctopusException {
        adaptor.checkURI(location);
        return new PathImplementation(properties, location, adaptor.getName(), octopusEngine);
    }

    
    // TODO close channels
    
    @Override
    public Path copy(Path source, Path target, CopyOption... options) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path createDirectories(Path dir, Set<PosixFilePermission> permissions) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

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
    public Path createFile(Path path, Set<PosixFilePermission> permissions) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path createSymbolicLink(Path link, Path target) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
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

// TODO will be removed
    @Override
    public boolean deleteIfExists(Path path, DeleteOption... options) throws OctopusIOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean exists(Path path) throws OctopusIOException { // TODO more specific exception, octopus really is couldnotinitcredential
        ChannelSftp channel;
        try {
            channel = adaptor.getSftpChannel(path.toUri());
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

    @Override
    public Path move(Path source, Path target, CopyOption... options) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
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

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<PosixFilePermission> permissions, OpenOption... options)
            throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileAttributes readAttributes(Path path) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path readSymbolicLink(Path link) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path setOwner(Path path, String owner, String group) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path setFileTimes(Path path, long lastModifiedTime, long lastAccessTime, long createTime) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAcl(Path path, List<AclEntry> acl) throws OctopusIOException {
        // TODO Auto-generated method stub

    }

    protected void end() {
    }
}
