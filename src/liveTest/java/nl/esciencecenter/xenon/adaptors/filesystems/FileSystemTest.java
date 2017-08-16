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

import static nl.esciencecenter.xenon.adaptors.Utils.buildCredential;
import static nl.esciencecenter.xenon.adaptors.Utils.buildProperties;
import static org.junit.Assume.assumeFalse;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;

public class FileSystemTest extends FileSystemTestParent {
    @BeforeClass
    static public void skipIfNotRequested() {
        String name = System.getProperty("xenon.filesystem");
        assumeFalse("Ignoring filesystem test, 'xenon.filesystem' system property not set", name == null);
    }

    @Override
    protected LocationConfig setupLocationConfig(FileSystem fileSystem) {
        return new LiveLocationConfig(fileSystem);
    }

    @Override
    public FileSystem setupFileSystem() throws XenonException {
        String name = System.getProperty("xenon.filesystem");
        String location = System.getProperty("xenon.filesystem.location");
        Credential cred = buildCredential();
        Map<String, String> props = buildProperties(FileAdaptor.ADAPTORS_PREFIX + System.getProperty("xenon.filesystem"));
        return FileSystem.create(name, location, cred, props);
    }

    @Test
    public void test_copy_existingTarget_replace_windows() throws Throwable {
        byte[] data = "Hello World!".getBytes();
        byte[] data2 = "Something else!".getBytes();
        generateAndCreateTestDir();
        Path file0 = createTestFile(testDir, data);
        Path file1 = createTestFile(testDir, data2);
        // copySync(file0, file1, CopyMode.REPLACE, false);
        assertSameContents(file0, file1);
    }
}
