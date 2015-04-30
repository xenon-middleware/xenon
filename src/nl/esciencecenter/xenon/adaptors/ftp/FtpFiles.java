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
import java.util.Map;
import java.util.Set;

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

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtpFiles implements Files {

    private static final Logger LOGGER = LoggerFactory.getLogger(FtpFiles.class);

    private static int currentID = 1;

    private static synchronized String getNewUniqueID() {
        String res = "ftp" + currentID;
        currentID++;
        return res;
    }

    /**
     * Used to store all state attached to a filesystem. This way, FileSystemImplementation is immutable.
     */
    static class FileSystemInfo {

        private final FileSystemImplementation impl;
        private final FTPClient ftpClient;

        public FileSystemInfo(FileSystemImplementation impl, FTPClient ftpClient) {
            super();
            this.impl = impl;
            this.ftpClient = ftpClient;
        }

        public FileSystemImplementation getImpl() {
            return impl;
        }

        public FTPClient getFtpClient() {
            return ftpClient;
        }
    }

    private final XenonEngine xenonEngine;
    private final FtpAdaptor adaptor;

    private Map<String, FileSystemInfo> fileSystems = Collections.synchronizedMap(new HashMap<String, FileSystemInfo>());

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
            throw new XenonException(adaptor.getName(), "Credentials was null.");
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
        FileSystemImplementation fileSystem = new FileSystemImplementation(FtpAdaptor.ADAPTOR_NAME, uniqueID, scheme, location,
                entryPath, credential, xenonProperties);
        fileSystems.put(uniqueID, new FileSystemInfo(fileSystem, ftpClient));
        LOGGER.debug("* newFileSystem OK remote cwd = {} entryPath = {} uniqueID = {}", cwd, entryPath, uniqueID);
        return fileSystem;
    }

    private String getCurrentWorkingDirectory(FTPClient ftpClient) throws XenonException {
        String wd = null;
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
            verifySuccessByServerCode(replyCode);
        } catch (XenonException | IOException e) {
            throw new XenonException(adaptor.getName(), "Failed to login", e);
        }
    }

    private void verifySuccessByServerCode(int replyCode) throws XenonException {
        if ((replyCode >= 200 && replyCode < 300) == false) {
            throw new XenonException(adaptor.getName(), "Server status not succesfull (status code " + replyCode + ").");
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
        System.out.println("Logging in as '" + user + "' : '" + password + "'."); // TODO remove this debugging line
        ftp.login(user, password);
    }

    @Override
    public Path newPath(FileSystem filesystem, RelativePath location) throws XenonException {
        return new PathImplementation(filesystem, location);
    }

    @Override
    public void close(FileSystem fileSystem) throws XenonException {
        LOGGER.debug("close fileSystem = {}", fileSystem);
        if (isOpen(fileSystem) == false) {
            throw new XenonException(adaptor.getName(), "File system is already closed");
        }

        FileSystemImplementation fs = (FileSystemImplementation) fileSystem;
        FileSystemInfo info = fileSystems.remove(fs.getUniqueID());

        try {
            info.getFtpClient().disconnect();
        } catch (IOException e) {
            throw new XenonException(adaptor.getName(), "Exception while disconnecting ftp file system.");
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
        return null;
    }

    @Override
    public void move(Path source, Path target) throws XenonException {
    }

    @Override
    public CopyStatus getCopyStatus(Copy copy) throws XenonException {
        return null;
    }

    @Override
    public CopyStatus cancelCopy(Copy copy) throws XenonException {
        return null;
    }

    @Override
    public void createDirectories(Path path) throws XenonException {
        RelativePath relativeParent = path.getRelativePath().getParent();

        if (relativeParent != null) {
            PathImplementation parentPath = new PathImplementation(path.getFileSystem(), relativeParent);
            if (exists(parentPath) == false) {
                // Recursive call
                createDirectories(parentPath);
            }
        }
        createDirectory(path);
    }

    @Override
    public void createDirectory(Path path) throws XenonException {
        FtpCommand ftpCommand = new FtpCommand() {
            @Override
            public void doWork(FTPClient ftpClient, String absolutePath) throws IOException {
                hasSucceeded = ftpClient.makeDirectory(absolutePath);
            }
        };
        String messageInCaseOfError = "Failed to create directory";

        ftpCommand.execute(getFtpClientByPath(path), path, messageInCaseOfError);
    }

    @Override
    public void createFile(Path path) throws XenonException {
        assertPathNotExists(path);

        FtpCommand ftpCommand = new FtpCommand() {
            @Override
            public void doWork(FTPClient ftpClient, String absolutePath) throws IOException {
                InputStream dummyContent = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
                hasSucceeded = ftpClient.storeFile(absolutePath, dummyContent);
            }
        };
        String messageInCaseOfError = "Failed to create file";

        ftpCommand.execute(getFtpClientByPath(path), path, messageInCaseOfError);
    }

    @Override
    public void delete(Path path) throws XenonException {
        if (getAttributes(path).isDirectory()) {
            deleteDirectory(path);
        } else {
            deleteFile(path);
        }
    }

    private void deleteDirectory(Path path) throws XenonException {
        FtpCommand ftpCommand = new FtpCommand() {
            @Override
            public void doWork(FTPClient ftpClient, String absolutePath) throws IOException {
                hasSucceeded = ftpClient.removeDirectory(absolutePath);
            }
        };
        String messageInCaseOfError = "Failed to delete file or directory";

        ftpCommand.execute(getFtpClientByPath(path), path, messageInCaseOfError);
    }

    private void deleteFile(Path path) throws XenonException {
        FtpCommand ftpCommand = new FtpCommand() {
            @Override
            public void doWork(FTPClient ftpClient, String absolutePath) throws IOException {
                hasSucceeded = ftpClient.deleteFile(absolutePath);
            }
        };
        String messageInCaseOfError = "Failed to delete file or directory";

        ftpCommand.execute(getFtpClientByPath(path), path, messageInCaseOfError);
    }

    @Override
    public boolean exists(Path path) throws XenonException {
        return fileExists(path) || directoryExists(path);
    }

    private boolean directoryExists(Path path) throws XenonException {
        FTPClient ftpClient = getFtpClientByPath(path);
        FtpQuery<Boolean> ftpQuery = new FtpQuery<Boolean>() {
            @Override
            public void doWork(FTPClient ftpClient, String path) throws IOException {
                String originalWorkingDirectory = ftpClient.printWorkingDirectory();
                boolean pathExists = ftpClient.changeWorkingDirectory(path);
                String[] replyStrings1 = ftpClient.getReplyStrings();
                ftpClient.changeWorkingDirectory(originalWorkingDirectory);
                String[] replyStrings2 = ftpClient.getReplyStrings();
                result = pathExists;
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
                String[] replyStrings3 = ftpClient.getReplyStrings();
                int length = listFiles.length;
                result = length == 1;
            }
        };
        ftpQuery.execute(ftpClient, path, "Could not inspect file");
        return ftpQuery.getResult();
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir) throws XenonException {
        return newDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter filter) throws XenonException {
        return new FtpDirectoryStream(dir, filter, listDirectory(dir, filter));
    }

    private LinkedList<FTPFile> listDirectory(Path path, Filter filter) throws XenonException {
        String absolutePath = path.getRelativePath().getAbsolutePath();
        FTPClient ftpClient = getFtpClientByPath(path);

        FTPFile[] listFiles = null;
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

        return new LinkedList<FTPFile>(Arrays.asList(listFiles));
    }

    private void assertDirectoryExists(Path path) throws XenonException {
        boolean directoryExists = false;
        directoryExists = directoryExists(path);
        if (directoryExists == false) {
            String absolutePath = path.getRelativePath().getAbsolutePath();
            String message = MessageFormat.format("Directory does not exist at path ", absolutePath);
            throw new XenonException(adaptor.getName(), message);
        }
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path path) throws XenonException {
        return newAttributesDirectoryStream(path, FilesEngine.ACCEPT_ALL_FILTER);
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path path, Filter filter) throws XenonException {
        if (path == null) {
            throw new XenonException(adaptor.getName(), "Cannot open attribute directory stream of null path");
        }
        return new FtpDirectoryAttributeStream(path, filter, listDirectory(path, filter));
    }

    @Override
    public InputStream newInputStream(Path path) throws XenonException {
        LOGGER.debug("newInputStream path = {}", path);
        assertValidArgumentsForNewInputStream(path);
        FTPClient ftpClient = getFtpClientByPath(path);
        InputStream inputStream = getInputStreamFromFtpClient(ftpClient, path);
        LOGGER.debug("newInputStream OK");
        return inputStream;
    }

    private InputStream getInputStreamFromFtpClient(FTPClient ftpClient, Path path) throws XenonException {
        FtpQuery<InputStream> ftpQuery = new FtpQuery<InputStream>() {
            @Override
            public void doWork(FTPClient ftpClient, String path) throws IOException {
                result = ftpClient.retrieveFileStream(path);
            }
        };
        ftpQuery.execute(ftpClient, path, "Failed to open input stream");
        return ftpQuery.getResult();
    }

    private void assertValidArgumentsForNewInputStream(Path path) throws XenonException, NoSuchPathException {
        assertPathExists(path);
        FileAttributes att = getAttributes(path);
        if (att.isDirectory()) {
            throw new XenonException(SshAdaptor.ADAPTOR_NAME, "Path " + path + " is a directory!");
        }
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws XenonException {
        // TODO work here:
        /* You are not using any of the options. You are not setting any of the modes like "OpenOption.CREATE" etc.
         * This might be the reason that the file is not written in one go.
         *
         * Also, read the OpenOption class and the Files interface about newoutputstream.
         *
         */
        LOGGER.debug("newOutputStream path = {} option = {}", path, options);
        assertValidArgumentsForNewOutputStream(path, options);
        FTPClient ftpClient = getFtpClientByPath(path);
        OutputStream outputStream = getOutputStreamFromFtpClient(ftpClient, path);
        FtpOutputStream ftpOutputStream = new FtpOutputStream(outputStream, ftpClient);
        LOGGER.debug("newOutputStream OK");
        return ftpOutputStream;
    }

    private OutputStream getOutputStreamFromFtpClient(FTPClient ftpClient, Path path) throws XenonException {
        FtpQuery<OutputStream> ftpQuery = new FtpQuery<OutputStream>() {
            @Override
            public void doWork(FTPClient ftpClient, String path) throws IOException {
                result = ftpClient.storeFileStream(path);
            }
        };
        ftpQuery.execute(ftpClient, path, "Failed to open outputstream");
        return ftpQuery.getResult();
    }

    private void assertValidArgumentsForNewOutputStream(Path path, OpenOption... options) throws InvalidOpenOptionsException,
    XenonException, PathAlreadyExistsException, NoSuchPathException {
        OpenOptions processedOptions = OpenOptions.processOptions(adaptor.getName(), options);

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
        assertPathExists(path);
        FTPClient ftpClient = getFtpClientByPath(path);
        FTPFile listFile = getFtpFile(ftpClient, path);
        return new FtpFileAttributes(listFile);
    }

    private FTPFile getFtpFile(FTPClient ftpClient, Path path) throws XenonException {
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
                result = ftpClient.listFiles(path)[0];
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
                        result = listFile;
                    }
                }
            }
        };
        ftpQuery.execute(getFtpClientByPath(path), path, "Failed to retrieve attributes of directory");
        return ftpQuery.getResult();
    }

    @Override
    public Path readSymbolicLink(Path link) throws XenonException {
        return null;
    }

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
    }

    public void end() {

        LOGGER.debug("end called, closing all file systems");

        //        while (fileSystems.size() > 0) {
        //            Set<String> keys = fileSystems.keySet();
        //            String first = keys.iterator().next();
        //            FileSystem fs = fileSystems.get(first).getImpl();
        //
        //            try {
        //                close(fs);
        //            } catch (XenonException e) {
        //                // ignore for now
        //            }
        //        }

        LOGGER.debug("end OK");
    }

    private FTPClient getFtpClientByPath(Path path) {
        FileSystemImplementation fileSystem = (FileSystemImplementation) path.getFileSystem();
        return fileSystems.get(fileSystem.getUniqueID()).getFtpClient();
    }

    private void assertPathNotExists(Path path) throws XenonException, PathAlreadyExistsException {
        if (exists(path)) {
            throw new PathAlreadyExistsException(adaptor.getName(), "File already exists: " + path);
        }
    }

    private void assertPathExists(Path path) throws XenonException, NoSuchPathException {
        if (exists(path) == false) {
            throw new NoSuchPathException(adaptor.getName(), "File does not exist: " + path);
        }
    }
}
