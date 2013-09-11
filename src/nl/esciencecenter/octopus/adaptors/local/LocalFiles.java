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
package nl.esciencecenter.octopus.adaptors.local;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.octopus.OctopusException;
import nl.esciencecenter.octopus.OctopusPropertyDescription.Component;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.files.FileSystemImplementation;
import nl.esciencecenter.octopus.engine.files.FilesEngine;
import nl.esciencecenter.octopus.engine.files.PathImplementation;
import nl.esciencecenter.octopus.engine.util.CopyEngine;
import nl.esciencecenter.octopus.engine.util.CopyInfo;
import nl.esciencecenter.octopus.engine.util.OpenOptions;
import nl.esciencecenter.octopus.files.Copy;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.CopyStatus;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.InvalidOpenOptionsException;
import nl.esciencecenter.octopus.files.NoSuchPathException;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.PathAlreadyExistsException;
import nl.esciencecenter.octopus.files.PathAttributesPair;
import nl.esciencecenter.octopus.files.PosixFilePermission;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.files.InvalidCopyOptionsException;
import nl.esciencecenter.octopus.util.Utils;

/**
 * LocalFiles implements an Octopus <code>Files</code> adaptor for local file operations.
 * 
 * @see nl.esciencecenter.octopus.files.Files
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class LocalFiles implements nl.esciencecenter.octopus.files.Files {

    /** The parent adaptor */
    private final LocalAdaptor localAdaptor;

    /** The copy engine */
    private final CopyEngine copyEngine;

    /** The next ID for a FileSystem */
    private static int fsID = 0;

    private static synchronized int getNextFsID() {
        return fsID++;
    }

    public LocalFiles(LocalAdaptor localAdaptor, CopyEngine copyEngine) throws OctopusException {
        this.localAdaptor = localAdaptor;
        this.copyEngine = copyEngine;
    }

    /**
     * Check if a parent directory exists and throw an exception if this is not the case.  
     *  
     * @param path the path of which the parent must be checked. 
     *
     * @throws OctopusException
     *          If the parent does not exist. 
     *  
     */
    private void checkParent(Path path) throws OctopusException {
        
        RelativePath parentName = path.getRelativePath().getParent();
        
        if (parentName == null) { 
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Parent directory does not exist!");
        }
        
        Path parent = newPath(path.getFileSystem(), parentName);
            
        if (!exists(parent)) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Parent directory " + parent + " does not exist!");
        }
    }
    
    /**
     * Move or rename an existing source path to a non-existing target path.
     * 
     * The parent of the target path (e.g. <code>target.getParent</code>) must exist.
     * 
     * If the source is a link, the link itself will be moved, not the path to which it refers. If the source is a directory, it
     * will be renamed to the target. This implies that a moving a directory between physical locations may fail.
     * 
     * @param source
     *            the existing source path.
     * @param target
     *            the non existing target path.
     * @return the target path.
     * 
     * @throws NoSuchPathException
     *             If the source file does not exist or the target parent directory does not exist.
     * @throws PathAlreadyExistsException
     *             If the target file already exists.
     * @throws OctopusException
     *             If the move failed.
     */
    @Override
    public void move(Path source, Path target) throws OctopusException {

        if (!exists(source)) {
            throw new NoSuchPathException(LocalAdaptor.ADAPTOR_NAME, "Source " + source + " does not exist!");
        }

        RelativePath sourceName = source.getRelativePath().normalize();
        RelativePath targetName = target.getRelativePath().normalize();
        
        if (sourceName.equals(targetName)) {
            return;
        }

        if (exists(target)) {
            throw new PathAlreadyExistsException(LocalAdaptor.ADAPTOR_NAME, "Target " + target + " already exists!");
        }

        checkParent(target);

        LocalUtils.move(source, target);
    }

    @Override
    public Path readSymbolicLink(Path link) throws OctopusException {

        try {
            java.nio.file.Path path = LocalUtils.javaPath(link);
            java.nio.file.Path target = Files.readSymbolicLink(path);

            RelativePath parent = link.getRelativePath().getParent();

            if (parent == null || target.isAbsolute()) {
                return new PathImplementation(link.getFileSystem(), new RelativePath(target.toString()));
            }

            return newPath(link.getFileSystem(), parent.resolve(new RelativePath(target.toString())));
        } catch (IOException e) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Failed to read symbolic link.", e);
        }
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter filter)
            throws OctopusException {

        FileAttributes att = getAttributes(dir);

        if (!att.isDirectory()) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "File is not a directory.");
        }

        if (filter == null) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Filter is null.");
        }

        return new LocalDirectoryStream(dir, filter);
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir, DirectoryStream.Filter filter)
            throws OctopusException {

        FileAttributes att = getAttributes(dir);

        if (!att.isDirectory()) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "File is not a directory.");
        }

        if (filter == null) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Filter is null.");
        }

        return new LocalDirectoryAttributeStream(this, new LocalDirectoryStream(dir, filter));
    }

    @Override
    public InputStream newInputStream(Path path) throws OctopusException {

        if (!exists(path)) {
            throw new NoSuchPathException(LocalAdaptor.ADAPTOR_NAME, "File " + path + " does not exist!");
        }

        FileAttributes att = getAttributes(path);

        if (att.isDirectory()) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Path " + path + " is a directory!");
        }

        return LocalUtils.newInputStream(path);
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws OctopusException {

        OpenOptions tmp = OpenOptions.processOptions(LocalAdaptor.ADAPTOR_NAME, options);

        if (tmp.getReadMode() != null) {
            throw new InvalidOpenOptionsException(LocalAdaptor.ADAPTOR_NAME, "Disallowed open option: READ");
        }

        if (tmp.getAppendMode() == null) {
            throw new InvalidOpenOptionsException(LocalAdaptor.ADAPTOR_NAME, "No append mode provided!");
        }

        if (tmp.getWriteMode() == null) {
            tmp.setWriteMode(OpenOption.WRITE);
        }

        if (tmp.getOpenMode() == OpenOption.CREATE) {
            if (exists(path)) {
                throw new PathAlreadyExistsException(LocalAdaptor.ADAPTOR_NAME, "File already exists: " + path);
            }
        } else if (tmp.getOpenMode() == OpenOption.OPEN) {
            if (!exists(path)) {
                throw new NoSuchPathException(LocalAdaptor.ADAPTOR_NAME, "File does not exist: " + path);
            }
        }

        try {
            return Files.newOutputStream(LocalUtils.javaPath(path), LocalUtils.javaOpenOptions(options));
        } catch (IOException e) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Failed to create OutputStream.", e);
        }
    }

    @Override
    public FileAttributes getAttributes(Path path) throws OctopusException {
        return new LocalFileAttributes(path);
    }

    @Override
    public boolean exists(Path path) throws OctopusException {
        return Files.exists(LocalUtils.javaPath(path));
    }

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws OctopusException {

        if (!exists(path)) {
            throw new NoSuchPathException(LocalAdaptor.ADAPTOR_NAME, "File " + path + " does not exist!");
        }

        if (permissions == null) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Permissions is null!");
        }

        LocalUtils.setPosixFilePermissions(path, permissions);
    }

    @Override
    public FileSystem newFileSystem(String scheme, String location, Credential credential, Map<String, String> properties) 
            throws OctopusException {

        localAdaptor.checkLocation(location);
        localAdaptor.checkCredential(credential);

        OctopusProperties p = new OctopusProperties(localAdaptor.getSupportedProperties(Component.FILESYSTEM), properties);

        String root = Utils.getLocalRoot(location);
        RelativePath relativePath = Utils.getRelativePath(location, root);
        
        return new FileSystemImplementation(LocalAdaptor.ADAPTOR_NAME, "localfs-" + getNextFsID(), scheme, root,  
                relativePath, credential, p);
    }

    @Override
    public Path newPath(FileSystem filesystem, RelativePath location) {
        return new PathImplementation(filesystem, location);
    }

    @Override
    public void close(FileSystem filesystem) throws OctopusException {
        // ignored!
    }

    @Override
    public boolean isOpen(FileSystem filesystem) throws OctopusException {
        return true;
    }

    @Override
    public void createDirectories(Path dir) throws OctopusException {

        if (exists(dir)) {
            throw new PathAlreadyExistsException(LocalAdaptor.ADAPTOR_NAME, "Directory " + dir + " already exists!");
        }

        Iterator<RelativePath> itt = dir.getRelativePath().iterator();

        while (itt.hasNext()) {
            Path tmp = newPath(dir.getFileSystem(), itt.next());
            
            if (!exists(tmp)) {
                createDirectory(tmp);
            }
        }
    }

    @Override
    public void createDirectory(Path dir) throws OctopusException {

        if (exists(dir)) {
            throw new PathAlreadyExistsException(LocalAdaptor.ADAPTOR_NAME, "Directory " + dir + " already exists!");
        }

        checkParent(dir);

        try {
            java.nio.file.Files.createDirectory(LocalUtils.javaPath(dir));
        } catch (IOException e) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Failed to create directory " + dir, e);
        }
    }

    @Override
    public void createFile(Path path) throws OctopusException {

        if (exists(path)) {
            throw new PathAlreadyExistsException(LocalAdaptor.ADAPTOR_NAME, "File " + path + " already exists!");
        }

        checkParent(path);

        LocalUtils.createFile(path);
    }

    @Override
    public void delete(Path path) throws OctopusException {
        LocalUtils.delete(path);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir) throws OctopusException {
        return newDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER);
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir) throws OctopusException {
        return newAttributesDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER);
    }
       
    @Override
    public Copy copy(Path source, Path target, CopyOption... options) throws OctopusException,
            InvalidCopyOptionsException {

        CopyInfo info = CopyInfo.createCopyInfo(LocalAdaptor.ADAPTOR_NAME, copyEngine.getNextID("LOCAL_COPY_"), source,
                target, options);
         
        copyEngine.copy(info);

        if (info.isAsync()) {
            return info.getCopy();
        } else {

            Exception e = info.getException();

            if (e != null) {
                throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Copy failed!", e);
            }

            return null;
        }
    }

    @Override
    public CopyStatus getCopyStatus(Copy copy) throws OctopusException {
        return copyEngine.getStatus(copy);
    }

    @Override
    public CopyStatus cancelCopy(Copy copy) throws OctopusException {
        return copyEngine.cancel(copy);
    }
}
