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
package nl.esciencecenter.xenon.adaptors.ftp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.InvalidCredentialException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.adaptors.ssh.SshAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.credentials.PasswordCredentialImplementation;
import nl.esciencecenter.xenon.engine.files.FileSystemImplementation;
import nl.esciencecenter.xenon.engine.files.FilesEngine;
import nl.esciencecenter.xenon.engine.files.PathImplementation;
import nl.esciencecenter.xenon.engine.util.CopyEngine;
import nl.esciencecenter.xenon.engine.util.CopyInfo;
import nl.esciencecenter.xenon.engine.util.OpenOptions;
import nl.esciencecenter.xenon.files.Copy;
import nl.esciencecenter.xenon.files.CopyOption;
import nl.esciencecenter.xenon.files.CopyStatus;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.DirectoryStream.Filter;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.InvalidOpenOptionsException;
import nl.esciencecenter.xenon.files.NoSuchPathException;
import nl.esciencecenter.xenon.files.OpenOption;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PathAlreadyExistsException;
import nl.esciencecenter.xenon.files.PathAttributesPair;
import nl.esciencecenter.xenon.files.PosixFilePermission;
import nl.esciencecenter.xenon.files.RelativePath;

public class FtpFiles implements Files {

    private static final Logger LOGGER = LoggerFactory.getLogger(FtpFiles.class);

    private static int currentID = 1;

    private static synchronized String getNewUniqueID() {
        String res = "ftp" + currentID;
        currentID++;
        return res;
    }

    /*
     * Used to store all state attached to a filesystem. This way, FileSystemImplementation is immutable.
     */
    static class FileSystemInfo {

        private final FileSystemImplementation impl;
        private final FTPClient ftpClient;
        private final Credential credential;

        public FileSystemInfo(FileSystemImplementation impl, FTPClient ftpClient, Credential credential) {
            super();
            this.impl = impl;
            this.ftpClient = ftpClient;
            this.credential = credential;
        }

        public FileSystemImplementation getImpl() {
            return impl;
        }

        public FTPClient getFtpClient() {
            return ftpClient;
        }

        public Credential getCredential() {
            return credential;
        }
    }

    private final XenonEngine xenonEngine;
    private final FtpAdaptor adaptor;

    private final Map<String, FileSystemInfo> fileSystems = Collections.synchronizedMap(new HashMap<String, FileSystemInfo>());

    public FtpFiles(FtpAdaptor ftpAdaptor, XenonEngine xenonEngine) {
        this.xenonEngine = xenonEngine;
        adaptor = ftpAdaptor;
    }

    @Override
    public FileSystem newFileSystem(String scheme, String location, Credential credential, Map<String, String> properties)
            throws XenonException {
        LOGGER.debug("newFileSystem scheme = {} location = {} credential = {} properties = {}", scheme, location, credential,
                properties);

        if (credential == null) {
            throw new InvalidCredentialException(adaptor.getName(), "Credentials was null.");
        }

        XenonProperties xenonProperties = new XenonProperties(adaptor.getSupportedProperties(Component.FILESYSTEM), properties);

        FtpLocation ftpLocation = FtpLocation.parse(location);
        FTPClient ftpClient = new FTPClient();
        ftpClient.setListHiddenFiles(true);
        connectToServer(ftpLocation, ftpClient);
        login(credential, ftpClient);
        return createAndRegisterFileSystem(scheme, location, credential, xenonProperties, ftpClient);
    }

    private FileSystemImplementation createAndRegisterFileSystem(String scheme, String location, Credential credential,
            XenonProperties xenonProperties, FTPClient ftpClient) throws XenonException {
        String cwd = getCurrentWorkingDirectory(ftpClient);
        RelativePath entryPath = new RelativePath(cwd);
        String uniqueID = getNewUniqueID();
        FileSystemImplementation fileSystem = new FileSystemImplementation(adaptor.getName(), uniqueID, scheme, location,
                entryPath, credential, xenonProperties);
        fileSystems.put(uniqueID, new FileSystemInfo(fileSystem, ftpClient, credential));
        LOGGER.debug("* newFileSystem OK remote cwd = {} entryPath = {} uniqueID = {}", cwd, entryPath, uniqueID);
        return fileSystem;
    }

