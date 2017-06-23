package nl.esciencecenter.xenon.adaptors.file.s3;



import static nl.esciencecenter.xenon.adaptors.file.sftp.SftpProperties.ADAPTOR_NAME;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import nl.esciencecenter.xenon.engine.files.*;
import nl.esciencecenter.xenon.files.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.files.DirectoryStream.Filter;

public class S3Files extends FileAdaptor {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(S3Files.class);

    private static int currentID = 1;

    private static synchronized String getNewUniqueID() {
        String res = "s3" + currentID;
        currentID++;
        return res;
    }
    
   
    /**
     * Used to store all state attached to a filesystem. This way, FileSystemImplementation is immutable.
     */
    static class FileSystemInfo {
        private final FileSystemImplementation impl;
        private final String bucket;
        private final String
        private final AmazonS3 client;
        boolean isShutdown;

		public FileSystemInfo(FileSystemImplementation impl, AmazonS3 client, String bucket) {
        	super();
        	this.impl = impl;
        	this.bucket = bucket;
        	this.client = client;
        	this.isShutdown = false;
        }

        FileSystemImplementation getImpl() {
        	return impl;
        }

        AmazonS3 getClient(){ return client; }

    }
    
    private final Map<String, FileSystemInfo> fileSystems = Collections.synchronizedMap(new HashMap<String, FileSystemInfo>());
    
	protected S3Files(FilesEngine engine, Map<String, String> properties) throws XenonException {
		super(engine, S3Properties.ADAPTOR_NAME, S3Properties.ADAPTOR_DESCRIPTION, S3Properties.ADAPTOR_SCHEME, S3Properties.ADAPTOR_LOCATIONS, S3Properties.VALID_PROPERTIES,
                new XenonProperties(S3Properties.VALID_PROPERTIES, Component.XENON, properties));
	}

