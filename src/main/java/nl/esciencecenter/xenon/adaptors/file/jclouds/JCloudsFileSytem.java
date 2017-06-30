package nl.esciencecenter.xenon.adaptors.file.jclouds;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.files.*;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.options.CopyOptions;
import org.jclouds.blobstore.options.ListContainerOptions;

import java.io.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by atze on 29-6-17.
 */
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

    public void copySync(CopyDescription description){
        // this is interface is wrong
        if(description.getSourceFileSystem() instanceof  JCloudsFileSytem){
            JCloudsFileSytem source = (JCloudsFileSytem)description.getSourceFileSystem();
            if( source.endPoint.equals(endPoint) && source.adaptorName.equals(adaptorName)){
                context.getBlobStore().copyBlob(source.bucket,description.getSourcePath().getRelativePath(),
                        bucket,description.getDestinationPath().getRelativePath(),CopyOptions.NONE);
                return;
            }
        }
        // Todo: Do Jason magic here
    }

    @Override
    public CopyHandle copy(CopyDescription description) throws XenonException {
        // Todo: Do async Jason magic here
        return null;
        //super.copy(description);
    }

    @Override
    public void move(Path source, Path target) throws XenonException {
        // Todo: use default implementation here
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
    public void delete(Path path) throws XenonException {
        String name = path.getRelativePath();
        boolean exists = context.getBlobStore().blobExists(bucket,name);
        if(exists){
            context.getBlobStore().removeBlob(bucket,name);
        }
    }

    @Override
    public boolean exists(Path path) throws XenonException {
        // Everything exists, since there are no directories in a blobstore, and we want do not want to fail
        // if we query for existence of a directory?
        return true;
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter filter) throws XenonException {
        // see list
        return null;
    }

    PathAttributesPair toPathAttributesPair(final StorageMetadata m){
        Path p = new Path(m.getName());
        return new PathAttributesPair(p, new FileAttributes() {
            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public boolean isOther() {
                return false;
            }

            @Override
            public boolean isRegularFile() {
                return true;
            }

            @Override
            public boolean isSymbolicLink() {
                return false;
            }

            @Override
            public long creationTime() {
                return m.getCreationDate().getTime();
            }

            @Override
            public long lastAccessTime() {
                // not supported
                return 0;
            }

            @Override
            public long lastModifiedTime() {
                return m.getLastModified().getTime();
            }

            @Override
            public long size() {
                Long size = m.getSize();
                if(size == null){
                    return 0;
                } else {
                    return size.longValue();
                }
            }

            @Override
            public boolean isExecutable() {
                return false;
            }

            @Override
            public boolean isHidden() {
                return false;
            }

            @Override
            public boolean isReadable() {
                return true;
            }

            @Override
            public boolean isWritable() {
                return false;
            }

            @Override
            public String group() throws AttributeNotSupportedException {
                throw new AttributeNotSupportedException(adaptorName, "Group attribute not supported by " + adaptorName);
            }

            @Override
            public String owner() throws AttributeNotSupportedException {
                throw new AttributeNotSupportedException(adaptorName, "Owner attribute not supported by " + adaptorName);
            }

            @Override
            public Set<PosixFilePermission> permissions() throws AttributeNotSupportedException {
                throw new AttributeNotSupportedException(adaptorName, "Posix file permissions not supported by " + adaptorName);
            }
        });
    }

    class ListingIterator implements Iterator<PathAttributesPair> {

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
        public PathAttributesPair next() {
            PathAttributesPair res = toPathAttributesPair(nxt);
            getNext();
            return res;
        }
    }



    public Iterable<PathAttributesPair> list(Path dir, boolean recursive){
        ListContainerOptions options = new ListContainerOptions().prefix(dir.getRelativePath());
        if(recursive) { options = options.recursive(); }
        final ListContainerOptions optionsFinal = options;

        return new Iterable<PathAttributesPair>() {
            @Override
            public Iterator<PathAttributesPair> iterator() {
                return new ListingIterator(optionsFinal,context.getBlobStore().list(bucket,optionsFinal));
            }
        };

    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir, DirectoryStream.Filter filter) throws XenonException {
        // see list
        return null;
    }

    @Override
    public InputStream newInputStream(Path path) throws XenonException {
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


    public OutputStream newOutputStream(Path path, long size, OpenOption... options) throws XenonException {
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
            throw new XenonException(adaptorName,"IO error when trying to read: " + e.getMessage());
        }

    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws XenonException {
        final PipedInputStream read = new PipedInputStream();
        final Blob b = context.getBlobStore().blobBuilder(bucket).name(path.toString()).payload(read).build();
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
            throw new XenonException(adaptorName,"IO error when trying to read: " + e.getMessage());
        }

    }

    @Override
    public FileAttributes getAttributes(Path path) throws XenonException {
        BlobMetadata md = context.getBlobStore().blobMetadata(bucket,path.getRelativePath());
        return toPathAttributesPair(md).attributes();
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
