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
package nl.esciencecenter.xenon.adaptors.shared.ssh;

import org.apache.sshd.client.SshClient;
import org.junit.ClassRule;
import org.junit.Test;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;

import nl.esciencecenter.xenon.credentials.CredentialMap;
import nl.esciencecenter.xenon.credentials.PasswordCredential;

public class SshTunnelDockerTest {

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder().file("src/integrationTest/resources/docker-compose/ssh-tunnel.yml")
            .waitingForService("ssh1", HealthChecks.toHaveAllPortsOpen()).build();

    public String getLocation() {
        return docker.containers().container("ssh1").port(22).inFormat("$HOST:$EXTERNAL_PORT");
    }

    @Test
    public void test_no_tunnel() throws Exception {
        SshClient client = SSHUtil.createSSHClient(false, false, false, false, false);

        SSHConnection session = SSHUtil.connect("test", client, getLocation(), new PasswordCredential("xenon", "javagat".toCharArray()), 0, 10 * 1000);
        session.close();
    }

    @Test
    public void test_tunnel_one_hop() throws Exception {
        SshClient client = SSHUtil.createSSHClient(false, false, false, false, false);

        String location = "ssh2 via:" + getLocation();

        CredentialMap map = new CredentialMap();
        map.put("ssh2", new PasswordCredential("xenon2", "javagat2".toCharArray()));
        map.put(getLocation(), new PasswordCredential("xenon", "javagat".toCharArray()));

        SSHConnection session = SSHUtil.connect("test", client, location, map, 0, 10 * 1000);
        session.close();
    }

    @Test
    public void test_tunnel_two_hop() throws Exception {
        SshClient client = SSHUtil.createSSHClient(false, false, false, false, false);

        String location = "ssh3 via:ssh2 via:" + getLocation();

        CredentialMap map = new CredentialMap();
        map.put("ssh3", new PasswordCredential("xenon", "javagat".toCharArray()));
        map.put("ssh2", new PasswordCredential("xenon2", "javagat2".toCharArray()));
        map.put(getLocation(), new PasswordCredential("xenon", "javagat".toCharArray()));

        SSHConnection session = SSHUtil.connect("test", client, location, map, 0, 10 * 1000);
        session.close();
    }

    public static void main(String[] args) throws Exception {
        new SshTunnelDockerTest().test_tunnel_one_hop();
    }
}
