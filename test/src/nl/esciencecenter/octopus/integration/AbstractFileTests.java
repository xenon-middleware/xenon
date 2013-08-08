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
package nl.esciencecenter.octopus.integration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.PathAttributesPair;
import nl.esciencecenter.octopus.files.RelativePath;

/**
 * Abstract FileSystem tests. This class runs a set of test scenarios on the (remote) filesystem. This is one abstract test class
 * which can be used for all FileSystem adaptors.
 * 
 * @author Piter T. de Boer
 */
abstract public class AbstractFileTests {

    /**
     * Singleton Engine for all tests
     */
    protected static Octopus octopus = null;

    protected static Files getFiles() throws OctopusException {

        // class synchronization:
        synchronized (AbstractFileTests.class) {

            // init octopus singleton instance: 
            if (octopus == null) {
                octopus = OctopusFactory.newOctopus(null);
            }

            return octopus.files();
        }
    }

    // todo logging
    public static void debugPrintf(String format, Object... args) {
        System.out.printf("DEBUG:" + format, args);
    }

    // todo logging
    public static void infoPrintf(String format, Object... args) {
        System.out.printf("INFO:" + format, args);
    }

    // todo logging
    public static void errorPrintf(String format, Object... args) {
        System.err.printf("ERROR:" + format, args);
    }

    protected static int uniqueIdcounter = 1;

    // ========
    // Instance 
    // ======== 

    /**
     * The FileSystem instance to run integration tests on.
     */
    protected FileSystem fileSystem = null;

    /**
     * Get actual FileSystem implementation to run test on. Test this before other tests:
     */
    protected FileSystem getFileSystem() throws Exception {

        // Use singleton for all tests. Could create new Filesystem instance per test.  
        synchronized (this) {
            if (fileSystem == null) {
                URI uri = getTestLocation();
                URI fsURI = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), "/", null, null);
                fileSystem = getFiles().newFileSystem(fsURI, getCredentials(), null);
            }

