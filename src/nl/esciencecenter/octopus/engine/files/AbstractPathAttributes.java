package nl.esciencecenter.octopus.engine.files;

import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.PathAttributes;

public class AbstractPathAttributes implements PathAttributes {
    
    private final Path path;
    private final FileAttributes attributes;

    public AbstractPathAttributes(Path path, FileAttributes attributes) {
        this.path = path;
        this.attributes = attributes;
    }

    @Override
    public Path path() {
        return path;
    }

    @Override
    public FileAttributes attributes() {
        return attributes;
    }

}
