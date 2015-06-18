package nl.esciencecenter.xenon.adaptors.webdav;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.adaptors.ftp.FtpFiles;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.credentials.PasswordCredentialImplementation;
import nl.esciencecenter.xenon.engine.files.FileSystemImplementation;
import nl.esciencecenter.xenon.engine.files.PathImplementation;
import nl.esciencecenter.xenon.files.Copy;
import nl.esciencecenter.xenon.files.CopyOption;
import nl.esciencecenter.xenon.files.CopyStatus;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.DirectoryStream.Filter;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.OpenOption;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PathAttributesPair;
import nl.esciencecenter.xenon.files.PosixFilePermission;
import nl.esciencecenter.xenon.files.RelativePath;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebdavFiles implements Files {
    private static final Logger LOGGER = LoggerFactory.getLogger(FtpFiles.class);
    private WebdavAdaptor adaptor;

    private final Map<String, FileSystemInfo> fileSystems = Collections.synchronizedMap(new HashMap<String, FileSystemInfo>());

    private static int currentID = 1;

    private static synchronized String getNewUniqueID() {
        String res = "webdav" + currentID;
        currentID++;
        return res;
    }

    /**
     * Used to store all state attached to a filesystem. This way, FileSystemImplementation is immutable.
     */
    static class FileSystemInfo {

        private final FileSystemImplementation impl;
        private final Credential credential;
        private final FileObject fileObject;

        public FileSystemInfo(FileSystemImplementation impl, FileObject fileObject, Credential credential) {
            super();
            this.impl = impl;
            this.credential = credential;
            this.fileObject = fileObject;
        }

        public FileObject getFileObject() {
            return fileObject;
        }

        public FileSystemImplementation getImpl() {
            return impl;
        }

        public Credential getCredential() {
            return credential;
        }
    }

    public WebdavFiles(WebdavAdaptor webdavAdaptor, XenonEngine xenonEngine) {
        adaptor = webdavAdaptor;
    }

    @Override
    public FileSystem newFileSystem(String scheme, String location, Credential credential, Map<String, String> properties)
            throws XenonException {
        LOGGER.debug("newFileSystem scheme = {} location = {} credential = {} properties = {}", scheme, location, credential,
                properties);

        WebdavLocation webdavLocation = new WebdavLocation(location);
        FileObject fileObject = openWithVfs(scheme, webdavLocation, credential);

        String cwd = getCurrentWorkingDirectory(fileObject);
        RelativePath entryPath = new RelativePath(cwd);
        String uniqueID = getNewUniqueID();
        XenonProperties xenonProperties = new XenonProperties(adaptor.getSupportedProperties(Component.FILESYSTEM), properties);
        FileSystemImplementation fileSystem = new FileSystemImplementation(adaptor.getName(), uniqueID, scheme, location,
                entryPath, credential, xenonProperties);
        fileSystems.put(uniqueID, new FileSystemInfo(fileSystem, fileObject, credential));
        LOGGER.debug("* newFileSystem OK remote cwd = {} entryPath = {} uniqueID = {}", cwd, entryPath, uniqueID);
        return fileSystem;
    }

    private String getCurrentWorkingDirectory(FileObject fileObject) throws XenonException {
        return "/";
        //        String cwd = null;
        //        try {
        //            URL url = fileObject.getURL();
        //            URI uri = url.toURI();
        //            cwd = uri.getPath();
        //        } catch (FileSystemException | URISyntaxException e) {
        //            throw new XenonException(adaptor.getName(), "Could not retrieve current working directory", e);
        //        }
        //        return cwd;
    }

    private FileObject openWithVfs(String scheme, WebdavLocation webdavLocation, Credential credential) throws XenonException {
        FileObject file = null;
        try {
            FileSystemManager manager = VFS.getManager();
            StaticUserAuthenticator auth = getAuthenticator(webdavLocation, credential);
            FileSystemOptions opts = new FileSystemOptions();
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);

            file = manager.resolveFile(scheme + "://" + webdavLocation.toString(), opts);
            boolean exists = file.exists();
            if (!exists) {
                throw new XenonException(adaptor.getName(), MessageFormat.format("Location {0} does not exist", webdavLocation));
            }

        } catch (FileSystemException e) {
            throw new XenonException(adaptor.getName(), "Failed to start file system manager", e);
        }
        return file;
    }

    private StaticUserAuthenticator getAuthenticator(WebdavLocation webdavLocation, Credential credential) {
        String password = "";
        String user = "";
        if (credential instanceof PasswordCredentialImplementation) {
            PasswordCredentialImplementation passwordCredential = (PasswordCredentialImplementation) credential;
            password = new String(passwordCredential.getPassword());
            user = passwordCredential.getUsername();
        }
        StaticUserAuthenticator auth = new StaticUserAuthenticator(webdavLocation.toString(), user, password);
        return auth;
    }

    @Override
    public Path newPath(FileSystem filesystem, RelativePath location) throws XenonException {
        return new PathImplementation(filesystem, location);
    }

    @Override
    public void close(FileSystem fileSystem) throws XenonException {
        // ignored!
    }

    @Override
    public boolean isOpen(FileSystem fileSystem) throws XenonException {
        LOGGER.debug("isOpen fileSystem = {}", fileSystem);
        FileSystemImplementation fs = (FileSystemImplementation) fileSystem;
        FileSystemInfo fileSystemInfo = fileSystems.get(fs.getUniqueID());
        boolean result = (fileSystemInfo != null) && fileSystemInfo.getFileObject().isAttached();
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
    public void createDirectories(Path dir) throws XenonException {
    }

    @Override
    public void createDirectory(Path path) throws XenonException {
        LOGGER.debug("createDirectory dir = {}", path);
        FileObject fileSystem = getFileSystemByPath(path);
        String absolutePath = path.getRelativePath().getRelativePath();
        try {
            FileObject newDirectory = fileSystem.resolveFile("public123");
            boolean exists = newDirectory.exists();
            boolean writeable = newDirectory.isWriteable();
            boolean readable = newDirectory.isReadable();
            boolean hidden = newDirectory.isHidden();
            boolean contentOpen = newDirectory.isContentOpen();
            boolean attached = newDirectory.isAttached();
            newDirectory.createFolder();
        } catch (FileSystemException e) {
            throw new XenonException(adaptor.getName(), "Failed to create directory " + absolutePath, e);
        }
        LOGGER.debug("createDirectory OK");
    }

    @Override
    public void createFile(Path path) throws XenonException {
    }

    @Override
    public void delete(Path path) throws XenonException {
    }

    @Override
    public boolean exists(Path path) throws XenonException {
        LOGGER.debug("exists path = {}", path);
        FileObject fileSystem = getFileSystemByPath(path);
        String absolutePath = path.getRelativePath().getAbsolutePath();
        boolean result;
        try {
            FileObject fileObject = fileSystem.resolveFile(absolutePath);
            result = fileObject.exists();
        } catch (FileSystemException e) {
            throw new XenonException(adaptor.getName(), "Failed to inspect directory " + absolutePath, e);
        }
        LOGGER.debug("exists OK result = {}", result);
        return result;
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir) throws XenonException {
        return null;
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter filter) throws XenonException {
        return null;
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir) throws XenonException {
        return null;
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir, Filter filter) throws XenonException {
        return null;
    }

    @Override
    public InputStream newInputStream(Path path) throws XenonException {
        return null;
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws XenonException {
        return null;
    }

    @Override
    public FileAttributes getAttributes(Path path) throws XenonException {
        return null;
    }

    @Override
    public Path readSymbolicLink(Path link) throws XenonException {
        return null;
    }

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
    }

    public void end() {
    }

    private FileObject getFileSystemByPath(Path path) {
        FileSystemImplementation fileSystem = (FileSystemImplementation) path.getFileSystem();
        return fileSystems.get(fileSystem.getUniqueID()).getFileObject();
    }
}
