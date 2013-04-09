package nl.esciencecenter.octopus.adaptors.ssh;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import nl.esciencecenter.octopus.ImmutableTypedProperties;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.files.FilesAdaptor;
import nl.esciencecenter.octopus.engine.files.PathImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.AclEntry;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.DeleteOption;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.DirectoryStream.Filter;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.PathAttributes;
import nl.esciencecenter.octopus.files.PosixFilePermission;
import nl.esciencecenter.octopus.security.Credentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshFiles implements FilesAdaptor {
    private static final Logger logger = LoggerFactory.getLogger(SshFiles.class);

    private final OctopusEngine octopusEngine;
    private final SshAdaptor sshAdaptor;

    public SshFiles(ImmutableTypedProperties properties, SshAdaptor sshAdaptor, OctopusEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
        this.sshAdaptor = sshAdaptor;

        if (logger.isDebugEnabled()) {
            Set<String> attributeViews = FileSystems.getDefault().supportedFileAttributeViews();

            logger.debug(Arrays.toString(attributeViews.toArray()));
        }
    }

    @Override
    public Path newPath(ImmutableTypedProperties properties, Credentials credentials, URI location) throws OctopusException {
        sshAdaptor.checkURI(location);
        return new PathImplementation(properties, credentials, location, sshAdaptor.getName(), octopusEngine);
    }

    @Override
    public Path copy(Path source, Path target, CopyOption... options) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path createDirectories(Path dir, Set<PosixFilePermission> permissions) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path createDirectory(Path dir, Set<PosixFilePermission> permissions) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path createFile(Path path, Set<PosixFilePermission> permissions) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path createSymbolicLink(Path link, Path target) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delete(Path path, DeleteOption... options) throws OctopusException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean deleteIfExists(Path path, DeleteOption... options) throws OctopusException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean exists(Path path) throws OctopusException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDirectory(Path path) throws OctopusException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Path move(Path source, Path target, CopyOption... options) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter filter) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DirectoryStream<PathAttributes> newAttributesDirectoryStream(Path dir, Filter filter) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream newInputStream(Path path) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<PosixFilePermission> permissions, OpenOption... options)
            throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileAttributes readAttributes(Path path) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path readSymbolicLink(Path link) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path setOwner(Path path, String owner, String group) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path setFileTimes(Path path, long lastModifiedTime, long lastAccessTime, long createTime) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAcl(Path path, List<AclEntry> acl) throws OctopusException {
        // TODO Auto-generated method stub
        
    }
    
    protected void end() {
    }
}
