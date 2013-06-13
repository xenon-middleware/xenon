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

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

public class RelativePathTest {

    @Test
    public void testRelativePath() {
        RelativePath path = new RelativePath();
        assertEquals(path.getPath(), "");
    }

    @Test
    public void testRelativePathString() {
        RelativePath path = new RelativePath("mydir/myfile");
        assertEquals(path.getPath(), "/mydir/myfile");
    }

    @Test
    public void testRelativePathStringArray() {
        String[] strings = new String[2];
        strings[0] = "mydir";
        strings[1] = "myfile";
        RelativePath path = new RelativePath(strings);
        assertEquals(path.getPath(), "/mydir/myfile");
    }

    @Test
    public void testRelativePathRelativePathArray() {
        RelativePath[] paths = new RelativePath[2];
        paths[0] = new RelativePath("mydir");
        paths[1] = new RelativePath("myfile");
        RelativePath path = new RelativePath(paths);
        assertEquals(path.getPath(), "/mydir/myfile");
    }

    @Test
    public void testRelativePathRelativePathMultiple() {
        RelativePath path1 = new RelativePath("mydir");
        RelativePath path2 = new RelativePath("myfile");
        RelativePath path = new RelativePath(path1, path2);
        assertEquals(path.getPath(), "/mydir/myfile");
    }

    @Test
    public void testRelativePathRelativePath() {
        RelativePath path1 = new RelativePath("mydir/myfile");
        RelativePath path = new RelativePath(path1);
        assertEquals(path.getPath(), "/mydir/myfile");
    }

    @Test
    public void testRelativePathPathSeperator() {
        RelativePath path = new RelativePath("mydir@myfile", "@");
        assertEquals(path.getPath(), "@mydir@myfile");
    }

    @Test
    public void testRelativePathStringArraySeperator() {
        String[] strings = new String[2];
        strings[0] = "mydir";
        strings[1] = "myfile";
        RelativePath path = new RelativePath(strings, "@");
        assertEquals(path.getPath(), "@mydir@myfile");
    }

    @Test
    public void testGetFileName_WithFile_LastElement() {
        RelativePath path = new RelativePath("mydir/myfile");
        String filename = path.getFileName();
        assertEquals(filename, "myfile");
    }

    @Test
    public void testGetFileName_EmptyPath_Null() {
        RelativePath path = new RelativePath();
        String filename = path.getFileName();
        assertNull(filename);
    }

    @Test
    public void testGetParent_MultiElement_EverythingExceptFilename() {
        RelativePath path = new RelativePath("mydir/myfile");
        RelativePath parent = path.getParent();
        assertEquals(parent, new RelativePath("mydir"));
    }

    @Test
    public void testGetParent_SingleElement_EmptyPath() {
        RelativePath path = new RelativePath("mydir");
        RelativePath parent = path.getParent();
        assertEquals(parent, new RelativePath());
    }

    @Test
    public void testGetParent_EmptyPath_Null() {
        RelativePath path = new RelativePath();
        RelativePath parent = path.getParent();
        assertNull(parent);
    }

    @Test
    public void testGetNameCount() {
        RelativePath path = new RelativePath("mydir/myfile");
        int nr_elements = path.getNameCount();
        assertEquals(nr_elements, 2);
    }

    @Test
    public void testGetNames() {
        String[] strings = new String[2];
        strings[0] = "mydir";
        strings[1] = "myfile";
        RelativePath path = new RelativePath(strings);
        String[] result = path.getNames();
        assertArrayEquals(result, strings);
    }

    @Test
    public void testGetName_IndexWithinElements_ReturnsElement() {
        RelativePath path = new RelativePath("mydir/myfile");
        String element = path.getName(1);
        assertEquals(element, "myfile");
    }

