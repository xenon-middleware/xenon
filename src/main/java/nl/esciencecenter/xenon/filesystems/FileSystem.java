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
package nl.esciencecenter.xenon.filesystems;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import nl.esciencecenter.xenon.InvalidAdaptorException;
import nl.esciencecenter.xenon.UnsupportedOperationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.NotConnectedException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.FileAdaptor;
import nl.esciencecenter.xenon.adaptors.filesystems.ftp.FtpFileAdaptor;
import nl.esciencecenter.xenon.adaptors.filesystems.local.LocalFileAdaptor;
import nl.esciencecenter.xenon.adaptors.filesystems.sftp.SftpFileAdaptor;
import nl.esciencecenter.xenon.adaptors.filesystems.webdav.WebdavFileAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.InvalidCredentialException;
import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.InvalidPropertyException;
import nl.esciencecenter.xenon.UnknownPropertyException;

/**
 * FileSystem represent a (possibly remote) file system that can be used to access data.
 * 
 * @version 1.0
 * @since 1.0
 */
public abstract class FileSystem {

	/** The name of this component, for use in exceptions */
	private static final String COMPONENT_NAME = "FileSystem";

	/** The default buffer size */
	private static final int BUFFER_SIZE = 4 * 1024;

	private static final HashMap<String, FileAdaptor> adaptors = new LinkedHashMap<>();

	static { 
		/** Load all supported file adaptors */
		addAdaptor(new LocalFileAdaptor());
		addAdaptor(new FtpFileAdaptor());
		addAdaptor(new SftpFileAdaptor());
		addAdaptor(new WebdavFileAdaptor());
	}

	private static void addAdaptor(FileAdaptor adaptor) { 
		adaptors.put(adaptor.getName(), adaptor);
	}

	private static FileAdaptor getAdaptorByName(String adaptorName) throws XenonException {

		if (adaptorName == null || adaptorName.trim().isEmpty()) {
			throw new InvalidAdaptorException(COMPONENT_NAME, "Adaptor name may not be null or empty");
		}

		FileAdaptor adaptor = adaptors.get(adaptorName);

		if (adaptor == null) {
			throw new InvalidAdaptorException(COMPONENT_NAME, "File adaptor not found " + adaptor);
		}

		return adaptor;
	}

	public static String [] getAdaptorNames() {
		return adaptors.keySet().toArray(new String[0]);
	}

	public static FileSystemAdaptorDescription getAdaptorDescription(String adaptorName) throws XenonException {
		return getAdaptorByName(adaptorName).getAdaptorDescription();
	}

	public static FileSystemAdaptorDescription [] getAdaptorDescriptions() throws XenonException {

		// TODO: see getNames
		
		String [] names = getAdaptorNames();

		FileSystemAdaptorDescription[] result = new FileSystemAdaptorDescription[names.length];

		for (int i=0;i<names.length;i++) { 
			result[i] = getAdaptorDescription(names[i]);
		}

		return result;
	}

	/**
	 * Create a new FileSystem that represents a (possibly remote) data store 
	 * at the <code>location</code> using the <code>credentials</code> to get 
	 * access. Make sure to always close {@code FileSystem} instances by calling 
	 * {@code close(FileSystem)} when you no longer need them, otherwise their 
	 * associated resources remain allocated.
	 * 
	 * @param type
	 *            the type of file system to connect to (e.g. "sftp" or "webdav")
	 * @param location
	 *            the location of the FileSystem.
	 * @param credential
	 *            the Credentials to use to get access to the FileSystem.
	 * @param properties
	 *            optional properties to use when creating the FileSystem.
	 * 
	 * @return the new FileSystem.
	 * 
	 * @throws UnknownPropertyException
	 *             If a unknown property was provided.
	 * @throws InvalidPropertyException
	 *             If a known property was provided with an invalid value.
	 * @throws InvalidAdaptorException
	 *             If the adaptor was invalid.
	 * @throws InvalidLocationException
	 *             If the location was invalid.
	 * @throws InvalidCredentialException
	 *             If the credential is invalid to access the location.
	 * 
	 * @throws XenonException
	 *             If the creation of the FileSystem failed.
	 */
	public static FileSystem create(String type, String location, Credential credential, Map<String, String> properties) 
			throws XenonException {
		return getAdaptorByName(type).createFileSystem(location, credential, properties);
	}

	public static FileSystem create(String type, String location, Credential credential) throws XenonException {
		return create(type, location, credential, new HashMap<String, String>(0));
	}