    private String getCurrentWorkingDirectory(FTPClient ftpClient) throws XenonException {
        String wd;
        try {
            wd = ftpClient.printWorkingDirectory();
        } catch (IOException e) {
            throw new XenonException(adaptor.getName(), "Could not retrieve current working directory", e);
        }
        return wd;
    }

    private void connectToServer(FtpLocation ftpLocation, FTPClient ftp) throws XenonException {
        try {
            ftp.connect(ftpLocation.getHost(), ftpLocation.getPort());
        } catch (IOException e) {
            throw new XenonException(adaptor.getName(), "Failed to connect", e);
        }
    }

    private void login(Credential credential, FTPClient ftp) throws XenonException {
        try {
            loginWithCredentialOrDefault(ftp, credential);
            int replyCode = ftp.getReplyCode();
            verifyLoginSuccess(replyCode);
        } catch (XenonException | IOException e) {
            throw new XenonException(adaptor.getName(), "Failed to login", e);
        }
    }

    /*
     * Returns true if code is in interval [200,300). See http://en.wikipedia.org/wiki/List_of_FTP_server_return_codes.
     *
     * @param replyCode
     */
    private void verifyLoginSuccess(int replyCode) throws XenonException {
        if (replyCode < 200 || replyCode >= 300) {
            String message = MessageFormat.format("Server status not succesfull after login (status code {0}).", replyCode);
            throw new XenonException(adaptor.getName(), message);
        }
    }

    private void loginWithCredentialOrDefault(FTPClient ftp, Credential credential) throws IOException {
        String password = "";
        String user = "anonymous";
        if (credential instanceof PasswordCredentialImplementation) {
            PasswordCredentialImplementation passwordCredential = (PasswordCredentialImplementation) credential;
            password = new String(passwordCredential.getPassword());
            user = passwordCredential.getUsername();
        }
        ftp.login(user, password);
    }

    @Override
    public Path newPath(FileSystem filesystem, RelativePath location) throws XenonException {
        return new PathImplementation(filesystem, location);
    }

    @Override
    public void close(FileSystem fileSystem) throws XenonException {
        LOGGER.debug("close fileSystem = {}", fileSystem);
        if (!isOpen(fileSystem)) {
            throw new XenonException(adaptor.getName(), "File system is already closed");
        }

        FileSystemImplementation fs = (FileSystemImplementation) fileSystem;
        FileSystemInfo info = fileSystems.remove(fs.getUniqueID());

        try {
            info.getFtpClient().disconnect();
        } catch (IOException e) {
            throw new XenonException(adaptor.getName(), "Exception while disconnecting ftp file system.", e);
        }
        LOGGER.debug("close OK");
    }

    @Override
    public boolean isOpen(FileSystem fileSystem) throws XenonException {
        LOGGER.debug("isOpen fileSystem = {}", fileSystem);
        FileSystemImplementation fs = (FileSystemImplementation) fileSystem;
        FileSystemInfo fileSystemInfo = fileSystems.get(fs.getUniqueID());
        boolean result = (fileSystemInfo != null) && fileSystemInfo.getFtpClient().isConnected();
        LOGGER.debug("isOpen OK result = {}", result);
        return result;
    }

    @Override
    public Copy copy(Path source, Path target, CopyOption... options) throws XenonException {
        LOGGER.debug("copy source = {} target = {} options = {}", source, target, options);
        CopyEngine ce = xenonEngine.getCopyEngine();

        CopyInfo info = CopyInfo.createCopyInfo(adaptor.getName(), ce.getNextID("FTP_COPY_"), source, target, options);

        ce.copy(info);

        Copy result;

        if (info.isAsync()) {
            result = info.getCopy();
        } else {

            Exception e = info.getException();

            if (e != null) {
                throw new XenonException(adaptor.getName(), "Copy failed!", e);
            }

            result = null;
        }

        LOGGER.debug("copy OK result = {}", result);

        return result;
    }

