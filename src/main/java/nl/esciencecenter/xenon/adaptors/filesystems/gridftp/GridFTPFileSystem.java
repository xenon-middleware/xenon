package nl.esciencecenter.xenon.adaptors.filesystems.gridftp;

import static nl.esciencecenter.xenon.adaptors.filesystems.ftp.FtpFileAdaptor.ADAPTOR_NAME;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.Vector;

import org.globus.ftp.DataSourceStream;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.InputStreamDataSink;
import org.globus.ftp.MlsxEntry;
import org.globus.ftp.OutputStreamDataSource;
import org.globus.ftp.exception.ServerException;
import org.globus.ftp.vanilla.TransferState;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.PathAttributesImplementation;
import nl.esciencecenter.xenon.adaptors.filesystems.TransferClientInputStream;
import nl.esciencecenter.xenon.adaptors.filesystems.TransferClientOutputStream;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.filesystems.DirectoryNotEmptyException;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.InvalidPathException;
import nl.esciencecenter.xenon.filesystems.NoSuchPathException;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.filesystems.PathAttributes;
import nl.esciencecenter.xenon.filesystems.PosixFilePermission;

public class GridFTPFileSystem extends FileSystem {

    private final GridFTPClient client;
    private final GridFTPFileAdaptor adaptor;
    private boolean closed = false;

    private static class CloseableClient implements Closeable {

        private final GridFTPClient client;
        private final TransferState state;

        private boolean closed = false;

        CloseableClient(TransferState state, GridFTPClient client) {
            this.state = state;
            this.client = client;
        }

        @Override
        public void close() throws IOException {
            // Added functionality:
            if (!closed) {
                closed = true;

                IOException ex = null;

                try {
                    state.waitForEnd();
                } catch (Exception e) {
                    ex = new IOException("Failed to wait for end of transfer", e);
                }

                try {
                    client.close();
                } catch (Exception e) {
                    if (ex != null) {
                        throw ex;
                    }
                    throw new IOException("Failed to close client after transfer", e);
                }
            }
        }
    }

    public GridFTPFileSystem(String uniqueID, String adaptorName, String location, Credential credential, GridFTPClient client, Path workDirectory,
            int bufferSize, GridFTPFileAdaptor adaptor, XenonProperties properties) {
        super(uniqueID, adaptorName, location, credential, workDirectory, bufferSize, properties);
        this.client = client;
        this.adaptor = adaptor;
    }

    // private void setPassiveMode() throws XenonException {
    // try {
    // HostPort hp = client.setPassive();
    //
    // System.out.println("HOSTPORT: " + hp.getHost() + ":" + hp.getPort());
    //
    // client.setLocalActive();
    // } catch (Exception e) {
    // throw new XenonException(ADAPTOR_NAME, "Failed to set client to passive mode", e);
    // }
    // }

    @Override
    public boolean isOpen() throws XenonException {
        return !closed;
    }

    @Override
    public void close() throws XenonException {

        if (closed) {
            return;
        }

        closed = true;

        super.close();

        try {
            client.close();
        } catch (ServerException | IOException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to close", e);
        }
    }

    @Override
    public void rename(Path source, Path target) throws XenonException {

        assertIsOpen();

        Path absSource = toAbsolutePath(source);
        Path absTarget = toAbsolutePath(target);

        assertPathExists(absSource);

        if (areSamePaths(absSource, absTarget)) {
            return;
        }

        assertPathNotExists(absTarget);
        assertParentDirectoryExists(absTarget);

        try {
            client.rename(absSource.toString(), absTarget.toString());
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to rename " + absSource.toString() + " to " + absTarget.toString(), e);
        }
    }

    @Override
    public void createDirectory(Path dir) throws XenonException {

        assertIsOpen();

        Path absPath = toAbsolutePath(dir);
        assertPathNotExists(absPath);
        assertParentDirectoryExists(absPath);

        try {
            client.makeDir(absPath.toString());
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to create directory: " + absPath.toString(), e);
        }
    }

    @Override
    public void createFile(Path file) throws XenonException {

        assertIsOpen();

        Path absPath = toAbsolutePath(file);
        assertPathNotExists(absPath);
        assertParentDirectoryExists(absPath);

        try {
            ByteArrayInputStream dummy = new ByteArrayInputStream(new byte[0]);
            client.setPassiveMode(true);
            client.put(absPath.toString(), new DataSourceStream(dummy), null, false);
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to create file: " + absPath.toString(), e);
        }
    }

