package nl.esciencecenter.octopus.engine.files;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.util.List;
import java.util.Set;

import nl.esciencecenter.octopus.OctopusProperties;
import nl.esciencecenter.octopus.exceptions.DirectoryNotEmptyException;
import nl.esciencecenter.octopus.exceptions.FileAlreadyExistsException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.UnsupportedOperationException;
import nl.esciencecenter.octopus.files.AclEntry;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.DeleteOption;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.PathAttributes;
import nl.esciencecenter.octopus.files.PosixFilePermission;

public interface FilesAdaptor {

    // functions used to create files and streams

    public Path newPath(OctopusProperties properties, URI location) throws OctopusException;

    /**
     * Copy a file to a target file.
     * 
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
    Path copy(Path source, Path target, CopyOption... options) throws OctopusIOException;

    /**
     * Creates a directory by creating all nonexistent parent directories first.
     * 
     * @throws UnsupportedOperationException
     *             if the given Permissions cannot be set when the file is created
     * @throws FileAlreadyExistsException
     *             if {@code dir} exists but is not a directory <i>(optional specific exception)</i>
     */
    Path createDirectories(Path dir, Set<PosixFilePermission> permissions) throws OctopusIOException;

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
    Path createDirectory(Path dir, Set<PosixFilePermission> permissions) throws OctopusIOException;

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
    Path createFile(Path path, Set<PosixFilePermission> permissions) throws OctopusIOException;

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
    Path createSymbolicLink(Path link, Path target) throws OctopusIOException;

    /**
     * Deletes a file.
     */
    void delete(Path path, DeleteOption... options) throws OctopusIOException;

    /**
     * Deletes a file if it exists.
     */
    boolean deleteIfExists(Path path, DeleteOption... options) throws OctopusIOException;

    /**
     * Tests whether a file exists.
     */
    boolean exists(Path path) throws OctopusIOException;

    /**
     * Tests whether a file is a directory.
     */
    boolean isDirectory(Path path) throws OctopusIOException;

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
    Path move(Path source, Path target, CopyOption... options) throws OctopusIOException;

    /**
     * Opens a directory, returning a DirectoryStream to iterate over the entries in the directory.
     */
    DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter filter) throws OctopusIOException;

    /**
     * Opens a directory, returning a DirectoryStream to iterate over the entries in the directory. Extra functionality in RAL to
     * efficiently fetch all attributes for a directory.
     */
    DirectoryStream<PathAttributes> newAttributesDirectoryStream(Path dir, DirectoryStream.Filter filter) throws OctopusIOException;

    /** Opens a file, returning an input stream to read from the file. */
    InputStream newInputStream(Path path) throws OctopusIOException;

    /**
     * Opens or creates a file, returning an output stream that may be used to write bytes to the file. If no options are present
     * then this method works as if the CREATE, TRUNCATE_EXISTING, and WRITE options are present.
     */
    OutputStream newOutputStream(Path path, OpenOption... options) throws OctopusIOException;

    /**
     * Opens or creates a file, returning a seekable byte channel to access the file.
     */
    SeekableByteChannel newByteChannel(Path path, Set<PosixFilePermission> permissions, OpenOption... options)
            throws OctopusIOException;

    /**
     * Reads a file's attributes.
     */
    FileAttributes readAttributes(Path path) throws OctopusIOException;

    /**
     * Reads the target of a symbolic link (optional operation).
     */
    Path readSymbolicLink(Path link) throws OctopusIOException;

    /**
     * Updates the file owner and group. Use null for either to keep current owner/group
     */
    Path setOwner(Path path, String owner, String group) throws OctopusIOException;

    /**
     * Sets a file's POSIX permissions.
     */
    Path setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws OctopusIOException;

    /**
     * Updates a file's last modified, last access, and create time attribute. Use -1 to not set a certain attribute
     */
    Path setFileTimes(Path path, long lastModifiedTime, long lastAccessTime, long createTime) throws OctopusIOException;

    /**
     * Updates (replace) the access control list.
     */
    void setAcl(Path path, List<AclEntry> acl) throws OctopusIOException;

}