	public static FileSystem create(String type, String location) throws XenonException {
		return create(type, location, new DefaultCredential());
	}

	public static FileSystem create(String type) throws XenonException {
		return create(type, null);
	}
	
    class CopyCallback {

		long bytesToCopy = 0;
		long bytesCopied = 0;
		
		boolean started = false;
		boolean cancel = false;
		
		synchronized void start(long bytesToCopy) {
			if (!started) { 
				started = true;
				this.bytesToCopy = bytesToCopy;
			}
		}
		
		synchronized boolean isStarted() { 
			return started;
		}
		
		synchronized void addBytesCopied(long bytes) { 
			this.bytesCopied += bytes;
		}
		
		synchronized void cancel() { 
			cancel = true;
		}
		
		synchronized boolean isCancelled() { 
			return cancel;
		}
	}
	
	private class PendingCopy { 
	
		Future<Void> future;
		CopyCallback callback;
		
		public PendingCopy(Future<Void> future, CopyCallback callback) {
			super();
			this.future = future;
			this.callback = callback;
		}
	}
	
	private final String uniqueID;
	private final String adaptor;
	private final String location;
	private final Path entryPath;
	private final XenonProperties properties;
	private final ExecutorService pool;
	
	private long nextCopyID = 0;
	
	private final HashMap<String, PendingCopy> pendingCopies = new HashMap<>();
	
	protected FileSystem(String uniqueID, String adaptor, String location, Path entryPath, XenonProperties properties) {

		if (uniqueID == null) {
			throw new IllegalArgumentException("Identifier may not be null!");
		}

		if (adaptor == null) {
			throw new IllegalArgumentException("Adaptor may not be null!");
		}

		if (location == null) {
			throw new IllegalArgumentException("Location may not be null!");
		}

		if (entryPath == null) {
			throw new IllegalArgumentException("EntryPath may not be null!");
		}

		this.uniqueID = uniqueID;
		this.adaptor = adaptor;
		this.location = location;
		this.entryPath = entryPath;
		this.properties = properties;		
		this.pool = Executors.newFixedThreadPool(1);
	}

	private synchronized String getNextCopyID() {
		return "COPY-" + getAdaptorName() + "-" + nextCopyID++;
	}	
	
	/**
	 * Get the name of the adaptor that created this FileSystem.
	 * 
	 * @return the name of the adaptor.
	 */
	public String getAdaptorName() { 
		return adaptor;
	}

	/**
	 * Get the location of the FileSystem.
	 * 
	 * @return the location of the FileSystem.
	 */
	public String getLocation() { 
		return location;
	}

	/**
	 * Get the properties used to create this FileSystem.
	 * 
	 * @return the properties used to create this FileSystem.
	 */
	public Map<String, String> getProperties() { 
		return properties.toMap();
	}

	/**
	 * Get the entry path of this file system.
	 * 
	 * The entry path is the initial path when the FileSystem is first accessed, for example <code>"/home/username"</code>.
	 * 
	 * @return the entry path of this file system.
	 */
	public Path getEntryPath() { 
		return entryPath;
	}

	/**
	 * Close this FileSystem.
	 * 
	 * @throws XenonException
	 *             If the FileSystem failed to close or if an I/O error occurred.
	 */
	public abstract void close() throws XenonException;

	/**
	 * Return if the connection to the FileSystem is open.
	 * 
	 * @throws XenonException
	 *          if the test failed or an I/O error occurred.
	 * @return
	 *          if the connection to the FileSystem is open.                     
	 */
	public abstract boolean isOpen() throws XenonException;

	/**
	 * Rename an existing source path to a non-existing target path (optional operation).
	 * <p>
	 * The parent of the target path (e.g. <code>target.getParent</code>) must exist.
	 * 
	 * If the target is equal to the source this method has no effect.
	 * 
	 * If the source is a link, the link itself will be moved, not the path to which it refers.
	 * 
	 * If the source is a directory, it will be renamed to the target. This implies that a moving a directory between physical
	 * locations may fail.
	 * </p>
	 * @param source
	 *            the existing source path.
	 * @param target
	 *            the non existing target path.
	 * 
	 * @throws NoSuchPathException
	 *             If the source file does not exist or the target parent directory does not exist.
	 * @throws PathAlreadyExistsException
	 *             If the target file already exists.
	 * @throws NotConnectedException
	 *             If file system is closed.
	 * @throws XenonException
	 *             If the move failed.
	 */
	public abstract void rename(Path source, Path target) throws XenonException;

