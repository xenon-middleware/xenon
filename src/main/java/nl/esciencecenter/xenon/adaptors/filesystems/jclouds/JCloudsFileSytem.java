/*
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
package nl.esciencecenter.xenon.adaptors.filesystems.jclouds;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobAccess;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.options.ListContainerOptions;

import nl.esciencecenter.xenon.UnsupportedOperationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.NotConnectedException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.PathAttributesImplementation;
import nl.esciencecenter.xenon.filesystems.AttributeNotSupportedException;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.InvalidPathException;
import nl.esciencecenter.xenon.filesystems.NoSuchPathException;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.filesystems.PathAttributes;
import nl.esciencecenter.xenon.filesystems.PosixFilePermission;

// We emulate the presence of (empty) directories by a file ending in a /
// this is the same behaviour as the official S3 console

public class JCloudsFileSytem extends FileSystem {

    private static final String NOT_EMPTY = "___not__empty___";
    final String bucket;
    final BlobStoreContext context;
    final String adaptorName;
    final String endPoint;
    boolean open ;

    public JCloudsFileSytem(String uniqueID,String adaptorName, String endPoint, BlobStoreContext context, String bucket,  XenonProperties properties) {
        super(uniqueID,adaptorName,endPoint,new Path(""),properties);
        this.context = context;
        this.bucket = bucket;
        this.adaptorName = adaptorName;
        this.open = true;
        this.endPoint = endPoint;
    }

    @Override
    public void close() throws XenonException {
        checkClosed();
        context.close();
        open = false;
    }

    @Override
    public boolean isOpen() throws XenonException {
        return open;
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
        assertPathNotExists(dir);
        assertParentDirectoryExists(dir);
        makeNonEmptyFile(dir);
    }

    private void makeNonEmptyFile(Path dir) {
        String existsFile = dir +  "/" + NOT_EMPTY;
        final Blob b = context.getBlobStore().blobBuilder(bucket).name(existsFile).payload(new ByteArrayInputStream(new byte[]{})).contentLength(0).build();
        context.getBlobStore().putBlob(bucket,b);
    }

    private void removeNonEmptyFile(Path dir){
        if(dir == null){
            return ;
        }
        String existsFile = dir +  "/" + NOT_EMPTY;
        if(context.getBlobStore().blobExists(bucket,existsFile )){
            context.getBlobStore().removeBlob(bucket, existsFile);
        }
    }

    @Override
    public void createFile(Path file) throws XenonException {
        checkClosed();
        assertPathNotExists(file);
        assertParentDirectoryExists(file);
        removeNonEmptyFile(file.getParent());
        makeEmptyFile(file);
    }

    private void makeEmptyFile(Path file) {
        String path = file.toString();
        InputStream emtpy = new org.apache.sshd.common.util.io.NullInputStream();
        final Blob b = context.getBlobStore().blobBuilder(bucket).name(path).payload(emtpy).contentLength(0).build();
        context.getBlobStore().putBlob(bucket,b);
    }

    @Override
    public void createSymbolicLink(Path link, Path target) throws XenonException {
        throw new AttributeNotSupportedException(adaptorName, "Symbolic link  not supported by " + adaptorName);
    }


    @Override
    public void deleteFile(Path file) throws XenonException{
        checkClosed();
        context.getBlobStore().removeBlob(bucket,file.toString());

        ensureDirectoryExists(file.getParent());

    }

    private void ensureDirectoryExists(Path dir) throws XenonException {
        if(dir == null){
            return;
        }
        if(dir.equals(new Path(""))){
            return;             // do not put a not empty file at the root
        }
        if(!dirExists(dir)){
            makeNonEmptyFile(dir);
        }
    }

    @Override
    protected void deleteDirectory(Path dir) throws XenonException {
        checkClosed();
        // there are two options: the directory is empty or not.
        // in the former case the __not_empty__ file is deleted and no exception is thrown (correct behaviour)
        // in the latter case the __not_empty__ file is deleted and an exception is thrown (correct behaviour)

        if(!dirExists(dir)){
            throw new XenonException(adaptorName, "Cannot delete directory: " + dir + " not empty!");
        }
        removeNonEmptyFile(dir);

    }

    @Override
    protected Iterable<PathAttributes> listDirectory(Path dir) throws XenonException {
        return list(dir, false);
    }



    @Override
    public boolean exists(Path path) throws XenonException {
        assertNotNull(path);
        checkClosed();
        return dirExists(path) || fileExists(path) ;
    }

    private boolean dirExists(Path path) {
        String name = path + "/";
        ListContainerOptions options = new ListContainerOptions().prefix(name);
        return context.getBlobStore().list(bucket,options).iterator().hasNext();

    }

    private boolean fileExists(Path path) {
        return context.getBlobStore().blobExists(bucket,path.toString());
    }

    PathAttributes makeDirAttributes(final StorageMetadata m, final BlobAccess access){
        Path p = new Path(m.getName());
        PathAttributesImplementation pa = new PathAttributesImplementation();
        pa.setPath(p);
        if(m.getSize() != null) {
            pa.setSize(m.getSize());
        }
        Date d = m.getLastModified();
        if(d != null){
            pa.setLastModifiedTime(d.getTime());
        } else {
            pa.setLastModifiedTime(0);
        }
        d = m.getCreationDate();
        if(d != null){
            pa.setCreationTime(d.getTime());
        } else {
            pa.setCreationTime(pa.getCreationTime());
        }
        pa.setLastAccessTime(pa.getLastModifiedTime());
        pa.setDirectory(true);
        pa.setReadable(true);
        pa.setWritable(true);
        return pa;
    }

    PathAttributes toPathAttributes(final StorageMetadata m, final BlobAccess access){

        switch(m.getType()){
            case RELATIVE_PATH:
                System.out.println("Dir: " + m.getName());
                return makeDirAttributes(m,access);
            case BLOB:
                System.out.println("File: " + m.getName());
                return makeBlobAttributes(m.getName());
            default: throw new Error("Unknow file type" + m.getType());
        }



    }

    private PathAttributes makeBlobAttributes(String name) {
        BlobMetadata md = context.getBlobStore().blobMetadata(bucket,name);
        PathAttributesImplementation pa = new PathAttributesImplementation();
        pa.setPath(new Path(name));
        pa.setLastAccessTime(md.getLastModified().getTime());
        pa.setSize(md.getSize());
        pa.setRegular(true);
        if(md.getCreationDate() == null){
            pa.setCreationTime(pa.getLastModifiedTime());
        } else {
            pa.setCreationTime(md.getCreationDate().getTime());
        }
        pa.setLastAccessTime(md.getLastModified().getTime());
        pa.setDirectory(false);
        pa.setReadable(true);
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
            if(nxt != null && nxt.getName().endsWith(NOT_EMPTY)){

                getNext();
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

    public Iterator<PathAttributes> listNonRecursiveIterator(Path dir) {
        ListContainerOptions options = new ListContainerOptions().prefix(dir + "/");
        // JClouds on S3 does not list directories if recursive is set :( Fixing it ourselves
        //if(recursive) { options = options.recursive(); }
        final ListContainerOptions optionsFinal = options;


                return new ListingIterator(optionsFinal,   context.getBlobStore().list(bucket,optionsFinal));

    }


    public Iterable<PathAttributes> list(Path dir, boolean recursive) throws XenonException{
        checkClosed();
        assertPathIsDirectory(dir);
        ListContainerOptions options = new ListContainerOptions().prefix(dir +"/");
        // JClouds on S3 does not list directories if recursive is set :( Fixing it ourselves
        //if(recursive) { options = options.recursive(); }
        final ListContainerOptions optionsFinal = options;
        final PageSet<? extends StorageMetadata> ps = context.getBlobStore().list(bucket,optionsFinal);
        final Iterator<? extends StorageMetadata> curIt = ps.iterator();
        if(!curIt.hasNext()){
            if(context.getBlobStore().blobExists(bucket,dir.toString())){
                throw new InvalidPathException(getAdaptorName(), "Not a directory: " + dir);
            } else {
                throw new NoSuchPathException(adaptorName, "No such directory: " + dir);
            }
        }
        if(!recursive) {
            return new Iterable<PathAttributes>() {
                @Override
                public Iterator<PathAttributes> iterator() {
                    return listNonRecursiveIterator(dir);
                }
            };
        } else {
            return new Iterable<PathAttributes>() {
                @Override
                public Iterator<PathAttributes> iterator() {
                    return new RecursiveListIterator(listNonRecursiveIterator(dir));
                }
            };
        }


    }

    private Iterable<PathAttributes> listPrefix(String prefix, boolean recursive){
        ListContainerOptions options = new ListContainerOptions().prefix(prefix);

        if(recursive) { options = options.recursive(); }
        final ListContainerOptions optionsFinal = options;


        return new Iterable<PathAttributes>() {
            @Override
            public Iterator<PathAttributes> iterator() {
                return new ListingIterator(optionsFinal, context.getBlobStore().list(bucket,optionsFinal));
            }
        };
    }

    @Override
    public InputStream readFromFile(Path path) throws XenonException {
        assertPathIsFile(path);
        String name = path.toString();
        boolean exists = context.getBlobStore().blobExists(bucket,name);
        if(exists) {
            Blob b = context.getBlobStore().getBlob(bucket, path.toString());
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
        assertPathNotExists(path);
        final PipedInputStream read = new PipedInputStream();
        final Blob b = context.getBlobStore().blobBuilder(bucket).name(path.toString()).payload(read).contentLength(size).build();
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
        assertNotNull(path);
        String name = path.toString();
        for (PathAttributes p : listPrefix(name, false)) {
            if ( p.getPath().equals(path)) {
                return p;
            }
        }

        throw new NoSuchPathException(adaptorName, "File does not exist: " + path);

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


    private class RecursiveListIterator implements Iterator<PathAttributes> {
        final Stack<Iterator<PathAttributes>> stack;
        public RecursiveListIterator(Iterator<PathAttributes> root) {
            stack = new Stack<>();
            stack.push(root);
        }

        void popEmpties(){
            while(!stack.empty()){
                if(!stack.peek().hasNext()){
                    stack.pop();
                } else {
                    return;
                }
            }
        }


        @Override
        public boolean hasNext() {
            popEmpties();
            return !stack.isEmpty();
        }

        @Override
        public PathAttributes next() {
            PathAttributes nxt = stack.peek().next();
            if(nxt.isDirectory()){
                stack.push(listNonRecursiveIterator(nxt.getPath()));
            }
            popEmpties();
            return nxt;
        }
    }
}