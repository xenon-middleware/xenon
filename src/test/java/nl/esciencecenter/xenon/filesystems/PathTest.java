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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.junit.Test;

import nl.esciencecenter.xenon.filesystems.Path;

public class PathTest {

    @Test
    public void testRelativePath1a() {
        Path path = new Path();
        assertEquals(path.getRelativePath(), "");
    }

    @Test
    public void testRelativePath1b() {
        Path path = new Path();
        assertEquals(path.getAbsolutePath(), "" + path.getSeparator());
    }

    @Test
    public void testRelativePath2a() {
        Path path = new Path(new Path[0]);
        assertEquals(path.getRelativePath(), "");
    }

    @Test
    public void testRelativePath2b() {
        Path path = new Path(new Path[0]);
        assertEquals(path.getAbsolutePath(), "" + path.getSeparator());
    }
    
    @Test
    public void testRelativePath3a() {
        String s = null;
        Path path = new Path(s, "");
        assertEquals(path.getRelativePath(), "");
    }

    @Test
    public void testRelativePath3b() {
        String s = null;
        Path path = new Path(s, "");
        assertEquals(path.getAbsolutePath(), "" + path.getSeparator());
    }
    
    @Test
    public void testRelativePathString() {
        Path path = new Path("mydir/myfile");
        assertEquals(path.getRelativePath(), "mydir/myfile");
    }

    @Test
    public void testRelativePathString2() {
        Path path = new Path("mydir/myfile");
        assertEquals(path.getAbsolutePath(), "/mydir/myfile");
    }
    
    @Test
    public void testRelativePathStringArray() {
        String[] strings = new String[2];
        strings[0] = "mydir";
        strings[1] = "myfile";
        Path path = new Path(strings);
        assertEquals(path.getRelativePath(), "mydir/myfile");
    }

    @Test
    public void testRelativePathStringArray2() {
        String[] strings = new String[2];
        strings[0] = "mydir";
        strings[1] = "myfile";
        Path path = new Path(strings);
        assertEquals(path.getAbsolutePath(), "/mydir/myfile");
    }

    @Test
    public void testRelativePathRelativePathArray1a() {
        Path[] paths = new Path[2];
        paths[0] = new Path("mydir");
        paths[1] = new Path("myfile");
        Path path = new Path(paths);
        assertEquals(path.getRelativePath(), "mydir/myfile");
    }
    
    @Test
    public void testRelativePathRelativePathArray1b() {
        Path[] paths = new Path[2];
        paths[0] = new Path("mydir");
        paths[1] = new Path("myfile");
        Path path = new Path(paths);
        assertEquals(path.getAbsolutePath(), "/mydir/myfile");
    }

    @Test
    public void testRelativePathRelativePathArray2a() {
        Path[] paths = new Path[4];
        paths[0] = new Path("mydir0");
        paths[1] = new Path("mydir1");
        paths[2] = new Path("mydir2");
        paths[3] = new Path("myfile");
        Path path = new Path(paths);
        assertEquals(path.getRelativePath(), "mydir0/mydir1/mydir2/myfile");
    }

    @Test
    public void testRelativePathRelativePathArray2b() {
        Path[] paths = new Path[4];
        paths[0] = new Path("mydir0");
        paths[1] = new Path("mydir1");
        paths[2] = new Path("mydir2");
        paths[3] = new Path("myfile");
        Path path = new Path(paths);
        assertEquals(path.getAbsolutePath(), "/mydir0/mydir1/mydir2/myfile");
    }

    @Test
    public void testRelativePathRelativePathMultiple1a() {
        Path path1 = new Path("mydir");
        Path path2 = new Path("myfile");
        Path path = new Path(path1, path2);
        assertEquals(path.getRelativePath(), "mydir/myfile");
    }

    @Test
    public void testRelativePathRelativePathMultiple1b() {
        Path path1 = new Path("mydir");
        Path path2 = new Path("myfile");
        Path path = new Path(path1, path2);
        assertEquals(path.getAbsolutePath(), "/mydir/myfile");
    }
    
    @Test
    public void testRelativePathRelativePath1a() {
        Path path1 = new Path("mydir/myfile");
        Path path = new Path(path1);
        assertEquals(path.getRelativePath(), "mydir/myfile");
    }