	/**
	 * Creates a new directory, failing if the directory already exists. All nonexistent parent directories are also created.
	 * 
	 * @param dir
	 *            the directory to create.
	 * 
	 * @throws PathAlreadyExistsException
	 *             If the directory already exists or if a parent directory could not be created because a file with the same name
	 *             already exists.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public void createDirectories(Path dir) throws XenonException { 

		Path parent = dir.getParent();

		if (parent != null) {

			if (!exists(parent)) {
				// Recursive call
				createDirectories(parent);
			}
		}
		createDirectory(dir);
	}

	/**
	 * Creates a new directory, failing if the directory already exists.
	 * 
	 * The parent directory of the file must already exists.
	 * 
	 * @param dir
	 *            the directory to create.
	 *
	 * @throws PathAlreadyExistsException
	 *             If the directory already exists.
	 * @throws NoSuchPathException
	 *             If the parent directory does not exist.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public abstract void createDirectory(Path dir) throws XenonException;

	/**
	 * Creates a new empty file, failing if the file already exists.
	 * 
	 * The parent directory of the file must already exists.
	 * 
	 * @param file
	 *            a path referring to the file to create.
	 * 
	 * @throws PathAlreadyExistsException
	 *             If the file already exists.
	 * @throws NoSuchPathException
	 *             If the parent directory does not exist.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public abstract void createFile(Path file) throws XenonException;

	/**
	 * Creates a new symbolic link, failing if the link already exists (optional operation).
	 * 
	 * @param link
	 *            the symbolic link to create.
	 * @param target
	 *            the target the symbolic link should refer to.
	 * 
	 * @throws PathAlreadyExistsException
	 *             If the link already exists.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public abstract void createSymbolicLink(Path link, Path target) throws XenonException;


	/**
	 * Deletes an existing path.
	 * 
	 * If path is a symbolic link the symbolic link is removed and the symbolic link's target is not deleted.
	 * 
	 * If the path is a directory and <code>recursive</code> is set to true, the contents of the directory will 
	 * also be deleted. If <code>recursive</code> is set to <code>false</code>, a directory will only be removed 
	 * if it is empty. 
	 * 
	 * @param path
	 *          the path to delete.
	 * @param recursive
	 * 			if the delete must be done recursively
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public void delete(Path path, boolean recursive) throws XenonException {

		if (isDotDot(path)) { 
			return;
		}

		if (getAttributes(path).isDirectory()) {

			for (PathAttributes p : list(path, false)) { 
			if (recursive) { 
					delete(p.getPath(), true);
				}
			}

			deleteDirectory(path);
		} else {
			deleteFile(path);
		}
	}   

	/**
	 * Tests if a path exists.
	 * 
	 * @param path
	 *            the path to test.
	 * 
	 * @return If the path exists.
	 * 
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public abstract boolean exists(Path path) throws XenonException;

	/**
	 * List all entries in the directory <code>dir</code>. 
	 * 
	 * All entries in the directory are returned, but subdirectories will not be traversed by default. 
	 * Set <code>recursive</code> to <code>true</code>, include the listing of all subdirectories.  
	 * 
	 * @param dir
	 *            the target directory.
	 * @param recursive
	 *            should the list recursively traverse the subdirectories ?
	 * 
	 * @return a {@link List} of {@link PathAttributes} that iterates over all entries in the directory <code>dir</code>.
	 * 
	 * @throws NoSuchPathException
	 *             If a directory does not exists.
	 * @throws InvalidPathException
	 *             If <code>dir</code> is not a directory.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public Iterable<PathAttributes> list(Path dir, boolean recursive) throws XenonException { 
		ArrayList<PathAttributes> result = new ArrayList<>();
		list(dir, result, recursive);
		return result;
	}

	/**
	 * Open an existing file and return an {@link InputStream} to read from this file.
	 * 
	 * @param file
	 *            the to read.
	 * 
	 * @return the {@link InputStream} to read from the file.
	 * 
	 * @throws NoSuchPathException
	 *             If the file does not exists.
	 * @throws InvalidPathException
	 *             If the file is not regular file.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public abstract InputStream readFromFile(Path file) throws XenonException;

	/**
	 * Open a file and return an {@link OutputStream} to write to this file.
	 * <p>
	 * If the file already exists it will be replaced and its data will be lost. 
	 * 
	 * The size of the file (once all data has been written) must be specified using 
	 * the <code>size</code> parameter. This is required by some implementations 
	 * (typically blob-stores).
	 * 
	 * </p>
	 * @param path
	 *            the target file for the OutputStream.
	 * @param size
	 *            the size of the file once fully written.
	 * 
	 * @return the {@link OutputStream} to write to the file.
	 * 
	 * @throws InvalidPathException
	 *             If the file could not be created.
	 *             
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public abstract OutputStream writeToFile(Path path, long size) throws XenonException;

	/**
	 * Open a file and return an {@link OutputStream} to write to this file.
	 * <p>
	 * If the file already exists it will be replaced and its data will be lost.
	 * 
	 * The amount of data that will be written to the file is not specified in advance. 
	 * This operation may not be supported by all implementations. 
	 *  
	 * </p>
	 * @param file
	 *            the target file for the OutputStream.
	 * 
	 * @return the {@link OutputStream} to write to the file.
	 * 
	 * @throws InvalidPathException
	 *             If the file could not be created.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public abstract OutputStream writeToFile(Path file) throws XenonException;

	/**
	 * Open an existing file and return an {@link OutputStream} to append data to this file.
	 * <p>
	 * If the file does not exist, an exception will be thrown.
	 * 
	 * This operation may not be supported by all implementations. 
	 *  
	 * </p>
	 * @param file
	 *            the target file for the OutputStream.
	 * 
	 * @return the {@link OutputStream} to write to the file.
	 * 
	 * @throws InvalidPathException
	 *             If the file is not a regula file.
	 * @throws NoSuchPathException
	 *             If the file does not exist.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public abstract OutputStream appendToFile(Path file) throws XenonException;


	/**
	 * Get the {@link PathAttributes} of an existing path.
	 * 
	 * @param path
	 *            the existing path.
	 * 
	 * @return the FileAttributes of the path.
	 * 
	 * @throws NoSuchPathException
	 *             If a file does not exists.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public abstract PathAttributes getAttributes(Path path) throws XenonException;

	/**
	 * Reads the target of a symbolic link (optional operation).
	 * 
	 * @param link
	 *            the link to read.
	 * 
	 * @return a Path representing the target of the link.
	 * 
	 * @throws NoSuchPathException
	 *             If the link does not exists.
	 * @throws InvalidPathException
	 *             If the source is not a link.
	 * @throws UnsupportedOperationException
	 * 		       If this FileSystem does not support symbolic links.            
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public abstract Path readSymbolicLink(Path link) throws XenonException;

	/**
	 * Sets the POSIX permissions of a path (optional operation).
	 * 
	 * @param path
	 *            the target path.
	 * @param permissions
	 *            the permissions to set.
	 * 
	 * @throws NoSuchPathException
	 *             If the target path does not exists.
	 * @throws UnsupportedOperationException
	 * 		       If this FileSystem does not support symbolic links.            
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public abstract void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException;

	

	protected void streamCopy(InputStream in, OutputStream out, int buffersize, CopyCallback callback) throws IOException, XenonException {

		byte[] buffer = new byte[buffersize];

		int size = in.read(buffer);

		while (size > 0) {
			out.write(buffer, 0, size);

			callback.addBytesCopied(size);
			
			if (callback.isCancelled()) { 
				throw new XenonException(getAdaptorName(), "Copy cancelled by user");
			}

			size = in.read(buffer);
		}
	}

	/**
	 * Copy a symbolic link to another file system (optional operation).
	 * 
	 * This is a blocking copy operation. It only returns once the link has been copied 
	 * or the copy has failed. 
	 * 
	 * This operation may be re-implemented by the various implementations of FileSystem. 
	 * 
	 * This default implementation is based on a creating a new link on the destination filesystem.
	 * Note that the file the link is referring to is not copied. Only the link itself is copied. 
	 * 
	 * @param source
	 * 		the link to copy.
	 * @param destinationFS
	 * 		the destination {@link FileSystem} to copy to.
	 * @param destination
	 * 		the destination link on the destination file system.
	 * @param mode
	 * 		selects what should happen if the destination link already exists
	 * @param callback
	 * 		a {@link CopyCallback} used to update the status of the copy, or cancel it while in progress.
	 * 
	 * @throws InvalidPathException 
	 * 		if the provide source is not a link.
	 * @throws NoSuchPathException
	 * 		if the source link does not exist or the destination parent directory does not exist.
	 * @throws PathAlreadyExistsException
	 * 		if the destination link already exists.
	 * @throws UnsupportedOperationException
	 * 		if the destination FileSystem does not support symbolic links.            
	 * @throws XenonException
	 *      if the link could not be copied.
	 */    
	protected void copySymbolicLink(Path source, FileSystem destinationFS, Path destination, CopyMode mode, CopyCallback callback) throws XenonException {
		
		PathAttributes attributes = getAttributes(source);

		if (!attributes.isSymbolicLink()) { 
			throw new InvalidPathException(getAdaptorName(), "Source is not a regular file: " + source);
		} 

		destinationFS.assertParentDirectoryExists(destination);

		if (destinationFS.exists(destination)) { 
			switch (mode) { 
			case CREATE:
				throw new PathAlreadyExistsException(getAdaptorName(), "Destination path already exists: " + destination);
			case IGNORE:
				return;
			case REPLACE:
				// continue 
				break;
			}
		} 

		Path target = readSymbolicLink(source);
		destinationFS.createSymbolicLink(destination, target);
	}
			