    /*
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

        if (areSamePaths(source, target)) {
            return;
        }

        assertValidArgumentsForMove(source, target);

        final String absoluteSourcePath = source.getRelativePath().getAbsolutePath();
        FTPClient ftpClient = getFtpClientByPath(target);
        FtpCommand ftpCommand = new FtpCommand() {
            @Override
            public void doWork(FTPClient ftpClient, String absoluteTargetPath) throws IOException {
                ftpClient.rename(absoluteSourcePath, absoluteTargetPath);
            }
        };
        ftpCommand.execute(ftpClient, target, "Failed to move to path");
        LOGGER.debug("move OK");
    }

    private void assertValidArgumentsForMove(Path source, Path target) throws XenonException {
        assertSameFileSystemsForMove(source, target);
        assertPathExists(source);
        assertPathNotExists(target);
        assertParentDirectoryExists(target);
    }

    private void assertSameFileSystemsForMove(Path source, Path target) throws XenonException {
        FileSystem sourcefs = source.getFileSystem();
        FileSystem targetfs = target.getFileSystem();

        if (!sourcefs.getLocation().equals(targetfs.getLocation())) {
            throw new XenonException(adaptor.getName(), "Cannot move between different FileSystems: " + sourcefs.getLocation()
                    + " and " + targetfs.getLocation());
        }
    }

    private boolean areSamePaths(Path source, Path target) {
        RelativePath sourceName = source.getRelativePath().normalize();
        RelativePath targetName = target.getRelativePath().normalize();
        return sourceName.equals(targetName);
    }

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

    @Override
    public void createDirectories(Path path) throws XenonException {
        LOGGER.debug("createDirectories dir = {}", path);
        RelativePath relativeParent = path.getRelativePath().getParent();

        if (relativeParent != null) {
            PathImplementation parentPath = new PathImplementation(path.getFileSystem(), relativeParent);
            if (!exists(parentPath)) {
                // Recursive call
                createDirectories(parentPath);
            }
        }
        createDirectory(path);
        LOGGER.debug("createDirectories OK");
    }

    @Override
    public void createDirectory(Path path) throws XenonException {
        LOGGER.debug("createDirectory dir = {}", path);
        FtpCommand ftpCommand = new FtpCommand() {
            @Override
            public void doWork(FTPClient ftpClient, String absolutePath) throws IOException {
                setHasSucceeded(ftpClient.makeDirectory(absolutePath));
            }
        };
        String messageInCaseOfError = "Failed to create directory";
        ftpCommand.execute(getFtpClientByPath(path), path, messageInCaseOfError);
        LOGGER.debug("createDirectory OK");
    }

    @Override
    public void createFile(Path path) throws XenonException {
        LOGGER.debug("createFile path = {}", path);
        assertPathNotExists(path);
        FtpCommand ftpCommand = new FtpCommand() {
            @Override
            public void doWork(FTPClient ftpClient, String absolutePath) throws IOException {
                InputStream dummyContent = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
                setHasSucceeded(ftpClient.storeFile(absolutePath, dummyContent));
            }
        };
        String messageInCaseOfError = "Failed to create file";
        ftpCommand.execute(getFtpClientByPath(path), path, messageInCaseOfError);
        LOGGER.debug("createFile OK");
    }

    @Override
    public void delete(Path path) throws XenonException {
        LOGGER.debug("delete path = {}", path);
        if (getAttributes(path).isDirectory()) {
            deleteDirectory(path);
        } else {
            deleteFile(path);
        }
        LOGGER.debug("delete OK");
    }

    private void deleteDirectory(Path path) throws XenonException {
        FtpCommand ftpCommand = new FtpCommand() {
            @Override
            public void doWork(FTPClient ftpClient, String absolutePath) throws IOException {
                setHasSucceeded(ftpClient.removeDirectory(absolutePath));
            }
        };
        String messageInCaseOfError = "Failed to delete file or directory";

        ftpCommand.execute(getFtpClientByPath(path), path, messageInCaseOfError);
    }

    private void deleteFile(Path path) throws XenonException {
        FtpCommand ftpCommand = new FtpCommand() {
            @Override
            public void doWork(FTPClient ftpClient, String absolutePath) throws IOException {
                setHasSucceeded(ftpClient.deleteFile(absolutePath));
            }
        };
        String messageInCaseOfError = "Failed to delete file or directory";

        ftpCommand.execute(getFtpClientByPath(path), path, messageInCaseOfError);
    }

    @Override
    public boolean exists(Path path) throws XenonException {
        LOGGER.debug("exists path = {}", path);
        boolean result = fileExists(path) || directoryExists(path);
        LOGGER.debug("exists OK result = {}", result);
        return result;
    }

    private boolean directoryExists(Path path) throws XenonException {
        FTPClient ftpClient = getFtpClientByPath(path);
        FtpQuery<Boolean> ftpQuery = new FtpQuery<Boolean>() {
            @Override
            public void doWork(FTPClient ftpClient, String path) throws IOException {
                String originalWorkingDirectory = ftpClient.printWorkingDirectory();
                boolean pathExists = ftpClient.changeWorkingDirectory(path);
                ftpClient.changeWorkingDirectory(originalWorkingDirectory);
                setResult(pathExists);
            }
        };
        ftpQuery.execute(ftpClient, path, "Could not inspect directory");
        return ftpQuery.getResult();
    }

    private boolean fileExists(Path path) throws XenonException {
        FTPClient ftpClient = getFtpClientByPath(path);
        FtpQuery<Boolean> ftpQuery = new FtpQuery<Boolean>() {
            @Override
            public void doWork(FTPClient ftpClient, String path) throws IOException {
                FTPFile[] listFiles = ftpClient.listFiles(path);
                int length = listFiles.length;
                setResult(length == 1);
            }
        };
        ftpQuery.execute(ftpClient, path, "Could not inspect file");
        return ftpQuery.getResult();
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir) throws XenonException {
        LOGGER.debug("newDirectoryStream path = {}", dir);
        return newDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path path, Filter filter) throws XenonException {
        LOGGER.debug("newDirectoryStream path = {} filter = <?>", path);
        return new FtpDirectoryStream(path, filter, listDirectory(path, filter));
    }

    private List<FTPFile> listDirectory(Path path, Filter filter) throws XenonException {
        String absolutePath = path.getRelativePath().getAbsolutePath();
        FTPClient ftpClient = getFtpClientByPath(path);

        FTPFile[] listFiles;
        try {
            if (filter == null) {
                throw new XenonException(adaptor.getName(), "Filter is null.");
            }
            assertDirectoryExists(path);
            listFiles = ftpClient.listFiles(absolutePath);
        } catch (IOException e) {
            String message = MessageFormat.format("Failed to retrieve directory listing of {0}", absolutePath);
            throw new XenonException(adaptor.getName(), message);
        }

        return new LinkedList<>(Arrays.asList(listFiles));
    }

    private void assertDirectoryExists(Path path) throws XenonException {
        if (!directoryExists(path)) {
            String absolutePath = path.getRelativePath().getAbsolutePath();
            String message = MessageFormat.format("Directory does not exist at path {0}", absolutePath);
            throw new XenonException(adaptor.getName(), message);
        }
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path path) throws XenonException {
        LOGGER.debug("newAttributesDirectoryStream path = {}", path);
        return newAttributesDirectoryStream(path, FilesEngine.ACCEPT_ALL_FILTER);
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path path, Filter filter) throws XenonException {
        LOGGER.debug("newAttributesDirectoryStream path = {} filter = <?>", path);
        if (path == null) {
            throw new XenonException(adaptor.getName(), "Cannot open attribute directory stream of null path");
        }
        return new FtpDirectoryAttributeStream(path, filter, listDirectory(path, filter));
    }

    @Override
    public InputStream newInputStream(Path path) throws XenonException {
        LOGGER.debug("newInputStream path = {}", path);
        assertValidArgumentsForNewInputStream(path);
        Path pathOnNewFileSystem = getPathOnNewFileSystem(path);
        FTPClient ftpClient = getFtpClientByPath(pathOnNewFileSystem);
        InputStream inputStream = getInputStreamFromFtpClient(ftpClient, pathOnNewFileSystem);
        FtpInputStream ftpInputStream = new FtpInputStream(inputStream, ftpClient, pathOnNewFileSystem, this);
        LOGGER.debug("newInputStream OK");
        return ftpInputStream;
    }

    private InputStream getInputStreamFromFtpClient(FTPClient ftpClient, Path path) throws XenonException {
        FtpQuery<InputStream> ftpQuery = new FtpQuery<InputStream>() {
            @Override
            public void doWork(FTPClient ftpClient, String path) throws IOException {
                setResult(ftpClient.retrieveFileStream(path));
            }
        };
        ftpQuery.execute(ftpClient, path, "Failed to open input stream");
        return ftpQuery.getResult();
    }

    private void assertValidArgumentsForNewInputStream(Path path) throws XenonException {
        assertPathExists(path);
        FileAttributes att = getAttributes(path);
        if (att.isDirectory()) {
            throw new XenonException(SshAdaptor.ADAPTOR_NAME, "Path " + path + " is a directory!");
        }
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws XenonException {
        LOGGER.debug("newOutputStream path = {} option = {}", path, options);
        OpenOptions processedOptions = OpenOptions.processOptions(adaptor.getName(), options);
        assertValidArgumentsForNewOutputStream(path, processedOptions);
        Path pathOnNewFileSystem = getPathOnNewFileSystem(path);
        FTPClient ftpClient = getFtpClientByPath(pathOnNewFileSystem);
        OutputStream outputStream = getOutputStreamFromFtpClient(ftpClient, pathOnNewFileSystem, processedOptions);
        FtpOutputStream ftpOutputStream = new FtpOutputStream(outputStream, ftpClient, pathOnNewFileSystem, this);
        LOGGER.debug("newOutputStream OK");
        return ftpOutputStream;
    }

    /*
     * Creates a copy of the file system of the path and returns a copy of the path that refers to the new file system. This way
     * structures like streams can have their own dedicated ftp client. This is necessary because ftp can't do two things over the
     * same connection like uploading to a stream and reading from another stream for example.
     *
     * @param path
     * @return copy of the path referring to a new file system
     * @throws XenonException
     */
    private Path getPathOnNewFileSystem(Path path) throws XenonException {
        FileSystem fileSystem = path.getFileSystem();
        FileSystemImplementation fs = (FileSystemImplementation) fileSystem;
        FileSystemInfo fileSystemInfo = fileSystems.get(fs.getUniqueID());
        Credential credential = fileSystemInfo.getCredential();
        FileSystem newFileSystem = newFileSystem(fileSystem.getScheme(), fileSystem.getLocation(), credential,
                fileSystem.getProperties());
        return newPath(newFileSystem, path.getRelativePath());
    }

