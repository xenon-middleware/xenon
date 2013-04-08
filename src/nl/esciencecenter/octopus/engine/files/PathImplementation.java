package nl.esciencecenter.octopus.engine.files;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import nl.esciencecenter.octopus.ImmutableTypedProperties;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.util.OSUtils;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.DeployRuntimeException;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.security.Credentials;

/**
 * Default implementation of Path. Will create new Paths directly, using
 * either an adaptor identical to the original in case of an absolute path,
 * or the local adaptor in case of a relative path.
 */
public class PathImplementation implements Path {

    private static final class PathIterator implements Iterator<Path> {

        private ImmutableTypedProperties properties;
        private Credentials credentials;
        private URI location;
        private String[] elements;

        private int next = 0;

        PathIterator(ImmutableTypedProperties properties, Credentials credentials, URI location, String[] elements) {
            this.properties = properties;
            this.credentials = credentials;
            this.location = location;
            this.elements = elements;
        }

        @Override
        public boolean hasNext() {
            return next < elements.length;
        }

        @Override
        public Path next() {
            if (!hasNext()) {
                throw new NoSuchElementException("no more elements in path");
            }
            return new PathImplementation(properties, credentials, location, "local", null, elements[next++]);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("cannot remove elements from path");
        }

    }

    private static final long serialVersionUID = 1L;

    private static URI createURIFor(URI location, String root, String... elements) {
        String path = root;
        if (path == null) {
            path = "";
        }
        if (elements.length > 0) {
            for (int i = 0; i < elements.length - 1; i++) {
                path = path + elements[i] + "/";
            }
            path = path + elements[elements.length - 1];
        }

        if (location.getScheme() == null || root == null) {
            return URI.create(path);
        }

        else {
            try {
                return new URI(location.getScheme(), location.getAuthority(), path, null, null);
            } catch (URISyntaxException e) {
                throw new DeployRuntimeException("Could not create URI", e, null, null);
            }
        }

    }

    private final ImmutableTypedProperties properties;

    private final Credentials credentials;

    private final URI location;

    // root of path, or null if relative
    private final String root;

    private final String[] elements;

    private final String adaptorName;

    private final OctopusEngine octopusEngine;


    public PathImplementation(ImmutableTypedProperties properties, Credentials credentials, URI location, String adaptorName,
            OctopusEngine octopusEngine) {
        this.properties = properties;
        this.credentials = credentials;
        this.location = location;
        this.adaptorName = adaptorName;
        this.octopusEngine = octopusEngine;

        String pathString = location.getPath();

        if (pathString == null) {
            root = null;
            elements = new String[0];
        } else if (isLocal() && OSUtils.isWindows()) {
            if (location.getPath().matches("^/[a-zA-Z]:/")) {
                root = pathString.substring(0, 4);
                pathString = pathString.substring(4);
            } else {
                root = null;
            }
            this.elements = pathString.split("/+");
        } else {
            if (pathString.startsWith("/")) {
                root = "/";
                pathString = pathString.substring(1);
            } else {
                root = null;
            }
            this.elements = pathString.split("/+");
        }

    }

    /*
     * Creates a new path based on an existing path, with a new location based
     * on the old location and the given root and elements.
     */
    private PathImplementation(ImmutableTypedProperties properties, Credentials credentials, URI baseLocation, String adaptorName,
            OctopusEngine octopusEngine, String root, String... elements) {
        this.properties = properties;
        this.credentials = credentials;
        this.adaptorName = adaptorName;
        this.octopusEngine = octopusEngine;

        // adjust URI to new location
        this.location = createURIFor(baseLocation, root, elements);

        this.root = root;
        this.elements = elements;
    }

    @Override
    public boolean isAbsolute() {
        return location.getScheme() != null && root != null;
    }

    @Override
    public Path getRoot() {
        if (root == null) {
            return null;
        }

        String[] newElements = new String[0];

        return new PathImplementation(properties, credentials, location, adaptorName, octopusEngine, root, newElements);
    }

    @Override
    public Path getFileName() {
        if (elements.length == 0) {
            return null;
        }
        String fileName = elements[elements.length - 1];

        return new PathImplementation(properties, credentials, location, "local", null, fileName);
    }

