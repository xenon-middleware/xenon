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
package nl.esciencecenter.xenon.adaptors.filesystems.sftp;

import nl.esciencecenter.xenon.adaptors.filesystems.FileSystemTestParent;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.filesystems.PathAttributes;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Extend this class for docker tests and live tests
 *
 * Can contain extra tests for sftp filesystem
 */
public abstract class SftpFileSystemTestParent extends FileSystemTestParent {
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
        generateAndCreateTestDir();
        // assumes location has UNIX-like file system where starts with '.' means hidden
        Path path = testDir.resolve(".myhiddenfile");
        fileSystem.createFile(path);

        Set<PathAttributes> res = listSet(testDir, false);

        assertTrue("Listing contains hidden file", res.stream().anyMatch(PathAttributes::isHidden));
    }
}
