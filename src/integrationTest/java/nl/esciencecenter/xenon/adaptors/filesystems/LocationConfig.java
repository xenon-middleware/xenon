package nl.esciencecenter.xenon.adaptors.filesystems;

import java.util.Map;

import nl.esciencecenter.xenon.filesystems.Path;

public abstract class LocationConfig {
    public abstract Path getExistingPath();

    // TODO return SymbolicLink object
    public abstract Map.Entry<Path,Path> getSymbolicLinksToExistingFile();
}