    @Override
    public void createSymbolicLink(Path link, Path target) throws XenonException {

        assertIsOpen();

        Path absLink = toAbsolutePath(link);

        assertPathNotExists(absLink);
        assertParentDirectoryExists(absLink);

        // The implementation of SYMLINKS by GridFTP seems to be very confused. The site specific SYMLINK command that our GridFTP client wants to use does not
        // seem to exist anymore. Instead we need a combination of SYMLINKFROM and SYMLINKTO commands, which seem to have reversed semantics. SYMLINKFROM
        // expects the (existing) target, SYMLINKTO expects the (non-existing) link name. Also, the SYMLINKFROM returns a reply code that indicates that the
        // SYMLINKTO should be the next command. The client does not recognize this and and interprets it as an error.
        try {
            try {
                client.site("SYMLINKFROM " + target);
            } catch (ServerException e) {
                // ignore, as it may be a positive reply misinterpreted by the client.
            }

            client.site("SYMLINKTO " + absLink.toString());

        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to create symlink " + absLink + " to " + target, e);
        }
    }

    @Override
    public boolean exists(Path path) throws XenonException {

        assertIsOpen();

        Path absPath = toAbsolutePath(path);

        try {
            return client.exists(absPath.toString());
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to check if path exists: " + absPath.toString(), e);
        }
    }

    @Override
    public InputStream readFromFile(Path file) throws XenonException {

        assertIsOpen();

        Path absPath = toAbsolutePath(file);
        assertPathExists(absPath);
        assertPathIsFile(absPath);

        // GridFTP connections can only do a single thing a time. If we do stuff in parallel, the command channel gets confused.
        // Therefore we need a new GridFTPClient to handle the stream.
        GridFTPClient newClient = adaptor.connectClient(getLocation(), getCredential(), getProperties());

        try {
            InputStreamDataSink sink = new InputStreamDataSink();
            newClient.setPassiveMode(true);
            TransferState ts = newClient.asynchGet(absPath.toString(), sink, null);
            return new TransferClientInputStream(sink.getInputStream(), new CloseableClient(ts, newClient));
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to read from file: " + absPath.toString(), e);
        }
    }

    private OutputStream writeToFile(Path file, long size, boolean append) throws XenonException {

        assertIsOpen();
        Path absPath = toAbsolutePath(file);

        if (!append) {
            assertPathNotExists(absPath);
            assertParentDirectoryExists(absPath);
        } else {
            assertFileExists(absPath);
        }

        // GridFTP connections can only do a single thing a time. If we do stuff in parallel, the command channel gets confused.
        // Therefore we need a new GridFTPClient to handle the stream.
        GridFTPClient newClient = adaptor.connectClient(getLocation(), getCredential(), getProperties());

        try {
            int buffer = getBufferSize();

            if (size > 0) {
                buffer = (int) (size > GridFTPFileAdaptor.MAX_BUFFER_SIZE ? GridFTPFileAdaptor.MAX_BUFFER_SIZE : size);
            }

            OutputStreamDataSource osds = new OutputStreamDataSource(buffer);
            newClient.setPassiveMode(true);
            TransferState ts = newClient.asynchPut(absPath.toString(), osds, null, append);
            return new TransferClientOutputStream(osds.getOutputStream(), new CloseableClient(ts, newClient));
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to write to file: " + absPath.toString(), e);
        }
    }

    @Override
    public OutputStream writeToFile(Path path, long size) throws XenonException {
        return writeToFile(path, size, false);
    }

    @Override
    public OutputStream writeToFile(Path file) throws XenonException {
        return writeToFile(file, -1, false);
    }

    @Override
    public OutputStream appendToFile(Path file) throws XenonException {
        return writeToFile(file, -1, true);
    }

    private PathAttributes convertAttributes(Path p, MlsxEntry entry) {

        PathAttributesImplementation ps = new PathAttributesImplementation();

        ps.setPath(p);

        String type = entry.get(MlsxEntry.TYPE);

        if (MlsxEntry.TYPE_DIR.equals(type) || MlsxEntry.TYPE_CDIR.equals(type) || MlsxEntry.TYPE_PDIR.equals(type)) {
            ps.setDirectory(true);
        } else if (MlsxEntry.TYPE_FILE.equals(type)) {
            ps.setRegular(true);
        } else if (MlsxEntry.TYPE_SLINK.equals(type)) {
            ps.setSymbolicLink(true);
        }

        String size = entry.get(MlsxEntry.SIZE);

        if (size != null) {
            ps.setSize(Integer.parseInt(size));
        }

        String modify = entry.get(MlsxEntry.MODIFY);
        long modifyTime = 0;

        if (modify != null) {
            modifyTime = entry.getDate(MlsxEntry.MODIFY).getTime();
            ps.setLastModifiedTime(modifyTime);
            ps.setLastAccessTime(modifyTime);
        }

        String create = entry.get(MlsxEntry.CREATE);

        if (create != null) {
            ps.setCreationTime(entry.getDate(MlsxEntry.CREATE).getTime());
        } else {
            ps.setCreationTime(modifyTime);
        }

        String owner = entry.get(MlsxEntry.UNIX_OWNER);

        if (owner != null) {
            ps.setOwner(owner);
        }

        String group = entry.get(MlsxEntry.UNIX_GROUP);

        if (group != null) {
            ps.setOwner(group);
        }

        String mode = entry.get(MlsxEntry.UNIX_MODE);

        if (mode != null) {
            ps.setPermissions(PosixFilePermission.convertFromOctal(mode));
        }

        return ps;
    }

