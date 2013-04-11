package nl.esciencecenter.octopus.engine.files;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.util.Properties;
import java.util.Set;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.OctopusRuntimeException;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.DirectoryStream.Filter;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.PathAttributes;
import nl.esciencecenter.octopus.files.PosixFilePermission;
import nl.esciencecenter.octopus.files.RelativePath;

/**
 * Engine for File operations. Implements functionality using File operations, Octopus create functions, and Adaptors' Files
 * object.
 * 
 * @author Niels Drost
 * 
 */
public class FilesEngine implements Files {

    public static DirectoryStream.Filter ACCEPT_ALL_FILTER = new DirectoryStream.Filter() {
        public boolean accept(AbsolutePath file) {
            return true;
        }
    };

    private final OctopusEngine octopusEngine;

    public FilesEngine(OctopusEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
    }
    
//    private Adaptor getAdaptor(FileSystem filesystem) throws OctopusIOException {
//        
//        try { 
//            return octopusEngine.getAdaptor(filesystem.getAdaptorName());
//
//        } catch (OctopusException e) { 
//            throw new OctopusIOException("Failed to locate adaptor for FileSystem!",  
//                    filesystem.getAdaptorName());
//        }
//    }
    
//    private Adaptor getAdaptor(AbsolutePath path) throws OctopusIOException {
//        return getAdaptor(path.getFileSystem());
//    }

    private Files getFilesAdaptor(FileSystem filesystem) throws OctopusIOException {
        
        try { 
            Adaptor adaptor = octopusEngine.getAdaptor(filesystem.getAdaptorName());
            return adaptor.filesAdaptor();        
        } catch (OctopusException e) {
            // This is a case that should never occur, the adaptor was already created, it cannot dissapear suddenly.
            // Therefore, we make this a runtime exception.
            throw new OctopusRuntimeException(getClass().getName(), "could not find adaptor named " + 
                    filesystem.getAdaptorName(), e);
        }
    }

    
    private Files getFilesAdaptor(AbsolutePath path) throws OctopusIOException {
        
        FileSystem filesystem = path.getFileSystem();
        
        try { 
            Adaptor adaptor = octopusEngine.getAdaptor(filesystem.getAdaptorName());
            return adaptor.filesAdaptor();        
        } catch (OctopusException e) {
            // This is a case that should never occur, the adaptor was already created, it cannot dissapear suddenly.
            // Therefore, we make this a runtime exception.
            throw new OctopusRuntimeException(getClass().getName(), "could not find adaptor named " + 
                    path.getFileSystem().getAdaptorName(), e);
        }
    }

    @Override
    public FileSystem newFileSystem(URI location, Credential credential, Properties properties) throws OctopusException,
            OctopusIOException {
        Adaptor adaptor = octopusEngine.getAdaptorFor(location.getScheme());
        return adaptor.filesAdaptor().newFileSystem(location, credential, properties);
    }
    
    @Override
    public AbsolutePath newPath(FileSystem filesystem, RelativePath location) throws OctopusException, OctopusIOException {
        return getFilesAdaptor(filesystem).newPath(filesystem, location);
    }
    
    @Override
    public AbsolutePath newPath(FileSystem filesystem, RelativePath... locations) throws OctopusException, OctopusIOException {
        return getFilesAdaptor(filesystem).newPath(filesystem, locations);
    }

    @Override
    public void close(FileSystem filesystem) throws OctopusException, OctopusIOException {
        getFilesAdaptor(filesystem).close(filesystem);
    }

    @Override
    public boolean isOpen(FileSystem filesystem) throws OctopusException, OctopusIOException {
        return getFilesAdaptor(filesystem).isOpen(filesystem);
    }

    @Override
    public AbsolutePath copy(AbsolutePath source, AbsolutePath target, CopyOption... options) throws OctopusIOException {
        
        FileSystem sourcefs = source.getFileSystem();
        FileSystem targetfs = target.getFileSystem();
        
        if (sourcefs.getAdaptorName().equals(targetfs.getAdaptorName())) {
            return getFilesAdaptor(source).copy(source, target, options);
        } else if (sourcefs.getAdaptorName().equals("local")) {
            return getFilesAdaptor(target).copy(source, target, options);
        } else if (targetfs.getAdaptorName().equals("local")) {
            return getFilesAdaptor(source).copy(source, target, options);
        } else {
            throw new OctopusIOException("cannot do inter-scheme third party copy (yet)", null, null);
        }
    }

    @Override
    public AbsolutePath createDirectories(AbsolutePath dir) throws OctopusIOException {
        return getFilesAdaptor(dir).createDirectories(dir);
    }

