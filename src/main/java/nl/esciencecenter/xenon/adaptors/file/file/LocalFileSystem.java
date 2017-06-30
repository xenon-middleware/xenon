package nl.esciencecenter.xenon.adaptors.file.file;

import static nl.esciencecenter.xenon.adaptors.file.file.LocalFileAdaptor.ADAPTOR_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.shared.local.LocalUtil;
import nl.esciencecenter.xenon.files.CopyDescription;
import nl.esciencecenter.xenon.files.CopyHandle;
import nl.esciencecenter.xenon.files.CopyStatus;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PathAttributesPair;
import nl.esciencecenter.xenon.files.PosixFilePermission;

public class LocalFileSystem extends FileSystem {

	protected LocalFileSystem(String uniqueID, String location, Path entryPath, XenonProperties properties) {
		super(uniqueID, ADAPTOR_NAME, location, entryPath, properties);
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
		
		if (areSamePaths(source, target)) {
			return;
		}

		assertPathExists(source);
		assertPathNotExists(target);
		assertParentDirectoryExists(target);
		
		LocalUtil.move(this, source, target);
	}

	@Override
	public void createDirectory(Path dir) throws XenonException {
		
		assertPathNotExists(dir);
		assertParentDirectoryExists(dir);
		
		try {
			java.nio.file.Files.createDirectory(LocalUtil.javaPath(this, dir));
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to create directory " + dir, e);
		}
	}
	
	@Override
	public void createFile(Path path) throws XenonException {

		assertPathNotExists(path);
		assertParentDirectoryExists(path);

		LocalUtil.createFile(this, path);
	}
	
	@Override
	protected void deleteFile(Path path) throws XenonException {
		LocalUtil.delete(this, path);
	}
	
	@Override
	protected void deleteDirectory(Path path) throws XenonException {
		LocalUtil.delete(this, path);
	}
	
	@Override
	public boolean exists(Path path) throws XenonException {
		return java.nio.file.Files.exists(LocalUtil.javaPath(this, path));
	}
	
	@Override
    protected List<PathAttributesPair> listDirectory(Path dir) throws XenonException {
		return LocalUtil.listDirectory(this, dir);	
    }

    @Override
    public InputStream readFromFile(Path path) throws XenonException {
    	assertFileExists(path);
        return LocalUtil.newInputStream(this, path);
    }
    
    @Override
    public OutputStream writeToFile(Path path, long size) throws XenonException {
        try {
            return java.nio.file.Files.newOutputStream(LocalUtil.javaPath(this, path), 
            		StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
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
        try {
            return java.nio.file.Files.newOutputStream(LocalUtil.javaPath(this, path), 
            		StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to create OutputStream.", e);
        }
    }
    
    @Override
    public FileAttributes getAttributes(Path path) throws XenonException {
    	return LocalUtil.getLocalFileAttributes(this, path);
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
		
		if (permissions == null) {
			throw new XenonException(ADAPTOR_NAME, "Permissions is null!");
		}

		assertPathExists(path);

		LocalUtil.setPosixFilePermissions(this, path, permissions);
	}

	@Override
	public CopyHandle copy(CopyDescription description) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CopyStatus getStatus(CopyHandle copy) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CopyStatus cancel(CopyHandle copy) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CopyStatus waitUntilDone(CopyHandle copy, long timeout) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}
}
