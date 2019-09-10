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
package nl.esciencecenter.xenon.adaptors.filesystems.ftp;

import static nl.esciencecenter.xenon.adaptors.filesystems.ftp.FtpFileAdaptor.ADAPTOR_NAME;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.UnsupportedOperationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.NotConnectedException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.PathAttributesImplementation;
import nl.esciencecenter.xenon.adaptors.filesystems.TransferClientInputStream;
import nl.esciencecenter.xenon.adaptors.filesystems.TransferClientOutputStream;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.InvalidPathException;
import nl.esciencecenter.xenon.filesystems.NoSuchPathException;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.filesystems.PathAttributes;
import nl.esciencecenter.xenon.filesystems.PosixFilePermission;

public class FtpFileSystem extends FileSystem {

    private static final Logger LOGGER = LoggerFactory.getLogger(FtpFileSystem.class);

    private static final int[] PERMISSION_TYPES = { FTPFile.READ_PERMISSION, FTPFile.WRITE_PERMISSION, FTPFile.EXECUTE_PERMISSION };

    private static final int[] USER_TYPES = { FTPFile.USER_ACCESS, FTPFile.GROUP_ACCESS, FTPFile.WORLD_ACCESS };

    private final FTPClient ftpClient;
    private final Credential credential;
    private final FtpFileAdaptor adaptor;

    private static class CloseableClient implements Closeable {

        private final FTPClient client;
        private boolean closed = false;

        CloseableClient(FTPClient client) {
            this.client = client;
        }

        @Override
        public void close() throws IOException {
            // Added functionality:
            if (!closed) {
                closed = true;
                client.completePendingCommand();
                client.disconnect();
            }
        }
    }

    protected FtpFileSystem(String uniqueID, String name, String location, Path entryPath, int bufferSize, FTPClient ftpClient, Credential credential,
            FtpFileAdaptor adaptor, XenonProperties properties) {
        super(uniqueID, name, location, credential, entryPath, bufferSize, properties);
        this.ftpClient = ftpClient;
        this.credential = credential;
        this.adaptor = adaptor;
    }

    @Override
    public void close() throws XenonException {
        LOGGER.debug("close fileSystem = {}", this);

        if (!isOpen()) {
            throw new NotConnectedException(ADAPTOR_NAME, "File system is already closed");
        }

        try {
            ftpClient.disconnect();
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Exception while disconnecting ftp file system.", e);
        }

        super.close();

        LOGGER.debug("close OK");
    }

    @Override
    public boolean isOpen() throws XenonException {
        return ftpClient.isConnected();
    }

    private HashSet<PosixFilePermission> getPermissions(FTPFile attributes) {
        HashSet<PosixFilePermission> permissions = new HashSet<>();
        for (int userType : USER_TYPES) {
            for (int permissionType : PERMISSION_TYPES) {
                if (attributes.hasPermission(userType, permissionType)) {
                    permissions.add(getPosixFilePermission(userType, permissionType));
                }
            }
        }
        return permissions;
    }

    private PosixFilePermission getPosixFilePermission(int userType, int permissionType) {
        PosixFilePermission permission = null;
        if (userType == FTPFile.USER_ACCESS) {
            if (permissionType == FTPFile.EXECUTE_PERMISSION) {
                permission = PosixFilePermission.OWNER_EXECUTE;
            }
            if (permissionType == FTPFile.WRITE_PERMISSION) {
                permission = PosixFilePermission.OWNER_WRITE;
            }
            if (permissionType == FTPFile.READ_PERMISSION) {
                permission = PosixFilePermission.OWNER_READ;
            }
        }
        if (userType == FTPFile.GROUP_ACCESS) {
            if (permissionType == FTPFile.EXECUTE_PERMISSION) {
                permission = PosixFilePermission.GROUP_EXECUTE;
            }
            if (permissionType == FTPFile.WRITE_PERMISSION) {
                permission = PosixFilePermission.GROUP_WRITE;
            }
            if (permissionType == FTPFile.READ_PERMISSION) {
                permission = PosixFilePermission.GROUP_READ;
            }
        }
        if (userType == FTPFile.WORLD_ACCESS) {
            if (permissionType == FTPFile.EXECUTE_PERMISSION) {
                permission = PosixFilePermission.OTHERS_EXECUTE;
            }
            if (permissionType == FTPFile.WRITE_PERMISSION) {
                permission = PosixFilePermission.OTHERS_WRITE;
            }
            if (permissionType == FTPFile.READ_PERMISSION) {
                permission = PosixFilePermission.OTHERS_READ;
            }
        }
        return permission;
    }

