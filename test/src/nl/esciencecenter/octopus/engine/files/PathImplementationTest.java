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

import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Pathname;

import org.junit.Before;
import org.junit.Test;

public class PathImplementationTest {
    FileSystem fs;

    @Before
    public void setUp() throws URISyntaxException {
        // reuse same file system for every test
        fs = new FileSystemImplementation("local", "local-fs-0", new URI("file:///"), new Pathname("/"), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor0() {
        new PathImplementation(null, new Pathname("/"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor1() {
        Pathname p = null;
        new PathImplementation(fs, p);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor2() {
        new PathImplementation(fs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor3() {
        new PathImplementation(null, new Pathname("/aap"), new Pathname("/noot"));
    }

    @Test
    public void testConstructor4() {
        Path path = new PathImplementation(fs, new Pathname("aap"), new Pathname("noot"));

        Pathname result = path.getPathname();
        
        assertTrue(result.getNameCount() == 2);

        assertEquals("aap", result.getName(0).getPath());
        assertEquals("noot", result.getName(1).getPath());
    }

    @Test
    public void test_getPathname() {

        Pathname rp = new Pathname("aap");
        Path path = new PathImplementation(fs, rp);

        assertEquals(rp, path.getPathname());
    }

//    @Test
//    public void testGetParent_Root_Null() {
//        PathImplementation path = new PathImplementation(fs, new Pathname("/"));
//
//        Path parentPath = path.getParent();
//
//        assertNull(parentPath);
//    }

//    @Test
//    public void test_getNames0() {
//        PathImplementation path = new PathImplementation(fs, new Pathname("/aap/noot/mies"));
//
//        assertTrue(path.getNameCount() == 3);
//
//        String[] names = path.getNames();
//
//        assertEquals("aap", names[0]);
//        assertEquals("noot", names[1]);
//        assertEquals("mies", names[2]);
//
//        String tmp = path.getName(0);
//        assertEquals("aap", tmp);
//
//        tmp = path.getName(1);
//        assertEquals("noot", tmp);
//
//        tmp = path.getName(2);
//        assertEquals("mies", tmp);
//    }

//    @Test(expected = IllegalArgumentException.class)
//    public void test_getNames1() {
//        PathImplementation path = new PathImplementation(fs, new Pathname("/aap/noot/mies"));
//        path.getName(-1);
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void test_getNames2() {
//        PathImplementation path = new PathImplementation(fs, new Pathname("/aap/noot/mies"));
//        path.getName(10);
//    }
//
//    @Test
//    public void test_getNames3() {
//        PathImplementation path = new PathImplementation(fs, new Pathname("/"));
//
//        assertTrue(path.getNameCount() == 0);
//
//        String[] names = path.getNames();
//
//        assertNotNull(names);
//        assertTrue(names.length == 0);
//    }
//
//    @Test
//    public void test_subpath0() {
//        Path path = new PathImplementation(fs, new Pathname("/aap/noot/mies/bla"));
//        Path expected = new PathImplementation(fs, new Pathname("/noot/mies"));
//        Path sub = path.subpath(1, 3);
//
//        assertEquals(expected, sub);
//    }

    @Test(expected = IllegalArgumentException.class)
    public void test_subpath1() {
        new PathImplementation(fs, new Pathname("/aap/noot/mies/bla").subpath(-1, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_subpath2() {
        new PathImplementation(fs, new Pathname("/aap/noot/mies/bla").subpath(1, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_subpath3() {
        new PathImplementation(fs, new Pathname("/aap/noot/mies/bla").subpath(2, 1));
    }

//    @Test
//    public void test_isLocal() {
//        Path path = new PathImplementation(fs, new Pathname("/aap/noot/mies/bla"));
//        assertTrue(path.isLocal());
//    }
//
//    @Test
//    public void test_startWith() {
//        Path path = new PathImplementation(fs, new Pathname("/aap/noot/mies/bla"));
//        assertTrue(path.startsWith(new Pathname("/aap/noot/mies")));
//        assertFalse(path.startsWith(new Pathname("/noot/aap/mies")));
//    }
//
//    @Test
//    public void test_endsWith() {
//        Path path = new PathImplementation(fs, new Pathname("/aap/noot/mies/bla"));
//        assertTrue(path.endsWith(new Pathname("/mies/bla")));
//        assertFalse(path.endsWith(new Pathname("/noot/aap")));
//    }

    @Test
    public void test_equals() throws Exception {
        Path path = new PathImplementation(fs, new Pathname("/aap/noot/mies/bla"));

        FileSystem fs2 = new FileSystemImplementation("other", "other-fs-0", new URI("file:///"), new Pathname("/"), null,
                null);
        FileSystem fs3 = new FileSystemImplementation("local", "local-fs-0", new URI("aap:///"), new Pathname("/"), null,
                null);

        Path path2 = new PathImplementation(fs2, new Pathname("/aap/noot/mies/bla"));
        Path path3 = new PathImplementation(fs3, new Pathname("/aap/noot/mies/bla"));

        Path path4 = new PathImplementation(fs, new Pathname("/aap/noot/mies/bla"));

        assertFalse(path.equals(null));
        assertFalse(path.equals("AAP"));
        assertFalse(path.equals(path2));
        assertFalse(path.equals(path3));

        assertTrue(path.equals(path4));
    }

//    @Test
//    public void test_resolveSibling() throws Exception {
//        Path path = new PathImplementation(fs, new Pathname("/aap/noot"));
//        Path path2 = new PathImplementation(fs, new Pathname("/aap/mies"));
//        Path path3 = path.resolveSibling(new Pathname("mies"));
//        assertEquals(path2, path3);
//    }
//
//    @Test
//    public void test_relativize() throws Exception {
//        Path path = new PathImplementation(fs, new Pathname("/aap/noot"));
//        Pathname path2 = path.relativize(new Pathname("/aap/noot/mies/bla"));
//        assertEquals(new Pathname("/mies/bla"), path2);
//    }
//
//    @Test(expected = UnsupportedOperationException.class)
//    public void test_iterator() throws Exception {
//        Path path = new PathImplementation(fs, new Pathname("/aap/noot/mies"));
//        path.iterator().remove();
//    }
}