	/**
	 * Copy a single file to another file system. 
	 * 
	 * This is a blocking copy operation. It only returns once the file has been copied 
	 * or the copy has failed. 
	 * 
	 * This operation may be re-implemented by the various implementations of FileSystem. 
	 * This default implementation is based on a simple stream based copy.
	 * 
	 * @param source
	 * 		the file to copy.
	 * @param destinationFS
	 * 		the destination {@link FileSystem} to copy to.
	 * @param destination
	 * 		the destination file on the destination file system.
	 * @param mode
	 * 		selects what should happen if the destination file already exists
	 * @param callback
	 * 		a {@link CopyCallback} used to update the status of the copy, or cancel it while in progress.
	 * 
	 * @throws InvalidPathException 
	 * 		if the provide source is not a regular file.
	 * @throws NoSuchPathException
	 * 		if the source file does not exist or the destination parent directory does not exist.
	 * @throws PathAlreadyExistsException
	 * 		if the destination file already exists.
	 * @throws XenonException
	 *      If the file could not be copied.
	 */    
	protected void copyFile(Path source, FileSystem destinationFS, Path destination, CopyMode mode, CopyCallback callback) throws XenonException {

		PathAttributes attributes = getAttributes(source);

		if (!attributes.isRegular()) { 
			throw new InvalidPathException(getAdaptorName(), "Source is not a regular file: " + source);
		} 

		destinationFS.assertParentDirectoryExists(destination);

		if (destinationFS.exists(destination)) { 
			switch (mode) { 
			case CREATE:
				throw new PathAlreadyExistsException(getAdaptorName(), "Destination path already exists: " + destination);
			case IGNORE:
				return;
			case REPLACE:
				// continue 
				break;
			}
		} 
		
		if (callback.isCancelled()) { 
			throw new XenonException(getAdaptorName(), "Copy cancelled by user");
		}

		try (InputStream in = readFromFile(source); 
				OutputStream out = destinationFS.writeToFile(destination, attributes.getSize())) { 
			streamCopy(in, out, BUFFER_SIZE, callback);
		} catch (Exception e) {
			throw new XenonException(getAdaptorName(), "Stream copy failed", e);	
		}
	}

