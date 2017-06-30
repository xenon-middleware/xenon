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
package nl.esciencecenter.xenon.files;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.file.CopyEngine;
import nl.esciencecenter.xenon.adaptors.file.FileAdaptor;
import nl.esciencecenter.xenon.adaptors.file.file.LocalFileAdaptor;
import nl.esciencecenter.xenon.adaptors.file.ftp.FtpFileAdaptor;
import nl.esciencecenter.xenon.adaptors.file.sftp.SftpFileAdaptor;
import nl.esciencecenter.xenon.adaptors.file.webdav.WebdavFileAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.jobs.InvalidAdaptorException;
import nl.esciencecenter.xenon.jobs.InvalidCredentialException;
import nl.esciencecenter.xenon.jobs.InvalidLocationException;
import nl.esciencecenter.xenon.jobs.InvalidPropertyException;
import nl.esciencecenter.xenon.jobs.UnknownPropertyException;

/**
 * FileSystem represent a (possibly remote) file system that can be used to access data.
 * 
 * @version 1.0
 * @since 1.0
 */
public abstract class FileSystem {

	/** The name of this component, for use in exceptions */
	private static final String COMPONENT_NAME = "FileSystem";

	public static final DirectoryStream.Filter ACCEPT_ALL_FILTER = new DirectoryStream.Filter() {
		public boolean accept(Path file) {
			return true;
		}
	};

	private static final HashMap<String, FileAdaptor> adaptors = new HashMap<>();

