package nl.esciencecenter.xenon.adaptors.filesystems.hdfs;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.NotConnectedException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.PathAttributesImplementation;
import nl.esciencecenter.xenon.adaptors.filesystems.RecursiveListIterator;
import nl.esciencecenter.xenon.filesystems.*;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.InvalidPathException;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import nl.esciencecenter.xenon.filesystems.Path;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

public class HDFSFileSystem extends nl.esciencecenter.xenon.filesystems.FileSystem{
    final org.apache.hadoop.fs.FileSystem fs;
    boolean closed;

    protected HDFSFileSystem(String uniqueID, String endPoint, org.apache.hadoop.fs.FileSystem fs,  int bufferSize, XenonProperties properties) {
        super(uniqueID,"hdfs",endPoint,fromHDFSPath(fs.getWorkingDirectory()),bufferSize,properties);
        this.fs = fs;
        closed = false;
    }


    void checkClosed() throws XenonException{
        if(!isOpen()){
            throw new NotConnectedException(getAdaptorName(), "Already closed file system!");
        }
    }

    @Override
    public void close() throws XenonException {
        checkClosed();
        try {
            fs.close();
            closed = true;
        } catch (IOException e){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }
    }

    @Override
    public boolean isOpen() throws XenonException {
        return !closed;
    }

    @Override
    public void rename(Path source, Path target) throws XenonException {
        checkClosed();
        assertPathExists(source);
        if(source.equals(target)){
            return;
        }
        assertPathNotExists(target);
        assertParentDirectoryExists(target);
        try {
            fs.rename(toHDFSPath(source), toHDFSPath(target));
        } catch(IOException e ){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }
    }





    @Override
    public void createDirectory(Path dir) throws XenonException {
        checkClosed();
        assertNotNull(dir);
        assertParentDirectoryExists(dir);
        assertPathNotExists(dir);
        try {
            fs.mkdirs(toHDFSPath(dir));
        } catch(IOException e ){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }

    }


    @Override
    public void createFile(Path file) throws XenonException {
        checkClosed();
        assertPathNotExists(file);
        assertParentDirectoryExists(file);
        try {
            fs.createNewFile(toHDFSPath(file));
        } catch(IOException e ){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }
    }

    @Override
    public void createSymbolicLink(Path link, Path target) throws XenonException {
        checkClosed();
        assertPathNotExists(link);
        assertPathExists(target);
        try {
            fs.createSymlink(toHDFSPath(target), toHDFSPath(link), false);
        } catch(IOException e ){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }

    }

    @Override
    public void delete(Path path, boolean recursive) throws XenonException {
        checkClosed();
        assertPathExists(path);
        if(!recursive){
            PathAttributes pa = getAttributes(path);
            if(pa.isDirectory() && list(path,false).iterator().hasNext()){
                throw new DirectoryNotEmptyException(getAdaptorName(), "Cannot delete directory: not empty");
            }
        }
        try {
            fs.delete(toHDFSPath(path), recursive);
        } catch(IOException e ){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }
    }



    @Override
    public boolean exists(Path path) throws XenonException {
        checkClosed();
        assertNotNull(path);
        try {
            return fs.exists(toHDFSPath(path));
        } catch(IOException e ){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }
    }


    public Iterable<PathAttributes> list(final Path dir,  boolean recursive) throws XenonException {
        checkClosed();
        assertDirectoryExists(dir);
        if(!recursive) {
            return new Iterable<PathAttributes>() {
                @Override
                public Iterator<PathAttributes> iterator() {
                    return listIteratorNoException(dir);
                }
            };
        } else {
            return new Iterable<PathAttributes>() {
                @Override
                public Iterator<PathAttributes> iterator(){
                    return new RecursiveListIterator(new Function<Path, Iterator<PathAttributes>>() {
                        @Override
                        public Iterator<PathAttributes> apply(Path path) {
                            return listIteratorNoException(path);
                        }
                    }, dir);
                }
            };

        }
    }

