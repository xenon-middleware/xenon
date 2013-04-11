package nl.esciencecenter.octopus.files;

import java.util.Arrays;
import java.util.Iterator;

import nl.esciencecenter.octopus.exceptions.OctopusException;

public class RelativePath {

    public static final String DEFAULT_SEPERATOR = "/";
    
    private final String[] elements;
    private final String seperator;
    
    public RelativePath(String path) {
        this(path, DEFAULT_SEPERATOR);
    }

    public RelativePath(String [] elements) {
        this(elements, DEFAULT_SEPERATOR);
    }
    
    public RelativePath(String path, String separator) {

        if (separator != null) { 
            this.seperator = separator;
        } else { 
            this.seperator = DEFAULT_SEPERATOR;
        }
        
        if (path == null) {
            elements = new String[0];
        } else {
            // FIXME: does this work for multi char separators???
            this.elements = path.split(this.seperator + "+");
        }

        //      String pathString = location.getPath();
        //
        //      if (pathString == null) {
        //          root = null;
        //          elements = new String[0];
        //      } else if (isLocal() && OSUtils.isWindows()) {
        //          if (location.getPath().matches("^/[a-zA-Z]:/")) {
        //              root = pathString.substring(0, 4);
        //              pathString = pathString.substring(4);
        //          } else {
        //              root = null;
        //          }
        //          this.elements = pathString.split("/+");
        //      } else {
        //          if (pathString.startsWith("/")) {
        //              root = "/";
        //              pathString = pathString.substring(1);
        //          } else {
        //              root = null;
        //          }
        //          this.elements = pathString.split("/+");
        //      }

    }

    public RelativePath(String[] elements, String separator) {

        if (separator != null) { 
            this.seperator = separator;
        } else { 
            this.seperator = DEFAULT_SEPERATOR;
        }

        if (elements == null) {
            this.elements = new String[0];
        } else {
            this.elements = elements.clone();
        }
    }

    public String getFileName() {
        if (elements.length == 0) {
            return null;
        }
        return elements[elements.length - 1];
    }

    public RelativePath getParent() {
        
        if (elements.length == 0) {
            return null;
        }

        String[] parentElements = Arrays.copyOfRange(elements, 0, elements.length - 1);

        return new RelativePath(parentElements, seperator);
    }

    public int getNameCount() {
        return elements.length;
    }

    public String[] getNames() {
        return elements.clone();
    }

    public String getName(int index) {
        if (index >= elements.length) {
            throw new IllegalArgumentException("index " + index + " not present in path " + this);
        }
        return elements[index];
    }

    public RelativePath subpath(int beginIndex, int endIndex) {

        if (beginIndex < 0 || beginIndex >= elements.length) {
            throw new IllegalArgumentException("beginIndex " + beginIndex + " not present in path " + this);
        }

        if (endIndex < 0 || endIndex >= elements.length) {
            throw new IllegalArgumentException("endIndex " + beginIndex + " not present in path " + this);
        }

        if (beginIndex >= endIndex) {
            throw new IllegalArgumentException("beginIndex " + beginIndex + " larger than endIndex " + endIndex);
        }
        
        String [] tmp = new String[endIndex-beginIndex];
        
        for (int i = beginIndex; i < endIndex; i++) {
            tmp[i-beginIndex] = elements[i];
        }

        return new RelativePath(tmp, seperator);
    }

    public boolean startsWith(RelativePath other) {
        return false;
    }

    public boolean endsWith(RelativePath other) {
        return false;
    }

    public RelativePath resolve(RelativePath other) {
        // TODO: implement;
        return null;
    }

    public RelativePath resolveSibling(RelativePath other) throws OctopusException {
        // TODO: implement;
        return null;
    }

    public RelativePath relativize(RelativePath other) throws OctopusException {
        // TODO: implement;
        return null;
    }

    public Iterator<RelativePath> iterator() {
        // TODO: implement;
        return null;
    }

    public String getPath() {

        if (elements.length == 0) {
            return null;
        }

        StringBuilder tmp = new StringBuilder();

        for (int i = 0; i < elements.length; i++) {
            tmp.append(seperator);
            tmp.append(elements[i]);
        }

        return tmp.toString();
    }

    public RelativePath normalize() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public String toString() {
        return getPath();
    }
    
}
