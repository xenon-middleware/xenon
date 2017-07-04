package nl.esciencecenter.xenon.adaptors.filesystems.webdav;

import static nl.esciencecenter.xenon.adaptors.filesystems.webdav.WebdavFileAdaptor.ADAPTOR_NAME;
import static nl.esciencecenter.xenon.adaptors.filesystems.webdav.WebdavFileAdaptor.OK_CODE;
import static nl.esciencecenter.xenon.adaptors.filesystems.webdav.WebdavFileAdaptor.isFolderPath;
import static nl.esciencecenter.xenon.adaptors.filesystems.webdav.WebdavFileAdaptor.isOkish;
import static nl.esciencecenter.xenon.adaptors.filesystems.webdav.WebdavFileAdaptor.toFilePath;
import static nl.esciencecenter.xenon.adaptors.filesystems.webdav.WebdavFileAdaptor.toFolderPath;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
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
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.filesystems.PathAttributes;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.filesystems.PosixFilePermission;

public class WebdavFileSystem extends FileSystem {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebdavFileAdaptor.class);

	private static final String CURRENT_DIR_SYMBOL = ".";

	private static final String CREATION_DATE_KEY = "creationdate";
	private static final String MODIFIED_DATE_KEY = "getlastmodified";
	private static final String CONTENT_LENGTH = "getcontentlength";

	private final HttpClient client;

	protected WebdavFileSystem(String uniqueID, String name, String location, Path entryPath,
			HttpClient client, XenonProperties properties) {
		super(uniqueID, name, location, entryPath, properties);
		this.client = client;
	}

	//	private void assertRegularFileExists(Path path) throws XenonException {
	//        assertExists(path);
	//        if (getAttributes(path).isDirectory()) {
	//            String message = "Specified path should be a file but is a directory: " + path.toString();
	//            throw new XenonException(ADAPTOR_NAME, message);
	//        }
	//    }

	//    private void assertExists(Path path) throws XenonException {
	//        if (!exists(path)) {
	//            throw new NoSuchPathException(ADAPTOR_NAME, "Path does not exist " + path.toString());
	//        }
	//    }

	//    private void assertNotExists(Path path) throws XenonException {
	//        if (exists(path)) {
	//            throw new PathAlreadyExistsException(ADAPTOR_NAME, "Path already exists " + path.toString());
	//        }
	//    }

	//    private void assertIsEmpty(Path path) throws XenonException {
	//        DirectoryStream<Path> newDirectoryStream = newDirectoryStream(path);
	//        boolean hasNext = newDirectoryStream.iterator().hasNext();
	//        try {
	//            newDirectoryStream.close();
	//        } catch (IOException e) {
	//            LOGGER.warn("Could not close stream at {} because of IOException with message \"{}\"", path.toString(),
	//                    e.getMessage());
	//        }
	//        if (hasNext) {
	//            throw new XenonException(ADAPTOR_NAME, "Path is not empty: " + path.toString());
	//        }
	//    }
	//    
	//    
	//    private void assertValidArgumentsForNewOutputStream(Path path, OpenOptions processedOptions) throws XenonException {
	//        if (processedOptions.getReadMode() != null) {
	//            throw new InvalidOptionsException(ADAPTOR_NAME, "Disallowed open option: READ");
	//        }
	//
	//        if (processedOptions.getAppendMode() == null) {
	//            throw new InvalidOptionsException(ADAPTOR_NAME, "No append mode provided!");
	//        }
	//
	//        if (processedOptions.getAppendMode() == OpenOption.APPEND) {
	//            throw new InvalidOptionsException(ADAPTOR_NAME,
	//                    "Webdav adaptor does not support appending when writing files.");
	//        }
	//
	//        if (processedOptions.getWriteMode() == null) {
	//            processedOptions.setWriteMode(OpenOption.WRITE);
	//        }
	//
	//        if (processedOptions.getOpenMode() == OpenOption.CREATE) {
	//            assertNotExists(path);
	//        } else if (processedOptions.getOpenMode() == OpenOption.OPEN) {
	//            assertRegularFileExists(path);
	//        }
	//    }
	//


	
	
	private MultiStatusResponse[] getResponsesFromPropFindMethod(String folderPath, PropFindMethod method) throws XenonException {
		MultiStatus multiStatus = null;
		try {
			multiStatus = method.getResponseBodyAsMultiStatus();
		} catch (IOException | DavException e) {
			throw new XenonException(ADAPTOR_NAME, "Could not create directory listing of " + folderPath, e);
		}
		return multiStatus.getResponses();
	}
	//
	//    private void throwDirectoryListingException(String folderPath, Exception e) throws XenonException {
	//    }

	private Object getProperty(DavPropertySet properties, String name) {
		DavPropertyName propertyName = DavPropertyName.create(name);
		DavProperty<?> davProperty = properties.get(propertyName);
		return davProperty == null ? null : davProperty.getValue();
	}

	private long getTimeProperty(DavPropertySet properties, String name, long defaultValue) {

		Object property = getProperty(properties, name);
		if (property == null) {
			return defaultValue;
		}
		DateTime dateTime = DateTime.parse((String) property);
		return dateTime.getMillis();
	}

	private long getLongProperty(DavPropertySet properties, String name, long defaultValue) {

		Object property = getProperty(properties, name);
		
		if (property == null) {
			return defaultValue;
		}
		
		try {
            return Long.parseLong((String) property);
        } catch (NumberFormatException e) {
            // Unable to determine size, return default.
            return defaultValue;
        }
	}


	private String getFileNameFromEntry(MultiStatusResponse entry, Path parentPath) {
		Path entryPath = new Path(entry.getHref());
		Path displacement = parentPath.relativize(entryPath);
		return displacement.isEmpty() ? CURRENT_DIR_SYMBOL : entryPath.getFileNameAsString();
	}

	private PathAttributes getAttributes(Path path, DavPropertySet p, boolean isDirectory) { 

		PathAttributes attributes = new PathAttributes();
		
		attributes.setPath(path);
		attributes.setDirectory(isDirectory);
		attributes.setRegular(!isDirectory);
		
		attributes.setCreationTime(getTimeProperty(p, CREATION_DATE_KEY, 0L));
		attributes.setLastAccessTime(getTimeProperty(p, MODIFIED_DATE_KEY, 0L));
		attributes.setLastModifiedTime(getTimeProperty(p, MODIFIED_DATE_KEY, 0L));
		attributes.setSize(getLongProperty(p, CONTENT_LENGTH, 0L));

		// TODO: no clue if this is right ?
		attributes.setReadable(true);
		attributes.setWritable(false);
		
		return attributes;
	}


	@Override
	protected List<PathAttributes> listDirectory(Path path)  throws XenonException {

		String folderPath = toFolderPath(path.toString());
		PropFindMethod method = null;
		try {
			method = new PropFindMethod(folderPath, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
			client.executeMethod(method);
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Could not create directory listing of " + folderPath, e);
		}
		
		MultiStatusResponse[] responses = getResponsesFromPropFindMethod(folderPath, method);

		ArrayList<PathAttributes> result = new ArrayList<>(responses.length);
		
		for (MultiStatusResponse r : responses) { 
			
			String filename = getFileNameFromEntry(r, path);
			DavPropertySet p = r.getProperties(WebdavFileAdaptor.OK_CODE);
			result.add(getAttributes(path.resolve(filename), p, isFolderPath(filename)));
		}

		return result;
	}

//
//
//	private List<MultiStatusResponse> listDirectory(Path path) throws XenonException {
//		assertPathExists(path);
//
//		String folderPath = toFolderPath(path.toString());
//		PropFindMethod method = null;
//		try {
//			method = new PropFindMethod(folderPath, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
//			client.executeMethod(method);
//		} catch (IOException e) {
//			throw new XenonException(ADAPTOR_NAME, "Could not create directory listing of " + folderPath, e);
//		}
//		MultiStatusResponse[] responses = getResponsesFromPropFindMethod(folderPath, method);
//		return new LinkedList<>(Arrays.asList(responses));
//	}

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
			throw new XenonException(ADAPTOR_NAME, "Could not inspect path " + path, e);
		}
		return properties;
	}

	private PathAttributes getFileOrDirAttributes(Path path, HttpClient client) throws XenonException {
		try {
			String folderPath = toFolderPath(path.toString());
			return getAttributes(path, getPathProperties(client, folderPath), true);
		} catch (XenonException e) {
			String filePath = toFilePath(path.toString());
			return getAttributes(path, getPathProperties(client, filePath), false);
		}
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

		assertPathExists(source);

		if (areSamePaths(source, target)) {
			return;
		}

		assertParentDirectoryExists(source);
		assertPathNotExists(target);

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

		assertPathNotExists(dir);

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
		createFile(file, 0, new ByteArrayInputStream(new byte[0]));
	}

