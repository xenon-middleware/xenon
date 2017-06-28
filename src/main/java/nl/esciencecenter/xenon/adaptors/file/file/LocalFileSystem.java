package nl.esciencecenter.xenon.adaptors.file.file;

import static nl.esciencecenter.xenon.adaptors.file.file.LocalFileAdaptor.ADAPTOR_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.file.OpenOptions;
import nl.esciencecenter.xenon.adaptors.shared.local.LocalUtil;
import nl.esciencecenter.xenon.files.CopyDescription;
import nl.esciencecenter.xenon.files.CopyHandle;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.InvalidOptionsException;
import nl.esciencecenter.xenon.files.InvalidPathException;
import nl.esciencecenter.xenon.files.NoSuchPathException;
import nl.esciencecenter.xenon.files.OpenOption;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PathAlreadyExistsException;
import nl.esciencecenter.xenon.files.PathAttributesPair;
import nl.esciencecenter.xenon.files.PosixFilePermission;

public class LocalFileSystem extends FileSystem {

	protected LocalFileSystem(String uniqueID, String location, Path entryPath, XenonProperties properties) {
		super(uniqueID, ADAPTOR_NAME, location, entryPath, properties);
	}
	
	/**
     * Check if a parent directory exists and throw an exception if this is not the case.  
     *  
     * @param path the path of which the parent must be checked. 
     *
     * @throws XenonException
     *          If the parent does not exist. 
     *  
     */
    private void checkParent(Path path) throws XenonException {
        Path parent = path.getParent();
        
        if (parent == null) { 
            throw new InvalidPathException(ADAPTOR_NAME, "Parent directory does not exist!");
        }
            
        if (!exists(parent)) {
            throw new XenonException(ADAPTOR_NAME, "Parent directory " + parent + " does not exist!");
        }
    }
    
	@Override
	public void close() throws XenonException {
		// ignored
	}

	@Override
	public boolean isOpen() throws XenonException {
		return true;
	}

	@Override
	public void move(Path source, Path target) throws XenonException {
		if (!exists(source)) {
			throw new NoSuchPathException(ADAPTOR_NAME, "Source " + source + " does not exist!");
		}

		Path sourceName = source.normalize();
		Path targetName = target.normalize();

		if (sourceName.equals(targetName)) {
			return;
		}

		if (exists(target)) {
			throw new PathAlreadyExistsException(ADAPTOR_NAME, "Target " + target + " already exists!");
		}

		checkParent(target);

		LocalUtil.move(this, source, target);
	}

	@Override
	public void createDirectory(Path dir) throws XenonException {
		if (exists(dir)) {
			throw new PathAlreadyExistsException(ADAPTOR_NAME, "Directory " + dir + " already exists!");
		}

		checkParent(dir);

		try {
			java.nio.file.Files.createDirectory(LocalUtil.javaPath(this, dir));
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to create directory " + dir, e);
		}
	}
	
	@Override
	public void createFile(Path path) throws XenonException {

		if (exists(path)) {
			throw new PathAlreadyExistsException(ADAPTOR_NAME, "File " + path + " already exists!");
		}

		checkParent(path);

		LocalUtil.createFile(this, path);
	}
	 
	@Override
	public void delete(Path path) throws XenonException {
		LocalUtil.delete(this, path);
	}
	
	@Override
	public boolean exists(Path path) throws XenonException {
		return java.nio.file.Files.exists(LocalUtil.javaPath(this, path));
	}
	 
	@Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter filter) throws XenonException {
        FileAttributes att = getAttributes(dir);

        if (!att.isDirectory()) {
            throw new InvalidPathException(ADAPTOR_NAME, "File is not a directory.");
        }

        if (filter == null) {
            throw new XenonException(ADAPTOR_NAME, "Filter is null.");
        }

        return new LocalDirectoryStream(this, dir, filter);
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir, DirectoryStream.Filter filter)
            throws XenonException {

        FileAttributes att = getAttributes(dir);

        if (!att.isDirectory()) {
            throw new InvalidPathException(ADAPTOR_NAME, "File is not a directory.");
        }

        if (filter == null) {
            throw new XenonException(ADAPTOR_NAME, "Filter is null.");
        }

        return new LocalDirectoryAttributeStream(this, new LocalDirectoryStream(this, dir, filter));
    }
    
    @Override
    public InputStream newInputStream(Path path) throws XenonException {

        if (!exists(path)) {
            throw new NoSuchPathException(ADAPTOR_NAME, "File " + path + " does not exist!");
        }

        FileAttributes att = getAttributes(path);

        if (att.isDirectory()) {
            throw new InvalidPathException(ADAPTOR_NAME, "Path " + path + " is a directory!");
        }

        return LocalUtil.newInputStream(this, path);
    }
    
    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws XenonException {

        OpenOptions tmp = OpenOptions.processOptions(ADAPTOR_NAME, options);

        if (tmp.getReadMode() != null) {
            throw new InvalidOptionsException(ADAPTOR_NAME, "Disallowed open option: READ");
        }

        if (tmp.getAppendMode() == null) {
            throw new InvalidOptionsException(ADAPTOR_NAME, "No append mode provided!");
        }

        if (tmp.getWriteMode() == null) {
            tmp.setWriteMode(OpenOption.WRITE);
        }

        if (tmp.getOpenMode() == OpenOption.CREATE && exists(path)) {
            throw new PathAlreadyExistsException(ADAPTOR_NAME, "File already exists: " + path);
        } else if (tmp.getOpenMode() == OpenOption.OPEN && !exists(path)) {
            throw new NoSuchPathException(ADAPTOR_NAME, "File does not exist: " + path);
        }

        try {
            return java.nio.file.Files.newOutputStream(LocalUtil.javaPath(this, path), LocalUtil.javaOpenOptions(options));
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to create OutputStream.", e);
        }
    }
    
    @Override
    public FileAttributes getAttributes(Path path) throws XenonException {
        return new LocalFileAttributes(this, path);
    }
	

	@Override
	public Path readSymbolicLink(Path link) throws XenonException {
		try {
			java.nio.file.Path path = LocalUtil.javaPath(this, link);
			java.nio.file.Path target = java.nio.file.Files.readSymbolicLink(path);

			Path parent = link.getParent();

			if (parent == null || target.isAbsolute()) {
				return new Path(target.toString());
			}

			return parent.resolve(new Path(target.toString()));
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to read symbolic link.", e);
		}
	}

	@Override
	public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {

		if (!exists(path)) {
			throw new NoSuchPathException(ADAPTOR_NAME, "File " + path + " does not exist!");
		}

		if (permissions == null) {
			throw new XenonException(ADAPTOR_NAME, "Permissions is null!");
		}

		LocalUtil.setPosixFilePermissions(this, path, permissions);
	}

	@Override
	public CopyHandle copy(CopyDescription description) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

}
