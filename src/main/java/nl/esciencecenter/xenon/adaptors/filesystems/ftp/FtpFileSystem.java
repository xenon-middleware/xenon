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
			throw new XenonException(ADAPTOR_NAME, "File system is already closed");
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

		assertPathExists(source);
	
		if (areSamePaths(source, target)) {
			return;
		}

		assertPathNotExists(target);
		assertParentDirectoryExists(target);
		
		try { 
			ftpClient.rename(source.getAbsolutePath(), target.getAbsolutePath());
		} catch (Exception e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to rename " + source.getAbsolutePath() + " to " + target.getAbsolutePath(), e);
		}
		
		checkClientReply("Failed to rename " + source.getAbsolutePath() + " to " + target.getAbsolutePath());
	}

	@Override
	public void createDirectory(Path path) throws XenonException {
		LOGGER.debug("createDirectory dir = {}", path);

		assertPathNotExists(path);
		assertParentDirectoryExists(path);
		
		try { 
			ftpClient.makeDirectory(path.getAbsolutePath());
		} catch (Exception e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to createDirectory " + path.getAbsolutePath(), e);
		}
		
		checkClientReply("Failed to create directory: " + path.getAbsolutePath());
	}

	@Override
	public void createFile(Path path) throws XenonException {
		LOGGER.debug("createFile path = {}", path);
		
		assertPathNotExists(path);
		assertParentDirectoryExists(path);
		
		try {
			ByteArrayInputStream dummy = new ByteArrayInputStream(new byte[0]);
			ftpClient.storeFile(path.getAbsolutePath(), dummy);
		} catch (Exception e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to createDirectory " + path.getAbsolutePath(), e);
		}

		checkClientReply("Failed to create file: " + path.getAbsolutePath());
	}

	@Override
	public void createSymbolicLink(Path link, Path path) throws XenonException {
		throw new UnsupportedOperationException(ADAPTOR_NAME, "Operation not supported");
	}
	
	@Override
	protected void deleteDirectory(Path path) throws XenonException {
		
		try {
			ftpClient.removeDirectory(path.getAbsolutePath());
		} catch (Exception e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to createDirectory " + path.getAbsolutePath(), e);
		}
		
		checkClientReply("Failed to delete directory: " + path.getAbsolutePath());
	}
	
	@Override
	protected void deleteFile(Path path) throws XenonException {
		try {
			ftpClient.deleteFile(path.getAbsolutePath());
		} catch (Exception e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to createDirectory " + path.getAbsolutePath(), e);
		}
		
		checkClientReply("Failed to delete file: " + path.getAbsolutePath());
	}

	
	private boolean fileExists(Path path) throws XenonException {
		
		try { 
			FTPFile[] listFiles = ftpClient.listFiles(path.getAbsolutePath());
			checkClientReply("Failed to check if file exists: " + path.getAbsolutePath());
			return (listFiles != null && listFiles.length == 1);
		} catch (Exception e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to check if file exists: " + path.getAbsolutePath() + " " + e.getMessage(), e);
		}
	}

	private boolean directoryExists(Path path) throws XenonException {
		
		try { 
			String originalWorkingDirectory = ftpClient.printWorkingDirectory();
			boolean pathExists = ftpClient.changeWorkingDirectory(path.getAbsolutePath());
			
			// checkClientReply("Failed to check if directory exists: " + path.getAbsolutePath());
			
			ftpClient.changeWorkingDirectory(originalWorkingDirectory);
			return pathExists;
		} catch (Exception e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to check if directory exists: " + path.getAbsolutePath(), e);
		}
	}
	
	@Override
	public boolean exists(Path path) throws XenonException {
		assertNotNull(path);
		return fileExists(path) || directoryExists(path);
	}
	
	@Override
	protected List<PathAttributes> listDirectory(Path path) throws XenonException {
		try {
			assertDirectoryExists(path);
		
			ArrayList<PathAttributes> result = new ArrayList<>();
			
			for (FTPFile f : ftpClient.listFiles(path.getAbsolutePath(), FTPFileFilters.NON_NULL)) { 
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
		
		assertPathExists(path);
		assertPathIsFile(path);
			
		// Since FTP connections can only do a single thing a time, we need a new FTPClient to handle the stream.
		FTPClient newClient = adaptor.connect(getLocation(), credential);
		newClient.enterLocalPassiveMode();
		
		try {
			InputStream in = newClient.retrieveFileStream(path.getAbsolutePath()); 
			
			//if (in == null) { 
				checkClientReply(newClient, "Failed to read from path: " + path.getAbsolutePath());
			//}
			
			return new FtpInputStream(in, newClient);
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to read from path: " + path);
		}
	}
	
	@Override
	public OutputStream writeToFile(Path path, long size) throws XenonException {
		LOGGER.debug("writeToFile path = {} size = {}", path, size);
		assertParentDirectoryExists(path);
		assertPathIsNotDirectory(path);
		
		// Since FTP connections can only do a single thing a time, we need a new FTPClient to handle the stream.
		FTPClient newClient = adaptor.connect(getLocation(), credential);
		newClient.enterLocalPassiveMode();
		
		try {
			OutputStream out = newClient.storeFileStream(path.getAbsolutePath());
			
			//if (out == null) { 
				checkClientReply(newClient, "Failed to write to path: " + path.getAbsolutePath());
			//}

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

		assertPathExists(path);
		assertPathIsNotDirectory(path);
		
		try {
            // Since FTP connections can only do a single thing a time, we need a new FTPClient to handle the stream.
            FTPClient newClient = adaptor.connect(getLocation(), credential);
            newClient.enterLocalPassiveMode();
			OutputStream out = newClient.appendFileStream(path.getAbsolutePath());
			
			if (out == null) { 
				checkClientReply("Failed to append to path: "+ path.getAbsolutePath());
			}

			return new FtpOutputStream(out, newClient);
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to append to path: " + path);
		}
	}
	
	private FTPFile getFtpFile(Path path) throws XenonException {
		FTPFile ftpFile = getRegularFtpFile(path);
		if (ftpFile == null) {
			ftpFile = getDirectoryFtpFile(path);
		}
		return ftpFile;
	}

	private FTPFile getRegularFtpFile(Path path) throws XenonException {
		
		
//		try {
//			ftpClient.pasv();
//			
//			FTPFile [] result = ftpClient.listFiles(path.getAbsolutePath());
//		
//			String replyString = ftpClient.getReplyString();
//	        int code = ftpClient.getReplyCode();
//			
//			System.out.println("FTP " + replyString + " " + code + " " + result + " " + result.length);
//	        
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		try { 
			FTPFile [] result = ftpClient.listFiles(path.getAbsolutePath());
			checkClientReply("Failed to retrieve attributes of path: " + path.getAbsolutePath());
			return result[0];
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to retrieve attributes of path: " + path.getAbsolutePath());
		}
		
//		
//		
//		FtpQuery<FTPFile> ftpQuery = new FtpQuery<FTPFile>() {
//			@Override
//			public void doWork(FTPClient ftpClient, String path) throws IOException {
//				setResult(ftpClient.listFiles(path)[0]);
//			}  
//		};
//		ftpQuery.execute(ftpClient, pathToRegularFile, "Failed to retrieve attributes of path");
//		return ftpQuery.getResult();
	}
	
	private FTPFile getDirectoryFtpFile(Path path) throws XenonException {

		try { 
			FTPFile[] result = ftpClient.listDirectories(path.getAbsolutePath());
			checkClientReply("Failed to retrieve attributes of directory");
			return result[0];
		} catch (Exception e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to retrieve attributes of directory: " + path);
		}
			
		
//		
//		
//		
//		FtpQuery<FTPFile> ftpQuery = new FtpQuery<FTPFile>() {
//			@Override
//			public void doWork(FTPClient ftpClient, String path) throws IOException {
//				String targetName = ".";
//				FTPFile[] listFiles = ftpClient.listDirectories(path);
//				for (FTPFile listFile : listFiles) {
//					if (listFile.getName().equals(targetName)) {
//						setResult(listFile);
//						break;
//					}
//				}
//			}
//		};
//		ftpQuery.execute(ftpClient, path, "Failed to retrieve attributes of directory");
//		return ftpQuery.getResult();
	}
	
	@Override
	public PathAttributes getAttributes(Path path) throws XenonException {
		LOGGER.debug("getAttributes path = {}", path);

		assertPathExists(path);
		PathAttributes fileAttributes = convertAttributes(path, getFtpFile(path));
		LOGGER.debug("getAttributes OK result = {}", fileAttributes);
		return fileAttributes;
	}

	@Override
	public Path readSymbolicLink(Path path) throws XenonException {
		assertNotNull(path);
		FTPFile file = getFtpFile(path);
		
		if (file.getType() != FTPFile.SYMBOLIC_LINK_TYPE) {
			throw new InvalidPathException(ADAPTOR_NAME, "Path is not a symbolic link: " + path);
		}
		
		return new Path(file.getLink());
	}

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
        LOGGER.debug("setPosixFilePermissions path = {} permissions = {}", path, permissions);
        LOGGER.debug("setPosixFilePermissions OK");
        assertNotNull(path);
        if(permissions == null) {
			throw new IllegalArgumentException("Permissions is null");
		}
		throw new UnsupportedOperationException(getAdaptorName(),"FTP does not support changing permissions.");
    }
}