    private Iterator<PathAttributes> listIteratorNoException(Path dir){
        try {
            return listIterator(dir);
        } catch (XenonException e) {
            throw new Error(e.getMessage());
        }

    }

    private Iterator<PathAttributes> listIterator(Path dir) throws XenonException {
        checkClosed();
        final RemoteIterator<LocatedFileStatus> it;
        try {
            it = fs.listLocatedStatus(toHDFSPath(dir));
        } catch(IOException e ){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);

        }
        return new Iterator<PathAttributes>() {
            @Override
            public boolean hasNext() {
                try{
                    return it.hasNext();
                } catch(IOException e){
                    throw new Error(e.getMessage());
                }

            }

            @Override
            public PathAttributes next() {
                try{
                    return toPathAttributes(it.next());
                } catch(IOException e){
                    throw new Error(e.getMessage());
                }
            }
        };
    }


    @Override
    public InputStream readFromFile(Path path) throws XenonException {
        checkClosed();
        assertFileExists(path);
        try {
            org.apache.hadoop.fs.Path p = toHDFSPath(path);
            if(!fs.isFile(p)){
                throw new InvalidPathException(getAdaptorName(), "Cannot read from file " + path.toString() + ". Not a regular file");
            }
            return fs.open(toHDFSPath(path));
        } catch (IOException e){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }
    }

    @Override
    public OutputStream writeToFile(Path path, long size) throws XenonException {
        return writeToFile(path);
    }

    @Override
    public OutputStream writeToFile(Path path) throws XenonException {
        checkClosed();
        assertNotNull(path);
        assertPathNotExists(path);

        try {
            return fs.create(toHDFSPath(path));
        } catch (IOException e){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }
    }

    @Override
    public OutputStream appendToFile(Path path) throws XenonException {
        checkClosed();
        assertPathIsFile(path);
        try {
            org.apache.hadoop.fs.Path p = toHDFSPath(path);
            return fs.append(p);
        } catch (IOException e){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }
    }

    @Override
    public PathAttributes getAttributes(Path path) throws XenonException {
        checkClosed();
        assertPathExists(path);
        try {
            return toPathAttributes(fs.getFileStatus(toHDFSPath(path)));
        } catch(IOException e){
            throw new XenonException(getAdaptorName(), "Cannot get file attributes", e);
        }

    }

    @Override
    protected void deleteFile(nl.esciencecenter.xenon.filesystems.Path file) throws XenonException {
        // not needed...
        throw new Error("Cannot happen!?");
    }

    @Override
    protected void deleteDirectory(nl.esciencecenter.xenon.filesystems.Path path) throws XenonException {
        // not needed...
        throw new Error("Cannot happen!?");
    }

    @Override
    protected Iterable<PathAttributes> listDirectory(Path dir) throws XenonException {
        return list(dir,false);
    }

    org.apache.hadoop.fs.Path toHDFSPath(Path p){
        return new org.apache.hadoop.fs.Path(toAbsolutePath(p).toString());
    }

    static Path fromHDFSPath(org.apache.hadoop.fs.Path p){
        return new Path(org.apache.hadoop.fs.Path.getPathWithoutSchemeAndAuthority(p).toString());
    }



    private PathAttributes toPathAttributes(FileStatus s){
        PathAttributesImplementation res = new PathAttributesImplementation();
        res.setPath(fromHDFSPath(s.getPath()));
        res.setDirectory(s.isDirectory());
        res.setOther(false); // cannot happen in hdfs? not supported by API
        res.setRegular(s.isFile());
        res.setSymbolicLink(s.isSymlink());
        if(s.getAccessTime() == 0){
            res.setLastAccessTime(s.getModificationTime());
        } else {
            res.setLastAccessTime(s.getAccessTime());
        }
        res.setCreationTime(s.getModificationTime());
        res.setLastModifiedTime(s.getModificationTime());
        res.setSize(s.getLen());
        res.setExecutable(s.getPermission().getUserAction().implies(FsAction.EXECUTE));
        res.setHidden(s.getPath().getName().startsWith("."));
        res.setReadable(s.getPermission().getUserAction().implies(FsAction.READ));
        res.setWritable(s.getPermission().getUserAction().implies(FsAction.WRITE));
        res.setGroup(s.getGroup());
        res.setOwner(s.getOwner());
        res.setPermissions(toPermissionSet(s.getPermission()));
        return res;
    }

    private Set<PosixFilePermission> toPermissionSet(FsPermission permission) {
        Set<PosixFilePermission> p = new HashSet<>();
        FsAction owner = permission.getUserAction();
        FsAction group = permission.getGroupAction();
        FsAction other = permission.getOtherAction();
        if(group.implies(FsAction.READ)){
            p.add(PosixFilePermission.OWNER_READ);
        }
        if(owner.implies(FsAction.WRITE)){
            p.add(PosixFilePermission.OWNER_WRITE);
        }
        if(owner.implies(FsAction.EXECUTE)){
            p.add(PosixFilePermission.OWNER_EXECUTE);
        }
        if(group.implies(FsAction.READ)){
            p.add(PosixFilePermission.GROUP_READ);
        }
        if(group.implies(FsAction.WRITE)){
            p.add(PosixFilePermission.GROUP_WRITE);
        }
        if(group.implies(FsAction.EXECUTE)){
            p.add(PosixFilePermission.GROUP_EXECUTE);
        }
        if(other.implies(FsAction.READ)){
            p.add(PosixFilePermission.OTHERS_READ);
        }
        if(other.implies(FsAction.WRITE)){
            p.add(PosixFilePermission.OTHERS_WRITE);
        }
        if(other.implies(FsAction.EXECUTE)){
            p.add(PosixFilePermission.OTHERS_EXECUTE);
        }
        return p;
    }

    @Override
    public Path readSymbolicLink(Path link) throws XenonException {
        checkClosed();

        try {
            org.apache.hadoop.fs.Path p  = toHDFSPath(link);
            if(!fs.exists(p)){
                throw new NoSuchPathException(getAdaptorName(), "Cannot resolve link " + link.toString() + ": No such file or symlink");
            }
            PathAttributes pa = getAttributes(link);
            if(!pa.isSymbolicLink()) {
                throw new InvalidPathException(getAdaptorName(), "Cannot resolve link " + link.toString() + ": Not a symlink");
            }
            return fromHDFSPath(fs.resolvePath(toHDFSPath(link)));
        } catch (IOException e){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }

    }

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
        checkClosed();
        if(!exists(path)){
            throw new NoSuchPathException(getAdaptorName(), "No such path :" + path.toString());
        }
        FsAction user  = FsAction.NONE;
        FsAction group = FsAction.NONE;
        FsAction other = FsAction.NONE;
        for(PosixFilePermission p : permissions){
            switch (p){
                case OWNER_READ    : user = user.or(FsAction.READ);      break;
                case OWNER_WRITE   : user = user.or(FsAction.WRITE);     break;
                case OWNER_EXECUTE : user = user.or(FsAction.EXECUTE);   break;
                case GROUP_READ    : group = group.or(FsAction.READ);    break;
                case GROUP_WRITE   : group = group.or(FsAction.WRITE);   break;
                case GROUP_EXECUTE : group = group.or(FsAction.EXECUTE); break;
                case OTHERS_READ   : other = other.or(FsAction.READ);    break;
                case OTHERS_WRITE  : other = other.or(FsAction.WRITE);   break;
                case OTHERS_EXECUTE: other = other.or(FsAction.EXECUTE); break;
            }
        }

        try {
            fs.setPermission(toHDFSPath(path),new FsPermission(user,group,other));
        } catch (IOException e){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }

    }

    // CopyFile: Weirdly not supported in HDFS


}
