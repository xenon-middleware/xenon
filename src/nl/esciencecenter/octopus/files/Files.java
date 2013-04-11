package nl.esciencecenter.octopus.files;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.util.Properties;
import java.util.Set;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.exceptions.DirectoryNotEmptyException;
import nl.esciencecenter.octopus.exceptions.FileAlreadyExistsException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.UnsupportedOperationException;

public interface Files {

    // functions used to create files and streams
    
    /**
     * Create a new FileSystem that represents a (possibly remote) data store at the given location and using the given 
     * credentials.  
     * 
     * @param location the location of the FileSystem.
     * @param credential the credentials to use to get access to the FileSystem. 
     * @param properties optional properties to use when creating the FileSystem.
     * @return
     * @throws OctopusException
     * @throws OctopusIOException
     */
    public FileSystem newFileSystem(URI location, Credential credential, Properties properties) 
            throws OctopusException, OctopusIOException;

    // FIXME add getCWDFileSystem()
    // FIXME add getHomeFileSystem()
    
    public Path newPath(FileSystem filesystem, String location) throws OctopusException, OctopusIOException;
    
    public void close(FileSystem filesystem) throws OctopusException, OctopusIOException;
    
    public boolean isOpen(FileSystem filesystem) throws OctopusException, OctopusIOException;
        
    /**
     * Copy a file to a target file.
     * 
     * @throws UnsupportedOperationException
     *             if the array contains a copy option that is not supported
     * @throws FileAlreadyExistsException
     *             if the target file exists but cannot be replaced because the {@code REPLACE_EXISTING} option is not specified
     *             <i>(optional specific exception)</i>
     * @throws DirectoryNotEmptyException
     *             the {@code REPLACE_EXISTING} option is specified but the file cannot be replaced because it is a non-empty
     *             directory <i>(optional specific exception)</i>
     * @throws OctopusIOException
     *             if an I/O error occurs
     * */
    public Path copy(Path source, Path target, CopyOption... options) throws OctopusIOException;

    /**
     * Creates a directory by creating all nonexistent parent directories first.
     * 
     * @throws UnsupportedOperationException
     *             if the given Permissions cannot be set when the file is created
     * @throws FileAlreadyExistsException
     *             if {@code dir} exists but is not a directory <i>(optional specific exception)</i>
     */
    //public Path createDirectories(Path dir, Set<PosixFilePermission> permissions) throws OctopusIOException;

    /**
     * Creates a directory by creating all nonexistent parent directories first.
     * 
     * @throws UnsupportedOperationException
     *             if the given Permissions cannot be set when the file is created
     * @throws FileAlreadyExistsException
     *             if {@code dir} exists but is not a directory <i>(optional specific exception)</i>
     */
    public Path createDirectories(Path dir) throws OctopusIOException;

    /**
     * Creates a new directory.
     * 
     * @throws UnsupportedOperationException
     *             if the given Permissions cannot be set when the file is created
     * @throws FileAlreadyExistsException
     *             if a directory could not otherwise be created because a file of that name already exists <i>(optional specific
     *             exception)</i>
     * @throws OctopusIOException
     *             if an I/O error occurs or the parent directory does not exist
     */
   // public Path createDirectory(Path dir, Set<PosixFilePermission> permissions) throws OctopusIOException;

    /**
     * Creates a new directory.
     * 
     * @throws UnsupportedOperationException
     *             if the given Permissions cannot be set when the file is created
     * @throws FileAlreadyExistsException
     *             if a directory could not otherwise be created because a file of that name already exists <i>(optional specific
     *             exception)</i>
     * @throws OctopusIOException
     *             if an I/O error occurs or the parent directory does not exist
     */
    public Path createDirectory(Path dir) throws OctopusIOException;

    /**
     * Creates a new and empty file, failing if the file already exists.
     * 
     * @throws UnsupportedOperationException
     *             if the given Permissions cannot be set when the file is created
     * @throws FileAlreadyExistsException
     *             if a file of that name already exists <i>(optional specific exception)</i>
     * @throws OctopusIOException
     *             if an I/O error occurs or the parent directory does not exist
     */
    //public Path createFile(Path path, Set<PosixFilePermission> permissions) throws OctopusIOException;

