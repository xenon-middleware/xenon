/**
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
import java.nio.charset.StandardCharsets;
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
		
		PathAttributes result = new PathAttributes();
		
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
	
	
	@Override
	public void move(Path source, Path target) throws XenonException {

		LOGGER.debug("move source = {} target = {}", source, target);

		assertPathExists(source);
	
		if (areSamePaths(source, target)) {
			return;
		}

		assertPathNotExists(target);
		assertParentDirectoryExists(target);
	
		final String absoluteSourcePath = source.getAbsolutePath();

		FtpCommand ftpCommand = new FtpCommand() {
			@Override
			public void doWork(FTPClient ftpClient, String absoluteTargetPath) throws IOException {
				ftpClient.rename(absoluteSourcePath, absoluteTargetPath);
			}
		};

		ftpCommand.execute(ftpClient, target, "Failed to move to path");

		LOGGER.debug("move OK");
	}

	@Override
	public void createDirectory(Path path) throws XenonException {
		LOGGER.debug("createDirectory dir = {}", path);

		FtpCommand ftpCommand = new FtpCommand() {
			@Override
			public void doWork(FTPClient ftpClient, String absolutePath) throws IOException {
				setHasSucceeded(ftpClient.makeDirectory(absolutePath));
			}
		};
		ftpCommand.execute(ftpClient, path, "Failed to create directory");
		LOGGER.debug("createDirectory OK");
	}

	@Override
	public void createFile(Path path) throws XenonException {
		LOGGER.debug("createFile path = {}", path);
		assertPathNotExists(path);
		FtpCommand ftpCommand = new FtpCommand() {
			@Override
			public void doWork(FTPClient ftpClient, String absolutePath) throws IOException {
				InputStream dummyContent = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
				setHasSucceeded(ftpClient.storeFile(absolutePath, dummyContent));
			}
		};
		ftpCommand.execute(ftpClient, path, "Failed to create file");
		LOGGER.debug("createFile OK");
	}

	@Override
	public void createSymbolicLink(Path link, Path path) throws XenonException {
		throw new UnsupportedOperationException(ADAPTOR_NAME, "Operation not supported");
	}
	
	@Override
	protected void deleteDirectory(Path path) throws XenonException {
		FtpCommand ftpCommand = new FtpCommand() {
			@Override
			public void doWork(FTPClient ftpClient, String absolutePath) throws IOException {
				setHasSucceeded(ftpClient.removeDirectory(absolutePath));
			}
		};

		ftpCommand.execute(ftpClient, path, "Failed to delete file or directory");
	}
	
	@Override
	protected void deleteFile(Path path) throws XenonException {
		FtpCommand ftpCommand = new FtpCommand() {
			@Override
			public void doWork(FTPClient ftpClient, String absolutePath) throws IOException {
				setHasSucceeded(ftpClient.deleteFile(absolutePath));
			}
		};

		ftpCommand.execute(ftpClient, path, "Failed to delete file or directory");
	}

	private boolean fileExists(Path path) throws XenonException {
		FtpQuery<Boolean> ftpQuery = new FtpQuery<Boolean>() {
			@Override
			public void doWork(FTPClient ftpClient, String path) throws IOException {
				FTPFile[] listFiles = ftpClient.listFiles(path);
				int length = listFiles.length;
				setResult(length == 1);
			}
		};
		ftpQuery.execute(ftpClient, path, "Could not inspect file");
		return ftpQuery.getResult();
	}

	private boolean directoryExists(Path path) throws XenonException {
		FtpQuery<Boolean> ftpQuery = new FtpQuery<Boolean>() {
			@Override
			public void doWork(FTPClient ftpClient, String path) throws IOException {
				String originalWorkingDirectory = ftpClient.printWorkingDirectory();
				boolean pathExists = ftpClient.changeWorkingDirectory(path);
				ftpClient.changeWorkingDirectory(originalWorkingDirectory);
				setResult(pathExists);
			}
		};
		ftpQuery.execute(ftpClient, path, "Could not inspect directory");
		return ftpQuery.getResult();
	}
	
	@Override
	public boolean exists(Path path) throws XenonException {
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
	
//	private FTPFile[] listDirectory(Path path) throws XenonException {
//		String absolutePath = path.getAbsolutePath();
//
//		try {
//			assertDirectoryExists(path);
//			return ftpClient.listFiles(absolutePath, FTPFileFilters.NON_NULL);
//		} catch (IOException e) {
//			throw new XenonException(ADAPTOR_NAME,"Failed to retrieve directory listing of " + absolutePath);
//		}
//	}

	
//	private List<FTPFile> listDirectory(Path path) throws XenonException {
//		String absolutePath = path.getAbsolutePath();
//
//		FTPFile[] listFiles;
//
//		try {
//			assertDirectoryExists(path);
//			listFiles = ftpClient.listFiles(absolutePath, FTPFileFilters.NON_NULL);
//		} catch (IOException e) {
//			throw new XenonException(ADAPTOR_NAME,"Failed to retrieve directory listing of " + absolutePath);
//		}
//
//		return new LinkedList<>(Arrays.asList(listFiles));
//	}
	
	

//	private List<PathAttributesPair> list(Path path, ArrayList<PathAttributesPair> list, boolean recursive) throws XenonException {
//		
//		FTPFile[] ftpList = listDirectory(path);
//	
//		for (int i=0;i<ftpList.length;i++) { 
//			
//			FTPFile current = ftpList[i];
//			
//			if (!isDotDot(path)) { 
//				list.add(convert(path, current));
//			
//				if (recursive && current.isDirectory()) { 
//					list(path.resolve(current.getName()), list, recursive);
//				}
//			}
//		}
//		
//		return list;
//	}
//	
//	@Override
//	public Iterable<PathAttributesPair> list(Path path, boolean recursive) throws XenonException {
//		return list(path, new ArrayList<PathAttributesPair>(), recursive);
//	}
	
//	@Override
//	public DirectoryStream<Path> newDirectoryStream(Path path, Filter filter) throws XenonException {
//		LOGGER.debug("newDirectoryStream path = {} filter = <?>", path);
//		return new FtpDirectoryStream(path, filter, listDirectory(path, filter));
//	}
//
//
//	@Override
//	public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path path, Filter filter) throws XenonException {
//		LOGGER.debug("newAttributesDirectoryStream path = {} filter = <?>", path);
//		if (path == null) {
//			throw new XenonException(ADAPTOR_NAME, "Cannot open attribute directory stream of null path");
//		}
//		return new FtpDirectoryAttributeStream(path, filter, listDirectory(path, filter));
//	}

	/*
	 * Creates a copy of the file system of the path and returns a copy of the path that refers to the new file system. This way
	 * structures like streams can have their own dedicated ftp client. This is necessary because ftp can't do two things over the
	 * same connection like uploading to a stream and reading from another stream for example.
	 *
	 * @param path
	 * @return copy of the path referring to a new file system
	 * @throws XenonException
	 */
