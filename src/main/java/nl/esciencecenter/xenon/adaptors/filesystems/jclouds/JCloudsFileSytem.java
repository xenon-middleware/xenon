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

    boolean open;

    public JCloudsFileSytem(String uniqueID, String adaptorName, String endPoint, BlobStoreContext context, String bucket, XenonProperties properties) {
        super(uniqueID, adaptorName, endPoint, new Path("/"), properties);
        this.context = context;
        this.bucket = bucket;
        this.adaptorName = adaptorName;
        this.open = true;
        this.endPoint = endPoint;
    }

    String toBucketEntry(Path path) {

        assertNotNull(path);

        if (path.isAbsolute()) {
            return path.toRelativePath().toString();
        }

        return path.toString();
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
        throw new UnsupportedOperationException(adaptorName, "This adaptor does not support renaming.");
    }

    void checkClosed() throws XenonException {
        if (!isOpen()) {
            throw new NotConnectedException(getAdaptorName(), "Already closed file system!");
        }
    }

    @Override
    public void createDirectory(Path dir) throws XenonException {
        checkClosed();

        Path absDir = toAbsolutePath(dir);

        assertPathNotExists(absDir);
        assertParentDirectoryExists(absDir);

        makeDirectoryPlaceholder(absDir);
    }

    // Simulate creating an empty directory by creating a bucket entry with the name "dir/___not__empty___"
    private void makeDirectoryPlaceholder(Path dir) {
        Blob b = context.getBlobStore().blobBuilder(bucket).name(toBucketEntry(dir) + "/" + NOT_EMPTY).payload(new ByteArrayInputStream(new byte[] {}))
                .contentLength(0).build();
        context.getBlobStore().putBlob(bucket, b);
    }

    // Remove a simulated directory
    private void removeDirectoryPlaceholder(Path dir) {
        if (dir == null) {
            return;
        }
        String existsFile = toBucketEntry(dir) + "/" + NOT_EMPTY;
        if (context.getBlobStore().blobExists(bucket, existsFile)) {
            context.getBlobStore().removeBlob(bucket, existsFile);
        }
    }

    @Override
    public void createFile(Path file) throws XenonException {
        checkClosed();

        Path absFile = toAbsolutePath(file);

        assertPathNotExists(absFile);
        assertParentDirectoryExists(absFile);

        // If needed, remove the empty directory placeholder
        removeDirectoryPlaceholder(absFile.getParent());

        // Create an empty file by adding an empty blob in the bucket.
        InputStream emtpy = new org.apache.sshd.common.util.io.NullInputStream();
        final Blob b = context.getBlobStore().blobBuilder(bucket).name(toBucketEntry(absFile)).payload(emtpy).contentLength(0).build();
        context.getBlobStore().putBlob(bucket, b);
    }

    @Override
    public void createSymbolicLink(Path link, Path target) throws XenonException {
        throw new AttributeNotSupportedException(adaptorName, "Symbolic link  not supported by " + adaptorName);
    }

    // Ensure that the specified directory exists by
    private void ensureDirectoryExists(Path dir) {
        if (!dirExists(dir)) {
            makeDirectoryPlaceholder(dir);
        }
    }

    @Override
    public void deleteFile(Path file) throws XenonException {
        checkClosed();

        Path absFile = toAbsolutePath(file);

        context.getBlobStore().removeBlob(bucket, toBucketEntry(absFile));

        // Ensure that the parent directory remains after the last file is deleted by inserting a placeholder.
        Path parent = absFile.getParent();

        if (parent != null && !parent.isEmpty()) {
            ensureDirectoryExists(absFile.getParent());
        }
    }

    @Override
    protected void deleteDirectory(Path dir) throws XenonException {
        checkClosed();
        // there are two options: the directory is empty or not.
        // in the former case the __not_empty__ file is deleted and no exception
        // is thrown (correct behaviour)
        // in the latter case the __not_empty__ file is deleted and an exception
        // is thrown (correct behaviour)
        if (!dirExists(dir)) {
            // TODO: shouldn't this be a NoSuchPathException ?
            throw new NoSuchPathException(adaptorName, "Directory does not exist: " + dir);
        }

        removeDirectoryPlaceholder(dir);

    }

    @Override
    protected Iterable<PathAttributes> listDirectory(Path dir) throws XenonException {
        return list(dir, false);
    }

    // Test if a directory exists by listing the bucket entry (followed by a
    // trailing slash).
    // If at least one element is returned, it is a directory
    private boolean dirExists(Path path) {
        ListContainerOptions options = new ListContainerOptions().prefix(toBucketEntry(path) + "/");
        return context.getBlobStore().list(bucket, options).iterator().hasNext();
    }

    // Test if a file exists by checking if the blob exists.
    private boolean fileExists(Path path) {
        return context.getBlobStore().blobExists(bucket, toBucketEntry(path));
    }

    @Override
    public boolean exists(Path path) throws XenonException {
        checkClosed();

        Path absPath = toAbsolutePath(path);

        return dirExists(absPath) || fileExists(absPath);
    }

    PathAttributes makeDirAttributes(final StorageMetadata m, final BlobAccess access) {

        PathAttributesImplementation pa = new PathAttributesImplementation();

        // Jclouds alway returns a name relative to the bucket, so we make it absolute first.
        pa.setPath(new Path("/" + m.getName()));

        if (m.getSize() != null) {
            pa.setSize(m.getSize());
        }
        Date d = m.getLastModified();
        if (d != null) {
            pa.setLastModifiedTime(d.getTime());
        } else {
            pa.setLastModifiedTime(0);
        }
        d = m.getCreationDate();
        if (d != null) {
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

    private PathAttributes makeBlobAttributes(String name) {

        BlobMetadata md = context.getBlobStore().blobMetadata(bucket, name);

        PathAttributesImplementation pa = new PathAttributesImplementation();

        // Jclouds alway returns a name relative to the bucket, so we make it absolute first.
        pa.setPath(new Path("/" + name));
        pa.setLastAccessTime(md.getLastModified().getTime());
        pa.setSize(md.getSize());
        pa.setRegular(true);
        if (md.getCreationDate() == null) {
            pa.setCreationTime(pa.getLastModifiedTime());
        } else {
            pa.setCreationTime(md.getCreationDate().getTime());
        }
        pa.setLastAccessTime(md.getLastModified().getTime());
        pa.setDirectory(false);
        pa.setReadable(true);
        return pa;
    }

    PathAttributes toPathAttributes(final StorageMetadata m, final BlobAccess access) {

        switch (m.getType()) {
        case RELATIVE_PATH:
            return makeDirAttributes(m, access);
        case BLOB:
            return makeBlobAttributes(m.getName());
        default:
            throw new RuntimeException("Unknow file type" + m.getType());
        }
    }

    class ListingIterator implements Iterator<PathAttributes> {

        private final ListContainerOptions options;
        Iterator<? extends StorageMetadata> curIterator;
        PageSet<? extends StorageMetadata> curPageSet;
        StorageMetadata nxt;

        ListingIterator(ListContainerOptions options, PageSet<? extends StorageMetadata> pageSet) {
            this.options = options;
            this.curPageSet = pageSet;
            this.curIterator = curPageSet.iterator();

            getNext();
        }

        void getNext() {
            if (!curIterator.hasNext() && curPageSet.getNextMarker() != null) {
                curPageSet = context.getBlobStore().list(bucket, options.afterMarker(curPageSet.getNextMarker()));
                curIterator = curPageSet.iterator();
            }
            if (curIterator.hasNext()) {
                nxt = curIterator.next();
            } else {
                nxt = null;
            }
            if (nxt != null && nxt.getName().endsWith(NOT_EMPTY)) {
                getNext();
            }
        }

        @Override
        public boolean hasNext() {
            return nxt != null;
        }

        @Override
        public PathAttributes next() {

            BlobAccess access = BlobAccess.PUBLIC_READ;
            PathAttributes res = toPathAttributes(nxt, access);
            getNext();
            return res;
        }
    }

    Iterator<PathAttributes> listNonRecursiveIterator(String bucketEntry) {
        ListContainerOptions options = new ListContainerOptions().prefix(bucketEntry + "/");
        // JClouds on S3 does not list directories if recursive is set :( Fixing it ourselves
        // if (recursive) { options = options.recursive(); }
        final ListContainerOptions optionsFinal = options;
        return new ListingIterator(optionsFinal, context.getBlobStore().list(bucket, optionsFinal));
    }

    @Override
    public Iterable<PathAttributes> list(Path path, boolean recursive) throws XenonException {
        checkClosed();

        final Path dir = toAbsolutePath(path);

        assertPathIsDirectory(dir);

        final String bucketEntry = toBucketEntry(dir);

        final ListContainerOptions options = new ListContainerOptions().prefix(bucketEntry + "/");

        // JClouds on S3 does not list directories if recursive is set :( Fixing
        // it ourselves
        // if (recursive) { options = options.recursive(); }
        // final ListContainerOptions optionsFinal = options;
        final PageSet<? extends StorageMetadata> ps = context.getBlobStore().list(bucket, options);
        final Iterator<? extends StorageMetadata> curIt = ps.iterator();

        if (!curIt.hasNext()) {
            if (context.getBlobStore().blobExists(bucket, bucketEntry)) {
                throw new InvalidPathException(getAdaptorName(), "Not a directory: " + dir);
            } else {
                throw new NoSuchPathException(adaptorName, "No such directory: " + dir);
            }
        }

        if (!recursive) {
            return new Iterable<PathAttributes>() {
                @Override
                public Iterator<PathAttributes> iterator() {
                    return listNonRecursiveIterator(bucketEntry);
                }
            };
        } else {
            return new Iterable<PathAttributes>() {
                @Override
                public Iterator<PathAttributes> iterator() {
                    return new RecursiveListIterator(listNonRecursiveIterator(bucketEntry));
                }
            };
        }
    }

    private Iterable<PathAttributes> listPrefix(String prefix, boolean recursive) {
        ListContainerOptions options = new ListContainerOptions().prefix(prefix);

        if (recursive) {
            options = options.recursive();
        }

        final ListContainerOptions optionsFinal = options;

        return new Iterable<PathAttributes>() {
            @Override
            public Iterator<PathAttributes> iterator() {
                return new ListingIterator(optionsFinal, context.getBlobStore().list(bucket, optionsFinal));
            }
        };
    }

    @Override
    public InputStream readFromFile(Path path) throws XenonException {

        Path absPath = toAbsolutePath(path);

        assertPathIsFile(absPath);

        try {
            return context.getBlobStore().getBlob(bucket, toBucketEntry(absPath)).getPayload().openStream();
        } catch (IOException e) {
            throw new XenonException(adaptorName, e.getMessage());
        }
    }

    @Override
    public OutputStream writeToFile(Path path, long size) throws XenonException {

        Path absPath = toAbsolutePath(path);
        assertPathNotExists(absPath);

        final PipedInputStream read = new PipedInputStream();
        final Blob b = context.getBlobStore().blobBuilder(bucket).name(toBucketEntry(absPath)).payload(read).contentLength(size).build();
        try {
            final OutputStream out = new PipedOutputStream(read);
            new Thread(new Runnable() {

                @Override
                public void run() {
                    context.getBlobStore().putBlob(bucket, b);
                }
            }).start();
            return out;
        } catch (IOException e) {
            throw new XenonException(adaptorName, "IO error when trying to write: " + e.getMessage());
        }
    }

    @Override
    public OutputStream writeToFile(Path file) throws XenonException {
        throw new UnsupportedOperationException(adaptorName, "WriteToFile without predefined size not supported");
    }

    @Override
    public OutputStream appendToFile(Path file) throws XenonException {
        throw new UnsupportedOperationException(adaptorName, "Append not supported");
    }

    @Override
    public PathAttributes getAttributes(Path path) throws XenonException {

        Path absPath = toAbsolutePath(path);

        String name = toBucketEntry(absPath);

        for (PathAttributes p : listPrefix(name, false)) {
            if (p.getPath().equals(absPath)) {
                return p;
            }
        }

        throw new NoSuchPathException(adaptorName, "File does not exist: " + absPath);
    }

    @Override
    public Path readSymbolicLink(Path link) throws XenonException {
        throw new AttributeNotSupportedException(adaptorName, "Symbolic link  not supported by " + adaptorName);
    }

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
        throw new UnsupportedOperationException(getAdaptorName(), "POSIX permissions not supported");
    }

    private class RecursiveListIterator implements Iterator<PathAttributes> {
        final Stack<Iterator<PathAttributes>> stack;

        public RecursiveListIterator(Iterator<PathAttributes> root) {
            stack = new Stack<>();
            stack.push(root);
        }

        void popEmpties() {
            while (!stack.empty()) {
                if (!stack.peek().hasNext()) {
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
            if (nxt.isDirectory()) {
                stack.push(listNonRecursiveIterator(toBucketEntry(nxt.getPath())));
            }
            popEmpties();
            return nxt;
        }
    }
}
