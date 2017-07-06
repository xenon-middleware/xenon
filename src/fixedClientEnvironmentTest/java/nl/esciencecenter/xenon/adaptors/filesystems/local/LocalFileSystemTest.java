package nl.esciencecenter.xenon.adaptors.filesystems.local;

import java.util.AbstractMap;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.filesystems.FileSystemTestParent;
import nl.esciencecenter.xenon.adaptors.filesystems.LocationConfig;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;

public class LocalFileSystemTest extends FileSystemTestParent {
    @Override
    protected LocationConfig setupLocationConfig() {
        return new LocationConfig() {
            @Override
            public Path getExistingPath() {
                return new Path("/home/xenon/filesystem-test-fixture/links/file0");
            }

            @Override
            public Map.Entry<Path, Path> getSymbolicLinksToExistingFile() {
                return new AbstractMap.SimpleEntry<>(
                    new Path("/home/xenon/filesystem-test-fixture/links/link0"),
                    new Path("/home/xenon/filesystem-test-fixture/links/file0")
                );
            }
        };
    }

    @Override
    public FileSystem setupFileSystem() throws XenonException {
        return FileSystem.create("file");
    }
}
