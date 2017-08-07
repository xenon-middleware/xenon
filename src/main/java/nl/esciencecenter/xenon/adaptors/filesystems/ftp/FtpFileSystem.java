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
package nl.esciencecenter.xenon.adaptors.filesystems.ftp;

import static nl.esciencecenter.xenon.adaptors.filesystems.ftp.FtpFileAdaptor.ADAPTOR_NAME;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.esciencecenter.xenon.adaptors.NotConnectedException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.UnsupportedOperationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.PathAttributesImplementation;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.InvalidPathException;
import nl.esciencecenter.xenon.filesystems.NoSuchPathException;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.filesystems.PathAttributes;
import nl.esciencecenter.xenon.filesystems.PosixFilePermission;

public class FtpFileSystem extends FileSystem {

	private static final Logger LOGGER = LoggerFactory.getLogger(FtpFileSystem.class);
	
	private static final int[] PERMISSION_TYPES = { FTPFile.READ_PERMISSION, FTPFile.WRITE_PERMISSION, FTPFile.EXECUTE_PERMISSION };
	
	private static final int[] USER_TYPES = { FTPFile.USER_ACCESS, FTPFile.GROUP_ACCESS, FTPFile.WORLD_ACCESS };
	
	private final FTPClient ftpClient;
	private final Credential credential;
	private final FtpFileAdaptor adaptor;

	protected FtpFileSystem(String uniqueID, String name, String location, Path entryPath, 
			FTPClient ftpClient, Credential credential, FtpFileAdaptor adaptor, XenonProperties properties) {
		super(uniqueID, name, location, entryPath, properties);
		this.ftpClient = ftpClient;
		this.credential = credential;
		this.adaptor = adaptor;
	}
	
	@Override
	public void close() throws XenonException {
		LOGGER.debug("close fileSystem = {}", this);

		if (!isOpen()) {
			throw new NotConnectedException(ADAPTOR_NAME, "File system is already closed");
		}

		try {
			ftpClient.disconnect();
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Exception while disconnecting ftp file system.", e);
		}

		super.close();
		
		LOGGER.debug("close OK");
	}

	@Override
	public boolean isOpen() throws XenonException {
		return ftpClient.isConnected();
	}
	
	private HashSet<PosixFilePermission> getPermissions(FTPFile attributes) {
        HashSet<PosixFilePermission> permissions = new HashSet<>();
        for (int userType : USER_TYPES) {
            for (int permissionType : PERMISSION_TYPES) {
                if (attributes.hasPermission(userType, permissionType)) {
                    permissions.add(getPosixFilePermission(userType, permissionType));
                }
            }
        }
        return permissions;
    }

    private PosixFilePermission getPosixFilePermission(int userType, int permissionType) {
        PosixFilePermission permission = null;
        if (userType == FTPFile.USER_ACCESS) {
            if (permissionType == FTPFile.EXECUTE_PERMISSION) {
                permission = PosixFilePermission.OWNER_EXECUTE;
            }
            if (permissionType == FTPFile.WRITE_PERMISSION) {
                permission = PosixFilePermission.OWNER_WRITE;
            }
            if (permissionType == FTPFile.READ_PERMISSION) {
                permission = PosixFilePermission.OWNER_READ;
            }
        }
        if (userType == FTPFile.GROUP_ACCESS) {
            if (permissionType == FTPFile.EXECUTE_PERMISSION) {
                permission = PosixFilePermission.GROUP_EXECUTE;
            }
            if (permissionType == FTPFile.WRITE_PERMISSION) {
                permission = PosixFilePermission.GROUP_WRITE;
            }
            if (permissionType == FTPFile.READ_PERMISSION) {
                permission = PosixFilePermission.GROUP_READ;
            }
        }
        if (userType == FTPFile.WORLD_ACCESS) {
            if (permissionType == FTPFile.EXECUTE_PERMISSION) {
                permission = PosixFilePermission.OTHERS_EXECUTE;
            }
            if (permissionType == FTPFile.WRITE_PERMISSION) {
                permission = PosixFilePermission.OTHERS_WRITE;
            }
            if (permissionType == FTPFile.READ_PERMISSION) {
                permission = PosixFilePermission.OTHERS_READ;
            }
        }
        return permission;
    }

