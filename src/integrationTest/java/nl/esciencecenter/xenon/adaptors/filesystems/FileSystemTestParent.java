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
package nl.esciencecenter.xenon.adaptors.filesystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assert.assertNotNull;

import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.FileSystemAdaptorDescription;
import nl.esciencecenter.xenon.filesystems.FileSystemClosedException;
import nl.esciencecenter.xenon.filesystems.NoSuchPathException;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.filesystems.PathAlreadyExistsException;
import nl.esciencecenter.xenon.filesystems.PathAttributes;

public abstract class FileSystemTestParent {
	
	public static final String TEST_DIR = "xenon_test";
    
    private Path testRoot;
    
    private FileSystem fileSystem;
    private FileSystemAdaptorDescription description;
    private LocationConfig locationConfig;
    private Path testDir;

    private static long counter = 0;
    
    private static long getNextCounter() { 
    	return counter++;
    }

    
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() throws XenonException {
        fileSystem = setupFileSystem();
        description = setupDescription();
        locationConfig = setupLocationConfig(fileSystem);
        
        Path root = locationConfig.getWritableTestDir();
        
        System.out.println("ROOT=" + root);
        
        assertNotNull(root);
        
        testRoot = root.resolve(TEST_DIR);
        
        System.out.println("TEST_ROOT=" + testRoot);
        
        fileSystem.createDirectories(testRoot);
        
        testDir = null;
    }

    protected abstract LocationConfig setupLocationConfig(FileSystem fileSystem);

    @After
    public void cleanup() throws XenonException {
        try {
            if (testRoot != null && fileSystem.exists(testRoot)) {
                fileSystem.delete(testRoot, true);
            }
        } finally {
            try {
                fileSystem.close();
            } catch (Exception ex) {
                // that's fine
            }
        }
    }

    public abstract FileSystem setupFileSystem() throws XenonException;

    private FileSystemAdaptorDescription setupDescription() throws XenonException {
        String name = fileSystem.getAdaptorName();
        return FileSystem.getAdaptorDescription(name);
    }

    @Test
    public void exists_fileDoesExist_fileExists() throws XenonException {
        Path path = locationConfig.getExistingPath();
        assertTrue(path.getAbsolutePath(), fileSystem.exists(path));
    }

    @Test
    public void readSymbolicLink_linkToExistingFile_targetMatches() throws XenonException {
    	assumeTrue("Does not support reading of symlinks", description.canReadSymboliclinks());
    	Map.Entry<Path, Path> linkTarget = locationConfig.getSymbolicLinksToExistingFile();
        Path target = fileSystem.readSymbolicLink(linkTarget.getKey());
        Path expectedTarget = linkTarget.getValue();
        assertEquals(target, expectedTarget);
    }


    public Path resolve(String... path) throws XenonException {
        return testRoot.resolve(new Path(path));
    }


    private void throwMissingElements(String name, Collection elements) {
        fail(name + " did NOT produce elements: " + elements);
    }

    private void throwUnexpected(String name, Throwable e) {
        throw new AssertionError(name + " throws unexpected Exception!", e);
    }

    private void throwExpected(String name) {
        fail(name + " did NOT throw Exception which was expected!");
    }

    private void throwWrong(String name, Object expected, Object result) {
        fail(name + " produced wrong result! Expected: " + expected + " but got: " + result);
    }

    private void throwUnexpectedElement(String name, Object element) {
        fail(name + " produced unexpected element: " + element);
    }

    private String generateTestDirName() throws XenonException {
    	return "dir" + getNextCounter();
    }

    private String generateTestFileName() throws XenonException {
    	return "file" + getNextCounter();
    }

    // Depends on: Path.resolve, RelativePath, exists
    private Path createNewTestDirName(Path root) throws XenonException {
        Path dir = resolve("dir" + getNextCounter());
        
        assertFalse("Generated test dir already exists! " + dir, fileSystem.exists(dir));

        return dir;
    }

    // Depends on: [createNewTestDirName], createDirectory, exists
    private Path createTestDir(Path root) throws Exception {
        Path dir = createNewTestDirName(root);
        fileSystem.createDirectory(dir);

        assertTrue("Failed to generate test dir! " + dir, fileSystem.exists(dir));

        return dir;
    }

    // Depends on: [createTestDir]
    protected void prepareTestDir(String testName) throws XenonException {
        testDir = resolve(testName);

        assertFalse("Test directory " + testName + " already exists", fileSystem.exists(testDir));
        fileSystem.createDirectories(testDir);
    }

