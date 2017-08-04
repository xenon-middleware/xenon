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

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.filesystems.FileSystemTestParent;
import nl.esciencecenter.xenon.adaptors.filesystems.LocationConfig;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.filesystems.PathAttributes;
import org.junit.Test;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import static nl.esciencecenter.xenon.utils.LocalFileSystemUtils.isWindows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

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
                return new AbstractMap.SimpleEntry<>(
                    new Path("/home/xenon/filesystem-test-fixture/links/link0"),
                    new Path("/home/xenon/filesystem-test-fixture/links/file0")
                );
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
    public void test_getAttributes_fileStartingWithDot_HiddenFile() throws Exception {
        // TODO move to FileSystemTestParent when we can detect adaptor/filesystem supports hidden files
        generateAndCreateTestDir();
        // assumes location has UNIX-like file system where starts with '.' means hidden
        Path path = testDir.resolve(".myhiddenfile");
        fileSystem.createFile(path);

        PathAttributes result = fileSystem.getAttributes(path);

        assertTrue(result.isHidden());
    }

    @Test
    public void test_list_hiddenFile() throws Exception {
        assumeFalse(isWindows());
        generateAndCreateTestDir();
        // assumes location has UNIX-like file system where starts with '.' means hidden
        Path path = testDir.resolve(".myhiddenfile");
        fileSystem.createFile(path);

        Set<PathAttributes> res = listSet(testDir, false);

        assertTrue("Listing contains hidden file", res.stream().anyMatch(PathAttributes::isHidden));
    }
}
