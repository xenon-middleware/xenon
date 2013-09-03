/*
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
package nl.esciencecenter.octopus.files;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * RelativePath contains a sequence of path elements separated by a separator.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class RelativePath {

    /** The default separator to use. */
    public static final char DEFAULT_SEPARATOR = '/';

    /** The path elements in this relative path */
    private final String[] elements;

    /** The separator used in this relative path */
    private final char separator;

    class RelativePathIterator implements Iterator<RelativePath> {

        private int index = 1;

        @Override
        public boolean hasNext() {
            return index <= elements.length;
        }

        @Override
        public RelativePath next() {

            if (index > elements.length) {
                throw new NoSuchElementException("No more elements available!");
            }

            return new RelativePath(separator, Arrays.copyOf(elements, index++));
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove not supported!");
        }
    }

    /**
     * Create a new empty RelativePath using the default separator.
     */
    public RelativePath() {
        this(DEFAULT_SEPARATOR, new String[0]);
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
    public RelativePath(String path) {
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
    public RelativePath(String... elements) {
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
    public RelativePath(RelativePath... paths) {

        if (paths.length == 0) {
            elements = new String[0];
            separator = DEFAULT_SEPARATOR;
            return;
        }

        if (paths.length == 1) {
            elements = paths[0].elements;
            separator = paths[0].separator;
            return;
        }

        String[] tmp = merge(paths[0].elements, paths[1].elements);

        for (int i = 2; i < paths.length; i++) {
            tmp = merge(tmp, paths[i].elements);
        }

        elements = tmp;
        separator = paths[0].separator;
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
    public RelativePath(char separator, String... elements) {

        this.separator = separator;

        if (elements == null || elements.length == 0) {
            this.elements = new String[0];
        } else {

            ArrayList<String> tmp = new ArrayList<String>();

            for (int i = 0; i < elements.length; i++) {

                String elt = elements[i];

                if (elt != null && elt.length() > 0) {
                    StringTokenizer tok = new StringTokenizer(elt, "" + this.separator);

                    while (tok.hasMoreTokens()) {
                        tmp.add(tok.nextToken());
                    }
                }
            }

            this.elements = tmp.toArray(new String[tmp.size()]);
        }
    }

    /**
     * Get the file name, or <code>null</code> if the RelativePath is empty.
     * 
     * The file name is the last element of the RelativePath.
     * 
     * @return the resulting file name or <code>null</code>.
     */
    public RelativePath getFileName() {
        if (elements.length == 0) {
            return null;
        }
        
        return new RelativePath(separator, elements[elements.length - 1]);
    }

    /**
     * Get the file name as a <code>String</code>, or <code>null</code> if the RelativePath is empty.
     * 
     * The file name is the last element of the RelativePath.
     * 
     * @return the resulting file name or <code>null</code>.
     */
    public String getFileNameAsString() {
        if (elements.length == 0) {
            return null;
        }
        
        return elements[elements.length - 1];
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
    public RelativePath getParent() {

        if (elements.length == 0) {
            return null;
        }

        String[] parentElements = Arrays.copyOfRange(elements, 0, elements.length - 1);

        return new RelativePath(separator, parentElements);
    }

    /**
     * Get the number of name elements in the RelativePath.
     * 
     * @return the number of elements in the RelativePath, or 0 if this is empty.
     */
    public int getNameCount() {
        return elements.length;
    }

    /**
     * Get a name element of this RelativePath.
     * 
     * @param index
     *            the index of the element
     * 
     * @return the name element
     * 
     * @throws IllegalArgumentException
     *             If the index is negative or greater or equal to the number of elements in the path.
     */
    public RelativePath getName(int index) {
        if (index < 0 || index >= elements.length) {
            throw new IllegalArgumentException("index " + index + " not present in path " + this);
        }
        return new RelativePath(separator, elements[index]);
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
     *             If the beginIndex or endIndex is negative or greater or equal to the number of elements in the path, or if
     *             beginIndex is larger that or equal to the endIndex.
     */
    public RelativePath subpath(int beginIndex, int endIndex) {

        if (beginIndex < 0 || beginIndex >= elements.length) {
            throw new IllegalArgumentException("beginIndex " + beginIndex + " not present in path " + this);
        }

        if (endIndex < 0 || endIndex > elements.length) {
            throw new IllegalArgumentException("endIndex " + endIndex + " not present in path " + this);
        }

        if (beginIndex >= endIndex) {
            throw new IllegalArgumentException("beginIndex " + beginIndex + " larger than endIndex " + endIndex);
        }

        String[] tmp = new String[endIndex - beginIndex];

        for (int i = beginIndex; i < endIndex; i++) {
            tmp[i - beginIndex] = elements[i];
        }

        return new RelativePath(separator, tmp);
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
    public boolean startsWith(RelativePath other) {

        if (other.isEmpty()) {
            return true;
        }

        if (isEmpty()) {
            return false;
        }

        if (other.elements.length > elements.length) {
            return false;
        }

        for (int i = 0; i < other.elements.length; i++) {
            if (!other.elements[i].equals(elements[i])) {
                return false;
            }
        }

        return true;
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
    public boolean endsWith(RelativePath other) {

        if (other.isEmpty()) {
            return true;
        }

        if (isEmpty()) {
            return false;
        }

        if (other.elements.length > elements.length) {
            return false;
        }

        int offset = elements.length - other.elements.length;

        for (int i = 0; i < other.elements.length; i++) {
            if (!other.elements[i].equals(elements[offset + i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Tests if this RelativePath starts with the given RelativePath.
     *
     * This method converts <code>other</code> into a <code>RelativePath</code> using {@link #RelativePath(String)} and then uses 
     * {@link #startsWith(RelativePath)} to compare the result to this RelativePath.
     * 
     * @param other
     *            the path to test.
     * 
     * @return If this RelativePath start with the name elements in <code>other</code>.
     */
    public boolean startsWith(String other) {
        return startsWith(new RelativePath(other));
    }

    /**
     * Tests if this RelativePath ends with the given RelativePath.
     * 
     * This method converts the <code>other</code> into a <code>RelativePath</code> using {@link #RelativePath(String)} and then uses 
     * {@link #endsWith(RelativePath)} to compare the result to this path.
     * 
     * @param other
     *            the path to test.
     * 
     * @return If this RelativePath ends with the elements in <code>other</code>.
     */
    public boolean endsWith(String other) {
        return endsWith(new RelativePath(other));
    }
    
    /**
     * Concatenate two String arrays.
     * 
     * @param a
     *            the first String array.
     * @param b
     *            the first String array.
     * 
     * @return the concatenation of the two String arrays.
     */
    private String[] merge(String[] a, String[] b) {

        int count = a.length + b.length;

        String[] tmp = new String[count];

        System.arraycopy(a, 0, tmp, 0, a.length);
        System.arraycopy(b, 0, tmp, a.length, b.length);

        return tmp;
    }

    /**
     * Resolve a RelativePath against this RelativePath.
     * 
     * If <code>other</code> represents an empty RelativePath, this RelativePath is returned.
     * 
     * If this RelativePath is empty, the <code>other</code> RelativePath is returned.
     * 
     * Otherwise, a new RelativePath is returned that contains the concatenation of the path elements this RelativePath and the
     * <code>other</code> RelativePath.
     * 
     * @param other
     *            the RelativePath.
     */
    public RelativePath resolve(RelativePath other) {

        if (other == null || other.isEmpty()) {
            return this;
        }

        if (isEmpty()) {
            return other;
        }

        return new RelativePath(separator, merge(elements, other.elements));
    }

    /**
     * Resolve a String containing a RelativePath against this path.
     * 
     * This method converts the <code>other</code> into a <code>RelativePath</code> using {@link #RelativePath(String)} and then uses 
     * {@link #resolve(RelativePath)} to resolve the result against this path.
     *
     * If <code>other</code> represents an empty path, or <code>null</code> this RelativePath is returned.
     * 
     * @param other
     *            the path.
     */
    public RelativePath resolve(String other) {

        if (other == null || other.length() == 0) {
            return this;
        }

        return resolve(new RelativePath(other));
    }

    /**
     * Is this RelativePath empty ?
     * 
     * @return If this RelativePath is empty.
     */
    public boolean isEmpty() {
        return elements.length == 0;
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
    public RelativePath relativize(RelativePath other) {

        if (isEmpty()) {
            return other;
        }

        if (other.isEmpty()) {
            return this;
        }

        RelativePath normalized = normalize();
        RelativePath normalizedOther = other.normalize();

        String[] elts = normalized.elements;
        String[] eltsOther = normalizedOther.elements;

        // The source may not be longer that target
        if (elts.length > eltsOther.length) {
            throw new IllegalArgumentException("Cannot relativize " + other.getAbsolutePath() + " to " + getAbsolutePath());
        }

        // If source and target must have the same start.
        for (int i = 0; i < elts.length; i++) {
            if (!elts[i].equals(eltsOther[i])) {
                throw new IllegalArgumentException("Cannot relativize " + other.getAbsolutePath() + " to " + getAbsolutePath());
            }
        }

        if (elts.length == eltsOther.length) {
            return new RelativePath(separator, new String[0]);
        }

        return new RelativePath(separator, Arrays.copyOfRange(eltsOther, elts.length, eltsOther.length));
    }

    /**
     * Create an {@link Iterator} that returns all possible sub RelativePaths of this RelativePath, in order of increasing length.
     * 
     * For example, for the RelativePath "/a/b/c/d" the iterator returns "/a", "/a/b", "a/b/c", "/a/b/c/d".
     * 
     * @return the iterator.
     */
    public Iterator<RelativePath> iterator() {
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

        if (elements.length == 0) {
            return "";
        }

        StringBuilder tmp = new StringBuilder();

        String sep = "";
        
        for (int i = 0; i < elements.length; i++) {
            tmp.append(sep);
            tmp.append(elements[i]);
            sep = "" + separator;
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
        
        if (elements.length == 0) {
            return "" + separator;
        }

        StringBuilder tmp = new StringBuilder();

        for (int i = 0; i < elements.length; i++) {
            tmp.append(separator);
            tmp.append(elements[i]);
        }
        
        return tmp.toString();
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

            for (int i = stack.size() - 1; i >= 0; i--) {

                if (i < stack.size()) {

                    String elt = stack.get(i);

                    if (elt.equals(".")) {
                        stack.remove(i);
                        change = true;

                    } else if (i > 0 && elt.equals("..")) {
                        String parent = stack.get(i - 1);

                        if (!(parent.equals(".") || parent.equals(".."))) {
                            // NOTE: order is VERY important here!
                            stack.remove(i);
                            stack.remove(i - 1);
                            change = true;
                        }
                    }
                }
            }
        }

        String[] tmp = stack.toArray(new String[stack.size()]);
        return new RelativePath(separator, tmp);
    }

    /* Generated */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(elements);
        result = prime * result + separator;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        RelativePath other = (RelativePath) obj;

        if (separator != other.separator) {
            return false;
        }

        return Arrays.equals(elements, other.elements);
    }

    @Override
    public String toString() {
        return "RelativePath [element=" + Arrays.toString(elements) + ", seperator=" + separator + "]";
    }
}