	/**
	 * Perform a (possibly) recursive copy from a path on this filesystem to a path on <code>destinationFS</code>.
	 *  
	 * @param source
	 * 		the source path on this FileSystem.
	 * @param destinationFS
	 * 		the destination FileSystem.
	 * @param destination
	 * 		the destination path.
	 * @param mode
	 * 		the copy mode that determines how to react if the destination already exists.
	 * @param recursive
	 * 		should the copy be performed recursively ?
	 * @param callback
	 * 		a {@link CopyCallback} used to return status information on the copy. 
	 * @throws XenonException
	 * 		if an error occurred.
	 */
	protected void performCopy(Path source, FileSystem destinationFS, Path destination, CopyMode mode, boolean recursive, CopyCallback callback) throws XenonException {
		
		long bytesToCopy = 0;
		
		PathAttributes attributes = getAttributes(source);

		if (attributes.isRegular() || attributes.isSymbolicLink()) {
			copyFile(source, destinationFS, destination, mode, callback);
			return;
		}

		if (attributes.isSymbolicLink()) {
			copySymbolicLink(source, destinationFS, destination, mode, callback);
			return;
		}

		if (!attributes.isDirectory()) { 
			throw new InvalidPathException(getAdaptorName(), "Source path is not a file, link or directory: " + source);
		}

		if (!recursive) { 
			throw new InvalidPathException(getAdaptorName(), "Source path is a directory: " + source);
		}

		if (!destinationFS.exists(destination)) {
			destinationFS.createDirectory(destination);
		}

		Iterable<PathAttributes> listing = list(source, true);

		for (PathAttributes p : listing) { 

			if (callback.isCancelled()) { 
				throw new XenonException(getAdaptorName(), "Copy cancelled by user");
			}

			if (p.isDirectory() && !isDotDot(p.getPath())) {

				Path rel = source.relativize(p.getPath());
				Path dst = destination.resolve(rel);

				destinationFS.createDirectories(dst);
			} else if (p.isRegular()) { 
				bytesToCopy += p.getSize();
			}
		}

		callback.start(bytesToCopy);

		for (PathAttributes p : listing) { 

			if (callback.isCancelled()) { 
				throw new XenonException(getAdaptorName(), "Copy cancelled by user");
			}

			if (p.isRegular()) { 

				Path rel = source.relativize(p.getPath());
				Path dst = destination.resolve(rel);

				copyFile(p.getPath(), destinationFS, dst, mode, callback);;
				//bytesCopied += p.getSize();				
				//callback.setBytesCopied(bytesCopied);
			}
		}
	}

