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
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.files.AbsolutePathImplementation;
import nl.esciencecenter.octopus.engine.files.CopyImplementation;
import nl.esciencecenter.octopus.engine.files.FileSystemImplementation;
import nl.esciencecenter.octopus.engine.files.FilesEngine;
import nl.esciencecenter.octopus.engine.util.CopyEngine;
import nl.esciencecenter.octopus.engine.util.CopyInfo;
import nl.esciencecenter.octopus.engine.util.OpenOptions;
import nl.esciencecenter.octopus.exceptions.FileAlreadyExistsException;
import nl.esciencecenter.octopus.exceptions.InvalidOpenOptionsException;
import nl.esciencecenter.octopus.exceptions.NoSuchFileException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.UnsupportedOperationException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.Copy;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.CopyStatus;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.PathAttributesPair;
import nl.esciencecenter.octopus.files.PosixFilePermission;
import nl.esciencecenter.octopus.files.RelativePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /** A logger for this class */
    private static final Logger logger = LoggerFactory.getLogger(LocalFiles.class);

    /** The parent adaptor */
    private final LocalAdaptor localAdaptor;

    /** The octopus engine */
    private final OctopusEngine octopusEngine;
    
    /** The next ID for a FileSystem */
    private static int fsID = 0;

    private static synchronized int getNextFsID() {
        return fsID++;
    }
    
//    class OpenOptions { 
//
//        public OpenOption openMode;
//        public OpenOption appendMode;
//        public OpenOption readMode;
//        public OpenOption writeMode;
//        
//        public OpenOption getOpenMode() {
//            return openMode;
//        }
//
//        public void setOpenMode(OpenOption openMode) throws InvalidOpenOptionsException {
//                        
//            if (this.openMode != null && openMode != this.openMode) { 
//                throw new InvalidOpenOptionsException(LocalAdaptor.ADAPTOR_NAME, "Conflicting open options: " + openMode 
//                        + " and " + this.openMode);
//            }
//            
//            this.openMode = openMode;
//        }
//        
//        public OpenOption getAppendMode() {
//            return appendMode;
//        }
//        
//        public void setAppendMode(OpenOption appendMode) throws InvalidOpenOptionsException {
//            
//            if (this.appendMode != null && appendMode != this.appendMode) { 
//                throw new InvalidOpenOptionsException(LocalAdaptor.ADAPTOR_NAME, "Conflicting append options: " + appendMode 
//                        + " and " + this.appendMode);
//            }
//            
//            
//            this.appendMode = appendMode;
//        }
//        
//        public OpenOption getReadMode() {
//            return readMode;
//        }
//        
//        public void setReadMode(OpenOption readMode) {
//            this.readMode = readMode;
//        }
//        
//        public OpenOption getWriteMode() {
//            return writeMode;
//        }
//        
//        public void setWriteMode(OpenOption writeMode) {
//            this.writeMode = writeMode;
//        }
//    }

    private final FileSystem cwd;
    private final FileSystem home;
    
    public LocalFiles(OctopusProperties properties, LocalAdaptor localAdaptor, OctopusEngine octopusEngine) throws OctopusException {
        this.localAdaptor = localAdaptor;
        this.octopusEngine = octopusEngine;
        
        if (logger.isDebugEnabled()) {
            Set<String> attributeViews = FileSystems.getDefault().supportedFileAttributeViews();

            logger.debug(Arrays.toString(attributeViews.toArray()));
        }
        
        cwd = new FileSystemImplementation(LocalAdaptor.ADAPTOR_NAME, "localfs-" + getNextFsID(), LocalUtils.getLocalFileURI(), 
                new RelativePath(LocalUtils.getCWD()), null, null);
        
        home = new FileSystemImplementation(LocalAdaptor.ADAPTOR_NAME, "localfs-" + getNextFsID(), LocalUtils.getLocalFileURI(), 
                new RelativePath(LocalUtils.getHome()), null, null);
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
     * @throws NoSuchFileException
     *             If the source file does not exist or the target parent directory does not exist.
     * @throws FileAlreadyExistsException
     *             If the target file already exists.
     * @throws OctopusIOException
     *             If the move failed.
     */
    @Override
    public AbsolutePath move(AbsolutePath source, AbsolutePath target) throws OctopusIOException {

        if (!exists(source)) {
            throw new NoSuchFileException(LocalAdaptor.ADAPTOR_NAME, "Source " + source.getPath() + " does not exist!");
        }

        if (source.normalize().equals(target.normalize())) {
            return target;
        }
        
        if (exists(target)) {
            throw new FileAlreadyExistsException(LocalAdaptor.ADAPTOR_NAME, "Target " + target.getPath() + " already exists!");
        }

        if (!exists(target.getParent())) {
            throw new NoSuchFileException(LocalAdaptor.ADAPTOR_NAME, "Target directory " + target.getParent().getPath()
                    + " does not exist!");
        }

        LocalUtils.move(source, target);
    
        return target;
    }

    @Override
    public AbsolutePath readSymbolicLink(AbsolutePath link) throws OctopusIOException {

        try {
              java.nio.file.Path path = LocalUtils.javaPath(link);
              java.nio.file.Path target = Files.readSymbolicLink(path);
              
//            This works, but throws an exception if the target does not exist!              
//            java.nio.file.Path tmp = LocalUtils.javaPath(link);
//            java.nio.file.Path target = tmp.toRealPath();

              AbsolutePath parent = link.getParent();
              
              if (parent == null || target.isAbsolute()) {
                  return new AbsolutePathImplementation(link.getFileSystem(), new RelativePath(target.toString()));
              }
              
              return parent.resolve(new RelativePath(target.toString()));              
//            System.err.println("FOLLOW link " + tmp.toString() + " realpath " + target.toString() + " readsymbolic " + target2.toString());
            
            // FIXME: No clue if this is correct!!
//            return new AbsolutePathImplementation(link.getFileSystem(), new RelativePath(target.toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Failed to read symbolic link.", e);
        }
    }

    @Override
    public DirectoryStream<AbsolutePath> newDirectoryStream(AbsolutePath dir, DirectoryStream.Filter filter)
            throws OctopusIOException {

        if (!isDirectory(dir)) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "File is not a directory.");
        }

        if (filter == null) { 
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Filter is null.");
        }
        
        return new LocalDirectoryStream(dir, filter);
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(AbsolutePath dir, DirectoryStream.Filter filter)
            throws OctopusIOException {
        
        if (!isDirectory(dir)) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "File is not a directory.");
        }

        if (filter == null) { 
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Filter is null.");
        }
        
        return new LocalDirectoryAttributeStream(this, new LocalDirectoryStream(dir, filter));
    }

    @Override
    public InputStream newInputStream(AbsolutePath path) throws OctopusIOException {

        if (!exists(path)) {
            throw new NoSuchFileException(LocalAdaptor.ADAPTOR_NAME, "File " + path.getPath() + " does not exist!");
        }

        if (isDirectory(path)) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Path " + path.getPath() + " is a directory!");
        }
        
        return LocalUtils.newInputStream(path);
    }
    
