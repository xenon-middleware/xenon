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

/**
 * Path represents a specific location on a FileSystem, as identified by a Pathname. 
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface Path {

    /**
     * Get the FileSystem to which this Path refers.
     * 
     * @return the FileSystem.
     */
    FileSystem getFileSystem();

    /**
     * Get the Pathname on the FileSystem.
     * 
     * @return the Pathname.
     */
    Pathname getPathname();

    /**
     * Is this Path created by the local adaptor ?
     * 
     * @return If this Path is created by the local adaptor ?
     */
//    boolean isLocal();

    /**
     * Get the file name, or <code>null</code> if the Path is empty.
     * 
     * @return the resulting file name or <code>null</code>.
     */
    //String getFileName();

    /**
     * Get the parent path, or <code>null</code> if this path does not have a parent.
     * 
     * @return a path representing the path's parent.
     */
//    Path getParent();

    /**
     * Get the number of name elements in the path.
     * 
     * @return the number of elements in the path, or 0 if it is empty.
     */
 //   int getNameCount();

    /**
     * Get the name elements in the path.
     * 
     * @return the name elements in the path or an empty array if it is empty.
     */
  //  String[] getNames();

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
 //   String getName(int index);

    /**
     * Returns a relative Path that is a subsequence of the name elements of this path.
     * 
     * @param beginIndex
     *            the index of the first element, inclusive
     * @param endIndex
     *            the index of the last element, exclusive
     * 
     * @return a new Path that is a subsequence of the name elements in this path.
     * 
     * @throws IllegalArgumentException
     *             If the beginIndex or endIndex is negative or greater or equal to the number of elements in the path, or if
     *             beginIndex is larger that or equal to the endIndex.
     */
  //  Path subpath(int beginIndex, int endIndex);

    /**
     * Tests if this path starts with the given path.
     * 
     * @param other
     *            the path to test.
     * 
     * @return If this paths start with the other path.
     */
 //   boolean startsWith(Pathname other);

    /**
     * Tests if this path ends with the given path.
     * 
     * @param other
     *            the path to test.
     * 
     * @return If this paths ends with the other path.
     */
  //  boolean endsWith(Pathname other);

    /**
     * Resolve a Pathname against this Path by appending all path elements in the Pathname to the path elements in
     * this Path.
     * 
     * @param other
     *            the Pathname.
     * 
     * @return the resulting Path.
     */
 //   Path resolve(Pathname other);

    /**
     * Normalize this Path by removing as many redundant path elements as possible.
     * 
     * Redundant path elements are <code>"."</code> (indicating the current directory) and <code>".."</code> (indicating the
     * parent directory).
     * 
     * Note that the resulting normalized path does may still contain <code>".."</code> elements which are not redundant.
     * 
     * @return the normalize path.
     */
  //  Path normalize();

    /**
     * Resolves the given path to this paths parent path, thereby creating a sibling to this path.
     * 
     * @param other
     *            the path to resolve as sibling.
     * 
     * @return a Path representing the sibling.
     * 
     * @throws IllegalArgumentException
     *             If the path can not be resolved as a sibling to this path.
     */
 //   Path resolveSibling(Pathname other);

    /**
     * Create a relative path between the given path and this path.
     * 
     * TODO: explain.
     * 
     * @param other
     *            the path to relativize.
     * 
     * @return a Path representing a relative path between the given path and this path.
     * 
     * @throws IllegalArgumentException
     *             If the path can not be relativized to this path.
     */
  //  Pathname relativize(Pathname other);

    /**
     * Create an {@link Iterator} that returns all possible sub paths of this path, in order of increasing length.
     * 
     * For example, for the path "/a/b/c/d" the iterator returns "/a", "/a/b", "a/b/c", "/a/b/c/d".
     * 
     * @return an {@link Iterator} that returns all possible sub paths of this path.
     */
 //   Iterator<Path> iterator();

    /**
     * Get a string representation of this path.
     * 
     * @return a string representation of this path.
     */
    String getPath();
}
