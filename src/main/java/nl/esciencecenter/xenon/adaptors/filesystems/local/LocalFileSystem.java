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
package nl.esciencecenter.xenon.adaptors.filesystems.local;

import static nl.esciencecenter.xenon.adaptors.filesystems.local.LocalFileAdaptor.ADAPTOR_NAME;
import static nl.esciencecenter.xenon.utils.LocalFileSystemUtils.getLocalRootlessPath;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.PathAttributesImplementation;
import nl.esciencecenter.xenon.filesystems.DirectoryNotEmptyException;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.NoSuchPathException;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.filesystems.PathAttributes;
import nl.esciencecenter.xenon.filesystems.PosixFilePermission;
import nl.esciencecenter.xenon.utils.LocalFileSystemUtils;

public class LocalFileSystem extends FileSystem {

    private final String root;

    protected LocalFileSystem(String uniqueID, String location, String root, Path entryPath, XenonProperties properties) {
        super(uniqueID, ADAPTOR_NAME, location, entryPath, properties);
        this.root = root;
    }

    // Path getRelativePath(String path, String root) throws XenonException {
    // if (!path.toUpperCase(Locale.getDefault()).startsWith(root.toUpperCase(Locale.getDefault()))) {
    // throw new XenonException(ADAPTOR_NAME, "Path does not start with root: " + path + " " + root);
    // }
    //
    // if (root.length() == path.length()) {
    // return new Path(LocalFileSystemUtils.getLocalSeparator(), "");
    // }
    //
    // return new Path(LocalFileSystemUtils.getLocalSeparator(), path.substring(root.length()));
    // }

    java.nio.file.Path javaPath(Path path) {

        if (path == null) {
            throw new IllegalArgumentException("Path may not be null");
        }

        Path relPath = path.normalize();
        int numElems = relPath.getNameCount();

        // replace tilde
        if (numElems != 0) {
            String firstPart = relPath.getName(0).toString();
            if ("~".equals(firstPart)) {
                String tmp = getLocalRootlessPath(System.getProperty("user.home"));
                Path home = new Path(LocalFileSystemUtils.getLocalSeparator(), tmp);

                if (numElems == 1) {
                    relPath = home;
                } else {
                    relPath = home.resolve(relPath.subpath(1, numElems));
                }
            }
        }

        Path absPath = toAbsolutePath(relPath);
        return FileSystems.getDefault().getPath(root, absPath.toString());
    }

    Set<PosixFilePermission> xenonPermissions(Set<java.nio.file.attribute.PosixFilePermission> permissions) {
        if (permissions == null) {
            return null;
        }

        Set<PosixFilePermission> result = new HashSet<>(permissions.size() * 4 / 3 + 1);

        for (java.nio.file.attribute.PosixFilePermission permission : permissions) {
            result.add(PosixFilePermission.valueOf(permission.toString()));
        }

        return result;
    }

    Set<java.nio.file.attribute.PosixFilePermission> javaPermissions(Set<PosixFilePermission> permissions) {

        if (permissions == null) {
            return new HashSet<>(0);
        }

        Set<java.nio.file.attribute.PosixFilePermission> result = new HashSet<>(permissions.size() * 4 / 3 + 1);

        for (PosixFilePermission permission : permissions) {
            result.add(java.nio.file.attribute.PosixFilePermission.valueOf(permission.toString()));
        }

        return result;
    }

    /*
     * Delete a local file or directory
     */
    void deleteLocal(Path path) throws XenonException {
        try {
            Files.delete(javaPath(path));
        } catch (java.nio.file.NoSuchFileException e1) {
            throw new NoSuchPathException(ADAPTOR_NAME, "File " + path + " does not exist!", e1);
        } catch (java.nio.file.DirectoryNotEmptyException e2) {
            throw new DirectoryNotEmptyException(ADAPTOR_NAME, "Directory " + path + " not empty!", e2);
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to delete file " + path, e);
        }
    }

    PathAttributes getLocalFileAttributes(Path path) throws XenonException {
        return getLocalFileAttributes(path, javaPath(path));
    }

