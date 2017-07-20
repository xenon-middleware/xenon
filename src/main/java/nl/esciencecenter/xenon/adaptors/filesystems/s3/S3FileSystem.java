package nl.esciencecenter.xenon.adaptors.filesystems.s3;

import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;
import nl.esciencecenter.xenon.UnsupportedOperationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.NotConnectedException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.filesystems.*;
import org.apache.commons.io.input.NullInputStream;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.ClientConfiguration;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.*;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

// We emulate the presence of (empty) directories by a file ending in a /
// this is the same behaviour as the official S3 console

public class S3FileSystem extends FileSystem {

    final String bucket;
    final AmazonS3 context;
    final String adaptorName;
    final String endPoint;

    public S3FileSystem(String uniqueID, String adaptorName, String endPoint, AmazonS3 context, String bucket, XenonProperties properties) {
        super(uniqueID,adaptorName,endPoint,new Path(""),properties);
        this.context = context;
        this.bucket = bucket;
        this.adaptorName = adaptorName;
        this.endPoint = endPoint;
    }

    @Override
    public void close() throws XenonException {
    }

    @Override
    public boolean isOpen() throws XenonException {
        return true;
    }

    @Override
    public void rename(Path source, Path target) throws XenonException {
        throw new UnsupportedOperationException(adaptorName,"This adaptor does not support renaming.");
    }

    void checkClosed() throws XenonException{
        if(!isOpen()){
            throw new NotConnectedException(getAdaptorName(), "Already closed file system!");
        }
    }

/*
    @Override
    protected void copyFile(Path source, FileSystem destinationFS, Path destination, CopyMode mode) throws XenonException{

        if(destinationFS instanceof  JCloudsFileSytem){
            JCloudsFileSytem dest = (JCloudsFileSytem)destinationFS;
            if( dest.endPoint.equals(endPoint) && dest.adaptorName.equals(adaptorName)){
                if (exists(destination)) {
                    switch (mode) {
                        case CREATE:
                            throw new PathAlreadyExistsException(getAdaptorName(), "Destination path already exists: " + destination);
                        case IGNORE:
                            return;
                        case REPLACE:
                            // continue
                            break;
                    }
                }
                context.getBlobStore().copyBlob(bucket,source.getRelativePath(),
                        dest.bucket,destination.getRelativePath(),CopyOptions.NONE);
                return;
            }
        }
        // revert to default copy
        super.copyFile(source,destinationFS,destination,mode);
    }
*/

    @Override
    public void createDirectory(Path dir) throws XenonException {
        checkClosed();
        if(exists(dir)){
            throw new PathAlreadyExistsException(adaptorName, "Cannot create directory, path already exists : " + dir.getRelativePath());
        }
        if(!exists(dir.getParent())){
            throw new NoSuchPathException(adaptorName, "Cannot create file, " + dir.getRelativePath() + ", parent directory " + dir.getParent().getRelativePath() + "does not exists.");
        }

        String path = dir.getRelativePath();
        if(path.endsWith("/")){
            path = path.substring(0,path.length()-1);
        }
        path = path + "/__not_empty__";
        context.putObject(bucket,path,"");
    }

    @Override
    public void createFile(Path file) throws XenonException {
        checkClosed();
        if(exists(file)){
            throw new PathAlreadyExistsException(adaptorName, "Cannot create file, path already exists : " + file.getRelativePath());
        }
        if(!exists(file.getParent())){
            throw new NoSuchPathException(adaptorName, "Cannot create file, " + file.getRelativePath() + ", parent directory " + file.getParent().getRelativePath() + "does not exists.");
        }
        String path = file.getRelativePath();
        context.putObject(bucket,path,"");
    }

    @Override
    public void createSymbolicLink(Path link, Path target) throws XenonException {
        throw new AttributeNotSupportedException(adaptorName, "Symbolic link  not supported by " + adaptorName);
    }



    @Override
    public void deleteFile(Path file) throws XenonException{
        checkClosed();
        //delete(file.getRelativePath());
    }

    @Override
    protected void deleteDirectory(Path dir) throws XenonException {
        checkClosed();
        String path = dir.getRelativePath();
        if(path.endsWith("/")){
            path = path.substring(0,path.length()-1);
        }
        //delete(path);
    }

    @Override
    protected Iterable<PathAttributes> listDirectory(Path dir) throws XenonException {
        return list(dir, false);
    }



