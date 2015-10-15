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
package nl.esciencecenter.xenon.files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.util.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract FileSystem tests. This class runs a set of test scenarios on the (remote) filesystem. This is one abstract test class
 * which can be used for all FileSystem adaptors.
 * 
 * @author Piter T. de Boer
 */
public abstract class AbstractFileTests {
    private static final Logger logger = LoggerFactory.getLogger(AbstractFileTests.class);
    protected Xenon xenon;
    protected Files files;

    /**
     * The FileSystem instance to run integration tests on.
     */
    protected FileSystem fileSystem;

    protected void initObject() throws Exception {
        setupFiles();
        try {
            Path testDir = getTestDir();
            if (!files.exists(testDir)) {
                files.createDirectory(testDir);
            }
        } finally {
            cleanupFiles();
        }
    }

    protected void finalizeObject() throws Exception {
        setupFiles();
        try {
            Utils.recursiveDelete(files, getTestDir());
        } finally {
            cleanupFiles();
        }
    }

    @Before
    public void setupFiles() throws XenonException, Exception {
        xenon = XenonFactory.newXenon(null);
        files = xenon.files();
        URI uri = getTestLocation();
        fileSystem = files.newFileSystem(uri.getScheme(), uri.getAuthority(), getCredentials(), null);
    }

    @After
    public void cleanupFiles() throws XenonException {
        try {
            files.close(fileSystem);
        } finally {
            XenonFactory.endXenon(xenon);
        }
    }

    protected static int uniqueIdcounter = 1;

    // ========
    // Instance 
    // ======== 

    /**
     * Return credentials for this FileSystem if needed for the integration tests.
     * 
     * @return Xenon Credential for the FileSystem to be tested.
     * @throws XenonException
     */
    public abstract Credential getCredentials() throws XenonException;

    /**
     * Return test location. Subclasses need to override this.
     * 
     * @return the test location as URI
     */
    public abstract URI getTestLocation() throws Exception;

    // =========================================
    // Helper Methods for the integration tests.  
    // =========================================

    /**
     * Helper method to return current test dir. This directory must exist and must be writable.
     */
    protected Path getTestDir() throws Exception {
        String testPath = this.getTestLocation().getPath();
        return files.newPath(fileSystem, new RelativePath(testPath));
    }

    protected Path createSubdir(Path parentDirPath, String subDir) throws XenonException {
        Path absPath = Utils.resolveWithRoot(files, parentDirPath, subDir);
        logger.info("createSubdir: '{}' -> '{}'", subDir, absPath);
        files.createDirectory(absPath);
        return absPath;
    }

    /**
     * Create new and unique sub-directory for testing purposes. To avoid previous failed tests to interfere with current test
     * run, an unique directory has to be created each time. It is recommend after each successful test run to delete the test
     * directory and its contents.
     * 
     * @param parentDirPath
     *            - parent directory to create (sub) directory in.
     * @param dirPrefix
     *            - prefix of the sub-directory. An unique number will be append to this name,
     * @return AbsolutPath of new created directory
     */
    protected Path createUniqueTestSubdir(Path parentDirPath, String dirPrefix) throws XenonException {
        int myid;
        Path absPath;
        do {
            myid = uniqueIdcounter++;
            absPath = Utils.resolveWithRoot(files, parentDirPath, dirPrefix + "." + myid);
        } while (files.exists(absPath));

        logger.info("createUniqueTestSubdir: '{}'+{} => '{}'\n", dirPrefix, myid, absPath);
        files.createDirectory(absPath);
        return absPath;
    }

    /**
     * Create new and unique test file.
     * 
     * @param parentDirPath
     *            - parent directory to create file into.
     * @param filePrefix
     *            - filePrefix to use as filename. An unique number will be added to the fileName.
     * @param createFile
     *            - actually create (empty) file on (remote) file system.
     * @return new Path, which points to existing file if createFile was true.
     * @throws XenonException
     */
    protected Path createUniqueTestFile(Path parentDirPath, String filePrefix, boolean createFile)
            throws XenonException {

        int myid;
        Path absPath;
        do {
            myid = uniqueIdcounter++;
            absPath = Utils.resolveWithRoot(files, parentDirPath, filePrefix + "." + myid);
        } while (files.exists(absPath));

        logger.info("createUniqueTestFile: '{}'+{} => '{}'\n", filePrefix, myid, absPath);
        if (createFile) {
            files.createFile(absPath);
        }
        return absPath;
    }

    protected Path createFile(Path parentDirPath, String subFile) throws XenonException {
        Path absPath = Utils.resolveWithRoot(files, parentDirPath, subFile);
        files.createFile(absPath);
        return absPath;
    }

    protected void deletePaths(Path[] paths, boolean assertDeletion) throws XenonException {
        for (Path path : paths) {
            files.delete(path);
            if (assertDeletion)
                assertFalse("After Files().delete(), the path may not exist:" + path, files.exists(path));
        }
    }

    // ========================
    // Actual integration tests  
    // ========================

