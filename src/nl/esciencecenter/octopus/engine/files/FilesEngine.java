package nl.esciencecenter.octopus.engine.files;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.OctopusRuntimeException;
import nl.esciencecenter.octopus.files.AclEntry;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.DeleteOption;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.DirectoryStream.Filter;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.PathAttributes;
import nl.esciencecenter.octopus.files.PosixFilePermission;

/**
 * Engine for File operations. Implements functionality using File operations, Octopus create functions, and Adaptors' Files
 * object.
 * 
 * @author Niels Drost
 * 
 */
public class FilesEngine implements Files {

    public static DirectoryStream.Filter ACCEPT_ALL_FILTER = new DirectoryStream.Filter() {
        public boolean accept(Path file) throws OctopusIOException {
            return true;
        }
    };

    public static final int BUFFER_SIZE = 10240;

    private final OctopusEngine octopusEngine;

    public FilesEngine(OctopusEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
    }

    //Functions already implemented by the Engine:

    @Override
    public Path newPath(URI location) throws OctopusException {
        return newPath(null, location);
    }

    @Override
    public boolean isDirectory(Path path) throws OctopusIOException {
        return getAttributes(path).isDirectory();
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, OpenOption... options) throws OctopusIOException {
        return newByteChannel(path, null, options);
    }

    // Iterators

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir) throws OctopusIOException {
        return newDirectoryStream(dir, ACCEPT_ALL_FILTER);
    }

    @Override
    public DirectoryStream<PathAttributes> newAttributesDirectoryStream(Path dir) throws OctopusIOException {
        return newAttributesDirectoryStream(dir, ACCEPT_ALL_FILTER);
    }

    //Functions that need a File Adaptors to do the work

    @Override
    public Path newPath(Properties properties, URI location) throws OctopusException {
        Adaptor adaptor = octopusEngine.getAdaptorFor(location.getScheme());

        return adaptor.filesAdaptor().newPath(octopusEngine.getCombinedProperties(properties), location);
    }

    @Override
    public Path copy(Path source, Path target, CopyOption... options) throws OctopusIOException {
        if (source.getAdaptorName().equals(target.getAdaptorName())) {
            return getAdaptor(source).filesAdaptor().copy(source, target, options);
        } else if (source.isLocal()) {
            return getAdaptor(target).filesAdaptor().copy(source, target, options);
        } else if (target.isLocal()) {
            return getAdaptor(source).filesAdaptor().copy(source, target, options);
        } else {
            throw new OctopusIOException("cannot do inter-scheme third party copy (yet)", null, null);
        }
    }

    @Override
    public Path createDirectories(Path dir, Set<PosixFilePermission> permissions) throws OctopusIOException {
        return getAdaptor(dir).filesAdaptor().createDirectories(dir, permissions);
    }

    @Override
    public Path createDirectories(Path dir) throws OctopusIOException {
        return createDirectories(dir, null);
    }

    @Override
    public Path createDirectory(Path dir, Set<PosixFilePermission> permissions) throws OctopusIOException {
        return getAdaptor(dir).filesAdaptor().createDirectory(dir, permissions);
    }

    @Override
    public Path createDirectory(Path dir) throws OctopusIOException {
        return createDirectory(dir, null);
    }

    @Override
    public Path createFile(Path path, Set<PosixFilePermission> permissions) throws OctopusIOException {
        return getAdaptor(path).filesAdaptor().createFile(path, permissions);
    }

    @Override
    public Path createSymbolicLink(Path link, Path target) throws OctopusIOException {
        return getAdaptor(link).filesAdaptor().createSymbolicLink(link, target);
    }

    @Override
    public void delete(Path path, DeleteOption... options) throws OctopusIOException {
        getAdaptor(path).filesAdaptor().delete(path, options);
    }

    @Override
    public boolean deleteIfExists(Path path, DeleteOption... options) throws OctopusIOException {
        return getAdaptor(path).filesAdaptor().deleteIfExists(path);
    }

    @Override
    public boolean exists(Path path) throws OctopusIOException {
        return getAdaptor(path).filesAdaptor().exists(path);
    }

    private Adaptor getAdaptor(Path path) {
        try {
            return octopusEngine.getAdaptor(path.getAdaptorName());
        } catch (OctopusException e) {
            // This is a case that should never occur, the adaptor was already created, it cannot dissapear suddenly.
            // Therefore, we make this a runtime exception.
            throw new OctopusRuntimeException(getClass().getName(), "could not find adaptor named " + path.getAdaptorName(), e);
        }
    }

    @Override
    public Path move(Path source, Path target, CopyOption... options) throws OctopusIOException {
        if (source.getAdaptorName().equals(target.getAdaptorName())) {
            return getAdaptor(source).filesAdaptor().move(source, target, options);
        } else if (source.isLocal()) {
            return getAdaptor(target).filesAdaptor().move(source, target, options);
        } else if (target.isLocal()) {
            return getAdaptor(source).filesAdaptor().move(source, target, options);
        } else {
            throw new OctopusIOException("cannot do inter-scheme third party move (yet)", null, null);
        }
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter filter) throws OctopusIOException {
        return getAdaptor(dir).filesAdaptor().newDirectoryStream(dir, filter);
    }

    @Override
    public DirectoryStream<PathAttributes> newAttributesDirectoryStream(Path dir, Filter filter) throws OctopusIOException {
        return getAdaptor(dir).filesAdaptor().newAttributesDirectoryStream(dir, filter);
    }

    @Override
    public InputStream newInputStream(Path path) throws OctopusIOException {
        return getAdaptor(path).filesAdaptor().newInputStream(path);
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws OctopusIOException {
        // TODO: set default options for all functions, not just this one
        if (options == null || options.length == 0) {
            options = new OpenOption[] { OpenOption.CREATE, OpenOption.TRUNCATE_EXISTING, OpenOption.WRITE };
        }

        return getAdaptor(path).filesAdaptor().newOutputStream(path, options);
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<PosixFilePermission> permissions, OpenOption... options)
            throws OctopusIOException {
        return getAdaptor(path).filesAdaptor().newByteChannel(path, permissions, options);
    }

    @Override
    public FileAttributes getAttributes(Path path) throws OctopusIOException {
        return getAdaptor(path).filesAdaptor().readAttributes(path);
    }

    @Override
    public Path readSymbolicLink(Path link) throws OctopusIOException {
        return getAdaptor(link).filesAdaptor().readSymbolicLink(link);
    }

    @Override
    public void setOwner(Path path, String owner, String group) throws OctopusIOException {
        getAdaptor(path).filesAdaptor().setOwner(path, owner, group);
    }

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws OctopusIOException {
        getAdaptor(path).filesAdaptor().setPosixFilePermissions(path, permissions);
    }

    @Override
    public void setFileTimes(Path path, long lastModifiedTime, long lastAccessTime, long createTime) throws OctopusIOException {
        getAdaptor(path).filesAdaptor().setFileTimes(path, lastModifiedTime, lastAccessTime, createTime);
    }

    @Override
    public void setAcl(Path path, List<AclEntry> acl) throws OctopusIOException {
        getAdaptor(path).filesAdaptor().setAcl(path, acl);
    }
}
