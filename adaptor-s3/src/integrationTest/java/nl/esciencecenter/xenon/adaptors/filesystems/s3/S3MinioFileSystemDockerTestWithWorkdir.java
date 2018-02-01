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
package nl.esciencecenter.xenon.adaptors.filesystems.s3;

import java.util.HashMap;
import java.util.Map;

import org.junit.ClassRule;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.filesystems.LocationConfig;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;

public class S3MinioFileSystemDockerTestWithWorkdir extends S3FileSystemTestParent {

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder().file("src/integrationTest/resources/docker-compose/minio.yml")
            .waitingForService("minio", HealthChecks.toHaveAllPortsOpen()).saveLogsTo("/var/tmp/bla").build();

    @Override
    protected LocationConfig setupLocationConfig(FileSystem fileSystem) {
        return new LocationConfig() {

            @Override
            public Map.Entry<Path, Path> getSymbolicLinksToExistingFile() {
                throw new Error("Symlinks not supported on S3");
            }

            @Override
            public Path getExistingPath() {
                return new Path("file0");
            }

            @Override
            public Path getWritableTestDir() {
                return fileSystem.getWorkingDirectory();
            }

            @Override
            public Path getExpectedWorkingDirectory() {
                return new Path("/links");
            }
        };
    }

    @Override
    public FileSystem setupFileSystem() throws XenonException {
        String location = docker.containers().container("minio").port(9000).inFormat("http://localhost:$EXTERNAL_PORT/filesystem-test-fixture/links");
        PasswordCredential cred = new PasswordCredential("xenon", "javagat01".toCharArray());
        Map<String, String> props = new HashMap<>();
        return FileSystem.create("s3", location, cred, props);
    }
}
