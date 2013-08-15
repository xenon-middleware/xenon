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

public class PathnameTest {

    @Test
    public void testPathname1a() {
        Pathname path = new Pathname();
        assertEquals(path.getRelativePath(), "");
    }

    @Test
    public void testPathname1b() {
        Pathname path = new Pathname();
        assertEquals(path.getAbsolutePath(), "" + path.getSeparator());
    }

    @Test
    public void testPathname2a() {
        Pathname path = new Pathname(new Pathname[0]);
        assertEquals(path.getRelativePath(), "");
    }

    @Test
    public void testPathname2b() {
        Pathname path = new Pathname(new Pathname[0]);
        assertEquals(path.getAbsolutePath(), "" + path.getSeparator());
    }
    
    @Test
    public void testPathname3a() {
        String s = null;
        Pathname path = new Pathname(s, "");
        assertEquals(path.getRelativePath(), "");
    }

    @Test
    public void testPathname3b() {
        String s = null;
        Pathname path = new Pathname(s, "");
        assertEquals(path.getAbsolutePath(), "" + path.getSeparator());
    }
    
    @Test
    public void testPathnameString() {
        Pathname path = new Pathname("mydir/myfile");
        assertEquals(path.getRelativePath(), "mydir/myfile");
    }

    @Test
    public void testPathnameString2() {
        Pathname path = new Pathname("mydir/myfile");
        assertEquals(path.getAbsolutePath(), "/mydir/myfile");
    }
    
    @Test
    public void testPathnameStringArray() {
        String[] strings = new String[2];
        strings[0] = "mydir";
        strings[1] = "myfile";
        Pathname path = new Pathname(strings);
        assertEquals(path.getRelativePath(), "mydir/myfile");
    }

    @Test
    public void testPathnameStringArray2() {
        String[] strings = new String[2];
        strings[0] = "mydir";
        strings[1] = "myfile";
        Pathname path = new Pathname(strings);
        assertEquals(path.getAbsolutePath(), "/mydir/myfile");
    }

    @Test
    public void testPathnamePathnameArray1a() {
        Pathname[] paths = new Pathname[2];
        paths[0] = new Pathname("mydir");
        paths[1] = new Pathname("myfile");
        Pathname path = new Pathname(paths);
        assertEquals(path.getRelativePath(), "mydir/myfile");
    }
    
    @Test
    public void testPathnamePathnameArray1b() {
        Pathname[] paths = new Pathname[2];
        paths[0] = new Pathname("mydir");
        paths[1] = new Pathname("myfile");
        Pathname path = new Pathname(paths);
        assertEquals(path.getAbsolutePath(), "/mydir/myfile");
    }

    @Test
    public void testPathnamePathnameArray2a() {
        Pathname[] paths = new Pathname[4];
        paths[0] = new Pathname("mydir0");
        paths[1] = new Pathname("mydir1");
        paths[2] = new Pathname("mydir2");
        paths[3] = new Pathname("myfile");
        Pathname path = new Pathname(paths);
        assertEquals(path.getRelativePath(), "mydir0/mydir1/mydir2/myfile");
    }

    @Test
    public void testPathnamePathnameArray2b() {
        Pathname[] paths = new Pathname[4];
        paths[0] = new Pathname("mydir0");
        paths[1] = new Pathname("mydir1");
        paths[2] = new Pathname("mydir2");
        paths[3] = new Pathname("myfile");
        Pathname path = new Pathname(paths);
        assertEquals(path.getAbsolutePath(), "/mydir0/mydir1/mydir2/myfile");
    }

    @Test
    public void testPathnamePathnameMultiple1a() {
        Pathname path1 = new Pathname("mydir");
        Pathname path2 = new Pathname("myfile");
        Pathname path = new Pathname(path1, path2);
        assertEquals(path.getRelativePath(), "mydir/myfile");
    }

    @Test
    public void testPathnamePathnameMultiple1b() {
        Pathname path1 = new Pathname("mydir");
        Pathname path2 = new Pathname("myfile");
        Pathname path = new Pathname(path1, path2);
        assertEquals(path.getAbsolutePath(), "/mydir/myfile");
    }
    
    @Test
    public void testPathnamePathname1a() {
        Pathname path1 = new Pathname("mydir/myfile");
        Pathname path = new Pathname(path1);
        assertEquals(path.getRelativePath(), "mydir/myfile");
    }

