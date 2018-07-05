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

import java.util.Map;

import org.junit.ClassRule;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.filesystems.LocationConfig;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;

public class WebdavFileSystemSecureXenonUserDockerTest extends WebdavFileSystemTestParent {

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
                return new Path("/~xenon/filesystem-test-fixture/links/file0");
            }

            @Override
            public Path getWritableTestDir() {
                return new Path("/~xenon/uploads");
            }

            @Override
            public Path getExpectedWorkingDirectory() {
                return new Path("/~xenon");
            }
        };
    }

    @Override
    public FileSystem setupFileSystem() throws XenonException {
        String location = docker.containers().container("webdav").port(443).inFormat("https://$HOST:$EXTERNAL_PORT/~xenon");
        PasswordCredential cred = new PasswordCredential("xenon", "javagat".toCharArray());
        return FileSystem.create("webdav", location, cred);
    }

}
