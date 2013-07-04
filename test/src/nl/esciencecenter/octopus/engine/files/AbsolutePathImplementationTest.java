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

package nl.esciencecenter.octopus.engine.files;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.RelativePath;

import org.junit.Before;
import org.junit.Test;

public class AbsolutePathImplementationTest {
    FileSystem fs;

    @Before
    public void setUp() throws URISyntaxException {
        // reuse same file system for every test
        fs = new FileSystemImplementation("local", "local-fs-0", new URI("file:///"), new RelativePath("/"), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor0() {
        new AbsolutePathImplementation(null, new RelativePath("/"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor1() {
        RelativePath p = null;
        new AbsolutePathImplementation(fs, p);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor2() {
        new AbsolutePathImplementation(fs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor3() {
        new AbsolutePathImplementation(null, new RelativePath("/aap"), new RelativePath("/noot"));
    }

    @Test
    public void testConstructor4() {
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("aap"), new RelativePath("noot"));

        assertTrue(path.getNameCount() == 2);

        String[] names = path.getNames();

        assertEquals("aap", names[0]);
        assertEquals("noot", names[1]);
    }

    @Test
    public void test_getRelativePath() {

        RelativePath rp = new RelativePath("aap");
        AbsolutePath path = new AbsolutePathImplementation(fs, rp);

        assertEquals(rp, path.getRelativePath());
    }

    @Test
    public void testGetParent_Root_Null() {
        AbsolutePathImplementation path = new AbsolutePathImplementation(fs, new RelativePath("/"));

        AbsolutePath parentPath = path.getParent();

        assertNull(parentPath);
    }

    @Test
    public void test_getNames0() {
        AbsolutePathImplementation path = new AbsolutePathImplementation(fs, new RelativePath("/aap/noot/mies"));

        assertTrue(path.getNameCount() == 3);

        String[] names = path.getNames();

        assertEquals("aap", names[0]);
        assertEquals("noot", names[1]);
        assertEquals("mies", names[2]);

        String tmp = path.getName(0);
        assertEquals("aap", tmp);

        tmp = path.getName(1);
        assertEquals("noot", tmp);

        tmp = path.getName(2);
        assertEquals("mies", tmp);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_getNames1() {
        AbsolutePathImplementation path = new AbsolutePathImplementation(fs, new RelativePath("/aap/noot/mies"));
        path.getName(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_getNames2() {
        AbsolutePathImplementation path = new AbsolutePathImplementation(fs, new RelativePath("/aap/noot/mies"));
        path.getName(10);
    }

    @Test
    public void test_getNames3() {
        AbsolutePathImplementation path = new AbsolutePathImplementation(fs, new RelativePath("/"));

        assertTrue(path.getNameCount() == 0);

        String[] names = path.getNames();

        assertNotNull(names);
        assertTrue(names.length == 0);
    }

    @Test
    public void test_subpath0() {
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/aap/noot/mies/bla"));
        AbsolutePath expected = new AbsolutePathImplementation(fs, new RelativePath("/noot/mies"));
        AbsolutePath sub = path.subpath(1, 3);

        assertEquals(expected, sub);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_subpath1() {
        new AbsolutePathImplementation(fs, new RelativePath("/aap/noot/mies/bla").subpath(-1, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_subpath2() {
        new AbsolutePathImplementation(fs, new RelativePath("/aap/noot/mies/bla").subpath(1, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_subpath3() {
        new AbsolutePathImplementation(fs, new RelativePath("/aap/noot/mies/bla").subpath(2, 1));
    }

    @Test
    public void test_isLocal() {
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/aap/noot/mies/bla"));
        assertTrue(path.isLocal());
    }

    @Test
    public void test_startWith() {
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/aap/noot/mies/bla"));
        assertTrue(path.startsWith(new RelativePath("/aap/noot/mies")));
        assertFalse(path.startsWith(new RelativePath("/noot/aap/mies")));
    }

    @Test
    public void test_endsWith() {
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/aap/noot/mies/bla"));
        assertTrue(path.endsWith(new RelativePath("/mies/bla")));
        assertFalse(path.endsWith(new RelativePath("/noot/aap")));
    }

    @Test
    public void test_equals() throws Exception {
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/aap/noot/mies/bla"));

        FileSystem fs2 =
                new FileSystemImplementation("other", "other-fs-0", new URI("file:///"), new RelativePath("/"), null, null);
        FileSystem fs3 =
                new FileSystemImplementation("local", "local-fs-0", new URI("aap:///"), new RelativePath("/"), null, null);

        AbsolutePath path2 = new AbsolutePathImplementation(fs2, new RelativePath("/aap/noot/mies/bla"));
        AbsolutePath path3 = new AbsolutePathImplementation(fs3, new RelativePath("/aap/noot/mies/bla"));

        AbsolutePath path4 = new AbsolutePathImplementation(fs, new RelativePath("/aap/noot/mies/bla"));

        assertFalse(path.equals(null));
        assertFalse(path.equals("AAP"));
        assertFalse(path.equals(path2));
        assertFalse(path.equals(path3));

        assertTrue(path.equals(path4));
    }

    @Test
    public void test_resolveSibling() throws Exception {
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/aap/noot"));
        AbsolutePath path2 = new AbsolutePathImplementation(fs, new RelativePath("/aap/mies"));
        AbsolutePath path3 = path.resolveSibling(new RelativePath("mies"));
        assertEquals(path2, path3);
    }

    @Test
    public void test_relativize() throws Exception {
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/aap/noot"));
        RelativePath path2 = path.relativize(new RelativePath("/aap/noot/mies/bla"));
        assertEquals(new RelativePath("/mies/bla"), path2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_iterator() throws Exception {
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/aap/noot/mies"));
        path.iterator().remove();
    }
}