    @Test
    public void testGetTestDir() throws Exception {
        Path path = getTestDir();
        assertNotNull("TestPath returned NULL", path);
        assertNotNull("Actual path element of Path may not be NULL", path);

        logger.info("Test location path scheme      ={}\n", path.getFileSystem().getScheme());
        logger.info("Test location path location    ={}\n", path.getFileSystem().getLocation());
        logger.info("Test location path          ={}\n", path.getRelativePath().getAbsolutePath());
        logger.info("Test location toString()    ={}\n", path.toString());
        logger.info("Test location getFileName() ={}\n", path.getRelativePath().getFileName());

        assertTrue("Root test location must exists (won't create here):" + path, files.exists(path));
    }

    /**
     * Test creation and delete of file in one test. If this test fails, all other test will fail to!
     */
    @Test
    public void testCreateDeleteEmptyFile() throws Exception {
        Path filePath = Utils.resolveWithRoot(files, getTestDir(), "testFile01");

        // Previous test run could have gone wrong. Indicate here that a previous test run failed. 
        boolean preExisting = files.exists(filePath);
        if (preExisting) {
            try {
                // try to delete first ! 
                files.delete(filePath);
            } catch (XenonException e) {
                // may fail, no problem
            }
            assertFalse(
                    "exists(): Can't test createFile is previous test file already exists. File should now be deleted, please run test again.",
                    preExisting);
        }

        files.createFile(filePath);
        // enforce ? 
        boolean exists = files.exists(filePath);
        assertTrue("exist(): After createFile() exists() reports false.", exists);

        files.delete(filePath);
        assertTrue("delet(): After delete, method exist() return true.", exists);
    }

    /**
     * Test creation and deletion of directory in one test. If this test fails, all other test will fail to!
     */
    @Test
    public void testCreateDeleteEmptySubdir() throws Exception {
        Path dirPath = Utils.resolveWithRoot(files, getTestDir(), "testSubdir01");
        assertFalse("Previous test directory already exists. Please clean test location.:" + dirPath,
                files.exists(dirPath));

        files.createDirectory(dirPath);
        // test both ? 
        boolean exists = files.exists(dirPath);
        assertTrue("After createDirectory(), method exists() reports false for path:" + dirPath, exists);

        assertDirIsEmpty(files, dirPath);

        files.delete(dirPath);
        exists = files.exists(dirPath);
        assertFalse("After delete() on directory, method exists() reports false.", exists);
    }

    public void assertDirIsEmpty(Files files, Path dirPath) throws Exception {

        DirectoryStream<Path> dirStream = files.newDirectoryStream(dirPath);
        Iterator<Path> iterator = dirStream.iterator();
        assertFalse("Method hasNext() from empty directory iterator must return false.", iterator.hasNext());
    }

    @Test
    public void testgetFileSystemEntryPath() throws Exception {
        // just test whether it works: 
        Path relEntryPath = fileSystem.getEntryPath();
        assertNotNull("Entry Path may not be null.", relEntryPath);
    }

    @Test
    public void testResolveRootPath() throws Exception {
        // resolve "/", for current filesystems this must equal to "/" ? 
        Path rootPath = files.newPath(fileSystem, new RelativePath("/"));
        assertEquals("Absolute path of resolved path '/' must equal to '/'.", "/", rootPath);
    }

    @Test
    public void testNewDirectoryStreamTestDir() throws Exception {
        Path path = getTestDir();

        // Just test whether it works and directory is readable (other tests will fail if this doesn't work). 
        for (Path pathEl : files.newDirectoryStream(path)) {
            logger.info(" -(Path)Path     ={}:'{}'\n", pathEl.getFileSystem().getScheme(), pathEl);
            logger.info(" -(Path)getPath()={}:'{}'\n", pathEl.getFileSystem().getLocation(), pathEl);
        }
    }

    @Test
    public void testNewDirectoryAttributesStreamTestDir() throws Exception {
        Path path = getTestDir();
        DirectoryStream<PathAttributesPair> dirStream = files.newAttributesDirectoryStream(path);

        // Just test whether it works and directory is readable (other tests will fail if this doesn't work). 
        for (PathAttributesPair pathEl : dirStream) {
            logger.info(" -(PathAttributesPair)path='{}'\n", pathEl.path());
        }
    }

    @Test
    public void testCreateListAndDelete3Subdirs() throws Exception {
        // PRE:
        Path testDirPath = createUniqueTestSubdir(getTestDir(), "testSubdir3");
        assertDirIsEmpty(files, testDirPath);

        // TEST: 
        Path dir1 = createSubdir(testDirPath, "subDir1");
        Path dir2 = createSubdir(testDirPath, "subDir2");
        Path dir3 = createSubdir(testDirPath, "subDir3");

        int count = 0;

        for (Path pathEl : files.newDirectoryStream(testDirPath)) {
            logger.info(" -(Path)Path     ={}:'{}'\n", pathEl.getFileSystem().getScheme(), pathEl);
            logger.info(" -(Path)getPath()={}:'{}'\n", pathEl.getFileSystem().getLocation(), pathEl);
            count++;
        }

        logger.info("Directory has: {} entries\n", count);
        assertEquals("Directory must have 3 sub directories\n", 3, count);

        // POST: 
        deletePaths(new Path[] { dir1, dir2, dir3, testDirPath }, true);

    }

