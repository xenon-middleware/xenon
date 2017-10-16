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
import org.apache.sshd.client.session.ClientSession;
import org.junit.ClassRule;
import org.junit.Test;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;

import nl.esciencecenter.xenon.credentials.CertificateCredential;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;

public class SSHUtilPasswordCredentialDockerTest {

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder().file("src/integrationTest/resources/docker-compose/openssh.yml")
            .waitingForService("ssh", HealthChecks.toHaveAllPortsOpen()).build();

    @Test
    public void test_connect_default() throws Exception {

        String location = docker.containers().container("ssh").port(22).inFormat("$HOST:$EXTERNAL_PORT");

        SshClient client = SSHUtil.createSSHClient(false, false, false, false, false);

        // Create a certificate credential that matches the (hard-coded) setup of the docker container.
        Credential credential = new DefaultCredential("xenon");

        ClientSession session = SSHUtil.connect("SSHTEST", client, location, credential, 10 * 1000);
        session.close();
    }

    @Test
    public void test_connect_rsa() throws Exception {

        String location = docker.containers().container("ssh").port(22).inFormat("$HOST:$EXTERNAL_PORT");

        SshClient client = SSHUtil.createSSHClient(false, false, false, false, false);

        // Create a certificate credential that matches the (hard-coded) setup of the docker container.
        Credential credential = new CertificateCredential("xenon", "/home/xenon/.ssh/id_rsa", null);

        ClientSession session = SSHUtil.connect("SSHTEST", client, location, credential, 10 * 1000);
        session.close();
    }

    @Test
    public void test_connect_rsa_with_password() throws Exception {

        String location = docker.containers().container("ssh").port(22).inFormat("$HOST:$EXTERNAL_PORT");

        SshClient client = SSHUtil.createSSHClient(false, false, false, false, false);

        // Create a certificate credential that matches the (hard-coded) setup of the docker container.
        Credential credential = new CertificateCredential("xenon2", "/home/xenon/.ssh/id_rsa_pw", "javagat2".toCharArray());

        ClientSession session = SSHUtil.connect("SSHTEST", client, location, credential, 10 * 1000);
        session.close();
    }

}