    // Depends on: Path.resolve, RelativePath, exists
    private Path createNewTestFileName(Path root) throws Exception {
        Path file = resolve( "file" + getNextCounter());
        
        assertFalse("Generated NEW test file already exists! " + file, fileSystem.exists(file));

        return file;
    }

    // Depends on: newOutputStream
    private void writeData(Path testFile, byte[] data) throws Exception {

        OutputStream out = null;

        try {
        	out = fileSystem.writeToFile(testFile, data.length);

            if (data != null) {
                out.write(data);
            }
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                //ignore
            }
        }
    }

    // Depends on: [createNewTestFileName], createFile, [writeData]
    protected Path createTestFile(Path root, byte[] data) throws Exception {
        Path file = createNewTestFileName(root);

        fileSystem.createFile(file);

        if (data != null && data.length > 0) {
            writeData(file, data);
        }
        return file;
    }

    // Depends on: exists, isDirectory, delete
    private void deleteTestFile(Path file) throws Exception {
        assertTrue("Cannot delete non-existing file: " + file, fileSystem.exists(file));

        PathAttributes att = fileSystem.getAttributes(file);
        assertFalse("Cannot delete directory: " + file, att.isDirectory());

        fileSystem.delete(file,false);
    }

    // Depends on: exists, isDirectory, delete
    protected void deleteTestDir(Path dir) throws Exception {
        assertTrue("Cannot delete non-existing directory: " + dir, fileSystem.exists(dir));

        PathAttributes att = fileSystem.getAttributes(dir);
        assertTrue("Cannot delete file: " + dir, att.isDirectory());

        fileSystem.delete(dir,true);
    }

    private void generateTestDir() throws XenonException { 
    	testDir = resolve(generateTestDirName());    	
    }
    
    private void generateAndCreateTestDir() throws XenonException { 
    	generateTestDir();
    	fileSystem.createDirectories(testDir);    	
    }
    
    // Tests to create directories
    
    @Test(expected=IllegalArgumentException.class)
    public void createDirectory_null_throw() throws Exception {
    	fileSystem.createDirectory(null);
    }
    
    @Test
    public void createDirectory_nonExisting_noThrow() throws Exception {
    	generateAndCreateTestDir();
    	assertTrue(fileSystem.exists(testDir));
    	
    }

    @Test(expected=PathAlreadyExistsException.class)
    public void createDirectory_existing_throw() throws Exception {
    	generateAndCreateTestDir();
        fileSystem.createDirectory(testDir);
    }

    @Test(expected=PathAlreadyExistsException.class)
    public void createDirectory_existingFile_throw() throws Exception {
    	generateAndCreateTestDir();

    	String file = generateTestFileName();
        
        Path tmp = testDir.resolve(file);
        fileSystem.createFile(tmp);
        
        // Should fail!
        fileSystem.createDirectory(tmp);
    }

    @Test(expected=NoSuchPathException.class)
    public void createDirectory_nonExistingParent_throw() throws Exception {
    	generateAndCreateTestDir();
    	fileSystem.createDirectory(testDir.resolve(new Path("aap", "noot")));
    }
    
//    @Test(expected=XenonException.class)
//    public void createDirectory_closedFileSystemIfSupported_throw() throws Exception {
//        assumeFalse(description.isConnectionless());
//        fileSystem.close();
//        generateAndCreateTestDir();
//    }
    
    
    