    @Override
    public AbsolutePath createDirectory(AbsolutePath dir) throws OctopusIOException {
        return getFilesAdaptor(dir).createDirectory(dir);
    }

    @Override
    public AbsolutePath createFile(AbsolutePath path) throws OctopusIOException { 
        return getFilesAdaptor(path).createFile(path);
    }
    
    @Override
    public AbsolutePath createSymbolicLink(AbsolutePath link, AbsolutePath target) throws OctopusIOException {
        return getFilesAdaptor(link).createSymbolicLink(link, target);
    }

    @Override
    public void delete(AbsolutePath path) throws OctopusIOException {
        getFilesAdaptor(path).delete(path);
    }
   
    @Override
    public boolean exists(AbsolutePath path) throws OctopusIOException {
        return getFilesAdaptor(path).exists(path);
    }

    @Override
    public boolean isDirectory(AbsolutePath path) throws OctopusIOException {
        return getAttributes(path).isDirectory();
    }

    @Override
    public AbsolutePath move(AbsolutePath source, AbsolutePath target, CopyOption... options) throws OctopusIOException {
        
        FileSystem sourcefs = source.getFileSystem();
        FileSystem targetfs = target.getFileSystem();
        
        if (sourcefs.getAdaptorName().equals(targetfs.getAdaptorName())) {
            return getFilesAdaptor(source).move(source, target, options);
        } else if (sourcefs.getAdaptorName().equals("local")) {
            return getFilesAdaptor(target).move(source, target, options);
        } else if (targetfs.getAdaptorName().equals("local")) {
            return getFilesAdaptor(source).move(source, target, options);
        } else {
            throw new OctopusIOException("cannot do inter-scheme third party move (yet)", null, null);
        }
    }

    @Override
    public DirectoryStream<AbsolutePath> newDirectoryStream(AbsolutePath dir) throws OctopusIOException {
        return newDirectoryStream(dir, ACCEPT_ALL_FILTER);
    }

    @Override
    public DirectoryStream<PathAttributes> newAttributesDirectoryStream(AbsolutePath dir) throws OctopusIOException {
        return newAttributesDirectoryStream(dir, ACCEPT_ALL_FILTER);
    }
    
    @Override
    public DirectoryStream<AbsolutePath> newDirectoryStream(AbsolutePath dir, Filter filter) throws OctopusIOException {
        return getFilesAdaptor(dir).newDirectoryStream(dir, filter);
    }

    @Override
    public DirectoryStream<PathAttributes> newAttributesDirectoryStream(AbsolutePath dir, Filter filter) throws OctopusIOException {
        return getFilesAdaptor(dir).newAttributesDirectoryStream(dir, filter);
    }
    
    @Override
    public InputStream newInputStream(AbsolutePath path) throws OctopusIOException {
        return getFilesAdaptor(path).newInputStream(path);
    }
    
    @Override
    public OutputStream newOutputStream(AbsolutePath path, OpenOption... options) throws OctopusIOException {
        // TODO: set default options for all functions, not just this one
        if (options == null || options.length == 0) {
            options = new OpenOption[] { OpenOption.CREATE, OpenOption.TRUNCATE_EXISTING, OpenOption.WRITE };
        }

        return getFilesAdaptor(path).newOutputStream(path, options);
    }
    
    @Override
    public SeekableByteChannel newByteChannel(AbsolutePath path, OpenOption... options) throws OctopusIOException {
        return newByteChannel(path, null, options);
    }

    @Override
    public SeekableByteChannel newByteChannel(AbsolutePath path, Set<PosixFilePermission> permissions, OpenOption... options)
            throws OctopusIOException {
        return getFilesAdaptor(path).newByteChannel(path, permissions, options);
    }

    @Override
    public FileAttributes getAttributes(AbsolutePath path) throws OctopusIOException {
        return getFilesAdaptor(path).getAttributes(path);
    }

    @Override
    public AbsolutePath readSymbolicLink(AbsolutePath link) throws OctopusIOException {
        return getFilesAdaptor(link).readSymbolicLink(link);
    }

    @Override
    public void setOwner(AbsolutePath path, String owner, String group) throws OctopusIOException {
        getFilesAdaptor(path).setOwner(path, owner, group);
    }

    @Override
    public void setPosixFilePermissions(AbsolutePath path, Set<PosixFilePermission> permissions) throws OctopusIOException {
        getFilesAdaptor(path).setPosixFilePermissions(path, permissions);
    }

    @Override
    public void setFileTimes(AbsolutePath path, long lastModifiedTime, long lastAccessTime, long createTime) throws OctopusIOException {
        getFilesAdaptor(path).setFileTimes(path, lastModifiedTime, lastAccessTime, createTime);
    }
}