    @Test
    public void testRelativePathRelativePath1b() {
        Path path1 = new Path("mydir/myfile");
        Path path = new Path(path1);
        assertEquals(path.getAbsolutePath(), "/mydir/myfile");
    }

    @Test
    public void testRelativePathPathSeperator1a() {
        Path path = new Path('@', "mydir@myfile");
        assertEquals(path.getRelativePath(), "mydir@myfile");
    }

    
    @Test
    public void testRelativePathPathSeperator1b() {
        Path path = new Path('@', "mydir@myfile");
        assertEquals(path.getAbsolutePath(), "@mydir@myfile");
    }

    @Test
    public void testRelativePathPathSeperator2a() {
        Path path = new Path('/', "mydir", "myfile");
        assertEquals(path.getRelativePath(), "mydir/myfile");
    }
    
    @Test
    public void testRelativePathPathSeperator2() {
        Path path = new Path('/', "mydir", "myfile");
        assertEquals(path.getAbsolutePath(), "/mydir/myfile");
    }

    @Test
    public void testRelativePathPathSeperator3a() {
        Path path = new Path('/', "mydir", null, "myfile");
        assertEquals(path.getRelativePath(), "mydir/myfile");
    }

    @Test
    public void testRelativePathPathSeperator3b() {
        Path path = new Path('/', "mydir", null, "myfile");
        assertEquals(path.getAbsolutePath(), "/mydir/myfile");
    }

    @Test
    public void testRelativePathPathSeperator4a() {
        Path path = new Path('/', "mydir", "", "myfile");
        assertEquals(path.getRelativePath(), "mydir/myfile");
    }
    
    @Test
    public void testRelativePathPathSeperator4b() {
        Path path = new Path('/', "mydir", "", "myfile");
        assertEquals(path.getAbsolutePath(), "/mydir/myfile");
    }

    @Test
    public void testRelativePathPathSeperator5a() {
        Path path = new Path('/', (String[]) null);
        assertEquals(path.getRelativePath(), "");
    }

    @Test
    public void testRelativePathPathSeperator5b() {
        Path path = new Path('/', (String[]) null);
        assertEquals(path.getAbsolutePath(), "/");
    }

    @Test
    public void testRelativePathPathSeperator6a() {
        Path path = new Path("mydir", null);
        assertEquals(path.getRelativePath(), "mydir");
    }

    @Test
    public void testRelativePathPathSeperator6b() {
        Path path = new Path("mydir", null);
        assertEquals(path.getAbsolutePath(), "/mydir");
    }

    @Test
    public void testRelativePathPathSeperator7a() {
        Path path = new Path("mydir", "");
        assertEquals(path.getRelativePath(), "mydir");
    }

    @Test
    public void testRelativePathPathSeperator7b() {
        Path path = new Path("mydir", "");
        assertEquals(path.getAbsolutePath(), "/mydir");
    }

    @Test
    public void testRelativePathPathSeperator8a() {
        Path path = new Path('/');
        assertEquals(path.getRelativePath(), "");
    }

    @Test
    public void testRelativePathPathSeperator8b() {
        Path path = new Path('/');
        assertEquals(path.getAbsolutePath(), "/");
    }
    
    @Test
    public void testRelativePathStringArraySeperator1a() {
        String[] strings = new String[] { "mydir", "myfile" };
        Path path = new Path('@', strings);
        assertEquals(path.getRelativePath(), "mydir@myfile");
    }

    @Test
    public void testRelativePathStringArraySeperator1b() {
        String[] strings = new String[] { "mydir", "myfile" };
        Path path = new Path('@', strings);
        assertEquals(path.getAbsolutePath(), "@mydir@myfile");
    }

    
    @Test
    public void testGetFileName_WithFile_LastElement() {
        Path path = new Path("mydir/myfile");
        String filename = path.getFileNameAsString();
        assertEquals(filename, "myfile");
    }

    @Test
    public void testGetFileName_EmptyPath_Null() {
        Path path = new Path();
        String filename = path.getFileNameAsString();
        assertNull(filename);
    }

    @Test
    public void testGetParent_MultiElement_EverythingExceptFilename() {
        Path path = new Path("mydir/myfile");
        Path parent = path.getParent();
        assertEquals(parent, new Path("mydir"));
    }

