package nl.esciencecenter.octopus.adaptors.local;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import nl.esciencecenter.octopus.OctopusProperties;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.files.FilesAdaptor;
import nl.esciencecenter.octopus.engine.files.FilesEngine;
import nl.esciencecenter.octopus.engine.files.PathImplementation;
import nl.esciencecenter.octopus.exceptions.DirectoryNotEmptyException;
import nl.esciencecenter.octopus.exceptions.FileAlreadyExistsException;
import nl.esciencecenter.octopus.exceptions.NoSuchFileException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.AclEntry;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.DeleteOption;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.PathAttributes;
import nl.esciencecenter.octopus.files.PosixFilePermission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalFiles implements FilesAdaptor {

    private static final Logger logger = LoggerFactory.getLogger(LocalFiles.class);

    private final OctopusEngine octopusEngine;
    private final LocalAdaptor localAdaptor;

    public LocalFiles(OctopusProperties properties, LocalAdaptor localAdaptor, OctopusEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
        this.localAdaptor = localAdaptor;

        if (logger.isDebugEnabled()) {
            Set<String> attributeViews = FileSystems.getDefault().supportedFileAttributeViews();

            logger.debug(Arrays.toString(attributeViews.toArray()));
        }
    }

    @Override
    public Path newPath(OctopusProperties properties, URI location) throws OctopusException {
        localAdaptor.checkURI(location);
        return new PathImplementation(properties, location, localAdaptor.getName(), octopusEngine);
    }

    @Override
    public Path copy(Path source, Path target, CopyOption... options) throws OctopusException {
        if (CopyOption.contains(options, CopyOption.REPLACE_EXISTING)) {
            if (exists(target) && isDirectory(target)
                    && newDirectoryStream(target, FilesEngine.ACCEPT_ALL_FILTER).iterator().hasNext()) {
                throw new DirectoryNotEmptyException("cannot replace dir " + target + " as it is not empty", "local",
                        source.toUri());
            }
        } else if (exists(target)) {
            throw new FileAlreadyExistsException("cannot copy to " + target + " as it already exists", "local", source.toUri());
        }

        try {
            Files.copy(LocalUtils.javaPath(source), LocalUtils.javaPath(target), LocalUtils.javaCopyOptions(options));
        } catch (IOException e) {
            throw new OctopusException("could not copy file", e, null, null);
        }

        //        if (CopyOption.contains(options, CopyOption.RECURSIVE) && isDirectory(source)) {
        //            for (Path child : newDirectoryStream(source, FilesEngine.ACCEPT_ALL_FILTER)) {
        //                copy(child, target.resolve(child.getFileName()), options);
        //            }
        //        }

        return target;
    }

    @Override
    public Path move(Path source, Path target, CopyOption... options) throws OctopusException {
        if (source.normalize().equals(target.normalize())) {
            return target;
        }

        if (exists(target) && isDirectory(target)
                && newDirectoryStream(target, FilesEngine.ACCEPT_ALL_FILTER).iterator().hasNext()
                && !CopyOption.contains(options, CopyOption.REPLACE_EXISTING)) {
            throw new OctopusException("cannot move file, target already exists", null, null);
        }

        // FIXME: test if this also works across different partitions/drives in
        // a single machine (different FileStores)
        try {
            Files.move(LocalUtils.javaPath(source), LocalUtils.javaPath(target), LocalUtils.javaCopyOptions(options));
        } catch (IOException e) {
            throw new OctopusException("could not move files", e, null, null);
        }

        return target;
    }

    @Override
    public Path createDirectories(Path dir, Set<PosixFilePermission> permissions) throws OctopusException {
        if (exists(dir) && !isDirectory(dir)) {
            throw new FileAlreadyExistsException("Cannot create directory, as it already exists (but is not a directory).",
                    "local", dir.toUri());
        }

        try {
            Files.createDirectories(LocalUtils.javaPath(dir), LocalUtils.javaPermissionAttribute(permissions));
        } catch (IOException e) {
            throw new OctopusException("could not create directories", e, null, null);
        }

        return dir;
    }

    @Override
    public Path createDirectory(Path dir, Set<PosixFilePermission> permissions) throws OctopusException {
        if (exists(dir)) {
            throw new FileAlreadyExistsException("Cannot create directory, as it already exists.", "local", dir.toUri());
        }

        try {
            Files.createDirectories(LocalUtils.javaPath(dir), LocalUtils.javaPermissionAttribute(permissions));
        } catch (IOException e) {
            throw new OctopusException("could not create directory", e, null, null);
        }

        return dir;
    }

    @Override
    public Path createFile(Path path, Set<PosixFilePermission> permissions) throws OctopusException {
        if (exists(path)) {
            throw new FileAlreadyExistsException("Cannot create file, as it already exists", "local", path.toUri());
        }

        try {
            Files.createFile(LocalUtils.javaPath(path), LocalUtils.javaPermissionAttribute(permissions));
        } catch (IOException e) {
            throw new OctopusException("could not create file", e, null, null);
        }

        return path;
    }

    @Override
    public Path createSymbolicLink(Path link, Path target) throws OctopusException {
        if (exists(link)) {
            throw new FileAlreadyExistsException("Cannot create link, as a file with this name already exists", "local",
                    link.toUri());
        }

        try {
            Files.createSymbolicLink(LocalUtils.javaPath(link), LocalUtils.javaPath(target));
        } catch (IOException e) {
            throw new OctopusException("could not create symbolic link", e, null, null);
        }

        return link;
    }

    @Override
    public Path readSymbolicLink(Path link) throws OctopusException {
        try {
            java.nio.file.Path target = Files.readSymbolicLink(LocalUtils.javaPath(link));

            return new PathImplementation(link.getProperties(), target.toUri(), link.getAdaptorName(),
                    octopusEngine);
        } catch (IOException e) {
            throw new OctopusException("could not create symbolic link", e, null, null);
        }
    }

    @Override
    public void delete(Path path, DeleteOption... options) throws OctopusException {
        if (!exists(path)) {
            throw new NoSuchFileException("cannot delete file, as it does not exist", "local", path.toUri());
        }

        // recursion step
        if (isDirectory(path) && DeleteOption.contains(options, DeleteOption.RECURSIVE)) {
            for (Path child : newDirectoryStream(path, FilesEngine.ACCEPT_ALL_FILTER)) {
                delete(child, options);
            }
        }

        try {
            Files.delete(LocalUtils.javaPath(path));
        } catch (IOException e) {
            throw new OctopusException("could not delete file", e, null, null);
        }
    }

    @Override
    public boolean deleteIfExists(Path path, DeleteOption... options) throws OctopusException {
        if (isDirectory(path) && DeleteOption.contains(options, DeleteOption.RECURSIVE)) {
            for (Path child : newDirectoryStream(path, FilesEngine.ACCEPT_ALL_FILTER)) {
                delete(child, options);
            }
        }

        try {
            return Files.deleteIfExists(LocalUtils.javaPath(path));
        } catch (IOException e) {
            throw new OctopusException("could not delete file", e, null, null);
        }
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter filter) throws OctopusException {
        if (!isDirectory(dir)) {
            throw new OctopusException("Cannot create directorystream, file is not a directory", null, null);
        }

        return new LocalDirectoryStream(dir, filter);
    }

    @Override
    public DirectoryStream<PathAttributes> newAttributesDirectoryStream(Path dir, DirectoryStream.Filter filter)
            throws OctopusException {
        if (!isDirectory(dir)) {
            throw new OctopusException("Cannot create DirectoryAttributeStream, file is not a directory", null, null);
        }

        return new LocalDirectoryAttributeStream(this, dir, filter);
    }

    @Override
    public InputStream newInputStream(Path path) throws OctopusException {
        try {
            return Files.newInputStream(LocalUtils.javaPath(path));
        } catch (IOException e) {
            throw new OctopusException("Could not create input stream", e, null, null);
        }
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws OctopusException {
        try {
            return Files.newOutputStream(LocalUtils.javaPath(path), LocalUtils.javaOpenOptions(options));
        } catch (IOException e) {
            throw new OctopusException("Could not output stream", e, null, null);
        }

    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<PosixFilePermission> permissions, OpenOption... options)
            throws OctopusException {
        try {
            return Files.newByteChannel(LocalUtils.javaPath(path), LocalUtils.javaOpenOptionsSet(options),
                    LocalUtils.javaPermissionAttribute(permissions));
        } catch (IOException e) {
            throw new OctopusException("Could not create byte channel", e, null, null);
        }
    }

    @Override
    public FileAttributes readAttributes(Path path) throws OctopusException {
        return new LocalFileAttributes(path);
    }

    @Override
    public boolean exists(Path path) throws OctopusException {
        return Files.exists(LocalUtils.javaPath(path));
    }

    @Override
    public boolean isDirectory(Path path) throws OctopusException {
        return Files.isDirectory(LocalUtils.javaPath(path));
    }

    @Override
    public Path setOwner(Path path, String user, String group) throws OctopusException {
        try {
            PosixFileAttributeView view = Files.getFileAttributeView(LocalUtils.javaPath(path), PosixFileAttributeView.class);

            if (user != null) {
                UserPrincipal userPrincipal =
                        FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(user);

                view.setOwner(userPrincipal);
            }

            if (group != null) {
                GroupPrincipal groupPrincipal =
                        FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByGroupName(group);

                view.setGroup(groupPrincipal);
            }

            return path;
        } catch (IOException e) {
            throw new OctopusException("Unable to set user and group", e, null, null);
        }

    }

    @Override
    public Path setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws OctopusException {
        try {
            PosixFileAttributeView view = Files.getFileAttributeView(LocalUtils.javaPath(path), PosixFileAttributeView.class);

            view.setPermissions(LocalUtils.javaPermissions(permissions));

            return path;
        } catch (IOException e) {
            throw new OctopusException("Unable to set permissions", e, null, null);
        }
    }

    @Override
    public Path setFileTimes(Path path, long lastModifiedTime, long lastAccessTime, long createTime) throws OctopusException {
        try {
            PosixFileAttributeView view = Files.getFileAttributeView(LocalUtils.javaPath(path), PosixFileAttributeView.class);

            FileTime lastModifiedFileTime = null;
            FileTime lastAccessFileTime = null;
            FileTime createFileTime = null;

            if (lastModifiedTime != -1) {
                lastModifiedFileTime = FileTime.fromMillis(lastModifiedTime);
            }

            if (lastAccessTime != -1) {
                lastAccessFileTime = FileTime.fromMillis(lastAccessTime);
            }

            if (createTime != -1) {
                createFileTime = FileTime.fromMillis(createTime);
            }

            view.setTimes(lastModifiedFileTime, lastAccessFileTime, createFileTime);

            return path;
        } catch (IOException e) {
            throw new OctopusException("Unable to set file times", e, "local", path.toUri());
        }
    }

    @Override
    public void setAcl(Path path, List<AclEntry> acl) throws OctopusException {
        throw new UnsupportedOperationException("Local adaptor cannot handle ACLs yet");
    }

}
