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

import java.util.Iterator;

/**
 * AbsolutePath represents a RelativePath on a specific FileSystem.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface AbsolutePath {

    /**
     * Get the FileSystem to which this AbsolutePath is linked.
     * 
     * @return the FileSystem.
     */
    FileSystem getFileSystem();

    /**
     * Get the RelativePath on the FileSystem.
     * 
     * @return the RelativePath.
     */
    RelativePath getRelativePath();

    /**
     * Is this AbsolutePath created by the local adaptor ?
     * 
     * @return If this AbsolutePath is created by the local adaptor ?
     */
    boolean isLocal();

    /**
     * Get the file name, or <code>null</code> if the AbsolutePath is empty.
     * 
     * @return the resulting file name or <code>null</code>.
     */
    String getFileName();

    /**
     * Get the parent path, or <code>null</code> if this path does not have a parent.
     * 
     * @return a path representing the path's parent.
     */
    AbsolutePath getParent();

    /**
     * Get the number of name elements in the path.
     * 
     * @return the number of elements in the path, or 0 if this is empty.
     */
    int getNameCount();

    /**
     * Get the number of name elements in the path.
     * 
     * @return the number of elements in the path, or 0 if this path is empty.
     */
    String[] getNames();

    /**
     * Get a name element of this path.
     * 
     * @param index
     *            the index of the element
     * 
     * @return the name element
     * 
     * @throws IllegalArgumentException
     *             If the index is negative or greater or equal to the number of elements in the path.
     */
    String getName(int index);

    /**
     * Returns a relative Path that is a subsequence of the name elements of this path.
     * 
     * @param beginIndex
     *            the index of the first element, inclusive
     * @param endIndex
     *            the index of the last element, exclusive
     * 
     * @return a new AbsolutePath that is a subsequence of the name elements in this path.
     * 
     * @throws IllegalArgumentException
     *             If the beginIndex or endIndex is negative or greater or equal to the number of elements in the path, or if
     *             beginIndex is larger that or equal to the endIndex.
     */
    AbsolutePath subpath(int beginIndex, int endIndex);

    /**
     * Tests if this path starts with the given path.
     * 
     * @param other
     *            the path to test.
     * 
     * @return If this paths start with the other path.
     */
    boolean startsWith(RelativePath other);

    /**
     * Tests if this path ends with the given path.
     * 
     * @param other
     *            the path to test.
     * 
     * @return If this paths ends with the other path.
     */
    boolean endsWith(RelativePath other);

    /**
     * Resolve a RelativePath against this AbsolutePath by appending all path elements in the RelativePath to the path elements in
     * this AbsolutePath.
     * 
     * @param other
     *            the RelativePath.
     * 
     * @return the resulting AbsolutePath.
     */
    AbsolutePath resolve(RelativePath other);

    /**
     * Normalize this AbsolutePath by removing as many redundant path elements as possible.
     * 
     * Redundant path elements are <code>"."</code> (indicating the current directory) and <code>".."</code> (indicating the
     * parent directory).
     * 
     * Note that the resulting normalized path does may still contain <code>".."</code> elements which are not redundant.
     * 
     * @return the normalize path.
     */
    AbsolutePath normalize();

    /**
     * Resolves the given path to this paths parent path, thereby creating a sibling to this path.
     * 
     * @param other
     *            the path to resolve as sibling.
     * 
     * @return a AbsolutePath representing the sibling.
     * 
     * @throws IllegalArgumentException
     *             If the path can not be resolved as a sibling to this path.
     */
    AbsolutePath resolveSibling(RelativePath other);

    /**
     * Create a relative path between the given path and this path.
     * 
     * TODO: explain.
     * 
     * @param other
     *            the path to relativize.
     * 
     * @return a AbsolutePath representing a relative path between the given path and this path.
     * 
     * @throws IllegalArgumentException
     *             If the path can not be relativized to this path.
     */
    RelativePath relativize(RelativePath other);

    /**
     * Create an {@link Iterator} that returns all possible sub paths of this path, in order of increasing length.
     * 
     * For example, for the path "/a/b/c/d" the iterator returns "/a", "/a/b", "a/b/c", "/a/b/c/d".
     * 
     * @return
     */
    Iterator<AbsolutePath> iterator();

    /**
     * Get a string representation of this path.
     * 
     * @return a string representation of this path.
     */
    String getPath();
}
