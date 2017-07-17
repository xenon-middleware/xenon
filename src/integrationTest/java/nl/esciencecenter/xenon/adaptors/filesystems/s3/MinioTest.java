package nl.esciencecenter.xenon.adaptors.filesystems.s3;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.filesystems.LocationConfig;
import nl.esciencecenter.xenon.filesystems.FileSystem;

/**
 * Created by atze on 13-7-17.
 */
public class MinioTest extends S3FileSystemTestParent {
    @Override
    protected LocationConfig setupLocationConfig(FileSystem fileSystem) {
        return null;
    }

    @Override
    public FileSystem setupFileSystem() throws XenonException {
        return null;
    }
}