	@Override
	public FileSystem newFileSystem(String location, Credential credential,
			Map<String, String> properties) throws XenonException {

		int split = location.lastIndexOf("/");
		if(split < 0){
			throw new InvalidPathException("s3","No bucket found in url: " + location);
		} 
		
		String server = location.substring(0,split);
		String bucket = location.substring(split + 1);
		
        XenonProperties xp = new XenonProperties(S3Properties.VALID_PROPERTIES, properties);
        
        if (!(credential instanceof PasswordCredential)){
        	throw new nl.esciencecenter.xenon.files.PermissionDeniedException("s3", "No secret key given for s3 connection.");
        }
		if(properties == null) { properties = new HashMap<>(); }
        String signingRegion = properties.get(S3Properties.SIGNING_REGION);
        if(signingRegion == null) { signingRegion = ""; }

        PasswordCredential c = (PasswordCredential)credential;
		AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
		builder.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(server,signingRegion));
		builder.setCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(c.getUsername(),new String(c.getPassword()))));
		AmazonS3 client = builder.build();



		if(!client.doesBucketExist(bucket)){
			throw new InvalidPathException("s3","Bucket does not exist: " + bucket);
		}

		

		String id = getNewUniqueID();
        RelativePath entryPath = new RelativePath("");
        
        FileSystemImplementation result = new FileSystemImplementation(S3Properties.ADAPTOR_NAME, id, "s3", location, 
                entryPath, credential, xp);
        

        fileSystems.put(id, new FileSystemInfo(result, client,bucket));
        return result;
	}




	@Override
	public void close(FileSystem filesystem) throws XenonException {
		FileSystemInfo info = getFileSystemInfo(filesystem);
		info.client.shutdown();
		info.isShutdown = true;

	}

	@Override
	public boolean isOpen(FileSystem filesystem) throws XenonException {
		FileSystemInfo info = getFileSystemInfo(filesystem);
		return !info.isShutdown;
	}

	@Override
	public void move(Path source, Path target) throws XenonException {
		FileSystemInfo infoSource = getFileSystemInfo(source.getFileSystem());
		FileSystemInfo targetSource = getFileSystemInfo(source.getFileSystem());


	}

	public Copy copySync(Path source, Path target, CopyOption... options) throws XenonException {
        FileSystemInfo infoSource = getFileSystemInfo(source.getFileSystem());
        FileSystemInfo targetSource = getFileSystemInfo(source.getFileSystem());
        if(!infoSource.impl.getLocation().equals(targetSource.impl.getLocation())){
            return super.copy(source,target);
        }
        CopyObjectResult res = infoSource.client.copyObject(infoSource.bucket,source.getRelativePath().getRelativePath(),
                targetSource.bucket, target.getRelativePath().getRelativePath());

    }

    public Copy copy(Path source, Path target, CopyOption... options) throws XenonException {
        F
        res.
    }

	@Override
	public void createDirectory(Path dir) throws XenonException {
		// No (real) directories in s3

	}

	FileSystemInfo getFileSystemInfo(FileSystem fs){
		FileSystemImplementation fsimp = (FileSystemImplementation) fs;
        return fileSystems.get(fsimp.getUniqueID());
	}
	
	@Override
	public void createFile(Path path) throws XenonException {
		FileSystemInfo info = getFileSystemInfo(path.getFileSystem());
		AmazonS3 client =  info.client;
        if (client.doesObjectExist(info.bucket,path.getRelativePath().getRelativePath())) {
            throw new PathAlreadyExistsException(ADAPTOR_NAME, "File " + path + " already exists!");
        }
        FileSystemInfo fsi = getFileSystemInfo(path.getFileSystem());
		client.putObject(info.bucket,path.getRelativePath().getRelativePath(),"");



	}

	@Override
	public void delete(Path path) throws XenonException {
		// TODO: What happens if file not exists? What if it was intended to be a directory? Should succeed?
		FileSystemInfo info = getFileSystemInfo(path.getFileSystem());
		AmazonS3 client =  info.client;
		String objectName = path.getRelativePath().getRelativePath();
		if(client.doesObjectExist(info.bucket, objectName)){
			client.deleteObject(info.bucket,objectName);
		}

	}

	@Override
	public boolean exists(Path path) throws XenonException {
		// Everything exists, since there are no directories in S3, and we want do not want to fail
		// if we query for existence of a directory?
        return true;
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir)
			throws XenonException {
		// Todo : This is the same for each adapter
		return newDirectoryStream(dir,DirectoryStream.filterNothing);
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter filter)
			throws XenonException {
	    final DirectoryStream<PathAttributesPair> st = newAttributesDirectoryStream(dir,filter);
        return new DirectoryStream<Path>() {
            @Override
            public Iterator<Path> iterator() {
                final Iterator<PathAttributesPair> pa = st.iterator();
                return new Iterator<Path>() {
                    @Override
                    public boolean hasNext() {
                        return pa.hasNext();
                    }

                    @Override
                    public Path next() {

                        return pa.next().path();
                    }

                    @Override
                    public void remove() { }
                };
            }

            @Override
            public void close() throws IOException {

            }
        };
	}

	@Override
	public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(
			Path dir) throws XenonException {
        // Todo : This is the same for each adapter
        return newAttributesDirectoryStream(dir, DirectoryStream.filterNothing);
	}

	@Override
	public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(
			final Path dir, final  Filter filter) throws XenonException {
        final FileSystemInfo info = getFileSystemInfo(dir.getFileSystem());
        final AmazonS3 client =  info.client;

        return new DirectoryStream<PathAttributesPair>() {
            @Override
            public Iterator<PathAttributesPair> iterator() {
                final ObjectListing list = client.listObjects(info.bucket,dir.getRelativePath().getRelativePath());
                return new ObjectIterator(info.getImpl(),info.client,list,filter);
            }

            @Override
            public void close() throws IOException {

            }
        };
	}

	@Override
	public InputStream newInputStream(Path path) throws XenonException {
        final FileSystemInfo info = getFileSystemInfo(path.getFileSystem());
        final AmazonS3 client =  info.client;
        S3Object obj = client.getObject(info.bucket,path.getRelativePath().getRelativePath());
        S3ObjectInputStream str = obj.getObjectContent();
        return str;
	}



    public OutputStream newOutputStream(final Path path, long size, OpenOption... options)
            throws XenonException {
        final FileSystemInfo info = getFileSystemInfo(path.getFileSystem());
        final AmazonS3 client =  info.client;
        final ObjectMetadata md = new ObjectMetadata();
        md.setContentLength(size);
	    try {
            final PipedInputStream read = new PipedInputStream();
            final OutputStream out = new PipedOutputStream(read);
            new Thread(new Runnable(){

                @Override
                public void run() {
                    client.putObject(info.bucket, path.getRelativePath().getRelativePath(), read, md);
                }
            }).start();
            return out;
        } catch (IOException e){
            // Todo: Handle this
        }
    }


	@Override
	public OutputStream newOutputStream(Path path, OpenOption... options)
			throws XenonException {
		return null;
	}

	@Override
	public FileAttributes getAttributes(Path path) throws XenonException {
		Iterator<PathAttributesPair> it = newAttributesDirectoryStream(path).iterator();
		if(!it.hasNext()){
		    throw new InvalidPathException("s3","Path for getAttributes does not exist");
        }
		return it.next().attributes();

	}

	@Override
	public Path readSymbolicLink(Path link) throws XenonException {
		return null;
	}

	@Override
	public void setPosixFilePermissions(Path path,
			Set<PosixFilePermission> permissions) throws XenonException {
		// No Posix with S3

	}

	@Override
	public Map<String, String> getAdaptorSpecificInformation() {
		// Todo: What is this?
        return null;
	}

	@Override
	public void end() {
		// Todo: This is a general thing, do not implement this for each adapter
		for (FileSystemInfo fsi : fileSystems.values()){
			FileSystem fs = fsi.getImpl();
			try {
				close(fs);
			} catch (XenonException e) {
				// ignore for now
			}

		}


	}

	class ObjectIterator implements Iterator<PathAttributesPair>{
	    ObjectListing listing;
	    Iterator<S3ObjectSummary> curIterator;
	    S3ObjectSummary s3Sum;
	    final FileSystem fs;
        final AmazonS3 client;
        final Filter filter;

	    ObjectIterator(FileSystem fs, AmazonS3 client, ObjectListing first, Filter filter){
            this.client = client;
            listing = first;
            this.fs = fs;
            this.filter = filter;
            nextListing();
            getNext();
        }

        void getNext() {
            do {
                if (!curIterator.hasNext()) {
                    if (listing.isTruncated()) {
                        System.out.println("MORE!!");
                        listing = client.listNextBatchOfObjects(listing);
                        nextListing();
                    } else {
                        s3Sum = null;
                        return;
                    }
                }
                s3Sum = curIterator.next();

            } while(!filter.accept(new PathImplementation(fs, new RelativePath(s3Sum.getKey()))));

        }

        void nextListing(){
	        curIterator = listing.getObjectSummaries().iterator();
        }

	    public boolean hasNext(){
	        return s3Sum != null;
        }

        public PathAttributesPair next(){
            if(s3Sum == null){
                throw new NoSuchElementException("Called next on directory stream while we have no next.");
            }
            S3ObjectSummary sumPrev = s3Sum;
            Path p = new PathImplementation(fs, new RelativePath(sumPrev.getKey()));
            getNext();
            return new PathAttributesPairImplementation(p,new S3FileAttributes(sumPrev));

        }

        public void remove(){ throw new Error("Cannot remove from this list."); }

    }


}