    private OutputStream getOutputStreamFromFtpClient(FTPClient ftpClient, Path path, OpenOptions options) throws XenonException {
        FtpQuery<OutputStream> ftpQuery = options.getAppendMode() == OpenOption.APPEND ? getAppendingOutputStreamQuery()
                : getTruncatingOrNewFileOutputStreamQuery();
        ftpQuery.execute(ftpClient, path, "Failed to open outputstream");
        return ftpQuery.getResult();
    }

    private FtpQuery<OutputStream> getTruncatingOrNewFileOutputStreamQuery() {
        FtpQuery<OutputStream> ftpQuery;
        ftpQuery = new FtpQuery<OutputStream>() {
            @Override
            public void doWork(FTPClient ftpClient, String path) throws IOException {
                setResult(ftpClient.storeFileStream(path));
            }
        };
        return ftpQuery;
    }

    private FtpQuery<OutputStream> getAppendingOutputStreamQuery() {
        FtpQuery<OutputStream> ftpQuery;
        ftpQuery = new FtpQuery<OutputStream>() {
            @Override
            public void doWork(FTPClient ftpClient, String path) throws IOException {
                setResult(ftpClient.appendFileStream(path));
            }
        };
        return ftpQuery;
    }

    private void assertValidArgumentsForNewOutputStream(Path path, OpenOptions processedOptions) throws XenonException {
        if (processedOptions.getReadMode() != null) {
            throw new InvalidOpenOptionsException(adaptor.getName(), "Disallowed open option: READ");
        }

        if (processedOptions.getAppendMode() == null) {
            throw new InvalidOpenOptionsException(adaptor.getName(), "No append mode provided!");
        }

        if (processedOptions.getWriteMode() == null) {
            processedOptions.setWriteMode(OpenOption.WRITE);
        }

        if (processedOptions.getOpenMode() == OpenOption.CREATE) {
            assertPathNotExists(path);
        } else if (processedOptions.getOpenMode() == OpenOption.OPEN) {
            assertPathExists(path);
        }
    }

