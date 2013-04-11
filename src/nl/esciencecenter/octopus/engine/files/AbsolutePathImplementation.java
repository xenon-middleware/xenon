package nl.esciencecenter.octopus.engine.files;

import java.util.Iterator;

import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.RelativePath;

/**
 * Implementation of Path. Will create new Paths directly, using either an adaptor identical to the original in case of an
 * absolute path, or the local adaptor in case of a relative path.
 */
public final class AbsolutePathImplementation implements AbsolutePath {

//    private static final class PathIterator implements Iterator<Path> {
//
//        private OctopusProperties properties;
//        private URI location;
//        private String[] elements;
//
//        private int next = 0;
//
//        PathIterator(OctopusProperties properties, URI location, String[] elements) {
//            this.properties = properties;
//            this.location = location;
//            this.elements = elements;
//        }
//
//        @Override
//        public boolean hasNext() {
//            return next < elements.length;
//        }
//
//        @Override
//        public Path next() {
//            if (!hasNext()) {
//                throw new NoSuchElementException("no more elements in path");
//            }
//            return new PathImplementation(properties, location, "local", null, elements[next++]);
//        }
//
//        @Override
//        public void remove() {
//            throw new UnsupportedOperationException("cannot remove elements from path");
//        }
//
//    }
    
//
//    private static URI createURIFor(URI location, String root, String... elements) {
//        String path = root;
//        if (path == null) {
//            path = "";
//        }
//        if (elements.length > 0) {
//            for (int i = 0; i < elements.length - 1; i++) {
//                path = path + elements[i] + "/";
//            }
//            path = path + elements[elements.length - 1];
//        }
//
//        if (location.getScheme() == null || root == null) {
//            return URI.create(path);
//        }
//
//        else {
//            try {
//                return new URI(location.getScheme(), location.getAuthority(), path, null, null);
//            } catch (URISyntaxException e) {
//                throw new OctopusRuntimeException("PathImplementation", "Could not create URI", e);
//            }
//        }
//    }

    private final FileSystem filesystem;
    private final RelativePath relativePath;
    
    public AbsolutePathImplementation(FileSystem filesystem, RelativePath relativePath) {
       
        this.filesystem = filesystem;
        this.relativePath = relativePath;
        
        
//        String pathString = location.getPath();
//
//        if (pathString == null) {
//            root = null;
//            elements = new String[0];
//        } else if (isLocal() && OSUtils.isWindows()) {
//            if (location.getPath().matches("^/[a-zA-Z]:/")) {
//                root = pathString.substring(0, 4);
//                pathString = pathString.substring(4);
//            } else {
//                root = null;
//            }
//            this.elements = pathString.split("/+");
//        } else {
//            if (pathString.startsWith("/")) {
//                root = "/";
//                pathString = pathString.substring(1);
//            } else {
//                root = null;
//            }
//            this.elements = pathString.split("/+");
//        }

    }

    public AbsolutePathImplementation(FileSystem filesystem, RelativePath... relativePaths) {
        
        this.filesystem = filesystem;
        
        if (relativePaths.length == 0) { 
            throw new IllegalArgumentException("AbsolutePathImplementation requires at least one RelativePath");
        }
        
        this.relativePath = new RelativePath(relativePaths);
    }
    
    
    @Override
    public FileSystem getFileSystem() {
        return filesystem;
    }
    
    @Override
    public RelativePath getRelativePath() {
        return relativePath;
    }
    
    @Override
    public boolean isLocal() {
        return filesystem.getAdaptorName().equals("local");
    }
    
    @Override
    public String getFileName() {
        return relativePath.getFileName();
    }

    @Override
    public AbsolutePath getParent() {
        return new AbsolutePathImplementation(filesystem, relativePath.getParent());
    }

    @Override
    public int getNameCount() {
        return relativePath.getNameCount();
    }

    @Override
    public String [] getNames() {
        return relativePath.getNames();
    }
    
    @Override
    public String getName(int index) {
        return relativePath.getName(index);
    }
    
    @Override
    public AbsolutePath subpath(int beginIndex, int endIndex) {
        return new AbsolutePathImplementation(filesystem, relativePath.subpath(beginIndex, endIndex));
    }

    @Override
    public AbsolutePath normalize() {
        return new AbsolutePathImplementation(filesystem, relativePath.normalize());
    }

    
    @Override
    public boolean startsWith(RelativePath other) {
        return relativePath.startsWith(other);
    }

    @Override
    public boolean endsWith(RelativePath other) {
        return relativePath.endsWith(other);
    }

    @Override
    public AbsolutePath resolve(RelativePath other) {
        return new AbsolutePathImplementation(filesystem, relativePath.resolve(other));
    }

    @Override
    public AbsolutePath resolveSibling(RelativePath other) throws OctopusException {
        return new AbsolutePathImplementation(filesystem, relativePath.resolveSibling(other));
    }

    @Override
    public AbsolutePath relativize(RelativePath other) throws OctopusException {
        return new AbsolutePathImplementation(filesystem, relativePath.relativize(other));
    }

    @Override
    public Iterator<AbsolutePath> iterator() {
        // TODO: implement;
        return null;
    }
    
    @Override
    public String getPath() {
        return relativePath.getPath();
    }
    
    public String toString() {
        return filesystem.toString() + relativePath.toString();
    }

}