//	private Path getPathOnNewFileSystem(Path path) throws XenonException {
//		FileSystem newFileSystem = adaptor.createFileSystem(getLocation(), credential, getProperties());
//		return newFileSystem.newPath(path.getRelativePath());
//	}

	@Override
	public InputStream readFromFile(Path path) throws XenonException {
		LOGGER.debug("newInputStream path = {}", path);
		
		assertPathExists(path);
		assertPathIsFile(path);
			
		// Since FTP connections can only do a single thing a time, we need a new FTPClient to handle the stream.
		FTPClient newClient = adaptor.connect(getLocation(), credential);
		
		FtpQuery<InputStream> ftpQuery = new FtpQuery<InputStream>() {
			@Override
			public void doWork(FTPClient ftpClient, String path) throws IOException {
				setResult(ftpClient.retrieveFileStream(path));
			}
		};
		ftpQuery.execute(newClient, path, "Failed to open input stream");
		
		return new FtpInputStream(ftpQuery.getResult(), ftpClient);
	}
//
//	private FtpOutputStream getOutputStreamFromFtpClient(FTPClient ftpClient, Path path, OpenOptions options) throws XenonException {
//		FtpQuery<OutputStream> ftpQuery = options.getAppendMode() == OpenOption.APPEND ? getAppendingOutputStreamQuery()
//				: getTruncatingOrNewFileOutputStreamQuery();
//		ftpQuery.execute(ftpClient, path, "Failed to open outputstream");
//		return new FtpOutputStream(ftpQuery.getResult(), ftpClient);
//	}

