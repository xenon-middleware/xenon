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

import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.RelativePath;

import org.junit.Before;
import org.junit.Test;

public class PathImplementationTest {
    FileSystem fs;

    @Before
    public void setUp() throws Exception {
        // reuse same file system for every test
        fs = new FileSystemImplementation("local", "local-fs-0", "file", "/", new RelativePath("/"), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor0() {
        new PathImplementation(null, new RelativePath("/"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor1() {
        RelativePath p = null;
        new PathImplementation(fs, p);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor2() {
        new PathImplementation(fs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor3() {
        new PathImplementation(null, new RelativePath("/aap"), new RelativePath("/noot"));
    }

    @Test
    public void testConstructor4() {
        Path path = new PathImplementation(fs, new RelativePath("aap"), new RelativePath("noot"));

        RelativePath result = path.getRelativePath();
        
        assertTrue(result.getNameCount() == 2);

        assertEquals("aap", result.getName(0).getRelativePath());
        assertEquals("noot", result.getName(1).getRelativePath());
    }

    @Test
    public void test_getRelativePath() {

        RelativePath rp = new RelativePath("aap");
        Path path = new PathImplementation(fs, rp);

        assertEquals(rp, path.getRelativePath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_subpath1() {
        new PathImplementation(fs, new RelativePath("/aap/noot/mies/bla").subpath(-1, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_subpath2() {
        new PathImplementation(fs, new RelativePath("/aap/noot/mies/bla").subpath(1, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_subpath3() {
        new PathImplementation(fs, new RelativePath("/aap/noot/mies/bla").subpath(2, 1));
    }

    @Test
    public void test_equals() throws Exception {
        Path path = new PathImplementation(fs, new RelativePath("/aap/noot/mies/bla"));

        FileSystem fs2 = new FileSystemImplementation("other", "other-fs-0", "file", "/", new RelativePath("/"), null,
                null);
        FileSystem fs3 = new FileSystemImplementation("local", "local-fs-0", "aap", "/", new RelativePath("/"), null,
                null);

        Path path2 = new PathImplementation(fs2, new RelativePath("/aap/noot/mies/bla"));
        Path path3 = new PathImplementation(fs3, new RelativePath("/aap/noot/mies/bla"));

        Path path4 = new PathImplementation(fs, new RelativePath("/aap/noot/mies/bla"));

        assertFalse(path.equals(null));
        assertFalse(path.equals("AAP"));
        assertFalse(path.equals(path2));
        assertFalse(path.equals(path3));

        assertTrue(path.equals(path4));
    }
}