	/**
	 * Delete a file. If the file does not exist, an exception will be thrown.
	 * 
	 * This operation must be implemented by the various implementations of FileSystem.
	 * 
	 * @param file
	 * 		the file to remove
	 * @throws InvalidPathException 
	 * 		if the provide path is not a file.
	 * @throws NoSuchPathException
	 * 		if the provides file does not exist.
	 * @throws XenonException
	 *      If the file could not be removed.
	 */    
	protected abstract void deleteFile(Path file) throws XenonException;

	/**
	 * Delete an empty directory. If the directory is not empty, an exception will be thrown.
	 * 
	 * This operation can only delete empty directories (analogous to <code>rmdir</code> in Linux). 
	 * 
	 * This operation must be implemented by the various implementations of FileSystem.
	 * 
	 * @param path
	 * 		the directory to remove
	 * @throws DirectoryNotEmptyException
	 * 		if the directory was not empty.
	 * @throws InvalidPathException 
	 * 		if the provide path is not a directory.
	 * @throws NoSuchPathException
	 * 		if the provides path does not exist.
	 * @throws XenonException
	 *      If the directory could not be removed.
	 */    
	protected abstract void deleteDirectory(Path path) throws XenonException;

	/**
	 * Return the list of entries in a directory. 
	 * 
	 * This operation is non-recursive; any subdirectories in <code>dir</code> will be returned as
	 * part of the list, but they will not be listed themselves.   
	 * 
	 * This operation must be implemented by the various implementations of FileSystem.
	 * 
	 * @param dir
	 * 		the directory to list
	 * @return
	 * 		a {@link Iterable} that iterates over all entries in <code>dir</code>
	 * @throws XenonException
	 *      If the list could not be retrieved.
	 */    
	protected abstract Iterable<PathAttributes> listDirectory(Path dir) throws XenonException;