/*
    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: createDirectories
    //
    // Possible parameters:
    //
    // Path null / non-existing dir / existing dir / dir with existing parents / dir with non existing parents /
    //               dir where last parent is file / closed filesystem
    //
    // Total combinations : 7
    //
    // Depends on: [getTestFileSystem], FileSystem.getEntryPath(), [createNewTestDirName], createDirectories,
    //             [deleteTestDir], [createTestFile], [deleteTestFile], [deleteTestDir], [closeTestFileSystem]

    private void test05_createDirectories(Path path, boolean mustFail) throws Exception {
        try {
            fileSystem.createDirectories(path);

            assertTrue(fileSystem.exists(path));

            PathAttributes att = fileSystem.getAttributes(path);

            assertTrue(att.isDirectory());
        } catch (Exception e) {
            if (mustFail) {
                // expected
                return;
            }
            throwUnexpected("test05_createDirectories", e);
        }

        if (mustFail) {
            throwExpected("createDirectory");
        }
    }

    @Test
    public void test05_createDirectories_null_throw() throws Exception {
        test05_createDirectories(null, true);
    }

    @Test
    public void test05_createDirectories_nonExisting_noThrow() throws Exception {
        testDir = resolve("test05_createDirectories");

        test05_createDirectories(testDir, false);
    }

    @Test
    public void test05_createDirectories_existingPath_throw() throws Exception {
        testDir = resolve( "test05_createDirectories");
        fileSystem.createDirectories(testDir);

        test05_createDirectories(testDir, true);
    }

    @Test
    public void test05_createDirectories_existingParent_noThrow() throws Exception {
        testDir = resolve("test05_createDirectories");
        Path dir0 = createNewTestDirName(testDir);

        fileSystem.createDirectories(testDir);

        test05_createDirectories(dir0, false);
        deleteTestDir(dir0);
    }

    @Test
    public void test05_createDirectories_nonExistingParents_noThrow() throws Exception {
        testDir = resolve( "test05_createDirectories");
        Path nonExistingDir = createNewTestDirName(testDir);

        // Directory with non-existing parents
        Path pathWithoutParent = createNewTestDirName(nonExistingDir);

        fileSystem.createDirectories(testDir);
        test05_createDirectories(pathWithoutParent, false);
        deleteTestDir(pathWithoutParent);
        deleteTestDir(nonExistingDir);
    }

    @Test
    public void test05_createDirectories_parentIsFile_throw() throws Exception {
        testDir = resolve("test05_createDirectories");
        fileSystem.createDirectories(testDir);

        // Directory where last parent is file
        Path file = createTestFile(testDir, null);

        Path pathWithFileParent = createNewTestDirName(file);
        test05_createDirectories(pathWithFileParent, true);

        deleteTestFile(file);
    }

//    @Test
//    public void test05_createDirectories_fileSystemClosed_throwIfSupported() throws Exception {
//        assumeTrue(!description.isConnectionless());
//        // Use root instead of testDir to prevent cleanup
//        testDir = resolve("test05_createDirectories");
//        files.close(testDir.getFileSystem());
//
//        try {
//            test05_createDirectories(testDir, true);
//        } finally {
//            // set up for cleaning again
//            cwd = config.getWorkingDir(files, credentials);
//            testDir = resolve(cwd, TEST_ROOT, "test05_createDirectories");
//        }
//    }
    
    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: createFile
    //
    // Possible parameters:
    //
    // Path null / non-existing file / existing file / existing dir / non-existing parent / closed filesystem
    //
    // Total combinations : 6
    //
    // Depends on: [getTestFileSystem], [createTestDir], [createNewTestFileName], createFile, delete, [deleteTestDir]
    //             [closeTestFileSystem]

    private void test07_createFile(Path path, boolean mustFail) throws Exception {
        try {
            fileSystem.createFile(path);
        } catch (Exception e) {
            if (mustFail) {
                // expected
                return;
            }

            throwUnexpected("test07_createFile", e);
        }

        if (mustFail) {
            throwExpected("test07_createFile");
        }
    }

    @Test
    public void test07_createFile_nullFile_throw() throws Exception {
        prepareTestDir("test07_createFile");

        test07_createFile(null, true);
    }

    @Test
    public void test07_createFile_nonExistingFile_noThrow() throws Exception {
        prepareTestDir("test07_createFile");
        Path file0 = createNewTestFileName(testDir);

        test07_createFile(file0, false);
        deleteTestFile(file0);
    }

    @Test
    public void test07_createFile_existingFile_throw() throws Exception {
        prepareTestDir("test07_createFile");
        Path existingFile = createNewTestFileName(testDir);

        fileSystem.createFile(existingFile);
        test07_createFile(existingFile, true);

        deleteTestFile(existingFile);
    }

    @Test
    public void test07_createFile_existingDir_throw() throws Exception {
        prepareTestDir("test07_createFile");

        test07_createFile(testDir, true);
    }

    @Test
    public void test07_createFile_nonExistentParent_throw() throws Exception {
        prepareTestDir("test07_createFile");
        Path nonExistingDir = createNewTestDirName(testDir);
        Path pathWithoutParent = createNewTestFileName(nonExistingDir);

        test07_createFile(pathWithoutParent, true);
    }

    @Test
    public void test07_createFile_closedFileSystem_throwIfSupported() throws Exception {
    	assumeTrue(!description.isConnectionless());

        prepareTestDir("test07_createFile");
        Path file0 = createNewTestFileName(testDir);
        fileSystem.close();

        try {
            test07_createFile(file0, true);
        } finally {
            // prepare for removal in cleanup
            testDir = resolve("test07_createFile");
        }
    }
    

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: exists
    //
    // Possible parameters:
    //
    // Path null / non-existing file / existing file
    //
    // Total combinations : 3
    //
    // Depends on: [getTestFileSystem], [createTestDir], [createNewTestFileName], [createTestFile], [deleteTestFile],
    //             [closeTestFileSystem], exists

    private void test08_exists(Path path, boolean expected, boolean mustFail) throws Exception {
        boolean result = false;

        try {
            result = fileSystem.exists(path);
        } catch (Exception e) {
            if (mustFail) {
                // expected
                return;
            }

            throwUnexpected("test08_exists", e);
        }

        if (mustFail) {
            throwExpected("test08_exists");
        }

        if (result != expected) {
            throwWrong("test08_exists", expected, result);
        }
    }

    @Test
    public void test08_exists_nullFile_throw() throws Exception {
        prepareTestDir("test08_exists");

        // test with null
        test08_exists(null, false, true);
    }

    @Test
    public void test08_exists_nonExistent_returnFalse() throws Exception {
        prepareTestDir("test08_exists");

        // test with non-existing file
        Path file0 = createNewTestFileName(testDir);
        test08_exists(file0, false, false);
    }

    @Test
    public void test08_exists_existingFile_returnTrue() throws Exception {
        prepareTestDir("test08_exists");

        // test with existing file
        Path file1 = createTestFile(testDir, null);
        test08_exists(file1, true, false);
        deleteTestFile(file1);
    }


    // TODO: Test recursive delete!
    
    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: delete
    //
    // Possible parameters:
    //
    // Path null / non-existing file / existing file / existing empty dir / existing non-empty dir /
    //              existing non-writable file / closed filesystem
    //
    // Total combinations : 7
    //
    // Depends on: [getTestFileSystem], [createTestDir], [createNewTestFileName], delete, [deleteTestFile], [deleteTestDir]
    //             [closeTestFileSystem]

    private void test09_delete(Path path, boolean mustFail) throws Exception {
        try {
            fileSystem.delete(path,false);
        } catch (Exception e) {
            if (mustFail) {
                // expected
                return;
            }

            throwUnexpected("test09_delete", e);
        }

        if (fileSystem.exists(path)) {
            throwWrong("test09_delete", "no file", "a file");
        }

        if (mustFail) {
            throwExpected("test09_delete");
        }
    }

    @Test
    public void test09_delete_null_throw() throws Exception {
        test09_delete(null, true);
    }

    @Test
    public void test09_delete_unexistingFile_throw() throws Exception {
        prepareTestDir("test09_delete");
        Path unexistingFile = createNewTestFileName(testDir);

        test09_delete(unexistingFile, true);
    }

    @Test
    public void test09_delete_existingFile_noThrow() throws Exception {
        prepareTestDir("test09_delete");
        Path existingFile = createTestFile(testDir, null);

        test09_delete(existingFile, false);
    }

    @Test
    public void test09_delete_existingEmptyDir_noThrow() throws Exception {
        prepareTestDir("test09_delete");
        Path existingEmptyDir = createTestDir(testDir);

        test09_delete(existingEmptyDir, false);
    }

    @Test
    public void test09_delete_existingNonEmptyDir_throw() throws Exception {
        prepareTestDir("test09_delete");
        Path nonEmptyDir = createTestDir(testDir);
        Path file = createTestFile(nonEmptyDir, null);

        test09_delete(nonEmptyDir, true);

        deleteTestFile(file);
        deleteTestDir(nonEmptyDir);
    }

    @Test
    public void test09_delete_closedFileSystem_throwIfSupported() throws Exception {
    	assumeTrue(!description.isConnectionless());
        prepareTestDir("test09_delete");

        fileSystem.close();

        try {
        	fileSystem.delete(testDir,false);
        } catch (FileSystemClosedException e) {
            // This is the expected exception
        } catch (Exception e) {
            // We do not expect another exception
            throwUnexpected("test09_delete", e);
        } finally {
            // set up for cleaning again
            testDir = resolve( "test09_delete");
        }
    }

    
    // TODO: Test recursive listing
    
    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: list
    //
    // Possible parameters:
    //
    // Path null / non-existing dir / existing empty dir / existing non-empty dir / existing dir with subdirs /
    //              existing file / closed filesystem
    //
    // Total combinations : 7
    //
    // Depends on: [getTestFileSystem], [createTestDir], [createNewTestDirName], [createTestFile], newDirectoryStream,
    //             [deleteTestDir], , [deleteTestFile], [deleteTestDir], [closeTestFileSystem]

    private void test15_list(Path root, Set<PathAttributes> expected, boolean mustFail)
            throws Exception {

        Set<PathAttributes> tmp;

        if (expected != null) {
            tmp = new HashSet<>(expected);
        } else {
            tmp = new HashSet<>(0);
        }

        Iterable<PathAttributes> in = null;

        try {
            in = fileSystem.list(root, false);
        } catch (Exception e) {
            if (mustFail) {
                // expected
                return;
            }

            throwUnexpected("test15_list", e);
        }

        if (mustFail) {
            throwExpected("test15_list");
        }

        logger.debug("Comparing PathAttributesPairs:");

        for (PathAttributes p : in) {
            logger.debug("Got input " + p);

            PathAttributes found = null;

            for (PathAttributes x : tmp) {
                logger.debug("  Comparing to " + x.getPath() );

                if (p.equals(x)) {
                    logger.debug("Found!");
                    found = x;
                    break;
                }
            }

            logger.debug("  Found = " + found);

            if (found != null) {
                tmp.remove(found);
            } else {
                logger.debug("NOT Found!");
                throwUnexpectedElement("test15_list", p.getPath());
            }
        }

        if (tmp.size() > 0) {
            throwMissingElements("test15_list", tmp);
        }
    }

    @Test
    public void test15_list_nullPath_throw() throws Exception {
        test15_list(null, null, true);
    }

    @Test
    public void test15_list_nonExistingDir_throw() throws Exception {
        prepareTestDir("test15_list");
        Path nonExistingDir = createNewTestDirName(testDir);

        test15_list(nonExistingDir, null, true);
    }

    @Test
    public void test15_list_existingDir_noThrow() throws Exception {
        prepareTestDir("test15_list");

        test15_list(testDir, null, false);
    }

    @Test
    public void test15_list_existingFile_noThrow() throws Exception {
        prepareTestDir("test15_list");
        test15_list(testDir, null, false);
        Path existingFile = createTestFile(testDir, null);

        test15_list(existingFile, null, true);

        // cleanup
        deleteTestFile(existingFile);
    }

    @Test
    public void test15_list_nonEmptyDir_correctListing() throws Exception {
        prepareTestDir("test15_list");
        Path file0 = createTestFile(testDir, null);
        Path file1 = createTestFile(testDir, null);
        Path file2 = createTestFile(testDir, null);
        Path file3 = createTestFile(testDir, null);
        Set<PathAttributes> result = new HashSet<>(6);
        result.add(fileSystem.getAttributes(file0));
        result.add(fileSystem.getAttributes(file1));
        result.add(fileSystem.getAttributes(file2));
        result.add(fileSystem.getAttributes(file3));

        test15_list(testDir, result, false);

        // cleanup
        deleteTestFile(file3);
        deleteTestFile(file2);
        deleteTestFile(file1);
        deleteTestFile(file0);
    }

    @Test
    public void test15_list_withSubDirs_onlyListTopDirContents() throws Exception {
        prepareTestDir("test15_list");
        Path file0 = createTestFile(testDir, null);
        Path file1 = createTestFile(testDir, null);
        Path file2 = createTestFile(testDir, null);
        Path file3 = createTestFile(testDir, null);
        Path subDir = createTestDir(testDir);
        Path irrelevantFileInSubDir = createTestFile(subDir, null);
        Set<PathAttributes> result = new HashSet<>(7);
        result.add(fileSystem.getAttributes(file0));
        result.add(fileSystem.getAttributes(file1));
        result.add(fileSystem.getAttributes(file2));
        result.add(fileSystem.getAttributes(file3));
        result.add(fileSystem.getAttributes(subDir));  

        test15_list(testDir, result, false);

        // cleanup
        deleteTestFile(irrelevantFileInSubDir);
        deleteTestDir(subDir);
        deleteTestFile(file3);
        deleteTestFile(file2);
        deleteTestFile(file1);
        deleteTestFile(file0);
    }

    @Test
    public void test15_list_closedFileSystem_throw_IfSupported() throws Exception {
    	assumeTrue(!description.isConnectionless());
        prepareTestDir("test15_list");
        fileSystem.close();

        try {
            test15_list(testDir, null, true);
        } finally {
            // set up for cleaning again
            testDir = resolve("test15_list");
        }
    }
    

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: getAttributes
    //
    // Possible parameters:
    //
    // Path null / non-existing file / existing empty file / existing non-empty file / existing dir / existing link (!)
    //              closed filesystem
    //
    // Total combinations : 7
    //
    // Depends on: [getTestFileSystem], FileSystem.getEntryPath(), [createNewTestDirName], createDirectories,
    //             [deleteTestDir], [createTestFile], [deleteTestFile], [deleteTestDir], [closeTestFileSystem]

    private void test13_getAttributes(Path path, boolean isDirectory, long size, long currentTime, boolean mustFail)
            throws Exception {

        PathAttributes result = null;

        try {
            result = fileSystem.getAttributes(path);
        } catch (Exception e) {
            if (mustFail) {
                // expected
                return;
            }

            throwUnexpected("test13_getFileAttributes", e);
        }

        if (mustFail) {
            throwExpected("test13_getFileAttributes");
        }

        if (result.isDirectory() && !isDirectory) {
            throwWrong("test13_getfileAttributes", "<not directory>", "<directory>");
        }

        if (size >= 0 && result.getSize() != size) {
            throwWrong("test13_getfileAttributes", "size=" + size, "size=" + result.getSize());
        }

        if (!isWithinMargin(currentTime, result.getLastModifiedTime())) {
            throwWrong("test13_getfileAttributes", "lastModifiedTime=" + currentTime,
                    "lastModifiedTime=" + result.getLastModifiedTime());
        }

        if (!isWithinMargin(currentTime, result.getCreationTime())) {
            throwWrong("test13_getfileAttributes", "creationTime=" + currentTime, "creationTime=" + result.getCreationTime());
        }

        if (!isWithinMargin(currentTime, result.getLastAccessTime())) {
            throwWrong("test13_getfileAttributes", "lastAccessTime=" + currentTime, "lastAccessTime=" + result.getLastAccessTime());
        }

    }
*/
    
    /**
     * Tests whether two times (in milliseconds) are within a mild margin of one another. The margin is large enough to be able to
     * cope with servers in other timezones and similar, expected, sources of discrepancy between times.
     *
     * @param time1
     * @param time2
     * @return
     */
    private boolean isWithinMargin(long time1, long time2) {
        final int millisecondsPerSecond = 1000;
        final int secondsPerHour = 3600;
        final long margin = 30 * secondsPerHour * millisecondsPerSecond;
        return Math.abs(time1 - time2) < margin;
    }
