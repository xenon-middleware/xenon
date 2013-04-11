package nl.esciencecenter.octopus.adaptors.local;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.files.FileSystemImplementation;
import nl.esciencecenter.octopus.engine.files.FilesEngine;
import nl.esciencecenter.octopus.engine.files.AbsolutePathImplementation;
import nl.esciencecenter.octopus.exceptions.DirectoryNotEmptyException;
import nl.esciencecenter.octopus.exceptions.FileAlreadyExistsException;
import nl.esciencecenter.octopus.exceptions.NoSuchFileException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.PathAttributesPair;
import nl.esciencecenter.octopus.files.PosixFilePermission;
import nl.esciencecenter.octopus.files.RelativePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalFiles implements nl.esciencecenter.octopus.files.Files {

    private static final Logger logger = LoggerFactory.getLogger(LocalFiles.class);

    private final OctopusEngine octopusEngine;
    private final LocalAdaptor localAdaptor;

    private static int fsID = 0;

    private static synchronized int getNextFsID() {
        return fsID++;
    }
    
    public LocalFiles(OctopusProperties properties, LocalAdaptor localAdaptor, OctopusEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
        this.localAdaptor = localAdaptor;

        if (logger.isDebugEnabled()) {
            Set<String> attributeViews = FileSystems.getDefault().supportedFileAttributeViews();

            logger.debug(Arrays.toString(attributeViews.toArray()));
        }
    }


    @Override
    public AbsolutePath copy(AbsolutePath source, AbsolutePath target, CopyOption... options) throws OctopusIOException {
        
        if (CopyOption.contains(options, CopyOption.REPLACE_EXISTING)) {
            if (exists(target) && isDirectory(target)
                    && newDirectoryStream(target, FilesEngine.ACCEPT_ALL_FILTER).iterator().hasNext()) {
                throw new DirectoryNotEmptyException(getClass().getName(), "cannot replace dir " + target + " as it is not empty");
            }
        } else if (exists(target)) {
            throw new FileAlreadyExistsException(getClass().getName(), "cannot copy to " + target + " as it already exists");
        }

        try {
            Files.copy(LocalUtils.javaPath(source), LocalUtils.javaPath(target), LocalUtils.javaCopyOptions(options));
        } catch (IOException e) {
            throw new OctopusIOException(getClass().getName(), "could not copy file", e);
        }

        //        if (CopyOption.contains(options, CopyOption.RECURSIVE) && isDirectory(source)) {
        //            for (Path child : newDirectoryStream(source, FilesEngine.ACCEPT_ALL_FILTER)) {
        //                copy(child, target.resolve(child.getFileName()), options);
        //            }
        //        }

        return target;
    }

    @Override
    public AbsolutePath move(AbsolutePath source, AbsolutePath target, CopyOption... options) throws OctopusIOException {
        
        FileSystem sourcefs = source.getFileSystem();
        FileSystem targetfs = target.getFileSystem();
 
        if (!sourcefs.equals(targetfs)) { 
            throw new OctopusIOException("Cannot move files between filesystems!", LocalAdaptor.ADAPTOR_NAME);
        }
        
        if (source.normalize().equals(target.normalize())) {
            return target;
        }

        if (exists(target) && isDirectory(target)
                && newDirectoryStream(target, FilesEngine.ACCEPT_ALL_FILTER).iterator().hasNext()
                && !CopyOption.contains(options, CopyOption.REPLACE_EXISTING)) {
            throw new OctopusIOException("cannot move file, target already exists", null, null);
        }

        // FIXME: test if this also works across different partitions/drives in
        // a single machine (different FileStores)
        try {
            Files.move(LocalUtils.javaPath(source), LocalUtils.javaPath(target), LocalUtils.javaCopyOptions(options));
        } catch (IOException e) {
            throw new OctopusIOException(getClass().getName(), "could not move files", e);
        }

        return target;
    }

//    @Override
//    public Path createDirectories(Path dir, Set<PosixFilePermission> permissions) throws OctopusIOException {
//        if (exists(dir) && !isDirectory(dir)) {
//            throw new FileAlreadyExistsException(getClass().getName(), "Cannot create directory, as it already exists (but is not a directory).");
//        }
//
//        try {
//            Files.createDirectories(LocalUtils.javaPath(dir), LocalUtils.javaPermissionAttribute(permissions));
//        } catch (IOException e) {
//            throw new OctopusIOException(getClass().getName(), "could not create directories", e);
//        }
//
//        return dir;
//    }
//
//    @Override
//    public Path createDirectory(Path dir, Set<PosixFilePermission> permissions) throws OctopusIOException {
//        if (exists(dir)) {
//            throw new FileAlreadyExistsException(getClass().getName(), "Cannot create directory, as it already exists.");
//        }
//
//        try {
//            Files.createDirectories(LocalUtils.javaPath(dir), LocalUtils.javaPermissionAttribute(permissions));
//        } catch (IOException e) {
//            throw new OctopusIOException(getClass().getName(), "could not create directory", e);
//        }
//
//        return dir;
//    }
//
//    @Override
//    public Path createFile(Path path, Set<PosixFilePermission> permissions) throws OctopusIOException {
//        if (exists(path)) {
//            throw new FileAlreadyExistsException(getClass().getName(), "Cannot create file, as it already exists");
//        }
//
//        try {
//            Files.createFile(LocalUtils.javaPath(path), LocalUtils.javaPermissionAttribute(permissions));
//        } catch (IOException e) {
//            throw new OctopusIOException(getClass().getName(), "could not create file", e);
//        }
//
//        return path;
//    }

    @Override
    public AbsolutePath createSymbolicLink(AbsolutePath link, AbsolutePath target) throws OctopusIOException {
        if (exists(link)) {
            throw new FileAlreadyExistsException(getClass().getName(), "Cannot create link, as a file with this name already exists");
        }

        try {
            Files.createSymbolicLink(LocalUtils.javaPath(link), LocalUtils.javaPath(target));
        } catch (IOException e) {
            throw new OctopusIOException(getClass().getName(), "could not create symbolic link", e);
        }

        return link;
    }

    @Override
    public AbsolutePath readSymbolicLink(AbsolutePath link) throws OctopusIOException {
        try {
            java.nio.file.Path target = Files.readSymbolicLink(LocalUtils.javaPath(link));
            
            // FIXME: No clue if this is correct!!
            return new AbsolutePathImplementation(link.getFileSystem(), new RelativePath(target.toString()));
        } catch (IOException e) {
            throw new OctopusIOException(getClass().getName(), "could not create symbolic link", e);
        }
    }

//    @Override
//    public void delete(Path path, DeleteOption... options) throws OctopusIOException {
//        if (!exists(path)) {
//            throw new NoSuchFileException(getClass().getName(), "cannot delete file, as it does not exist");
//        }
//
//        // recursion step
//        if (isDirectory(path) && DeleteOption.contains(options, DeleteOption.RECURSIVE)) {
//            for (Path child : newDirectoryStream(path, FilesEngine.ACCEPT_ALL_FILTER)) {
//                delete(child, options);
//            }
//        }
//
//        try {
//            Files.delete(LocalUtils.javaPath(path));
//        } catch (IOException e) {
//            throw new OctopusIOException(getClass().getName(), "could not delete file", e);
//        }
//    }
//
//    @Override
//    public boolean deleteIfExists(Path path, DeleteOption... options) throws OctopusIOException {
//        if (isDirectory(path) && DeleteOption.contains(options, DeleteOption.RECURSIVE)) {
//            for (Path child : newDirectoryStream(path, FilesEngine.ACCEPT_ALL_FILTER)) {
//                delete(child, options);
//            }
//        }
//
//        try {
//            return Files.deleteIfExists(LocalUtils.javaPath(path));
//        } catch (IOException e) {
//            throw new OctopusIOException(getClass().getName(), "could not delete file", e);
//        }
//    }

    @Override
    public DirectoryStream<AbsolutePath> newDirectoryStream(AbsolutePath dir, DirectoryStream.Filter filter) throws OctopusIOException {
        if (!isDirectory(dir)) {
            throw new OctopusIOException(getClass().getName(), "Cannot create directorystream, file is not a directory");
        }

        return new LocalDirectoryStream(dir, filter);
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(AbsolutePath dir, DirectoryStream.Filter filter)
            throws OctopusIOException {
        if (!isDirectory(dir)) {
            throw new OctopusIOException(getClass().getName(), "Cannot create DirectoryAttributeStream, file is not a directory");
        }

        return new LocalDirectoryAttributeStream(this, dir, filter);
    }

    @Override
    public InputStream newInputStream(AbsolutePath path) throws OctopusIOException {
        try {
            return Files.newInputStream(LocalUtils.javaPath(path));
        } catch (IOException e) {
            throw new OctopusIOException(getClass().getName(), "Could not create input stream", e);
        }
    }

    @Override
    public OutputStream newOutputStream(AbsolutePath path, OpenOption... options) throws OctopusIOException {
        try {
            return Files.newOutputStream(LocalUtils.javaPath(path), LocalUtils.javaOpenOptions(options));
        } catch (IOException e) {
            throw new OctopusIOException(getClass().getName(), "Could not output stream", e);
        }

    }

    @Override
    public SeekableByteChannel newByteChannel(AbsolutePath path, Set<PosixFilePermission> permissions, OpenOption... options)
            throws OctopusIOException {
        try {
            return Files.newByteChannel(LocalUtils.javaPath(path), LocalUtils.javaOpenOptionsSet(options),
                    LocalUtils.javaPermissionAttribute(permissions));
        } catch (IOException e) {
            throw new OctopusIOException(getClass().getName(), "Could not create byte channel", e);
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
    public void setOwner(AbsolutePath path, String user, String group) throws OctopusIOException {
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
        } catch (IOException e) {
            throw new OctopusIOException(getClass().getName(), "Unable to set user and group", e);
        }

    }

    @Override
    public void setPosixFilePermissions(AbsolutePath path, Set<PosixFilePermission> permissions) throws OctopusIOException {
        try {
            PosixFileAttributeView view = Files.getFileAttributeView(LocalUtils.javaPath(path), PosixFileAttributeView.class);
            view.setPermissions(LocalUtils.javaPermissions(permissions));
        } catch (IOException e) {
            throw new OctopusIOException(getClass().getName(), "Unable to set permissions", e);
        }
    }

    @Override
    public void setFileTimes(AbsolutePath path, long lastModifiedTime, long lastAccessTime, long createTime) throws OctopusIOException {
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

        } catch (IOException e) {
            throw new OctopusIOException(getClass().getName(), "Unable to set file times", e);
        }
    }


    @Override
    public FileSystem newFileSystem(URI location, Credential credential, Properties properties) throws OctopusException,
            OctopusIOException {
    
        localAdaptor.checkURI(location);
      
        String path = location.getPath();

        if (path != null && !path.equals("/")) {
            throw new OctopusException("local", "Cannot create local file system with path!");
        }
        
        path = System.getProperty("user.home");
        
        if (!LocalUtils.exists(path)) { 
            throw new OctopusException("local", "Cannot create FileSystem with non-existing entry path (" + path + ")");
        }
            
        return new FileSystemImplementation(LocalAdaptor.ADAPTOR_NAME, "localfs-" + getNextFsID(), location, 
                new RelativePath(path), credential, new OctopusProperties(properties));
    }

    @Override
    public AbsolutePath newPath(FileSystem filesystem, RelativePath location) throws OctopusException, OctopusIOException {
        return new AbsolutePathImplementation(filesystem, location);
    }


    @Override
    public AbsolutePath newPath(FileSystem filesystem, RelativePath... locations) throws OctopusException, OctopusIOException {
        return new AbsolutePathImplementation(filesystem, locations);
    }

    @Override
    public void close(FileSystem filesystem) throws OctopusException, OctopusIOException {
        // TODO Auto-generated method stub
    }


    @Override
    public boolean isOpen(FileSystem filesystem) throws OctopusException, OctopusIOException {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public AbsolutePath createDirectories(AbsolutePath dir) throws OctopusIOException {
        
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
        
        try { 
            java.nio.file.Files.createFile(LocalUtils.javaPath(path));
        } catch (IOException e) { 
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Failed to create file " + path.getPath(), e);
        }
        
        return path;
    }

    @Override
    public void delete(AbsolutePath path) throws OctopusIOException {
        
        try { 
            java.nio.file.Files.delete(LocalUtils.javaPath(path));
        } catch (NoSuchFileException e1) {
            throw new NoSuchFileException(LocalAdaptor.ADAPTOR_NAME, "File " + path.getPath() + " does not exist!");
            
        } catch (DirectoryNotEmptyException e2) {
            // TODO: handle exception
            throw new DirectoryNotEmptyException(LocalAdaptor.ADAPTOR_NAME, "Directory " + path.getPath() + " not empty!");
            
        } catch (Exception e) { 
            throw new OctopusIOException(LocalAdaptor.ADAPTOR_NAME, "Failed to delete file " + path.getPath(), e);
        }
    }

    @Override
    public DirectoryStream<AbsolutePath> newDirectoryStream(AbsolutePath dir) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(AbsolutePath dir) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public SeekableByteChannel newByteChannel(AbsolutePath path, OpenOption... options) throws OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }
 
 }
