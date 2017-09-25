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
package nl.esciencecenter.xenon.filesystems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Path contains a sequence of path elements separated by a separator.
 *
 * It is designed to be immutable.
 *
 * @version 1.0
 * @since 1.0
 */
public class Path implements Iterable<Path> {

    /** The default separator to use. */
    public static final char DEFAULT_SEPARATOR = '/';

    /** The path elements in this path */
    private final List<String> elements;

    /** The separator used in this path */
    private final char separator;

    /** Does path start with / ? **/
    private boolean isAbsolute;

    /** Estimate of path element String length. */
    private static final int PATH_ELEMENT_LENGTH = 25;

    private class PathIterator implements Iterator<Path> {
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
     * Create a new empty Path using the default separator.
     */
    public Path() {
        this(DEFAULT_SEPARATOR, false, new ArrayList<String>(0));
    }

    /**
     * Create a new Path by parsing <code>path</code> using the default separator.
     *
     * If <code>path</code> is <code>null</code> or an empty String, the resulting Path is empty and relative. Otherwise it will be split into multiple elements
     * using the default separator. If <code>path</code> starts with a separator, the resulting path will be absolute. Otherwise, it will be relative.
     *
     * @param path
     *            the path to parse
     */
    public Path(String path) {
        this(DEFAULT_SEPARATOR, path);
    }

    /**
     * Create a new Path by parsing <code>path</code> using the provided separator.
     *
     * If <code>path</code> is <code>null</code> or an empty String, the resulting Path is empty and relative. Otherwise it will be split into multiple elements
     * using the provided separator. If <code>path</code> starts with a separator, the resulting path will be absolute. Otherwise, it will be relative.
     *
     * @param separator
     *            the separator to use
     * @param path
     *            the path to parse
     */
    public Path(char separator, String path) {

        this.separator = separator;

        if (path == null || path.isEmpty()) {
            this.isAbsolute = false;
            this.elements = new ArrayList<>(0);
        } else {
            this.isAbsolute = (path.indexOf(separator) == 0);
            StringTokenizer tok = new StringTokenizer(path, "" + separator);

            this.elements = new ArrayList<>(tok.countTokens());

            while (tok.hasMoreTokens()) {
                this.elements.add(tok.nextToken());
            }
        }
    }

    /**
     * Create a new Path using the given path elements and the default separator.
     *
     * If <code>elements</code> is <code>null</code> or empty, the resulting Path is empty. The <code>elements</code> may not contain <code>null</code> or empty
     * Strings. The elements will not be parsed individually, and may not contain the separator character.
     *
     * @param isAbsolute
     *            should the resulting path must be absolute?
     * @param elements
     *            the path elements to use.
     */
    public Path(boolean isAbsolute, String... elements) {
        this(isAbsolute, elements == null ? new ArrayList<String>(0) : Arrays.asList(elements));
    }

    /**
     * Create a new Path using the given path elements and the provided separator.
     *
     * If <code>elements</code> is <code>null</code> or empty, the resulting Path is empty. The <code>elements</code> may not contain <code>null</code> or empty
     * Strings. The elements will not be parsed individually, and may not contain the separator character.
     *
     * @param separator
     *            the separator to use.
     * @param isAbsolute
     *            should the resulting path must be absolute?
     * @param elements
     *            the path elements to use.
     */
    public Path(char separator, boolean isAbsolute, String... elements) {
        this(separator, isAbsolute, elements == null ? new ArrayList<String>(0) : Arrays.asList(elements));
    }

    /**
     * Create a new Path using the given path elements and the default separator.
     *
     * If <code>elements</code> is <code>null</code> or empty, the resulting Path is empty. The <code>elements</code> list may not contain <code>null</code> or
     * empty Strings, or Strings that contain the separator character. If they do, an <code>IllegalArgumentException</code> will be thrown.
     *
     * @param isAbsolute
     *            should the resulting path must be absolute?
     * @param elements
     *            the path elements to use.
     */
    public Path(boolean isAbsolute, List<String> elements) {
        this(DEFAULT_SEPARATOR, isAbsolute, elements);
    }

    /**
     * Create a new Path using the given path separator, absoluteness, and elements.
     *
     * If <code>elements</code> is <code>null</code> or empty, the resulting Path is empty. The <code>elements</code> list may not contain <code>null</code> or
     * empty Strings, or Strings that contain the separator character. If they do, an <code>IllegalArgumentException</code> will be thrown.
     *
     * @param separator
     *            the separator to use.
     * @param isAbsolute
     *            should the resulting path should be absolute ?
     * @param elements
     *            the path elements to use.
     */
    public Path(char separator, boolean isAbsolute, List<String> elements) {
        this.separator = separator;
        this.isAbsolute = isAbsolute;
        this.elements = checkForNullAndSeparator(separator, elements);
    }