	private static final CopyEngine copyEngine = new CopyEngine();

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
		return null;
	}

	public static FileSystemAdaptorDescription getAdaptorDescription(String adaptorName) {
		return null;
	}

	public static FileSystemAdaptorDescription [] getAdaptorDescriptions() {
		return null;
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
	
	private final String uniqueID;
	private final String adaptor;
	private final String location;
	private final Path entryPath;
	private final XenonProperties properties;

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
	 * Move or rename an existing source path to a non-existing target path.
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
	public abstract void move(Path source, Path target) throws XenonException;

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
	 * @param dir
	 *            the directory to create.
	 * 
	 * @throws PathAlreadyExistsException
	 *             If the directory already exists.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public abstract void createDirectory(Path dir) throws XenonException;

	/**
	 * Creates a new empty file, failing if the file already exists.
	 * 
	 * @param file
	 *            a path referring to the file to create.
	 * 
	 * @throws PathAlreadyExistsException
	 *             If the directory already exists.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public abstract void createFile(Path file) throws XenonException;

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
			
			if (recursive) { 
				for (PathAttributesPair p : list(path, false)) { 
					delete(p.path(), true);
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
	 * 
	 * @return a {@link Iterable} of {@PathAttributePair}s that iterates over all entries in the directory <code>dir</code>.
	 * 
	 * @throws NoSuchPathException
	 *             If a directory does not exists.
	 * @throws InvalidPathException
	 *             If <code>dir</code> is not a directory.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public Iterable<PathAttributesPair> list(Path dir, boolean recursive) throws XenonException { 
		ArrayList<PathAttributesPair> result = new ArrayList<>();
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
	 * Get the {@link FileAttributes} of an existing path.
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
	public abstract FileAttributes getAttributes(Path path) throws XenonException;

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
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public abstract Path readSymbolicLink(Path link) throws XenonException;

	/**
	 * Sets the POSIX permissions of a path.
	 * 
	 * @param path
	 *            the target path.
	 * @param permissions
	 *            the permissions to set.
	 * 
	 * @throws NoSuchPathException
	 *             If the target path does not exists.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public abstract void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException;

	/**
	 * Copy an existing source file or symbolic link to a target file.
	 * 
	 * @param description
	 *            a description of the copy to perform.

	 * @return a {@link CopyHandle} that can be used to inspect the status of the copy.
	 * 
	 * @throws NoSuchPathException
	 *             If the source file does not exist, the target parent directory does not exist, or the target file does not
	 *             exist and the <code>APPEND</code> or <code>RESUME</code> option is provided.
	 * @throws PathAlreadyExistsException
	 *             If the target file already exists.
	 * @throws InvalidPathException
	 *             If the source or target path is not a file.
	 * @throws InvalidOptionsException
	 *             If a conflicting set of copy options is provided.
	 * @throws InvalidResumeTargetException
	 *             If the data in the target of a resume does not match the data in the source. 
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public CopyHandle copy(CopyDescription description) throws XenonException { 
		// TODO: implement!
		return null;
	}
	
//	public abstract CopyHandle copy(Path sourcePath, Path destinationPath, CopyOption option, boolean recursive) throws XenonException;
	
//	public abstract CopyHandle copy(FileSystem sourceFS, Path sourcePath, Path destinationPath, CopyOption option, boolean recursive) throws XenonException;
	
//	public abstract CopyHandle copyRecursive(Path sourcePath, Path destinationPath, CopyOption option) throws XenonException;
//	
//	public abstract CopyHandle copyRecursive(FileSystem sourceFS, Path sourcePath, Path destinationPath, CopyOption option) throws XenonException;
//	
	
	/**
     * Retrieve the status of an copy.
     * 
     * @param copy
     *            the copy for which to retrieve the status.
     * 
     * @return a {@link CopyStatus} containing the status of the asynchronous copy.
     * 
     * @throws NoSuchCopyException
     *             If the copy is not known.
     * @throws XenonException
     *             If an I/O error occurred.
     */
    public CopyStatus getStatus(CopyHandle copy) throws XenonException { 
    	// TODO: implement!
		return null;
    }

    /**
     * Cancel a copy operation.
     * 
     * @param copy
     *            the copy operation which to cancel.
     *            
     * @return a {@link CopyStatus} containing the status of the copy.
     * 
     * @throws NoSuchCopyException
     *             If the copy is not known.
     * @throws XenonException
     *             If an I/O error occurred.
     */
    public CopyStatus cancel(CopyHandle copy) throws XenonException { 
    	// TODO: implement!
		return null;
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
     * A {@link Copystatus} is returned that can be used to determine why the call returned.
     * </p>
     * @param copy
     *            a handle for the copy operation 
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
    public CopyStatus waitUntilDone(CopyHandle copy, long timeout) throws XenonException { 
    	// TODO: implement!
		return null;
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
     * @throws XenonException.
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
     * @param dir
     * 		the directory to remove
     * @throws DirectoryNotEmptyException
     * 		if the directory was not empty.
     * @throws InvalidPathException 
     * 		if the provide path is not a directory.
     * @throws NoSuchPathException
     * 		if the provides path does not exist.
     * @throws XenonException.
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
     * 		a list of all entries in <code>dir</code>
     * @throws XenonException
     *      If the list could not be retrieved.
     */    
    protected abstract List<PathAttributesPair> listDirectory(Path dir) throws XenonException;
    
    /**
     * Returns an (optionally recursive) listing of the entries in a directory <code>dir</code>. 
     * 
     * This is a generic implementation which relies on <code>listDirectory</code> to provide 
     * listings of individal directories.
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
    protected void list(Path dir, ArrayList<PathAttributesPair> list, boolean recursive) throws XenonException {
		
		List<PathAttributesPair> tmp = listDirectory(dir);
		list.addAll(tmp);
		
		if (recursive) { 
			for (PathAttributesPair current : tmp) { 
				// traverse subdirs provided they are not "." or "..". 
				if (current.attributes().isDirectory() && !isDotDot(dir)) { 
					list(dir.resolve(current.path().getFileNameAsString()), list, recursive);
				}
			}
		}
	}
	
    protected void assertPathExists(Path path) throws XenonException {
   		if (!exists(path)) { 
   			throw new NoSuchPathException(getAdaptorName(), "Path does not exist: " + path);
    	}   	
	}

    protected void assertPathNotExists(Path path) throws XenonException {
    	if (exists(path)) {
    		throw new PathAlreadyExistsException(getAdaptorName(), "File already exists: " + path);
    	}
    }

   	protected void assertPathIsFile(Path path) throws XenonException {
    	if (!getAttributes(path).isRegular()) { 
   			throw new InvalidPathException(getAdaptorName(), "Path is not a file: " + path);
    	}
   	}
   	
   	protected void assertPathIsDirectory(Path path) throws XenonException {
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
		Path parent = path.getParent();
        
        if (parent == null) { 
            throw new InvalidPathException(getAdaptorName(), "Parent directory does not exist: " + path);
        }
            
        assertDirectoryExists(parent);
	}

	protected boolean areSamePaths(Path source, Path target) {
		Path sourceName = source.normalize();
		Path targetName = target.normalize();
		return sourceName.equals(targetName);
	}
	
	protected boolean isDotDot(Path path) {
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
