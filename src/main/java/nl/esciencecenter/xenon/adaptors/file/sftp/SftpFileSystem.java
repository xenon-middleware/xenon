package nl.esciencecenter.xenon.adaptors.file.sftp;

import static nl.esciencecenter.xenon.adaptors.file.sftp.SftpFileAdaptor.ADAPTOR_NAME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
import nl.esciencecenter.xenon.adaptors.file.EndOfFileException;
import nl.esciencecenter.xenon.adaptors.file.NoSpaceException;
import nl.esciencecenter.xenon.adaptors.file.PermissionDeniedException;
import nl.esciencecenter.xenon.adaptors.file.PosixFileUtils;
import nl.esciencecenter.xenon.files.CopyDescription;
import nl.esciencecenter.xenon.files.CopyHandle;
import nl.esciencecenter.xenon.files.CopyStatus;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.InvalidPathException;
import nl.esciencecenter.xenon.files.NoSuchPathException;
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
		
		assertPathExists(source);
		
		if (areSamePaths(source, target)) {
			return;
		}

		assertPathNotExists(target);
		assertParentDirectoryExists(target);

		try {
			client.rename(source.getAbsolutePath(), target.getAbsolutePath());
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed to rename path", e);
		}

		LOGGER.debug("move OK");
	}

	@Override
	public void createDirectory(Path dir) throws XenonException {

		LOGGER.debug("createDirectory dir = {}", dir);

		assertPathNotExists(dir);
		assertParentDirectoryExists(dir);
		
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

		OutputStream out = null;

		try {
			out = writeToFile(file);
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
	protected void deleteFile(Path file) throws XenonException { 
		try { 
			client.remove(file.getAbsolutePath());
		} catch (IOException e) {
			sftpExceptionToXenonException(e, "Cannot delete file: " + file);
		}
	}
	   
	@Override
	protected void deleteDirectory(Path dir) throws XenonException { 
		try { 
			client.rmdir(dir.getAbsolutePath());
		} catch (IOException e) {
			sftpExceptionToXenonException(e, "Cannot delete directory: " + dir);
		}
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

		try {
			stat(path);
			return true;
		} catch (NoSuchPathException e) {
			return false;
		}
	}

//	private PathAttributesPair convert(Path root, SftpClient.DirEntry e) { 
//		FileAttributes attributes = convertAttributes(e.getAttributes());
//		return new PathAttributesPair(root.resolve(e.getFilename()), attributes);
//	}
//
//	
//	private List<PathAttributesPair> listDirectory(Path path) throws XenonException {
//		
//		assertDirectoryExists(path);
//
//		ArrayList<PathAttributesPair> result = new ArrayList<>();
//		
//		try { 
//			for (SftpClient.DirEntry e : client.readDir(path.getAbsolutePath())) { 
//				result.add(convert(path, e));
//			}
//		} catch (IOException e) { 
//			throw new XenonException(ADAPTOR_NAME,"Failed to retrieve directory listing of " + path);
//		}
//		
//		return result;		
//	}
		
	@Override
	protected List<PathAttributesPair> listDirectory(Path path) throws XenonException {

		try {
			assertDirectoryExists(path);
		
			ArrayList<PathAttributesPair> result = new ArrayList<>();
			
			for (SftpClient.DirEntry f : client.readDir(path.getAbsolutePath())) { 
				result.add(new PathAttributesPair(path.resolve(f.getFilename()), convertAttributes(f.getAttributes())));
			}
	
			return result;
		} catch (IOException e) {
			throw sftpExceptionToXenonException(e, "Failed to list directory " + path);
		}
	}
	
	
//	@Override
//	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter filter) throws XenonException {
//		LOGGER.debug("newDirectoryStream path = {} filter = <?>", dir);
//		return new SftpDirectoryStream(dir, filter, listDirectory(dir, filter));
//	}
//
//	@Override
//	public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir, Filter filter) throws XenonException {
//		LOGGER.debug("newAttributesDirectoryStream path = {} filter = <?>", dir);
//		return new SftpDirectoryAttributeStream(dir, filter, listDirectory(dir, filter));
//	}

	@Override
	public InputStream readFromFile(Path path) throws XenonException {
		LOGGER.debug("newInputStream path = {}", path);

		assertFileExists(path);
		
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
	public OutputStream writeToFile(Path path, long size) throws XenonException {
		try {
			return client.write(path.getAbsolutePath(), SftpClient.OpenMode.Write, SftpClient.OpenMode.Truncate);      	
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed open stream to write to: " + path, e);
		}
	}
		
	@Override
	public OutputStream writeToFile(Path path) throws XenonException {
		return writeToFile(path, -1);
	}

	@Override
	public OutputStream appendToFile(Path path) throws XenonException {
		try {
			return client.write(path.getAbsolutePath(), SftpClient.OpenMode.Write, SftpClient.OpenMode.Append);      	
		} catch (IOException e) {
			throw new XenonException(ADAPTOR_NAME, "Failed open stream to write to: " + path, e);
		}
	}

	@Override
	public FileAttributes getAttributes(Path path) throws XenonException {
		return convertAttributes(stat(path));
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
	
	private static FileAttributes convertAttributes(SftpClient.Attributes attributes) { 
		
		FileAttributes result = new FileAttributes();
		
		result.setDirectory(attributes.isDirectory());
		result.setRegular(attributes.isRegularFile());
		result.setOther(attributes.isOther());
		result.setSymbolicLink(attributes.isSymbolicLink());
		
		result.setLastModifiedTime(attributes.getModifyTime().toMillis());
		result.setCreationTime(attributes.getCreateTime().toMillis());
		result.setLastAccessTime(attributes.getAccessTime().toMillis());
		
		result.setSize(attributes.getSize());
		
		Set<PosixFilePermission> permission = PosixFileUtils.bitsToPermissions(attributes.getPermissions());
		result.setPermissions(permission);
		
		result.setExecutable(permission.contains(PosixFilePermission.OWNER_EXECUTE));
		result.setReadable(permission.contains(PosixFilePermission.OWNER_READ));
		result.setWritable(permission.contains(PosixFilePermission.OWNER_WRITE));
		
		result.setGroup(attributes.getGroup());
		result.setOwner(attributes.getOwner());
		
		return result;
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
				return new NotConnectedException(ADAPTOR_NAME, "Connection lost", e);

			case SftpConstants.SSH_FX_OP_UNSUPPORTED:
				return new XenonException(ADAPTOR_NAME, "Unsupported operation", e);

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
				return new XenonException(ADAPTOR_NAME, "Invalid owner", e);

			case SftpConstants.SSH_FX_GROUP_INVALID:
				return new XenonException(ADAPTOR_NAME, "Invalid group", e);

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