	/**
	 * Returns an (optionally recursive) listing of the entries in a directory <code>dir</code>. 
	 * 
	 * This is a generic implementation which relies on <code>listDirectory</code> to provide 
	 * listings of individual directories.
	 * 
	 * @param dir
	 * 		the directory to list.
	 * @param list
	 * 		the list to which the directory entries will be added.
	 * @param recursive
	 * 		if the listing should be done recursively.
	 * @throws XenonException
	 *      If the list could not be retrieved.
	 */
	protected void list(Path dir, ArrayList<PathAttributes> list, boolean recursive) throws XenonException {

		Iterable<PathAttributes> tmp = listDirectory(dir);

		for (PathAttributes p : tmp) { 
			list.add(p);
		}

		if (recursive) { 
			for (PathAttributes current : tmp) {
				// traverse subdirs provided they are not "." or "..". 
				if (current.isDirectory() && !isDotDot(current.getPath())) { 
					list(dir.resolve(current.getPath().getFileNameAsString()), list, recursive);
				}
			}
		}
	}

	
	/**
	 * Copy an existing source path to a target path on a different file system.
	 * 
	 * If the source path is a file, it will be copied to the destination file on the target file system.
	 * 
	 * If the source path is a directory, it will only be copied if <code>recursive</code> is set to <code>true</code>. 
	 * Otherwise, an exception will be thrown. When copying recursively, the directory and its content (both files 
	 * and subdirectories with content), will be copied to <code>destination</code>.
	 * 
	 * @param source
	 *            the source path (on this filesystem) to copy from.
	 * @param destinationFS
	 *            the destination filesystem to copy to.
	 * @param destination
	 *            the destination path (on the destination filesystem) to copy to.
	 * @param mode
	 *            how to react if the destination already exists.
	 * @param recursive
	 *            if the copy should be recursive.
	 *
	 * @return a {@link String} that identifies this copy and be used to inspect its progress.
	 * 
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public synchronized String copy(Path source, FileSystem destinationFS, Path destination, CopyMode mode, boolean recursive) throws XenonException { 
	
		if (source == null) { 
			throw new IllegalArgumentException("Source path is null");
		}

		if (destinationFS == null) { 
			throw new IllegalArgumentException("Destination filesystem is null");
		}

		if (destination == null) { 
			throw new IllegalArgumentException("Destination path is null");
		}
		
		String ID = getNextCopyID();
		
		CopyCallback callback = new CopyCallback();
		
		Future<Void> future = pool.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				
				if (Thread.currentThread().isInterrupted()) { 
					throw new XenonException(getAdaptorName(), "Copy cancelled by user");
				}
					
				performCopy(source, destinationFS, destination, mode, recursive, callback);
				return null;
			}
		}) ;
		
		pendingCopies.put(ID, new PendingCopy(future, callback));
		return ID;
	}
	
	/**
	 * Cancel a copy operation.
	 * 
	 * @param copyIdentifier
	 *            the identifier of the copy operation which to cancel.
	 *            
	 * @return a {@link CopyStatus} containing the status of the copy.
	 * 
	 * @throws NoSuchCopyException
	 *             If the copy is not known.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public synchronized CopyStatus cancel(String copyIdentifier) throws XenonException { 
	
		if (copyIdentifier == null) { 
			throw new IllegalArgumentException("Copy identifier may not be null");
		}
		
		PendingCopy copy = pendingCopies.remove(copyIdentifier);
		
		if (copy == null) { 
			throw new NoSuchCopyException(getAdaptorName(), "Copy not found: " + copyIdentifier);
		}
		
		copy.callback.cancel();
		copy.future.cancel(true);
		
		Throwable ex = null;
		String state = "DONE";
		
		try { 
			copy.future.get();
		} catch (ExecutionException ee) {
			ex = ee.getCause();
			state = "FAILED";
		} catch (CancellationException | InterruptedException ec) {
			ex = new XenonException(getAdaptorName(), "Copy cancelled by user");			
			state = "FAILED";
		}
		
		return new CopyStatus(copyIdentifier, state, copy.callback.bytesToCopy, copy.callback.bytesCopied, ex);
	}
	
	/**
	 * Wait until a copy operation is done or until a timeout expires.
	 * <p>
	 * This method will wait until a copy operation is done (either gracefully or by producing an error), or until
	 * the timeout expires, whichever comes first. If the timeout expires, the copy operation will continue to run.
	 * </p>
	 * <p>
	 * The timeout is in milliseconds and must be &gt;= 0. When timeout is 0, it will be ignored and this method will wait until
	 * the copy operation is done.  
	 * </p>
	 * <p>
	 * A {@link CopyStatus} is returned that can be used to determine why the call returned.
	 * </p>
	 * @param copyIdentifier
	 *            the identifier of the copy operation to wait for. 
	 * @param timeout
	 *            the maximum time to wait for the copy operation in milliseconds.     
	 *            
	 * @return a {@link CopyStatus} containing the status of the copy.
	 * 
	 * @throws IllegalArgumentException 
	 *             If the value of timeout is negative
	 * @throws NoSuchCopyException
	 *             If the copy handle is not known.
	 * @throws XenonException
	 *             If the status of the copy operation could not be retrieved.
	 */    
	public CopyStatus waitUntilDone(String copyIdentifier, long timeout) throws XenonException { 

		if (copyIdentifier == null) { 
			throw new IllegalArgumentException("Copy identifier may not be null");
		}
		
		PendingCopy copy = pendingCopies.get(copyIdentifier);
		
		if (copy == null) { 
			throw new NoSuchCopyException(getAdaptorName(), "Copy not found: " + copyIdentifier);
		}
		
		Throwable ex = null;
		String state = "DONE";
		
		try { 
			copy.future.get(timeout, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			state = "RUNNING";
		} catch (ExecutionException ee) {
			ex = ee.getCause();
			state = "FAILED";
		} catch (CancellationException | InterruptedException ec) {
			ex = new XenonException(getAdaptorName(), "Copy cancelled by user");			
			state = "FAILED";
		}
		
		if (copy.future.isDone()) { 
			pendingCopies.remove(copyIdentifier);
		}
		
		return new CopyStatus(copyIdentifier, state, copy.callback.bytesToCopy, copy.callback.bytesCopied, ex);
	}
	
	/**
	 * Retrieve the status of an copy.
	 * 
	 * @param copyIdentifier
	 *            the identifier of the copy for which to retrieve the status.
	 * 
	 * @return a {@link CopyStatus} containing the status of the asynchronous copy.
	 * 
	 * @throws NoSuchCopyException
	 *             If the copy is not known.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public CopyStatus getStatus(String copyIdentifier) throws XenonException { 

		if (copyIdentifier == null) { 
			throw new IllegalArgumentException("Copy identifier may not be null");
		}
	
		PendingCopy copy = pendingCopies.get(copyIdentifier);
		
		if (copy == null) { 
			throw new NoSuchCopyException(getAdaptorName(), "Copy not found: " + copyIdentifier);
		}
		
		Throwable ex = null;
		String state = "PENDING";
		
		if (copy.future.isDone()) { 
			pendingCopies.remove(copyIdentifier);
			
			// We have either finished, crashed, or cancelled
			try { 
				copy.future.get();
				state = "DONE";
			} catch (ExecutionException ee) {
				ex = ee.getCause();
				state = "FAILED";
			} catch (CancellationException | InterruptedException ec) {
				ex = new XenonException(getAdaptorName(), "Copy cancelled by user");			
				state = "FAILED";
			}
		} else if (copy.callback.isStarted()) { 
			state = "RUNNING";
		}		
			
		return new CopyStatus(copyIdentifier, state, copy.callback.bytesToCopy, copy.callback.bytesCopied, ex);
	}
	
	protected void assertPathExists(Path path) throws XenonException {

		if (path == null) { 
			throw new IllegalArgumentException("Path is null");
		}

		if (!exists(path)) { 
			throw new NoSuchPathException(getAdaptorName(), "Path does not exist: " + path);
		}   	
	}

	protected void assertPathNotExists(Path path) throws XenonException {

		if (path == null) { 
			throw new IllegalArgumentException("Path is null");
		}

		if (exists(path)) {
			throw new PathAlreadyExistsException(getAdaptorName(), "File already exists: " + path);
		}
	}

	protected void assertPathIsFile(Path path) throws XenonException {

		if (path == null) { 
			throw new IllegalArgumentException("Path is null");
		}

		if (!getAttributes(path).isRegular()) { 
			throw new InvalidPathException(getAdaptorName(), "Path is not a file: " + path);
		}
	}

	protected void assertPathIsDirectory(Path path) throws XenonException {

		if (path == null) { 
			throw new IllegalArgumentException("Path is null");
		}

		if (!getAttributes(path).isDirectory()) { 
			throw new InvalidPathException(getAdaptorName(), "Path is not a directory: " + path);
		}
	}

	protected void assertFileExists(Path file) throws XenonException { 
		assertPathExists(file);
		assertPathIsFile(file);
	}

	protected void assertDirectoryExists(Path dir) throws XenonException { 
		assertPathExists(dir);
		assertPathIsDirectory(dir);
	}

	protected void assertParentDirectoryExists(Path path) throws XenonException {

		if (path == null) { 
			throw new IllegalArgumentException("Path is null");
		}

		Path parent = path.getParent();

		if (parent == null) { 
			throw new InvalidPathException(getAdaptorName(), "Parent directory does not exist: " + path);
		}

		assertDirectoryExists(parent);
	}

	protected boolean areSamePaths(Path source, Path target) {

		if (source == null) { 
			throw new IllegalArgumentException("Source is null");
		}

		if (target == null) { 
			throw new IllegalArgumentException("Target is null");
		}

		Path sourceName = source.normalize();
		Path targetName = target.normalize();
		return sourceName.equals(targetName);
	}

	protected boolean isDotDot(Path path) {

		if (path == null) { 
			throw new IllegalArgumentException("Path is null");
		}

		String filename = path.getFileNameAsString();
		return ".".equals(filename) || "..".equals(filename);
	}

	@Override
	public int hashCode() {
		return uniqueID.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		return uniqueID.equals(((FileSystem)obj).uniqueID);
	}    
}