            return fileSystem;
        }
    }

    /**
     * Return credentials for this FileSystem if needed for the integration tests.
     * 
     * @return Octopus Credential for the FileSystem to be tested.
     * @throws OctopusException
     */
    abstract Credential getCredentials() throws OctopusException;

    /**
     * Return test location. Subclasses need to override this.
     * 
     * @return the test location as URI
     */
    abstract public java.net.URI getTestLocation() throws Exception;

    // =========================================
    // Helper Methods for the integration tests.  
    // =========================================

    /**
     * Helper method to return current test dir. This directory must exist and must be writable.
     */
    protected AbsolutePath getTestDir() throws Exception {
        FileSystem fs = getFileSystem();
        String testPath = this.getTestLocation().getPath();
        return getFiles().newPath(fs, new RelativePath(testPath));
    }

    protected AbsolutePath createSubdir(AbsolutePath parentDirPath, String subDir) throws OctopusIOException, OctopusException {
        RelativePath relSubPath = new RelativePath(subDir);
        AbsolutePath absPath = parentDirPath.resolve(relSubPath);
        infoPrintf("createSubdir: '%s' -> '%s'\n", subDir, absPath.getPath());
        return getFiles().createDirectory(absPath);
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
    protected AbsolutePath createUniqueTestSubdir(AbsolutePath parentDirPath, String dirPrefix) throws OctopusIOException,
            OctopusException {
        do {
            int myid = uniqueIdcounter++;
            RelativePath relSubPath = new RelativePath(dirPrefix + "." + myid);
            AbsolutePath absPath = parentDirPath.resolve(relSubPath);

            if (getFiles().exists(absPath) == false) {
                infoPrintf("createUniqueTestSubdir: '%s'+%d => '%s'\n", dirPrefix, myid, absPath.getPath());
                return getFiles().createDirectory(absPath);
            }

        } while (true);
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
     * @return new AbsolutePath, which points to existing file if createFile was true.
     * @throws OctopusIOException
     * @throws OctopusException
     */
    protected AbsolutePath createUniqueTestFile(AbsolutePath parentDirPath, String filePrefix, boolean createFile)
            throws OctopusIOException, OctopusException {

        do {
            int myid = uniqueIdcounter++;
            RelativePath relSubPath = new RelativePath(filePrefix + "." + myid);
            AbsolutePath absPath = parentDirPath.resolve(relSubPath);

            if (getFiles().exists(absPath) == false) {

                infoPrintf("createUniqueTestFile: '%s'+%d => '%s'\n", filePrefix, myid, absPath.getPath());
                if (createFile)
                    return getFiles().createFile(absPath);
                else
                    return absPath;
            }

        } while (true);
    }

    protected AbsolutePath createFile(AbsolutePath parentDirPath, String subFile) throws OctopusIOException, OctopusException {

        RelativePath relSubPath = new RelativePath(subFile);
        AbsolutePath absPath = parentDirPath.resolve(relSubPath);
        return getFiles().createFile(absPath);
    }

    protected void deletePaths(AbsolutePath[] absolutePaths, boolean assertDeletion) throws OctopusIOException, OctopusException {

        for (AbsolutePath path : absolutePaths) {
            getFiles().delete(path);
            if (assertDeletion)
                assertFalse("After Files().delete(), the path may not exist:" + path.getPath(), getFiles().exists(path));
        }
    }

    // ========================
    // SetUp Integration Tests
    // ========================

    @org.junit.Before
    public void checkTestSetup() throws Exception {
        // Basic Sanity checks of the test environment; 
        // Typically Exceptions should be thrown here if the call fails. 
        URI uri = getTestLocation();
        assertNotNull("Setup: Can't do tests on a NULL location", uri);
        assertNotNull("Setup: The Files() interface is NULL", getFiles());
        assertNotNull("Setup: Actual FileSystem to run tests on is NULL", getFileSystem());
    }

    // ========================
    // Actual integration tests  
    // ========================

    @org.junit.Test
    public void testGetTestDir() throws Exception {
        AbsolutePath path = getTestDir();
        assertNotNull("TestPath returned NULL", path);
        assertNotNull("Actual path element of AbsolutePath may not be NULL", path.getPath());

        infoPrintf("Test location path URI      =%s\n", path.getFileSystem().getUri());
        infoPrintf("Test location path          =%s\n", path.getPath());
        infoPrintf("Test location toString()    =%s\n", path.toString());
        infoPrintf("Test location getFileName() =%s\n", path.getFileName());

        assertTrue("Root test location must exists (won't create here):" + path.getPath(), getFiles().exists(path));
    }

    /**
     * Test creation and delete of file in one test. If this test fails, all other test will fail to!
     */
    @org.junit.Test
    public void testCreateDeleteEmptyFile() throws Exception {
        AbsolutePath filePath = getTestDir().resolve(new RelativePath("testFile01"));

        Files files = getFiles();

        // Previous test run could have gone wrong. Indicate here that a previous test run failed. 
        boolean preExisting = files.exists(filePath);
        if (preExisting) {
            try {
                // try to delete first ! 
                files.delete(filePath);
            } catch (Exception e) {

            }
            assertFalse(
                    "exists(): Can't test createFile is previous test file already exists. File should now be deleted, please run test again.",
                    preExisting);
        }

        AbsolutePath actualPath = files.createFile(filePath);
        // enforce ? 
        assertEquals("createFile(): New path is not equal to given path", filePath, actualPath);
        boolean exists = files.exists(filePath);
        assertTrue("exist(): After createFile() exists() reports false.", exists);

        files.delete(filePath);
        assertTrue("delet(): After delete, method exist() return true.", exists);
    }

    /**
     * Test creation and deletion of directory in one test. If this test fails, all other test will fail to!
     */
    @org.junit.Test
    public void testCreateDeleteEmptySubdir() throws Exception {

        Files files = getFiles();

        AbsolutePath dirPath = getTestDir().resolve(new RelativePath("testSubdir01"));
        assertFalse("Previous test directory already exists. Please clean test location.:" + dirPath.getPath(),
                files.exists(dirPath));

        AbsolutePath actualPath = getFiles().createDirectory(dirPath);
        // test both ? 
        boolean exists = files.exists(dirPath);
        assertTrue("After createDirectory(), method exists() reports false for given path:" + dirPath, exists);
        exists = files.exists(actualPath);
        assertTrue("After createDirectory(), method exists() reports false for returned path:" + actualPath, exists);

        assertDirIsEmpty(files, dirPath);

        files.delete(dirPath);
        exists = files.exists(dirPath);
        assertFalse("After delete() on directory, method exists() reports false.", exists);
    }

    public void assertDirIsEmpty(Files files, AbsolutePath dirPath) throws Exception {

        DirectoryStream<AbsolutePath> dirStream = files.newDirectoryStream(dirPath);
        Iterator<AbsolutePath> iterator = dirStream.iterator();
        assertFalse("Method hasNext() from empty directory iterator must return false.", iterator.hasNext());
    }

    @org.junit.Test
    public void testgetFileSystemEntryPath() throws Exception {

        FileSystem fs = getFileSystem();

        // just test whether it works: 
        AbsolutePath relEntryPath = fs.getEntryPath();
        assertNotNull("Entry Path may not be null.", relEntryPath);
    }

    @org.junit.Test
    public void testResolveRootPath() throws Exception {

        FileSystem fs = getFileSystem();

        // resolve "/", for current filesystems this must equal to "/" ? 
        AbsolutePath rootPath = getFiles().newPath(fs, new RelativePath("/"));
        assertEquals("Absolute path of resolved path '/' must equal to '/'.", "/", rootPath.getPath());
    }

    @org.junit.Test
    public void testNewDirectoryStreamTestDir() throws Exception {

        AbsolutePath path = getTestDir();
        DirectoryStream<AbsolutePath> dirStream = getFiles().newDirectoryStream(path);
        Iterator<AbsolutePath> iterator = dirStream.iterator();

        // Just test whether it works and directory is readable (other tests will fail if this doesn't work). 
        while (iterator.hasNext()) {
            AbsolutePath pathEl = iterator.next();
            URI fsUri = pathEl.getFileSystem().getUri();
            infoPrintf(" -(AbsolutePath)Path     =%s:'%s'\n", fsUri, pathEl);
            infoPrintf(" -(AbsolutePath)getPath()=%s:'%s'\n", fsUri, pathEl.getPath());
        }
    }

    @org.junit.Test
    public void testNewDirectoryAttributesStreamTestDir() throws Exception {

        AbsolutePath path = getTestDir();
        DirectoryStream<PathAttributesPair> dirStream = getFiles().newAttributesDirectoryStream(path);
        Iterator<PathAttributesPair> iterator = dirStream.iterator();

        // Just test whether it works and directory is readable (other tests will fail if this doesn't work). 
        while (iterator.hasNext()) {
            PathAttributesPair pathEl = iterator.next();
            infoPrintf(" -(PathAttributesPair)path='%s'\n", pathEl.path().getPath());
        }
    }

    @org.junit.Test
    public void testCreateListAndDelete3Subdirs() throws Exception {

        // PRE:
        Files files = getFiles();
        AbsolutePath testDirPath = createUniqueTestSubdir(getTestDir(), "testSubdir3");
        assertDirIsEmpty(files, testDirPath);

        // TEST: 
        AbsolutePath dir1 = createSubdir(testDirPath, "subDir1");
        AbsolutePath dir2 = createSubdir(testDirPath, "subDir2");
        AbsolutePath dir3 = createSubdir(testDirPath, "subDir3");

        DirectoryStream<AbsolutePath> dirStream = getFiles().newDirectoryStream(testDirPath);
        Iterator<AbsolutePath> iterator = dirStream.iterator();

        int count = 0;

        while (iterator.hasNext()) {
            AbsolutePath pathEl = iterator.next();
            URI fsUri = pathEl.getFileSystem().getUri();
            infoPrintf(" -(AbsolutePath)Path     =%s:'%s'\n", fsUri, pathEl);
            infoPrintf(" -(AbsolutePath)getPath()=%s:'%s'\n", fsUri, pathEl.getPath());
            count++;
        }

        infoPrintf("Directory has:%d entries\n", count);
        assertEquals("Directory must have 3 sub directories\n", 3, count);

        // POST: 
        deletePaths(new AbsolutePath[] { dir1, dir2, dir3, testDirPath }, true);

    }

    @org.junit.Test
    public void testCreateListAndDelete3FilesWithAttributes() throws Exception {

        // PRE: 
        Files files = getFiles();
        AbsolutePath testDirPath = createUniqueTestSubdir(getTestDir(), "testSubdir4");
        assertDirIsEmpty(files, testDirPath);

        // TEST: 
        AbsolutePath file1 = createFile(testDirPath, "file1");
        AbsolutePath file2 = createFile(testDirPath, "file2");
        AbsolutePath file3 = createFile(testDirPath, "file3");

        DirectoryStream<PathAttributesPair> dirStream = getFiles().newAttributesDirectoryStream(testDirPath);
        Iterator<PathAttributesPair> iterator = dirStream.iterator();

        // Regression test: this has failed before. Issue #91
        int count = 0;

        while (iterator.hasNext()) {
            PathAttributesPair el = iterator.next();
            AbsolutePath path = el.path();
            URI fsUri = el.path().getFileSystem().getUri();
            infoPrintf(" -(AbsolutePath)Path     =%s:'%s'\n", fsUri, path);
            infoPrintf(" -(AbsolutePath)getPath()=%s:'%s'\n", fsUri, path.getPath());
            count++;
        }

        infoPrintf("Directory has:%d entries\n", count);
        assertEquals("Directory must have 3 file entries\n", 3, count);

        // POST: 
        deletePaths(new AbsolutePath[] { file1, file2, file3, testDirPath }, true);
    }

    @org.junit.Test
    public void testCreateListAndDelete3SubdirsWithAttributes() throws Exception {

        // PRE:
        Files files = getFiles();
        AbsolutePath testDirPath = createUniqueTestSubdir(getTestDir(), "testSubdir5");
        assertDirIsEmpty(files, testDirPath);

        // TEST: 
        AbsolutePath dir1 = createSubdir(testDirPath, "subDir1");
        AbsolutePath dir2 = createSubdir(testDirPath, "subDir2");
        AbsolutePath dir3 = createSubdir(testDirPath, "subDir3");

        DirectoryStream<PathAttributesPair> dirStream = getFiles().newAttributesDirectoryStream(testDirPath);
        Iterator<PathAttributesPair> iterator = dirStream.iterator();

        int count = 0;

        while (iterator.hasNext()) {
            PathAttributesPair el = iterator.next();
            AbsolutePath path = el.path();
            URI fsUri = el.path().getFileSystem().getUri();
            infoPrintf(" -(AbsolutePath)Path     =%s:'%s'\n", fsUri, path);
            infoPrintf(" -(AbsolutePath)getPath()=%s:'%s'\n", fsUri, path.getPath());
            count++;
        }

        infoPrintf("Directory has:%d entries\n", count);
        assertEquals("Directory must have 3 sub directories\n", 3, count);

        // POST: 
        deletePaths(new AbsolutePath[] { dir1, dir2, dir3, testDirPath }, true);
    }

    // ===================================
    // Test Stream Read and Write Methods 
    // ===================================

    /**
     * Test write and read back 0 bytes.
     */
    @org.junit.Test
    public void testStreamWriteAndReadNillBytes() throws Exception {

        // empty array: 
        byte nilBytes[] = new byte[0];
        testStreamWriteAndReadBytes(nilBytes, 0);
    }

    /**
     * Test write and read back 256 bytes to a NEW file.
     */
    @org.junit.Test
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
        AbsolutePath testFilePath = createUniqueTestFile(getTestDir(), "testStreaReadWriteFile03", true);
        assertTrue("Test file doesn't exists:" + testFilePath, getFiles().exists(testFilePath));

        // TEST: 
        java.io.OutputStream outps = getFiles().newOutputStream(testFilePath, OpenOption.CREATE);

        outps.write(bytes);

        try {
            outps.close();
        } catch (IOException e) {
            debugPrintf("IOException when closing test file:%s\n", e);
        }

        InputStream inps = getFiles().newInputStream(testFilePath);

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
            debugPrintf("IOException when closing test file:%s\n", e);
        }

        // POST: 
        deletePaths(new AbsolutePath[] { testFilePath }, true);
    }

}
