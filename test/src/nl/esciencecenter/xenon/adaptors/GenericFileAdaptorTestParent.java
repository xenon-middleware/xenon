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

package nl.esciencecenter.xenon.adaptors;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.engine.files.PathAttributesPairImplementation;
import nl.esciencecenter.xenon.files.Copy;
import nl.esciencecenter.xenon.files.CopyOption;
import nl.esciencecenter.xenon.files.CopyStatus;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.OpenOption;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.PathAttributesPair;
import nl.esciencecenter.xenon.files.PosixFilePermission;
import nl.esciencecenter.xenon.files.RelativePath;
import nl.esciencecenter.xenon.util.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class GenericFileAdaptorTestParent {

    private static final Logger logger = LoggerFactory.getLogger(GenericFileAdaptorTestParent.class);

    protected static String TEST_ROOT;

    public static FileTestConfig config;

    protected Xenon xenon;
    protected Files files;
    protected Credentials credentials;

    protected Path testDir;

    private long counter = 0;

    @Rule
    public TestWatcher watcher = new TestWatcher() {

        @Override
        public void starting(Description description) {
            logger.info("Running test {}", description.getMethodName());
        }

        @Override
        public void failed(Throwable reason, Description description) {
            logger.info("Test {} failed due to exception", description.getMethodName(), reason);
        }

        @Override
        public void succeeded(Description description) {
            logger.info("Test {} succeeded", description.getMethodName());
        }

        @Override
        public void skipped(AssumptionViolatedException reason, Description description) {
            logger.info("Test {} skipped due to failed assumption", description.getMethodName(), reason);
        }

    };

    // MUST be invoked by a @BeforeClass method of the subclass!
    public static void prepareClass(FileTestConfig testConfig) throws Exception {
        config = testConfig;
        TEST_ROOT = "xenon_test_" + config.getAdaptorName() + "_" + System.currentTimeMillis();
    }

    // MUST be invoked by a @AfterClass method of the subclass!
    public static void cleanupClass() throws Exception {

        System.err.println("GenericFileAdaptorTest.cleanupClass() attempting to remove: " + TEST_ROOT);

        Xenon xenon = XenonFactory.newXenon(null);

        Files files = xenon.files();
        Credentials credentials = xenon.credentials();

        Path p = config.getWorkingDir(files, credentials);
        Path root = files.newPath(p.getFileSystem(), p.getRelativePath().resolve(TEST_ROOT));

        if (files.exists(root)) {
            files.delete(root);
        }

        XenonFactory.endXenon(xenon);
    }

    public Path resolve(Path root, String... path) throws XenonException {
        return files.newPath(root.getFileSystem(), root.getRelativePath().resolve(new RelativePath(path)));
    }

    @Before
    public void prepare() throws Exception {
        xenon = XenonFactory.newXenon(null);
        files = xenon.files();
        credentials = xenon.credentials();
    }

    @After
    public void cleanup() throws Exception {
        XenonFactory.endXenon(xenon);
        files = null;
        xenon = null;
    }

    // Various util functions ------------------------------------------------------------

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

    class Select implements DirectoryStream.Filter {

        private Set<Path> set;

        public Select(Set<Path> set) {
            this.set = set;
        }

        @Override
        public boolean accept(Path entry) {
            return set.contains(entry);
        }
    }

    private void throwUnexpected(String name, Exception e) throws Exception {

        throw new Exception(name + " throws unexpected Exception!", e);
    }

    private void throwExpected(String name) throws Exception {

        throw new Exception(name + " did NOT throw Exception which was expected!");
    }

    private void throwWrong(String name, Object expected, Object result) throws Exception {

        throw new Exception(name + " produced wrong result! Expected: " + expected + " but got: " + result);
    }

    private void throwUnexpectedElement(String name, Object element) throws Exception {

        throw new Exception(name + " produced unexpected element: " + element);
    }

    //    private void throwMissingElement(String name, String element) throws Exception {
    //
    //        throw new Exception(name + " did NOT produce element: " + element);
    //    }

    private void throwMissingElements(String name, Collection elements) throws Exception {

        throw new Exception(name + " did NOT produce elements: " + elements);
    }

    private void close(Closeable c) {

        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (Exception e) {
            // ignore
        }
    }

    // Depends on: Path.resolve, RelativePath, exists
    private Path createNewTestDirName(Path root) throws Exception {

        Path dir = resolve(root, "dir" + counter);
        counter++;

        if (files.exists(dir)) {
            throw new Exception("Generated test dir already exists! " + dir);
        }

        return dir;
    }

    // Depends on: [createNewTestDirName], createDirectory, exists
    private Path createTestDir(Path root) throws Exception {

        Path dir = createNewTestDirName(root);

        files.createDirectory(dir);

        if (!files.exists(dir)) {
            throw new Exception("Failed to generate test dir! " + dir);
        }

        return dir;
    }

    // Depends on: [createTestDir]
    protected void prepareTestDir(String testName) throws Exception {

        Path p = config.getWorkingDir(files, credentials);

        if (testDir != null) {
            return;
        }

        testDir = resolve(p, TEST_ROOT, testName);

        if (!files.exists(testDir)) {
            files.createDirectories(testDir);
        }
    }

    // Depends on: [createTestDir]
    private void closeTestFS() throws Exception {

        if (testDir == null) {
            return;
        }

        files.close(testDir.getFileSystem());
        testDir = null;
    }

    // Depends on: Path.resolve, RelativePath, exists
    private Path createNewTestFileName(Path root) throws Exception {

        Path file = resolve(root, "file" + counter);
        counter++;

        if (files.exists(file)) {
            throw new Exception("Generated NEW test file already exists! " + file);
        }

        return file;
    }

    // Depends on: newOutputStream
    private void writeData(Path testFile, byte[] data) throws Exception {

        OutputStream out = files.newOutputStream(testFile, OpenOption.OPEN, OpenOption.TRUNCATE, OpenOption.WRITE);
        if (data != null) {
            out.write(data);
        }
        out.close();
    }

    // Depends on: [createNewTestFileName], createFile, [writeData]
    protected Path createTestFile(Path root, byte[] data) throws Exception {

        Path file = createNewTestFileName(root);

        files.createFile(file);
        if (data != null && data.length > 0) {
            writeData(file, data);
        }
        return file;
    }

    // Depends on: exists, isDirectory, delete
    private void deleteTestFile(Path file) throws Exception {

        if (!files.exists(file)) {
            throw new Exception("Cannot delete non-existing file: " + file);
        }

        FileAttributes att = files.getAttributes(file);

        if (att.isDirectory()) {
            throw new Exception("Cannot delete directory: " + file);
        }

        files.delete(file);
    }

    // Depends on: exists, isDirectory, delete
    protected void deleteTestDir(Path dir) throws Exception {

        if (!files.exists(dir)) {
            throw new Exception("Cannot delete non-existing dir: " + dir);
        }

        FileAttributes att = files.getAttributes(dir);

        if (!att.isDirectory()) {
            throw new Exception("Cannot delete file: " + dir);
        }

        files.delete(dir);
    }

    private byte[] readFully(InputStream in) throws Exception {

        byte[] buffer = new byte[1024];

        int offset = 0;
        int read = in.read(buffer, offset, buffer.length - offset);

        while (read != -1) {

            offset += read;

            if (offset == buffer.length) {
                buffer = Arrays.copyOf(buffer, buffer.length * 2);
            }

            read = in.read(buffer, offset, buffer.length - offset);
        }

        close(in);

        return Arrays.copyOf(buffer, offset);
    }

    //    private byte [] readFully(SeekableByteChannel channel) throws Exception {
    //
    //        ByteBuffer buffer = ByteBuffer.allocate(1024);
    //
    //        int read = channel.read(buffer);
    //
    //        while (read != -1) {
    //
    //            System.err.println("READ from channel " + read);
    //
    //            if (buffer.position() == buffer.limit()) {
    //                ByteBuffer tmp = ByteBuffer.allocate(buffer.limit()*2);
    //                buffer.flip();
    //                tmp.put(buffer);
    //                buffer = tmp;
    //            }
    //
    //            read = channel.read(buffer);
    //        }
    //
    //        close(channel);
    //
    //        buffer.flip();
    //        byte [] tmp = new byte[buffer.remaining()];
    //        buffer.get(tmp);
    //
    //        System.err.println("Returning byte[" + tmp.length + "]");
    //
    //        return tmp;
    //    }

    // The test start here.

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST newFileSystem
    //
    // Possible parameters:
    //   URI         - correct URI / wrong user / wrong location / wrong path
    //   Credentials - default / null / value
    //   Properties  - null / empty / set right / set wrong
    //
    // Total combinations: 4 + 2 + 3 = 9
    //
    // Depends on: newFileSystem, close

    private void test00_newFileSystem(String scheme, String location, Credential c, Map<String, String> p, boolean mustFail)
            throws Exception {

        try {
            FileSystem fs = files.newFileSystem(scheme, location, c, p);
            files.close(fs);
        } catch (Exception e) {
            if (mustFail) {
                // exception was expected.
                return;
            }

            // exception was not expected
            throwUnexpected("test00_newFileSystem", e);
        }

        if (mustFail) {
            // expected an exception!
            throwExpected("test00_newFileSystem");
        }
    }

    @org.junit.Test
    public void test00_newFileSystem_nullUriAndCredentials_shouldThrow() throws Exception {
        test00_newFileSystem(null, null, null, null, true);
    }

    @org.junit.Test
    public void test00_newFileSystem_nullCredentials_shouldThrow() throws Exception {
        test00_newFileSystem(config.getScheme(), null, null, null, true);
    }

    @org.junit.Test
    public void test00_newFileSystem_nullProperties_throwIfApplicable() throws Exception {
        // test with correct URI without credential and without properties
        boolean allowNull = config.supportNullCredential();
        test00_newFileSystem(config.getScheme(), config.getCorrectLocation(), null, null, !allowNull);
    }

    @org.junit.Test
    public void test00_newFileSystem_correctArguments_noThrow() throws Exception {
        // test with correct scheme with, correct location, location
        test00_newFileSystem(config.getScheme(), config.getCorrectLocation(), config.getDefaultCredential(credentials), null,
                false);
    }

    @org.junit.Test
    public void test00_newFileSystem_wrongLocation_throw() throws Exception {
        // test with correct scheme with, wrong location
        test00_newFileSystem(config.getScheme(), config.getWrongLocation(), config.getDefaultCredential(credentials), null, true);
    }

    @org.junit.Test
    public void test00_newFileSystem_userInUriIfSupported_noThrow() throws Exception {
        if (!config.supportUserInUri()) {
            return;
        }

        String uriWithUsername = config.getCorrectLocationWithUser();
        test00_newFileSystem(config.getScheme(), uriWithUsername, null, null, false);
    }

    @org.junit.Test
    public void test00_newFileSystem_wrongUserInUriIfSupported_noThrow() throws Exception {
        if (!config.supportUserInUri()) {
            return;
        }

        String uriWithWrongUser = config.getCorrectLocationWithWrongUser();
        test00_newFileSystem(config.getScheme(), uriWithWrongUser, null, null, true);
    }

    @org.junit.Test
    public void test00_newFileSystem_nonDefaultCredentialIfSupported_noThrow() throws Exception {
        if (!config.supportNonDefaultCredential()) {
            return;
        }

        Credential nonDefaultCredential = config.getNonDefaultCredential(credentials);
        test00_newFileSystem(config.getScheme(), config.getCorrectLocation(), nonDefaultCredential, null, false);
    }

    @org.junit.Test
    public void test00_newFileSystem_emptyProperties_noThrow() throws Exception {
        test00_newFileSystem(config.getScheme(), config.getCorrectLocation(), config.getDefaultCredential(credentials),
                new HashMap<String, String>(), false);
    }

    @org.junit.Test
    public void test00_newFileSystem_correctProperties_noThrow() throws Exception {
        if (!config.supportsProperties()) {
            return;
        }

        test00_newFileSystem(config.getScheme(), config.getCorrectLocation(), config.getDefaultCredential(credentials),
                config.getCorrectProperties(), false);
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
    // Depends on: [getTestFileSystem], close, isOpen

    private void test01_isOpen(FileSystem fs, boolean expected, boolean mustFail) throws Exception {

        boolean result = false;

        try {
            result = files.isOpen(fs);
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

    @org.junit.Test
    public void test01_isOpen_fsIsNull_throw() throws Exception {
        test01_isOpen(null, false, true);
    }

    @org.junit.Test
    public void test01_isOpen_openFs_true() throws Exception {
        FileSystem fs = config.getTestFileSystem(files, credentials);
        test01_isOpen(fs, true, false);
    }

    @org.junit.Test
    public void test01_isOpen_closedFsIfSupported_false() throws Exception {
        if (!config.supportsClose()) {
            return;
        }
        FileSystem fs = config.getTestFileSystem(files, credentials);
        files.close(fs);
        test01_isOpen(fs, false, false);
    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST close
    //
    // Possible parameters:
    //
    // FileSystem - null / open FS / closed FS
    //
    // Total combinations : 3
    //
    // Depends on: [getTestFileSystem], close

    private void test02_close(FileSystem fs, boolean mustFail) throws Exception {

        try {
            files.close(fs);
        } catch (Exception e) {
            if (mustFail) {
                // expected
                return;
            }
            throwUnexpected("test02_close", e);
        }

        if (mustFail) {
            throwExpected("test02_close");
        }
    }

    @org.junit.Test
    public void test02_close() throws Exception {

        // test with null filesystem
        test02_close(null, true);

        if (config.supportsClose()) {

            FileSystem fs = config.getTestFileSystem(files, credentials);

            // test with correct open filesystem
            test02_close(fs, false);

            // test with correct closed filesystem
            test02_close(fs, true);
        }

    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST newPath
    //
    // Possible parameters:
    //
    // FileSystem - null / correct
    // RelativePath - null / empty / value
    //
    // Total combinations : 2
    //
    // Depends on: [getTestFileSystem], FileSystem.getEntryPath(), Path.getPath(), RelativePath, close

    private void test03_newPath(FileSystem fs, RelativePath path, String expected, boolean mustFail) throws Exception {

        String result = null;

        try {
            result = files.newPath(fs, path).getRelativePath().getAbsolutePath();
        } catch (Exception e) {
            if (mustFail) {
                // expected exception
                return;
            }

            throwUnexpected("test03_newPath", e);
        }

        if (mustFail) {
            throwExpected("test03_newPath");
        }

        if (!result.equals(expected)) {
            throwWrong("test03_newPath", expected, result);
        }
    }

    @org.junit.Test
    public void test03_newPath() throws Exception {

        FileSystem fs = config.getTestFileSystem(files, credentials);
        String root = "/";

        // test with null filesystem and null relative path
        test03_newPath(null, null, null, true);

        // test with correct filesystem and null relative path
        test03_newPath(fs, null, null, true);

        // test with correct filesystem and empty relative path
        test03_newPath(fs, new RelativePath(), root, false);

        // test with correct filesystem and relativepath with value
        test03_newPath(fs, new RelativePath("test"), root + "test", false);

        files.close(fs);

    }

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

    private void test04_createDirectory(Path path, boolean mustFail) throws Exception {

        try {
            files.createDirectory(path);
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

    @org.junit.Test
    public void test04_createDirectory() throws Exception {

        // test with null
        test04_createDirectory(null, true);

        Path cwd = config.getWorkingDir(files, credentials);
        Path root = resolve(cwd, TEST_ROOT);

        // test with non-existing dir
        test04_createDirectory(root, false);

        // test with existing dir
        test04_createDirectory(root, true);

        // test with existing file
        Path file0 = createTestFile(root, null);
        test04_createDirectory(file0, true);
        deleteTestFile(file0);

        // test with non-existent parent dir
        Path parent = createNewTestDirName(root);
        Path dir0 = createNewTestDirName(parent);
        test04_createDirectory(dir0, true);

        // cleanup
        deleteTestDir(root);

        // close test FS
        files.close(cwd.getFileSystem());

        if (config.supportsClose()) {
            // test with closed fs
            test04_createDirectory(root, true);
        }

    }

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
            files.createDirectories(path);

            assert (files.exists(path));

            FileAttributes att = files.getAttributes(path);

            assert (att.isDirectory());

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

    @org.junit.Test
    public void test05_createDirectories() throws Exception {

        // test with null
        test05_createDirectories(null, true);

        Path cwd = config.getWorkingDir(files, credentials);

        Path root = resolve(cwd, TEST_ROOT, "test05_createDirectories");

        // test with non-existing dir
        test05_createDirectories(root, false);

        // test with existing dir
        test05_createDirectories(root, true);

        // dir with existing parents
        Path dir0 = createNewTestDirName(root);
        test05_createDirectories(dir0, false);
        deleteTestDir(dir0);

        // dir with non-existing parents
        Path dir1 = createNewTestDirName(dir0);
        test05_createDirectories(dir1, false);

        // dir where last parent is file
        Path file0 = createTestFile(dir0, null);
        Path dir2 = createNewTestDirName(file0);
        test05_createDirectories(dir2, true);

        // cleanup
        deleteTestDir(dir1);
        deleteTestFile(file0);
        deleteTestDir(dir0);
        deleteTestDir(root);

        // close test FS
        files.close(cwd.getFileSystem());

        if (config.supportsClose()) {
            // test with closed fs
            test05_createDirectories(root, true);
        }

    }

    // From this point on we can use prepareTestDir

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: isDirectory
    //
    // Possible parameters:
    //
    // Path null / non-existing file / existing file / existing dir / closed filesystem
    //
    // Total combinations : 4
    //
    // Depends on: [getTestFileSystem], [createTestDir], [createNewTestFileName], [createTestFile], [deleteTestFile]
    //             [closeTestFileSystem]
    //
    //    private void test06_isDirectory(Path path, boolean expected, boolean mustFail) throws Exception {
    //
    //        boolean result = false;
    //
    //        try {
    //            result = files.isDirectory(path);
    //        } catch (Exception e) {
    //
    //            if (mustFail) {
    //                // expected
    //                return;
    //            }
    //
    //            throwUnexpected("test06_isDirectory", e);
    //        }
    //
    //        if (mustFail) {
    //            throwExpected("test06_isDirectory");
    //        }
    //
    //        if (result != expected) {
    //            throwWrong("test06_isDirectory", "" + expected, "" + result);
    //        }
    //    }
    //
    //    @org.junit.Test
    //    public void test06_isDirectory() throws Exception {
    //
    //
    //
    //        // prepare
    //        FileSystem fs = config.getTestFileSystem(files, credentials);
    //        prepareTestDir(fs, "test06_isDirectory");
    //
    //        // test with null
    //        test06_isDirectory(null, false, true);
    //
    //        // test with non-existing file
    //        Path file0 = createNewTestFileName(testDir);
    //        test06_isDirectory(file0, false, false);
    //
    //        // test with existing file
    //        Path file1 = createTestFile(testDir, null);
    //        test06_isDirectory(file1, false, false);
    //        deleteTestFile(file1);
    //
    //        // test with existing dir
    //        test06_isDirectory(testDir, true, false);
    //
    //        // cleanup
    //        deleteTestDir(testDir);
    //        config.closeTestFileSystem(files, fs);
    //
    //        if (config.supportsClose()) {
    //            // test with closed filesystem
    //            test06_isDirectory(testDir, true, true);
    //        }
    //
    //
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
            files.createFile(path);
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

    @org.junit.Test
    public void test07_createFile() throws Exception {

        // prepare
        prepareTestDir("test07_createFile");

        // test with null
        test07_createFile(null, true);

        // test with non-existing file
        Path file0 = createNewTestFileName(testDir);
        test07_createFile(file0, false);

        // test with existing file
        test07_createFile(file0, true);

        // test with existing dir
        test07_createFile(testDir, true);

        Path tmp = createNewTestDirName(testDir);
        Path file1 = createNewTestFileName(tmp);

        // test with non-existing parent
        test07_createFile(file1, true);

        // cleanup
        files.delete(file0);
        deleteTestDir(testDir);

        // close test FS
        closeTestFS();

        if (config.supportsClose()) {
            // test with closed filesystem
            test07_createFile(file0, true);
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
            result = files.exists(path);
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

    @org.junit.Test
    public void test08_exists() throws Exception {

        // prepare
        prepareTestDir("test08_exists");

        // test with null
        test08_exists(null, false, true);

        // test with non-existing file
        Path file0 = createNewTestFileName(testDir);
        test08_exists(file0, false, false);

        // test with existing file
        Path file1 = createTestFile(testDir, null);
        test08_exists(file1, true, false);
        deleteTestFile(file1);

        // cleanup
        deleteTestDir(testDir);

        // close test FS
        closeTestFS();

    }

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
            files.delete(path);
        } catch (Exception e) {

            if (mustFail) {
                // expected
                return;
            }

            throwUnexpected("test09_delete", e);
        }

        if (files.exists(path)) {
            throwWrong("test09_delete", "no file", "a file");
        }

        if (mustFail) {
            throwExpected("test09_delete");
        }
    }

    @org.junit.Test
    public void test09_delete() throws Exception {

        // test with null
        test09_delete(null, true);

        prepareTestDir("test09_delete");

        // test with non-existing file
        Path file0 = createNewTestFileName(testDir);
        test09_delete(file0, true);

        // test with existing file
        Path file1 = createTestFile(testDir, null);
        test09_delete(file1, false);

        // test with existing empty dir
        Path dir0 = createTestDir(testDir);
        test09_delete(dir0, false);

        // test with existing non-empty dir
        Path dir1 = createTestDir(testDir);
        Path file2 = createTestFile(dir1, null);
        test09_delete(dir1, true);

        // test with non-writable file
        //        Path file3 = createTestFile(testDir, null);
        //        files.setPosixFilePermissions(file3, new HashSet<PosixFilePermission>());

        //      System.err.println("Attempting to delete: " + file3.getPath() + " " + files.getAttributes(file3));

        //        test09_delete(file3, true);

        // cleanup
        deleteTestFile(file2);
        deleteTestDir(dir1);
        deleteTestDir(testDir);

        // Close test fs
        closeTestFS();

        if (config.supportsClose()) {
            // test with closed fs
            test09_delete(testDir, true);
        }

    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: size
    //
    // Possible parameters:
    //
    // Path null / non-existing file / existing file size 0 / existing file size N / file from closed FS
    //
    // Total combinations : 5
    //
    // Depends on: [getTestFileSystem], [createTestDir], [createNewTestFileName], [createTestFile], [deleteTestFile],
    //             [deleteTestDir], [closeTestFileSystem], size, close

    //    private void test10_size(Path path, long expected, boolean mustFail) throws Exception {
    //
    //        long result = -1;
    //
    //        try {
    //            result = files.size(path);
    //        } catch (Exception e) {
    //
    //            if (mustFail) {
    //                // expected
    //                return;
    //            }
    //
    //            throwUnexpected("test10_size", e);
    //        }
    //
    //        if (mustFail) {
    //            throwExpected("test10_size");
    //        }
    //
    //        if (result != expected) {
    //            throwWrong("test10_size", "" + expected, "" + result);
    //        }
    //    }
    //
    //    @org.junit.Test
    //    public void test10_size() throws Exception {
    //
    //
    //
    //        // test with null parameter
    //        test10_size(null, -1, true);
    //
    //        FileSystem fs = config.getTestFileSystem(files, credentials);
    //        prepareTestDir(fs, "test10_size");
    //
    //        // test with non existing file
    //        Path file1 = createNewTestFileName(testDir);
    //        test10_size(file1, -1, true);
    //
    //        // test with existing empty file
    //        Path file2 = createTestFile(testDir, new byte[0]);
    //        test10_size(file2, 0, false);
    //        deleteTestFile(file2);
    //
    //        // test with existing filled file
    //        Path file3 = createTestFile(testDir, new byte[13]);
    //        test10_size(file3, 13, false);
    //        deleteTestFile(file3);
    //
    //        // test with dir
    //        Path dir0 = createTestDir(testDir);
    //        test10_size(dir0, 0, false);
    //        deleteTestDir(dir0);
    //        deleteTestDir(testDir);
    //
    //        // test with closed filesystem
    //        if (config.supportsClose()) {
    //            config.closeTestFileSystem(files, fs);
    //            test10_size(file1, 0, true);
    //        }
    //
    //
    //    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: newDirectoryStream
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

    private void test11_newDirectoryStream(Path root, Set<Path> expected, boolean mustFail) throws Exception {

        Set<Path> tmp = new HashSet<Path>();

        if (expected != null) {
            tmp.addAll(expected);
        }

        DirectoryStream<Path> in = null;

        try {
            in = files.newDirectoryStream(root);
        } catch (Exception e) {

            if (mustFail) {
                // expected
                return;
            }

            throwUnexpected("test11_newDirectoryStream", e);
        }

        if (mustFail) {
            close(in);
            throwExpected("test11_newDirectoryStream");
        }

        for (Path p : in) {

            if (tmp.contains(p)) {
                tmp.remove(p);
            } else {
                close(in);
                throwUnexpectedElement("test11_newDirectoryStream", p);
            }
        }

        close(in);

        if (tmp.size() > 0) {
            throwMissingElements("test11_newDirectoryStream", tmp);
        }
    }

    @org.junit.Test
    public void test11_newDirectoryStream() throws Exception {

        // test with null
        test11_newDirectoryStream(null, null, true);

        prepareTestDir("test11_newDirectoryStream");

        // test with empty dir
        test11_newDirectoryStream(testDir, null, false);

        // test with non-existing dir
        Path dir0 = createNewTestDirName(testDir);
        test11_newDirectoryStream(dir0, null, true);

        // test with exising file
        Path file0 = createTestFile(testDir, null);
        test11_newDirectoryStream(file0, null, true);

        // test with non-empty dir
        Path file1 = createTestFile(testDir, null);
        Path file2 = createTestFile(testDir, null);
        Path file3 = createTestFile(testDir, null);

        Set<Path> tmp = new HashSet<Path>();
        tmp.add(file0);
        tmp.add(file1);
        tmp.add(file2);
        tmp.add(file3);

        test11_newDirectoryStream(testDir, tmp, false);

        // test with subdirs
        Path dir1 = createTestDir(testDir);
        Path file4 = createTestFile(dir1, null);

        tmp.add(dir1);

        test11_newDirectoryStream(testDir, tmp, false);

        deleteTestFile(file4);
        deleteTestDir(dir1);
        deleteTestFile(file3);
        deleteTestFile(file2);
        deleteTestFile(file1);
        deleteTestFile(file0);
        deleteTestDir(testDir);

        // Close test fs
        closeTestFS();

        if (config.supportsClose()) {
            // test with closed fs
            test11_newDirectoryStream(testDir, null, true);
        }

    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: newDirectoryStream with filter
    //
    // Possible parameters:
    //
    // Path null / non-existing dir / existing empty dir / existing non-empty dir / existing dir with subdirs /
    //              existing file / closed filesystem
    //
    // directoryStreams.Filter null / filter returns all / filter returns none / filter selects one.

    // Total combinations : 7 + 8
    //
    // Depends on: [getTestFileSystem], FileSystem.getEntryPath(), [createNewTestDirName], createDirectories,
    //             [deleteTestDir], [createTestFile], [deleteTestFile], [deleteTestDir], [closeTestFileSystem]

    public void test12_newDirectoryStream(Path root, DirectoryStream.Filter filter, Set<Path> expected, boolean mustFail)
            throws Exception {

        Set<Path> tmp = new HashSet<Path>();

        if (expected != null) {
            tmp.addAll(expected);
        }

        DirectoryStream<Path> in = null;

        try {
            in = files.newDirectoryStream(root, filter);
        } catch (Exception e) {

            if (mustFail) {
                // expected
                return;
            }

            throwUnexpected("test12_newDirectoryStream_with_filter", e);
        }

        if (mustFail) {
            close(in);
            throwExpected("test12_newDirectoryStream_with_filter");
        }

        Iterator<Path> itt = in.iterator();

        while (itt.hasNext()) {

            Path p = itt.next();

            if (p == null) {
                throwUnexpectedElement("test12_newDirectoryStream_with_filter", null);
            }

            if (tmp.contains(p)) {
                tmp.remove(p);
            } else {
                close(in);
                throwUnexpectedElement("test12_newDirectoryStream_with_filter", p.toString());
            }
        }

        close(in);

        if (tmp.size() > 0) {
            throwMissingElements("test12_newDirectoryStream_with_filter", tmp);
        }

        // close(in); // double close should result in exception
    }

    @org.junit.Test
    public void test12_newDirectoryStream_with_filter() throws Exception {

        // test with null
        test12_newDirectoryStream(null, null, null, true);

        prepareTestDir("test12_newDirectoryStream_with_filter");

        // test with empty dir + null filter
        test12_newDirectoryStream(testDir, null, null, true);

        // test with empty dir + true filter
        test12_newDirectoryStream(testDir, new AllTrue(), null, false);

        // test with empty dir + false filter
        test12_newDirectoryStream(testDir, new AllTrue(), null, false);

        // test with non-existing dir
        Path dir0 = createNewTestDirName(testDir);
        test12_newDirectoryStream(dir0, new AllTrue(), null, true);

        // test with existing file
        Path file0 = createTestFile(testDir, null);
        test12_newDirectoryStream(file0, new AllTrue(), null, true);

        // test with non-empty dir and allTrue
        Path file1 = createTestFile(testDir, null);
        Path file2 = createTestFile(testDir, null);
        Path file3 = createTestFile(testDir, null);

        Set<Path> tmp = new HashSet<Path>();
        tmp.add(file0);
        tmp.add(file1);
        tmp.add(file2);
        tmp.add(file3);

        test12_newDirectoryStream(testDir, new AllTrue(), tmp, false);

        // test with non-empty dir and allFalse
        test12_newDirectoryStream(testDir, new AllFalse(), null, false);

        tmp.remove(file3);

        // test with non-empty dir and select
        test12_newDirectoryStream(testDir, new Select(tmp), tmp, false);

        // test with subdirs
        Path dir1 = createTestDir(testDir);
        Path file4 = createTestFile(dir1, null);

        test12_newDirectoryStream(testDir, new Select(tmp), tmp, false);

        deleteTestFile(file4);
        deleteTestDir(dir1);
        deleteTestFile(file3);
        deleteTestFile(file2);
        deleteTestFile(file1);
        deleteTestFile(file0);
        deleteTestDir(testDir);

        // Close test fs
        closeTestFS();

        if (config.supportsClose()) {
            // test with closed fs
            test12_newDirectoryStream(testDir, new AllTrue(), null, true);
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

        FileAttributes result = null;

        try {
            result = files.getAttributes(path);
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

        if (size >= 0 && result.size() != size) {
            throwWrong("test13_getfileAttributes", "size=" + size, "size=" + result.size());
        }

        if (isWithinMargin(currentTime, result.lastModifiedTime()) == false) {
            throwWrong("test13_getfileAttributes", "lastModifiedTime=" + currentTime,
                    "lastModifiedTime=" + result.lastModifiedTime());
        }

        if (isWithinMargin(currentTime, result.creationTime()) == false) {
            throwWrong("test13_getfileAttributes", "creationTime=" + currentTime, "creationTime=" + result.creationTime());
        }

        if (isWithinMargin(currentTime, result.lastAccessTime()) == false) {
            throwWrong("test13_getfileAttributes", "lastAccessTime=" + currentTime, "lastAccessTime=" + result.lastAccessTime());
        }

        System.err.println("File " + path + " has attributes: " + result.isReadable() + " " + result.isWritable() + " "
                + result.isExecutable() + " " + result.isSymbolicLink() + " " + result.isDirectory() + " "
                + result.isRegularFile() + " " + result.isHidden() + " " + result.isOther() + " " + result.lastAccessTime() + " "
                + result.lastModifiedTime());
    }

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

    @org.junit.Test
    public void test13_getAttributes() throws Exception {

        long currentTime = System.currentTimeMillis();

        // test with null
        test13_getAttributes(null, false, -1, currentTime, true);

        prepareTestDir("test13_getAttributes");

        // test with non-existing file
        Path file0 = createNewTestFileName(testDir);
        test13_getAttributes(file0, false, -1, currentTime, true);

        // test with existing empty file
        Path file1 = createTestFile(testDir, null);
        test13_getAttributes(file1, false, 0, currentTime, false);

        // test with existing non-empty file
        Path file2 = createTestFile(testDir, new byte[] { 1, 2, 3 });
        test13_getAttributes(file2, false, 3, currentTime, false);

        // test with existing dir
        Path dir0 = createTestDir(testDir);
        test13_getAttributes(dir0, true, -1, currentTime, false);

        // TODO: test with link!

        deleteTestDir(dir0);
        deleteTestFile(file2);
        deleteTestFile(file1);
        deleteTestDir(testDir);

        // Close test fs
        closeTestFS();

        if (config.supportsClose()) {
            // test with closed fs
            test13_getAttributes(testDir, false, -1, currentTime, true);
        }
    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: setPosixFilePermissions
    //
    // Possible parameters:
    //
    // Path null / non-existing file / existing file / existing dir / existing link (!) / closed filesystem
    // Set<PosixFilePermission> null / empty set / [various correct set]
    //
    // Total combinations : N
    //
    // Depends on: [getTestFileSystem], FileSystem.getEntryPath(), [createNewTestDirName], createDirectories,
    //             [deleteTestDir], [createTestFile], [deleteTestFile], [deleteTestDir], [closeTestFileSystem]

    private void test14_setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions, boolean mustFail)
            throws Exception {

        try {
            files.setPosixFilePermissions(path, permissions);
        } catch (Exception e) {
            if (mustFail) {
                // expected
                return;
            }

            throwUnexpected("test14_setPosixFilePermissions", e);
        }

        if (mustFail) {
            throwExpected("test14_setPosixFilePermissions");
        }

        // Check result
        FileAttributes attributes = files.getAttributes(path);
        Set<PosixFilePermission> tmp = attributes.permissions();

        if (!permissions.equals(tmp)) {
            throwWrong("test14_setPosixFilePermissions", permissions, tmp);
        }
    }

    @org.junit.Test
    public void test14_setPosixFilePermissions() throws Exception {

        if (!config.supportsPosixPermissions()) {
            return;
        }

        // test with null, null
        test14_setPosixFilePermissions(null, null, true);

        prepareTestDir("test14_setPosixFilePermissions");

        // test with existing file, null set
        Path file0 = createTestFile(testDir, null);
        test14_setPosixFilePermissions(file0, null, true);

        // test with existing file, empty set
        Set<PosixFilePermission> permissions = new HashSet<PosixFilePermission>();
        test14_setPosixFilePermissions(file0, permissions, false);

        // test with existing file, non-empty set
        permissions.add(PosixFilePermission.OWNER_EXECUTE);
        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.OWNER_WRITE);
        test14_setPosixFilePermissions(file0, permissions, false);

        permissions.add(PosixFilePermission.OTHERS_READ);
        test14_setPosixFilePermissions(file0, permissions, false);

        permissions.add(PosixFilePermission.GROUP_READ);
        test14_setPosixFilePermissions(file0, permissions, false);

        // test with non-existing file
        Path file1 = createNewTestFileName(testDir);
        test14_setPosixFilePermissions(file1, permissions, true);

        // test with existing dir
        Path dir0 = createTestDir(testDir);

        permissions.add(PosixFilePermission.OWNER_EXECUTE);
        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.OWNER_WRITE);
        test14_setPosixFilePermissions(dir0, permissions, false);

        permissions.add(PosixFilePermission.OTHERS_READ);
        test14_setPosixFilePermissions(dir0, permissions, false);

        permissions.add(PosixFilePermission.GROUP_READ);
        test14_setPosixFilePermissions(dir0, permissions, false);

        deleteTestDir(dir0);
        deleteTestFile(file0);
        deleteTestDir(testDir);

        // Close test fs
        closeTestFS();

        if (config.supportsClose()) {
            // test with closed fs
            test14_setPosixFilePermissions(file0, permissions, true);
        }
    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: newAttributesDirectoryStream
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

    private void test15_newAttributesDirectoryStream(Path root, Set<PathAttributesPair> expected, boolean mustFail)
            throws Exception {

        Set<PathAttributesPair> tmp = new HashSet<PathAttributesPair>();

        if (expected != null) {
            tmp.addAll(expected);
        }

        DirectoryStream<PathAttributesPair> in = null;

        try {
            in = files.newAttributesDirectoryStream(root);
        } catch (Exception e) {

            if (mustFail) {
                // expected
                return;
            }

            throwUnexpected("test15_newAttributesDirectoryStream", e);
        }

        if (mustFail) {
            close(in);
            throwExpected("test15_newAttributesDirectoryStream");
        }

        System.err.println("Comparing PathAttributesPairs:");

        for (PathAttributesPair p : in) {

            System.err.println("Got input " + p.path() + " " + p.attributes());

            PathAttributesPair found = null;

            for (PathAttributesPair x : tmp) {

                System.err.println("  Comparing to " + x.path() + " " + x.attributes());

                if (x.path().equals(p.path()) && x.attributes().equals(p.attributes())) {
                    System.err.println("Found!");
                    found = x;
                    break;
                }
            }

            System.err.println("  Found = " + found);

            if (found != null) {
                tmp.remove(found);
            } else {
                System.err.println("NOT Found!");
                close(in);
                throwUnexpectedElement("test15_newAttributesDirectoryStream", p.path());

            }

            //            if (tmp.contains(p)) {
            //                System.err.println("Found!");
            //                tmp.remove(p);
            //            } else {
            //                System.err.println("NOT Found!");
            //
            //                close(in);
            //                throwUnexpectedElement("newAttributesDirectoryStream", p.path().getPath());
            //            }
        }

        close(in);

        if (tmp.size() > 0) {
            throwMissingElements("test15_newAttributesDirectoryStream", tmp);
        }
    }

    @org.junit.Test
    public void test15_newAttrributesDirectoryStream() throws Exception {

        // test with null
        test15_newAttributesDirectoryStream(null, null, true);

        prepareTestDir("test15_newAttrributesDirectoryStream");

        // test with empty dir
        test15_newAttributesDirectoryStream(testDir, null, false);

        // test with non-existing dir
        Path dir0 = createNewTestDirName(testDir);
        test15_newAttributesDirectoryStream(dir0, null, true);

        // test with exising file
        Path file0 = createTestFile(testDir, null);
        test15_newAttributesDirectoryStream(file0, null, true);

        // test with non-empty dir
        Path file1 = createTestFile(testDir, null);
        Path file2 = createTestFile(testDir, null);
        Path file3 = createTestFile(testDir, null);

        Set<PathAttributesPair> result = new HashSet<PathAttributesPair>();
        result.add(new PathAttributesPairImplementation(file0, files.getAttributes(file0)));
        result.add(new PathAttributesPairImplementation(file1, files.getAttributes(file1)));
        result.add(new PathAttributesPairImplementation(file2, files.getAttributes(file2)));
        result.add(new PathAttributesPairImplementation(file3, files.getAttributes(file3)));

        test15_newAttributesDirectoryStream(testDir, result, false);

        // test with subdirs
        Path dir1 = createTestDir(testDir);
        Path file4 = createTestFile(dir1, null);

        result.add(new PathAttributesPairImplementation(dir1, files.getAttributes(dir1)));

        test15_newAttributesDirectoryStream(testDir, result, false);

        deleteTestFile(file4);
        deleteTestDir(dir1);
        deleteTestFile(file3);
        deleteTestFile(file2);
        deleteTestFile(file1);
        deleteTestFile(file0);
        deleteTestDir(testDir);

        // Close test fs
        closeTestFS();

        if (config.supportsClose()) {
            // test with closed fs
            test15_newAttributesDirectoryStream(testDir, null, true);
        }

    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: newAttributesDirectoryStream with filter
    //
    // Possible parameters:
    //
    // Path null / non-existing dir / existing empty dir / existing non-empty dir / existing dir with subdirs /
    //              existing file / closed filesystem
    //
    // directoryStreams.Filter null / filter returns all / filter returns none / filter selects one.

    // Total combinations : 7 + 8
    //
    // Depends on: [getTestFileSystem], FileSystem.getEntryPath(), [createNewTestDirName], createDirectories,
    //             [deleteTestDir], [createTestFile], [deleteTestFile], [deleteTestDir], [closeTestFileSystem]

    private void test16_newAttributesDirectoryStream(Path root, DirectoryStream.Filter filter, Set<PathAttributesPair> expected,
            boolean mustFail) throws Exception {

        Set<PathAttributesPair> tmp = new HashSet<PathAttributesPair>();

        if (expected != null) {
            tmp.addAll(expected);
        }

        DirectoryStream<PathAttributesPair> in = null;

        try {
            in = files.newAttributesDirectoryStream(root, filter);
        } catch (Exception e) {

            if (mustFail) {
                // expected
                return;
            }

            throwUnexpected("test16_newAttributesDirectoryDirectoryStream_with_filter", e);
        }

        if (mustFail) {
            close(in);
            throwExpected("test16_newAttributesDirectoryDirectoryStream_with_filter");
        }

        for (PathAttributesPair p : in) {

            System.err.println("Got input " + p.path() + " " + p.attributes());

            PathAttributesPair found = null;

            for (PathAttributesPair x : tmp) {

                System.err.println("  Comparing to " + x.path() + " " + x.attributes());

                if (x.path().equals(p.path()) && x.attributes().equals(p.attributes())) {
                    System.err.println("Found!");
                    found = x;
                    break;
                }
            }

            System.err.println("  Found = " + found);

            if (found != null) {
                tmp.remove(found);
            } else {
                System.err.println("NOT Found!");
                close(in);
                throwUnexpectedElement("test16_newAttributesDirectoryStream_with_filter", p.path());

            }

            //            if (tmp.contains(p)) {
            //                System.err.println("Found!");
            //                tmp.remove(p);
            //            } else {
            //                System.err.println("NOT Found!");
            //
            //                close(in);
            //                throwUnexpectedElement("newAttributesDirectoryStream", p.path().getPath());
            //            }
        }

        close(in);

        if (tmp.size() > 0) {
            throwMissingElements("test16_newAttributesDirectoryDirectoryStream_with_filter", tmp);
        }
    }

    @org.junit.Test
    public void test15_newAttributesDirectoryStream_with_filter() throws Exception {

        // test with null
        test16_newAttributesDirectoryStream(null, null, null, true);

        prepareTestDir("test15_newAttributesDirectoryStream_with_filter");

        // test with empty dir + null filter
        test16_newAttributesDirectoryStream(testDir, null, null, true);

        // test with empty dir + true filter
        test16_newAttributesDirectoryStream(testDir, new AllTrue(), null, false);

        // test with empty dir + false filter
        test16_newAttributesDirectoryStream(testDir, new AllTrue(), null, false);

        // test with non-existing dir
        Path dir0 = createNewTestDirName(testDir);
        test16_newAttributesDirectoryStream(dir0, new AllTrue(), null, true);

        // test with existing file
        Path file0 = createTestFile(testDir, null);
        test16_newAttributesDirectoryStream(file0, new AllTrue(), null, true);

        // test with non-empty dir and allTrue
        Path file1 = createTestFile(testDir, null);
        Path file2 = createTestFile(testDir, null);
        Path file3 = createTestFile(testDir, null);

        Set<PathAttributesPair> result = new HashSet<PathAttributesPair>();
        result.add(new PathAttributesPairImplementation(file0, files.getAttributes(file0)));
        result.add(new PathAttributesPairImplementation(file1, files.getAttributes(file1)));
        result.add(new PathAttributesPairImplementation(file2, files.getAttributes(file2)));
        result.add(new PathAttributesPairImplementation(file3, files.getAttributes(file3)));

        test16_newAttributesDirectoryStream(testDir, new AllTrue(), result, false);

        // test with non-empty dir and allFalse
        test16_newAttributesDirectoryStream(testDir, new AllFalse(), null, false);

        // test with subdirs
        Path dir1 = createTestDir(testDir);
        Path file4 = createTestFile(dir1, null);

        result.add(new PathAttributesPairImplementation(dir1, files.getAttributes(dir1)));
        test16_newAttributesDirectoryStream(testDir, new AllTrue(), result, false);

        // test with non-empty dir and select
        Set<Path> tmp = new HashSet<Path>();
        tmp.add(file0);
        tmp.add(file1);
        tmp.add(file2);

        result = new HashSet<PathAttributesPair>();
        result.add(new PathAttributesPairImplementation(file0, files.getAttributes(file0)));
        result.add(new PathAttributesPairImplementation(file1, files.getAttributes(file1)));
        result.add(new PathAttributesPairImplementation(file2, files.getAttributes(file2)));

        test16_newAttributesDirectoryStream(testDir, new Select(tmp), result, false);

        deleteTestFile(file4);
        deleteTestDir(dir1);
        deleteTestFile(file3);
        deleteTestFile(file2);
        deleteTestFile(file1);
        deleteTestFile(file0);
        deleteTestDir(testDir);

        // Close test fs
        closeTestFS();

        if (config.supportsClose()) {
            // test with closed fs
            test16_newAttributesDirectoryStream(testDir, new AllTrue(), null, true);
        }

    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: newInputStream
    //
    // Possible parameters:
    //
    // Path null / non-existing file / existing empty file / existing non-empty file / existing dir / closed filesystem
    //
    // Total combinations : 6
    //
    // Depends on:

    private void test20_newInputStream(Path file, byte[] expected, boolean mustFail) throws Exception {

        InputStream in = null;

        try {
            in = files.newInputStream(file);
        } catch (Exception e) {

            if (mustFail) {
                // expected
                return;
            }

            throwUnexpected("test20_newInputStream", e);
        }

        if (mustFail) {
            close(in);
            throwExpected("test20_newInputStream");
        }

        byte[] data = readFully(in);

        if (expected == null) {
            if (data.length != 0) {
                throwWrong("test20_newInputStream", "zero bytes", data.length + " bytes");
            }
            return;
        }

        if (expected.length != data.length) {
            throwWrong("test20_newInputStream", expected.length + " bytes", data.length + " bytes");
        }

        if (!Arrays.equals(expected, data)) {
            throwWrong("test20_newInputStream", Arrays.toString(expected), Arrays.toString(data));
        }
    }

    @org.junit.Test
    public void test20_newInputStream() throws Exception {

        byte[] data = "Hello World".getBytes();

        // test with null
        test20_newInputStream(null, null, true);

        prepareTestDir("test20_newInputStream");

        // test with non-existing file
        Path file0 = createNewTestFileName(testDir);
        test20_newInputStream(file0, null, true);

        // test with existing empty file
        Path file1 = createTestFile(testDir, null);
        test20_newInputStream(file1, null, false);

        // test with existing non-empty file
        Path file2 = createTestFile(testDir, data);
        test20_newInputStream(file2, data, false);

        // test with existing dir
        Path dir0 = createTestDir(testDir);
        test20_newInputStream(dir0, null, true);

        // cleanup
        deleteTestFile(file1);
        deleteTestFile(file2);
        deleteTestDir(dir0);
        deleteTestDir(testDir);

        // Close test fs
        closeTestFS();

        if (config.supportsClose()) {
            // test with closed fs
            test20_newInputStream(file2, data, true);
        }

    }

    @org.junit.Test
    public void test20b_newInputStreamDoubleClose() throws Exception {

        // See what happens when we close an in input stream twice and then reopen the stream. This failed
        // on the SSH adaptor due to a bug in the sftp channel cache.

        byte[] data = "Hello World".getBytes();

        prepareTestDir("test20b_newInputStreamDoubleClose");

        Path file = createTestFile(testDir, data);

        InputStream in = null;

        try {
            in = files.newInputStream(file);
        } catch (Exception e) {
            // should not fail
            throwUnexpected("test20b_newInputStreamDoubleClose", e);
        }

        try {
            // should not fail
            in.close();
        } catch (Exception e) {
            throwUnexpected("test20b_newInputStreamDoubleClose", e);
        }

        try {
            in.close();
        } catch (Exception e) {
            // should fail
        }

        try {
            in = files.newInputStream(file);
        } catch (Exception e) {
            // should not fail
            throwUnexpected("test20b_newInputStreamDoubleClose", e);
        }

        try {
            in.close();
        } catch (Exception e) {
            // should not fail
            throwUnexpected("test20b_newInputStreamDoubleClose", e);
        }

        deleteTestFile(file);
        deleteTestDir(testDir);

    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: newOuputStream
    //
    // Possible parameters:
    //
    // Path null / non-existing file / existing empty file / existing non-empty file / existing dir / closed filesystem
    // OpenOption null / CREATE / OPEN / OPEN_OR_CREATE / READ / TRUNCATE / READ / WRITE + combinations
    //
    // Total combinations : N
    //
    // Depends on:

    private void test21_newOutputStream(Path path, OpenOption[] options, byte[] data, byte[] expected, boolean mustFail)
            throws Exception {

        OutputStream out = null;

        try {
            out = files.newOutputStream(path, options);
        } catch (Exception e) {

            if (mustFail) {
                // expected
                return;
            }

            throwUnexpected("test21_newOutputStream", e);
        }

        if (mustFail) {
            close(out);
            throwExpected("test21_newOutputStream");
        }

        out.write(data);
        close(out);

        InputStream in = files.newInputStream(path);

        byte[] tmp = readFully(in);

        if (expected == null) {
            if (data.length != 0) {
                throwWrong("test21_newOutputStream", "zero bytes", tmp.length + " bytes");
            }
            return;
        }

        if (expected.length != tmp.length) {
            throwWrong("test21_newOutputStream", expected.length + " bytes", tmp.length + " bytes");
        }

        if (!Arrays.equals(expected, tmp)) {
            throwWrong("test21_newOutputStream", Arrays.toString(expected), Arrays.toString(tmp));
        }
    }

    @org.junit.Test
    public void test21_newOutputStream() throws Exception {

        byte[] data = "Hello World".getBytes();
        byte[] data2 = "Hello WorldHello World".getBytes();

        // test with null
        test21_newOutputStream(null, null, null, null, true);

        prepareTestDir("test21_newOuputStream");

        // test with existing file and null options
        Path file0 = createTestFile(testDir, null);
        test21_newOutputStream(file0, null, null, null, true);

        // test with existing file and empty options
        test21_newOutputStream(file0, new OpenOption[0], null, null, true);

        // test with existing file and CREATE option
        test21_newOutputStream(file0, new OpenOption[] { OpenOption.CREATE }, null, null, true);

        // test with existing file and OPEN option
        test21_newOutputStream(file0, new OpenOption[] { OpenOption.OPEN }, null, null, true);

        // test with existing file and OPEN_OR_CREATE option
        test21_newOutputStream(file0, new OpenOption[] { OpenOption.OPEN_OR_CREATE }, null, null, true);

        // test with existing file and APPEND option
        test21_newOutputStream(file0, new OpenOption[] { OpenOption.APPEND }, null, null, true);

        // test with existing file and TRUNCATE option
        test21_newOutputStream(file0, new OpenOption[] { OpenOption.TRUNCATE }, null, null, true);

        // test with existing file and READ option
        test21_newOutputStream(file0, new OpenOption[] { OpenOption.READ }, null, null, true);

        // test with existing file and WRITE option
        test21_newOutputStream(file0, new OpenOption[] { OpenOption.WRITE }, null, null, true);

        // test with existing file and CREATE + APPEND option
        test21_newOutputStream(file0, new OpenOption[] { OpenOption.CREATE, OpenOption.APPEND }, null, null, true);

        // test with existing file and CREATE + APPEND + READ option
        test21_newOutputStream(file0, new OpenOption[] { OpenOption.CREATE, OpenOption.APPEND, OpenOption.READ }, null, null,
                true);

        // test with existing file and OPEN_OR_CREATE + APPEND option
        test21_newOutputStream(file0, new OpenOption[] { OpenOption.OPEN_OR_CREATE, OpenOption.APPEND }, data, data, false);

        // test with existing file and OPEN + APPEND option
        test21_newOutputStream(file0, new OpenOption[] { OpenOption.OPEN, OpenOption.APPEND }, data, data2, false);

        // test with existing file and OPEN_OR_CREATE + APPEND + WRITE option
        test21_newOutputStream(file0, new OpenOption[] { OpenOption.OPEN, OpenOption.TRUNCATE, OpenOption.WRITE }, data, data,
                false);

        // test with existing file and CREATE + TRUNCATE option
        test21_newOutputStream(file0, new OpenOption[] { OpenOption.CREATE, OpenOption.TRUNCATE }, null, null, true);

        // test with existing file and OPEN_OR_CREATE + TRUNCATE option
        test21_newOutputStream(file0, new OpenOption[] { OpenOption.OPEN_OR_CREATE, OpenOption.TRUNCATE }, data, data, false);

        // test with existing file and OPEN + TRUNCATE option
        test21_newOutputStream(file0, new OpenOption[] { OpenOption.OPEN, OpenOption.TRUNCATE }, data, data, false);

        deleteTestFile(file0);

        // test with non-existing and CREATE + APPEND option
        Path file1 = createNewTestFileName(testDir);
        test21_newOutputStream(file1, new OpenOption[] { OpenOption.CREATE, OpenOption.APPEND }, data, data, false);
        deleteTestFile(file1);

        // test with non-existing and OPEN_OR_CREATE + APPEND option
        Path file2 = createNewTestFileName(testDir);
        test21_newOutputStream(file2, new OpenOption[] { OpenOption.OPEN_OR_CREATE, OpenOption.APPEND }, data, data, false);
        deleteTestFile(file2);

        // test with non-existing and OPEN + APPEND option
        Path file3 = createNewTestFileName(testDir);
        test21_newOutputStream(file3, new OpenOption[] { OpenOption.OPEN, OpenOption.APPEND }, null, null, true);

        // test with exising dir
        Path dir0 = createTestDir(testDir);

        test21_newOutputStream(dir0, new OpenOption[] { OpenOption.CREATE, OpenOption.APPEND }, null, null, true);
        test21_newOutputStream(dir0, new OpenOption[] { OpenOption.OPEN_OR_CREATE, OpenOption.APPEND }, null, null, true);
        test21_newOutputStream(dir0, new OpenOption[] { OpenOption.OPEN, OpenOption.APPEND }, null, null, true);

        deleteTestDir(dir0);

        // test with conflicting options
        Path file4 = createTestFile(testDir, null);

        test21_newOutputStream(file4, new OpenOption[] { OpenOption.CREATE, OpenOption.OPEN, OpenOption.APPEND }, null, null,
                true);
        test21_newOutputStream(file4, new OpenOption[] { OpenOption.OPEN, OpenOption.TRUNCATE, OpenOption.APPEND }, null, null,
                true);
        test21_newOutputStream(file4, new OpenOption[] { OpenOption.OPEN, OpenOption.APPEND, OpenOption.READ }, null, null, true);

        deleteTestFile(file4);

        // test with non-existing and CREATE option
        Path file5 = createNewTestFileName(testDir);
        test21_newOutputStream(file5, new OpenOption[] { OpenOption.CREATE, OpenOption.APPEND }, data, data, false);
        deleteTestFile(file5);

        deleteTestDir(testDir);

        // Close test fs
        closeTestFS();

        if (config.supportsClose()) {
            // test with closed fs
            test21_newOutputStream(file0, new OpenOption[] { OpenOption.OPEN_OR_CREATE, OpenOption.APPEND }, null, null, true);
        }

    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: newByteChannel
    //
    // Possible parameters:
    //
    // Path null / non-existing file / existing empty file / existing non-empty file / existing dir / closed filesystem
    // OpenOption null / CREATE / OPEN / OPEN_OR_CREATE / READ / TRUNCATE / READ / WRITE + combinations
    //
    // Total combinations : N
    //
    // Depends on:

    //    public void test22_newByteChannel(Path path, OpenOption [] options, byte [] toWrite, byte [] toRead,
    //            boolean mustFail) throws Exception {
    //
    //        if (!config.supportsNewByteChannel()) {
    //            return;
    //        }
    //
    //        SeekableByteChannel channel = null;
    //
    //        try {
    //            channel = files.newByteChannel(path, options);
    //        } catch (Exception e) {
    //
    //            if (mustFail) {
    //                // expected
    //                return;
    //            }
    //
    //            throwUnexpected("test22_newByteChannel", e);
    //        }
    //
    //        if (mustFail) {
    //            close(channel);
    //            throwExpected("test22_newByteChannel");
    //        }
    //
    //        if (toWrite != null) {
    //            channel.write(ByteBuffer.wrap(toWrite));
    //        }
    //
    //        if (toRead != null) {
    //
    //            channel.position(0);
    //
    //            byte [] tmp = readFully(channel);
    //
    //            if (toRead.length != tmp.length) {
    //                throwWrong("test22_newByteChannel", toRead.length + " bytes", tmp.length + " bytes");
    //            }
    //
    //            if (!Arrays.equals(toRead, tmp)) {
    //                throwWrong("test22_newByteChannel", Arrays.toString(toRead), Arrays.toString(tmp));
    //            }
    //        }
    //
    //        close(channel);
    //    }

    //    @org.junit.Test
    //    public void test21_newByteChannel() throws Exception {
    //
    //        if (!config.supportsNewByteChannel()) {
    //            return;
    //        }
    //
    //        byte [] data = "Hello World".getBytes();
    //        byte [] data2 = "Hello WorldHello World".getBytes();
    //
    //
    //
    //        // test with null
    //        test22_newByteChannel(null, null, null, null, true);
    //
    //        FileSystem fs =  config.getTestFileSystem(files, credentials);
    //        prepareTestDir(fs, "test22_newByteChannel");
    //
    //        // test with existing file and null options
    //        Path file0 = createTestFile(testDir, null);
    //        test22_newByteChannel(file0, null, null, null, true);
    //
    //        // test with existing file and empty options
    //        test22_newByteChannel(file0, new OpenOption[0],  null, null, true);
    //
    //        // test with existing file and CREATE option
    //        test22_newByteChannel(file0, new OpenOption [] { OpenOption.CREATE }, null, null, true);
    //
    //        // test with existing file and OPEN option
    //        test22_newByteChannel(file0, new OpenOption [] { OpenOption.OPEN }, null, null, true);
    //
    //        // test with existing file and OPEN_OR_CREATE option
    //        test22_newByteChannel(file0, new OpenOption [] { OpenOption.OPEN_OR_CREATE }, null, null, true);
    //
    //        // test with existing file and APPEND option
    //        test22_newByteChannel(file0, new OpenOption [] { OpenOption.APPEND }, null, null, true);
    //
    //        // test with existing file and TRUNCATE option
    //        test22_newByteChannel(file0, new OpenOption [] { OpenOption.TRUNCATE }, null, null, true);
    //
    //        // test with existing file and READ option
    //        test22_newByteChannel(file0, new OpenOption [] { OpenOption.READ }, null, null, true);
    //
    //        // test with existing file and WRITE option
    //        test22_newByteChannel(file0, new OpenOption [] { OpenOption.WRITE }, null, null, true);
    //
    //        // test with existing file and CREATE + APPEND option
    //        test22_newByteChannel(file0, new OpenOption [] { OpenOption.CREATE, OpenOption.APPEND }, null, null, true);
    //
    //        // test with existing file and OPEN + READ + APPEND option
    //        test22_newByteChannel(file0, new OpenOption [] { OpenOption.OPEN, OpenOption.READ, OpenOption.APPEND }, null, null, true);
    //
    //        // test with existing file and OPEN + READ option
    //        Path file1 = createTestFile(testDir, data);
    //        test22_newByteChannel(file1, new OpenOption [] { OpenOption.OPEN, OpenOption.READ }, null, data, false);
    //
    //        // Test with existing file and OPEN + APPEND + READ + WRITE
    //        test22_newByteChannel(file1, new OpenOption [] { OpenOption.OPEN, OpenOption.WRITE, OpenOption.READ }, data, data, false);
    //
    //        // Test with existing file and OPEN + APPEND + READ + WRITE
    //        test22_newByteChannel(file1, new OpenOption [] { OpenOption.OPEN, OpenOption.APPEND, OpenOption.WRITE, OpenOption.READ }, null, null, true);
    //
    //        // test with existing file and OPEN + WRITE without APPEND option
    //        test22_newByteChannel(file1, new OpenOption [] { OpenOption.OPEN, OpenOption.WRITE }, null, null, true);
    //
    //        // test with existing file and CREATE + WRITE + APPEND
    //        test22_newByteChannel(file1, new OpenOption [] { OpenOption.CREATE, OpenOption.WRITE, OpenOption.APPEND }, null, null, true);
    //
    //        deleteTestFile(file1);
    //
    //        // test with non-existing file and CREATE + WRITE + APPEND
    //        Path file2 = createNewTestFileName(testDir);
    //        test22_newByteChannel(file2, new OpenOption [] { OpenOption.CREATE, OpenOption.WRITE, OpenOption.APPEND }, data, null, false);
    //        test22_newByteChannel(file2, new OpenOption [] { OpenOption.OPEN, OpenOption.READ }, null, data, false);
    //        deleteTestFile(file2);
    //
    //        // test with non-existing file and OPEN + READ
    //        Path file3 = createNewTestFileName(testDir);
    //        test22_newByteChannel(file3, new OpenOption [] { OpenOption.OPEN, OpenOption.READ }, null, null, true);
    //
    //        // test with non-existing file and OPEN_OR_CREATE + WRITE + READ + APPEND
    //        Path file4 = createNewTestFileName(testDir);
    //        test22_newByteChannel(file4, new OpenOption [] { OpenOption.OPEN_OR_CREATE, OpenOption.WRITE, OpenOption.READ }, data, data, false);
    //
    //        // test with existing file and OPEN_OR_CREATE + WRITE + READ + APPEND
    //        test22_newByteChannel(file4, new OpenOption [] { OpenOption.OPEN_OR_CREATE, OpenOption.WRITE, OpenOption.APPEND }, data,
    //                null, false);
    //        test22_newByteChannel(file4, new OpenOption [] { OpenOption.OPEN, OpenOption.READ, }, null, data2, false);
    //
    //        deleteTestFile(file0);
    //        deleteTestFile(file4);
    //
    //        deleteTestDir(testDir);
    //
    //        if (config.supportsClose()) {
    //            // test with closed fs
    //            config.closeTestFileSystem(files,fs);
    //            test22_newByteChannel(file0, new OpenOption [] { OpenOption.OPEN_OR_CREATE, OpenOption.APPEND, OpenOption.READ },
    //                    null, null, true);
    //        }
    //
    //
    //    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: copy (synchronous)
    //
    // Possible parameters:
    //
    // Path null / non-existing file / existing empty file / existing non-empty file / existing dir / closed filesystem
    // CopyOptions  null / CREATE / REPLACE / IGNORE / APPEND / RESUME / VERIFY / ASYNCHRONOUS
    //
    // Total combinations : N
    //
    // Depends on:

    private void test23_copy(Path source, Path target, CopyOption[] options, byte[] expected, boolean mustFail) throws Exception {

        Copy copy;

        try {
            copy = files.copy(source, target, options);
        } catch (Exception e) {

            if (mustFail) {
                // expected
                return;
            }

            throwUnexpected("test23_copy", e);
        }

        if (mustFail) {
            throwExpected("test23_copy");
        }

        if (expected != null) {
            byte[] tmp = readFully(files.newInputStream(target));

            if (!Arrays.equals(expected, tmp)) {
                throwWrong("test23_copy", Arrays.toString(expected), Arrays.toString(tmp));
            }
        }
    }

    @org.junit.Test
    public void test23_copy() throws Exception {

        byte[] data = "Hello World!".getBytes();
        byte[] data2 = "Goodbye World!".getBytes();
        byte[] data3 = "Hello World!Goodbye World!".getBytes();
        byte[] data4 = "Hello World!Hello World!".getBytes();
        byte[] data5 = "Hello World!Hello World!Hello World!".getBytes();

        // test with null
        test23_copy(null, null, null, null, true);

        prepareTestDir("test23_copy");

        Path file0 = createTestFile(testDir, data);

        // test without target
        test23_copy(file0, null, new CopyOption[] { CopyOption.CREATE }, null, true);

        // test without source
        test23_copy(null, file0, new CopyOption[] { CopyOption.CREATE }, null, true);

        Path file1 = createNewTestFileName(testDir);
        Path file2 = createNewTestFileName(testDir);
        Path file3 = createNewTestFileName(testDir);

        Path file4 = createTestFile(testDir, data2);
        Path file5 = createTestFile(testDir, data3);

        Path dir0 = createTestDir(testDir);
        Path dir1 = createNewTestDirName(testDir);

        Path file6 = createNewTestFileName(dir1);

        // test copy with non-existing source
        test23_copy(file1, file2, new CopyOption[0], null, true);

        // test copy with dir source
        test23_copy(dir0, file1, new CopyOption[] { CopyOption.CREATE }, null, true);

        // test copy using conflicting options should fail
        test23_copy(file0, file1, new CopyOption[] { CopyOption.IGNORE, CopyOption.CREATE }, null, true);
        test23_copy(file0, file1, new CopyOption[] { CopyOption.CREATE, CopyOption.IGNORE }, null, true);
        test23_copy(file0, file1, new CopyOption[] { CopyOption.CREATE, CopyOption.REPLACE }, null, true);
        test23_copy(file0, file1, new CopyOption[] { CopyOption.CREATE, CopyOption.RESUME }, null, true);
        test23_copy(file0, file1, new CopyOption[] { CopyOption.CREATE, CopyOption.APPEND }, null, true);

        // test copy with non-existing target
        test23_copy(file0, file1, new CopyOption[] { CopyOption.CREATE }, data, false);
        test23_copy(file0, file2, new CopyOption[] { CopyOption.CREATE, CopyOption.CREATE }, data, false);

        // test copy with non-existing target with non-existing parent
        test23_copy(file0, file6, new CopyOption[] { CopyOption.CREATE }, null, true);

        // test copy with existing target
        test23_copy(file0, file1, new CopyOption[0], null, true);
        test23_copy(file0, file1, new CopyOption[] { CopyOption.CREATE }, null, true);

        // test copy with same target as source
        test23_copy(file0, file0, new CopyOption[] { CopyOption.CREATE }, data, false);

        // test ignore with existing target
        test23_copy(file4, file1, new CopyOption[] { CopyOption.IGNORE }, data, false);
        test23_copy(file4, file1, new CopyOption[] { CopyOption.IGNORE, CopyOption.IGNORE }, data, false);

        // test resume with existing target
        test23_copy(file4, file1, new CopyOption[] { CopyOption.RESUME, CopyOption.VERIFY }, null, true);
        test23_copy(file1, file5, new CopyOption[] { CopyOption.RESUME }, null, true);
        test23_copy(file5, file1, new CopyOption[] { CopyOption.RESUME, CopyOption.VERIFY }, data3, false);
        test23_copy(file5, file1, new CopyOption[] { CopyOption.RESUME }, data3, false);
        test23_copy(file5, file2, new CopyOption[] { CopyOption.RESUME, CopyOption.RESUME }, data3, false);
        test23_copy(file4, file1, new CopyOption[] { CopyOption.RESUME, CopyOption.VERIFY }, null, true);

        // test resume with non-existing source
        test23_copy(file3, file1, new CopyOption[] { CopyOption.RESUME, CopyOption.VERIFY }, null, true);

        // test resume with non-exising target
        test23_copy(file5, file3, new CopyOption[] { CopyOption.RESUME, CopyOption.VERIFY }, null, true);

        // test resume with dir source
        test23_copy(dir0, file1, new CopyOption[] { CopyOption.RESUME, CopyOption.VERIFY }, null, true);

        // test resume with dir target
        test23_copy(file5, dir0, new CopyOption[] { CopyOption.RESUME, CopyOption.VERIFY }, null, true);

        // test resume with same dir and target
        test23_copy(file5, file5, new CopyOption[] { CopyOption.RESUME, CopyOption.VERIFY }, data3, false);

        // test replace with existing target
        test23_copy(file0, file1, new CopyOption[] { CopyOption.REPLACE }, data, false);
        test23_copy(file0, file1, new CopyOption[] { CopyOption.REPLACE, CopyOption.REPLACE }, data, false);
        test23_copy(file0, file1, new CopyOption[] { CopyOption.REPLACE, CopyOption.VERIFY }, null, true);

        // test append with existing target
        test23_copy(file0, file1, new CopyOption[] { CopyOption.APPEND }, data4, false);
        test23_copy(file0, file1, new CopyOption[] { CopyOption.APPEND, CopyOption.APPEND }, data5, false);

        // test append with non-existing source
        test23_copy(file3, file1, new CopyOption[] { CopyOption.APPEND }, null, true);

        // test append with non-existing target
        test23_copy(file0, file3, new CopyOption[] { CopyOption.APPEND }, null, true);

        // test append with dir source
        test23_copy(dir0, file1, new CopyOption[] { CopyOption.APPEND }, null, true);

        // test append with dir target
        test23_copy(file0, dir0, new CopyOption[] { CopyOption.APPEND }, null, true);

        // test append with source equals target
        test23_copy(file0, file0, new CopyOption[] { CopyOption.APPEND }, null, true);

        // test with source equals target and empty option
        test23_copy(file0, file0, new CopyOption[] { null }, null, true);

        deleteTestDir(dir0);
        deleteTestFile(file5);
        deleteTestFile(file4);
        deleteTestFile(file2);
        deleteTestFile(file1);
        deleteTestFile(file0);
        deleteTestDir(testDir);

        closeTestFS();

    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: copy (asynchronous)
    //
    // Possible parameters:
    //
    // Path null / non-existing file / existing empty file / existing non-empty file / existing dir / closed filesystem
    // CopyOptions  null / CREATE / REPLACE / IGNORE / APPEND / RESUME / VERIFY / ASYNCHRONOUS
    //
    // Total combinations : N
    //
    // Depends on:

    @org.junit.Test
    public void test24_copy_async() throws Exception {

        byte[] data = "Hello World!".getBytes();

        prepareTestDir("test24_copy_async");
        Path file0 = createTestFile(testDir, data);
        Path file1 = createNewTestFileName(testDir);

        // Test the async copy
        Copy copy = files.copy(file0, file1, new CopyOption[] { CopyOption.CREATE, CopyOption.ASYNCHRONOUS });
        CopyStatus status = files.getCopyStatus(copy);

        while (!status.isDone()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignored
            }

            status = files.getCopyStatus(copy);
        }

        // Test the cancel
        copy = files.copy(file0, file1, new CopyOption[] { CopyOption.REPLACE, CopyOption.ASYNCHRONOUS });
        status = files.cancelCopy(copy);

        deleteTestFile(file1);
        deleteTestFile(file0);
        deleteTestDir(testDir);

    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: copy (synchronous)
    //
    // Possible parameters:
    //
    // Path null / non-existing file / existing empty file / existing non-empty file / existing dir / closed filesystem
    // CopyOptions  null / CREATE / REPLACE / IGNORE / APPEND / RESUME / VERIFY / ASYNCHRONOUS
    //
    // Total combinations : N
    //
    // Depends on:

    @org.junit.Test
    public void test25_getLocalCWD() throws Exception {

        if (config.supportsLocalCWD()) {

            try {
                Utils.getLocalCWD(files);
            } catch (Exception e) {
                throwUnexpected("test25_getLocalCWD", e);
            }

        }
    }

    @org.junit.Test
    public void test26_getLocalHomeFileSystem() throws Exception {

        if (config.supportsLocalHome()) {

            try {
                Utils.getLocalHome(files);
            } catch (Exception e) {
                throwUnexpected("test26_getLocalHomeFileSystem", e);
            }

        }
    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: move
    //
    // Possible parameters:
    //
    // source null / non-existing file / existing file / existing dir
    // target null / non-existing file / existing file / non-existing parent dir / existing dir
    // +  closed filesystem
    //
    // Total combinations :
    //
    // Depends on:

    private void test27_move(Path source, Path target, boolean mustFail) throws Exception {

        try {
            files.move(source, target);
        } catch (Exception e) {

            if (mustFail) {
                // expected
                return;
            }

            throwUnexpected("test27_move", e);
        }

        if (mustFail) {
            throwExpected("test27_move");
        }

        RelativePath sourceName = source.getRelativePath().normalize();
        RelativePath targetName = target.getRelativePath().normalize();

        if (sourceName.equals(targetName)) {
            // source == target, so the move did nothing.
            return;
        }

        // make sure the source no longer exists, and the target does exist
        if (files.exists(source)) {
            throwWrong("test27_move", "no source file", "source file");
        }

        if (!files.exists(target)) {
            throwWrong("test27_move", "target file", "no target file");
        }
    }

    @org.junit.Test
    public void test27_move() throws Exception {

        test27_move(null, null, true);

        prepareTestDir("test27_move");

        // test with non-existing source
        Path file0 = createNewTestFileName(testDir);
        Path file1 = createNewTestFileName(testDir);
        test27_move(file0, file1, true);

        // test with existing source, non-existing target
        Path file2 = createTestFile(testDir, null);
        test27_move(file2, file0, false);

        // test with existing source and target
        Path file3 = createTestFile(testDir, null);
        test27_move(file3, file0, true);

        // test file existing source, and target with non-existing parent
        Path dir0 = createNewTestDirName(testDir);
        Path file4 = createNewTestFileName(dir0);

        test27_move(file0, file4, true);

        // test with source equals target
        test27_move(file0, file0, false);

        deleteTestFile(file0);
        deleteTestFile(file3);

        // test with existing dir
        Path dir1 = createTestDir(testDir);
        test27_move(dir1, file1, false);

        deleteTestDir(file1);
        deleteTestDir(testDir);

        closeTestFS();

    }

    // ---------------------------------------------------------------------------------------------------------------------------
    // TEST: readSymbolicLink
    //
    // Possible parameters:
    //
    // link null / non-existing file / existing file / existing dir / existing link / broken link / closed filesystem
    //
    // Total combinations : 7
    //
    // Depends on:

    private void test28_readSymbolicLink(Path link, Path expected, boolean mustFail) throws Exception {

        Path target = null;

        try {
            target = files.readSymbolicLink(link);
        } catch (Exception e) {

            if (mustFail) {
                // expected
                return;
            }

            throwUnexpected("test28_readSymboliclink", e);
        }

        if (mustFail) {
            throwExpected("test28_readSymbolicLink");
        }

        // make sure the target is what was expected
        if (expected != null && !target.equals(expected)) {
            throwWrong("test28_readSymbolicLink", expected, target);
        }
    }

    @org.junit.Test
    public void test28_readSymbolicLink() throws Exception {

        if (!config.supportsSymboliclinks()) {
            return;
        }

        // test with null
        test28_readSymbolicLink(null, null, true);

        prepareTestDir("test28_readSybmolicLink");

        // test with non-exising file
        Path file0 = createNewTestFileName(testDir);
        test28_readSymbolicLink(file0, null, true);

        // test with existing file
        Path file1 = createTestFile(testDir, null);
        test28_readSymbolicLink(file1, null, true);
        deleteTestFile(file1);

        // test with existing dir
        Path dir0 = createTestDir(testDir);
        test28_readSymbolicLink(dir0, null, true);

        deleteTestDir(dir0);
        deleteTestDir(testDir);

        closeTestFS();

    }

    @org.junit.Test
    public void test29_readSymbolicLink() throws Exception {

        if (!config.supportsSymboliclinks()) {
            return;
        }

        Path cwd = config.getWorkingDir(files, credentials);

        // Use external test dir with is assumed to be in fs.getEntryPath().resolve("xenon_test/links");
        Path root = resolve(cwd, "xenon_test/links");

        if (!files.exists(root)) {
            throw new Exception("Cannot find symbolic link test dir at " + root);
        }

        // prepare the test files
        Path file0 = resolve(root, "file0"); // exists
        Path file1 = resolve(root, "file1"); // exists
        Path file2 = resolve(root, "file2"); // does not exist

        // prepare the test links
        Path link0 = resolve(root, "link0"); // points to file0 (contains text)
        Path link1 = resolve(root, "link1"); // points to file1 (is empty)
        Path link2 = resolve(root, "link2"); // points to non-existing file2
        Path link3 = resolve(root, "link3"); // points to link0 which points to file0 (contains text)
        Path link4 = resolve(root, "link4"); // points to link2 which points to non-existing file2
        Path link5 = resolve(root, "link5"); // points to link6 (circular)
        Path link6 = resolve(root, "link6"); // points to link5 (circular)

        // link0 should point to file0
        test28_readSymbolicLink(link0, file0, false);

        // link1 should point to file1
        test28_readSymbolicLink(link1, file1, false);

        // link2 should point to file2 which fails
        test28_readSymbolicLink(link2, file2, false);

        // link3 should point to link0 which points to file0
        test28_readSymbolicLink(link3, link0, false);

        // link4 should point to link2 which points to file2
        test28_readSymbolicLink(link4, link2, false);

        // link5 should point to link6 which points to link5
        test28_readSymbolicLink(link5, link6, false);

        // link6 should point to link5 which points to link6
        test28_readSymbolicLink(link6, link5, false);

    }

    //    @org.junit.Test
    //    public void test30_isSymbolicLink() throws Exception {
    //
    //
    //
    //        FileSystem fs = config.getTestFileSystem(files, credentials);
    //
    //        // Use external test dir with is assumed to be in fs.getEntryPath().resolve("xenon_test/links");
    //        Path root = fs.getEntryPath().resolve(new RelativePath("xenon_test/links"));
    //
    //        if (!files.exists(root)) {
    //            throw new Exception("Cannot find symbolic link test dir at " + root.getPath());
    //        }
    //
    //        // prepare the test files
    //        boolean v = files.isSymbolicLink(root.resolve(new RelativePath("file0")));
    //        assertFalse(v);
    //
    //        v = files.isSymbolicLink(root.resolve(new RelativePath("link0")));
    //        assertTrue(v);
    //
    //        v = files.isSymbolicLink(root.resolve(new RelativePath("file2")));
    //        assertFalse(v);
    //
    //
    //    }

    @org.junit.Test
    public void test31_newDirectoryStreamWithBrokenLinks() throws Exception {

        if (!config.supportsSymboliclinks()) {
            return;
        }

        Path cwd = config.getWorkingDir(files, credentials);

        // Use external test dir with is assumed to be in fs.getEntryPath().resolve("xenon_test/links");
        Path root = resolve(cwd, "xenon_test/links");

        if (!files.exists(root)) {
            throw new Exception("Cannot find symbolic link test dir at " + root);
        }

        // prepare the test files
        Path file0 = resolve(root, "file0"); // exists
        Path file1 = resolve(root, "file1"); // exists

        // prepare the test links
        Path link0 = resolve(root, "link0"); // points to file0 (contains text)
        Path link1 = resolve(root, "link1"); // points to file1 (is empty)
        Path link2 = resolve(root, "link2"); // points to non-existing file2
        Path link3 = resolve(root, "link3"); // points to link0 which points to file0 (contains text)
        Path link4 = resolve(root, "link4"); // points to link2 which points to non-existing file2
        Path link5 = resolve(root, "link5"); // points to link6 (circular)
        Path link6 = resolve(root, "link6"); // points to link5 (circular)

        Set<Path> tmp = new HashSet<Path>();
        tmp.add(file0);
        tmp.add(file1);
        tmp.add(link0);
        tmp.add(link1);
        tmp.add(link2);
        tmp.add(link3);
        tmp.add(link4);
        tmp.add(link5);
        tmp.add(link6);

        test11_newDirectoryStream(root, tmp, false);

    }

    @org.junit.Test
    public void test32_newAttributesDirectoryStreamWithBrokenLinks() throws Exception {

        if (!config.supportsSymboliclinks()) {
            return;
        }

        Path cwd = config.getWorkingDir(files, credentials);

        // Use external test dir with is assumed to be in fs.getEntryPath().resolve("xenon_test/links");
        Path root = resolve(cwd, "xenon_test/links");

        if (!files.exists(root)) {
            throw new Exception("Cannot find symbolic link test dir at " + root);
        }

        // prepare the test files
        Path file0 = resolve(root, "file0"); // exists
        Path file1 = resolve(root, "file1"); // exists

        // prepare the test links
        Path link0 = resolve(root, "link0"); // points to file0 (contains text)
        Path link1 = resolve(root, "link1"); // points to file1 (is empty)
        Path link2 = resolve(root, "link2"); // points to non-existing file2
        Path link3 = resolve(root, "link3"); // points to link0 which points to file0 (contains text)
        Path link4 = resolve(root, "link4"); // points to link2 which points to non-existing file2
        Path link5 = resolve(root, "link5"); // points to link6 (circular)
        Path link6 = resolve(root, "link6"); // points to link5 (circular)

        Set<PathAttributesPair> tmp = new HashSet<PathAttributesPair>();
        tmp.add(new PathAttributesPairImplementation(file0, files.getAttributes(file0)));
        tmp.add(new PathAttributesPairImplementation(file1, files.getAttributes(file1)));
        tmp.add(new PathAttributesPairImplementation(link0, files.getAttributes(link0)));
        tmp.add(new PathAttributesPairImplementation(link1, files.getAttributes(link1)));
        tmp.add(new PathAttributesPairImplementation(link2, files.getAttributes(link2)));
        tmp.add(new PathAttributesPairImplementation(link3, files.getAttributes(link3)));
        tmp.add(new PathAttributesPairImplementation(link4, files.getAttributes(link4)));
        tmp.add(new PathAttributesPairImplementation(link5, files.getAttributes(link5)));
        tmp.add(new PathAttributesPairImplementation(link6, files.getAttributes(link6)));

        test15_newAttributesDirectoryStream(root, tmp, false);

    }

    /*
    public Path readSymbolicLink(Path link) throws XenonException;

    public boolean isSymbolicLink(Path path) throws XenonException;


     */

    @org.junit.Test
    public void test33_multipleFileSystemsOpenSimultaneously() throws Exception {

        // Open two file systems. They should both be open afterwards.
        FileSystem fs0 = files.newFileSystem(config.getScheme(), config.getCorrectLocation(),
                config.getDefaultCredential(credentials), null);
        FileSystem fs1 = files.newFileSystem(config.getScheme(), config.getCorrectLocation(),
                config.getDefaultCredential(credentials), null);
        assert (files.isOpen(fs0));
        assert (files.isOpen(fs1));

        // Close them both. We should get no exceptions.
        files.close(fs0);
        files.close(fs1);

    }

}
