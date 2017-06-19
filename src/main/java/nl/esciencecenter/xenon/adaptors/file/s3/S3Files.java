package nl.esciencecenter.xenon.adaptors.file.s3;



import static nl.esciencecenter.xenon.adaptors.file.sftp.SftpProperties.ADAPTOR_NAME;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.NoResponseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.adaptors.file.sftp.InvalidPathException;
import nl.esciencecenter.xenon.adaptors.file.sftp.PermissionDeniedException;
import nl.esciencecenter.xenon.adaptors.file.sftp.SftpFiles;
import nl.esciencecenter.xenon.adaptors.file.sftp.SftpProperties;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.files.FileAdaptor;
import nl.esciencecenter.xenon.engine.files.FileSystemImplementation;
import nl.esciencecenter.xenon.engine.files.FilesEngine;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.DirectoryStream.Filter;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.NoSuchPathException;
import nl.esciencecenter.xenon.files.OpenOption;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PathAttributesPair;
import nl.esciencecenter.xenon.files.PosixFilePermission;
import nl.esciencecenter.xenon.files.RelativePath;

public class S3Files extends FileAdaptor {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(S3Files.class);

    private static int currentID = 1;

    private static synchronized String getNewUniqueID() {
        String res = "s3" + currentID;
        currentID++;
        return res;
    }
    
	protected S3Files(FilesEngine engine, Map<String, String> properties) throws XenonException {
		super(engine, S3Properties.ADAPTOR_NAME, S3Properties.ADAPTOR_DESCRIPTION, S3Properties.ADAPTOR_SCHEME, S3Properties.ADAPTOR_LOCATIONS, S3Properties.VALID_PROPERTIES,
                new XenonProperties(S3Properties.VALID_PROPERTIES, Component.XENON, properties));
	}

	@Override
	public FileSystem newFileSystem(String location, Credential credential,
			Map<String, String> properties) throws XenonException {
		
       LOGGER.debug("newFileSystem scheme = S3 location = {} credential = {} properties = {}", location, credential, properties);
        
        XenonProperties xp = new XenonProperties(S3Properties.VALID_PROPERTIES, properties);
        
        if (!(credential instanceof PasswordCredential)){
        	throw new PermissionDeniedException("s3", "No secret key given for s3 connection.");
        }
        PasswordCredential c = (PasswordCredential)credential;
        
        MinioClient minioClient;
        
		try {
			minioClient = new MinioClient(location, c.getUsername(), new String(c.getPassword()));
		} catch (InvalidEndpointException | InvalidPortException e) {
        	throw new XenonException(ADAPTOR_NAME, "Failed to create S3 session", e);
		}
		
        RelativePath entryPath = new RelativePath("");
		String id = getNewUniqueID();
		return new S3FileSystem(minioClient,ADAPTOR_NAME, id,  location, 
				entryPath, credential, xp);
	}
	
	private S3FileSystem castFileSystem(FileSystem system) throws XenonException{
		if(!(system instanceof S3FileSystem)){
			throw new XenonException("s3", "Internal invariant broken: cannot cast filesystem to s3 system. This is a bug, please report!");
		}
		return (S3FileSystem)system;
	}
	
	private RelativePath checkS3Path(RelativePath p) throws InvalidPathException{
		if (p.getNameCount() < 2){
			throw new InvalidPathException("s3", "s3 paths must be \"<bucket-name>/<file-name>\"");
		}
		return p;
	}

	@Override
	public void close(FileSystem filesystem) throws XenonException {
		S3FileSystem system = castFileSystem(filesystem);
		// minio does not need closing

	}

	@Override
	public boolean isOpen(FileSystem filesystem) throws XenonException {
		S3FileSystem system = castFileSystem(filesystem);
		// minio client cannot open/close
		return true;
	}

	@Override
	public void move(Path source, Path target) throws XenonException {
		// TODO Auto-generated method stub

	}

	@Override
	public void createDirectory(Path dir) throws XenonException {
		// TODO Auto-generated method stub

	}

	@Override
	public void createFile(Path path) throws XenonException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Path path) throws XenonException {
		RelativePath p ;
		path.getRelativePath().getParent();

	}

	@Override
	public boolean exists(Path path) throws XenonException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir)
			throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter filter)
			throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(
			Path dir) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(
			Path dir, Filter filter) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream newInputStream(Path path) throws XenonException {
		S3FileSystem system = castFileSystem(path.getFileSystem());
		RelativePath p = checkS3Path(path.getRelativePath());
		String bucket = p.getName(0).toString();
		String fileName = p.subpath(1,p.getNameCount()).toString();
			try {
				return system.minioClient.getObject(bucket, fileName);
			} catch (InvalidBucketNameException e) {
				throw new NoSuchPathException("s3", "No such bucket : " + bucket);
			}catch (InvalidKeyException 
					| NoSuchAlgorithmException | InsufficientDataException
					| NoResponseException | ErrorResponseException
					| InternalException | InvalidArgumentException
					| IOException | XmlPullParserException e) {
				throw new XenonException("s3", e.getMessage());
			}

	}
	
	static class OutputEater implements Runnable{

		
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}

	@Override
	public OutputStream newOutputStream(Path path, OpenOption... options)
			throws XenonException {
		S3FileSystem system = castFileSystem(path.getFileSystem());
		RelativePath p = checkS3Path(path.getRelativePath());
		String bucket = p.getName(0).toString();
		String fileName = p.subpath(1,p.getNameCount()).toString();
			try {
				system.minioClient.putObject(bucket, ;
			} catch (InvalidBucketNameException e) {
				throw new NoSuchPathException("s3", "No such bucket : " + bucket);
			}catch (InvalidKeyException 
					| NoSuchAlgorithmException | InsufficientDataException
					| NoResponseException | ErrorResponseException
					| InternalException | InvalidArgumentException
					| IOException | XmlPullParserException e) {
				throw new XenonException("s3", e.getMessage());
			}
	}

	@Override
	public FileAttributes getAttributes(Path path) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path readSymbolicLink(Path link) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPosixFilePermissions(Path path,
			Set<PosixFilePermission> permissions) throws XenonException {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, String> getAdaptorSpecificInformation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void end() {
		// TODO Auto-generated method stub

	}

}
