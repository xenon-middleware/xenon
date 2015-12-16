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
package nl.esciencecenter.xenon.adaptors.local;

import static org.mockito.Mockito.mock;

import java.util.NoSuchElementException;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.engine.files.PathImplementation;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;
import nl.esciencecenter.xenon.util.Utils;

/**
 * 
 */
public class LocalDirectoryStreamTest {

    private static final String TEST_DIR = "xenon_test_" + System.currentTimeMillis();

    private static Path resolve(Files files, Path root, String path) throws XenonException { 
        return files.newPath(root.getFileSystem(), root.getRelativePath().resolve(path));
    }
    
    @org.junit.BeforeClass
    public static void prepareClass() throws XenonException, XenonException {

        Xenon xenon = XenonFactory.newXenon(null);

        Files files = xenon.files();
        Path root = Utils.getLocalCWD(files);
        Path testDir = resolve(files, root, TEST_DIR);
        files.createDirectory(testDir);

        Path file0 = resolve(files, testDir, "file0");
        Path file1 = resolve(files, testDir, "file2");
        Path file2 = resolve(files, testDir, "file3");

        files.createFile(file0);
        files.createFile(file1);
        files.createFile(file2);

        XenonFactory.endXenon(xenon);
    }

    @org.junit.AfterClass
    public static void cleanupClass() throws XenonException, XenonException {

        Xenon xenon = XenonFactory.newXenon(null);

        Files files = xenon.files();
        Path root = Utils.getLocalCWD(files);
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

        XenonFactory.endXenon(xenon);
    }

    private Xenon xenon;
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
    public void prepareTest() throws XenonException, XenonException {

        xenon = XenonFactory.newXenon(null);

        Files files = xenon.files();
        Path root = Utils.getLocalCWD(files);
        testDir = resolve(files, root, TEST_DIR);
    }

    @org.junit.After
    public void cleanupTest() throws XenonException, XenonException {
        XenonFactory.endXenon(xenon);
    }

    @org.junit.Test(expected = XenonException.class)
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

        // expecting at most 10000 items, otherwise, the stream might go on forever
        for (int i = 0; i < 10000; i++) {
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
