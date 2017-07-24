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
package nl.esciencecenter.xenon.adaptors.filesystems.local;

import java.util.AbstractMap;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.filesystems.FileSystemTestParent;
import nl.esciencecenter.xenon.adaptors.filesystems.LocationConfig;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;

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
				return new Path("/home/xenon");
			}
        };
    }

    @Override
    public FileSystem setupFileSystem() throws XenonException {
        return FileSystem.create("file");
    }
}
