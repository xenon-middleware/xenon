package nl.esciencecenter.octopus.adaptors.ssh;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.files.AbsolutePathImplementation;
import nl.esciencecenter.octopus.engine.files.FilesEngine;
import nl.esciencecenter.octopus.exceptions.FileAlreadyExistsException;
import nl.esciencecenter.octopus.exceptions.NoSuchFileException;
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

    private final OctopusEngine octopusEngine;
    private final SshAdaptor adaptor;
    private final Properties properties;

    public SshFiles(OctopusProperties properties, SshAdaptor sshAdaptor, OctopusEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
        this.adaptor = sshAdaptor;
        this.properties = properties;

        if (logger.isDebugEnabled()) {
            Set<String> attributeViews = FileSystems.getDefault().supportedFileAttributeViews();

            logger.debug(Arrays.toString(attributeViews.toArray()));
        }
    }

    private SshFileSystem getFileSystem(AbsolutePath path) {
        return (SshFileSystem) path.getFileSystem();
    }

    @Override
    public FileSystem newFileSystem(URI location, Credential credential, Properties properties) throws OctopusException,
            OctopusIOException {

        if (!location.getPath().equals("")) {
            throw new OctopusException(adaptor.getName(),
                    "Adaptor does not support a specific entry point. The location URI should not contain a path. URI = "
                            + location);
        }

        String uniqueID = getNewUniqueID();

        Session session = adaptor.createNewSession(uniqueID, location, credential);

        ChannelSftp channel;
        channel = SshFileSystem.getSftpChannel(session);

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

        return new SshFileSystem(adaptor.getName(), uniqueID, location, entryPath, credential, new OctopusProperties(properties),
                adaptor, session);
    }

    @Override
    public AbsolutePath newPath(FileSystem filesystem, RelativePath location) throws OctopusException, OctopusIOException {
        return new AbsolutePathImplementation(filesystem, location);
    }

    @Override
    public AbsolutePath newPath(FileSystem filesystem, RelativePath... locations) throws OctopusException, OctopusIOException {
        return new AbsolutePathImplementation(filesystem, locations);
    }

    @Override
    public void close(FileSystem filesystem) throws OctopusException, OctopusIOException {
        SshFileSystem fs = (SshFileSystem) filesystem;
        fs.close();
    }

    @Override
    public boolean isOpen(FileSystem filesystem) throws OctopusException, OctopusIOException {
        SshFileSystem fs = (SshFileSystem) filesystem;
        return fs.isOpen();
    }

    @Override
    public AbsolutePath copy(AbsolutePath source, AbsolutePath target, CopyOption... options) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbsolutePath createDirectory(AbsolutePath dir) throws OctopusIOException {
        if (exists(dir)) {
            throw new FileAlreadyExistsException(getClass().getName(), "Cannot create directory, as it already exists.");
        }

        ChannelSftp channel = getFileSystem(dir).getSftpChannel();

        try {
            channel.mkdir(dir.getPath());
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            getFileSystem(dir).putSftpChannel(channel);
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
        if (!exists(path)) {
            throw new NoSuchFileException(getClass().getName(), "cannot delete file, as it does not exist");
        }

        ChannelSftp channel = getFileSystem(path).getSftpChannel();

        try {
            if (isDirectory(path)) {
                channel.rmdir(path.getPath());
            } else {
                channel.rm(path.getPath());
            }
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            getFileSystem(path).putSftpChannel(channel);
        }
    }

    @Override
    public boolean exists(AbsolutePath path) throws OctopusIOException {
        ChannelSftp channel = getFileSystem(path).getSftpChannel();

        try {
            channel.lstat(path.getPath());
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return false;
            }

            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            getFileSystem(path).putSftpChannel(channel);
        }

        return true;
    }

    @Override
    public boolean isDirectory(AbsolutePath path) throws OctopusIOException {
        ChannelSftp channel = getFileSystem(path).getSftpChannel();

        try {
            SftpATTRS attributes = channel.lstat(path.getPath());
            return attributes.isDir();
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            getFileSystem(path).putSftpChannel(channel);
        }
    }

    @Override
    public AbsolutePath move(AbsolutePath source, AbsolutePath target, CopyOption... options) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirectoryStream<AbsolutePath> newDirectoryStream(AbsolutePath path, Filter filter) throws OctopusIOException {
        if (!isDirectory(path)) {
            throw new OctopusIOException(getClass().getName(), "Cannot create directorystream, file is not a directory");
        }

        ChannelSftp channel = getFileSystem(path).getSftpChannel();

        Vector<LsEntry> listing = null;
        try {
            listing = channel.ls(path.getPath());
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            getFileSystem(path).putSftpChannel(channel);
        }

        return new SshDirectoryStream(path, filter, listing);
    }

    @Override
    public DirectoryStream<AbsolutePath> newDirectoryStream(AbsolutePath dir) throws OctopusIOException {
        return newDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER);
    }

    @Override
    public DirectoryStream<PathAttributes> newAttributesDirectoryStream(AbsolutePath dir) throws OctopusIOException {
        return newAttributesDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirectoryStream<PathAttributes> newAttributesDirectoryStream(AbsolutePath path, Filter filter)
            throws OctopusIOException {
        if (!isDirectory(path)) {
            throw new OctopusIOException(getClass().getName(), "Cannot create directorystream, file is not a directory");
        }

        ChannelSftp channel = getFileSystem(path).getSftpChannel();

        Vector<LsEntry> listing = null;
        try {
            listing = channel.ls(path.getPath());
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            getFileSystem(path).putSftpChannel(channel);
        }

        return new SshDirectoryAttributeStream(path, filter, listing);
    }

    @Override
    public InputStream newInputStream(AbsolutePath path) throws OctopusIOException {
        if (isDirectory(path)) {
            throw new OctopusIOException(getClass().getName(), "Cannot create input stream, path is a directory");
        }

        ChannelSftp channel = getFileSystem(path).getSftpChannel();

        try {
            InputStream in = channel.get(path.getPath());
            return new SshInputStream(in, channel);
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        }
    }

    private boolean contains(OpenOption toFind, OpenOption... options) {
        for(OpenOption curr : options) {
            if(curr == toFind) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public OutputStream newOutputStream(AbsolutePath path, OpenOption... options) throws OctopusIOException {
        if (isDirectory(path)) {
            throw new OctopusIOException(getClass().getName(), "Cannot create input stream, path is a directory");
        }

        if(contains(OpenOption.READ, options)) {
            throw new IllegalArgumentException("Cannot open an output stream for reading");
        }

        if(contains(OpenOption.CREATE_NEW, options) && exists(path)) {
            throw new FileAlreadyExistsException(getClass().getName(), "Cannot create file, as it already exists, and you specified the CREATE_NEW option.");
        }

        boolean append = false;
        
        if(contains(OpenOption.APPEND, options)) {
            append = true;
        }
        
        ChannelSftp channel = getFileSystem(path).getSftpChannel();

        try {
            OutputStream out = channel.put(path.getPath());
            return new SshOutputStream(out, channel);
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        }
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
        ChannelSftp channel = getFileSystem(path).getSftpChannel();

        try {
            SftpATTRS a = channel.lstat(path.getPath());
            return new SshFileAttributes(a, path);
        } catch (SftpException e) {
            throw adaptor.sftpExceptionToOctopusException(e);
        } finally {
            getFileSystem(path).putSftpChannel(channel);
        }
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
        // TODO close all filesystems
    }
}
