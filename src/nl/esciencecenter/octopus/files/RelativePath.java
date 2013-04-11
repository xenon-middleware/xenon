package nl.esciencecenter.octopus.files;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class RelativePath {

    public static final char DEFAULT_SEPERATOR = '/';
    
    private final String[] elements;
    private final String seperator;
    
    class RelativePathIterator implements Iterator<RelativePath> {

        private int index = 0;
        
        @Override
        public boolean hasNext() {
            return index <= elements.length; 
        }

        @Override
        public RelativePath next() {
            
            if (index > elements.length) {
                throw new NoSuchElementException("No more elements available!");
            }
            
            return new RelativePath(Arrays.copyOf(elements, index++), seperator);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove not supported!");
        } 
    }
    
    public RelativePath() {
        this(new String[0], "" + DEFAULT_SEPERATOR);
    }
    
    public RelativePath(String path) {
        this(path, "" + DEFAULT_SEPERATOR);
    }

    public RelativePath(String [] elements) {
        this(elements, "" + DEFAULT_SEPERATOR);
    }
    
    public RelativePath(RelativePath... paths) {
        
        if (paths.length == 0) {
            elements = new String[0];
            seperator = "" + DEFAULT_SEPERATOR;
            return;
        }

        if (paths.length == 1) {
            elements = paths[0].elements;
            seperator = paths[0].seperator;
            return;
        }
        
        String [] tmp = merge(paths[0].elements, paths[1].elements);
        
        for (int i=2;i<paths.length;i++) { 
            tmp = merge(tmp, paths[i].elements);
        }
     
        elements = tmp;
        seperator = paths[0].seperator;
    }
    
    public RelativePath(String path, String separator) {

        if (separator == null || separator.length() == 0) {
            this.seperator = "" + DEFAULT_SEPERATOR;
        } else { 
            if (separator.length() != 1) { 
                throw new IllegalArgumentException("Separator may not have more than one character!");
            }
            
            this.seperator = separator;
        }
        
        if (path == null) {
            elements = new String[0];
        } else {
            StringTokenizer tok = new StringTokenizer(path, this.seperator);

            elements = new String[tok.countTokens()];
            
            for (int i=0;i<elements.length;i++) { 
                elements[i] = tok.nextToken();
            }
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

        if (separator == null || separator.length() == 0) {
            this.seperator = "" + DEFAULT_SEPERATOR;
        } else { 
            if (separator.length() != 1) { 
                throw new IllegalArgumentException("Separator may not have more than one character!");
            }
            
            this.seperator = separator;
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

        if (endIndex < 0 || endIndex > elements.length) {
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

    private String [] merge(String [] a, String [] b) {
        
        int count = a.length + b.length;
        
        String [] tmp = new String[count];
        
        System.arraycopy(a, 0, tmp, 0, a.length);
        System.arraycopy(b, 0, tmp, a.length, b.length);
        
        return tmp;
    }
    
    public RelativePath resolve(RelativePath other) {
        
        if (other == null || other.isEmpty()) { 
            return this;
        }

        return new RelativePath(merge(elements, other.elements), seperator);
    }

    private boolean isEmpty() {
        return elements.length == 0;
    }

    public RelativePath resolveSibling(RelativePath other) {
        
        if (isEmpty()) {
            if (other == null) { 
                return this;
            } else { 
                return other;
            }
        }
        
        RelativePath parent = getParent();
        
        if (other == null || other.isEmpty()) { 
            return parent;
        }
        
        return parent.resolve(other);
    }

    public RelativePath relativize(RelativePath other) throws IllegalArgumentException {
        
        if (isEmpty()) { 
            return other;
        }
         
        if (other.isEmpty()) { 
            return this;
        }
        
        RelativePath normalized = normalize();
        RelativePath normalizedOther = other.normalize();

        String [] elts = normalized.elements;
        String [] eltsOther = normalizedOther.elements;
        
        // The source may not be longer that target
        if (elts.length > eltsOther.length) { 
            throw new IllegalArgumentException("Cannot relativize " + other.getPath() + " to " + getPath());
        }

        // If source and target must have the same start.
        for (int i=0;i<elts.length;i++) { 
            if (!elts[i].equals(eltsOther[i])) { 
                throw new IllegalArgumentException("Cannot relativize " + other.getPath() + " to " + getPath());
            }
        }

        if (elts.length == eltsOther.length) { 
            return new RelativePath(new String[0], seperator);
        }

        return new RelativePath(Arrays.copyOfRange(eltsOther, elts.length, eltsOther.length), seperator);
    }

    public Iterator<RelativePath> iterator() {
        return new RelativePathIterator();
    }

    public String getPath() {

        if (elements.length == 0) {
            return "";
        }

        StringBuilder tmp = new StringBuilder();

        for (int i = 0; i < elements.length; i++) {
            tmp.append(seperator);
            tmp.append(elements[i]);
        }

        return tmp.toString();
    }

    public RelativePath normalize() {
        
        if (isEmpty()) { 
            return this;
        }
        
        ArrayList<String> stack = new ArrayList<String>();
        
        for (String s : elements) { 
            stack.add(s);
        }
        
        boolean change = true;
        
        while (change) { 
            change = false;
            
            for (int i=stack.size()-1;i>=0;i--) { 

                if (i < stack.size()) { 

                    String elt = stack.get(i);

                    if (elt.equals(".")) { 
                        stack.remove(i);
                        change = true;
                        
                    } else if (elt.equals("..")) {
                        
                        if (i > 0) { 
                            String parent = stack.get(i-1);
                            
                            if (!(parent.equals(".") || parent.equals(".."))) {
                                // NOTE: order is VERY important here!
                                stack.remove(i);
                                stack.remove(i-1);
                                change = true;
                            }
                        }
                    }
                }
            }
        } 

        String [] tmp = stack.toArray(new String [stack.size()]);        
        return new RelativePath(tmp, seperator);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(elements);
        result = prime * result + ((seperator == null) ? 0 : seperator.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RelativePath other = (RelativePath) obj;
        if (!Arrays.equals(elements, other.elements))
            return false;
        if (seperator == null) {
            if (other.seperator != null)
                return false;
        } else if (!seperator.equals(other.seperator))
            return false;
        return true;
    }

    public String toString() {
        return getPath();
    }
}
