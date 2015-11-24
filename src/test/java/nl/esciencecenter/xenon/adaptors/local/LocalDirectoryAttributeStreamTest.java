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

import java.util.HashMap;
import java.util.NoSuchElementException;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.XenonRuntimeException;
import nl.esciencecenter.xenon.Util;
import nl.esciencecenter.xenon.adaptors.local.LocalAdaptor;
import nl.esciencecenter.xenon.adaptors.local.LocalDirectoryAttributeStream;
import nl.esciencecenter.xenon.adaptors.local.LocalDirectoryStream;
import nl.esciencecenter.xenon.adaptors.local.LocalFiles;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.files.PathImplementation;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;
import nl.esciencecenter.xenon.util.Utils;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class LocalDirectoryAttributeStreamTest {

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

        Path dir0 = resolve(files, testDir, "dir0");
        Path file4 = resolve(files, dir0, "file4");

        if (files.exists(testDir)) {

            if (files.exists(file4)) {
                files.delete(file4);
            }

            if (files.exists(dir0)) {
                files.delete(dir0);
            }

            files.delete(file0);
            files.delete(file1);
            files.delete(file2);
            files.delete(testDir);
        }

        XenonFactory.endXenon(xenon);
    }

    private XenonEngine xenon;
    private FileSystem fs;
    private Path testDir;

    private LocalFiles localFiles;

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
    public void prepareTest() throws Exception {
        xenon = Util.createXenonEngine(null);
        LocalAdaptor localAdaptor = new LocalAdaptor(xenon, new HashMap<String, String>());
        localFiles = new LocalFiles(localAdaptor, xenon.getCopyEngine());
        Path root = Utils.getLocalCWD(localFiles);
        fs = root.getFileSystem();
        testDir = resolve(localFiles, root, TEST_DIR);
    }

    @org.junit.After
    public void cleanupTest() throws Exception {
        Util.endXenonEngine(xenon);
    }

    @org.junit.Test(expected = XenonException.class)
    public void test_nonexistant_dir() throws Exception {
        Path path = new PathImplementation(fs, new RelativePath("aap"));
        new LocalDirectoryAttributeStream(localFiles, new LocalDirectoryStream(path, new AllTrue()));
    }

    @org.junit.Test
    public void test_ok_allTrue() throws Exception {

        LocalDirectoryAttributeStream stream = new LocalDirectoryAttributeStream(localFiles, new LocalDirectoryStream(testDir,
                new AllTrue()));

        while (stream.hasNext()) {
            stream.next();
        }

        stream.close();
    }

    @org.junit.Test
    public void test_ok_allTrue2() throws Exception {

        LocalDirectoryAttributeStream stream = new LocalDirectoryAttributeStream(localFiles, new LocalDirectoryStream(testDir,
                new AllTrue()));

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

        LocalDirectoryAttributeStream stream = new LocalDirectoryAttributeStream(localFiles, new LocalDirectoryStream(testDir,
                new AllFalse()));

        while (stream.hasNext()) {
            stream.next();
        }

        stream.close();
    }

    @org.junit.Test(expected = NoSuchElementException.class)
    public void test_ok_allFalse2() throws Exception {

        LocalDirectoryAttributeStream stream = new LocalDirectoryAttributeStream(localFiles, new LocalDirectoryStream(testDir,
                new AllFalse()));

        stream.next();
    }

    @org.junit.Test(expected = NoSuchElementException.class)
    public void test_fails_next() throws Exception {

        LocalDirectoryAttributeStream stream = new LocalDirectoryAttributeStream(localFiles, new LocalDirectoryStream(testDir,
                new AllTrue()));

        // expecting at most 10000 items, otherwise, the stream might go on forever
        for (int i = 0; i < 10000; i++) {
            stream.next();
        }
    }

    @org.junit.Test(expected = XenonRuntimeException.class)
    public void test_remove_file_halfway_allTrue() throws Exception {

        Path dir0 = resolve(localFiles, testDir, "dir0");
        localFiles.createDirectory(dir0);

        Path file4 = resolve(localFiles, dir0, "file4");
        localFiles.createFile(file4);

        if (!localFiles.exists(file4)) {
            throw new Exception("Failed to create file4!");
        }

        LocalDirectoryAttributeStream stream = new LocalDirectoryAttributeStream(localFiles, new LocalDirectoryStream(dir0,
                new AllTrue()));

        while (stream.hasNext()) {
            localFiles.delete(file4);
            stream.next();
        }

        stream.close();
    }

    @org.junit.Test(expected = UnsupportedOperationException.class)
    public void test_fails_remove() throws Exception {

        LocalDirectoryAttributeStream stream = new LocalDirectoryAttributeStream(localFiles, new LocalDirectoryStream(testDir,
                new AllTrue()));

        stream.remove();
    }

    @org.junit.Test
    public void test_ok_double_close() throws Exception {
        LocalDirectoryAttributeStream stream = new LocalDirectoryAttributeStream(localFiles, new LocalDirectoryStream(testDir,
                new AllTrue()));

        stream.close();
        stream.close();
    }
}