//	private OutputStream getTruncatingOrNewFileOutputStream(FTPClient ftpClient, Path path) throws XenonException {
//		FtpQuery<OutputStream> ftpQuery;
//		ftpQuery = new FtpQuery<OutputStream>() {
//			@Override
//			public void doWork(FTPClient ftpClient, String path) throws IOException {
//				setResult(ftpClient.storeFileStream(path));
//			}
//		};
//		ftpQuery.execute(ftpClient, path, "Failed to open outputstream");
//		return new FtpOutputStream(ftpQuery.getResult(), ftpClient);
//	}
//
//	private FtpQuery<OutputStream> getAppendingOutputStreamQuery() {
//		FtpQuery<OutputStream> ftpQuery;
//		ftpQuery = new FtpQuery<OutputStream>() {
//			@Override
//			public void doWork(FTPClient ftpClient, String path) throws IOException {
//				setResult(ftpClient.appendFileStream(path));
//			}
//		};
//		return ftpQuery;
//	}

//	private void assertValidArgumentsForNewOutputStream(Path path, OpenOptions processedOptions) throws XenonException {
//		if (processedOptions.getReadMode() != null) {
//			throw new InvalidOptionsException(ADAPTOR_NAME, "Disallowed open option: READ");
//		}
//
//		if (processedOptions.getAppendMode() == null) {
//			throw new InvalidOptionsException(ADAPTOR_NAME, "No append mode provided!");
//		}
//
//		if (processedOptions.getWriteMode() == null) {
//			processedOptions.setWriteMode(OpenOption.WRITE);
//		}
//
//		if (processedOptions.getOpenMode() == OpenOption.CREATE) {
//			assertPathNotExists(path);
//		} else if (processedOptions.getOpenMode() == OpenOption.OPEN) {
//			assertPathExists(path);
//		}
//	}

	
	@Override
	public OutputStream writeToFile(Path path, long size) throws XenonException {
		LOGGER.debug("writeToFile path = {} size = {}", path, size);

		FtpQuery<OutputStream> ftpQuery = new FtpQuery<OutputStream>() {
			@Override
			public void doWork(FTPClient ftpClient, String path) throws IOException {
				setResult(ftpClient.storeFileStream(path));
			}
		};
		
		ftpQuery.execute(ftpClient, path, "Failed to open outputstream for write");
		return new FtpOutputStream(ftpQuery.getResult(), ftpClient);		
	}

	@Override
	public OutputStream writeToFile(Path path) throws XenonException {
		return writeToFile(path, -1);
	}
	
	@Override
	public OutputStream appendToFile(Path path) throws XenonException {
		LOGGER.debug("appendToFile path = {}", path);

		assertPathExists(path);
		
		FtpQuery<OutputStream> ftpQuery = new FtpQuery<OutputStream>() {
			@Override
			public void doWork(FTPClient ftpClient, String path) throws IOException {
				setResult(ftpClient.appendFileStream(path));
			}
		};
		
		ftpQuery.execute(ftpClient, path, "Failed to open outputstream for append");
		return new FtpOutputStream(ftpQuery.getResult(), ftpClient);		
	}
	
	private FTPFile getFtpFile(Path path) throws XenonException {
		FTPFile ftpFile = getRegularFtpFile(path);
		if (ftpFile == null) {
			ftpFile = getDirectoryFtpFile(path);
		}
		return ftpFile;
	}

	private FTPFile getRegularFtpFile(Path pathToRegularFile) throws XenonException {
		
		try {
			ftpClient.pasv();
			
			
			
			FTPFile [] result = ftpClient.listFiles(pathToRegularFile.getAbsolutePath());
		
			String replyString = ftpClient.getReplyString();
	        int code = ftpClient.getReplyCode();
			
			System.out.println("FTP " + replyString + " " + code + " " + result + " " + result.length);
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		FtpQuery<FTPFile> ftpQuery = new FtpQuery<FTPFile>() {
			@Override
			public void doWork(FTPClient ftpClient, String path) throws IOException {
				setResult(ftpClient.listFiles(path)[0]);
			}
		};
		ftpQuery.execute(ftpClient, pathToRegularFile, "Failed to retrieve attributes of path");
		return ftpQuery.getResult();
	}
	
	private FTPFile getDirectoryFtpFile(Path path) throws XenonException {
		FtpQuery<FTPFile> ftpQuery = new FtpQuery<FTPFile>() {
			@Override
			public void doWork(FTPClient ftpClient, String path) throws IOException {
				String targetName = ".";
				FTPFile[] listFiles = ftpClient.listDirectories(path);
				for (FTPFile listFile : listFiles) {
					if (listFile.getName().equals(targetName)) {
						setResult(listFile);
						break;
					}
				}
			}
		};
		ftpQuery.execute(ftpClient, path, "Failed to retrieve attributes of directory");
		return ftpQuery.getResult();
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
    }
}
