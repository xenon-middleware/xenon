package nl.esciencecenter.xenon.adaptors.filesystems.gridftp;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.filesystems.PathAttributes;
import nl.esciencecenter.xenon.filesystems.PosixFilePermission;

public class GridFTPFileSystem extends FileSystem {

    public GridFTPFileSystem(String uniqueID, String adaptor, String location, Credential credential, Path workDirectory, int bufferSize,
            XenonProperties properties) {
        super(uniqueID, adaptor, location, credential, workDirectory, bufferSize, properties);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean isOpen() throws XenonException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void rename(Path source, Path target) throws XenonException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createDirectory(Path dir) throws XenonException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createFile(Path file) throws XenonException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createSymbolicLink(Path link, Path target) throws XenonException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean exists(Path path) throws XenonException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public InputStream readFromFile(Path file) throws XenonException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OutputStream writeToFile(Path path, long size) throws XenonException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OutputStream writeToFile(Path file) throws XenonException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OutputStream appendToFile(Path file) throws XenonException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PathAttributes getAttributes(Path path) throws XenonException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path readSymbolicLink(Path link) throws XenonException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void deleteFile(Path file) throws XenonException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void deleteDirectory(Path path) throws XenonException {
        // TODO Auto-generated method stub

    }

    @Override
    protected Iterable<PathAttributes> listDirectory(Path dir) throws XenonException {
        // TODO Auto-generated method stub
        return null;
    }

}
