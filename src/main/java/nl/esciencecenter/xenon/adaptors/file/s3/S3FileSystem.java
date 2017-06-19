package nl.esciencecenter.xenon.adaptors.file.s3;

import io.minio.MinioClient;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.files.FileSystemImplementation;
import nl.esciencecenter.xenon.files.RelativePath;

public class S3FileSystem extends FileSystemImplementation {

	final MinioClient minioClient;
	
	public S3FileSystem(MinioClient minioClient, String identifier, String scheme,
			String location, RelativePath entryPath, Credential credential,
			XenonProperties properties) {
		
		super(S3Properties.ADAPTOR_NAME, identifier, scheme, location, entryPath, credential,
				properties);
		this.minioClient = minioClient;
	}

}