    public Path createFile(Path path) throws OctopusIOException;

    
    /**
     * Creates a symbolic link to a target (optional operation).
     * 
     * @throws UnsupportedOperationException
     *             if the adaptor used does not support symbolic links.
     * @throws FileAlreadyExistsException
     *             if a file with the name already exists <i>(optional specific exception)</i>
     * @throws OctopusIOException
     *             if an I/O error occurs
     */
    public Path createSymbolicLink(Path link, Path target) throws OctopusIOException;

    /**
     * Deletes a file.
     */
    public void delete(Path path) throws OctopusIOException;
    
    /**
     * Tests whether a file exists.
     */
    public boolean exists(Path path) throws OctopusIOException;

    /**
     * Tests whether a file is a directory.
     */
    public boolean isDirectory(Path path) throws OctopusIOException;

    /**
     * Move or rename a file to a target file.
     * 
     * @throws UnsupportedOperationException
     *             if the array contains a copy option that is not supported
     * @throws FileAlreadyExistsException
     *             if the target file exists but cannot be replaced because the {@code REPLACE_EXISTING} option is not specified
     *             <i>(optional specific exception)</i>
     * @throws DirectoryNotEmptyException
     *             the {@code REPLACE_EXISTING} option is specified but the file cannot be replaced because it is a non-empty
     *             directory <i>(optional specific exception)</i>
     * @throws OctopusIOException
     *             if an I/O error occurs
     */
    public Path move(Path source, Path target, CopyOption... options) throws OctopusIOException;

    /**
     * Opens a directory, returning a DirectoryStream to iterate over all entries in the directory.
     */
    public DirectoryStream<Path> newDirectoryStream(Path dir) throws OctopusIOException;

    /**
     * Opens a directory, returning a DirectoryStream to iterate over the entries in the directory.
     */
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter filter) throws OctopusIOException;

    /**
     * Opens a directory, returning a DirectoryStream to iterate over the PathAttributes of all entries in the directory.
     */
    public DirectoryStream<PathAttributes> newAttributesDirectoryStream(Path dir) throws OctopusIOException;

    /**
     * Opens a directory, returning a DirectoryStream to iterate over the entries in the directory. Extra functionality in RAL to
     * efficiently fetch all attributes for a directory.
     */
    public DirectoryStream<PathAttributes> newAttributesDirectoryStream(Path dir, DirectoryStream.Filter filter)
            throws OctopusIOException;

    /** Opens a file, returning an input stream to read from the file. */
    public InputStream newInputStream(Path path) throws OctopusIOException;

    /**
     * Opens or creates a file, returning an output stream that may be used to write bytes to the file. If no options are present
     * then this method works as if the CREATE, TRUNCATE_EXISTING, and WRITE options are present.
     */
    public OutputStream newOutputStream(Path path, OpenOption... options) throws OctopusIOException;

    /**
     * Opens or creates a file, returning a seekable byte channel to access the file.
     */
    public SeekableByteChannel newByteChannel(Path path, Set<PosixFilePermission> permissions, OpenOption... options)
            throws OctopusIOException;

    /**
     * Opens or creates a file, returning a seekable byte channel to access the file.
     */
    public SeekableByteChannel newByteChannel(Path path, OpenOption... options) throws OctopusIOException;

    /**
     * Reads a file's attributes.
     */
    public FileAttributes getAttributes(Path path) throws OctopusIOException;

    /**
     * Reads the target of a symbolic link (optional operation).
     */
    public Path readSymbolicLink(Path link) throws OctopusIOException;

    /**
     * Updates the file owner and group. Use null for either to keep current owner/group
     */
    public void setOwner(Path path, String user, String group) throws OctopusIOException;

    /**
     * Sets a file's POSIX permissions.
     */
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws OctopusIOException;

    /**
     * Updates a file's last modified, last access, and create time attribute. Use -1 to not set a certain attribute
     */
    public void setFileTimes(Path path, long lastModifiedTime, long lastAccessTime, long createTime) throws OctopusIOException;

    /**
     * Updates (replace) the access control list.
     */
    // public void setAcl(Path path, List<AclEntry> acl) throws OctopusIOException;
}