    @Override
    public Path getParent() {
        if (elements.length == 0) {
            return null;
        }

        String[] parentElements = Arrays.copyOfRange(elements, 0, elements.length - 1);

        return new PathImplementation(properties, credentials, location, adaptorName, octopusEngine, root, parentElements);
    }

    @Override
    public int getNameCount() {
        return elements.length;
    }

    @Override
    public Path getName(int index) {
        if (index >= elements.length) {
            throw new IllegalArgumentException("index " + index + " not present in path " + this);
        }
        return new PathImplementation(properties, credentials, location, "local", null, elements[index]);
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        return new PathImplementation(properties, credentials, location, "local", octopusEngine, null, Arrays.copyOfRange(elements,
                beginIndex, endIndex));
    }

    @Override
    public boolean startsWith(Path other) {
        // check some of the components of the uri of this path.
        if (!other.getAdaptorName().equals(adaptorName) || !other.toUri().getScheme().equals(location.getScheme())
                || !other.toUri().getHost().equals(location.getHost())) {
            return false;
        }
        return getPath().startsWith(other.getPath());
    }

    @Override
    public boolean startsWith(String other) {
        return getPath().startsWith(other);
    }

    @Override
    public boolean endsWith(Path other) {
        return getPath().endsWith(other.getPath());
    }

    @Override
    public boolean endsWith(String other) {
        return getPath().endsWith(other);
    }

    @Override
    public Path normalize() {
        return new PathImplementation(properties, credentials, location.normalize(), adaptorName, octopusEngine);
    }

    @Override
    public Path resolve(Path other) {
        if (other.isAbsolute()) {
            return other;
        }

        return new PathImplementation(properties, credentials, location.resolve(other.toUri()), adaptorName, octopusEngine);
    }

    @Override
    public Path resolve(String other) throws OctopusException {
        Path otherPath = octopusEngine.files().newPath(URI.create(other));

        return resolve(otherPath);
    }

    @Override
    public Path resolveSibling(Path other) {
        if (other.isAbsolute()) {
            return other;
        }

        Path parent = getParent();

        if (parent == null) {
            return other;
        }

        if (other.getNameCount() == 0) {
            return parent;
        }

        return parent.resolve(other);
    }

    @Override
    public Path resolveSibling(String other) throws OctopusException {
        Path otherPath = octopusEngine.files().newPath(URI.create(other));

        return resolveSibling(otherPath);
    }

    @Override
    public Path relativize(Path other) throws OctopusException {
        if (equals(other)) {
            return this;
        }

        if (!isAbsolute()) {
            throw new OctopusException("cannot relativize against an already relative path", null, null);
        }

        return new PathImplementation(properties, credentials, location.relativize(other.toUri()), "local", octopusEngine);
    }

    @Override
    public URI toUri() {
        return location;
    }
    
    @Override
    public ImmutableTypedProperties getProperties() {
       return properties;
    }

    @Override
    public Credentials getCredentials() {
        return credentials;
    }

    @Override
    public Path toAbsolutePath() throws OctopusException {
        if (isAbsolute()) {
            return this;
        }

        if (isLocal()) {
            // Path for cwd
            Path cwd = new PathImplementation(properties, credentials, URI.create(System.getProperty("user.dir")), "local", octopusEngine);

            return cwd.resolve(this);
        }

        throw new OctopusException("can only get absolute path for local files", null, null);
    }

    @Override
    public Iterator<Path> iterator() {
        return new PathIterator(properties, credentials, location, elements);
    }

    @Override
    public int compareTo(Path other) {
        return location.compareTo(other.toUri());
    }

    @Override
    public String getAdaptorName() {
        return adaptorName;
    }

    @Override
    public boolean isLocal() {
        return adaptorName.equalsIgnoreCase("local");
    }

    @Override
    public String getPath() {
        String result = root;
        if (result == null) {
            result = "";
        }
        if (elements.length == 0) {
            return result;
        }
        for (int i = 0; i < elements.length - 1; i++) {
            result = result + elements[i] + "/";
        }
        result = result + elements[elements.length - 1];

        return result;
    }

    public String toString() {
        return location.toString();
    }

}
