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
package nl.esciencecenter.xenon.adaptors.filesystems.local;

import static nl.esciencecenter.xenon.utils.LocalFileSystemUtils.isWindows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import nl.esciencecenter.xenon.InvalidCredentialException;
import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.filesystems.FileSystemTestParent;
import nl.esciencecenter.xenon.adaptors.filesystems.LocationConfig;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.DirectoryNotEmptyException;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.NoSuchPathException;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.filesystems.PathAttributes;
import nl.esciencecenter.xenon.filesystems.PosixFilePermission;

public class LocalFileSystemTest extends FileSystemTestParent {
    @Override
    protected LocationConfig setupLocationConfig(FileSystem fileSystem) {
        return new LocationConfig() {
            @Override
            public Path getExistingPath() {
                return new Path("/home/xenon/filesystem-test-fixture/links/file0");
            }

            @Override
            public Map.Entry<Path, Path> getSymbolicLinksToExistingFile() {
                return new AbstractMap.SimpleEntry<>(new Path("/home/xenon/filesystem-test-fixture/links/link0"),
                        new Path("/home/xenon/filesystem-test-fixture/links/file0"));
            }

            @Override
            public Path getWritableTestDir() {
                return new Path("/tmp");
            }

            @Override
            public Path getExpectedEntryPath() {
                return new Path(System.getProperty("user.dir"));
            }
        };
    }

    @Override
    public FileSystem setupFileSystem() throws XenonException {
        return FileSystem.create("file");
    }

    @Test
    public void test_credential_default() throws XenonException {
        FileSystem.create("file", null, new DefaultCredential()).close();
    }

    @Test(expected = InvalidCredentialException.class)
    public void test_credential_wrong() throws XenonException {
        FileSystem.create("file", null, new PasswordCredential("aap", "noot".toCharArray()));
    }

    @Test
    public void test_location_empty() throws XenonException {
        FileSystem.create("file", "").close();
    }

    @Test
    public void test_location_localRoot() throws XenonException {
        FileSystem.create("file", "/").close();
    }

    @Test(expected = InvalidLocationException.class)
    public void test_location_wrong() throws XenonException {
        FileSystem.create("file", "aap").close();
    }

    @Test(expected = NoSuchPathException.class)
    public void test_deleteLocal_doesNotExist() throws Exception {
        LocalFileSystem f = (LocalFileSystem) fileSystem;
        Path doesNotExist = testRoot.resolve("foobar");
        f.deleteLocal(doesNotExist);
    }

    @Test(expected = DirectoryNotEmptyException.class)
    public void test_deleteLocal_dirNotEmpty() throws Exception {
        LocalFileSystem f = (LocalFileSystem) fileSystem;
        generateAndCreateTestDir();
        f.deleteLocal(testRoot);
    }

    @Test(expected = XenonException.class)
    public void test_deleteLocal_notAllowed() throws Exception {
        LocalFileSystem f = (LocalFileSystem) fileSystem;
        Path notAllowed = new Path("/dev/null");
        f.deleteLocal(notAllowed);
    }

    @Test
    public void test_javaPath_tilde_withPath() throws Exception {
        LocalFileSystem f = (LocalFileSystem) fileSystem;
        Path tilde = new Path("~/filesystem-test-fixture/links/file0");
        assertEquals(locationConfig.getExistingPath().toString(), f.javaPath(tilde).toString());
    }

    @Test
    public void test_javaPath_tilde() throws Exception {
        LocalFileSystem f = (LocalFileSystem) fileSystem;
        assertEquals("/home/xenon", f.javaPath(new Path("~")).toString());
    }

    @Test
    public void test_javaPath_empty() throws Exception {
        LocalFileSystem f = (LocalFileSystem) fileSystem;
        assertEquals("/", f.javaPath(new Path("")).toString());
    }

    @Test(expected = XenonException.class)
    public void test_getAttributes_nonExisting() throws Exception {
        fileSystem.getAttributes(testRoot.resolve("foo"));
    }

    @Test(expected = XenonException.class)
    public void test_list_nonExisting() throws Exception {
        fileSystem.list(testRoot.resolve("foo"), true);
    }

    @Test(expected = XenonException.class)
    public void test_writeNotAllowed() throws Exception {
        fileSystem.writeToFile(new Path("/dev/foo"));
    }

    @Test(expected = XenonException.class)
    public void test_appendNotAllowed() throws Exception {
        fileSystem.appendToFile(new Path("/dev/foo"));
    }

    @Test(expected = XenonException.class)
    public void test_rename_invalidSource() throws Exception {
        fileSystem.rename(new Path("/dev/null"), testRoot.resolve("foo"));
    }

    @Test(expected = XenonException.class)
    public void test_createDirectory_invalidPath() throws Exception {
        fileSystem.createDirectory(new Path("/dev/foo"));
    }

    @Test(expected = XenonException.class)
    public void test_createFile_invalidPath() throws Exception {
        fileSystem.createFile(new Path("/dev/foo"));
    }

    @Test(expected = XenonException.class)
    public void test_createSymbolicLink_invalidPath() throws Exception {
        fileSystem.createSymbolicLink(new Path("/dev/foo"), new Path("/dev/null"));
    }

    public void test_setPermissionsNotAllowed() throws Exception {
        Set<PosixFilePermission> permissions = new HashSet<>();
        permissions.add(PosixFilePermission.GROUP_EXECUTE);
        fileSystem.setPosixFilePermissions(new Path("/dev/null"), permissions);
    }

    @Test
    public void test_getAttributes_fileStartingWithDot_HiddenFile() throws Exception {
        // TODO move to FileSystemTestParent when we can detect
        // adaptor/filesystem supports hidden files
        generateAndCreateTestDir();
        // assumes location has UNIX-like file system where starts with '.'
        // means hidden
        Path path = testDir.resolve(".myhiddenfile");
        fileSystem.createFile(path);

        PathAttributes result = fileSystem.getAttributes(path);

        assertTrue(result.isHidden());
    }

    @Test
    public void test_list_hiddenFile() throws Exception {
        assumeFalse(isWindows());
        generateAndCreateTestDir();
        // assumes location has UNIX-like file system where starts with '.'
        // means hidden
        Path path = testDir.resolve(".myhiddenfile");
        fileSystem.createFile(path);

        Set<PathAttributes> res = listSet(testDir, false);

        assertTrue("Listing contains hidden file", res.stream().anyMatch(PathAttributes::isHidden));
    }

    @Test
    public void test_exists_existingDot_returnTrue() throws Exception {
        testDir = new Path(".");

        assertTrue(fileSystem.exists(testDir));
    }

    @Test
    public void test_exists_existingDoubleDot_returnTrue() throws Exception {
        testDir = new Path("..");

        assertTrue(fileSystem.exists(testDir));
    }
}