    @Override
    public FileAttributes getAttributes(Path path) throws XenonException {
        LOGGER.debug("getAttributes path = {}", path);
        assertPathExists(path);
        FTPFile listFile = getFtpFile(path);
        FileAttributes fileAttributes = new FtpFileAttributes(listFile);
        LOGGER.debug("getAttributes OK result = {}", fileAttributes);
        return fileAttributes;
    }

    private FTPFile getFtpFile(Path path) throws XenonException {
        FTPFile ftpFile = getRegularFtpFile(path);
        if (ftpFile == null) {
            ftpFile = getDirectoryFtpFile(path);
        }
        return ftpFile;
    }

    private FTPFile getRegularFtpFile(Path pathToRegularFile) throws XenonException {
        FtpQuery<FTPFile> ftpQuery = new FtpQuery<FTPFile>() {
            @Override
            public void doWork(FTPClient ftpClient, String path) throws IOException {
                setResult(ftpClient.listFiles(path)[0]);
            }
        };
        ftpQuery.execute(getFtpClientByPath(pathToRegularFile), pathToRegularFile, "Failed to retrieve attributes of path");
        return ftpQuery.getResult();
    }

    private FTPFile getDirectoryFtpFile(Path path) throws XenonException {
        FtpQuery<FTPFile> ftpQuery = new FtpQuery<FTPFile>() {
            @Override
            public void doWork(FTPClient ftpClient, String path) throws IOException {
                String targetName = ".";
                FTPFile[] listFiles = ftpClient.listDirectories(path);
                for (FTPFile listFile : listFiles) {
                    if (listFile.getName().equals(targetName)) {
                        setResult(listFile);
                        break;
                    }
                }
            }
        };
        ftpQuery.execute(getFtpClientByPath(path), path, "Failed to retrieve attributes of directory");
        return ftpQuery.getResult();
    }

