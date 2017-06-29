package nl.esciencecenter.xenon.adaptors.file.webdav;

import static nl.esciencecenter.xenon.adaptors.file.webdav.WebdavFileAdaptor.ADAPTOR_NAME;
import static nl.esciencecenter.xenon.adaptors.file.webdav.WebdavFileAdaptor.BUFFER_SIZE;
import static nl.esciencecenter.xenon.adaptors.file.webdav.WebdavFileAdaptor.OK_CODE;
import static nl.esciencecenter.xenon.adaptors.file.webdav.WebdavFileAdaptor.isOkish;
import static nl.esciencecenter.xenon.adaptors.file.webdav.WebdavFileAdaptor.toFolderPath;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
import org.apache.jackrabbit.webdav.client.methods.MoveMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.file.OpenOptions;
import nl.esciencecenter.xenon.files.CopyHandle;
import nl.esciencecenter.xenon.files.CopyDescription;
import nl.esciencecenter.xenon.files.CopyOption;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.DirectoryStream.Filter;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.InvalidOptionsException;
import nl.esciencecenter.xenon.files.NoSuchPathException;
import nl.esciencecenter.xenon.files.OpenOption;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PathAlreadyExistsException;
import nl.esciencecenter.xenon.files.PathAttributesPair;
import nl.esciencecenter.xenon.files.PosixFilePermission;