    @Test
    public void testPathnamePathname1b() {
        Pathname path1 = new Pathname("mydir/myfile");
        Pathname path = new Pathname(path1);
        assertEquals(path.getAbsolutePath(), "/mydir/myfile");
    }

    @Test
    public void testPathnamePathSeperator1a() {
        Pathname path = new Pathname('@', "mydir@myfile");
        assertEquals(path.getRelativePath(), "mydir@myfile");
    }

    
    @Test
    public void testPathnamePathSeperator1b() {
        Pathname path = new Pathname('@', "mydir@myfile");
        assertEquals(path.getAbsolutePath(), "@mydir@myfile");
    }

    @Test
    public void testPathnamePathSeperator2a() {
        Pathname path = new Pathname('/', "mydir", "myfile");
        assertEquals(path.getRelativePath(), "mydir/myfile");
    }
    
    @Test
    public void testPathnamePathSeperator2() {
        Pathname path = new Pathname('/', "mydir", "myfile");
        assertEquals(path.getAbsolutePath(), "/mydir/myfile");
    }

    @Test
    public void testPathnamePathSeperator3a() {
        Pathname path = new Pathname('/', "mydir", null, "myfile");
        assertEquals(path.getRelativePath(), "mydir/myfile");
    }

    @Test
    public void testPathnamePathSeperator3b() {
        Pathname path = new Pathname('/', "mydir", null, "myfile");
        assertEquals(path.getAbsolutePath(), "/mydir/myfile");
    }

    @Test
    public void testPathnamePathSeperator4a() {
        Pathname path = new Pathname('/', "mydir", "", "myfile");
        assertEquals(path.getRelativePath(), "mydir/myfile");
    }
    
    @Test
    public void testPathnamePathSeperator4b() {
        Pathname path = new Pathname('/', "mydir", "", "myfile");
        assertEquals(path.getAbsolutePath(), "/mydir/myfile");
    }

    @Test
    public void testPathnamePathSeperator5a() {
        Pathname path = new Pathname('/', (String[]) null);
        assertEquals(path.getRelativePath(), "");
    }

    @Test
    public void testPathnamePathSeperator5b() {
        Pathname path = new Pathname('/', (String[]) null);
        assertEquals(path.getAbsolutePath(), "/");
    }

    @Test
    public void testPathnamePathSeperator6a() {
        Pathname path = new Pathname("mydir", null);
        assertEquals(path.getRelativePath(), "mydir");
    }

    @Test
    public void testPathnamePathSeperator6b() {
        Pathname path = new Pathname("mydir", null);
        assertEquals(path.getAbsolutePath(), "/mydir");
    }

    @Test
    public void testPathnamePathSeperator7a() {
        Pathname path = new Pathname("mydir", "");
        assertEquals(path.getRelativePath(), "mydir");
    }

    @Test
    public void testPathnamePathSeperator7b() {
        Pathname path = new Pathname("mydir", "");
        assertEquals(path.getAbsolutePath(), "/mydir");
    }

    @Test
    public void testPathnamePathSeperator8a() {
        Pathname path = new Pathname('/', new String[0]);
        assertEquals(path.getRelativePath(), "");
    }

    @Test
    public void testPathnamePathSeperator8b() {
        Pathname path = new Pathname('/', new String[0]);
        assertEquals(path.getAbsolutePath(), "/");
    }
    
    @Test
    public void testPathnameStringArraySeperator1a() {
        String[] strings = new String[] { "mydir", "myfile" };
        Pathname path = new Pathname('@', strings);
        assertEquals(path.getRelativePath(), "mydir@myfile");
    }

    @Test
    public void testPathnameStringArraySeperator1b() {
        String[] strings = new String[] { "mydir", "myfile" };
        Pathname path = new Pathname('@', strings);
        assertEquals(path.getAbsolutePath(), "@mydir@myfile");
    }

    
    @Test
    public void testGetFileName_WithFile_LastElement() {
        Pathname path = new Pathname("mydir/myfile");
        String filename = path.getFileNameAsString();
        assertEquals(filename, "myfile");
    }

    @Test
    public void testGetFileName_EmptyPath_Null() {
        Pathname path = new Pathname();
        String filename = path.getFileNameAsString();
        assertNull(filename);
    }

    @Test
    public void testGetParent_MultiElement_EverythingExceptFilename() {
        Pathname path = new Pathname("mydir/myfile");
        Pathname parent = path.getParent();
        assertEquals(parent, new Pathname("mydir"));
    }