    @Override
    public PathAttributes getAttributes(Path path) throws XenonException {

        assertIsOpen();

        Path absPath = toAbsolutePath(path);

        try {
            return convertAttributes(path, client.mlst(absPath.toString()));
        } catch (ServerException e1) {
            throw new NoSuchPathException(ADAPTOR_NAME, "Failed to get attibutes for: " + absPath.toString(), e1);
        } catch (IOException e2) {
            throw new XenonException(ADAPTOR_NAME, "Failed to get attibutes for: " + absPath.toString(), e2);
        }
    }

    @Override
    public Path readSymbolicLink(Path link) throws XenonException {

        assertIsOpen();

        Path absPath = toAbsolutePath(link);

        assertPathExists(absPath);

        try {
            MlsxEntry entry = client.mlst(absPath.toString());

            // NOTE: If the path is a link, the entry type seems to reflect the type that the link points to, not MlsxEntry.TYPE_SLINK itself.
            // So the only way to figure out if something is a link is to see if the "points to" field is set.
            String target = entry.get(MlsxEntry.UNIX_SLINK);

            if (target == null || target.isEmpty()) {
                throw new InvalidPathException(ADAPTOR_NAME, "Path is not a link: " + absPath);
            }

            return new Path(target);

        } catch (IOException | ServerException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to read symbolic link: " + absPath);
        }
    }

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {

        assertIsOpen();

        Path absPath = toAbsolutePath(path);

        assertPathExists(absPath);

        String octal = PosixFilePermission.convertToOctal(permissions);

        try {
            client.site("chmod " + octal + " " + absPath.toString());
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to chang permissions of " + absPath + " to " + octal);
        }
    }

    @Override
    public void delete(Path path, boolean recursive) throws XenonException {

        // Since gridftp has a recursive delete command, we can use than when needed.

        Path absPath = toAbsolutePath(path);

        assertPathExists(absPath);

        if (recursive) {
            try {
                client.site("RDEL " + absPath.toString());
            } catch (Exception e) {
                throw new XenonException(ADAPTOR_NAME, "Failed to delete path: " + path, e);
            }

            return;
        }

        if (getAttributes(absPath).isDirectory()) {

            Iterable<PathAttributes> itt = list(absPath, false);

            if (itt.iterator().hasNext()) {
                throw new DirectoryNotEmptyException(getAdaptorName(), "Directory not empty: " + absPath.toString());
            }

            deleteDirectory(absPath);
        } else {
            deleteFile(absPath);
        }
    }

    @Override
    protected void deleteFile(Path file) throws XenonException {

        assertIsOpen();

        try {
            client.deleteFile(file.toString());
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to delete file: " + file, e);
        }
    }

    @Override
    protected void deleteDirectory(Path path) throws XenonException {

        assertIsOpen();

        try {
            client.deleteDir(path.toString());
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to delete file: " + path, e);
        }
    }

    private Iterable<PathAttributes> convertFileInfoVector(Path parentDir, @SuppressWarnings("rawtypes") Vector info) {

        ArrayList<PathAttributes> result = new ArrayList<>(info.size());

        for (Object e : info) {
            MlsxEntry tmp = (MlsxEntry) e;
            result.add(convertAttributes(parentDir.resolve(tmp.getFileName()), tmp));
        }

        return result;
    }

    @Override
    protected Iterable<PathAttributes> listDirectory(Path dir) throws XenonException {

        assertIsOpen();
        assertDirectoryExists(dir);

        Path absPath = toAbsolutePath(dir);

        try {
            client.setPassiveMode(true);
            return convertFileInfoVector(dir, client.mlsd(absPath.toString() + "/"));
        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to list directory: " + absPath.toString(), e);
        }
    }

}
