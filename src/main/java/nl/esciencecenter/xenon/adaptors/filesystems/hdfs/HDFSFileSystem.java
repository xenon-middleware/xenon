package nl.esciencecenter.xenon.adaptors.filesystems.hdfs;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.files.*;
import nl.esciencecenter.xenon.filesystems.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import nl.esciencecenter.xenon.filesystems.Path;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by atze on 3-7-17.
 */
public class HDFSFileSystem extends nl.esciencecenter.xenon.filesystems.FileSystem{
    final org.apache.hadoop.fs.FileSystem fs;
    boolean closed;

    protected HDFSFileSystem(String uniqueID, String endPoint, org.apache.hadoop.fs.FileSystem fs, XenonProperties properties) {
        super(uniqueID,"hdfs",endPoint,new nl.esciencecenter.xenon.filesystems.Path(""),properties);
        this.fs = fs;
        closed = false;
    }


    @Override
    public void close() throws XenonException {
        try {
            fs.close();
            closed = false;
        } catch (IOException e){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }
    }

    @Override
    public boolean isOpen() throws XenonException {
        return !closed;
    }


    @Override
    public boolean exists(Path path) throws XenonException {
        try {
            return fs.exists(toHDFSPath(path));
        } catch(IOException e ){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Path path, boolean recursive) throws XenonException {
        try {
            fs.delete(toHDFSPath(path), recursive);
        } catch(IOException e ){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }
    }

    @Override
    public InputStream readFromFile(Path path) throws XenonException {
        try {
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
        try {
            return fs.create(toHDFSPath(path));
        } catch (IOException e){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }
    }

    @Override
    public OutputStream appendToFile(Path path) throws XenonException {
        try {
            return fs.append(toHDFSPath(path));
        } catch (IOException e){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
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


    @Override
    public void move(Path source, Path target) throws XenonException {
        try {
            if (!exists(source)){
                throw new NoSuchPathException(getAdaptorName(), source.getRelativePath());
            }
            if(exists(target)){
                throw new PathAlreadyExistsException(getAdaptorName(), target.getRelativePath());
            }
            fs.rename(toHDFSPath(source), toHDFSPath(target));
        } catch(IOException e ){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }
    }

    @Override
    public void createDirectory(Path dir) throws XenonException {
        try {
            if(!exists(dir.getParent())){
                throw new XenonException(getAdaptorName(), "createDirectory: parent does not exist: " + dir.getRelativePath());
            }
            fs.mkdirs(toHDFSPath(dir));
        } catch(IOException e ){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }

    }

    @Override
    public void createDirectories(Path dir) throws XenonException {
        try {
            fs.mkdirs(toHDFSPath(dir));
        } catch(IOException e ){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }

    }

    @Override
    public void createFile(Path file) throws XenonException {
        try {
            fs.createNewFile(toHDFSPath(file));
        } catch(IOException e ){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }
    }


    org.apache.hadoop.fs.Path toHDFSPath(Path p){
        return new org.apache.hadoop.fs.Path(p.getRelativePath());
    }

    Path fromHDFSPath(org.apache.hadoop.fs.Path p){
        return new Path(p.toString());
    }



    private PathAttributes toPathAttributes(LocatedFileStatus s){
        PathAttributes res = new PathAttributes();
        res.setDirectory(s.isDirectory());
        res.setOther(false); // cannot happen in hdfs? not supported by API
        res.setRegular(s.isFile());
        res.setSymbolicLink(s.isSymlink());
        res.setCreationTime(0);
        res.setLastAccessTime(s.getAccessTime());
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

    public Iterable<PathAttributes> list(final Path dir,  boolean recursive) throws XenonException {
        final Iterator<PathAttributes> it =  listIterator(dir,recursive);
        return new Iterable<PathAttributes>() {
            @Override
            public Iterator<PathAttributes> iterator() {
                return it;
            }
        };
    }

    private Iterator<PathAttributes> listIterator(Path dir, boolean recursive) throws XenonException {
        final RemoteIterator<LocatedFileStatus> it;
        try {
            it = fs.listFiles(toHDFSPath(dir), recursive);
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
    public PathAttributes getAttributes(Path path) throws XenonException {
        Iterator<PathAttributes> p = list(path, false).iterator();
        while(p.hasNext()){
            PathAttributes at = p.next();
            if(at.getPath().equals(path)){
                return at;
            }
        }
        throw new XenonException("hdfs", "No such file: " + path.getRelativePath());
    }

    @Override
    public Path readSymbolicLink(Path link) throws XenonException {
        try {
            return fromHDFSPath(fs.resolvePath(toHDFSPath(link)));
        } catch (IOException e){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }

    }

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
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

}

