/**
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.esciencecenter.xenon.filesystems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * RelativePath contains a sequence of path elements separated by a separator.
 *
 * It is designed to be immutable.
 *
 * @version 1.0
 * @since 1.0
 */
public class Path implements Iterable<Path> {

    /** The default separator to use. */
    private static final char DEFAULT_SEPARATOR = '/';

    /** The path elements in this relative path */
    private final List<String> elements;

    /** The separator used in this relative path */
    private final char separator;

    /** Estimate of path element String length. */
    private static final int PATH_ELEMENT_LENGTH = 25;

    private class RelativePathIterator implements Iterator<Path> {
        private int index = 1;

        @Override
        public boolean hasNext() {
            return index <= elements.size();
        }

        @Override
        public Path next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements available!");
            }

            return subpath(0, index++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove not supported!");
        }
    }

    /**
     * Create a new empty RelativePath using the default separator.
     */
    public Path() {
        this(DEFAULT_SEPARATOR, new ArrayList<String>(0));
    }

    /**
     * Create a new RelativePath using the path and the default separator.
     * 
     * If <code>path</code> is <code>null</code> or an empty String, the resulting RelativePath is empty.
     * If <code>path</code> contains the separator it will be split into multiple elements. 
     * 
     * @param path
     *            the path to use.
     */
    public Path(String path) {
        this(DEFAULT_SEPARATOR, path);
    }

    /**
     * Create a new RelativePath using the given path elements and the default separator.
     * 
     * If <code>elements</code> is <code>null</code> or an empty String array, the resulting RelativePath is empty.
     * 
     * Any elements that are <code>null</code> or an empty String will be ignored.
     * Any elements that contain the separator will be split into multiple elements. 
     * 
     * @param elements
     *            the path elements to use.
     */
    public Path(String... elements) {
        this(DEFAULT_SEPARATOR, elements);
    }

    /**
     * Create a new RelativePath by appending the provided <code>paths</code>.
     * 
     * If the <code>paths</code> is <code>null</code> the resulting RelativePath is empty.
     * 
     * @param paths
     *            the path elements to use.
     */
    public Path(Path... paths) {
        if (paths == null || paths.length == 0) {
            elements = new ArrayList<>(0);
            separator = DEFAULT_SEPARATOR;
        } else {
            elements = new ArrayList<>(paths.length);
            for (Path path : paths) {
                if (path != null) {
                    elements.addAll(path.elements);
                }
            }
            separator = paths[0].separator;
        }
    }

    /**
     * Create a new RelativePath using the given path elements and the separator.
     * 
     * If the <code>elements</code> is <code>null</code> or an empty String array, the resulting RelativePath is empty.
     * 
     * Otherwise, each of the elements will be parsed individually, splitting them into elements wherever a separator is
     * encountered. Elements that are <code>null</code> or contain an empty String are ignored.
     * 
     * @param elements
     *            the path elements to use.
     * @param separator
     *            the separator to use.
     */
    public Path(char separator, String... elements) {
        this(separator, elements == null ? new ArrayList<String>(0) : Arrays.asList(elements));
    }

    /**
     * Create a new RelativePath using the given path elements and the separator.
     *
     * If the <code>elements</code> is <code>null</code> or an empty String array, the resulting RelativePath is empty.
     *
     * Otherwise, each of the elements will be parsed individually, splitting them into elements wherever a separator is
     * encountered. Elements that are <code>null</code> or contain an empty String are ignored.
     *
     * @param elts
     *            the path elements to use.
     * @param separator
     *            the separator to use.
     */
    public Path(char separator, List<String> elts) {
        
        List<String> tmp = elts;
        
        if (tmp == null) {
            tmp = new ArrayList<>(0);
        }
        
        this.separator = separator;

        String delim = String.valueOf(separator);

        this.elements = new ArrayList<>(tmp.size());
        for (String elt : tmp) {
            if (elt == null || elt.isEmpty()) {
                continue;
            }
            StringTokenizer tok = new StringTokenizer(elt, delim);

            while (tok.hasMoreTokens()) {
                this.elements.add(tok.nextToken());
            }
        }
    }
    /**
     * Get the file name, or <code>null</code> if the RelativePath is empty.
     * 
     * The file name is the last element of the RelativePath.
     * 
     * @return the resulting file name or <code>null</code>.
     */
    public Path getFileName() {
        if (isEmpty()) {
            return null;
        }
        
        return new Path(separator, getFileNameAsString());
    }

    /**
     * Get the file name as a <code>String</code>, or <code>null</code> if the RelativePath is empty.
     * 
     * The file name is the last element of the RelativePath.
     * 
     * @return the resulting file name or <code>null</code>.
     */
    public String getFileNameAsString() {
        if (isEmpty()) {
            return null;
        }
        
        return elements.get(elements.size() - 1);
    }
    
    /**
     * Get the separator.
     * 
     * @return the separator.
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * Get the parent RelativePath, or <code>null</code> if this RelativePath does not have a parent.
     * 
     * @return a RelativePath representing this RelativePaths parent.
     */
    public Path getParent() {
        if (isEmpty()) {
            return null;
        }

        return new Path(separator, elements.subList(0, elements.size() - 1));
    }

    /**
     * Get the number of name elements in the RelativePath.
     * 
     * @return the number of elements in the RelativePath, or 0 if this is empty.
     */
    public int getNameCount() {
        return elements.size();
    }

    /**
     * Get a name element of this RelativePath.
     * 
     * @param index
     *            the index of the element
     * 
     * @return the name element
     * 
     * @throws IndexOutOfBoundsException
     *             If the index is negative or greater or equal to the number of elements in the path.
     */
    public Path getName(int index) {
        return new Path(separator, elements.get(index));
    }

    /**
     * Returns a RelativePath that is a subsequence of the name elements of this path.
     * 
     * @param beginIndex
     *            the index of the first element, inclusive
     * @param endIndex
     *            the index of the last element, exclusive
     * 
     * @return a new RelativePath that is a subsequence of the name elements in this path.
     * 
     * @throws IllegalArgumentException
     *             If beginIndex is larger than or equal to the endIndex.
     * @throws ArrayIndexOutOfBoundsException
     *             If beginIndex &lt; 0 or beginIndex &gt; elements.length
     */
    public Path subpath(int beginIndex, int endIndex) {
        if (beginIndex == endIndex) {
            throw new IllegalArgumentException("beginIndex " + beginIndex + " equal to endIndex " + endIndex);
        }

        return new Path(separator, elements.subList(beginIndex, endIndex));
    }

    /**
     * Tests if this RelativePath starts with the given RelativePath.
     * 
     * This method returns <code>true</code> if this RelativePath starts with the name elements in the given RelativePath. If the 
     * given RelativePath has more name elements than this path then false is returned.
     * 
     * @param other
     *            the RelativePath to compare to.
     * 
     * @return If this RelativePath start with the name elements in the other RelativePath.
     */
    public boolean startsWith(Path other) {
        return other.elements.size() <= elements.size()
            && elements.subList(0, other.elements.size()).equals(other.elements);
    }

    /**
     * Tests if this RelativePath ends with the given RelativePath.
     * 
     * This method returns <code>true</code> if this RelativePath end with the name elements in the given RelativePath. If the 
     * given RelativePath has more name elements than this RelativePath then false is returned.
     * 
     * @param other
     *            the RelativePath to compare to.
     * 
     * @return If this RelativePath ends with the name elements in the other RelativePath.
     */
    public boolean endsWith(Path other) {
        int offset = elements.size() - other.elements.size();

        return other.elements.size() <= elements.size() &&
            this.elements.subList(offset, elements.size()).equals(other.elements);
    }

    /**
     * Tests if this RelativePath starts with the given RelativePath.
     *
     * This method converts <code>other</code> into a <code>RelativePath</code> using {@link #RelativePath(String)} and then uses 
     * {@link #startsWith(Path)} to compare the result to this RelativePath.
     * 
     * @param other
     *            the path to test.
     * 
     * @return If this RelativePath start with the name elements in <code>other</code>.
     */
    public boolean startsWith(String other) {
        return startsWith(new Path(other));
    }

    /**
     * Tests if this RelativePath ends with the given RelativePath.
     * 
     * This method converts the <code>other</code> into a <code>RelativePath</code> using {@link #RelativePath(String)} and then uses 
     * {@link #endsWith(Path)} to compare the result to this path.
     * 
     * @param other
     *            the path to test.
     * 
     * @return If this RelativePath ends with the elements in <code>other</code>.
     */
    public boolean endsWith(String other) {
        return endsWith(new Path(other));
    }

    /**
     * Resolve a RelativePath against this RelativePath.
     * 
     * Concatenates the path elements of this RelativePath with the
     * <code>other</code> RelativePath.
     * 
     * @param other
     *            the RelativePath to concatenate with.
     * @return concatenation of this RelativePath with the other
     */
    public Path resolve(Path other) {
        if (other == null || other.isEmpty()) {
            return this;
        } else if (isEmpty()) {
            return other;
        }

        ArrayList<String> tmp = new ArrayList<>(elements.size() + other.elements.size());
        tmp.addAll(elements);
        tmp.addAll(other.elements);
        return new Path(separator, tmp);
    }

    /**
     * Resolve a String containing a RelativePath against this path.
     * 
     * Converts the <code>other</code> into a <code>RelativePath</code> using {@link #RelativePath(String)} and then uses 
     * {@link #resolve(Path)} to resolve the result against this path.
     * 
     * @param other
     *            the String to concatenate with.
     * @return concatenation of this RelativePath with the other
     */
    public Path resolve(String other) {
        if (other == null || other.isEmpty()) {
            return this;
        }

        return resolve(new Path(other));
    }

    /**
     * Is this RelativePath empty ?
     * 
     * @return If this RelativePath is empty.
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * Resolves the given RelativePath to this paths parent RelativePath, thereby creating a sibling to this RelativePath.
     * 
     * If this RelativePath is empty, <code>other</code> will be returned, unless other is <code>null</code> in which case an 
     * empty RelativePath is returned.
     * 
     * If this RelativePath is not empty, but <code>other</code> is <code>null</code> or empty, the parent of this RelativePath 
     * will be returned. 
     *    
     * If neither this RelativePath and other are empty, <code>getParent.resolve(other)</code> will be returned.
     * 
     * @param other
     *            the RelativePath to resolve as sibling.
     * 
     * @return a RelativePath representing the sibling.
     * 
     * @throws IllegalArgumentException
     *             If the RelativePath can not be resolved as a sibling to this RelativePath.
     */
    public Path resolveSibling(Path other) {
        if (isEmpty()) {
            if (other == null) {
                return this;
            } else {
                return other;
            }
        }

        return getParent().resolve(other);
    }

    /**
     * Create a relative RelativePath between the given RelativePath and this RelativePath.
     * 
     * Relativation is the inverse of resolving. This method returns a RelativePath that, when resolved against this RelativePath,
     * results in the given RelativePath <code>other</code>.   
     * 
     * @param other
     *            the RelativePath to relativize.
     * 
     * @return a RelativePath representing a relative path between the given path and this path.
     * 
     * @throws IllegalArgumentException
     *             If the path can not be relativized to this path.
     */
    public Path relativize(Path other) {
        if (isEmpty()) {
            return other;
        }

        List<String> normalized = normalize().elements;
        List<String> normalizedOther = other.normalize().elements;

        // The source may not be longer that target
        if (normalized.size() > normalizedOther.size()) {
            throw new IllegalArgumentException("Cannot relativize " + other.getAbsolutePath() + " to " + getAbsolutePath());
        }

        // Source and target must have the same start.
        if (!normalizedOther.subList(0, normalized.size()).equals(normalized)) {
            throw new IllegalArgumentException("Cannot relativize " + other.getAbsolutePath() + " to " + getAbsolutePath());
        }

        return new Path(separator, normalizedOther.subList(normalized.size(), normalizedOther.size()));
    }

    /**
     * Create an {@link Iterator} that returns all possible sub RelativePaths of this RelativePath, in order of increasing length.
     * 
     * For example, for the RelativePath "/a/b/c/d" the iterator returns "/a", "/a/b", "/a/b/c", "/a/b/c/d".
     * 
     * @return the iterator.
     */
    public Iterator<Path> iterator() {
        return new RelativePathIterator();
    }

    /**
     * Return a <code>String</code> representation of this RelativePath interpreted as a relative path.
     * 
     * A relative path does not start with a separator. 
     * 
     * @return a String representation of this RelativePath interpreted as a relative path.
     */
    public String getRelativePath() {
        StringBuilder tmp = new StringBuilder(elements.size() * PATH_ELEMENT_LENGTH);

        String sep = "";
        
        for (String element : elements) {
            tmp.append(sep);
            tmp.append(element);
            sep = String.valueOf(separator);
        }
        
        return tmp.toString();
    }

    /**
     * Return a <code>String</code> representation of this RelativePath interpreted as an absolute path.
     * 
     * An absolute path starts with a separator. 
     * 
     * @return a String representation of this path interpreted as an absolute path.
     */
    public String getAbsolutePath() {
        return separator + getRelativePath();
    }

    /**
     * Normalize this RelativePath by removing as many redundant path elements as possible.
     * 
     * Redundant path elements are <code>"."</code> (indicating the current directory) and <code>".."</code> (indicating the
     * parent directory).
     * 
     * Note that the resulting normalized path does may still contain <code>".."</code> elements which are not redundant.
     * 
     * @return the normalize path.
     */
    public Path normalize() {
        if (isEmpty()) {
            return this;
        }

        ArrayList<String> stack = new ArrayList<>(elements);

        boolean change = true;

        while (change) {
            change = false;

            for (int i = stack.size() - 1; i >= 0; i--) {
                if (i >= stack.size()) {
                    continue;
                }

                String elt = stack.get(i);

                if (".".equals(elt)) {
                    stack.remove(i);
                    change = true;
                } else if (i > 0 && "..".equals(elt)) {
                    String parent = stack.get(i - 1);

                    if (!(".".equals(parent) || "..".equals(parent))) {
                        stack.subList(i - 1, i + 1).clear();
                        change = true;
                    }
                }
            }
        }

        return new Path(separator, stack);
    }

    /* Generated */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + elements.hashCode();
        result = prime * result + separator;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Path other = (Path) obj;
        return separator == other.separator && elements.equals(other.elements);
    }

    @Override
    public String toString() {
        return "RelativePath [element=" + elements + ", seperator=" + separator + "]";
    }
}
