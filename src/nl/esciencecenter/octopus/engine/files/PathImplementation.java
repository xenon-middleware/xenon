package nl.esciencecenter.octopus.engine.files;

import java.util.Arrays;
import java.util.Iterator;

import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Path;

/**
 * Implementation of Path. Will create new Paths directly, using either an adaptor identical to the original in case of an
 * absolute path, or the local adaptor in case of a relative path.
 */
public final class PathImplementation implements Path {

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

    private final String [] elements;
    private final FileSystem filesystem;
    
    public PathImplementation(FileSystem filesystem, String path) {
       
        this.filesystem = filesystem;
        
        if (path == null) {
            elements = new String[0];
        } else { 
            this.elements = path.split("/+");    
        }
        
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

    public PathImplementation(FileSystem filesystem, String [] elements) {
        
        this.filesystem = filesystem;
        
        if (elements == null) {
            this.elements = new String[0];
        } else { 
            this.elements = elements.clone();    
        }
    }

    @Override
    public String getFileName() {
        if (elements.length == 0) {
            return null;
        }
        return elements[elements.length - 1];
    }

    @Override
    public Path getParent() {
        if (elements.length == 0) {
            return null;
        }

        String[] parentElements = Arrays.copyOfRange(elements, 0, elements.length - 1);

        return new PathImplementation(filesystem, parentElements);
    }

    @Override
    public int getNameCount() {
        return elements.length;
    }

    @Override
    public String [] getNames() {
        return elements.clone();
    }
    
    @Override
    public String getName(int index) {
        if (index >= elements.length) {
            throw new IllegalArgumentException("index " + index + " not present in path " + this);
        }
        return elements[index];
    }
    
    @Override
    public String subpath(int beginIndex, int endIndex) {
        
        if (beginIndex < 0 || beginIndex >= elements.length) {
            throw new IllegalArgumentException("beginIndex " + beginIndex + " not present in path " + this);
        }
        
        if (endIndex < 0 || endIndex >= elements.length) {
            throw new IllegalArgumentException("endIndex " + beginIndex + " not present in path " + this);
        }
        
        if (beginIndex > endIndex) {
            throw new IllegalArgumentException("beginIndex " + beginIndex + " larger than endIndex " + endIndex);
        }
        
        StringBuilder tmp = new StringBuilder();
       
        for (int i=beginIndex;i<endIndex;i++) {
            tmp.append("/");
            tmp.append(elements[i]);
        }
        
        return tmp.toString();
    }

    @Override
    public boolean startsWith(String other) {
        // TODO: implement;
        return false;
    }

    @Override
    public boolean endsWith(String other) {
        // TODO: implement;
        return false;
    }

    @Override
    public Path resolve(String other) {
        // TODO: implement;
        return null;
    }

    @Override
    public Path resolveSibling(String other) throws OctopusException {
        // TODO: implement;
        return null;
    }

    @Override
    public String relativize(Path other) throws OctopusException {
        // TODO: implement;
        return null;
    }

    @Override
    public Iterator<Path> iterator() {
        // TODO: implement;
        return null;
    }
    
    @Override
    public String getPath() {

        if (elements.length == 0) {
            return null;
        }
        
        StringBuilder tmp = new StringBuilder();
        
        for (int i = 0; i < elements.length; i++) {
            tmp.append("/");
            tmp.append(elements[i]);
        }

        return tmp.toString();
    }

    public String toString() {
        return filesystem.toString() + getPath();
    }

    @Override
    public FileSystem getFileSystem() {
        return filesystem;
    }

    @Override
    public Path normalize() {
        // TODO Auto-generated method stub
        return null;
    }

}
