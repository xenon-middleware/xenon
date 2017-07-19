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
package nl.esciencecenter.xenon.adaptors.filesystems.webdav;

import static nl.esciencecenter.xenon.adaptors.filesystems.webdav.WebdavFileAdaptor.ADAPTOR_NAME;
import static nl.esciencecenter.xenon.adaptors.filesystems.webdav.WebdavFileAdaptor.OK_CODE;
import static nl.esciencecenter.xenon.adaptors.filesystems.webdav.WebdavFileAdaptor.isOkish;

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
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.UnsupportedOperationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.filesystems.PathAttributes;
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
    
	private String toFolderPath(Path path) {
		return getLocation() + path.getAbsolutePath() + "/";
    }

	private String toFilePath(Path path) {
		return getLocation() + path.getAbsolutePath();
	}
    
    private boolean isFolderPath(String path) {
        return path.endsWith("/");
    }
	
	private MultiStatusResponse[] getResponsesFromPropFindMethod(String folderPath, PropFindMethod method) throws XenonException {
		MultiStatus multiStatus = null;
		try {
			multiStatus = method.getResponseBodyAsMultiStatus();
		} catch (IOException | DavException e) {
			throw new XenonException(ADAPTOR_NAME, "Could not create directory listing of " + folderPath, e);
		}
		return multiStatus.getResponses();
	}

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
		
		// "Tue, 18 Jul 2017 15:23:01 GMT"
		try { 
			DateTime dateTime = DateTime.parse((String) property);
			return dateTime.getMillis();
		} catch (Exception e) {
			String format = "E, d MMMM yyyy H:m:s z";
			DateTime dateTime = DateTime.parse((String) property, DateTimeFormat.forPattern(format));
			return dateTime.getMillis();
		}
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

		String folderPath = toFolderPath(path);
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


	private void executeMethod(HttpClient client, HttpMethod method) throws IOException {
		
		System.out.println("Client: " + client + " " + method);
		
		int response = client.executeMethod(method);
		
		
		String responseBodyAsString = method.getStatusLine().toString();
		method.releaseConnection();
		
		System.out.println("Got response: " + responseBodyAsString);
		
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
		
		LOGGER.debug("Retrieving attributes for path {}", path);
		
		try {
			String folderPath = toFolderPath(path);
			
			LOGGER.debug("Trying folderpath {}", folderPath);
				
			return getAttributes(path, getPathProperties(client, folderPath), true);
		} catch (XenonException e) {
			String filePath = toFilePath(path);
			
			LOGGER.debug("Trying filepath {}", filePath);
			
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
	public void rename(Path source, Path target) throws XenonException {

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
			sourcePath = toFolderPath(source);
			targetPath = toFolderPath(target);
		} else {
			sourcePath = toFilePath(source);
			targetPath = toFilePath(target);
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

		String folderPath = toFolderPath(dir);
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

	@Override
	public void createSymbolicLink(Path link, Path path) throws XenonException {
		throw new UnsupportedOperationException(ADAPTOR_NAME, "Operation not supported");
	}

	private void createFile(Path file, long size, InputStream data) throws XenonException {
		LOGGER.debug("createFile path = {}", file);
		assertPathNotExists(file);
		String filePath = toFilePath(file);
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
		executeDeleteMethod(toFilePath(path), client);
	}

	@Override
	protected void deleteDirectory(Path path) throws XenonException {
		executeDeleteMethod(toFolderPath(path), client);
	}

	@Override
	public boolean exists(Path path) throws XenonException {
		System.out.println("exists path =" +path);
		try {
			PathAttributes a = getAttributes(path);
			
			System.out.println("exists path =" + a);
			
			return true;
		} catch (XenonException e) {
			// getAttributes did not find evidence that the specified path exists
			
			System.out.println("exists path failed");
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public InputStream readFromFile(Path path) throws XenonException {
		String filePath = toFilePath(path);
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
}