//    private OpenOptions processOptions(OpenOption... options) throws InvalidOpenOptionsException { 
//        
//        if (options == null || options.length == 0) { 
//            throw new InvalidOpenOptionsException(LocalAdaptor.ADAPTOR_NAME, "Missing open options!");
//        }
//        
//        OpenOptions result = new OpenOptions();
//        
//        for (OpenOption opt : options) {             
//            switch (opt) { 
//            case CREATE:
//            case OPEN:
//            case OPEN_OR_CREATE:
//                result.setOpenMode(opt);
//                break;
//                
//            case APPEND:
//            case TRUNCATE:
//                result.setAppendMode(opt);
//                break;
//                
//            case WRITE:
//                result.setWriteMode(opt);
//                break;     
//            case READ:
//                result.setReadMode(opt);
//                break;
//            }
//        }
//        
//        if (result.getOpenMode() == null) { 
//            throw new InvalidOpenOptionsException(LocalAdaptor.ADAPTOR_NAME, "No open mode provided!");
//        }
//        
//        return result;
//    }
    
    
    @Override
    public OutputStream newOutputStream(AbsolutePath path, OpenOption... options) throws OctopusIOException {

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
                throw new FileAlreadyExistsException(LocalAdaptor.ADAPTOR_NAME, "File already exists: " + path.getPath());
            }
        } else if (tmp.getOpenMode() == OpenOption.OPEN) { 
            if (!exists(path)) { 
                throw new NoSuchFileException(LocalAdaptor.ADAPTOR_NAME, "File does not exist: " + path.getPath());
            }
        }
        
        try {
            return Files.newOutputStream(LocalUtils.javaPath(path), LocalUtils.javaOpenOptions(options));
        } catch (IOException e) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Failed to create OutputStream.", e);
        }
    }

    @Override
    public FileAttributes getAttributes(AbsolutePath path) throws OctopusIOException {
        return new LocalFileAttributes(path);
    }

    @Override
    public boolean exists(AbsolutePath path) throws OctopusIOException {
        return Files.exists(LocalUtils.javaPath(path));
    }

    @Override
    public boolean isDirectory(AbsolutePath path) throws OctopusIOException {
        return Files.isDirectory(LocalUtils.javaPath(path));
    }

    @Override
    public void setPosixFilePermissions(AbsolutePath path, Set<PosixFilePermission> permissions) throws OctopusIOException {

        if (!exists(path)) { 
            throw new NoSuchFileException(LocalAdaptor.ADAPTOR_NAME, "File " + path.getPath() + " does not exist!");
        }

        if (permissions == null) { 
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Permissions is null!");
        }

        LocalUtils.setPosixFilePermissions(path, permissions);
    }

    @Override
    public FileSystem newFileSystem(URI location, Credential credential, Properties properties) throws OctopusException,
            OctopusIOException {

        localAdaptor.checkURI(location);

        String path = location.getPath();
        
        if (path != null && !path.equals("/")) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Cannot create local file system with path!");
        }

        return new FileSystemImplementation(LocalAdaptor.ADAPTOR_NAME, "localfs-" + getNextFsID(), location, 
                new RelativePath(LocalUtils.getHome()), credential, new OctopusProperties(properties));
    }

    @Override
    public AbsolutePath newPath(FileSystem filesystem, RelativePath location) throws OctopusException, OctopusIOException {
        return new AbsolutePathImplementation(filesystem, location);
    }

    @Override
    public void close(FileSystem filesystem) throws OctopusException, OctopusIOException {
        // ignored!
    }

    @Override
    public boolean isOpen(FileSystem filesystem) throws OctopusException, OctopusIOException {
        return true;
    }

    @Override
    public AbsolutePath createDirectories(AbsolutePath dir) throws OctopusIOException {

        if (exists(dir)) {
            throw new FileAlreadyExistsException(LocalAdaptor.ADAPTOR_NAME, "Directory " + dir.getPath() + " already exists!");
        }
        
        Iterator<AbsolutePath> itt = dir.iterator();

        while (itt.hasNext()) {
            AbsolutePath path = itt.next();

            if (!exists(path)) {
                createDirectory(path);
            }
        }

        return dir;
    }

    @Override
    public AbsolutePath createDirectory(AbsolutePath dir) throws OctopusIOException {

        if (exists(dir)) {
            throw new FileAlreadyExistsException(LocalAdaptor.ADAPTOR_NAME, "Directory " + dir.getPath() + " already exists!");
        }

        if (!exists(dir.getParent())) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Parent directory " + dir.getParent() + " does not exist!");
        }

        try {
            java.nio.file.Files.createDirectory(LocalUtils.javaPath(dir));
        } catch (IOException e) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Failed to create directory " + dir.getPath(), e);
        }

        return dir;
    }

    @Override
    public AbsolutePath createFile(AbsolutePath path) throws OctopusIOException {

        if (exists(path)) {
            throw new FileAlreadyExistsException(LocalAdaptor.ADAPTOR_NAME, "File " + path.getPath() + " already exists!");
        }

        if (!exists(path.getParent())) {
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Parent directory " + path.getParent() + " does not exist!");
        }

        LocalUtils.createFile(path);
        
        return path;
    }

    @Override
    public void delete(AbsolutePath path) throws OctopusIOException {
        LocalUtils.delete(path);
    }

    @Override
    public DirectoryStream<AbsolutePath> newDirectoryStream(AbsolutePath dir) throws OctopusIOException {
        return newDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER);
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(AbsolutePath dir) throws OctopusIOException {
        return newAttributesDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER);
    }
    
    @Override
    public SeekableByteChannel newByteChannel(AbsolutePath path, OpenOption... options) throws OctopusIOException {

        OpenOptions tmp = OpenOptions.processOptions(LocalAdaptor.ADAPTOR_NAME, options);

        if (tmp.getReadMode() != null && tmp.getAppendMode() == OpenOption.APPEND) { 
            throw new InvalidOpenOptionsException(LocalAdaptor.ADAPTOR_NAME, "Cannot set append mode when reading a file!");
        }
        
        if (tmp.getWriteMode() == null) { 
            if (tmp.getReadMode() == null) { 
                throw new InvalidOpenOptionsException(LocalAdaptor.ADAPTOR_NAME, "Must set either READ or WRITE, or both!");        
            } 
        } else { 
            if (tmp.getAppendMode() == null) {
                throw new InvalidOpenOptionsException(LocalAdaptor.ADAPTOR_NAME, "Must set append mode when writing a file!");
            }
        }
        
        if (tmp.getOpenMode() == OpenOption.CREATE) { 
            if (exists(path)) { 
                throw new FileAlreadyExistsException(LocalAdaptor.ADAPTOR_NAME, "File already exists: " + path.getPath());
            }
        } else if (tmp.getOpenMode() == OpenOption.OPEN) { 
            if (!exists(path)) { 
                throw new NoSuchFileException(LocalAdaptor.ADAPTOR_NAME, "File does not exist: " + path.getPath());
            }
        }
 
        return LocalUtils.newByteChannel(path, options);
    }

    @Override
    public FileSystem getLocalCWDFileSystem() throws OctopusException {
        return cwd;
    }

    @Override
    public FileSystem getLocalHomeFileSystem() throws OctopusException {
        return home;
    }

    @Override
    public boolean isSymbolicLink(AbsolutePath path) throws OctopusIOException {
        try { 
            return Files.isSymbolicLink(LocalUtils.javaPath(path));
        } catch (Exception e) {
            // We should return false if the check fails. 
            return false;
        }
    }

    @Override
    public long size(AbsolutePath path) throws OctopusIOException {
        
        if (!exists(path)) {
            throw new NoSuchFileException(LocalAdaptor.ADAPTOR_NAME, "File " + path.toString() + " does not exist!");
        }
        
        if (isDirectory(path) || isSymbolicLink(path)) { 
            return 0;
        }
        
        return LocalUtils.size(path);
    }
    
    @Override
    public Copy copy(AbsolutePath source, AbsolutePath target, CopyOption... options) 
            throws UnsupportedOperationException, OctopusIOException {
        
        boolean async = false;
        boolean verify = false;
        
        CopyOption mode = null;
        
        for (CopyOption opt : options) {             
            switch (opt) { 
            case CREATE:
                if (mode != null && mode != opt) { 
                    throw new UnsupportedOperationException(LocalAdaptor.ADAPTOR_NAME, "Conflicting copy options: " + mode 
                            + " and CREATE");
                }
                
                mode = opt;
                break;                
            case REPLACE:
                if (mode != null && mode != opt) { 
                    throw new UnsupportedOperationException(LocalAdaptor.ADAPTOR_NAME, "Conflicting copy options: " + mode 
                            + " and REPLACE");
                }
                
                mode = opt;
                break;                
            case APPEND:
                if (mode != null && mode != opt) { 
                    throw new UnsupportedOperationException(LocalAdaptor.ADAPTOR_NAME, "Conflicting copy options: " + mode 
                            + " and APPEND");
                }
                
                mode = opt;
                break;                
            case RESUME:
                if (mode != null && mode != opt) { 
                    throw new UnsupportedOperationException(LocalAdaptor.ADAPTOR_NAME, "Conflicting copy options: " + mode 
                            + " and RESUME");
                }
                
                mode = opt;
                break;     
            case IGNORE:
                if (mode != null && mode != opt) { 
                    throw new UnsupportedOperationException(LocalAdaptor.ADAPTOR_NAME, "Conflicting copy options: " + mode 
                            + " and RESUME");
                }
                
                mode = opt;
                break;
            case VERIFY:
                verify = true;
                break;                
            case ASYNCHRONOUS:
                async = true;
                break;                
            }
        }
        
        if (mode == null) { 
            mode = CopyOption.CREATE;
        }
        
        if (verify && mode != CopyOption.RESUME) { 
            throw new UnsupportedOperationException(LocalAdaptor.ADAPTOR_NAME, "Conflicting copy options: " + mode 
                            + " and VERIFY");
        }

        CopyEngine ce = octopusEngine.getCopyEngine();
        CopyImplementation copy = new CopyImplementation(LocalAdaptor.ADAPTOR_NAME, ce.getNextID("LOCAL_COPY_"), source, target);
        CopyInfo info = new CopyInfo(copy, mode, verify);
        ce.copy(info, async);
        
        if (async) { 
            return copy;
        } else { 
        
            Exception e = info.getException();
            
            if (e != null) { 
                throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Copy failed!", e);
            }
            
            return null;
        }
        
    }

    @Override
    public CopyStatus getCopyStatus(Copy copy) throws OctopusException, OctopusIOException {
        return octopusEngine.getCopyEngine().getStatus(copy);
    }

    @Override
    public CopyStatus cancelCopy(Copy copy) throws OctopusException, OctopusIOException {
        return octopusEngine.getCopyEngine().cancel(copy);
    }
}
