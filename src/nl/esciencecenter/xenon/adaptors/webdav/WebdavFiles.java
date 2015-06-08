package nl.esciencecenter.xenon.adaptors.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.adaptors.ftp.FtpFiles;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.files.FileSystemImplementation;
import nl.esciencecenter.xenon.files.Copy;
import nl.esciencecenter.xenon.files.CopyOption;
import nl.esciencecenter.xenon.files.CopyStatus;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.DirectoryStream.Filter;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.OpenOption;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PathAttributesPair;
import nl.esciencecenter.xenon.files.PosixFilePermission;
import nl.esciencecenter.xenon.files.RelativePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

public class WebdavFiles implements Files {
    private static final Logger LOGGER = LoggerFactory.getLogger(FtpFiles.class);

    private static int currentID = 1;

    private static synchronized String getNewUniqueID() {
        String res = "ftp" + currentID;
        currentID++;
        return res;
    }

    private WebdavAdaptor adaptor;

    public WebdavFiles(WebdavAdaptor webdavAdaptor, XenonEngine xenonEngine) {
        adaptor = webdavAdaptor;
    }

    @Override
    public FileSystem newFileSystem(String scheme, String location, Credential credential, Map<String, String> properties)
            throws XenonException {
        LOGGER.debug("newFileSystem scheme = {} location = {} credential = {} properties = {}", scheme, location, credential,
                properties);

        Sardine sardine = SardineFactory.begin("xenon", "xenon1");
        try {
            sardine.exists(location);
        } catch (IOException e) {
            throw new XenonException(adaptor.getName(), "Failed to open filesystem", e);
        }

        String cwd = null;
        RelativePath entryPath = new RelativePath(cwd);
        String uniqueID = getNewUniqueID();
        XenonProperties xenonProperties = new XenonProperties(adaptor.getSupportedProperties(Component.FILESYSTEM), properties);
        FileSystemImplementation fileSystem = new FileSystemImplementation(adaptor.getName(), uniqueID, scheme, location,
                entryPath, credential, xenonProperties);
        LOGGER.debug("* newFileSystem OK remote cwd = {} entryPath = {} uniqueID = {}", cwd, entryPath, uniqueID);
        return fileSystem;
    }

    @Override
    public Path newPath(FileSystem filesystem, RelativePath location) throws XenonException {
        return null;
    }

    @Override
    public void close(FileSystem filesystem) throws XenonException {
    }

    @Override
    public boolean isOpen(FileSystem filesystem) throws XenonException {
        return false;
    }

    @Override
    public Copy copy(Path source, Path target, CopyOption... options) throws XenonException {
        return null;
    }

    @Override
    public void move(Path source, Path target) throws XenonException {
    }

    @Override
    public CopyStatus getCopyStatus(Copy copy) throws XenonException {
        return null;
    }

    @Override
    public CopyStatus cancelCopy(Copy copy) throws XenonException {
        return null;
    }

    @Override
    public void createDirectories(Path dir) throws XenonException {
    }

    @Override
    public void createDirectory(Path dir) throws XenonException {
    }

    @Override
    public void createFile(Path path) throws XenonException {
    }

    @Override
    public void delete(Path path) throws XenonException {
    }

    @Override
    public boolean exists(Path path) throws XenonException {
        return false;
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir) throws XenonException {
        return null;
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter filter) throws XenonException {
        return null;
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir) throws XenonException {
        return null;
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir, Filter filter) throws XenonException {
        return null;
    }

    @Override
    public InputStream newInputStream(Path path) throws XenonException {
        return null;
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws XenonException {
        return null;
    }

    @Override
    public FileAttributes getAttributes(Path path) throws XenonException {
        return null;
    }

    @Override
    public Path readSymbolicLink(Path link) throws XenonException {
        return null;
    }

    @Override
    public void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException {
    }

    public void end() {
    }

}