    @Test
    public void testGetParent_SingleElement_EmptyPath() {
        Path path = new Path("mydir");
        Path parent = path.getParent();
        assertEquals(parent, new Path());
    }

    @Test
    public void testGetParent_EmptyPath_Null() {
        Path path = new Path();
        Path parent = path.getParent();
        assertNull(parent);
    }

    @Test
    public void testGetNameCount() {
        Path path = new Path("mydir/myfile");
        int nr_elements = path.getNameCount();
        assertEquals(nr_elements, 2);
    }

    @Test
    public void testGetName_IndexWithinElements_ReturnsElement() {
        Path path = new Path("mydir/myfile");
        Path element = path.getName(1);
        assertEquals(element.getRelativePath(), "myfile");
    }

    @Test
    public void testGetName_IndexOutsideElements_IllegalArgmentException() {
        Path path = new Path("mydir/myfile");
        try {
            path.getName(3);
            fail("Able to fetch index out of bounds");
        } catch (IndexOutOfBoundsException e) {
            assertEquals("Index: 3, Size: 2", e.getMessage());
        }
    }

    public void doSubPath(String[] input_path, int beginIndex, int endIndex, String[] epath) {
        Path path = new Path(input_path);
        Path expected_path = new Path(epath);
        Path npath = path.subpath(beginIndex, endIndex);
        assertEquals(expected_path, npath);
    }

    public void doSubPathWithException(int beginIndex, int endIndex, String input_path, String expected_message) {
        Path path = new Path(input_path);
        try {
            path.subpath(beginIndex, endIndex);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals(expected_message, e.getMessage());
        }
    }

    @Test
    public void testSubpath_First() {
        doSubPath(new String[] { "a", "b", "c" }, 0, 1, new String[] { "a" });
    }

    @Test
    public void testSubpath_Middle() {
        doSubPath(new String[] { "a", "b", "c" }, 1, 2, new String[] { "b" });
    }

    @Test
    public void testSubpath_Last() {
        doSubPath(new String[] { "a", "b", "c" }, 2, 3, new String[] { "c" });
    }

    @Test
    public void testSubpath_NotLast() {
        doSubPath(new String[] { "a", "b", "c" }, 0, 2, new String[] { "a", "b" });
    }