    @Override
    public boolean exists(Path path) throws XenonException {
        checkClosed();
        String name = path.getRelativePath();
        boolean exists = context.doesObjectExist(bucket,name);
        if(!exists){
            ListObjectsRequest req = new ListObjectsRequest().withBucketName(bucket).withPrefix(name);
            ObjectListing list = context.listObjects(req);
            System.out.println(list.getCommonPrefixes().size());
            System.out.println(list.getObjectSummaries().size());
            return !list.getObjectSummaries().isEmpty() || !list.getCommonPrefixes().isEmpty();
        } else {
            return true;
        }


    }

/*
    PathAttributes toPathAttributes(final StorageMetadata m, final BlobAccess access){
        Path p = new Path(m.getName());

        PathAttributes pa = new PathAttributes();
        pa.setPath(p);
        Date d = m.getCreationDate();
        if(d != null){
            pa.setCreationTime(d.getTime());
        }
        d = m.getLastModified();
        if(d != null){
            pa.setLastModifiedTime(d.getTime());
        }
        pa.setReadable(true);
        pa.setWritable(true);
        /*
        Set<PosixFilePermission> permissions = new HashSet<>();
        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.OWNER_WRITE);
        if(access == BlobAccess.PUBLIC_READ){
            permissions.add(PosixFilePermission.OTHERS_READ);
           // if(isDir) { permissions.add(PosixFilePermission.OTHERS_EXECUTE); }
        }

        pa.setPermissions(null);
        return pa;
    }

    class ListingIterator implements Iterator<PathAttributes> {

        private final ListContainerOptions options;
        Iterator<? extends StorageMetadata> curIterator;
        PageSet<? extends StorageMetadata> curPageSet;
        StorageMetadata nxt;

        ListingIterator(ListContainerOptions options, PageSet<? extends StorageMetadata> pageSet){
            this.options = options;
            this.curPageSet = pageSet;
            this.curIterator = curPageSet.iterator();
            getNext();
        }

        void getNext(){
            if (!curIterator.hasNext() && curPageSet.getNextMarker() != null){
                curPageSet = context.getBlobStore().list(bucket,options.afterMarker(curPageSet.getNextMarker()));
                curIterator = curPageSet.iterator();
            }
            if(curIterator.hasNext()){
                nxt = curIterator.next();
            } else {
                nxt = null;
            }

        }

        @Override
        public boolean hasNext() { return  nxt != null; }

        @Override
        public PathAttributes next() {

            BlobAccess access = BlobAccess.PUBLIC_READ; // context.getBlobStore().getBlobAccess(bucket,nxt.getName());
            PathAttributes res = toPathAttributes(nxt,access);

            getNext();
            return res;
        }
    }
*/

    public Iterable<PathAttributes> list(Path dir, boolean recursive) throws XenonException{
        /*
        checkClosed();
        String name = dir.getRelativePath();
        Iterable<PathAttributes> pa = listPrefix(dir.getRelativePath() + "/",recursive);
        if(!pa.iterator().hasNext()){
            throw new NoSuchPathException(adaptorName, "No such directory: "  + dir.getRelativePath());
        }

        return pa;
        */
        return null;

    }
/*
    private Iterable<PathAttributes> listPrefix(String prefix,boolean recursive){
        ListContainerOptions options = new ListContainerOptions().prefix(prefix);

        if(recursive) { options = options.recursive(); }
        final ListContainerOptions optionsFinal = options;


        return new Iterable<PathAttributes>() {
            @Override
            public Iterator<PathAttributes> iterator() {
                return new nl.esciencecenter.xenon.adaptors.filesystems.jclouds.JCloudsFileSytem.ListingIterator(optionsFinal,context.getBlobStore().list(bucket,optionsFinal));
            }
        };}
*/
    @Override
    public InputStream readFromFile(Path path) throws XenonException {
        /*
        String name = path.getRelativePath();
        boolean exists = context.getBlobStore().blobExists(bucket,name);
        if(exists) {
            Blob b = context.getBlobStore().getBlob(bucket, path.getRelativePath());
            try {
                return b.getPayload().openStream();
            } catch (IOException e) {
                throw new XenonException(adaptorName, e.getMessage());
            }
        } else {
            throw new NoSuchPathException(adaptorName,"No such file: " + name);
        }
        */
        return null;
    }

    @Override
    public OutputStream writeToFile(Path path, long size) throws XenonException {
        /*
        final PipedInputStream read = new PipedInputStream();
        final Blob b = context.getBlobStore().blobBuilder(bucket).name(path.getRelativePath()).payload(read).contentLength(size).build();
        try {
            final OutputStream out = new PipedOutputStream(read);
            new Thread(new Runnable() {

                @Override
                public void run() {
                    context.getBlobStore().putBlob(bucket,b);
                }
            }).start();
            return out;
        } catch(IOException e){
            throw new XenonException(adaptorName,"IO error when trying to write: " + e.getMessage());
        }
        */
        return null;
    }

    @Override
    public OutputStream writeToFile(Path file) throws XenonException {
        throw new XenonException(adaptorName, "Sorry, this adaptor needs to know the size of a file before we start writing.");
    }

    @Override
    public OutputStream appendToFile(Path file) throws XenonException {
        throw new XenonException(adaptorName, "Sorry, this adaptor does not support appending.");
    }




    @Override
    public PathAttributes getAttributes(Path path) throws XenonException {
        /*
        if(!context.getBlobStore().blobExists(bucket,path.getRelativePath())){
            throw new NoSuchPathException(adaptorName, "File does not exist: " + path.getRelativePath());
        }
        BlobMetadata md = context.getBlobStore().blobMetadata(bucket,path.getRelativePath());
        BlobAccess access = BlobAccess.PUBLIC_READ; // context.getBlobStore().getBlobAccess(bucket, path.getRelativePath());
        return toPathAttributes(md,access);
        */
        return null;
    }

    @Override
    public Path readSymbolicLink(Path link) throws XenonException {
        throw new AttributeNotSupportedException(adaptorName, "Symbolic link  not supported by " + adaptorName);
    }

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
        throw new UnsupportedOperationException(getAdaptorName(), "This adaptor does not support POSIX permissions");
        /*
        checkClosed();
        if(!context.getBlobStore().blobExists(bucket,path.getRelativePath())){
            throw new NoSuchPathException(adaptorName,"Cannot set permissions, no such blob: " + path.getRelativePath());
        }
        String s = path.getRelativePath();

        boolean publicAccess = false;
        for(PosixFilePermission p : permissions){
            switch(p){
                case OTHERS_READ: publicAccess = true; break;
                default : break;
            }
        }
        BlobAccess ba = publicAccess ? BlobAccess.PUBLIC_READ : BlobAccess.PRIVATE;
        context.getBlobStore().setBlobAccess(bucket,s,ba);
        */
    }





}
