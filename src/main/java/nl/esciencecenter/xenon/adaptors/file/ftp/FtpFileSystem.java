package nl.esciencecenter.xenon.adaptors.file.ftp;

import static nl.esciencecenter.xenon.adaptors.file.ftp.FtpFileAdaptor.ADAPTOR_NAME;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.file.OpenOptions;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.files.CopyHandle;
import nl.esciencecenter.xenon.files.CopyDescription;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.DirectoryStream.Filter;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.InvalidOptionsException;
import nl.esciencecenter.xenon.files.NoSuchPathException;
import nl.esciencecenter.xenon.files.OpenOption;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PathAlreadyExistsException;
import nl.esciencecenter.xenon.files.PathAttributesPair;
import nl.esciencecenter.xenon.files.PosixFilePermission;

public class FtpFileSystem extends FileSystem {

	private static final Logger LOGGER = LoggerFactory.getLogger(FtpFileSystem.class);

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

	private boolean areSamePaths(Path source, Path target) {
		Path sourceName = source.normalize();
		Path targetName = target.normalize();
		return sourceName.equals(targetName);
	}

	private void assertPathNotExists(Path path) throws XenonException {
		if (exists(path)) {
			throw new PathAlreadyExistsException(ADAPTOR_NAME, "File already exists: " + path);
		}
	}