    private PathAttributes convertAttributes(Path path, FTPFile attributes) {

        PathAttributesImplementation result = new PathAttributesImplementation();

        result.setPath(path);
        result.setDirectory(attributes.isDirectory());
        result.setRegular(attributes.isFile());
        result.setOther(attributes.isUnknown());
        result.setSymbolicLink(attributes.isSymbolicLink());

        result.setLastModifiedTime(attributes.getTimestamp().getTimeInMillis());
        result.setCreationTime(attributes.getTimestamp().getTimeInMillis());
        result.setLastAccessTime(attributes.getTimestamp().getTimeInMillis());

        result.setSize(attributes.getSize());

        Set<PosixFilePermission> permission = getPermissions(attributes);

        result.setExecutable(permission.contains(PosixFilePermission.OWNER_EXECUTE));
        result.setReadable(permission.contains(PosixFilePermission.OWNER_READ));
        result.setWritable(permission.contains(PosixFilePermission.OWNER_WRITE));

        result.setPermissions(permission);

        result.setGroup(attributes.getGroup());
        result.setOwner(attributes.getUser());

        return result;
    }

    private void checkClientReply(FTPClient client, String message) throws XenonException {

        int replyCode = client.getReplyCode();
        String replyString = client.getReplyString();

        if (replyCode >= 100 && replyCode < 300) {
            return;
        }

        throw new XenonException(ADAPTOR_NAME, message, new IOException(replyString));
    }

    private void checkClientReply(String message) throws XenonException {
        checkClientReply(ftpClient, message);
    }

    @Override
    public void rename(Path source, Path target) throws XenonException {

        LOGGER.debug("move source = {} target = {}", source, target);

        assertIsOpen();

        Path absSource = toAbsolutePath(source);
        Path absTarget = toAbsolutePath(target);

        assertPathExists(absSource);

        if (areSamePaths(absSource, absTarget)) {
            return;
        }

        assertPathNotExists(absTarget);
        assertParentDirectoryExists(absTarget);

        try {
            ftpClient.rename(absSource.toString(), absTarget.toString());
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to rename " + absSource.toString() + " to " + absTarget.toString(), e);
        }

        checkClientReply("Failed to rename " + absSource.toString() + " to " + absTarget.toString());
    }

    @Override
    public void createDirectory(Path path) throws XenonException {
        LOGGER.debug("createDirectory dir = {}", path);

        assertIsOpen();

        Path absPath = toAbsolutePath(path);
        assertPathNotExists(absPath);
        assertParentDirectoryExists(absPath);

        try {
            ftpClient.makeDirectory(absPath.toString());
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to createDirectory " + absPath.toString(), e);
        }

        checkClientReply("Failed to create directory: " + absPath.toString());
    }

    @Override
    public void createFile(Path path) throws XenonException {
        LOGGER.debug("createFile path = {}", path);

        Path absPath = toAbsolutePath(path);
        assertIsOpen();
        assertPathNotExists(absPath);
        assertParentDirectoryExists(absPath);

        try {
            ByteArrayInputStream dummy = new ByteArrayInputStream(new byte[0]);
            ftpClient.storeFile(absPath.toString(), dummy);
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to createDirectory " + absPath.toString(), e);
        }

        checkClientReply("Failed to create file: " + absPath.toString());
    }

    @Override
    public void createSymbolicLink(Path link, Path path) throws XenonException {
        throw new UnsupportedOperationException(ADAPTOR_NAME, "Operation not supported");
    }

    @Override
    protected void deleteDirectory(Path path) throws XenonException {

        assertIsOpen();

        try {
            ftpClient.removeDirectory(path.toString());
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to createDirectory " + path.toString(), e);
        }

        checkClientReply("Failed to delete directory: " + path.toString());
    }

    @Override
    protected void deleteFile(Path path) throws XenonException {

        assertIsOpen();

        try {
            ftpClient.deleteFile(path.toString());
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to createDirectory " + path.toString(), e);
        }

        checkClientReply("Failed to delete file: " + path.toString());
    }

    @Override
    public boolean exists(Path path) throws XenonException {

        try {
            /*
             * if (path.isEmpty()) { try { // special case for the root directory String originalWorkingDirectory = ftpClient.printWorkingDirectory();
             * 
             * boolean pathExists = ftpClient.changeWorkingDirectory(path.toString());
             * 
             * ftpClient.changeWorkingDirectory(originalWorkingDirectory);
             * 
             * return pathExists; } catch (IOException e) { return false; } }
             */
            getFTPFileInfo(toAbsolutePath(path));
            return true;
        } catch (NoSuchPathException e) {
            return false;
        }
    }

    private FTPFile findFTPFile(FTPFile[] files, Path path) throws NoSuchPathException {

        if (files == null || files.length == 0) {
            throw new NoSuchPathException(ADAPTOR_NAME, "Path not found: " + path);
        }

        String name = path.getFileNameAsString();

        if (path.isEmpty()) {
            // special case for the root directory
            // Some FTP servers show the "." directory so lets use that as a name.
            name = ".";
        }

        for (FTPFile f : files) {
            if (f != null && f.getName().equals(name)) {
                return f;
            }
        }

        if (path.isEmpty()) {
            // special case for the root directory
            // Even though the root dir exists, there no way to get any info on it in some FTP servers. So we'll just return a default here.
            FTPFile tmp = new FTPFile();
            tmp.setType(FTPFile.DIRECTORY_TYPE);
            Calendar time = Calendar.getInstance();
            time.setTimeInMillis(0);
            tmp.setTimestamp(time);
            return tmp;
        }

        throw new NoSuchPathException(ADAPTOR_NAME, "Path not found: " + path);
    }

