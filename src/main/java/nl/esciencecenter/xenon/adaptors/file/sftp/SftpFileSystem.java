package nl.esciencecenter.xenon.adaptors.file.sftp;

import static nl.esciencecenter.xenon.adaptors.file.sftp.SftpFileAdaptor.ADAPTOR_NAME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.apache.sshd.client.subsystem.sftp.SftpClient;
import org.apache.sshd.common.subsystem.sftp.SftpConstants;
import org.apache.sshd.common.subsystem.sftp.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.NotConnectedException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.file.ConnectionLostException;
import nl.esciencecenter.xenon.adaptors.file.OpenOptions;
import nl.esciencecenter.xenon.adaptors.file.PosixFileUtils;
import nl.esciencecenter.xenon.files.CopyHandle;
import nl.esciencecenter.xenon.files.CopyDescription;
import nl.esciencecenter.xenon.files.DirectoryNotEmptyException;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.DirectoryStream.Filter;
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

public class SftpFileSystem extends FileSystem {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SftpFileSystem.class);
	
	private final SftpClient client;
	
	protected SftpFileSystem(String uniqueID, String name, String location, Path entryPath, SftpClient client, 
			XenonProperties properties) {
		super(uniqueID, name, location, entryPath, properties);
		this.client = client;
	}
	
	private void checkParent(Path path) throws XenonException {

		Path parent = path.getParent();

		if (parent == null) { 
			throw new XenonException(ADAPTOR_NAME, "Parent directory does not exist!");
		}

		if (!exists(parent)) {
			throw new XenonException(ADAPTOR_NAME, "Parent directory " + parent + " does not exist!");
		}
	}
		
	@Override
	public void close() throws XenonException {
		
		LOGGER.debug("close fileSystem = {}", this);

		try {
			client.close();
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to close sftp client", e);
		}

		//info.getSession().disconnect();

		LOGGER.debug("close OK");        
	}
	
	@Override
	public boolean isOpen() throws XenonException {
		return client.isOpen();
	}

	@Override
	public void move(Path source, Path target) throws XenonException {
		
		LOGGER.debug("move source = {} target = {}", source, target);
		
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

		try {
			LOGGER.debug("move from " + source + " to " + target);

			client.rename(source.getAbsolutePath(), target.getAbsolutePath());
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to rename path", e);
		}

		LOGGER.debug("move OK");
	}

	@Override
	public void createDirectory(Path dir) throws XenonException {

		LOGGER.debug("createDirectory dir = {}", dir);

		if (exists(dir)) {
			throw new PathAlreadyExistsException(ADAPTOR_NAME, "Directory " + dir + " already exists!");
		}

		checkParent(dir);

		try {
			client.mkdir(dir.getAbsolutePath());
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to mkdir", e);
		}

		LOGGER.debug("createDirectory OK");        
	}

	@Override
	public void createFile(Path file) throws XenonException {
		
		LOGGER.debug("createFile path = {}", file);

		if (exists(file)) {
			throw new PathAlreadyExistsException(ADAPTOR_NAME, "File " + file + " already exists!");
		}

		checkParent(file);

		OutputStream out = null;

		try {
			out = newOutputStream(file, OpenOption.CREATE, OpenOption.WRITE, OpenOption.TRUNCATE);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				// ignore
			}
		}

		LOGGER.debug("createFile OK");
	}

	@Override
	public void delete(Path path) throws XenonException {
		
		LOGGER.debug("delete path = {}", path);

		System.out.println("SFTP DELETE: " + path);

		new Exception().printStackTrace(System.out);

		if (!exists(path)) {
			throw new NoSuchPathException(getClass().getName(), "Cannot delete file, as it does not exist");
		}

		FileAttributes att = getAttributes(path);

		try {
			if (att.isDirectory()) {
				if (newDirectoryStream(path).iterator().hasNext()) {
					throw new DirectoryNotEmptyException(ADAPTOR_NAME, "cannot delete dir " + path
							+ " as it is not empty");
				}

				client.rmdir(path.getAbsolutePath());
			} else {
				client.remove(path.getAbsolutePath());
			}
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to remove " + path, e);
		}

		LOGGER.debug("delete OK");
	}

	private SftpClient.Attributes stat(Path path) throws XenonException {

		LOGGER.debug("* stat path = {}", path);

		SftpClient.Attributes result;

		try {
			result = client.lstat(path.getAbsolutePath());
		} catch (IOException e) {
			throw sftpExceptionToXenonException(e, "Failed to retrieve attributes from: " + path);
		}

		LOGGER.debug("* stat OK result = {}", result);

		return result;
	}
	
	@Override
	public boolean exists(Path path) throws XenonException {

		LOGGER.debug("exists path = {}", path);

		boolean result;

		try {
			stat(path);
			result = true;
		} catch (NoSuchPathException e) {
			result = false;
		}

		LOGGER.debug("exists OK result = {}", result);

		return result;
	}
		
	@SuppressWarnings("unchecked")
	private List<SftpClient.DirEntry> listDirectory(Path path, Filter filter) throws XenonException {

		FileAttributes att = getAttributes(path);

		if (!att.isDirectory()) {
			throw new XenonException(ADAPTOR_NAME, "File is not a directory.");
		}

		if (filter == null) {
			throw new XenonException(ADAPTOR_NAME, "Filter is null.");
		}

		List<SftpClient.DirEntry> result;

		try {
			SftpClient.Handle handle = client.openDir(path.getAbsolutePath());
			result = client.readDir(handle);
		} catch (IOException e) {
			throw sftpExceptionToXenonException(e, "Failed to list directory " + path);
		}
		return result;
	}
	
	
	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter filter) throws XenonException {
		LOGGER.debug("newDirectoryStream path = {} filter = <?>", dir);
		return new SftpDirectoryStream(dir, filter, listDirectory(dir, filter));
	}

	@Override
	public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir, Filter filter) throws XenonException {
		LOGGER.debug("newAttributesDirectoryStream path = {} filter = <?>", dir);
		return new SftpDirectoryAttributeStream(dir, filter, listDirectory(dir, filter));
	}

	@Override
	public InputStream newInputStream(Path path) throws XenonException {
		LOGGER.debug("newInputStream path = {}", path);

		if (!exists(path)) {
			throw new NoSuchPathException(ADAPTOR_NAME, "File " + path + " does not exist!");
		}

		FileAttributes att = getAttributes(path);

		if (att.isDirectory()) {
			throw new XenonException(ADAPTOR_NAME, "Path " + path + " is a directory!");
		}

		InputStream in;

		try {
			in = client.read(path.getAbsolutePath());        	
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to open stream to read from " + path, e);
		}

		LOGGER.debug("newInputStream OK");

		return in;
	}

	@Override
	public OutputStream newOutputStream(Path path, OpenOption... options) throws XenonException {

		LOGGER.debug("newOutputStream path = {} option = {}", path, options);

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

		SftpClient.OpenMode mode = SftpClient.OpenMode.Create;

		if (exists(path)) { 

			if (tmp.getOpenMode() == OpenOption.CREATE) { 
				throw new PathAlreadyExistsException(ADAPTOR_NAME, "File already exists: " + path);
			} 

			if (OpenOption.APPEND.occursIn(options)) {
				mode = SftpClient.OpenMode.Append;
			} else if (OpenOption.TRUNCATE.occursIn(options)) { 
				mode = SftpClient.OpenMode.Truncate;
			}

		} else {         
			if (tmp.getOpenMode() == OpenOption.OPEN) {
				throw new NoSuchPathException(ADAPTOR_NAME, "File does not exist: " + path);
			}
		}

		OutputStream out;

		try {
			out = client.write(path.getAbsolutePath(), SftpClient.OpenMode.Write, mode);      	
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed open stream to write to: " + path, e);
		}

		LOGGER.debug("newOutputStream OK");

		return out;
	}

	@Override
	public FileAttributes getAttributes(Path path) throws XenonException {
		LOGGER.debug("getAttributes path = {}", path);
		return new SftpFileAttributes(stat(path), path);
	}

	@Override
	public Path readSymbolicLink(Path link) throws XenonException {
		LOGGER.debug("readSymbolicLink path = {}", link);

		Path result;

		try {
			String target = client.readLink(link.getAbsolutePath());

			if (!target.startsWith(File.separator)) {                
				Path parent = link.getParent();
				result = parent.resolve(target);
			} else {
				result = new Path(target);
			}
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to read link: " + link, e);
		}

		LOGGER.debug("readSymbolicLink OK result = {}", result);

		return result;
	}

	@Override
	public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
		LOGGER.debug("setPosixFilePermissions path = {} permissions = {}", path, permissions);

		try {
			// We need to create a new Attributes object here. SFTP will only forward the fields that are actually set 
			// when we call setStat. If we retrieve the existing attributes, change permissions and send the lot back 
			// we'll receive an error since some of the other attributes cannot be changed (learned this the hard way).
			SftpClient.Attributes a = new SftpClient.Attributes();
			a.setPermissions(PosixFileUtils.permissionsToBits(permissions));
			client.setStat(path.getAbsolutePath(), a);
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to set permissions on: " + path, e);
		}
		LOGGER.debug("setPosixFilePermissions OK");
	}

	@Override
	public CopyHandle copy(CopyDescription description) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	
	private static XenonException sftpExceptionToXenonException(IOException e, String message) {

		if (e instanceof SftpException) { 
			SftpException x = (SftpException) e;
			switch (x.getStatus()) { 

			case SftpConstants.SSH_FX_EOF:
				return new EndOfFileException(ADAPTOR_NAME, "Unexpected EOF", e);

			case SftpConstants.SSH_FX_NO_SUCH_FILE:
			case SftpConstants.SSH_FX_NO_SUCH_PATH:
				return new NoSuchPathException(ADAPTOR_NAME, "Path does not exists", e);

			case SftpConstants.SSH_FX_PERMISSION_DENIED:
				return new PermissionDeniedException(ADAPTOR_NAME, "Permission denied", e);

			case SftpConstants.SSH_FX_NO_CONNECTION:
				return new NotConnectedException(ADAPTOR_NAME, "Not connected", e);

			case SftpConstants.SSH_FX_CONNECTION_LOST:
				return new ConnectionLostException(ADAPTOR_NAME, "Connection lost", e);

			case SftpConstants.SSH_FX_OP_UNSUPPORTED:
				return new UnsupportedIOOperationException(ADAPTOR_NAME, "Unsupported operation", e);

			case SftpConstants.SSH_FX_FILE_ALREADY_EXISTS:
				return new PathAlreadyExistsException(ADAPTOR_NAME, "Already exists", e);

			case SftpConstants.SSH_FX_WRITE_PROTECT:
				return new PermissionDeniedException(ADAPTOR_NAME, "Write protected", e);

			case SftpConstants.SSH_FX_CANNOT_DELETE:
				return new PermissionDeniedException(ADAPTOR_NAME, "Cannot delete", e);

			case SftpConstants.SSH_FX_DELETE_PENDING:
				return new PermissionDeniedException(ADAPTOR_NAME, "Delete pending", e);

			case SftpConstants.SSH_FX_NO_MEDIA:
			case SftpConstants.SSH_FX_NO_SPACE_ON_FILESYSTEM:
				return new NoSpaceException(ADAPTOR_NAME, "No space on filesystem", e);

			case SftpConstants.SSH_FX_QUOTA_EXCEEDED:
				return new NoSpaceException(ADAPTOR_NAME, "Quota exceeded", e);

			case SftpConstants.SSH_FX_FILE_CORRUPT:
				return new InvalidPathException(ADAPTOR_NAME, "File corrupt", e);

			case SftpConstants.SSH_FX_DIR_NOT_EMPTY:
				return new InvalidPathException(ADAPTOR_NAME, "Directory not empty", e);

			case SftpConstants.SSH_FX_NOT_A_DIRECTORY:
				return new InvalidPathException(ADAPTOR_NAME, "Not a directory", e);

			case SftpConstants.SSH_FX_INVALID_FILENAME:
				return new InvalidPathException(ADAPTOR_NAME, "Invalid file name", e);

			case SftpConstants.SSH_FX_LINK_LOOP:
				return new InvalidPathException(ADAPTOR_NAME, "Link loop", e);

			case SftpConstants.SSH_FX_FILE_IS_A_DIRECTORY:
				return new InvalidPathException(ADAPTOR_NAME, "File is a directory", e);

			case SftpConstants.SSH_FX_OWNER_INVALID:
				return new InvalidAttributeException(ADAPTOR_NAME, "Invalid owner", e);

			case SftpConstants.SSH_FX_GROUP_INVALID:
				return new InvalidAttributeException(ADAPTOR_NAME, "Invalid group", e);


			case SftpConstants.SSH_FX_INVALID_HANDLE:
				return new XenonException(ADAPTOR_NAME, "Invalid handle", e);

			case SftpConstants.SSH_FX_INVALID_PARAMETER:
				return new XenonException(ADAPTOR_NAME, "Invalid parameter", e);

			case SftpConstants.SSH_FX_LOCK_CONFLICT:
			case SftpConstants.SSH_FX_BYTE_RANGE_LOCK_CONFLICT:
			case SftpConstants.SSH_FX_BYTE_RANGE_LOCK_REFUSED:
			case SftpConstants.SSH_FX_NO_MATCHING_BYTE_RANGE_LOCK:
				return new XenonException(ADAPTOR_NAME, "Locking failed", e);

			case SftpConstants.SSH_FX_UNKNOWN_PRINCIPAL:
				return new XenonException(ADAPTOR_NAME, "Unknown principal", e);

			case SftpConstants.SSH_FX_BAD_MESSAGE:
				return new XenonException(ADAPTOR_NAME, "Malformed message", e);

			}

			// Fall through if we do not know the error
		} 

		return new XenonException(ADAPTOR_NAME, message, e);
	}
}