    @Override
    public Path readSymbolicLink(Path path) throws XenonException {
        LOGGER.debug("readSymbolicLink path = {}", path);
        throw new XenonException(adaptor.getName(), "Ftp file system does not support symbolic links");
    }

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
        LOGGER.debug("setPosixFilePermissions path = {} permissions = {}", path, permissions);
        LOGGER.debug("setPosixFilePermissions OK");
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
                // TODO: ignore for now
            }
        }
        LOGGER.debug("end OK");
    }

    private FTPClient getFtpClientByPath(Path path) {
        FileSystemImplementation fileSystem = (FileSystemImplementation) path.getFileSystem();
        return fileSystems.get(fileSystem.getUniqueID()).getFtpClient();
    }

    private void assertPathNotExists(Path path) throws XenonException {
        if (exists(path)) {
            throw new PathAlreadyExistsException(adaptor.getName(), "File already exists: " + path);
        }
    }

    private void assertPathExists(Path path) throws XenonException {
        if (!exists(path)) {
            throw new NoSuchPathException(adaptor.getName(), "File does not exist: " + path);
        }
    }

    private void assertParentDirectoryExists(Path target) throws XenonException {
        Path parentDirectory = newPath(target.getFileSystem(), target.getRelativePath().getParent());
        if (parentDirectory != null) {
            assertDirectoryExists(parentDirectory);
        }
    }
}