//	public void createFile(Path file, byte [] data) throws XenonException {
//		createFile(file, new ByteArrayInputStream(data));
//	}
//
//	public void createFile(Path file, InputStream data) throws XenonException {
//		LOGGER.debug("createFile path = {}", file);
//		assertPathNotExists(file);
//		String filePath = toFilePath(file.toString());
//		PutMethod method = new PutMethod(filePath);
//		method.setRequestEntity(new InputStreamRequestEntity(data));
//		try {
//			executeMethod(client, method);
//		} catch (IOException e) {
//			throw new XenonException(ADAPTOR_NAME, "Could not create file " + filePath, e);
//		}
//		LOGGER.debug("createFile OK");
//	}

	private void createFile(Path file, long size, InputStream data) throws XenonException {
		LOGGER.debug("createFile path = {}", file);
		assertPathNotExists(file);
		String filePath = toFilePath(file.toString());
		PutMethod method = new PutMethod(filePath);
		method.setRequestEntity(new InputStreamRequestEntity(data, size));
		try {
			executeMethod(client, method);
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Could not create file " + filePath, e);
		}
		LOGGER.debug("createFile OK");
	}

	
	@Override
	protected void deleteFile(Path path) throws XenonException {
		executeDeleteMethod(toFilePath(path.toString()), client);
	}

	@Override
	protected void deleteDirectory(Path path) throws XenonException {
		executeDeleteMethod(toFolderPath(path.toString()), client);
	}

	@Override
	public boolean exists(Path path) throws XenonException {
		LOGGER.debug("exists path = {}", path);
		try {
			getAttributes(path);
			return true;
		} catch (XenonException e) {
			// getAttributes did not find evidence that the specified path exists
			return false;
		}
	}

	//	@Override
	//	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter filter) throws XenonException {
	//		LOGGER.debug("newDirectoryStream path = {} filter = <?>", dir);
	//		
	//		if (filter == null) {
	//			throw new XenonException(ADAPTOR_NAME, "Filter cannot be null.");
	//		}
	//
	//		return new WebdavDirectoryStream(dir, filter, listDirectory(dir));
	//	}
	//
	//	@Override
	//	public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir, Filter filter)
	//			throws XenonException {
	//		LOGGER.debug("newAttributesDirectoryStream path = {} filter = <?>", dir);
	//		if (dir == null) {
	//			throw new XenonException(ADAPTOR_NAME, "Cannot open attribute directory stream of null path");
	//		}
	//		if (filter == null) {
	//			throw new XenonException(ADAPTOR_NAME, "Filter cannot be null.");
	//		}
	//		return new WebdavDirectoryAttributeStream(dir, filter, listDirectory(dir));
	//	}

	@Override
	public InputStream readFromFile(Path path) throws XenonException {
		String filePath = toFilePath(path.toString());
		assertFileExists(path);
		GetMethod method = new GetMethod(filePath);
		try {
			client.executeMethod(method);
			return method.getResponseBodyAsStream();
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Could not open inputstream to " + filePath, e);
		}
	}

	@Override
	public OutputStream writeToFile(Path file, long size) throws XenonException {
	
		try { 
			PipedInputStream in = new PipedInputStream(4096);
			PipedOutputStream out = new PipedOutputStream(in);
		
			createFile(file, size, in);
		
			return out;
		} catch (Exception e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to open stream for writing", e);
		}
	}

	@Override
	public OutputStream appendToFile(Path file) throws XenonException {
		throw new XenonException(ADAPTOR_NAME, "Appending to file not supported");
	}
	
	@Override
	public OutputStream writeToFile(Path file) throws XenonException {
		return writeToFile(file, -1);
	}

	@Override
	public PathAttributes getAttributes(Path path) throws XenonException {
		return getFileOrDirAttributes(path, client);
	}

	@Override
	public Path readSymbolicLink(Path link) throws XenonException {
		throw new XenonException(ADAPTOR_NAME, "Operation not supported");
	}

	@Override
	public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
		throw new XenonException(ADAPTOR_NAME, "Operation not supported");
	}

