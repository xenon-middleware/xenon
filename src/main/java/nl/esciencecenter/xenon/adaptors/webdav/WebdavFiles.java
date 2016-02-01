package nl.esciencecenter.xenon.adaptors.webdav;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
import org.apache.jackrabbit.webdav.client.methods.OptionsMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonProperties;
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

public class WebdavFiles implements Files {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebdavFiles.class);
    private final WebdavAdaptor adaptor;

    private final Map<String, FileSystemInfo> fileSystems = Collections.synchronizedMap(new HashMap<String, FileSystemInfo>());

    private static int currentID = 1;
    static int OK = 200;

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
        private final HttpClient client;

        public FileSystemInfo(FileSystemImplementation impl, HttpClient client, Credential credential) {
            super();
            this.impl = impl;
            this.credential = credential;
            this.client = client;
        }

        public HttpClient getClient() {
            return client;
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

        WebdavLocation webdavLocation = new WebdavLocation(location, scheme);

        HttpClient client = getClient(webdavLocation);
        HttpMethod method;
        try {
            method = new OptionsMethod(toFolderPath(webdavLocation.toString())); // TODO
            int response = client.executeMethod(method);
            String responseBodyAsString = method.getStatusLine().toString();
            method.releaseConnection();
            if (!isOkish(response)) {
                throw new IOException(responseBodyAsString);
            }
        } catch (IOException e) {
            throw new XenonException(adaptor.getName(), "Could not open connection to " + location, e);
        }

        String cwd = webdavLocation.getPath();
        RelativePath entryPath = new RelativePath(cwd);
        String uniqueID = getNewUniqueID();
        XenonProperties xenonProperties = new XenonProperties(adaptor.getSupportedProperties(Component.FILESYSTEM), properties);
        FileSystemImplementation fileSystem = new FileSystemImplementation(adaptor.getName(), uniqueID, scheme,
                webdavLocation.getHost(), entryPath, credential, xenonProperties);
        fileSystems.put(uniqueID, new FileSystemInfo(fileSystem, client, credential));
        LOGGER.debug("* newFileSystem OK remote cwd = {} entryPath = {} uniqueID = {}", cwd, entryPath, uniqueID);
        return fileSystem;
    }

    private HttpClient getClient(WebdavLocation webdavLocation) {
        HostConfiguration hostConfig = new HostConfiguration();
        hostConfig.setHost(webdavLocation.getHost(), webdavLocation.getPort());
        HttpConnectionManager connectionManager = getConnectionManager(hostConfig);
        HttpClient client = new HttpClient(connectionManager);
        Credentials creds = new UsernamePasswordCredentials("xenon", "xenon1");
        client.getState().setCredentials(AuthScope.ANY, creds);
        client.setHostConfiguration(hostConfig);
        return client;
    }

    private HttpConnectionManager getConnectionManager(HostConfiguration hostConfig) {
        HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        int maxHostConnections = 20;
        params.setMaxConnectionsPerHost(hostConfig, maxHostConnections);
        connectionManager.setParams(params);
        return connectionManager;
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
        boolean result = true;
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
        HttpClient client = getFileSystemByPath(path);
        String folderPath = toFolderPath(path.toString());
        DavMethod method = new MkColMethod(folderPath);
        try {
            executeMethod(client, method);
        } catch (IOException e) {
            throw new XenonException(adaptor.getName(), "Could not create directory " + folderPath, e);
        }
        LOGGER.debug("createDirectory OK");
    }

    @Override
    public void createFile(Path path) throws XenonException {
        createFile(path, new byte[0]);
    }

    public void createFile(Path path, byte[] data) throws XenonException {
        LOGGER.debug("createFile path = {}", path);
        assertNotExists(path);
        HttpClient client = getFileSystemByPath(path);
        String filePath = toFilePath(path.toString());
        PutMethod method = new PutMethod(filePath);
        method.setRequestEntity(new InputStreamRequestEntity(new ByteArrayInputStream(data)));
        try {
            executeMethod(client, method);
        } catch (IOException e) {
            throw new XenonException(adaptor.getName(), "Could not create file " + filePath, e);
        }
        LOGGER.debug("createFile OK");
    }

    @Override
    public void delete(Path path) throws XenonException {
        LOGGER.debug("delete path = {}", path);
        assertExists(path);
        HttpClient client = getFileSystemByPath(path);
        FileAttributes attributes = getAttributes(path);
        if (attributes.isDirectory()) {
            assertIsEmpty(path);
            String folderPath = toFolderPath(path.toString());
            executeDeleteMethod(folderPath, client);
        } else {
            executeDeleteMethod(toFilePath(path.toString()), client);
        }
        LOGGER.debug("delete OK");
    }

    private void executeDeleteMethod(String deletePath, HttpClient client) throws XenonException {
        DeleteMethod method = new DeleteMethod(deletePath);
        try {
            executeMethod(client, method);
        } catch (IOException e) {
            throw new XenonException(adaptor.getName(), "Could not delete path " + deletePath, e);
        }
    }

    private String getFileOrFolderPath(Path path) throws XenonException {
        FileAttributes attributes = getAttributes(path);
        if (attributes.isDirectory()) {
            return toFolderPath(path.toString());
        } else {
            return toFilePath(path.toString());
        }
    }

    @Override
    public boolean exists(Path path) {
        LOGGER.debug("exists path = {}", path);
        boolean result = false;
        try {
            getAttributes(path);
            result = true;
        } catch (XenonException e) {
            // getAttributes did not find evidence that the specified path exists
        }
        LOGGER.debug("exists OK result = {}", result);
        return result;
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path path) throws XenonException {
        return newDirectoryStream(path, FilesEngine.ACCEPT_ALL_FILTER);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path path, Filter filter) throws XenonException {
        LOGGER.debug("newDirectoryStream path = {} filter = <?>", path);
        if (filter == null) {
            throw new XenonException(adaptor.getName(), "Filter cannot be null.");
        }

        List<MultiStatusResponse> responses = listDirectory(path);

        DirectoryStream<Path> result = new WebdavDirectoryStream(path, filter, responses);
        LOGGER.debug("newDirectoryStream OK");
        return result;
    }

    private List<MultiStatusResponse> listDirectory(Path path) throws XenonException {
        assertExists(path);
        HttpClient client = getFileSystemByPath(path);
        String folderPath = toFolderPath(path.toString());
        PropFindMethod method = null;
        try {
            method = new PropFindMethod(folderPath, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
            client.executeMethod(method);
        } catch (IOException e) {
            throwDirectoryListingException(folderPath, e);
        }
        MultiStatusResponse[] responses = getResponsesFromPropFindMethod(folderPath, method);
        return new LinkedList<>(Arrays.asList(responses));
    }

    private MultiStatusResponse[] getResponsesFromPropFindMethod(String folderPath, PropFindMethod method) throws XenonException {
        MultiStatus multiStatus = null;
        try {
            multiStatus = method.getResponseBodyAsMultiStatus();
        } catch (IOException | DavException e) {
            throwDirectoryListingException(folderPath, e);
        }
        MultiStatusResponse[] responses = multiStatus.getResponses();
        return responses;
    }

    private void throwDirectoryListingException(String folderPath, Exception e) throws XenonException {
        throw new XenonException(adaptor.getName(), "Could not create directory listing of " + folderPath, e);
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
        if (filter == null) {
            throw new XenonException(adaptor.getName(), "Filter cannot be null.");
        }
        return new WebdavDirectoryAttributeStream(path, filter, listDirectory(path));
    }

    @Override
    public InputStream newInputStream(Path path) throws XenonException {
        String filePath = toFilePath(path.toString());
        assertRegularFileExists(path);
        HttpClient client = getFileSystemByPath(path);
        GetMethod method = new GetMethod(filePath);
        try {
            //            executeMethod(client, method);
            client.executeMethod(method);
            InputStream stream = method.getResponseBodyAsStream();
            return stream;
        } catch (IOException e) {
            throw new XenonException(adaptor.getName(), "Could not open inputstream to " + filePath, e);
        }
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws XenonException {
        OpenOptions tmp = OpenOptions.processOptions(adaptor.getName(), options);
        assertValidArgumentsForNewOutputStream(path, tmp);
        if (tmp.getOpenMode() == OpenOption.CREATE) {
            assertNotExists(path);
        } else {
            assertRegularFileExists(path);
        }

        return new WebdavOutputStream(path, this);
    }

    private void assertValidArgumentsForNewOutputStream(Path path, OpenOptions processedOptions) throws XenonException {
        if (processedOptions.getReadMode() != null) {
            throw new InvalidOpenOptionsException(adaptor.getName(), "Disallowed open option: READ");
        }

        if (processedOptions.getAppendMode() == null) {
            throw new InvalidOpenOptionsException(adaptor.getName(), "No append mode provided!");
        }

        if (processedOptions.getAppendMode() == OpenOption.APPEND) {
            throw new XenonException(adaptor.getName(), "Webdav adaptor does not support appending when writing files.");
        }

        if (processedOptions.getWriteMode() == null) {
            processedOptions.setWriteMode(OpenOption.WRITE);
        }

        if (processedOptions.getOpenMode() == OpenOption.CREATE) {
            assertNotExists(path);
        } else if (processedOptions.getOpenMode() == OpenOption.OPEN) {
            assertRegularFileExists(path);
        }
    }

    @Override
    public FileAttributes getAttributes(Path path) throws XenonException {
        LOGGER.debug("getAttributes path = {}", path);
        HttpClient client = getFileSystemByPath(path);
        FileAttributes fileAttributes = getFileOrDirAttributes(path, client);
        LOGGER.debug("getAttributes OK result = {}", fileAttributes);
        return fileAttributes;
    }

    private FileAttributes getFileOrDirAttributes(Path path, HttpClient client) throws XenonException {
        WebdavFileAttributes fileAttributes;
        try {
            String folderPath = toFolderPath(path.toString());
            DavPropertySet properties = getPathProperties(client, folderPath);
            fileAttributes = new WebdavDirectoryAttributes(properties);
        } catch (PathUninspectableException e) {
            String filePath = toFilePath(path.toString());
            DavPropertySet properties = getPathProperties(client, filePath);
            fileAttributes = new WebdavRegularFileAttributes(properties);
        }
        return fileAttributes;
    }

    private DavPropertySet getPathProperties(HttpClient client, String path) throws XenonException {
        PropFindMethod method;
        DavPropertySet properties = null;
        try {
            method = new PropFindMethod(path, DavPropertyNameSet.PROPFIND_ALL_PROP_INCLUDE, DavPropertyNameSet.DEPTH_1);
        } catch (IOException e) {
            throw new XenonException(adaptor.getName(), "Could not inspect path " + path, e);
        }
        try {
            executeMethod(client, method);
            properties = getProperties(method);
        } catch (IOException | DavException e) {
            throw new PathUninspectableException(adaptor.getName(), "Could not inspect path " + path, e);
        }
        return properties;
    }

    private DavPropertySet getProperties(PropFindMethod method) throws IOException, DavException {
        MultiStatus document = method.getResponseBodyAsMultiStatus();
        MultiStatusResponse[] responses = document.getResponses();
        DavPropertySet properties = null;
        for (MultiStatusResponse multiStatusResponse : responses) {
            properties = multiStatusResponse.getProperties(OK);
        }
        return properties;
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

    private void assertIsEmpty(Path path) throws XenonException {
        DirectoryStream<Path> newDirectoryStream = newDirectoryStream(path);
        boolean isEmpty = newDirectoryStream.iterator().hasNext() == false;
        try {
            newDirectoryStream.close();
        } catch (IOException e) {
            LOGGER.warn("Could not close stream at {} because of IOException with message \"{}\"", path.toString(),
                    e.getMessage());
        }
        if (isEmpty == false) {
            throw new XenonException(adaptor.getName(), "Path is not empty: " + path.toString());
        }
    }

    private void assertRegularFileExists(Path path) throws XenonException {
        assertExists(path);
        if (getAttributes(path).isDirectory()) {
            String message = "Specified path should be a file but is a directory: " + path.toString();
            throw new XenonException(adaptor.getName(), message);
        }
    }

    private void assertExists(Path path) throws XenonException {
        if (!exists(path)) {
            throw new NoSuchPathException(adaptor.getName(), "Path does not exist " + path.toString());
        }
    }

    private void assertNotExists(Path path) throws XenonException {
        if (exists(path)) {
            throw new PathAlreadyExistsException(adaptor.getName(), "Path already exists " + path.toString());
        }
    }

    private String toFolderPath(String path) {
        return path.endsWith("/") ? path : path + "/";
    }

    private String toFilePath(String path) {
        // Removes all, if any, trailing slashes
        return path.replaceAll("/+$", "");
    }

    private HttpClient getFileSystemByPath(Path path) {
        FileSystemImplementation fileSystem = (FileSystemImplementation) path.getFileSystem();
        return fileSystems.get(fileSystem.getUniqueID()).getClient();
    }

    public static void executeMethod(HttpClient client, HttpMethod method) throws IOException, HttpException {
        int response = client.executeMethod(method);
        String responseBodyAsString = method.getStatusLine().toString();
        method.releaseConnection();
        if (!isOkish(response)) {
            throw new IOException(responseBodyAsString);
        }
    }

    private static boolean isOkish(int response) {
        return response == HttpStatus.SC_OK || response == HttpStatus.SC_CREATED || response == HttpStatus.SC_MULTI_STATUS
                || response == HttpStatus.SC_NO_CONTENT;
    }

}
