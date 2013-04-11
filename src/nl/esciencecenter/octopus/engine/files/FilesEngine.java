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
    
    private Adaptor getAdaptor(FileSystem filesystem) throws OctopusIOException {
        
        try { 
            return octopusEngine.getAdaptor(filesystem.getAdaptorName());
        } catch (OctopusException e) { 
            throw new OctopusIOException("Failed to locate adaptor for FileSystem!",  
                    filesystem.getAdaptorName() + "/" + filesystem.getUniqueID());
        }
    }
    
    private Adaptor getAdaptor(AbsolutePath path) throws OctopusIOException {
        return getAdaptor(path.getFileSystem());
    }
    
//        
//        try {
//            return octopusEngine.getAdaptor(path.getAdaptorName());
//        } catch (OctopusException e) {
//            // This is a case that should never occur, the adaptor was already created, it cannot dissapear suddenly.
//            // Therefore, we make this a runtime exception.
//            throw new OctopusRuntimeException(getClass().getName(), "could not find adaptor named " + path.getAdaptorName(), e);
//        }
//    }


    @Override
    public FileSystem newFileSystem(URI location, Credential credential, Properties properties) throws OctopusException,
            OctopusIOException {
        Adaptor adaptor = octopusEngine.getAdaptorFor(location.getScheme());
        return adaptor.filesAdaptor().newFileSystem(location, credential, properties);
    }
    
    @Override
    public AbsolutePath newPath(FileSystem filesystem, String location) throws OctopusException, OctopusIOException {
        return getAdaptor(filesystem).filesAdaptor().newPath(filesystem, location);
    }

    @Override
    public void close(FileSystem filesystem) throws OctopusException, OctopusIOException {
        getAdaptor(filesystem).filesAdaptor().close(filesystem);
    }

    @Override
    public boolean isOpen(FileSystem filesystem) throws OctopusException, OctopusIOException {
        return getAdaptor(filesystem).filesAdaptor().isOpen(filesystem);
    }

    @Override
    public AbsolutePath copy(AbsolutePath source, AbsolutePath target, CopyOption... options) throws OctopusIOException {
        
        FileSystem sourcefs = source.getFileSystem();
        FileSystem targetfs = target.getFileSystem();
        
        if (sourcefs.getAdaptorName().equals(targetfs.getAdaptorName())) {
            return getAdaptor(source).filesAdaptor().copy(source, target, options);
        } else if (sourcefs.getAdaptorName().equals("Local")) {
            return getAdaptor(target).filesAdaptor().copy(source, target, options);
        } else if (targetfs.getAdaptorName().equals("Local")) {
            return getAdaptor(source).filesAdaptor().copy(source, target, options);
        } else {
            throw new OctopusIOException("cannot do inter-scheme third party copy (yet)", null, null);
        }
    }

    @Override
    public AbsolutePath createDirectories(AbsolutePath dir) throws OctopusIOException {
        return getAdaptor(dir).filesAdaptor().createDirectories(dir);
    }

    @Override
    public AbsolutePath createDirectory(AbsolutePath dir) throws OctopusIOException {
        return getAdaptor(dir).filesAdaptor().createDirectory(dir);
    }

    @Override
    public AbsolutePath createFile(AbsolutePath path) throws OctopusIOException { 
        return getAdaptor(path).filesAdaptor().createFile(path);
    }
    
    @Override
    public AbsolutePath createSymbolicLink(AbsolutePath link, AbsolutePath target) throws OctopusIOException {
        return getAdaptor(link).filesAdaptor().createSymbolicLink(link, target);
    }

    @Override
    public void delete(AbsolutePath path) throws OctopusIOException {
        getAdaptor(path).filesAdaptor().delete(path);
    }
   
    @Override
    public boolean exists(AbsolutePath path) throws OctopusIOException {
        return getAdaptor(path).filesAdaptor().exists(path);
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
            return getAdaptor(source).filesAdaptor().move(source, target, options);
        } else if (sourcefs.getAdaptorName().equals("Local")) {
            return getAdaptor(target).filesAdaptor().move(source, target, options);
        } else if (targetfs.getAdaptorName().equals("Local")) {
            return getAdaptor(source).filesAdaptor().move(source, target, options);
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
        return getAdaptor(dir).filesAdaptor().newDirectoryStream(dir, filter);
    }

    @Override
    public DirectoryStream<PathAttributes> newAttributesDirectoryStream(AbsolutePath dir, Filter filter) throws OctopusIOException {
        return getAdaptor(dir).filesAdaptor().newAttributesDirectoryStream(dir, filter);
    }
    
    @Override
    public InputStream newInputStream(AbsolutePath path) throws OctopusIOException {
        return getAdaptor(path).filesAdaptor().newInputStream(path);
    }
    
    @Override
    public OutputStream newOutputStream(AbsolutePath path, OpenOption... options) throws OctopusIOException {
        // TODO: set default options for all functions, not just this one
        if (options == null || options.length == 0) {
            options = new OpenOption[] { OpenOption.CREATE, OpenOption.TRUNCATE_EXISTING, OpenOption.WRITE };
        }

        return getAdaptor(path).filesAdaptor().newOutputStream(path, options);
    }
    
    @Override
    public SeekableByteChannel newByteChannel(AbsolutePath path, OpenOption... options) throws OctopusIOException {
        return newByteChannel(path, null, options);
    }

    @Override
    public SeekableByteChannel newByteChannel(AbsolutePath path, Set<PosixFilePermission> permissions, OpenOption... options)
            throws OctopusIOException {
        return getAdaptor(path).filesAdaptor().newByteChannel(path, permissions, options);
    }

    @Override
    public FileAttributes getAttributes(AbsolutePath path) throws OctopusIOException {
        return getAdaptor(path).filesAdaptor().getAttributes(path);
    }

    @Override
    public AbsolutePath readSymbolicLink(AbsolutePath link) throws OctopusIOException {
        return getAdaptor(link).filesAdaptor().readSymbolicLink(link);
    }

    @Override
    public void setOwner(AbsolutePath path, String owner, String group) throws OctopusIOException {
        getAdaptor(path).filesAdaptor().setOwner(path, owner, group);
    }

    @Override
    public void setPosixFilePermissions(AbsolutePath path, Set<PosixFilePermission> permissions) throws OctopusIOException {
        getAdaptor(path).filesAdaptor().setPosixFilePermissions(path, permissions);
    }

    @Override
    public void setFileTimes(AbsolutePath path, long lastModifiedTime, long lastAccessTime, long createTime) throws OctopusIOException {
        getAdaptor(path).filesAdaptor().setFileTimes(path, lastModifiedTime, lastAccessTime, createTime);
    }
}