    @Test
    public void testCreateListAndDelete3FilesWithAttributes() throws Exception {
        // PRE: 
        Path testDirPath = createUniqueTestSubdir(getTestDir(), "testSubdir4");
        assertDirIsEmpty(files, testDirPath);

        // TEST: 
        Path file1 = createFile(testDirPath, "file1");
        Path file2 = createFile(testDirPath, "file2");
        Path file3 = createFile(testDirPath, "file3");

        DirectoryStream<PathAttributesPair> dirStream = files.newAttributesDirectoryStream(testDirPath);

        // Regression test: this has failed before. Issue #91
        int count = 0;

        for (PathAttributesPair el : dirStream) {
            Path path = el.path();
            logger.info(" -(Path)Path     ={}:'{}'\n", path.getFileSystem().getScheme(), path);
            logger.info(" -(Path)getPath()={}:'{}'\n", path.getFileSystem().getLocation(), path);
            count++;
        }

        logger.info("Directory has:{} entries\n", count);
        assertEquals("Directory must have 3 file entries\n", 3, count);

        // POST: 
        deletePaths(new Path[] { file1, file2, file3, testDirPath }, true);
    }

    @Test
    public void testCreateListAndDelete3SubdirsWithAttributes() throws Exception {
        // PRE:
        Path testDirPath = createUniqueTestSubdir(getTestDir(), "testSubdir5");
        assertDirIsEmpty(files, testDirPath);

        // TEST: 
        Path dir1 = createSubdir(testDirPath, "subDir1");
        Path dir2 = createSubdir(testDirPath, "subDir2");
        Path dir3 = createSubdir(testDirPath, "subDir3");

        DirectoryStream<PathAttributesPair> dirStream = files.newAttributesDirectoryStream(testDirPath);

        int count = 0;

        for (PathAttributesPair el : dirStream) {
            Path path = el.path();
            logger.info(" -(Path)Path     ={}:'{}'\n", path.getFileSystem().getScheme(), path);
            logger.info(" -(Path)getPath()={}:'{}'\n", path.getFileSystem().getLocation(), path);
            count++;
        }

        logger.info("Directory has:{} entries\n", count);
        assertEquals("Directory must have 3 sub directories\n", 3, count);

        // POST: 
        deletePaths(new Path[] { dir1, dir2, dir3, testDirPath }, true);
    }

    // ===================================
    // Test Stream Read and Write Methods 
    // ===================================

    /**
     * Test write and read back 0 bytes.
     */
    @Test
    public void testStreamWriteAndReadNillBytes() throws Exception {
        // empty array: 
        byte nilBytes[] = new byte[0];
        testStreamWriteAndReadBytes(nilBytes, 0);
    }

    /**
     * Test write and read back 256 bytes to a NEW file.
     */
    @Test
    public void testStreamWriteAndRead256Bytes() throws Exception {
        // one byte: 
        byte oneByte[] = new byte[1];
        oneByte[0] = 13;
        testStreamWriteAndReadBytes(oneByte, 1);

        // 256 bytes 
        int n = 256;
        byte bytes[] = new byte[n];

        for (int i = 0; i < n; i++) {
            bytes[i] = (byte) (n % 256);
        }
        testStreamWriteAndReadBytes(bytes, 256);
    }

    /**
     * Helper method to write a series of bytes.
     */
    protected void testStreamWriteAndReadBytes(byte bytes[], int numBytes) throws Exception {
        // PRE: 
        Path testFilePath = createUniqueTestFile(getTestDir(), "testStreaReadWriteFile03", true);
        assertTrue("Test file doesn't exists:" + testFilePath, files.exists(testFilePath));

        // TEST: 
        java.io.OutputStream outps = files.newOutputStream(testFilePath, OpenOption.CREATE);

        outps.write(bytes);

        try {
            outps.close();
        } catch (IOException e) {
            logger.debug("IOException when closing test file:{}\n", e);
        }

        InputStream inps = files.newInputStream(testFilePath);

        byte readBytes[] = new byte[numBytes];
        int totalRead = 0;

        while (totalRead < numBytes) { // read loop: 

            int numRead = inps.read(readBytes, totalRead, (numBytes - totalRead));
            if (numRead >= 0) {
                totalRead += numRead;
            } else {
                throw new IOException("Got EOF when reading from testFile:" + testFilePath);
            }
        }

        // readBytes[100]=13; // test fault insertion here. 

        for (int i = 0; i < numBytes; i++) {
            assertEquals("Byte at #" + i + " does not equal orginal value.", bytes[i], readBytes[i]);
        }

        try {
            inps.close();
        } catch (IOException e) {
            logger.debug("IOException when closing test file:{}\n", e);
        }

        // POST: 
        deletePaths(new Path[] { testFilePath }, true);
    }
}