	private PathAttributes convertAttributes(Path path, FTPFile attributes)  { 

		PathAttributesImplementation result = new PathAttributesImplementation();
		
		result.setPath(path);
		result.setDirectory(attributes.isDirectory());
		result.setRegular(attributes.isFile());
		result.setOther(attributes.isUnknown());
		result.setSymbolicLink(attributes.isSymbolicLink());
		
		result.setLastModifiedTime(attributes.getTimestamp().getTimeInMillis());
		result.setCreationTime(attributes.getTimestamp().getTimeInMillis());
		result.setLastAccessTime(attributes.getTimestamp().getTimeInMillis());
		
		result.setSize(attributes.getSize());
		
		Set<PosixFilePermission> permission = getPermissions(attributes);
		
		result.setExecutable(permission.contains(PosixFilePermission.OWNER_EXECUTE));
		result.setReadable(permission.contains(PosixFilePermission.OWNER_READ));
		result.setWritable(permission.contains(PosixFilePermission.OWNER_WRITE));
		
		result.setPermissions(permission);
		
		result.setGroup(attributes.getGroup());
		result.setOwner(attributes.getUser());
		
		return result;
	}

	private void checkClientReply(FTPClient client, String message) throws XenonException {
		
		int replyCode = client.getReplyCode();
		String replyString = client.getReplyString();

		//System.out.println("REPLY " + replyCode + " " + replyString);
		
		if (replyCode >= 100 && replyCode < 300) { 
			return;
		}
		
		//String replyString = ftpClient.getReplyString();
		throw new XenonException(ADAPTOR_NAME, message, new IOException(replyString));
	}

	
	private void checkClientReply(String message) throws XenonException {
		checkClientReply(ftpClient, message);
	}
	
	@Override
	public void rename(Path source, Path target) throws XenonException {

		LOGGER.debug("move source = {} target = {}", source, target);

		assertIsOpen();

		assertPathExists(source);
	
		if (areSamePaths(source, target)) {
			return;
		}

		assertPathNotExists(target);
		assertParentDirectoryExists(target);
		
		try { 
			ftpClient.rename(source.toString(), target.toString());
		} catch (Exception e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to rename " + source.toString() + " to " + target.toString(), e);
		}
		
		checkClientReply("Failed to rename " + source.toString() + " to " + target.toString());
	}

	@Override
	public void createDirectory(Path path) throws XenonException {
		LOGGER.debug("createDirectory dir = {}", path);

		assertIsOpen();
		assertPathNotExists(path);
		assertParentDirectoryExists(path);
		
		try { 
			ftpClient.makeDirectory(path.toString());
		} catch (Exception e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to createDirectory " + path.toString(), e);
		}
		
		checkClientReply("Failed to create directory: " + path.toString());
	}

	@Override
	public void createFile(Path path) throws XenonException {
		LOGGER.debug("createFile path = {}", path);
		
		assertIsOpen();
		assertPathNotExists(path);
		assertParentDirectoryExists(path);
		
		try {
			ByteArrayInputStream dummy = new ByteArrayInputStream(new byte[0]);
			ftpClient.storeFile(path.toString(), dummy);
		} catch (Exception e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to createDirectory " + path.toString(), e);
		}

		checkClientReply("Failed to create file: " + path.toString());
	}

	@Override
	public void createSymbolicLink(Path link, Path path) throws XenonException {
		throw new UnsupportedOperationException(ADAPTOR_NAME, "Operation not supported");
	}
	
	@Override
	protected void deleteDirectory(Path path) throws XenonException {

	    assertIsOpen();

		try {
			ftpClient.removeDirectory(path.toString());
		} catch (Exception e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to createDirectory " + path.toString(), e);
		}
		
		checkClientReply("Failed to delete directory: " + path.toString());
	}
	
	@Override
	protected void deleteFile(Path path) throws XenonException {

	    assertIsOpen();

		try {
			ftpClient.deleteFile(path.toString());
		} catch (Exception e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to createDirectory " + path.toString(), e);
		}
		
		checkClientReply("Failed to delete file: " + path.toString());
	}

	@Override
	public boolean exists(Path path) throws XenonException {
	    
	    try { 
	        getFTPFileInfo(path);
	        return true;
	    } catch (NoSuchPathException e) {
	        return false;
        }
	}
	
    private FTPFile findFTPFile(FTPFile [] files, Path path) throws NoSuchPathException { 
        
        if (files == null || files.length == 0) { 
            throw new NoSuchPathException(ADAPTOR_NAME, "Path not found: " + path);
        }
        
        String name = path.getFileNameAsString();
        
        for (FTPFile f : files) { 
            
            if (f != null && f.getName().equals(name)) { 
                return f;
            }
        }

        throw new NoSuchPathException(ADAPTOR_NAME, "Path not found: " + path);
    }
    
