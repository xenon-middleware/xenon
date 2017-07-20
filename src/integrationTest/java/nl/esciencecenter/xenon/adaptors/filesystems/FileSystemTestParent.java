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
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.OutputStream;
import java.util.Map;

import net.schmizz.sshj.sftp.FileAttributes;
import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.FileSystemAdaptorDescription;
import nl.esciencecenter.xenon.filesystems.Path;

import nl.esciencecenter.xenon.filesystems.PathAttributes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.Rule;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
public abstract class FileSystemTestParent {
    private FileSystem fileSystem;
    private FileSystemAdaptorDescription description;
    private LocationConfig locationConfig;
    private long counter = 0;
    private Path testDir;

    protected abstract Path getTestRoot();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() throws XenonException {
        fileSystem = setupFileSystem();
        description = setupDescription();
        locationConfig = setupLocationConfig(fileSystem);
        testDir = null;
    }

    protected abstract LocationConfig setupLocationConfig(FileSystem fileSystem);

    @After
    public void cleanup() throws XenonException {
        try {
            if (testDir != null && fileSystem.exists(testDir)) {
                fileSystem.delete(testDir,true);
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
        assertTrue(path.toString(), fileSystem.exists(path));
    }

    @Test
    public void readSymbolicLink_linkToExistingFile_targetMatches() throws XenonException {
        assumeTrue(description.supportsSymboliclinks());
        Map.Entry<Path, Path> linkTarget = locationConfig.getSymbolicLinksToExistingFile();
        Path target = fileSystem.readSymbolicLink(linkTarget.getKey());
        Path expectedTarget = linkTarget.getValue();
        assertEquals(target, expectedTarget);
    }


    public Path resolve(String... path) throws XenonException {
        return getTestRoot().resolve(new Path(path));
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



    // Depends on: Path.resolve, RelativePath, exists
    private Path createNewTestDirName(Path root) throws XenonException {
        Path dir = root.resolve(new Path( "dir" + counter));
        counter++;

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
        Path testDir = resolve(testName);

        assertFalse("Test directory " + testName + " already exists", fileSystem.exists(testDir));
        fileSystem.createDirectories(testDir);
    }

    // Depends on: Path.resolve, RelativePath, exists
    private Path createNewTestFileName(Path root) throws Exception {
        Path file = root.resolve(resolve( "file" + counter));
        counter++;

        assertFalse("Generated NEW test file already exists! " + file, fileSystem.exists(file));

        return file;
    }

    // Depends on: newOutputStream
    private void writeData(Path testFile, byte[] data) throws Exception {

        OutputStream out = null;

        try {
            fileSystem.writeToFile(testFile, data.length);

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

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST isOpen
    //
    // Possible parameters:
    //
    // FileSystem - null / open FS / closed FS
    //
    // Total combinations : 3
    //
    // Depends on: close, isOpen
/*
    private void test01_isOpen(boolean expected, boolean mustFail) throws Exception {
        boolean result = false;

        try {
            result = fileSystem.isOpen();
        } catch (Exception e) {
            if (mustFail) {
                // expected
                return;
            }

            throwUnexpected("test01_isOpen", e);
        }

        if (mustFail) {
            throwExpected("test01_isOpen");
        }

        if (result != expected) {
            throwWrong("test01_isOpen", expected, result);
        }
    }


    @Test
    public void test01_isOpen_openFs_true() throws Exception {
        test01_isOpen(true, false);
    }

    @Test
    public void test01_isOpen_closedFsIfSupported_false() throws Exception {
        assumeTrue(!description.isConnectionless());
        fileSystem.close();
        test01_isOpen(false, false);
    }

    @Test(expected = XenonException.class)
    public void test02_close_closedFileSystemIfSupported_throw() throws Exception {
        assumeTrue(!description.isConnectionless());
        fileSystem.close();
        fileSystem.close();
    }

*/

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: createDirectory
    //
    // Possible parameters:
    //
    // Path null / non-existing dir / existing dir / existing file / non-exising parent / closed filesystem
    //
    // Total combinations : 5
    //
    // Depends on: [getTestFileSystem], FileSystem.getEntryPath(), [createNewTestDirName], [createTestFile],
    //             createDirectory, [deleteTestDir], [deleteTestFile], [closeTestFileSystem]

    private void test04_createDirectory(Path path, boolean mustFail) throws XenonException {
        try {
            fileSystem.createDirectory(path);
        } catch (Exception e) {
            if (mustFail) {
                // expected
                return;
            }

            throwUnexpected("test04_createDirectory", e);
        }

        if (mustFail) {
            throwExpected("test04_createDirectory");
        }
    }

    @Test
    public void test04_createDirectory_null_throw() throws Exception {
        test04_createDirectory(null, true);
    }

    @Test
    public void test04_createDirectory_nonExisting_noThrow() throws Exception {
        testDir = resolve( "test04_createDirectory");

        test04_createDirectory(testDir, false);
    }

    @Test
    public void test04_createDirectory_existing_throw() throws Exception {
        testDir = resolve( "test04_createDirectory");
        fileSystem.createDirectory(testDir);

        test04_createDirectory(testDir, true);
    }

    @Test
    public void test04_createDirectory_existingFile_throw() throws Exception {
        testDir = resolve("test04_createDirectory");
        fileSystem.createDirectory(testDir);

        Path file = createTestFile(testDir, null);
        test04_createDirectory(file, true);
        deleteTestFile(file);
    }

    @Test
    public void test04_createDirectory_nonExistingParent_throw() throws Exception {
        testDir = resolve( "test04_createDirectory");
        Path parent = createNewTestDirName(testDir);
        Path dir0 = createNewTestDirName(parent);
        fileSystem.createDirectory(testDir);

        test04_createDirectory(dir0, true);
    }

    /*
    @Test
    public void test04_createDirectory_closedFileSystemIfSupported_throw() throws Exception {
        assumeTrue(!description.isConnectionless());

        Path testDir = resolve( "test04_createDirectory");
        fileSystem.close();
        test04_createDirectory(testDir, true);
    }
    */


}