/*
    @Test
    public void test13_getAttributes_nullPath_throw() throws Exception {
        long currentTime = System.currentTimeMillis();
        test13_getAttributes(null, false, -1, currentTime, true);
    }

    @Test
    public void test13_getAttributes_nonExistingFile_throw() throws Exception {
        long currentTime = System.currentTimeMillis();
        prepareTestDir("test13_getAttributes");
        Path nonExistingFile = createNewTestFileName(testDir);

        test13_getAttributes(nonExistingFile, false, -1, currentTime, true);
    }

    @Test
    public void test13_getAttributes_emptyFile_noThrow() throws Exception {
        long currentTime = System.currentTimeMillis();
        prepareTestDir("test13_getAttributes");
        Path emptyFile = createTestFile(testDir, null);

        test13_getAttributes(emptyFile, false, 0, currentTime, false);
        deleteTestFile(emptyFile);
    }

    @Test
    public void test13_getAttributes_nonEmptyFile_noThrow() throws Exception {
        long currentTime = System.currentTimeMillis();
        prepareTestDir("test13_getAttributes");
        Path nonEmptyFile = createTestFile(testDir, new byte[] { 1, 2, 3 });

        test13_getAttributes(nonEmptyFile, false, 3, currentTime, false);
        deleteTestFile(nonEmptyFile);
    }

    @Test
    public void test13_getAttributes_existingDir_noThrow() throws Exception {
        long currentTime = System.currentTimeMillis();
        prepareTestDir("test13_getAttributes");
        Path existingDir = createTestDir(testDir);

        test13_getAttributes(existingDir, true, -1, currentTime, false);
        deleteTestDir(existingDir);
    }

    @Test
    public void test13_getAttributes_closedFileSystem_throwIfSupported() throws Exception {
    	assumeTrue(!description.isConnectionless());
        long currentTime = System.currentTimeMillis();
        prepareTestDir("test13_getAttributes");

        fileSystem.close();
        try {
            test13_getAttributes(testDir, false, -1, currentTime, true);
        } finally {
            // set up for cleaning again
            testDir = resolve( "test13_getAttributes");
        }
    }
*/
}
