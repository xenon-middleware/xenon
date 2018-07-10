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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class PathTest {

    @Test
    public void test_separatorOnly() {
        Path path = new Path("/");
        assertTrue(path.isAbsolute());
    }

    @Test
    public void test_empty() {
        Path path = new Path("");
        assertFalse(path.isAbsolute());
    }

    @Test
    public void testStartsWith_Absolute_True() {
        Path path = new Path("/aap/noot");
        assertTrue(path.startsWith("/aap"));
    }

    @Test
    public void testStartsWith_Absolute_False() {
        Path path = new Path("/aap/noot");
        assertFalse(path.startsWith("/noot"));
    }

    @Test
    public void testEndsWith_Absolute_True() {
        Path path = new Path("/aap/noot");
        assertTrue(path.endsWith("noot"));
    }

    @Test
    public void testEndsWith_Absolute_False() {
        Path path = new Path("/aap/noot");
        assertFalse(path.endsWith("aap"));
    }

    @Test
    public void testToString_empty_empty() {
        Path path = new Path();
        assertEquals(path.toString(), "");
    }

    @Test
    public void testRelativePath1b() {
        Path path = new Path().toAbsolutePath();
        assertEquals(path.toString(), "" + path.getSeparator());
    }

    @Test
    public void testToRelativePath() {
        Path path = new Path("/bla").toRelativePath();
        assertEquals("bla", path.toString());
    }

    // @Test
    // public void testRelativePath2a() {
    // Path path = new Path(new Path[0]);
    // assertEquals(path.toString(), "");
    // }

    // @Test
    // public void testRelativePath2b() {
    // Path path = new Path(new Path[0]).toAbsolutePath();
    // assertEquals(path.toString(), "" + path.getSeparator());
    // }

    @Test(expected = IllegalArgumentException.class)
    public void test_path_withNullFails() {
        String[] tmp = new String[] { "aap", null, "noot" };
        new Path(true, tmp);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_path_withEmptyFails() {
        String[] tmp = new String[] { "aap", "", "noot" };
        new Path(true, tmp);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_path_withSeparatorFails() {
        String[] tmp = new String[] { "aap", "bla", "noot/aap" };
        new Path(true, tmp);
    }

    @Test
    public void test_path_withEmptyRelative() {
        Path path = new Path("");
        assertFalse(path.isAbsolute());
    }

    @Test
    public void test_path_absoluteWithEmpty() {
        Path path = new Path("").toAbsolutePath();
        assertTrue(path.isAbsolute());
    }

    @Test
    public void testRelativePathString() {
        Path path = new Path("mydir/myfile");
        assertEquals(path.toString(), "mydir/myfile");
    }

    @Test
    public void testRelativePathString2() {
        Path path = new Path("mydir/myfile").toAbsolutePath();
        assertEquals(path.toString(), "/mydir/myfile");
    }

    @Test
    public void test_createRelativePathStringArray() {
        String[] strings = new String[2];
        strings[0] = "mydir";
        strings[1] = "myfile";
        Path path = new Path(false, strings);
        assertEquals(path.toString(), "mydir/myfile");
    }

    @Test
    public void test_convertRelativePathStringArray() {
        String[] strings = new String[2];
        strings[0] = "mydir";
        strings[1] = "myfile";
        Path path = new Path(false, strings).toAbsolutePath();
        assertEquals(path.toString(), "/mydir/myfile");
    }

    @Test
    public void testRelativePathStringArray2() {
        String[] strings = new String[2];
        strings[0] = "mydir";
        strings[1] = "myfile";
        Path path = new Path(true, strings);
        assertEquals(path.toString(), "/mydir/myfile");
    }

    @Test
    public void testRelativePathPathSeparator1a() {
        Path path = new Path('@', "mydir@myfile");
        assertEquals(path.toString(), "mydir@myfile");
    }

    @Test
    public void testRelativePathPathSeparator1b() {
        Path path = new Path('@', "mydir@myfile").toAbsolutePath();
        assertEquals(path.toString(), "@mydir@myfile");
    }

    @Test
    public void testRelativePathPathSeperator2a() {
        Path path = new Path('/', false, "mydir", "myfile");
        assertEquals(path.toString(), "mydir/myfile");
    }

    @Test
    public void testRelativePathPathSeperator2() {
        Path path = new Path('/', true, "mydir", "myfile").toAbsolutePath();
        assertEquals(path.toString(), "/mydir/myfile");
    }

    @Test
    public void testRelativePathPathOtherSeperator() {
        Path path = new Path('\\', true, "mydir", "myfile").toAbsolutePath();
        assertEquals(path.toString(), "\\mydir\\myfile");
    }

    @Test
    public void testRelativePathPathSeperator5a() {
        Path path = new Path('/', false, (String[]) null);
        assertEquals(path.toString(), "");
    }

    @Test
    public void testRelativePathPathSeperator5b() {
        Path path = new Path('/', true, (String[]) null);
        assertEquals(path.toString(), "/");
    }

    @Test
    public void testRelativePathPathSeperator8b() {
        Path path = new Path('/', null).toAbsolutePath();
        assertEquals(path.toString(), "/");
    }

    @Test
    public void testRelativePathStringArraySeparator1a() {
        String[] strings = new String[] { "mydir", "myfile" };
        Path path = new Path('@', false, strings);
        assertEquals(path.toString(), "mydir@myfile");
    }

    @Test
    public void testRelativePathStringArraySeparator1b() {
        String[] strings = new String[] { "mydir", "myfile" };
        Path path = new Path('@', true, strings);
        assertEquals(path.toString(), "@mydir@myfile");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPathWithSeparatorInElt() {
        String[] strings = new String[] { "mydir", "/myfile" };
        new Path('/', true, strings);
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
        assertNull(parent);
    }

    @Test
    public void testGetParent_EmptyPath_Null() {
        Path path = new Path();
        Path parent = path.getParent();
        assertNull(parent);
    }

    @Test
    public void testGetParent_slash() {
        Path path = new Path("/");
        Path parent = path.getParent();
        assertNull(parent);
    }

    @Test
    public void testGetParent_empty() {
        Path path = new Path("");
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
        assertEquals(element.toString(), "myfile");
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

    @Test
    public void testGetName_FirstElementOfRelativePath_Element() {
        Path path = new Path("mydir/myfile");

        Path name = path.getName(0);

        Path expected = new Path("mydir");
        assertEquals(name, expected);
    }

    @Test
    public void testGetName_FirstElementOfAbsolutePath_SeparatorElement() {
        Path path = new Path("/mydir/myfile");

        Path name = path.getName(0);

        Path expected = new Path("/mydir");
        assertEquals(name, expected);
    }

    public void doSubPath(String[] input_path, int beginIndex, int endIndex, boolean absolute, String[] epath) {
        Path path = new Path(true, input_path);
        Path expected_path = new Path(absolute, epath);
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

    @Test(expected = IllegalArgumentException.class)
    public void testSubpathFails() {
        doSubPath(new String[] { "a", "b", "c" }, 1, 1, true, new String[] { "a" });
    }

    @Test
    public void testSubpath_First() {
        doSubPath(new String[] { "a", "b", "c" }, 0, 1, true, new String[] { "a" });
    }

    @Test
    public void testSubpath_Middle() {
        doSubPath(new String[] { "a", "b", "c" }, 1, 2, false, new String[] { "b" });
    }

    @Test
    public void testSubpath_Last() {
        doSubPath(new String[] { "a", "b", "c" }, 2, 3, false, new String[] { "c" });
    }

    @Test
    public void testSubpath_NotLast() {
        doSubPath(new String[] { "a", "b", "c" }, 0, 2, true, new String[] { "a", "b" });
    }

    @Test
    public void testSubpath_NotFirst() {
        doSubPath(new String[] { "a", "b", "c" }, 1, 3, false, new String[] { "b", "c" });
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubpath_EndAfterLast() {
        doSubPath(new String[] { "a", "b", "c" }, 1, 5, false, null);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubpath_BeginBeforeFirst() {
        doSubPath(new String[] { "a", "b", "c" }, -1, 1, false, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubpath_BeginGreaterThanEnd() {
        doSubPath(new String[] { "a", "b", "c" }, 2, 1, false, null);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubpath_BeginGreaterThanLength() {
        doSubPath(new String[] { "a", "b", "c" }, 4, 5, false, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubpath_EndBeforeFirst() {
        doSubPath(new String[] { "a", "b", "c" }, 1, -1, false, null);
    }

    @Test
    public void testStartsWith_Relative_False() {
        Path path = new Path("mydir/myfile");
        Path path2 = new Path("myfile");
        assertFalse(path.startsWith(path2));
    }

    @Test
    public void testStartsWith_AbsoluteEndsWithRelative_False() {
        Path path = new Path("/mydir/myfile");
        Path other = new Path("mydir/myfile");

        assertFalse(path.startsWith(other));
    }

    @Test
    public void testStartsWith_RelativeEndsWithAbsolute_false() {
        Path path = new Path("mydir/myfile");
        Path other = new Path("/mydir/myfile");

        assertFalse(path.startsWith(other));
    }

    @Test
    public void testStartsWith_RelativeSelf_True() {
        Path path = new Path("mydir/myfile");
        assertTrue(path.startsWith(path));
    }

    @Test
    public void testStartsWith_AbsoluteSelf_True() {
        Path path = new Path("/mydir/myfile");
        assertTrue(path.startsWith(path));
    }

    @Test
    public void testEndsWith_Relative_False() {
        Path path = new Path("mydir/myfile");
        Path path2 = new Path("mydir");

        assertFalse(path.endsWith(path2));
    }

    @Test
    public void testEndsWith_Relative_True() {
        Path path = new Path("mydir/myfile");
        assertTrue(path.startsWith(path));
    }

    @Test
    public void testEndsWith_RelativeSelf_True() {
        Path path = new Path("mydir/myfile");
        assertTrue(path.endsWith(path));
    }

    @Test
    public void testEndsWith_AbsoluteSelf_True() {
        Path path = new Path("/mydir/myfile");
        assertTrue(path.endsWith(path));
    }

    @Test
    public void testEndsWith_AbsoluteEndsWithRelative_True() {
        Path path = new Path("/mydir/myfile");
        Path other = new Path("mydir/myfile");

        assertTrue(path.endsWith(other));
    }

    @Test
    public void testEndsWith_RelativeEndsWithAbsolute_False() {
        Path path = new Path("mydir/myfile");
        Path other = new Path("/mydir/myfile");

        assertFalse(path.endsWith(other));
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
        Path path3 = new Path("c/d");
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
        String path_as_string = path.toString();
        assertEquals(path_as_string, "mydir/myfile");
    }

    @Test
    public void testGetPath_OneElement_FilledString() {
        Path path = new Path("myfile");
        String path_as_string = path.toString();
        assertEquals(path_as_string, "myfile");
    }

    @Test
    public void testGetPath_NullElement_EmptyString() {
        Path path = new Path();
        String path_as_string = path.toString();
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
        Path epath = new Path("myfile");
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
    public void testNormalize_AbsolutePath_SamePath() {
        Path path = new Path("/mydir/myfile");

        Path npath = path.normalize();

        Path expected = new Path("/mydir/myfile");
        assertEquals(expected, npath);
    }

    @Test
    public void testNormalize_SingleAndDoubleDotsAsFirstElement_SamePath() {
        Path path = new Path("./../mydir/myfile");
        Path epath = new Path("../mydir/myfile");
        assertEquals(path.normalize(), epath);
    }

    @Test
    public void testToString() {
        Path path = new Path("/mydir/myfile");
        String path_as_string = path.toString();
        assertEquals(path_as_string, "/mydir/myfile");
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

        Path p1 = new Path('/', true, s);
        Path p2 = new Path('@', true, s);

        assertFalse(p1.equals(p2));
    }

    @Test
    public void testEquals4() {
        Path p1 = new Path('/', "/a/b/c");
        Path p2 = new Path("/a/b/c");

        assertTrue(p1.equals(p2));
    }

    @Test
    public void testEquals5() {
        Path p1 = new Path("a/b");
        Path p2 = new Path("a");

        assertFalse(p1.equals(p2));
    }

    @Test
    public void testStartsWith_RelativeStartsWithEmpty_True() {
        Path p1 = new Path("a/b");
        Path p2 = new Path("");

        assertTrue(p1.startsWith(p2));
    }

    @Test
    public void testStartsWith_EmptyStartsWithRelative_False() {
        Path p1 = new Path("a/b");
        Path p2 = new Path("");

        assertFalse(p2.startsWith(p1));
    }

    @Test
    public void testStartsWith_LongerRelative_False() {
        Path p1 = new Path("a/b");
        Path p2 = new Path("a");

        assertFalse(p2.startsWith(p1));
    }

    @Test
    public void testStartsWith_ShorterRelative_True() {
        Path p1 = new Path("a/b");
        Path p2 = new Path("a");

        assertTrue(p1.startsWith(p2));
    }

    @Test
    public void testEndsWith_RelativeEndsWithEmpty_True() {
        Path p1 = new Path("a/b");
        Path p2 = new Path("");

        boolean v = p1.endsWith(p2);
        assert (v);
    }

    @Test
    public void testEndsWith_EmptyEndsWithRelative_False() {
        Path p1 = new Path("a/b");
        Path p2 = new Path("");

        boolean v = p2.endsWith(p1);
        assert (!v);
    }

    @Test
    public void testEndsWith_LongerRelative_False() {
        Path p1 = new Path("a/b");
        Path p2 = new Path("b");

        boolean v = p2.endsWith(p1);
        assert (!v);
    }

    @Test
    public void testEndsWith_ShorterRelative_True() {
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

    @Test
    public void testGetFileName() {
        Path p1 = new Path("/a/b/c");
        Path p2 = p1.getFileName();

        assertEquals(p2, new Path("c"));
    }

    @Test
    public void testGetFileNameNull() {
        Path p1 = new Path("");
        Path p2 = p1.getFileName();

        assertNull(p2);
    }

    @Test
    public void test_create_eltNull() {

        List<String> elt = null;
        Path p = new Path('/', true, elt);
        assertTrue(p.isEmpty());
    }

    @Test
    public void test_hashcode() {
        Path p1 = new Path("/aap/noot");

        List<String> elements = new ArrayList<>();
        elements.add("aap");
        elements.add("noot");

        char separator = '/';

        final int prime = 31;
        int result = 1;
        result = prime * result + elements.hashCode();
        result = prime * result + separator;

        assertEquals(result, p1.hashCode());
    }

    @Test
    public void testStartsRoundTripAbsolute() {
        String s = "/aap/noot/mies";
        assertEquals(s, new Path(s).toString());
    }

    @Test
    public void testStartsRoundTripRelative() {
        String s = "aap/noot/mies";
        assertEquals(s, new Path(s).toString());
    }

    @Test
    public void testStartsRoundTripRelativeParent() {
        String s = "aap/noot/mies";
        Path p = new Path(s);
        assertEquals(p.getParent(), new Path("aap/noot"));
    }

    @Test
    public void testStartsRoundTripAbsoluteParent() {
        String s = "/aap/noot/mies";
        Path p = new Path(s);
        assertEquals(p.getParent(), new Path("/aap/noot"));
    }

    @Test
    public void testStartsIsAbsoluteSubString() {
        String s = "/aap/noot/mies";
        Path p = new Path(s);
        assertEquals(p.subpath(0, 2), new Path("/aap/noot"));
    }

    @Test
    public void testStartsNoMoreAbsoluteSubString() {
        String s = "/aap/noot/mies";
        Path p = new Path(s);
        assertEquals(p.subpath(1, 3), new Path("noot/mies"));
    }

    @Test
    public void testStartResolveAbsolute() {
        String s = "/aap/noot";
        Path p = new Path(s);
        assertEquals(p.resolve(new Path("mies")), new Path("/aap/noot/mies"));
    }

    @Test
    public void testStartResolveRelative() {
        String s = "aap/noot";
        Path p = new Path(s);
        assertEquals(p.resolve(new Path("mies")), new Path("aap/noot/mies"));
    }

    @Test
    public void testStartRelativize() {
        Path p = new Path("/aap/noot");
        Path q = new Path("/aap/noot/mies/bla");
        Path z = p.relativize(q);
        assertEquals(q, p.resolve(z));
    }

    @Test
    public void testHome() {
        String s = "~xenon/bla/bla";
        Path q = new Path(s);
        assertEquals(s, q.toString());
    }

}