//	private void streamCopy(InputStream in, OutputStream out) throws IOException {
//		byte[] buffer = new byte[BUFFER_SIZE];
//		int size = in.read(buffer);
//		while (size > 0) {
//			out.write(buffer, 0, size);
//			size = in.read(buffer);
//		}
//	}

//	private void assertValidOptionsForCopy(CopyOption option) throws InvalidOptionsException {
//		if (CopyOption.APPEND.equals(option)) {
//			throw new InvalidOptionsException(ADAPTOR_NAME,
//					"Webdav adaptor does not support appending when copying files.");
//		}
//		if (CopyOption.RESUME.equals(option)) {
//			throw new InvalidOptionsException(ADAPTOR_NAME,
//					"Webdav adaptor does not support resuming when copying files.");
//		}
//		if (CopyOption.VERIFY_AND_RESUME.equals(option)) {
//			throw new InvalidOptionsException(ADAPTOR_NAME,
//					"Webdav adaptor does not support resuming when copying files.");
//		}
//	}

//	@Override
//	public CopyHandle copy(CopyDescription description) throws XenonException {

//		LOGGER.debug("copy description = {} ", description);
//
//		Path source = description.getSourcePath();
//		Path target = description.getDestinationPath();
//
//		CopyOption option = description.getOption();
//
//		assertValidOptionsForCopy(option);
//
//		// Extra check just to match behavior of other files adaptors and tests.
//		if (areSamePaths(description.getSourcePath(), target) && CopyOption.CREATE.equals(option)) {
//			return null;
//		}
//
//		if (exists(target)) { 
//			if (CopyOption.CREATE.equals(option)) { 
//				throw new PathAlreadyExistsException(ADAPTOR_NAME, "Destination path already exists: " + target);
//			}
//
//			if (CopyOption.IGNORE.equals(option)) { 
//				return null;
//			}
//		}

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

//		InputStream inputStream = newInputStream(source);
//
//		OutputStream outputStream = newOutputStream(target, OpenOption.OPEN_OR_CREATE, OpenOption.TRUNCATE, OpenOption.WRITE);
//
//		try {
//			streamCopy(inputStream, outputStream);
//			inputStream.close();
//			// NB. Webdav output stream buffers all and writes it all at once upon close.
//			outputStream.close();
//		} catch (IOException e) {
//			throw new XenonException(ADAPTOR_NAME, "Copy failed!", e);
//		}
//
//		CopyHandle result = null;
//		LOGGER.debug("copy OK result = {}", result);
//		return result;
//
//		return null;
//		
//	}
//
//	@Override
//	public CopyStatus getStatus(CopyHandle copy) throws XenonException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public CopyStatus cancel(CopyHandle copy) throws XenonException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public CopyStatus waitUntilDone(CopyHandle copy, long timeout) throws XenonException {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