    // We assume path is non-null, absolute and normalized.
    private FTPFile getFTPFileInfo(Path path) throws XenonException {

        assertIsOpen();

        // We cannot always get the FTPFile of the path directly, behavior of
        // FTP servers seems to vary. Instead,
        // we get the listing of the parent directory and extract the
        // information we need from there.
        try {
            Path p = path.getParent();

            String originalWorkingDirectory = ftpClient.printWorkingDirectory();

            if (p == null) {
                p = new Path("/");
            }

            boolean pathExists = ftpClient.changeWorkingDirectory(p.toString());

            if (!pathExists) {
                // parent must be an existing dir, otherwise dir/path certainly
                // does not exist.
                throw new NoSuchPathException(ADAPTOR_NAME, "Path not found: " + path);
            }

            FTPFile[] files = ftpClient.listFiles();

            ftpClient.changeWorkingDirectory(originalWorkingDirectory);

            return findFTPFile(files, path);
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to get attributes for path: " + path, e);
        }
    }

    @Override
    public PathAttributes getAttributes(Path path) throws XenonException {
        LOGGER.debug("getAttributes path = {}", path);
        Path absPath = toAbsolutePath(path);
        return convertAttributes(absPath, getFTPFileInfo(absPath));
    }

    @Override
    protected List<PathAttributes> listDirectory(Path path) throws XenonException {
        assertIsOpen();
        assertDirectoryExists(path);

        try {
            ArrayList<PathAttributes> result = new ArrayList<>();

            for (FTPFile f : ftpClient.listFiles(path.toString(), FTPFileFilters.NON_NULL)) {
                result.add(convertAttributes(path.resolve(f.getName()), f));
            }

            return result;
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to retrieve directory listing of " + path, e);
        }
    }

    @Override
    public InputStream readFromFile(Path path) throws XenonException {
        LOGGER.debug("newInputStream path = {}", path);

        assertIsOpen();
        Path absPath = toAbsolutePath(path);
        assertPathExists(absPath);
        assertPathIsFile(absPath);

        // Since FTP connections can only do a single thing a time, we need a
        // new FTPClient to handle the stream.
        FTPClient newClient = adaptor.connect(getLocation(), credential);
        newClient.enterLocalPassiveMode();

        try {
            InputStream in = newClient.retrieveFileStream(absPath.toString());

            checkClientReply(newClient, "Failed to read from path: " + absPath.toString());

            return new TransferClientInputStream(in, new CloseableClient(newClient));
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to read from path: " + absPath);
        }
    }

    @Override
    public OutputStream writeToFile(Path path, long size) throws XenonException {
        LOGGER.debug("writeToFile path = {} size = {}", path, size);

        assertIsOpen();
        Path absPath = toAbsolutePath(path);
        assertPathNotExists(absPath);
        assertParentDirectoryExists(absPath);

        // Since FTP connections can only do a single thing a time, we need a
        // new FTPClient to handle the stream.
        FTPClient newClient = adaptor.connect(getLocation(), credential);
        newClient.enterLocalPassiveMode();

        try {
            newClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            OutputStream out = newClient.storeFileStream(absPath.toString());
            checkClientReply(newClient, "Failed to write to path: " + absPath.toString());
            return new TransferClientOutputStream(out, new CloseableClient(newClient));
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to write to path: " + absPath);
        }
    }

    @Override
    public OutputStream writeToFile(Path path) throws XenonException {
        return writeToFile(path, -1);
    }

    @Override
    public OutputStream appendToFile(Path path) throws XenonException {
        LOGGER.debug("appendToFile path = {}", path);

        assertIsOpen();
        Path absPath = toAbsolutePath(path);
        assertPathExists(absPath);
        assertPathIsNotDirectory(absPath);

        try {
            // Since FTP connections can only do a single thing a time, we need
            // a new FTPClient to handle the stream.
            FTPClient newClient = adaptor.connect(getLocation(), credential);
            newClient.enterLocalPassiveMode();
            OutputStream out = newClient.appendFileStream(absPath.toString());

            if (out == null) {
                checkClientReply("Failed to append to path: " + absPath.toString());
            }

            return new TransferClientOutputStream(out, new CloseableClient(newClient));
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to append to path: " + absPath);
        }
    }

    @Override
    public Path readSymbolicLink(Path path) throws XenonException {

        Path absPath = toAbsolutePath(path);

        FTPFile file = getFTPFileInfo(absPath);

        if (file.getType() != FTPFile.SYMBOLIC_LINK_TYPE) {
            throw new InvalidPathException(ADAPTOR_NAME, "Path is not a symbolic link: " + absPath);
        }

        return new Path(file.getLink());
    }

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
        throw new UnsupportedOperationException(getAdaptorName(), "FTP does not support changing permissions.");
    }
}
