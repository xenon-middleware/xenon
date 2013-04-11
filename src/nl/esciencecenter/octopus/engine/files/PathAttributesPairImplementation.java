package nl.esciencecenter.octopus.engine.files;

import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.PathAttributesPair;

public class PathAttributesPairImplementation implements PathAttributesPair {

    private final AbsolutePath path;
    private final FileAttributes attributes;

    public PathAttributesPairImplementation(AbsolutePath path, FileAttributes attributes) {
        this.path = path;
        this.attributes = attributes;
    }

    @Override
    public AbsolutePath path() {
        return path;
    }

    @Override
    public FileAttributes attributes() {
        return attributes;
    }
}