	private FTPFile getFTPFileInfo(Path path) throws XenonException {

	    assertIsOpen();
        assertNotNull(path);

        // We cannot always get the FTPFile of the path directly, behavior of FTP servers seems to vary. Instead, 
        // we get the listing of the parent directory and extract the information we need from there.
        try { 
            Path p = path.getParent();

            String originalWorkingDirectory = ftpClient.printWorkingDirectory();

            if (p == null) {
                p = new Path("/");
            }

            boolean pathExists = ftpClient.changeWorkingDirectory(p.getAbsolutePath());

            if (!pathExists) { 
                // parent must be an existing dir, otherwise dir/path certainly does not exist. 
                throw new NoSuchPathException(ADAPTOR_NAME, "Path not found: " + path);
            }

            FTPFile [] files = ftpClient.listFiles();

            ftpClient.changeWorkingDirectory(originalWorkingDirectory);

            return findFTPFile(files, path);
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to get attributes for path: " + path, e);
        }
	}
    
	@Override
	public PathAttributes getAttributes(Path path) throws XenonException {
	    LOGGER.debug("getAttributes path = {}", path);
	    return convertAttributes(path, getFTPFileInfo(path));
	}
		
	@Override
	protected List<PathAttributes> listDirectory(Path path) throws XenonException {
        assertIsOpen();
	    assertDirectoryExists(path);

	    try {
			ArrayList<PathAttributes> result = new ArrayList<>();
			
			for (FTPFile f : ftpClient.listFiles(path.toString(), FTPFileFilters.NON_NULL)) {
				result.add(convertAttributes(path.resolve(f.getName()), f));
			}
	
			return result;
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME,"Failed to retrieve directory listing of " + path, e);
		}
	}
	
	@Override
	public InputStream readFromFile(Path path) throws XenonException {
		LOGGER.debug("newInputStream path = {}", path);

		assertIsOpen();
		assertPathExists(path);
		assertPathIsFile(path);
			
		// Since FTP connections can only do a single thing a time, we need a new FTPClient to handle the stream.
		FTPClient newClient = adaptor.connect(getLocation(), credential);
		newClient.enterLocalPassiveMode();
		
		try {
			InputStream in = newClient.retrieveFileStream(path.toString());
			
			//if (in == null) { 
				checkClientReply(newClient, "Failed to read from path: " + path.toString());
			//}
			
			return new FtpInputStream(in, newClient);
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to read from path: " + path);
		}
	}
	
	@Override
	public OutputStream writeToFile(Path path, long size) throws XenonException {
		LOGGER.debug("writeToFile path = {} size = {}", path, size);

		assertIsOpen();
		assertPathNotExists(path);
		assertParentDirectoryExists(path);
		
		// Since FTP connections can only do a single thing a time, we need a new FTPClient to handle the stream.
		FTPClient newClient = adaptor.connect(getLocation(), credential);
		newClient.enterLocalPassiveMode();
		
		try {
		    newClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			OutputStream out = newClient.storeFileStream(path.toString());
			checkClientReply(newClient, "Failed to write to path: " + path.toString());
			return new FtpOutputStream(out, newClient);
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to write to path: " + path);
		}
	}

	@Override
	public OutputStream writeToFile(Path path) throws XenonException {
		return writeToFile(path, -1);
	}

	@Override
	public OutputStream appendToFile(Path path) throws XenonException {
		LOGGER.debug("appendToFile path = {}", path);

		assertIsOpen();
		assertPathExists(path);
		assertPathIsNotDirectory(path);
		
		try {
            // Since FTP connections can only do a single thing a time, we need a new FTPClient to handle the stream.
            FTPClient newClient = adaptor.connect(getLocation(), credential);
            newClient.enterLocalPassiveMode();
			OutputStream out = newClient.appendFileStream(path.toString());
			
			if (out == null) { 
				checkClientReply("Failed to append to path: "+ path.toString());
			}

			return new FtpOutputStream(out, newClient);
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to append to path: " + path);
		}
	}
	
	@Override
	public Path readSymbolicLink(Path path) throws XenonException {
	    
	    FTPFile file = getFTPFileInfo(path);
		
		if (file.getType() != FTPFile.SYMBOLIC_LINK_TYPE) {
			throw new InvalidPathException(ADAPTOR_NAME, "Path is not a symbolic link: " + path);
		}
		
		return new Path(file.getLink());
	}

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
        throw new UnsupportedOperationException(getAdaptorName(),"FTP does not support changing permissions.");
    }
}
