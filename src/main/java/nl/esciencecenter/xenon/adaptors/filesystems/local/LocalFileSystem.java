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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.shared.local.LocalUtil;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.filesystems.PathAttributes;
import nl.esciencecenter.xenon.filesystems.PosixFilePermission;

public class LocalFileSystem extends FileSystem {

	protected LocalFileSystem(String uniqueID, String location, Path entryPath, XenonProperties properties) {
		super(uniqueID, ADAPTOR_NAME, location, entryPath, properties);
	}
	
	@Override
	public boolean isOpen() throws XenonException {
		return true;
	}

	@Override
	public void rename(Path source, Path target) throws XenonException {
		
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
	public void createSymbolicLink(Path link, Path path) throws XenonException {

		assertPathNotExists(link);
		assertParentDirectoryExists(link);

		LocalUtil.createSymbolicLink(this, link, path);
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
    protected List<PathAttributes> listDirectory(Path dir) throws XenonException {
		return LocalUtil.listDirectory(this, dir);	
    }

    @Override
    public InputStream readFromFile(Path path) throws XenonException {
    	assertFileExists(path);
        return LocalUtil.newInputStream(this, path);
    }
    
    @Override
    public OutputStream writeToFile(Path path, long size) throws XenonException {
		assertPathIsNotDirectory(path);
        try {
            return java.nio.file.Files.newOutputStream(LocalUtil.javaPath(this, path), 
            		StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
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
		assertFileExists(path);

        try {
            return java.nio.file.Files.newOutputStream(LocalUtil.javaPath(this, path), 
            		StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to create OutputStream.", e);
        }
    }
    
    @Override
    public PathAttributes getAttributes(Path path) throws XenonException {
		assertPathExists(path);
    	return LocalUtil.getLocalFileAttributes(this, path);
    }

	@Override
	public Path readSymbolicLink(Path link) throws XenonException {
		assertFileIsSymbolicLink(link);
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
			throw new IllegalArgumentException("Permissions is null!");
		}

		assertPathExists(path);

		LocalUtil.setPosixFilePermissions(this, path, permissions);
	}
}
