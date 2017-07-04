package nl.esciencecenter.xenon.adaptors.file.hdfs;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.files.*;
import nl.esciencecenter.xenon.files.Path;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by atze on 3-7-17.
 */
public class HDFSFileSystem extends nl.esciencecenter.xenon.files.FileSystem{
    final org.apache.hadoop.fs.FileSystem fs;
    boolean closed;

    public HDFSFileSystem(String uniqueID, String endPoint, org.apache.hadoop.fs.FileSystem fs, XenonProperties properties) {
        super(uniqueID,"hdfs",endPoint,new Path(""),properties);
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
    public void move(Path source, Path target) throws XenonException {
        try {
            fs.rename(toHDFSPath(source), toHDFSPath(target));
        } catch(IOException e ){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }
    }

    @Override
    public void createDirectory(Path dir) throws XenonException {
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

    @Override
    public void delete(Path path) throws XenonException {
        try {
            // Todo: Fix recursive
            fs.delete(toHDFSPath(path), false);
            //return fs
        } catch(IOException e ){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }
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
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter filter) throws XenonException {
        return null;
    }

    public Iterator<PathAttributesPair> list(Path dir, boolean recursive) throws XenonException {
        final RemoteIterator<LocatedFileStatus> it;
        try {
            it = fs.listFiles(toHDFSPath(dir), recursive);
        } catch(IOException e ){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }
           return new Iterator<PathAttributesPair>() {
                @Override
                public boolean hasNext() {
                    try{
                        return it.hasNext();
                    } catch(IOException e){
                        throw new Error(e.getMessage());
                    }

                }

                @Override
                public PathAttributesPair next() {
                    final LocatedFileStatus s;
                    try{
                        s = it.next();
                    } catch(IOException e){
                        throw new Error(e.getMessage());
                    }
                    return new PathAttributesPair(new Path(s.getPath().toString()),
                            new FileAttributes() {
                        @Override
                        public boolean isDirectory() {
                            return s.isDirectory();
                        }

                        @Override
                        public boolean isOther() {
                            return false; // cannot happen in hdfs? not supported by API
                        }

                        @Override
                        public boolean isRegularFile() {
                            return s.isFile();
                        }

                        @Override
                        public boolean isSymbolicLink() {
                            return s.isSymlink();
                        }

                        @Override
                        public long creationTime() {
                            return 0; // not supported by API
                        }

                        @Override
                        public long lastAccessTime() {
                            return s.getAccessTime();
                        }

                        @Override
                        public long lastModifiedTime() {
                            return s.getModificationTime();
                        }

                        @Override
                        public long size() {
                            return s.getLen();
                        }

                        @Override
                        public boolean isExecutable() {
                            return s.getPermission().getUserAction().and(FsAction.EXECUTE) == FsAction.EXECUTE;
                        }

                        @Override
                        public boolean isHidden() {
                            return s.getPath().getName().startsWith(".");
                        }

                        @Override
                        public boolean isReadable() {
                            return s.getPermission().getUserAction().and(FsAction.READ) == FsAction.READ;
                        }

                        @Override
                        public boolean isWritable() {
                            return s.getPermission().getUserAction().and(FsAction.WRITE) == FsAction.WRITE;
                        }

                        @Override
                        public String group() throws AttributeNotSupportedException {
                            return s.getGroup();
                        }

                        @Override
                        public String owner() throws AttributeNotSupportedException {
                            return s.getOwner();
                        }

                        @Override
                        public Set<PosixFilePermission> permissions() throws AttributeNotSupportedException {
                            Set<PosixFilePermission> p = new HashSet<>();
                            FsAction owner = s.getPermission().getUserAction();
                            FsAction group = s.getPermission().getGroupAction();
                            FsAction other = s.getPermission().getOtherAction();
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
                    });
                }
            };
    }


    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir, DirectoryStream.Filter filter) throws XenonException {
        return null;
    }

    @Override
    public InputStream newInputStream(Path path) throws XenonException {
        // Todo: Set open options
        try {
            return fs.open(toHDFSPath(path));
        } catch (IOException e){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws XenonException {
        // Todo: Set open options
        try {
            return fs.create(toHDFSPath(path));
        } catch (IOException e){
            throw new XenonException("hdfs", "Error in HDFS connector :" + e.getMessage(), e);
        }
    }

    @Override
    public FileAttributes getAttributes(Path path) throws XenonException {
        Iterator<PathAttributesPair> p = list(path, false);
        while(p.hasNext()){
            PathAttributesPair at = p.next();
            if(at.path().equals(path)){
                return at.attributes();
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

    @Override
    public CopyHandle copy(CopyDescription description) throws XenonException {
        return null;
    }
}
