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

package nl.esciencecenter.octopus.adaptors.local;

import static org.mockito.Mockito.*;

import java.util.NoSuchElementException;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.engine.files.PathImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.util.FileUtils;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class LocalDirectoryStreamTest {

    private static final String TEST_DIR = "octopus_test_" + System.currentTimeMillis();

    private static Path resolve(Files files, Path root, String path) throws OctopusIOException { 
        return files.newPath(root.getFileSystem(), root.getRelativePath().resolve(path));
    }
    
    @org.junit.BeforeClass
    public static void prepareClass() throws OctopusIOException, OctopusException {

        Octopus octopus = OctopusFactory.newOctopus(null);

        Files files = octopus.files();
        Path root = FileUtils.getLocalCWD(files);
        Path testDir = resolve(files, root, TEST_DIR);
        files.createDirectory(testDir);

        Path file0 = resolve(files, testDir, "file0");
        Path file1 = resolve(files, testDir, "file2");
        Path file2 = resolve(files, testDir, "file3");

        files.createFile(file0);
        files.createFile(file1);
        files.createFile(file2);

        OctopusFactory.endOctopus(octopus);
    }

    @org.junit.AfterClass
    public static void cleanupClass() throws OctopusException, OctopusIOException {

        Octopus octopus = OctopusFactory.newOctopus(null);

        Files files = octopus.files();
        Path root = FileUtils.getLocalCWD(files);
        Path testDir = resolve(files, root, TEST_DIR);
        
        Path file0 = resolve(files, testDir, "file0");
        Path file1 = resolve(files, testDir, "file2");
        Path file2 = resolve(files, testDir, "file3");

        if (files.exists(testDir)) {
            files.delete(file0);
            files.delete(file1);
            files.delete(file2);
            files.delete(testDir);
        }

        OctopusFactory.endOctopus(octopus);
    }

    private Octopus octopus;
    private Files files;
    private FileSystem fs;
    private Path root;
    private Path testDir;

    class AllTrue implements DirectoryStream.Filter {
        @Override
        public boolean accept(Path entry) {
            return true;
        }
    }

    class AllFalse implements DirectoryStream.Filter {
        @Override
        public boolean accept(Path entry) {
            return false;
        }
    }

    @org.junit.Before
    public void prepareTest() throws OctopusIOException, OctopusException {

        octopus = OctopusFactory.newOctopus(null);

        files = octopus.files();
        root = FileUtils.getLocalCWD(files);
        fs = root.getFileSystem();
        testDir = resolve(files, root, TEST_DIR);
    }

    @org.junit.After
    public void cleanupTest() throws OctopusIOException, OctopusException {
        OctopusFactory.endOctopus(octopus);
    }

    @org.junit.Test(expected = OctopusIOException.class)
    public void test_nonexistant_dir() throws Exception {
        Path path = new PathImplementation(mock(FileSystem.class), new RelativePath("aap"));
        new LocalDirectoryStream(path, new AllTrue());
    }

    @org.junit.Test
    public void test_ok_allTrue() throws Exception {

        LocalDirectoryStream stream = new LocalDirectoryStream(testDir, new AllTrue());

        while (stream.hasNext()) {
            stream.next();
        }

        stream.close();
    }

    @org.junit.Test
    public void test_ok_allTrue2() throws Exception {

        LocalDirectoryStream stream = new LocalDirectoryStream(testDir, new AllTrue());

        for (int i = 0; i < 10; i++) {
            if (!stream.hasNext()) {
                throw new Exception("Failed to call hasNext in test_ok_allTrue!");
            }
        }

        while (stream.hasNext()) {
            stream.next();
        }

        stream.close();
    }

    @org.junit.Test
    public void test_ok_allFalse() throws Exception {

        LocalDirectoryStream stream = new LocalDirectoryStream(testDir, new AllFalse());

        while (stream.hasNext()) {
            stream.next();
        }

        stream.close();
    }

    @org.junit.Test(expected = NoSuchElementException.class)
    public void test_ok_allFalse2() throws Exception {

        LocalDirectoryStream stream = new LocalDirectoryStream(testDir, new AllFalse());
        stream.next();
    }

    @org.junit.Test(expected = NoSuchElementException.class)
    public void test_fails_next() throws Exception {

        LocalDirectoryStream stream = new LocalDirectoryStream(testDir, new AllTrue());

        while (true) {
            stream.next();
        }
    }

    @org.junit.Test(expected = UnsupportedOperationException.class)
    public void test_fails_remove() throws Exception {
        LocalDirectoryStream stream = new LocalDirectoryStream(testDir, new AllTrue());
        stream.remove();
    }

    @org.junit.Test
    public void test_ok_double_close() throws Exception {
        LocalDirectoryStream stream = new LocalDirectoryStream(testDir, new AllTrue());
        stream.close();
        stream.close();
    }
}