    @Test
    public void testGetName_IndexOutsideElements_IllegalArgmentException() {
        RelativePath path = new RelativePath("mydir/myfile");
        try {
            path.getName(3);
            fail("Able to fetch index out of bounds");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "index 3 not present in path RelativePath [element=[mydir, myfile], seperator=/]");
        }
    }

    public void doSubPath(String input_path, int beginIndex, int endIndex, String epath) {
        RelativePath path = new RelativePath(input_path);
        RelativePath expected_path = new RelativePath(epath);
        RelativePath npath = path.subpath(beginIndex, endIndex);
        assertEquals(expected_path, npath);
    }

    public void doSubPathWithException(int beginIndex, int endIndex, String input_path, String expected_message) {
        RelativePath path = new RelativePath(input_path);
        try {
            path.subpath(beginIndex, endIndex);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals(expected_message, e.getMessage());
        }
    }

    @Test
    public void testSubpath_First() {
        doSubPath("a/b/c", 0, 1, "a");
    }

    @Test
    public void testSubpath_Middle() {
        doSubPath("a/b/c", 1, 2, "b");
    }

    @Test
    public void testSubpath_Last() {
        doSubPath("a/b/c", 2, 3, "c");
    }

    @Test
    public void testSubpath_NotLast() {
        doSubPath("a/b/c", 0, 2, "a/b");
    }

    @Test
    public void testSubpath_NotFirst() {
        doSubPath("a/b/c", 1, 3, "b/c");
    }

    @Test
    public void testSubpath_EndAfterLast() {
        doSubPathWithException(1, 5, "a/b/c", "endIndex 5 not present in path RelativePath [element=[a, b, c], seperator=/]");
    }

    @Test
    public void testSubpath_BeginBeforeFirst() {
        doSubPathWithException(-1, 1, "a/b/c", "beginIndex -1 not present in path RelativePath [element=[a, b, c], seperator=/]");
    }

    @Test
    public void testSubpath_BeginGreaterThanEnd() {
        doSubPathWithException(2, 1, "a/b/c", "beginIndex 2 larger than endIndex 1");
    }

    @Test
    public void testStartsWith_False() {
        RelativePath path = new RelativePath("mydir/myfile");
        RelativePath path2 = new RelativePath("myfile");
        assertFalse(path.startsWith(path2));
    }

    @Test
    public void testStartsWith_True() {
        RelativePath path = new RelativePath("mydir/myfile");
        assertTrue(path.startsWith(path));
    }

    @Test
    public void testEndsWith_False() {
        RelativePath path = new RelativePath("mydir/myfile");
        RelativePath path2 = new RelativePath("mydir");

        assertFalse(path.endsWith(path2));
    }

    @Test
    public void testEndsWith_True() {
        RelativePath path = new RelativePath("mydir/myfile");
        assertTrue(path.startsWith(path));
    }

    @Test
    public void testResolve() {

        RelativePath path = new RelativePath("mydir");
        RelativePath path2 = new RelativePath("file");
        RelativePath path3 = new RelativePath("mydir/file");
        RelativePath path4 = path.resolve(path2);
        assertEquals(path3, path4);
    }

    @Test
    public void testResolveSibling() {

        RelativePath path = new RelativePath("mydir/aap");
        RelativePath path2 = new RelativePath("noot");
        RelativePath path3 = new RelativePath("mydir/noot");
        RelativePath path4 = path.resolveSibling(path2);
        assertEquals(path3, path4);
    }

    @Test
    public void testRelativize() {
        RelativePath path = new RelativePath("a/b/c/d");
        RelativePath path2 = new RelativePath("/a/b");
        RelativePath path3 = new RelativePath("/c/d");
        RelativePath path4 = path.relativize(path2);
        assertEquals(path3, path4);
    }

    @Test
    public void testIterator() {
        RelativePath path = new RelativePath("mydir/myfile");
        Iterator<RelativePath> iterator = path.iterator();
        assertEquals(iterator.next(), new RelativePath("mydir"));
        assertEquals(iterator.next(), new RelativePath("mydir/myfile"));
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testGetPath_MultiElement_FilledString() {
        RelativePath path = new RelativePath("mydir/myfile");
        String path_as_string = path.getPath();
        assertEquals(path_as_string, "/mydir/myfile");
    }

    @Test
    public void testGetPath_OneElement_FilledString() {
        RelativePath path = new RelativePath("myfile");
        String path_as_string = path.getPath();
        assertEquals(path_as_string, "/myfile");
    }

    @Test
    public void testGetPath_NullElement_EmptyString() {
        RelativePath path = new RelativePath();
        String path_as_string = path.getPath();
        assertEquals(path_as_string, "");
    }

    @Test
    public void testNormalize_EmptyIn_EmptyOut() {
        RelativePath path = new RelativePath();
        assertEquals(path.normalize(), path);
    }

    @Test
    public void testNormalize_NoDots_SamePath() {
        RelativePath path = new RelativePath("mydir/myfile");
        assertEquals(path.normalize(), path);
    }

    @Test
    public void testNormalize_DoubleDotsAfterFirstElement_SamePath() {
        RelativePath path = new RelativePath("mydir/../myfile");
        RelativePath epath = new RelativePath("/myfile");
        assertEquals(path.normalize(), epath);
    }

    @Test
    public void testNormalize_DoubleDotsAsFirstElement_SamePath() {
        RelativePath path = new RelativePath("../mydir/myfile");
        RelativePath epath = new RelativePath("../mydir/myfile");
        assertEquals(path.normalize(), epath);
    }

    @Test
    public void testToString() {
        RelativePath path = new RelativePath("mydir/myfile");
        String path_as_string = path.toString();
        assertEquals(path_as_string, "RelativePath [element=[mydir, myfile], seperator=/]");
    }

}
