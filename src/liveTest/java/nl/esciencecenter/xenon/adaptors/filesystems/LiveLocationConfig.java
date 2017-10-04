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

import static nl.esciencecenter.xenon.utils.LocalFileSystemUtils.getLocalRootlessPath;

import java.util.AbstractMap;
import java.util.Map;

import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;

public class LiveLocationConfig extends LocationConfig {
    private final FileSystem fileSystem;

    public LiveLocationConfig(FileSystem fileSystem) {
        super();
        this.fileSystem = fileSystem;
    }

    // TODO the paths should be relative to the filesystem.getEntryPath()
    private Path createPath(String... path) {
        String baseDir = System.getProperty("xenon.filesystem.basedir");
        char sep = Path.DEFAULT_SEPARATOR;

        if (System.getProperty("xenon.separator") != null) {
            sep = System.getProperty("xenon.separator").charAt(0);
        }

        if (baseDir == null) {
            return fileSystem.getWorkingDirectory().resolve(new Path(sep, false, path));
        }

        return new Path(sep, baseDir).resolve(new Path(sep, false, path));
    }

    @Override
    public Path getExistingPath() {
        return createPath("filesystem-test-fixture", "links", "file0");
    }

    @Override
    public Map.Entry<Path, Path> getSymbolicLinksToExistingFile() {
        return new AbstractMap.SimpleEntry<>(createPath("filesystem-test-fixture", "links", "link0"), createPath("filesystem-test-fixture", "links", "file0"));
    }

    @Override
    public Path getWritableTestDir() {
        String baseDir = System.getProperty("xenon.filesystem.basedir");
        if (baseDir == null) {
            return fileSystem.getWorkingDirectory();
        }
        char sep = Path.DEFAULT_SEPARATOR;
        if (System.getProperty("xenon.separator") != null) {
            sep = System.getProperty("xenon.separator").charAt(0);
        }
        return new Path(sep, baseDir);
    }

    @Override
    public Path getExpectedWorkingDirectory() {

        String baseDir = System.getProperty("xenon.filesystem.expected.workdir");

        if (baseDir == null) {
            baseDir = getLocalRootlessPath(System.getProperty("user.dir"));
        }

        if (baseDir == null) {
            throw new RuntimeException("Workdir and user dir property not set. Current working directory not known!");
        }

        char sep = Path.DEFAULT_SEPARATOR;
        if (System.getProperty("xenon.separator") != null) {
            sep = System.getProperty("xenon.separator").charAt(0);
        }

        return new Path(sep, baseDir);
    }
}