    protected static List<String> checkForNullAndSeparator(char separator, List<String> elements) {

        ArrayList<String> result = new ArrayList<String>(0);

        if (elements == null || elements.isEmpty()) {
            return result;
        }

        for (String s : elements) {
            if (s == null) {
                throw new IllegalArgumentException("Path elements list contains null");
            }

            if (s.isEmpty()) {
                throw new IllegalArgumentException("Path elements list contains an empty element");
            }

            if (s.indexOf(separator) != -1) {
                throw new IllegalArgumentException("Path element " + s + " contains separator '" + separator + "'");
            }

            result.add(s);
        }

        return result;
    }

    // List<String> filterNonEmpty(List<String> elts) {
    // List<String> res = new LinkedList<>();
    // for (String s : elts) {
    // if (s != null && !s.isEmpty()) {
    // res.add(s);
    // }
    // }
    // return res;
    // }

    /**
     * Get the file name or <code>null</code> if the Path is empty.
     *
     * The file name is the last element of the Path.
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
     * Get the file name as a <code>String</code>, or <code>null</code> if the Path is empty.
     *
     * The file name is the last element of the Path.
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
     * Get the parent Path, or <code>null</code> if this Path does not have a parent.
     *
     * The parent path will contain all path elements in this pasth except the last one.
     *
     * @return a Path representing this Paths parent.
     */
    public Path getParent() {
        if (isEmpty() || elements.size() == 1) {
            return null;
        }

        return new Path(separator, isAbsolute, elements.subList(0, elements.size() - 1));
    }

    /**
     * Get the number of elements in the Path.
     *
     * @return the number of elements in the Path, or 0 if this path is empty.
     */
    public int getNameCount() {
        return elements.size();
    }

    /**
     * Get an element of this Path. Path elements indices start at 0.
     *
     * @param index
     *            the index of the element
     *
     * @return the element at the specified index
     *
     * @throws IndexOutOfBoundsException
     *             If the index is negative or greater or equal to the number of elements in the path.
     */
    public Path getName(int index) {
        boolean alsoAbsolute = (index == 0 && isAbsolute);
        return new Path(separator, alsoAbsolute, Collections.singletonList(elements.get(index)));
    }

    /**
     * Returns a Path that is a subsequence of the name elements of this path. Path elements indices start at 0.
     *
     * @param beginIndex
     *            the index of the first element, inclusive
     * @param endIndex
     *            the index of the last element, exclusive
     *
     * @return a new Path that is a subsequence of the name elements in this path.
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
        boolean alsoAbsolute = beginIndex == 0 && isAbsolute;
        return new Path(separator, alsoAbsolute, elements.subList(beginIndex, endIndex));
    }

    /**
     * Tests if this Path starts with the given Path.
     *
     * This method returns <code>true</code> if this Path starts with the same sequence of the name elements as the given Path. In addition, the absoluteness of
     * this Path should match the absoluteness of the given Path.
     *
     * False is returned if the given Path starts with a different sequence of elements, has more name elements than this path, or has a different absoluteness
     * than this path.
     *
     * @param other
     *            the Path to compare to.
     *
     * @return If this Path start with the name elements in the other Path and has the same absoluteness.
     */
    public boolean startsWith(Path other) {
        return other.isAbsolute == isAbsolute && other.elements.size() <= elements.size() && elements.subList(0, other.elements.size()).equals(other.elements);
    }

    /**
     * Tests if this Path ends with the given Path.
     *
     * This method returns <code>true</code> if this Path end with the name elements in the given Path. If the given Path has more name elements than this Path
     * then false is returned.
     *
     * @param other
     *            the Path to compare to.
     *
     * @return If this Path ends with the name elements in the other Path.
     */
    public boolean endsWith(Path other) {
        if (other.isAbsolute) {
            return equals(other);
        }
        int offset = elements.size() - other.elements.size();

        return other.elements.size() <= elements.size() && this.elements.subList(offset, elements.size()).equals(other.elements);
    }

    /**
     * Tests if this Path starts with the given Path represented as a String.
     *
     * This method converts <code>other</code> into a <code>Path</code> using {@link #Path(String)} and then uses {@link #startsWith(Path)} to compare the
     * result to this Path.
     *
     * @param other
     *            the path to test.
     *
     * @return If this Path start with the name elements in <code>other</code>.
     */
    public boolean startsWith(String other) {
        return startsWith(new Path(separator, other));
    }

