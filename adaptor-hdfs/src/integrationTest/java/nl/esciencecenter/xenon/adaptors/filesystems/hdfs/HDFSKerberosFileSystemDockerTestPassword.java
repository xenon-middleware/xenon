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
package nl.esciencecenter.xenon.adaptors.filesystems.hdfs;

import static nl.esciencecenter.xenon.adaptors.filesystems.hdfs.HDFSFileAdaptor.HADOOP_SETTINGS_FILE;

import java.util.HashMap;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.filesystems.FileSystemTestInfrastructure;
import nl.esciencecenter.xenon.adaptors.filesystems.LocationConfig;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.CopyMode;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;

public class HDFSKerberosFileSystemDockerTestPassword extends FileSystemTestInfrastructure {

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder().file("src/integrationTest/resources/docker-compose/hdfs-kerberos.yml")
            .waitingForService("hdfs", HealthChecks.toHaveAllPortsOpen()).build();

    @Override
    protected LocationConfig setupLocationConfig(FileSystem fileSystem) {
        return new LocationConfig() {

            @Override
            public Map.Entry<Path, Path> getSymbolicLinksToExistingFile() {
                // TODO: fix me
                throw new Error("Symlinks not yet supported on HDFS");
            }

            @Override
            public Path getExistingPath() {
                return new Path("/filesystem-test-fixture/links/file0");
            }

            @Override
            public Path getWritableTestDir() {
                return fileSystem.getWorkingDirectory();
            }

            @Override
            public Path getExpectedWorkingDirectory() {
                return new Path("/filesystem-test-fixture");
            }
        };
    }

    @Test
    public void test_copy_rec_kerberos() throws Throwable {
        byte[] data = "Hello World!".getBytes();
        byte[] data2 = "Party people!".getBytes();
        byte[] data3 = "yes | rm -rf ".getBytes();
        byte[] data4 = "Use Xenon!".getBytes();
        generateAndCreateTestDir();

        Path source = createTestSubDir(testDir);
        createTestFile(source, data);
        Path testSubDir = createTestSubDir(source);

        Path testSubDir2 = createTestSubDir(source);
        createTestFile(testSubDir2, data2);
        createTestFile(testSubDir, data3);

        Path testSubSub = createTestSubDir(testSubDir);
        createTestFile(testSubSub, data4);

        Path target = createTestSubDirName(testDir);
        copySync(source, target, CopyMode.CREATE, true);
        assertSameContentsDir(source, target);
    }

    public FileSystem setupFileSystem() throws XenonException {
        String location = docker.containers().container("hdfs").port(8020).inFormat("localhost:$EXTERNAL_PORT");

        System.setProperty("java.security.krb5.conf", "src/integrationTest/resources/kerberos/krb5.conf");
        Map<String, String> props = new HashMap<>();
        props.put(HADOOP_SETTINGS_FILE, "src/integrationTest/resources/core-site-kerberos.xml");
        Credential c = new PasswordCredential("xenon@esciencecenter.nl", "javagat".toCharArray());
        FileSystem fs = FileSystem.create("hdfs", location, c, props);
        fs.setWorkingDirectory(new Path("/filesystem-test-fixture"));
        return fs;
    }

}