	private void assertPathExists(Path path) throws XenonException {
		if (!exists(path)) {
			throw new NoSuchPathException(ADAPTOR_NAME, "File does not exist: " + path);
		}
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

	private void assertDirectoryExists(Path path) throws XenonException {
		if (!directoryExists(path)) {
			throw new XenonException(ADAPTOR_NAME, "Directory does not exist at path " + path.getAbsolutePath());
		}
	}

	private void assertParentDirectoryExists(Path target) throws XenonException {
		Path parentDirectory = target.getParent();

		if (parentDirectory != null) {
			assertDirectoryExists(parentDirectory);
		}
	}

	private void assertValidArgumentsForMove(Path source, Path target) throws XenonException {
		assertPathExists(source);
		assertPathNotExists(target);
		assertParentDirectoryExists(target);
	}

	@Override
	public void move(Path source, Path target) throws XenonException {

		LOGGER.debug("move source = {} target = {}", source, target);

		if (areSamePaths(source, target)) {
			return;
		}

		assertValidArgumentsForMove(source, target);

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

	private void deleteDirectory(Path path) throws XenonException {
		FtpCommand ftpCommand = new FtpCommand() {
			@Override
			public void doWork(FTPClient ftpClient, String absolutePath) throws IOException {
				setHasSucceeded(ftpClient.removeDirectory(absolutePath));
			}
		};

		ftpCommand.execute(ftpClient, path, "Failed to delete file or directory");
	}

	private void deleteFile(Path path) throws XenonException {
		FtpCommand ftpCommand = new FtpCommand() {
			@Override
			public void doWork(FTPClient ftpClient, String absolutePath) throws IOException {
				setHasSucceeded(ftpClient.deleteFile(absolutePath));
			}
		};

		ftpCommand.execute(ftpClient, path, "Failed to delete file or directory");
	}

	@Override
	public void delete(Path path) throws XenonException {
		if (getAttributes(path).isDirectory()) {
			deleteDirectory(path);
		} else {
			deleteFile(path);
		}
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

	@Override
	public boolean exists(Path path) throws XenonException {
		return fileExists(path) || directoryExists(path);
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path path, Filter filter) throws XenonException {
		LOGGER.debug("newDirectoryStream path = {} filter = <?>", path);
		return new FtpDirectoryStream(path, filter, listDirectory(path, filter));
	}

	private List<FTPFile> listDirectory(Path path, Filter filter) throws XenonException {
		String absolutePath = path.getAbsolutePath();

		FTPFile[] listFiles;

		try {
			if (filter == null) {
				throw new XenonException(ADAPTOR_NAME, "Filter is null.");
			}
			assertDirectoryExists(path);
			listFiles = ftpClient.listFiles(absolutePath);
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME,"Failed to retrieve directory listing of " + absolutePath);
		}

		return new LinkedList<>(Arrays.asList(listFiles));
	}

	@Override
	public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path path, Filter filter) throws XenonException {
		LOGGER.debug("newAttributesDirectoryStream path = {} filter = <?>", path);
		if (path == null) {
			throw new XenonException(ADAPTOR_NAME, "Cannot open attribute directory stream of null path");
		}
		return new FtpDirectoryAttributeStream(path, filter, listDirectory(path, filter));
	}

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

	private void assertValidArgumentsForNewInputStream(Path path) throws XenonException {
		assertPathExists(path);
		FileAttributes att = getAttributes(path);
		if (att.isDirectory()) {
			throw new XenonException(ADAPTOR_NAME, "Path " + path + " is a directory!");
		}
	}

	private FtpInputStream getInputStreamFromFtpClient(FTPClient ftpClient, Path path) throws XenonException {
		FtpQuery<InputStream> ftpQuery = new FtpQuery<InputStream>() {
			@Override
			public void doWork(FTPClient ftpClient, String path) throws IOException {
				setResult(ftpClient.retrieveFileStream(path));
			}
		};
		ftpQuery.execute(ftpClient, path, "Failed to open input stream");
		return new FtpInputStream(ftpQuery.getResult(), ftpClient);
	}

	@Override
	public InputStream newInputStream(Path path) throws XenonException {
		LOGGER.debug("newInputStream path = {}", path);
		assertValidArgumentsForNewInputStream(path);

		// Since FTP connections can only do a single thing a time, we need a new FTPClient to handle the stream.
		FTPClient newClient = adaptor.connect(getLocation(), credential);
		return getInputStreamFromFtpClient(newClient, path);
	}

	private FtpOutputStream getOutputStreamFromFtpClient(FTPClient ftpClient, Path path, OpenOptions options) throws XenonException {
		FtpQuery<OutputStream> ftpQuery = options.getAppendMode() == OpenOption.APPEND ? getAppendingOutputStreamQuery()
				: getTruncatingOrNewFileOutputStreamQuery();
		ftpQuery.execute(ftpClient, path, "Failed to open outputstream");
		return new FtpOutputStream(ftpQuery.getResult(), ftpClient);
	}

	private FtpQuery<OutputStream> getTruncatingOrNewFileOutputStreamQuery() {
		FtpQuery<OutputStream> ftpQuery;
		ftpQuery = new FtpQuery<OutputStream>() {
			@Override
			public void doWork(FTPClient ftpClient, String path) throws IOException {
				setResult(ftpClient.storeFileStream(path));
			}
		};
		return ftpQuery;
	}

	private FtpQuery<OutputStream> getAppendingOutputStreamQuery() {
		FtpQuery<OutputStream> ftpQuery;
		ftpQuery = new FtpQuery<OutputStream>() {
			@Override
			public void doWork(FTPClient ftpClient, String path) throws IOException {
				setResult(ftpClient.appendFileStream(path));
			}
		};
		return ftpQuery;
	}

	private void assertValidArgumentsForNewOutputStream(Path path, OpenOptions processedOptions) throws XenonException {
		if (processedOptions.getReadMode() != null) {
			throw new InvalidOptionsException(ADAPTOR_NAME, "Disallowed open option: READ");
		}

		if (processedOptions.getAppendMode() == null) {
			throw new InvalidOptionsException(ADAPTOR_NAME, "No append mode provided!");
		}

		if (processedOptions.getWriteMode() == null) {
			processedOptions.setWriteMode(OpenOption.WRITE);
		}

		if (processedOptions.getOpenMode() == OpenOption.CREATE) {
			assertPathNotExists(path);
		} else if (processedOptions.getOpenMode() == OpenOption.OPEN) {
			assertPathExists(path);
		}
	}


	@Override
	public OutputStream newOutputStream(Path path, OpenOption... options) throws XenonException {
		LOGGER.debug("newOutputStream path = {} option = {}", path, options);

		OpenOptions processedOptions = OpenOptions.processOptions(ADAPTOR_NAME, options);
		assertValidArgumentsForNewOutputStream(path, processedOptions);
		
		// Since FTP connections can only do a single thing a time, we need a new FTPClient to handle the stream.
		FTPClient newClient = adaptor.connect(getLocation(), credential);
		return getOutputStreamFromFtpClient(newClient, path, processedOptions);
	}
	
	private FTPFile getFtpFile(Path path) throws XenonException {
		FTPFile ftpFile = getRegularFtpFile(path);
		if (ftpFile == null) {
			ftpFile = getDirectoryFtpFile(path);
		}
		return ftpFile;
	}

	private FTPFile getRegularFtpFile(Path pathToRegularFile) throws XenonException {
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
	public FileAttributes getAttributes(Path path) throws XenonException {
		LOGGER.debug("getAttributes path = {}", path);
		assertPathExists(path);
		FTPFile listFile = getFtpFile(path);
		FileAttributes fileAttributes = new FtpFileAttributes(listFile);
		LOGGER.debug("getAttributes OK result = {}", fileAttributes);
		return fileAttributes;
	}

	@Override
	public Path readSymbolicLink(Path path) throws XenonException {
		LOGGER.debug("readSymbolicLink path = {}", path);
		throw new XenonException(ADAPTOR_NAME, "Ftp file system does not support symbolic links");
	}

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
        LOGGER.debug("setPosixFilePermissions path = {} permissions = {}", path, permissions);
        LOGGER.debug("setPosixFilePermissions OK");
    }

	@Override
	public CopyHandle copy(CopyDescription description) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

}
