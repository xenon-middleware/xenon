package nl.esciencecenter.octopus.files;

import java.io.Serializable;
import java.net.URI;
import java.util.Iterator;

import nl.esciencecenter.octopus.ImmutableTypedProperties;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.security.Credentials;

public interface Path extends Serializable {

    public boolean isAbsolute();

    public Path getRoot();

    public Path getFileName();

    public Path getParent();

    public int getNameCount();

    public Path getName(int index);

    public Path subpath(int beginIndex, int endIndex);

    public boolean startsWith(Path other);

    public boolean startsWith(String other);

    public boolean endsWith(Path other);

    public boolean endsWith(String other);

    public Path normalize();

    public Path resolve(Path other);

    public Path resolve(String other) throws OctopusException;

    public Path resolveSibling(Path other);

    public Path resolveSibling(String other) throws OctopusException;

    public Path relativize(Path other) throws OctopusException;

    public URI toUri();

    /**
     * Note: this will most likely only work for local files
     */
    public Path toAbsolutePath() throws OctopusException;

    public Iterator<Path> iterator();

    public int compareTo(Path other);

    public String getAdaptorName();

    public boolean isLocal();

    public String getPath();

    public ImmutableTypedProperties getProperties();

    public Credentials getCredentials();

}
