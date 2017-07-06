package nl.esciencecenter.xenon.adaptors.filesystems;

import java.util.AbstractMap;
import java.util.Map;

import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;

public class LiveLocationConfig extends LocationConfig {
    private final FileSystem fileSystem;

    public LiveLocationConfig(FileSystem fileSystem) {
        super();
        this.fileSystem = fileSystem;
    }
    // TODO the paths should be relative to the filesystem.getEntryPath()

    @Override
    public Path getExistingPath() {
        return fileSystem.getEntryPath().resolve("filesystem-test-fixture/links/file0");
    }

    @Override
    public Map.Entry<Path, Path> getSymbolicLinksToExistingFile() {
        return new AbstractMap.SimpleEntry<>(
            fileSystem.getEntryPath().resolve("filesystem-test-fixture/links/link0"),
            fileSystem.getEntryPath().resolve("filesystem-test-fixture/links/file0")
        );
    }
}