    /**
     * Tests if this Path ends with the given Path represented as a String.
     *
     * This method converts the <code>other</code> into a <code>Path</code> using {@link #Path(String)} and then uses {@link #endsWith(Path)} to compare the
     * result to this path.
     *
     * @param other
     *            the path to test.
     *
     * @return If this Path ends with the elements in <code>other</code>.
     */
    public boolean endsWith(String other) {
        return endsWith(new Path(separator, other));
    }

    /**
     * Resolve a Path against this Path.
     *
     * Returns a new Path that concatenates the path elements of this Path with <code>other</code>. The resulting path will use the separator and absoluteness
     * of this path.
     *
     * @param other
     *            the Path to concatenate with.
     * @return concatenation of this Path with the other
     */
    public Path resolve(Path other) {
        if (other == null || other.isEmpty()) {
            return this;
        }

        ArrayList<String> tmp = new ArrayList<>(elements.size() + other.elements.size());
        tmp.addAll(elements);
        tmp.addAll(other.elements);
        return new Path(separator, isAbsolute, tmp);
    }

    /**
     * Resolve a Path represented as a String against this path.
     *
     * Converts <code>other</code> into a <code>Path</code> using {@link #Path(char, String)} and the separator of the this path. Next, {@link #resolve(Path)}
     * is used to resolve the result against this path.
     *
     * @param other
     *            the path to concatenate with.
     *
     * @return concatenation of this Path with the other
     */
    public Path resolve(String other) {
        if (other == null || other.isEmpty()) {
            return this;
        }

        return resolve(new Path(separator, other));
    }

    /**
     * Is this Path empty ?
     *
     * @return If this Path is empty.
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * Resolves the given Path to this paths parent Path, thereby creating a sibling to this Path.
     *
     * If this Path is empty, <code>other</code> will be returned, unless other is <code>null</code> in which case an empty Path is returned.
     *
     * If this Path is not empty, but <code>other</code> is <code>null</code> or empty, the parent of this Path will be returned.
     *
     * If neither this Path and other are empty, <code>getParent.resolve(other)</code> will be returned.
     *
     * @param other
     *            the Path to resolve as sibling.
     *
     * @return a Path representing the sibling.
     *
     * @throws IllegalArgumentException
     *             If the Path can not be resolved as a sibling to this Path.
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
     * Create a relative Path between the given Path and this Path.
     *
     * Relativation is the inverse of resolving. This method returns a Path that, when resolved against this Path, results in the given Path <code>other</code>.
     *
     * @param other
     *            the Path to relativize.
     *
     * @return a Path representing a relative path between the given path and this path.
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
            throw new IllegalArgumentException("Cannot relativize " + other + " to " + this);
        }

        // Source and target must have the same start.
        if (!normalizedOther.subList(0, normalized.size()).equals(normalized)) {
            throw new IllegalArgumentException("Cannot relativize " + other + " to " + this);
        }

        return new Path(separator, false, normalizedOther.subList(normalized.size(), normalizedOther.size()));
    }

    /**
     * Create an {@link Iterator} that returns all possible sub Paths of this Path, in order of increasing length.
     *
     * For example, for the Path "/a/b/c/d" the iterator returns "/a", "/a/b", "/a/b/c", "/a/b/c/d".
     *
     * @return the iterator.
     */
    public Iterator<Path> iterator() {
        return new PathIterator();
    }

    /**
     * Return a <code>String</code> representation of this Path interpreted as a relative path.
     *
     * A relative path does not start with a separator.
     *
     * @return a String representation of this Path interpreted as a relative path.
     */
    private String getRelativePath() {
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
     * Return a <code>String</code> representation of this Path interpreted as an absolute path.
     *
     * An absolute path starts with a separator.
     *
     * @return a String representation of this path interpreted as an absolute path.
     */
    private String getAbsolutePath() {
        return separator + getRelativePath();
    }

    /**
     * Normalize this Path by removing as many redundant path elements as possible.
     *
     * Redundant path elements are <code>"."</code> (indicating the current directory) and <code>".."</code> (indicating the parent directory).
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

        return new Path(separator, isAbsolute, stack);
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
        return isAbsolute == other.isAbsolute && separator == other.separator && elements.equals(other.elements);
    }

    @Override
    public String toString() {

        if (isAbsolute) {
            return getAbsolutePath();
        } else {
            return getRelativePath();
        }
    }

    public boolean isAbsolute() {
        return isAbsolute;
    }

    public Path toRelativePath() {
        return new Path(separator, false, elements);
    }

    public Path toAbsolutePath() {
        return new Path(separator, true, elements);
    }
}
