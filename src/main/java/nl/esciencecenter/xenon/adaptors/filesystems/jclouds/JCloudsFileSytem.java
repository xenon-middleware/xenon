package nl.esciencecenter.xenon.adaptors.filesystems.jclouds;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.filesystems.*;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.options.CopyOptions;
import org.jclouds.blobstore.options.ListContainerOptions;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by atze on 29-6-17.
 */

// TODO: Fix exceptions!
public class JCloudsFileSytem extends FileSystem {

    final String bucket;
    final BlobStoreContext context;
    final String adaptorName;
    final String endPoint;
    boolean open ;

    public JCloudsFileSytem(String uniqueID,String adaptorName, String endPoint, BlobStoreContext context, String bucket, XenonProperties properties) {
        super(uniqueID,adaptorName,endPoint,new Path(""),properties);
        this.context = context;
        this.bucket = bucket;
        this.adaptorName = adaptorName;
        this.open = true;
        this.endPoint = endPoint;
    }

    @Override
    public void close() throws XenonException {
        context.close();
        open = false;
    }

    @Override
    public boolean isOpen() throws XenonException {
        return open;
    }

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


    @Override
    public void createDirectory(Path dir) throws XenonException {
        // No true directories in a blobstore, so everything exists

    }

    @Override
    public void createFile(Path file) throws XenonException {
        // everything exists? do nothing?
    }

    @Override
    public void deleteFile(Path path) throws XenonException{
        String name = path.getRelativePath();
        boolean exists = context.getBlobStore().blobExists(bucket,name);
        if(exists){
            context.getBlobStore().removeBlob(bucket,name);
        }
    }

    @Override
    protected void deleteDirectory(Path path) throws XenonException {
        // no directories
    }

    @Override
    protected Iterable<PathAttributes> listDirectory(Path dir) throws XenonException {
        return list(dir, false);
    }


    @Override
    public boolean exists(Path path) throws XenonException {
        // Everything exists, since there are no directories in a blobstore, and we want do not want to fail
        // if we query for existence of a directory?
        return true;
    }


    PathAttributes toPathAttributes(final StorageMetadata m){
        Path p = new Path(m.getName());
        PathAttributes pa = new PathAttributes();
        pa.setPath(p);
        pa.setCreationTime(m.getCreationDate().getTime());
        pa.setLastModifiedTime(m.getLastModified().getTime());
        pa.setReadable(true);
        pa.setWritable(true);
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
            PathAttributes res = toPathAttributes(nxt);
            getNext();
            return res;
        }
    }



    public Iterable<PathAttributes> list(Path dir, boolean recursive){
        ListContainerOptions options = new ListContainerOptions().prefix(dir.getRelativePath());
        if(recursive) { options = options.recursive(); }
        final ListContainerOptions optionsFinal = options;

        return new Iterable<PathAttributes>() {
            @Override
            public Iterator<PathAttributes> iterator() {
                return new ListingIterator(optionsFinal,context.getBlobStore().list(bucket,optionsFinal));
            }
        };

    }

    @Override
    public InputStream readFromFile(Path path) throws XenonException {
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
    }

    @Override
    public OutputStream writeToFile(Path path, long size) throws XenonException {
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
        BlobMetadata md = context.getBlobStore().blobMetadata(bucket,path.getRelativePath());
        return toPathAttributes(md);
    }

    @Override
    public Path readSymbolicLink(Path link) throws XenonException {
        throw new AttributeNotSupportedException(adaptorName, "Symbolic link  not supported by " + adaptorName);
    }

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
        throw new AttributeNotSupportedException(adaptorName, "Posix file permissions not supported by " + adaptorName);
    }





}
