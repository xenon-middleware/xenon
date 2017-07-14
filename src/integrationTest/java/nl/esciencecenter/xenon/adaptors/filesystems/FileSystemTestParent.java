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

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.FileSystemAdaptorDescription;
import nl.esciencecenter.xenon.filesystems.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class FileSystemTestParent {
    private FileSystem fileSystem;
    private FileSystemAdaptorDescription description;
    private LocationConfig locationConfig;

    @Before
    public void setup() throws XenonException {
        fileSystem = setupFileSystem();
        description = setupDescription();
        locationConfig = setupLocationConfig(fileSystem);
    }

    protected abstract LocationConfig setupLocationConfig(FileSystem fileSystem);

    @After
    public void cleanup() throws XenonException {
        fileSystem.close();
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
}
