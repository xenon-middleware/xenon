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
package nl.esciencecenter.xenon.adaptors.filesystems.gridftp;

import java.net.InetAddress;
import java.util.AbstractMap;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.filesystems.LocationConfig;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;

public class GridFTPFileSystemDockerTest extends GridFTPFileSystemTestParent {

    // @ClassRule
    // public static DockerComposeRule docker = DockerComposeRule.builder().file("src/integrationTest/resources/docker-compose/ftp.yml")
    // .waitingForService("ftp", HealthChecks.toHaveAllPortsOpen()).build();

    @Override
    protected LocationConfig setupLocationConfig(FileSystem fileSystem) {

        return new LocationConfig() {
            @Override
            public Path getExistingPath() {
                return new Path("/home/xenon/filesystem-test-fixture/links/file0");
            }

            @Override
            public Map.Entry<Path, Path> getSymbolicLinksToExistingFile() {
                return new AbstractMap.SimpleEntry<>(new Path("/home/xenon/filesystem-test-fixture/links/link0"),
                        new Path("/home/xenon/filesystem-test-fixture/links/file0"));
            }

            @Override
            public Path getWritableTestDir() {
                return fileSystem.getWorkingDirectory();
            }

            @Override
            public Path getExpectedWorkingDirectory() {
                return new Path("/home/xenon");
            }
        };
    }

    @Override
    public FileSystem setupFileSystem() throws XenonException {
        // String location = docker.containers().container("ftp").port(21).inFormat("$HOST:$EXTERNAL_PORT");
        // PasswordCredential cred = new PasswordCredential("xenon", "javagat".toCharArray());

        String cname = "gridftp1.xenontest.nlesc.nl";

        try {
            System.out.println("HOST resolves to: " + InetAddress.getByName(cname));
            System.out.println("HOST resolves to: " + InetAddress.getByName(cname).getHostName());
            System.out.println("HOST resolves to: " + InetAddress.getByName(cname).getCanonicalHostName());
        } catch (Exception e) {
            // TODO: handle exception
        }

        System.out.println("CREATE FILESYSTEM");

        String location = "gsiftp://gridftp1.xenontest.nlesc.nl:2811/home/xenon";

        Credential cred = new DefaultCredential();

        FileSystem f = FileSystem.create("gridftp", location, cred);

        return f;
    }
}
