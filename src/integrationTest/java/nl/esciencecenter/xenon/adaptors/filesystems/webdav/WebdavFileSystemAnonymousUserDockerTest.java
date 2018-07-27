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
package nl.esciencecenter.xenon.adaptors.filesystems.webdav;

import java.util.HashMap;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;

import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.InvalidPropertyException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.filesystems.LocationConfig;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;

public class WebdavFileSystemAnonymousUserDockerTest extends WebdavFileSystemTestParent {

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder().file("src/integrationTest/resources/docker-compose/webdav.yml")
            .waitingForService("webdav", HealthChecks.toHaveAllPortsOpen()).saveLogsTo("/tmp/webdav.txt").build();

    @Override
    protected LocationConfig setupLocationConfig(FileSystem fileSystem) {
        return new LocationConfig() {

            @Override
            public Map.Entry<Path, Path> getSymbolicLinksToExistingFile() {
                throw new Error("Symlinks not supported on webdav");
            }

            @Override
            public Path getExistingPath() {
                return new Path("/downloads/filesystem-test-fixture/links/file0");
            }

            @Override
            public Path getWritableTestDir() {
                // return fileSystem.getWorkingDirectory().resolve("uploads");
                return new Path("/uploads");
            }

            @Override
            public Path getExpectedWorkingDirectory() {
                return new Path("/");
            }
        };
    }

    @Override
    public FileSystem setupFileSystem() throws XenonException {
        String location = docker.containers().container("webdav").port(80).inFormat("http://$HOST:$EXTERNAL_PORT/");
        return FileSystem.create("webdav", location);
    }

    @Test(expected = InvalidPropertyException.class)
    public void createFileSystem_bufferSize_underflow() throws XenonException {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("xenon.adaptors.filesystems.webdav.bufferSize", "0");
        FileSystem.create("webdav", "http://localhost", new DefaultCredential(), properties);
    }

    @Test(expected = InvalidPropertyException.class)
    public void createFileSystem_bufferSize_overflow() throws XenonException {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("xenon.adaptors.filesystems.webdav.bufferSize", Long.toString(Long.MAX_VALUE));
        FileSystem.create("webdav", "http://localhost", new DefaultCredential(), properties);
    }

    @Test(expected = InvalidLocationException.class)
    public void createFileSystem_illegal_location() throws XenonException {
        FileSystem.create("webdav", ":/bla");
    }
}