    PathAttributes getLocalFileAttributes(Path p, java.nio.file.Path path) throws XenonException {
        try {
            PathAttributesImplementation result = new PathAttributesImplementation();

            result.setPath(p);
            result.setExecutable(Files.isExecutable(path));
            result.setReadable(Files.isReadable(path));
            result.setReadable(Files.isWritable(path));

            boolean isWindows = LocalFileSystemUtils.isWindows();

            BasicFileAttributes basicAttributes;

            if (isWindows) {
                // TODO: Seems to fail in Windows ?
                result.setHidden(false);

                // These should always work.
                basicAttributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);

                // // These are windows only.
                // AclFileAttributeView aclAttributes =
                // Files.getFileAttributeView(javaPath,
                // AclFileAttributeView.class,
                // LinkOption.NOFOLLOW_LINKS);

            } else {
                result.setHidden(Files.isHidden(path));

                // Note: when in a posix environment, basicAttributes point to
                // posixAttributes.
                java.nio.file.attribute.PosixFileAttributes posixAttributes = Files.readAttributes(path, java.nio.file.attribute.PosixFileAttributes.class,
                        LinkOption.NOFOLLOW_LINKS);

                basicAttributes = posixAttributes;

                result.setOwner(posixAttributes.owner().getName());
                result.setGroup(posixAttributes.group().getName());
                result.setPermissions(xenonPermissions(posixAttributes.permissions()));
            }

            result.setCreationTime(basicAttributes.creationTime().toMillis());
            result.setLastAccessTime(basicAttributes.lastAccessTime().toMillis());
            result.setLastModifiedTime(basicAttributes.lastModifiedTime().toMillis());

            result.setDirectory(basicAttributes.isDirectory());
            result.setRegular(basicAttributes.isRegularFile());
            result.setSymbolicLink(basicAttributes.isSymbolicLink());
            result.setOther(basicAttributes.isOther());

            if (result.isRegular()) {
                result.setSize(basicAttributes.size());
            }

            return result;
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Cannot read attributes.", e);
        }
    }

    @Override
    public boolean isOpen() throws XenonException {
        return true;
    }

    @Override
    public void rename(Path source, Path target) throws XenonException {

        Path absSource = toAbsolutePath(source);
        Path absTarget = toAbsolutePath(target);

        if (areSamePaths(absSource, absTarget)) {
            return;
        }

        assertPathExists(absSource);
        assertPathNotExists(absTarget);
        assertParentDirectoryExists(absTarget);

        try {
            Files.move(javaPath(absSource), javaPath(absTarget));
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to move " + absSource + " to " + absTarget, e);
        }
    }

    @Override
    public void createDirectory(Path dir) throws XenonException {

        Path absdir = toAbsolutePath(dir);
        assertPathNotExists(absdir);
        assertParentDirectoryExists(absdir);

        try {
            Files.createDirectory(javaPath(absdir));
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to create directory " + dir, e);
        }
    }

    @Override
    public void createFile(Path file) throws XenonException {

        Path absFile = toAbsolutePath(file);
        assertPathNotExists(absFile);
        assertParentDirectoryExists(absFile);

        try {
            Files.createFile(javaPath(absFile));
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to create file " + absFile, e);
        }
    }

    @Override
    public void createSymbolicLink(Path link, Path path) throws XenonException {

        Path absLink = toAbsolutePath(link);
        assertPathNotExists(absLink);
        assertParentDirectoryExists(absLink);

        try {
            Files.createSymbolicLink(javaPath(absLink), javaPath(path));
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to create link " + absLink + " to " + path, e);
        }
    }

    @Override
    protected void deleteFile(Path path) throws XenonException {
        deleteLocal(path);
    }

    @Override
    protected void deleteDirectory(Path path) throws XenonException {
        deleteLocal(path);
    }

    @Override
    public boolean exists(Path path) throws XenonException {
        return Files.exists(javaPath(toAbsolutePath(path)), java.nio.file.LinkOption.NOFOLLOW_LINKS);
    }

    @Override
    protected List<PathAttributes> listDirectory(Path dir) throws XenonException {

        try {
            ArrayList<PathAttributes> result = new ArrayList<>();

            DirectoryStream<java.nio.file.Path> s = Files.newDirectoryStream(javaPath(dir));

            for (java.nio.file.Path p : s) {
                result.add(getLocalFileAttributes(dir.resolve(p.getFileName().toString()), p));
            }

            s.close();

            return result;
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to list directory: " + dir, e);
        }
    }

    @Override
    public InputStream readFromFile(Path path) throws XenonException {

        Path absPath = toAbsolutePath(path);
        assertFileExists(absPath);

        try {
            return Files.newInputStream(javaPath(absPath));
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to create InputStream.", e);
        }
    }

    @Override
    public OutputStream writeToFile(Path path, long size) throws XenonException {

        Path absPath = toAbsolutePath(path);
        assertPathNotExists(absPath);
        try {
            return Files.newOutputStream(javaPath(absPath), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to create OutputStream.", e);
        }
    }

    @Override
    public OutputStream writeToFile(Path path) throws XenonException {
        return writeToFile(path, -1);
    }

    @Override
    public OutputStream appendToFile(Path path) throws XenonException {

        Path absPath = toAbsolutePath(path);
        assertFileExists(absPath);

        try {
            return Files.newOutputStream(javaPath(absPath), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to create OutputStream.", e);
        }
    }

    @Override
    public PathAttributes getAttributes(Path path) throws XenonException {
        Path absPath = toAbsolutePath(path);
        assertPathExists(absPath);
        return getLocalFileAttributes(absPath);
    }

    @Override
    public Path readSymbolicLink(Path link) throws XenonException {

        Path absLink = toAbsolutePath(link);
        assertFileIsSymbolicLink(absLink);

        try {
            java.nio.file.Path path = javaPath(absLink);
            java.nio.file.Path target = Files.readSymbolicLink(path);

            Path parent = absLink.getParent();

            if (parent == null || target.isAbsolute()) {
                return new Path(absLink.getSeparator(), getLocalRootlessPath(target.toString()));
            }

            return parent.resolve(new Path(absLink.getSeparator(), getLocalRootlessPath(target.toString())));
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to read symbolic link.", e);
        }
    }

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {

        if (permissions == null) {
            throw new IllegalArgumentException("Permissions is null!");
        }

        Path absPath = toAbsolutePath(path);

        assertPathExists(absPath);

        try {
            PosixFileAttributeView view = Files.getFileAttributeView(javaPath(absPath), PosixFileAttributeView.class);
            view.setPermissions(javaPermissions(permissions));
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to set permissions " + absPath, e);
        }
    }
}