public class WebdavFileSystem extends FileSystem {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WebdavFileAdaptor.class);
    
	private final HttpClient client;
	
	protected WebdavFileSystem(String uniqueID, String name, String location, Path entryPath,
			HttpClient client, XenonProperties properties) {
		super(uniqueID, name, location, entryPath, properties);
		this.client = client;
	}

	// TODO: lift to FileSystem ?
    private boolean areSamePaths(Path source, Path target) {
        Path sourceName = source.normalize();
        Path targetName = target.normalize();
        return sourceName.equals(targetName);
    }

    private void assertRegularFileExists(Path path) throws XenonException {
        assertExists(path);
        if (getAttributes(path).isDirectory()) {
            String message = "Specified path should be a file but is a directory: " + path.toString();
            throw new XenonException(ADAPTOR_NAME, message);
        }
    }

    private void assertExists(Path path) throws XenonException {
        if (!exists(path)) {
            throw new NoSuchPathException(ADAPTOR_NAME, "Path does not exist " + path.toString());
        }
    }

    private void assertNotExists(Path path) throws XenonException {
        if (exists(path)) {
            throw new PathAlreadyExistsException(ADAPTOR_NAME, "Path already exists " + path.toString());
        }
    }

    private void assertIsEmpty(Path path) throws XenonException {
        DirectoryStream<Path> newDirectoryStream = newDirectoryStream(path);
        boolean hasNext = newDirectoryStream.iterator().hasNext();
        try {
            newDirectoryStream.close();
        } catch (IOException e) {
            LOGGER.warn("Could not close stream at {} because of IOException with message \"{}\"", path.toString(),
                    e.getMessage());
        }
        if (hasNext) {
            throw new XenonException(ADAPTOR_NAME, "Path is not empty: " + path.toString());
        }
    }
    
    
    private void assertValidArgumentsForNewOutputStream(Path path, OpenOptions processedOptions) throws XenonException {
        if (processedOptions.getReadMode() != null) {
            throw new InvalidOptionsException(ADAPTOR_NAME, "Disallowed open option: READ");
        }

        if (processedOptions.getAppendMode() == null) {
            throw new InvalidOptionsException(ADAPTOR_NAME, "No append mode provided!");
        }

        if (processedOptions.getAppendMode() == OpenOption.APPEND) {
            throw new InvalidOptionsException(ADAPTOR_NAME,
                    "Webdav adaptor does not support appending when writing files.");
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

    

    private String toFilePath(String path) {
        // Removes all, if any, trailing slashes
        return path.replaceAll("/+$", "");
    }
    
    private MultiStatusResponse[] getResponsesFromPropFindMethod(String folderPath, PropFindMethod method) throws XenonException {
        MultiStatus multiStatus = null;
        try {
            multiStatus = method.getResponseBodyAsMultiStatus();
        } catch (IOException | DavException e) {
            throwDirectoryListingException(folderPath, e);
        }
        return multiStatus.getResponses();
    }

    private void throwDirectoryListingException(String folderPath, Exception e) throws XenonException {
        throw new XenonException(ADAPTOR_NAME, "Could not create directory listing of " + folderPath, e);
    }
    
    private List<MultiStatusResponse> listDirectory(Path path) throws XenonException {
        assertExists(path);
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
    
    private void executeMethod(HttpClient client, HttpMethod method) throws IOException {
        int response = client.executeMethod(method);
        String responseBodyAsString = method.getStatusLine().toString();
        method.releaseConnection();
        if (!isOkish(response)) {
            throw new IOException(responseBodyAsString);
        }
    }

    private void executeDeleteMethod(String deletePath, HttpClient client) throws XenonException {
        DeleteMethod method = new DeleteMethod(deletePath);
        try {
            executeMethod(client, method);
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Could not delete path " + deletePath, e);
        }
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
            throw new XenonException(ADAPTOR_NAME, "Could not inspect path " + path, e);
        }
        try {
            executeMethod(client, method);
            properties = getProperties(method);
        } catch (IOException | DavException e) {
            throw new PathUninspectableException(ADAPTOR_NAME, "Could not inspect path " + path, e);
        }
        return properties;
    }

    private DavPropertySet getProperties(PropFindMethod method) throws IOException, DavException {
        MultiStatus document = method.getResponseBodyAsMultiStatus();
        MultiStatusResponse[] responses = document.getResponses();
        DavPropertySet properties = null;
        for (MultiStatusResponse multiStatusResponse : responses) {
            properties = multiStatusResponse.getProperties(OK_CODE);
        }
        return properties;
    }

	@Override
	public void close() throws XenonException {
		// ignored
	}

	@Override
	public boolean isOpen() throws XenonException {
		return true;
	}

	@Override
	public void move(Path source, Path target) throws XenonException {
		
		LOGGER.debug("move source = {} to target = {}", source, target);
		
		if (areSamePaths(source, target)) {
			return;
		}

		assertExists(source);
		String sourcePath;
		String targetPath;
		if (getAttributes(source).isDirectory()) {
			sourcePath = toFolderPath(source.toString());
			targetPath = toFolderPath(target.toString());
		} else {
			sourcePath = toFilePath(source.toString());
			targetPath = toFilePath(target.toString());
		}
		
		MoveMethod method = new MoveMethod(sourcePath, targetPath, false);
		try {
			executeMethod(client, method);
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Could not move " + sourcePath + " to " + targetPath, e);
		}
		LOGGER.debug("move OK");
	}

	@Override
	public void createDirectory(Path dir) throws XenonException {
		LOGGER.debug("createDirectory dir = {}", dir);
		String folderPath = toFolderPath(dir.toString());
		DavMethod method = new MkColMethod(folderPath);
		try {
			executeMethod(client, method);
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Could not create directory " + folderPath, e);
		}
		LOGGER.debug("createDirectory OK");
	}

	@Override
	public void createFile(Path file) throws XenonException {
		createFile(file, new byte[0]);
	}
	
	public void createFile(Path file, byte [] data) throws XenonException {
	    LOGGER.debug("createFile path = {}", file);
        assertNotExists(file);
        String filePath = toFilePath(file.toString());
        PutMethod method = new PutMethod(filePath);
        method.setRequestEntity(new InputStreamRequestEntity(new ByteArrayInputStream(data)));
        try {
            executeMethod(client, method);
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Could not create file " + filePath, e);
        }
        LOGGER.debug("createFile OK");
    }

	public void createFile(Path file, InputStream data) throws XenonException {
	    LOGGER.debug("createFile path = {}", file);
        assertNotExists(file);
        String filePath = toFilePath(file.toString());
        PutMethod method = new PutMethod(filePath);
        method.setRequestEntity(new InputStreamRequestEntity(data));
        try {
            executeMethod(client, method);
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Could not create file " + filePath, e);
        }
        LOGGER.debug("createFile OK");
    }

	
	@Override
	public void delete(Path path) throws XenonException {
		LOGGER.debug("delete path = {}", path);
        assertExists(path);
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

	@Override
	public boolean exists(Path path) throws XenonException {
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
	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter filter) throws XenonException {
		LOGGER.debug("newDirectoryStream path = {} filter = <?>", dir);
		
		if (filter == null) {
			throw new XenonException(ADAPTOR_NAME, "Filter cannot be null.");
		}

		return new WebdavDirectoryStream(dir, filter, listDirectory(dir));
	}

	@Override
	public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir, Filter filter)
			throws XenonException {
		LOGGER.debug("newAttributesDirectoryStream path = {} filter = <?>", dir);
		if (dir == null) {
			throw new XenonException(ADAPTOR_NAME, "Cannot open attribute directory stream of null path");
		}
		if (filter == null) {
			throw new XenonException(ADAPTOR_NAME, "Filter cannot be null.");
		}
		return new WebdavDirectoryAttributeStream(dir, filter, listDirectory(dir));
	}

	@Override
	public InputStream newInputStream(Path path) throws XenonException {
		String filePath = toFilePath(path.toString());
		assertRegularFileExists(path);
		GetMethod method = new GetMethod(filePath);
		try {
			client.executeMethod(method);
			return method.getResponseBodyAsStream();
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Could not open inputstream to " + filePath, e);
		}
	}

	@Override
	public OutputStream newOutputStream(Path path, OpenOption... options) throws XenonException {
		OpenOptions tmp = OpenOptions.processOptions(ADAPTOR_NAME, options);
        assertValidArgumentsForNewOutputStream(path, tmp);
        if (tmp.getOpenMode() == OpenOption.CREATE) {
            assertNotExists(path);
        } else {
            assertRegularFileExists(path);
        }

        return new WebdavOutputStream(path, this);
	}

	@Override
	public FileAttributes getAttributes(Path path) throws XenonException {
		LOGGER.debug("getAttributes path = {}", path);
		FileAttributes fileAttributes = getFileOrDirAttributes(path, client);
		LOGGER.debug("getAttributes OK result = {}", fileAttributes);
		return fileAttributes;
	}

	@Override
	public Path readSymbolicLink(Path link) throws XenonException {
		throw new XenonException(ADAPTOR_NAME, "Operation not supported");
	}

	@Override
	public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
		throw new XenonException(ADAPTOR_NAME, "Operation not supported");
	}
	
	private void streamCopy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int size = in.read(buffer);
        while (size > 0) {
            out.write(buffer, 0, size);
            size = in.read(buffer);
        }
    }

    @Override
    public CopyHandle copy(CopyDescription description) throws XenonException {
    	
    	LOGGER.debug("copy description = {} ", description);
        
        Path source = description.getSourcePath();
        Path target = description.getDestinationPath();
        
        CopyOption option = description.getOption();
        
        assertValidOptionsForCopy(option);
        
        // Extra check just to match behavior of other files adaptors and tests.
        if (areSamePaths(description.getSourcePath(), target) && CopyOption.CREATE.equals(option)) {
            return null;
        }

        if (exists(target)) { 
        	if (CopyOption.CREATE.equals(option)) { 
        		throw new PathAlreadyExistsException(ADAPTOR_NAME, "Destination path already exists: " + target);
        	}
        	
        	if (CopyOption.IGNORE.equals(option)) { 
        		return null;
        	}
        }
        
        /* TODO: Test if this works -- and move into separate thread 
         
        String filePath = toFilePath(target.toString());
        PutMethod method = new PutMethod(filePath);
        method.setRequestEntity(new PathRequestEntity(this, source));
        
        try {
            executeMethod(client, method);
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Could not create file " + filePath, e);
        }
        */
        
        InputStream inputStream = newInputStream(source);
        
        OutputStream outputStream = newOutputStream(target, OpenOption.OPEN_OR_CREATE, OpenOption.TRUNCATE, OpenOption.WRITE);
      
        try {
            streamCopy(inputStream, outputStream);
            inputStream.close();
            // NB. Webdav output stream buffers all and writes it all at once upon close.
            outputStream.close();
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Copy failed!", e);
        }
        
        CopyHandle result = null;
        LOGGER.debug("copy OK result = {}", result);
        return result;

    }

    private void assertValidOptionsForCopy(CopyOption option) throws InvalidOptionsException {
        if (CopyOption.APPEND.equals(option)) {
            throw new InvalidOptionsException(ADAPTOR_NAME,
                    "Webdav adaptor does not support appending when copying files.");
        }
        if (CopyOption.RESUME.equals(option)) {
            throw new InvalidOptionsException(ADAPTOR_NAME,
                    "Webdav adaptor does not support resuming when copying files.");
        }
        if (CopyOption.VERIFY_AND_RESUME.equals(option)) {
            throw new InvalidOptionsException(ADAPTOR_NAME,
                    "Webdav adaptor does not support resuming when copying files.");
        }
    }

    /*
    @Override
    public CopyStatus getCopyStatus(Copy copy) throws XenonException {
        throw new XenonException(ADAPTOR_NAME, "Webdav adaptor does not support copy status requests.");
    }

    @Override
    public CopyStatus cancelCopy(Copy copy) throws XenonException {
        throw new XenonException(ADAPTOR_NAME, "Webdav adaptor does not support canceling a copy.");
    }
     */
 	
}