    @Test
    public void testSubpath_NotFirst() {
        doSubPath(new String[] { "a", "b", "c" }, 1, 3, new String[] { "b", "c" });
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubpath_EndAfterLast() {
        doSubPath(new String[] { "a", "b", "c" }, 1, 5, null);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubpath_BeginBeforeFirst() {
        doSubPath(new String[] { "a", "b", "c" }, -1, 1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubpath_BeginGreaterThanEnd() {
        doSubPath(new String[] { "a", "b", "c" }, 2, 1, null);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubpath_BeginGreaterThanLength() {
        doSubPath(new String[] { "a", "b", "c" }, 4, 5, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubpath_EndBeforeFirst() {
        doSubPath(new String[] { "a", "b", "c" }, 1, -1, null);
    }

    @Test
    public void testStartsWith_False() {
        Path path = new Path("mydir/myfile");
        Path path2 = new Path("myfile");
        assertFalse(path.startsWith(path2));
    }

    @Test
    public void testStartsWith_True() {
        Path path = new Path("mydir/myfile");
        assertTrue(path.startsWith(path));
    }

    @Test
    public void testEndsWith_False() {
        Path path = new Path("mydir/myfile");
        Path path2 = new Path("mydir");

        assertFalse(path.endsWith(path2));
    }

    @Test
    public void testEndsWith_True() {
        Path path = new Path("mydir/myfile");
        assertTrue(path.startsWith(path));
    }

    @Test
    public void testResolve1() {

        Path path = new Path("mydir");
        Path path2 = new Path("file");
        Path path3 = new Path("mydir/file");
        Path path4 = path.resolve(path2);
        assertEquals(path3, path4);
    }

    @Test
    public void testResolve2() {

        Path path = new Path("mydir");
        Path path2 = new Path("");
        Path path4 = path.resolve(path2);
        assertEquals(path, path4);
    }

    @Test
    public void testResolve3() {

        Path path = new Path("mydir");
        Path path2 = null;
        Path path4 = path.resolve(path2);
        assertEquals(path, path4);
    }

    @Test
    public void testResolve4() {
        Path path = new Path();
        Path path2 = new Path("mydir");
        Path path4 = path.resolve(path2);
        assertEquals(path2, path4);
    }

    @Test
    public void testResolve5() {
        Path path = new Path("mydir");
        String s = null;
        Path path2 = path.resolve(s);
        assertEquals(path, path2);
    }

    @Test
    public void testResolve6() {
        Path path = new Path("mydir");
        String s = "";
        Path path4 = path.resolve(s);
        assertEquals(path, path4);
    }

    @Test
    public void testResolve7() {
        Path path = new Path("mydir");
        Path path2 = new Path("mydir/test");
        String s = "test";
        Path path4 = path.resolve(s);
        assertEquals(path2, path4);
    }

    @Test
    public void testResolveSibling() {

        Path path = new Path("mydir/aap");
        Path path2 = new Path("noot");
        Path path3 = new Path("mydir/noot");
        Path path4 = path.resolveSibling(path2);
        assertEquals(path3, path4);
    }

    @Test
    public void testResolveSibling2() {

        Path path = new Path();
        Path path2 = new Path("noot");
        Path path4 = path.resolveSibling(path2);
        assertEquals(path2, path4);
    }

    @Test
    public void testResolveSibling3() {

        Path path = new Path();
        Path path2 = null;
        Path path4 = path.resolveSibling(path2);
        assertEquals(path4, path);
    }

    @Test
    public void testResolveSibling4() {

        Path path = new Path("a/b/c");
        Path path2 = new Path("");
        Path path3 = new Path("a/b");
        Path path4 = path.resolveSibling(path2);
        assertEquals(path3, path4);
    }

    @Test
    public void testResolveSibling5() {

        Path path = new Path("a/b/c");
        Path path2 = null;
        Path path3 = new Path("a/b");
        Path path4 = path.resolveSibling(path2);
        assertEquals(path3, path4);
    }

    @Test
    public void testRelativize() {
        Path path = new Path("/a/b");
        Path path2 = new Path("/a/b/c/d");
        Path path3 = new Path("/c/d");
        Path path4 = path.relativize(path2);
        assertEquals(path3, path4);
    }

    @Test
    public void testRelativize2() {
        Path path = new Path();
        Path path2 = new Path("/a/b");
        Path path4 = path.relativize(path2);
        assertEquals(path2, path4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRelativize3() {
        Path path = new Path("a/b/c/d");
        Path path2 = new Path();
        Path path4 = path.relativize(path2);
        assertEquals(path, path4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRelativize4() {
        Path path = new Path("/a/b/c/d");
        Path path2 = new Path("/a/b");
        path.relativize(path2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRelativize5() {
        Path path = new Path("/p/q");
        Path path2 = new Path("/a/b/c/d");
        path.relativize(path2);
    }

    @Test
    public void testRelativize6() {
        Path path = new Path("/a/b");
        Path path2 = new Path("/a/b");
        Path path3 = path.relativize(path2);
        assertEquals(path3, new Path());
    }

    @Test
    public void testIterator() {
        Path path = new Path("mydir/myfile");
        Iterator<Path> iterator = path.iterator();
        assertEquals(iterator.next(), new Path("mydir"));
        assertEquals(iterator.next(), new Path("mydir/myfile"));
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testGetPath_MultiElement_FilledString() {
        Path path = new Path("mydir/myfile");
        String path_as_string = path.getRelativePath();
        assertEquals(path_as_string, "mydir/myfile");
    }

    @Test
    public void testGetPath_OneElement_FilledString() {
        Path path = new Path("myfile");
        String path_as_string = path.getRelativePath();
        assertEquals(path_as_string, "myfile");
    }

    @Test
    public void testGetPath_NullElement_EmptyString() {
        Path path = new Path();
        String path_as_string = path.getRelativePath();
        assertEquals(path_as_string, "");
    }

    @Test
    public void testNormalize_EmptyIn_EmptyOut() {
        Path path = new Path();
        assertEquals(path.normalize(), path);
    }

    @Test
    public void testNormalize_NoDots_SamePath() {
        Path path = new Path("mydir/myfile");
        assertEquals(path.normalize(), path);
    }

    @Test
    public void testNormalize_DoubleDotsAfterFirstElement_SamePath() {
        Path path = new Path("mydir/../myfile");
        Path epath = new Path("/myfile");
        assertEquals(path.normalize(), epath);
    }

    @Test
    public void testNormalize_DoubleDotsAsFirstElement_SamePath() {
        Path path = new Path("../mydir/myfile");
        Path epath = new Path("../mydir/myfile");
        assertEquals(path.normalize(), epath);
    }

    @Test
    public void testNormalize_DoubleDotsAsFirstTwoElement_SamePath() {
        Path path = new Path("../../mydir/myfile");
        Path epath = new Path("../../mydir/myfile");
        assertEquals(path.normalize(), epath);
    }

    @Test
    public void testNormalize_DoubleDotsAsLastElement_SamePath() {
        Path path = new Path("mydir/mydir/..");
        Path epath = new Path("mydir");
        assertEquals(path.normalize(), epath);
    }

    @Test
    public void testNormalize_SingleDotsAsFirstElement_SamePath() {
        Path path = new Path("./mydir/myfile");
        Path epath = new Path("mydir/myfile");
        assertEquals(path.normalize(), epath);
    }

    @Test
    public void testNormalize_SingleAndDoubleDotsAsFirstElement_SamePath() {
        Path path = new Path("./../mydir/myfile");
        Path epath = new Path("../mydir/myfile");
        assertEquals(path.normalize(), epath);
    }

    @Test
    public void testToString() {
        Path path = new Path("mydir/myfile");
        String path_as_string = path.toString();
        assertEquals(path_as_string, "RelativePath [element=[mydir, myfile], seperator=/]");
    }

    @Test
    public void testEquals1() {
        Path path = new Path("a/b/c");
        boolean v = path.equals(null);
        assert (!v);
    }

    @Test
    public void testEquals2() {
        Path path = new Path("a/b/c");
        boolean v = path.equals("Hello world");
        assert (!v);
    }

    @Test
    public void testEquals3() {
        String[] s = new String[] { "a", "b", "c" };

        Path p1 = new Path('/', s);
        Path p2 = new Path('@', s);

        boolean v = p1.equals(p2);
        assert (!v);
    }

    @Test
    public void testEquals4() {
        String[] s = new String[] { "a", "b", "c" };

        Path p1 = new Path('/', s);
        Path p2 = new Path(s);

        boolean v = p1.equals(p2);
        assert (v);
    }

    @Test
    public void testEquals5() {
        Path p1 = new Path("a/b");
        Path p2 = new Path("a");

        boolean v = p1.equals(p2);
        assert (!v);
    }

    @Test
    public void testStartsWith1() {
        Path p1 = new Path("a/b");
        Path p2 = new Path("");

        boolean v = p1.startsWith(p2);
        assert (v);
    }

    @Test
    public void testStartsWith2() {
        Path p1 = new Path("a/b");
        Path p2 = new Path("");

        boolean v = p2.startsWith(p1);
        assert (!v);
    }

    @Test
    public void testStartsWith3() {
        Path p1 = new Path("a/b");
        Path p2 = new Path("a");

        boolean v = p2.startsWith(p1);
        assert (!v);
    }

    @Test
    public void testStartsWith4() {
        Path p1 = new Path("a/b");
        Path p2 = new Path("a");

        boolean v = p1.startsWith(p2);
        assert (v);
    }

    @Test
    public void testEndsWith1() {
        Path p1 = new Path("a/b");
        Path p2 = new Path("");

        boolean v = p1.endsWith(p2);
        assert (v);
    }

    @Test
    public void testEndsWith2() {
        Path p1 = new Path("a/b");
        Path p2 = new Path("");

        boolean v = p2.endsWith(p1);
        assert (!v);
    }

    @Test
    public void testEndsWith3() {
        Path p1 = new Path("a/b");
        Path p2 = new Path("b");

        boolean v = p2.endsWith(p1);
        assert (!v);
    }

    @Test
    public void testEndsWith4() {
        Path p1 = new Path("a/b");
        Path p2 = new Path("b");

        boolean v = p1.endsWith(p2);
        assert (v);
    }

    @Test
    public void testEndsWith5() {
        Path p1 = new Path("a/b/c");
        Path p2 = new Path("c/c");

        boolean v = p1.endsWith(p2);
        assert (!v);
    }
}
