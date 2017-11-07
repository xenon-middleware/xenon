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
package nl.esciencecenter.xenon.adaptors.filesystems;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.filesystems.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public abstract class FileSystemTestInfrastructure {


    public static final String TEST_DIR = "xenon_test";

    protected Path testRoot;

    protected FileSystem fileSystem;
    protected FileSystemAdaptorDescription description;
    protected LocationConfig locationConfig;
    protected Path testDir;

    protected static long counter = 0;

    protected static long getNextCounter() {
        return counter++;
    }

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 60 seconds max per
    // method tested

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() throws XenonException {
        fileSystem = setupFileSystem();
        description = setupDescription();
        locationConfig = setupLocationConfig(fileSystem);

        Path root = locationConfig.getWritableTestDir();

        // System.out.println("ROOT=" + root);

        assertNotNull(root);

        testRoot = root.resolve(TEST_DIR);

        assertNotNull(testRoot);

        // System.out.println("TEST_ROOT=" + testRoot);

        if (fileSystem.exists(testRoot)) {
            fileSystem.delete(testRoot, true);
        }

        fileSystem.createDirectory(testRoot);

        testDir = null;
    }

    protected abstract LocationConfig setupLocationConfig(FileSystem fileSystem);

    @After
    public void cleanup() throws XenonException {
        FileSystem cleanFileSystem = null;
        try {
            // close the file system under test, so any copy operations still running are killed, before we clean the test root
            //  if (fileSystem.isOpen()) {
            // fileSystem.close();
            // }

            cleanFileSystem = setupFileSystem();

            if (testRoot != null && cleanFileSystem.exists(testRoot)) {
                cleanFileSystem.delete(testRoot, true);
            }
        } finally {
            try {
                if (cleanFileSystem != null) {
                    cleanFileSystem.close();
                }
            } catch (Exception ex) {
                // that's fine
            }
        }
    }

    public abstract FileSystem setupFileSystem() throws XenonException;

    protected FileSystemAdaptorDescription setupDescription() throws XenonException {
        String name = fileSystem.getAdaptorName();
        return FileSystem.getAdaptorDescription(name);
    }


    public Path resolve(String path) throws XenonException {
        return testRoot.resolve(new Path(path));
    }

    protected void copySync(Path source, Path target, CopyMode mode, boolean recursive) throws Throwable {
        String s = fileSystem.copy(source, fileSystem, target, mode, recursive);
        CopyStatus status = fileSystem.waitUntilDone(s, 1000);

        // For some adaptors (like webdav) it may take a few moments for the
        // copy to fully arrive at the server.
        // To prevent the next operation from overtaking this copy, we sleep for
        // a second to let the target settle.
        Thread.sleep(1000);

        if (status.hasException()) {
            throw status.getException();
        }
    }

    protected void throwUnexpected(String name, Throwable e) {
        throw new AssertionError(name + " throws unexpected Exception!", e);
    }

    protected void throwWrong(String name, Object expected, Object result) {
        fail(name + " produced wrong result! Expected: " + expected + " but got: " + result);
    }

    protected String generateTestDirName() throws XenonException {
        return "dir" + getNextCounter();
    }

    protected String generateTestFileName() throws XenonException {
        return "file" + getNextCounter();
    }

    // Depends on: Path.resolve, RelativePath, exists
    // protected Path createNewTestDirName(Path root) throws XenonException {
    // Path dir = resolve("dir" + getNextCounter());
    //
    // assertFalse("Generated test dir already exists! " + dir,
    // fileSystem.exists(dir));
    //
    // return dir;
    // }

    // Depends on: [createNewTestDirName], createDirectory, exists
    // protected Path createTestDir(Path root) throws Exception {
    // Path dir = createNewTestDirName(root);
    // fileSystem.createDirectory(dir);
    //
    // assertTrue("Failed to generate test dir! " + dir,
    // fileSystem.exists(dir));
    //
    // return dir;
    // }

    // Depends on: [createTestDir]
    protected void prepareTestDir(String testName) throws XenonException {
        testDir = resolve(testName);

        assertFalse("Test directory " + testName + " already exists", fileSystem.exists(testDir));
        fileSystem.createDirectories(testDir);
    }

    // Depends on: Path.resolve, exists
    protected Path createNewTestFileName(Path root) throws Exception {
        Path file = root.resolve("file" + getNextCounter());

        assertFalse("Generated NEW test file already exists! " + file, fileSystem.exists(file));

        return file;
    }

    protected void ensureUpToDate(Path testFile, byte[] data, long maxTimeout) throws Exception {

        long deadline = System.currentTimeMillis() + maxTimeout;

        while (!fileSystem.exists(testFile)) {
            if (System.currentTimeMillis() > deadline) {
                fail("Failed to ensure file " + testFile + " exists after " + maxTimeout + " ms. ");
            }

            Thread.sleep(500);
        }

        if (data != null && data.length > 0) {

            PathAttributes att = fileSystem.getAttributes(testFile);

            while (att.getSize() != data.length) {
                // System.out.println("Wrong size " + att.getSize() + " != " +
                // data.length);

                if (System.currentTimeMillis() > deadline) {
                    fail("Failed to ensure file " + testFile + " contains " + data.length + " bytes after " + maxTimeout + " ms. ");
                }

                Thread.sleep(500);
                att = fileSystem.getAttributes(testFile);
            }
        }
    }

    protected void writeData(Path testFile, byte[] data) throws Exception {

        OutputStream out = fileSystem.writeToFile(testFile, data.length);

        if (data != null) {
            out.write(data);
        }

        out.close();

        // Note: it may take some time for the data to become available on the
        // server side. This may lead to problems
        // if any subsequent commands (like exists or readFromFile) overtaking
        // this write on the network. Therefore we
        // wait for the remote file to be updated.
        ensureUpToDate(testFile, data, 5000);
    }

    // Depends on: [createNewTestFileName], createFile, [writeData]
    protected Path createTestFile(Path root, byte[] data) throws Exception {
        Path file = createNewTestFileName(root);
        return createNamedTestFile(file, data);
    }

    protected Path createNamedTestFile(Path file, byte[] data) throws Exception {

        if (data != null && data.length > 0) {
            writeData(file, data);
        } else {
            fileSystem.createFile(file);
        }
        return file;
    }

    // // Depends on: exists, isDirectory, delete
    // protected void deleteTestFile(Path file) throws Exception {
    // assertTrue("Cannot delete non-existing file: " + file,
    // fileSystem.exists(file));
    //
    // PathAttributes att = fileSystem.getAttributes(file);
    // assertFalse("Cannot delete directory: " + file, att.isDirectory());
    //
    // fileSystem.delete(file,false);
    // }

    // Depends on: exists, isDirectory, delete
    protected void deleteTestDir(Path dir) throws Exception {
        assertTrue("Cannot delete non-existing directory: " + dir, fileSystem.exists(dir));

        PathAttributes att = fileSystem.getAttributes(dir);
        assertTrue("Cannot delete file: " + dir, att.isDirectory());

        fileSystem.delete(dir, true);
    }

    protected void generateTestDir() throws XenonException {
        testDir = resolve(generateTestDirName());
    }

    protected void generateAndCreateTestDir() throws XenonException {
        generateTestDir();
        fileSystem.createDirectories(testDir);
    }

    protected Path createTestSubDir(Path testDir) throws XenonException {
        Path res = createTestSubDirName(testDir);
        fileSystem.createDirectory(res);
        return res;
    }

    protected Path createTestSubDirName(Path testDir) throws XenonException {
        return testDir.resolve(new Path(generateTestDirName()));
    }


    protected Set<PathAttributes> listSet(Path dir, boolean recursive) throws XenonException {
        Set<PathAttributes> res = new HashSet<>();
        for (PathAttributes p : fileSystem.list(dir, recursive)) {
            if (res.contains(p)) {
                throw new XenonException(fileSystem.getAdaptorName(), "Duplicate element in listing!");
            } else {
                res.add(p);
            }
        }
        return res;
    }

    protected void assertListSetEqual(Set<PathAttributes> res, Set<PathAttributes> expected) {
        if (!res.equals(expected)) {
            Set<PathAttributes> superfluous = new HashSet<>(res);
            superfluous.removeAll(expected);
            Set<PathAttributes> missing = new HashSet<>(expected);
            missing.removeAll(res);
            String superfluousString;
            String missingString;
            if (!superfluous.isEmpty()) {
                superfluousString = "Superfluous elements : " + listPathsInString(res);
            } else {
                superfluousString = "";
            }
            if (!missing.isEmpty()) {
                missingString = "Missing elements : " + listPathsInString(missing);
            } else {
                missingString = "";
            }
            fail("listing is not as expected! " + superfluousString + " " + missingString);
        }
    }

    protected String listPathsInString(Set<PathAttributes> res) {
        String superfluous = "";
        for (PathAttributes p : res) {
            superfluous += p.getPath().toString() + " ";
        }
        return superfluous;
    }


    /**
     * Read all bytes from the input stream and return them in a byte array.
     * <p>
     * NOTE: <code>in</code> will NOT be explicitly closed once the end of the stream is reached.
     * </p>
     *
     * @param in
     *            the input stream to read.
     * @return a byte array containing all bytes that the input stream produced.
     * @throws IOException
     *             if an I/O error was produced while reading the stream.
     */
    public static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        copy(in, buffer);

        return buffer.toByteArray();
    }

    protected void assertReadsExpected(Path file, byte[] expected) throws Exception {

        InputStream in = fileSystem.readFromFile(file);

        byte[] data = readAllBytes(in);
        in.close();

        if (expected == null) {
            if (data.length != 0) {
                throwWrong("readFromFile", "zero bytes", data.length + " bytes");
            }
            return;
        }

        if (expected.length != data.length) {
            throwWrong("readFromFile", expected.length + " bytes", data.length + " bytes");
        }

        if (!Arrays.equals(expected, data)) {
            throwWrong("readFromFile", Arrays.toString(expected), Arrays.toString(data));
        }
    }

    protected void assertContents(Path file, byte[] data) throws Exception {
        InputStream a = fileSystem.readFromFile(file);
        byte[] abytes = readAllBytes(a);
        a.close();

        if (!Arrays.equals(abytes, data)) {
            throwWrong("copy", Arrays.toString(abytes), Arrays.toString(data));
        }
    }

    protected void assertSameContents(Path source, Path target) throws Exception {

        InputStream a = fileSystem.readFromFile(source);
        InputStream b = fileSystem.readFromFile(target);

        byte[] abytes = readAllBytes(a);
        a.close();

        byte[] bbytes = readAllBytes(b);
        b.close();

        if (!Arrays.equals(abytes, bbytes)) {
            throwWrong("copy", Arrays.toString(abytes), Arrays.toString(bbytes));
        }
    }



    protected void assertSameContentsDir(Path dir1, Path dir2) throws Exception {
        for (PathAttributes p : fileSystem.list(dir1, true)) {
            Path sub = dir1.relativize(p.getPath());
            Path other = dir2.resolve(sub);
            if (!fileSystem.exists(other)) {
                fail("Cannot find equivalent for " + p.getPath().toString() + " is not " + other.toString());
            }
            if (p.isRegular()) {
                assertSameContents(p.getPath(), other);
            } else if (p.isDirectory()) {
                assert (fileSystem.getAttributes(other).isDirectory());
            }
        }

    }

    /**
     * Copy all bytes from an input stream to an output stream.
     *
     * A temporary buffer of size <code>100</code> is used.
     * <p>
     * NOTE: <code>in</code> and <code>out</code> will NOT be explicitly closed once the end of the stream is reached.
     * </p>
     *
     * @param in
     *            the InputStream to read from.
     * @param out
     *            the OutputStream to write to.
     * @return the number of bytes copied.
     * @throws IOException
     *             if an I/O error occurs during the copy operation.
     */
    public static long copy(InputStream in, OutputStream out) throws IOException {
        long bytes = 0;

        byte[] buffer = new byte[100];

        int len = in.read(buffer);

        while (len != -1) {
            bytes += len;
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }

        return bytes;
    }



}
