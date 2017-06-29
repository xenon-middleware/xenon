package nl.esciencecenter.xenon.adaptors.file.s3;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.files.FileAdaptor;
import nl.esciencecenter.xenon.engine.files.FilesEngine;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;
import nl.esciencecenter.xenon.files.*;
import org.jclouds.blobstore.BlobStoreContext;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

/**
 * Created by atze on 29-6-17.
 */
public class JCloudsFiles extends FileAdaptor{

    BlobStoreContext

    /**
     * @param filesEngine
     * @param name
     * @param description
     * @param supportedSchemes
     * @param supportedLocations
     * @param validProperties
     * @param properties
     */
    protected JCloudsFiles(FilesEngine filesEngine, String name, String description, ImmutableArray<String> supportedSchemes, ImmutableArray<String> supportedLocations, ImmutableArray<XenonPropertyDescription> validProperties, XenonProperties properties) {
        super(filesEngine, name, description, supportedSchemes, supportedLocations, validProperties, properties);
    }

    @Override
    public FileSystem newFileSystem(String location, Credential credential, Map<String, String> properties) throws XenonException {
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
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter filter) throws XenonException {
        return null;
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir) throws XenonException {
        return null;
    }

    @Override
    public DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir, DirectoryStream.Filter filter) throws XenonException {
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

    @Override
    public Map<String, String> getAdaptorSpecificInformation() {
        return null;
    }

    @Override
    public void end() {

    }
}
