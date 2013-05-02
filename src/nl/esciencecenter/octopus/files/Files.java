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
package nl.esciencecenter.octopus.files;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.util.Properties;
import java.util.Set;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.exceptions.FileAlreadyExistsException;
import nl.esciencecenter.octopus.exceptions.IllegalSourcePathException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.UnknownPropertyException;
import nl.esciencecenter.octopus.exceptions.UnsupportedOperationException;

/**
 * Files represents the Files interface Octopus.
 * 
 * This interface contains various methods for creating and closing FileSystems, creating AbsolutePaths and RelativePaths, and
 * operations on these Paths.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface Files {

    /**
     * Create a new FileSystem that represents a (possibly remote) data store at the <code>location</code>, using the
     * <code>credentials</code> to get access.
     * 
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
     * @throws InvalidLocationException
     *             If the location was invalid.
     * @throws InvalidCredentialsException
     *             If the credentials where invalid to access the location.
     * 
     * @throws OctopusException
     *             If the creation of the FileSystem failed.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public FileSystem newFileSystem(URI location, Credential credential, Properties properties) throws OctopusException,
            OctopusIOException;

    /**
     * Get a FileSystem that represents the local current working directory.
     * 
     * Multiple invocations of this method may return the same FileSystem.
     * 
     * @param properties
     *            optional properties to use when creating the FileSystem.
     * 
     * @return a FileSystem that represents the local current working directory.
     * 
     * @throws UnknownPropertyException
     *             If a unknown property was provided.
     * @throws InvalidPropertyException
     *             If a known property was provided with an invalid value.
     * 
     * @throws OctopusException
     *             If the creation of the FileSystem failed.
     */
    public FileSystem getLocalCWDFileSystem() throws OctopusException;

    /**
     * Get a FileSystem that represents the local home directory of the current user.
     * 
     * Multiple invocations of this method may return the same FileSystem.
     * 
     * @param properties
     *            optional properties to use when creating the FileSystem.
     * 
     * @return a FileSystem that represents the local home directory of the current user.
     * 
     * @throws UnknownPropertyException
     *             If a unknown property was provided.
     * @throws InvalidPropertyException
     *             If a known property was provided with an invalid value.
     * 
     * @throws OctopusException
     *             If the creation of the FileSystem failed.
     */
    public FileSystem getLocalHomeFileSystem() throws OctopusException;

    /**
     * Create a new AbsolutePath that represents a (possibly non existing) location on <code>filesystem.</code>
     * 
     * The AbsolutePath does not necessarily exists on the target FileSystem.
     * 
     * @param filesystem
     *            the FileSystem for which to create the path.
     * @param location
     *            the of the AbsolutePath within the given FileSystem.
     * 
     * @return the resulting AbsolutePath.
     * 
     * @throws OctopusException
     *             If the AbsolutePath could not be created.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public AbsolutePath newPath(FileSystem filesystem, RelativePath location) throws OctopusException, OctopusIOException;

    /**
     * Close a FileSystem.
     * 
     * @param filesystem
     *            the FileSystem to close.
     * 
     * @throws CannotClosedException
     *             If the FileSystem cannot be closed (for example a local FileSystem).
     * @throws OctopusException
     *             If the FileSystem failed to close.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public void close(FileSystem filesystem) throws OctopusException, OctopusIOException;

    /**
     * Test is a FileSystem is open.
     * 
     * @param filesystem
     *            the FileSystem to test.
     * 
     * @throws OctopusException
     *             If the test failed.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public boolean isOpen(FileSystem filesystem) throws OctopusException, OctopusIOException;

    /**
     * Copy an existing source file or link to a non-existing target path.
     * 
     * The source must NOT be a directory.
     * 
     * The parent of the target path (e.g. <code>target.getParent</code>) must exist.
     * 
     * If the target is equal to the source this method has no effect.
     * 
     * If the source is a link, the link itself will be copied, not the path to which it refers.
     * 
     * @param source
     *            the existing source file or link.
     * @param target
     *            the non existing target path.
     * @return the target path.
     * 
     * @throws NoSuchFileException
     *             If the source file does not exist or the target parent directory does not exist.
     * @throws FileAlreadyExistsException
     *             If the target file already exists.
     * @throws IllegalSourcePathException
     *             If the source is a directory.
     * @throws OctopusIOException
     *             If the move failed.
     */
    public AbsolutePath copy(AbsolutePath source, AbsolutePath target) throws OctopusIOException;

    /**
     * Move or rename an existing source path to a non-existing target path.
     * 
     * The parent of the target path (e.g. <code>target.getParent</code>) must exist.
     * 
     * If the target is equal to the source this method has no effect.
     * 
     * If the source is a link, the link itself will be moved, not the path to which it refers.
     * 
     * If the source is a directory, it will be renamed to the target. This implies that a moving a directory between physical
     * locations may fail.
     * 
     * @param source
     *            the existing source path.
     * @param target
     *            the non existing target path.
     * @return the target path.
     * 
     * @throws NoSuchFileException
     *             If the source file does not exist or the target parent directory does not exist.
     * @throws FileAlreadyExistsException
     *             If the target file already exists.
     * @throws OctopusIOException
     *             If the move failed.
     */
    public AbsolutePath move(AbsolutePath source, AbsolutePath target) throws OctopusIOException;

    /**
     * Creates a new directory, failing if the directory already exists. All nonexistent parent directories are also created.
     * 
     * @param dir
     *            the directory to create.
     * @return an AbsolutePath representing the created directory.
     * 
     * @throws FileAlreadyExistsException
     *             If the directory already exists or if a parent directory could not be created because a file with the same name
     *             already exists.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public AbsolutePath createDirectories(AbsolutePath dir) throws OctopusIOException;

    /**
     * Creates a new directory, failing if the directory already exists.
     * 
     * @param dir
     *            the directory to create.
     * 
     * @return an AbsolutePath representing the created directory.
     * 
     * @throws FileAlreadyExistsException
     *             If the directory already exists.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public AbsolutePath createDirectory(AbsolutePath dir) throws OctopusIOException;

    /**
     * Creates a new empty file, failing if the file already exists.
     * 
     * @param path
     *            the file to create.
     * 
     * @return an AbsolutePath representing the created file.
     * 
     * @throws FileAlreadyExistsException
     *             If the directory already exists.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public AbsolutePath createFile(AbsolutePath path) throws OctopusIOException;

    /**
     * Creates a symbolic link to a target, failing if the <code>link</code> already exists (optional operation).
     * 
     * @param link
     *            the link to create.
     * @param target
     *            the target to link to.
     * 
     * @return an AbsolutePath representing the created link.
     * 
     * @throws FileAlreadyExistsException
     *             If the <code>link</code> already exists.
     * @throws UnsupportedOperationException
     *             If the adaptor used does not support symbolic links.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public AbsolutePath createSymbolicLink(AbsolutePath link, AbsolutePath target) throws OctopusIOException;

    /**
     * Deletes an existing path.
     * 
     * If path is a symbolic link the symbolic link is removed and the symbolic link's target is not deleted.
     * 
     * @param path
     *            the path to delete.
     * 
     * @throws NoSuchFileExistsException
     *             If the <code>path</code> does not exist.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public void delete(AbsolutePath path) throws OctopusIOException;

    /**
     * Tests if a path exists.
     * 
     * @param path
     *            the path to test.
     * 
     * @return If the path exists.
     * 
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public boolean exists(AbsolutePath path) throws OctopusIOException;

    /**
     * Test if a path represents a directory.
     * 
     * @param path
     *            the path to test.
     * 
     * @return If the path represents a directory.
     * 
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public boolean isDirectory(AbsolutePath path) throws OctopusIOException;

    /**
     * Create a DirectoryStream that iterates over all entries in the directory <code>dir</code>.
     * 
     * @param dir
     *            the target directory.
     * @return a new DirectoryStream that iterates over all entries in the given directory.
     * 
     * @throws NoSuchFileException
     *             If a directory does not exists.
     * @throws IllegalSourcePathException
     *             If dir is not a directory.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public DirectoryStream<AbsolutePath> newDirectoryStream(AbsolutePath dir) throws OctopusIOException;

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
     * @throws NoSuchFileException
     *             If a directory does not exists.
     * @throws IllegalSourcePathException
     *             If dir is not a directory.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public DirectoryStream<AbsolutePath> newDirectoryStream(AbsolutePath dir, DirectoryStream.Filter filter)
            throws OctopusIOException;

    /**
     * Create a DirectoryStream that iterates over all PathAttributePair entries in the directory <code>dir</code>.
     * 
     * @param dir
     *            the target directory.
     * 
     * @return a new DirectoryStream that iterates over all PathAttributePair entries in the directory <code>dir</code>.
     * 
     * @throws NoSuchFileException
     *             If a directory does not exists.
     * @throws IllegalSourcePathException
     *             If dir is not a directory.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(AbsolutePath dir) throws OctopusIOException;

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
     * @throws NoSuchFileException
     *             If a directory does not exists.
     * @throws IllegalSourcePathException
     *             If dir is not a directory.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(AbsolutePath dir, DirectoryStream.Filter filter)
            throws OctopusIOException;

    /**
     * Open an existing file and return an {@link InputStream} to read from this file.
     * 
     * @param path
     *            the existing file to read.
     * 
     * @return the {@link InputStream} to read from the file.
     * 
     * @throws NoSuchFileException
     *             If a file does not exists.
     * @throws IllegalSourcePathException
     *             If path not file.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public InputStream newInputStream(AbsolutePath path) throws OctopusIOException;

    /**
     * Open an file and return an {@link OutputStream} to write to this file.
     * 
     * If no options are present then this method works as if the CREATE, TRUNCATE_EXISTING, and WRITE options are present.
     * 
     * If options are set, the <code>WRITE</code> option must be set.
     * 
     * If the file does not exist, it will be created first if the <code>CREATE</code> option is specified.
     * 
     * If the <code>CREATE_NEW</code> option is specified, a new file will be created and an exception is thrown if the file
     * already exists.
     * 
     * If the <code>APPEND</code> option is specified, data will be appended to the file.
     * 
     * If the <code>TRUNCATE_EXISTING</code> option is specified, an existing file will be truncated before data is appended.
     * 
     * @param path
     *            the target file for the OutputStream.
     * @param options
     *            the options to use for opening this file.
     * 
     * @return the {@link OutputStream} to write to the file.
     * 
     * @throws IllegalSourcePathException
     *             If path is not a file.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public OutputStream newOutputStream(AbsolutePath path, OpenOption... options) throws OctopusIOException;

    /**
     * Open an file and return an {@link SeekableByteChannel} to read from or write to this file.
     * 
     * Options may not be empty.
     * 
     * If the file does not exist, it will be created first if the <code>CREATE</code> option is specified.
     * 
     * If the <code>CREATE_NEW</code> option is specified, a new file will be created and an exception is thrown if the file
     * already exists.
     * 
     * If the <code>APPEND</code> option is specified, data will be appended to the file.
     * 
     * If the <code>TRUNCATE_EXISTING</code> option is specified, an existing file will be truncated before data is appended.
     * 
     * The <code>READ</code> and <code>WRITE</code> determine if the file is opened for reading or writing.
     * 
     * @param path
     *            the target file for the SeekableByteChannel.
     * @param options
     *            the options to use for opening this file.
     * 
     * @return the {@link SeekableByteChannel} to access the file.
     * 
     * @throws IllegalSourcePathException
     *             If path is not a file.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public SeekableByteChannel newByteChannel(AbsolutePath path, OpenOption... options) throws OctopusIOException;

    /**
     * Get the {@link FileAttributes} of an existing path.
     * 
     * @param path
     *            the existing path.
     * 
     * @return the FileAttributes of the path.
     * 
     * @throws NoSuchFileException
     *             If a file does not exists.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public FileAttributes getAttributes(AbsolutePath path) throws OctopusIOException;

    /**
     * Reads the target of a symbolic link (optional operation).
     * 
     * @param link
     *            the link to read.
     * 
     * @return an AbsolutePath representing the target of the link.
     * 
     * @throws NoSuchFileException
     *             If the link does not exists.
     * @throws IllegalSourcePathException
     *             If the source is not a link.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public AbsolutePath readSymbolicLink(AbsolutePath link) throws OctopusIOException;

    /**
     * Updates the file owner and group. Use null for either to keep current owner/group
     */

    /**
     * Set the owner an group of a path.
     * 
     * @param path
     *            the target path.
     * @param user
     *            the new user, or <code>null</code> if unused.
     * @param group
     *            the new group, or <code>null</code> if unused.
     * 
     * @throws NoSuchFileException
     *             If the target path does not exists.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public void setOwner(AbsolutePath path, String user, String group) throws OctopusIOException;

    /**
     * Sets the POSIX permissions of a path.
     * 
     * @param path
     *            the target path.
     * @param permissions
     *            the permissions to set.
     * 
     * @throws NoSuchFileException
     *             If the target path does not exists.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public void setPosixFilePermissions(AbsolutePath path, Set<PosixFilePermission> permissions) throws OctopusIOException;

    /**
     * Sets the last modified, last access and create time attributes of a path.
     * 
     * @param path
     *            the target path.
     * 
     * @param lastModifiedTime
     *            the new last modified time, or <code>-1</code> if unused.
     * @param lastAccessTime
     *            the new last access time, or <code>-1</code> if unused.
     * @param createTime
     *            the new create time, or <code>-1</code> if unused.
     * 
     * @throws NoSuchFileException
     *             If the target path does not exists.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    public void setFileTimes(AbsolutePath path, long lastModifiedTime, long lastAccessTime, long createTime)
            throws OctopusIOException;
}