    @Test
    public void testGetParent_SingleElement_EmptyPath() {
        Pathname path = new Pathname("mydir");
        Pathname parent = path.getParent();
        assertEquals(parent, new Pathname());
    }

    @Test
    public void testGetParent_EmptyPath_Null() {
        Pathname path = new Pathname();
        Pathname parent = path.getParent();
        assertNull(parent);
    }

    @Test
    public void testGetNameCount() {
        Pathname path = new Pathname("mydir/myfile");
        int nr_elements = path.getNameCount();
        assertEquals(nr_elements, 2);
    }

    @Test
    public void testGetName_IndexWithinElements_ReturnsElement() {
        Pathname path = new Pathname("mydir/myfile");
        Pathname element = path.getName(1);
        assertEquals(element.getRelativePath(), "myfile");
    }

    @Test
    public void testGetName_IndexOutsideElements_IllegalArgmentException() {
        Pathname path = new Pathname("mydir/myfile");
        try {
            path.getName(3);
            fail("Able to fetch index out of bounds");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "index 3 not present in path Pathname [element=[mydir, myfile], seperator=/]");
        }
    }

    public void doSubPath(String[] input_path, int beginIndex, int endIndex, String[] epath) {
        Pathname path = new Pathname(input_path);
        Pathname expected_path = new Pathname(epath);
        Pathname npath = path.subpath(beginIndex, endIndex);
        assertEquals(expected_path, npath);
    }

    public void doSubPathWithException(int beginIndex, int endIndex, String input_path, String expected_message) {
        Pathname path = new Pathname(input_path);
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

    @Test(expected = IllegalArgumentException.class)
    public void testSubpath_EndAfterLast() {
        doSubPath(new String[] { "a", "b", "c" }, 1, 5, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubpath_BeginBeforeFirst() {
        doSubPath(new String[] { "a", "b", "c" }, -1, 1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubpath_BeginGreaterThanEnd() {
        doSubPath(new String[] { "a", "b", "c" }, 2, 1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubpath_BeginGreaterThanLength() {
        doSubPath(new String[] { "a", "b", "c" }, 4, 5, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubpath_EndBeforeFirst() {
        doSubPath(new String[] { "a", "b", "c" }, 1, -1, null);
    }

    @Test
    public void testStartsWith_False() {
        Pathname path = new Pathname("mydir/myfile");
        Pathname path2 = new Pathname("myfile");
        assertFalse(path.startsWith(path2));
    }

    @Test
    public void testStartsWith_True() {
        Pathname path = new Pathname("mydir/myfile");
        assertTrue(path.startsWith(path));
    }

    @Test
    public void testEndsWith_False() {
        Pathname path = new Pathname("mydir/myfile");
        Pathname path2 = new Pathname("mydir");

        assertFalse(path.endsWith(path2));
    }

    @Test
    public void testEndsWith_True() {
        Pathname path = new Pathname("mydir/myfile");
        assertTrue(path.startsWith(path));
    }

    @Test
    public void testResolve1() {

        Pathname path = new Pathname("mydir");
        Pathname path2 = new Pathname("file");
        Pathname path3 = new Pathname("mydir/file");
        Pathname path4 = path.resolve(path2);
        assertEquals(path3, path4);
    }

    @Test
    public void testResolve2() {

        Pathname path = new Pathname("mydir");
        Pathname path2 = new Pathname("");
        Pathname path4 = path.resolve(path2);
        assertEquals(path, path4);
    }

    @Test
    public void testResolve3() {

        Pathname path = new Pathname("mydir");
        Pathname path2 = null;
        Pathname path4 = path.resolve(path2);
        assertEquals(path, path4);
    }

    @Test
    public void testResolve4() {
        Pathname path = new Pathname();
        Pathname path2 = new Pathname("mydir");
        Pathname path4 = path.resolve(path2);
        assertEquals(path2, path4);
    }

    @Test
    public void testResolve5() {
        Pathname path = new Pathname("mydir");
        String s = null;
        Pathname path2 = path.resolve(s);
        assertEquals(path, path2);
    }

    @Test
    public void testResolve6() {
        Pathname path = new Pathname("mydir");
        String s = "";
        Pathname path4 = path.resolve(s);
        assertEquals(path, path4);
    }

    @Test
    public void testResolve7() {
        Pathname path = new Pathname("mydir");
        Pathname path2 = new Pathname("mydir/test");
        String s = "test";
        Pathname path4 = path.resolve(s);
        assertEquals(path2, path4);
    }

    @Test
    public void testResolveSibling() {

        Pathname path = new Pathname("mydir/aap");
        Pathname path2 = new Pathname("noot");
        Pathname path3 = new Pathname("mydir/noot");
        Pathname path4 = path.resolveSibling(path2);
        assertEquals(path3, path4);
    }

    @Test
    public void testResolveSibling2() {

        Pathname path = new Pathname();
        Pathname path2 = new Pathname("noot");
        Pathname path4 = path.resolveSibling(path2);
        assertEquals(path2, path4);
    }

    @Test
    public void testResolveSibling3() {

        Pathname path = new Pathname();
        Pathname path2 = null;
        Pathname path4 = path.resolveSibling(path2);
        assertEquals(path4, path);
    }

    @Test
    public void testResolveSibling4() {

        Pathname path = new Pathname("a/b/c");
        Pathname path2 = new Pathname("");
        Pathname path3 = new Pathname("a/b");
        Pathname path4 = path.resolveSibling(path2);
        assertEquals(path3, path4);
    }

    @Test
    public void testResolveSibling5() {

        Pathname path = new Pathname("a/b/c");
        Pathname path2 = null;
        Pathname path3 = new Pathname("a/b");
        Pathname path4 = path.resolveSibling(path2);
        assertEquals(path3, path4);
    }

    @Test
    public void testRelativize() {
        Pathname path = new Pathname("/a/b");
        Pathname path2 = new Pathname("/a/b/c/d");
        Pathname path3 = new Pathname("/c/d");
        Pathname path4 = path.relativize(path2);
        assertEquals(path3, path4);
    }

    @Test
    public void testRelativize2() {
        Pathname path = new Pathname();
        Pathname path2 = new Pathname("/a/b");
        Pathname path4 = path.relativize(path2);
        assertEquals(path2, path4);
    }

    @Test
    public void testRelativize3() {
        Pathname path = new Pathname("a/b/c/d");
        Pathname path2 = new Pathname();
        Pathname path4 = path.relativize(path2);
        assertEquals(path, path4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRelativize4() {
        Pathname path = new Pathname("/a/b/c/d");
        Pathname path2 = new Pathname("/a/b");
        path.relativize(path2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRelativize5() {
        Pathname path = new Pathname("/p/q");
        Pathname path2 = new Pathname("/a/b/c/d");
        path.relativize(path2);
    }

    @Test
    public void testRelativize6() {
        Pathname path = new Pathname("/a/b");
        Pathname path2 = new Pathname("/a/b");
        Pathname path3 = path.relativize(path2);
        assertEquals(path3, new Pathname());
    }

    @Test
    public void testIterator() {
        Pathname path = new Pathname("mydir/myfile");
        Iterator<Pathname> iterator = path.iterator();
        assertEquals(iterator.next(), new Pathname("mydir"));
        assertEquals(iterator.next(), new Pathname("mydir/myfile"));
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testGetPath_MultiElement_FilledString() {
        Pathname path = new Pathname("mydir/myfile");
        String path_as_string = path.getRelativePath();
        assertEquals(path_as_string, "mydir/myfile");
    }

    @Test
    public void testGetPath_OneElement_FilledString() {
        Pathname path = new Pathname("myfile");
        String path_as_string = path.getRelativePath();
        assertEquals(path_as_string, "myfile");
    }

    @Test
    public void testGetPath_NullElement_EmptyString() {
        Pathname path = new Pathname();
        String path_as_string = path.getRelativePath();
        assertEquals(path_as_string, "");
    }

    @Test
    public void testNormalize_EmptyIn_EmptyOut() {
        Pathname path = new Pathname();
        assertEquals(path.normalize(), path);
    }

    @Test
    public void testNormalize_NoDots_SamePath() {
        Pathname path = new Pathname("mydir/myfile");
        assertEquals(path.normalize(), path);
    }

    @Test
    public void testNormalize_DoubleDotsAfterFirstElement_SamePath() {
        Pathname path = new Pathname("mydir/../myfile");
        Pathname epath = new Pathname("/myfile");
        assertEquals(path.normalize(), epath);
    }

    @Test
    public void testNormalize_DoubleDotsAsFirstElement_SamePath() {
        Pathname path = new Pathname("../mydir/myfile");
        Pathname epath = new Pathname("../mydir/myfile");
        assertEquals(path.normalize(), epath);
    }

    @Test
    public void testNormalize_DoubleDotsAsFirstTwoElement_SamePath() {
        Pathname path = new Pathname("../../mydir/myfile");
        Pathname epath = new Pathname("../../mydir/myfile");
        assertEquals(path.normalize(), epath);
    }

    @Test
    public void testNormalize_DoubleDotsAsLastElement_SamePath() {
        Pathname path = new Pathname("mydir/mydir/..");
        Pathname epath = new Pathname("mydir");
        assertEquals(path.normalize(), epath);
    }

    @Test
    public void testNormalize_SingleDotsAsFirstElement_SamePath() {
        Pathname path = new Pathname("./mydir/myfile");
        Pathname epath = new Pathname("mydir/myfile");
        assertEquals(path.normalize(), epath);
    }

    @Test
    public void testNormalize_SingleAndDoubleDotsAsFirstElement_SamePath() {
        Pathname path = new Pathname("./../mydir/myfile");
        Pathname epath = new Pathname("../mydir/myfile");
        assertEquals(path.normalize(), epath);
    }

    @Test
    public void testToString() {
        Pathname path = new Pathname("mydir/myfile");
        String path_as_string = path.toString();
        assertEquals(path_as_string, "Pathname [element=[mydir, myfile], seperator=/]");
    }

    @Test
    public void testEquals1() {
        Pathname path = new Pathname("a/b/c");
        boolean v = path.equals(null);
        assert (!v);
    }

    @Test
    public void testEquals2() {
        Pathname path = new Pathname("a/b/c");
        boolean v = path.equals("Hello world");
        assert (!v);
    }

    @Test
    public void testEquals3() {
        String[] s = new String[] { "a", "b", "c" };

        Pathname p1 = new Pathname('/', s);
        Pathname p2 = new Pathname('@', s);

        boolean v = p1.equals(p2);
        assert (!v);
    }

    @Test
    public void testEquals4() {
        String[] s = new String[] { "a", "b", "c" };

        Pathname p1 = new Pathname('/', s);
        Pathname p2 = new Pathname(s);

        boolean v = p1.equals(p2);
        assert (v);
    }

    @Test
    public void testEquals5() {
        Pathname p1 = new Pathname("a/b");
        Pathname p2 = new Pathname("a");

        boolean v = p1.equals(p2);
        assert (!v);
    }

    @Test
    public void testStartsWith1() {
        Pathname p1 = new Pathname("a/b");
        Pathname p2 = new Pathname("");

        boolean v = p1.startsWith(p2);
        assert (v);
    }

    @Test
    public void testStartsWith2() {
        Pathname p1 = new Pathname("a/b");
        Pathname p2 = new Pathname("");

        boolean v = p2.startsWith(p1);
        assert (!v);
    }

    @Test
    public void testStartsWith3() {
        Pathname p1 = new Pathname("a/b");
        Pathname p2 = new Pathname("a");

        boolean v = p2.startsWith(p1);
        assert (!v);
    }

    @Test
    public void testStartsWith4() {
        Pathname p1 = new Pathname("a/b");
        Pathname p2 = new Pathname("a");

        boolean v = p1.startsWith(p2);
        assert (v);
    }

    @Test
    public void testEndsWith1() {
        Pathname p1 = new Pathname("a/b");
        Pathname p2 = new Pathname("");

        boolean v = p1.endsWith(p2);
        assert (v);
    }

    @Test
    public void testEndsWith2() {
        Pathname p1 = new Pathname("a/b");
        Pathname p2 = new Pathname("");

        boolean v = p2.endsWith(p1);
        assert (!v);
    }

    @Test
    public void testEndsWith3() {
        Pathname p1 = new Pathname("a/b");
        Pathname p2 = new Pathname("b");

        boolean v = p2.endsWith(p1);
        assert (!v);
    }

    @Test
    public void testEndsWith4() {
        Pathname p1 = new Pathname("a/b");
        Pathname p2 = new Pathname("b");

        boolean v = p1.endsWith(p2);
        assert (v);
    }

    @Test
    public void testEndsWith5() {
        Pathname p1 = new Pathname("a/b/c");
        Pathname p2 = new Pathname("c/c");

        boolean v = p1.endsWith(p2);
        assert (!v);
    }
}
