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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.InvalidAdaptorException;
import nl.esciencecenter.xenon.adaptors.InvalidCredentialException;
import nl.esciencecenter.xenon.adaptors.InvalidLocationException;
import nl.esciencecenter.xenon.adaptors.InvalidPropertyException;
import nl.esciencecenter.xenon.adaptors.UnknownPropertyException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.file.CopyEngine;
import nl.esciencecenter.xenon.adaptors.file.FileAdaptor;
import nl.esciencecenter.xenon.adaptors.file.file.LocalFileAdaptor;
import nl.esciencecenter.xenon.adaptors.file.ftp.FtpFileAdaptor;
import nl.esciencecenter.xenon.adaptors.file.sftp.SftpFileAdaptor;
import nl.esciencecenter.xenon.adaptors.file.webdav.WebdavFileAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;

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
	 * @param path
	 *            the path to delete.
	 * 
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public abstract void delete(Path path) throws XenonException;

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
	 * Create a DirectoryStream that iterates over all entries in the directory <code>dir</code>.
	 * 
	 * @param dir
	 *            the target directory.
	 * @return a new DirectoryStream that iterates over all entries in the given directory.
	 * 
	 * @throws NoSuchPathException
	 *             If a directory does not exists.
	 * @throws InvalidPathException
	 *             If dir is not a directory.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public DirectoryStream<Path> newDirectoryStream(Path dir) throws XenonException { 
		return newDirectoryStream(dir, ACCEPT_ALL_FILTER);
	}

	/**
	 * Create a DirectoryStream that iterates over all entries in the directory <code>dir</code> that are accepted by the filter.
	 * 
	 * @param dir
	 *            the target directory.
	 * @param filter
	 *            the filter.
	 * 
	 * @return a new DirectoryStream that iterates over all entries in the directory <code>dir</code>.
	 * 
	 * @throws NoSuchPathException
	 *             If a directory does not exists.
	 * @throws InvalidPathException
	 *             If dir is not a directory.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public abstract DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter filter) throws XenonException;

	/**
	 * Create a DirectoryStream that iterates over all PathAttributePair entries in the directory <code>dir</code>.
	 * 
	 * @param dir
	 *            the target directory.
	 * 
	 * @return a new DirectoryStream that iterates over all PathAttributePair entries in the directory <code>dir</code>.
	 * 
	 * @throws NoSuchPathException
	 *             If a directory does not exists.
	 * @throws InvalidPathException
	 *             If dir is not a directory.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir) throws XenonException {
		return newAttributesDirectoryStream(dir, ACCEPT_ALL_FILTER);
	}

	/**
	 * Create a DirectoryStream that iterates over all PathAttributePair entries in the directory <code>dir</code> that are
	 * accepted by the filter.
	 * 
	 * @param dir
	 *            the target directory.
	 * @param filter
	 *            the filter.
	 * 
	 * @return a new DirectoryStream that iterates over all entries in the directory <code>dir</code>.
	 * 
	 * @throws NoSuchPathException
	 *             If a directory does not exists.
	 * @throws InvalidPathException
	 *             If dir is not a directory.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public abstract DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir, DirectoryStream.Filter filter) throws XenonException;

	/**
	 * Open an existing file and return an {@link InputStream} to read from this file.
	 * 
	 * @param path
	 *            the existing file to read.
	 * 
	 * @return the {@link InputStream} to read from the file.
	 * 
	 * @throws NoSuchPathException
	 *             If a file does not exists.
	 * @throws InvalidPathException
	 *             If path not file.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public abstract InputStream newInputStream(Path path) throws XenonException;

	/**
	 * Open an file and return an {@link OutputStream} to write to this file.
	 * <p>
	 * The options determine how the file is opened, if a new file is created, if the existing data in the file is preserved, and
	 * if the file should be written or read:
	 * </p>
	 * <ul>
	 * <li>
	 * If the <code>CREATE</code> option is specified, a new file will be created and an exception is thrown if the file already
	 * exists.
	 * </li>
	 * <li>
	 * If the <code>OPEN_EXISTING</code> option is specified, an existing file will be opened, and an exception is thrown if the
	 * file does not exist.
	 * </li>
	 * <li>
	 * If the <code>OPEN_OR_CREATE</code> option is specified, an attempt will be made to open an existing file. If it does not
	 * exist a new file will be created.
	 * </li>
	 * <li>
	 * If the <code>APPEND</code> option is specified, data will be added to the end of the file. No existing data will be
	 * overwritten.
	 * </li>
	 * <li>
	 * If the <code>TRUNCATE</code> option is specified, any existing data in the file will be deleted (resulting in a file of
	 * size 0). The data will then be appended from the beginning of the file.
	 * </li>
	 * </ul>
	 * <p>
	 * One of <code>CREATE</code>, <code>OPEN_EXISTING</code> or <code>OPEN_OR_CREATE</code> must be specified. Specifying more
	 * than one will result in an exception.
	 * </p>
	 * <p>
	 * Either <code>APPEND</code> or <code>TRUNCATE</code> must be specified. Specifying both will result in an exception.
	 * </p>
	 * <p>
	 * The <code>READ</code> option must not be set. If it is set, an exception will be thrown.
	 * </p>
	 * <p>
	 * If the <code>WRITE</code> option is specified, the file is opened for writing. As this is the default behavior, the
	 * <code>WRITE</code> option may be omitted.
	 * </p>
	 * @param path
	 *            the target file for the OutputStream.
	 * @param options
	 *            the options to use for opening this file.
	 * 
	 * @return the {@link OutputStream} to write to the file.
	 * 
	 * @throws InvalidPathException
	 *             If path is not a file.
	 * @throws InvalidOptionsException
	 *             If an invalid combination of OpenOptions was provided.
	 * @throws XenonException
	 *             If an I/O error occurred.
	 */
	public abstract OutputStream newOutputStream(Path path, OpenOption... options) throws XenonException;

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
	public abstract CopyHandle copy(CopyDescription description) throws XenonException;

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
