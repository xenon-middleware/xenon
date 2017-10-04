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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.adaptors.shared.ssh.SSHUtil.PasswordProvider;

public class SSHUtilTest {

    @Test
    public void test_passwordProvider() throws IOException {
        PasswordProvider p = new PasswordProvider("Hello World".toCharArray());
        assertEquals("Hello World", p.getPassword(null));
    }

    @Test(expected = InvalidLocationException.class)
    public void test_getHost_null() throws InvalidLocationException {
        SSHUtil.getHost("TEST", null);
    }

    @Test(expected = InvalidLocationException.class)
    public void test_getHost_empty() throws InvalidLocationException {
        SSHUtil.getHost("TEST", "");
    }

    @Test(expected = InvalidLocationException.class)
    public void test_getHost_whitespace() throws InvalidLocationException {
        SSHUtil.getHost("TEST", " \n\t ");
    }

    @Test
    public void test_getHost_simple() throws InvalidLocationException {
        String result = SSHUtil.getHost("TEST", "localhost");
        assertEquals("localhost", result);
    }

    @Test
    public void test_getHost_withPort() throws InvalidLocationException {
        String result = SSHUtil.getHost("TEST", "localhost:1234");
        assertEquals("localhost", result);
    }

    @Test(expected = InvalidLocationException.class)
    public void test_getPort_null() throws InvalidLocationException {
        SSHUtil.getHost("TEST", null);
    }

    @Test(expected = InvalidLocationException.class)
    public void test_getPort_empty() throws InvalidLocationException {
        SSHUtil.getHost("TEST", "");
    }

    @Test(expected = InvalidLocationException.class)
    public void test_getPort_whitespace() throws InvalidLocationException {
        SSHUtil.getHost("TEST", " \n\t ");
    }

    @Test
    public void test_getPort_default() throws InvalidLocationException {
        int result = SSHUtil.getPort("TEST", "localhost");
        assertEquals(SSHUtil.DEFAULT_SSH_PORT, result);
    }

    @Test
    public void test_getPort_withPortSet() throws InvalidLocationException {
        int result = SSHUtil.getPort("TEST", "localhost:1234");
        assertEquals(1234, result);
    }

    @Test(expected = InvalidLocationException.class)
    public void test_getPort_withPortInvalid() throws InvalidLocationException {
        SSHUtil.getPort("TEST", "localhost:aap");
    }

    @Test
    public void test_translateProperties() throws InvalidLocationException {

        Map<String, String> prop = new HashMap<>();

        // These are translated
        prop.put("a.b.c.1", "1");
        prop.put("a.b.c.2", "2");
        prop.put("a.b.c.3", "3");

        // These are skipped since they are not valid in target
        prop.put("a.b.c.6", "6");
        prop.put("a.b.c.7", "7");

        // This on is skipped, as it starts with the wrong prefix
        prop.put("c.b.a.8", "8");

        Set<String> valid = new HashSet<>();
        valid.add("p.q.r.1");
        valid.add("p.q.r.2");
        valid.add("p.q.r.3");
        valid.add("p.q.r.4");
        valid.add("p.q.r.5");

        Map<String, String> result = SSHUtil.translateProperties(prop, valid, "a.b.c", "p.q.r");

        assertEquals(3, result.size());

        assertTrue(result.containsKey("p.q.r.1"));
        assertTrue(result.containsKey("p.q.r.2"));
        assertTrue(result.containsKey("p.q.r.3"));

        assertEquals("1", result.get("p.q.r.1"));
        assertEquals("2", result.get("p.q.r.2"));
        assertEquals("3", result.get("p.q.r.3"));
    }

    @Test
    public void test_translateProperties_ssh_sftp() throws InvalidLocationException {

        Map<String, String> prop = new HashMap<>();
        prop.put("xenon.adaptors.schedulers.ssh.strictHostKeyChecking", "false");
        prop.put("xenon.adaptors.schedulers.ssh.agentForwarding", "true");
        prop.put("xenon.adaptors.schedulers.ssh.sshConfigFile", "/somewhere/config");

        Map<String, String> result = SSHUtil.sshToSftpProperties(prop);

        assertEquals(3, result.size());

        assertTrue(result.containsKey("xenon.adaptors.filesystems.sftp.strictHostKeyChecking"));
        assertTrue(result.containsKey("xenon.adaptors.filesystems.sftp.agentForwarding"));
        assertTrue(result.containsKey("xenon.adaptors.filesystems.sftp.sshConfigFile"));

        assertEquals("false", result.get("xenon.adaptors.filesystems.sftp.strictHostKeyChecking"));
        assertEquals("true", result.get("xenon.adaptors.filesystems.sftp.agentForwarding"));
        assertEquals("/somewhere/config", result.get("xenon.adaptors.filesystems.sftp.sshConfigFile"));
    }

    @Test
    public void test_translateProperties_sftp_ssh() throws InvalidLocationException {

        Map<String, String> prop = new HashMap<>();
        prop.put("xenon.adaptors.filesystems.sftp.strictHostKeyChecking", "false");
        prop.put("xenon.adaptors.filesystems.sftp.agentForwarding", "true");
        prop.put("xenon.adaptors.filesystems.sftp.sshConfigFile", "/somewhere/config");

        Map<String, String> result = SSHUtil.sftpToSshProperties(prop);

        assertEquals(3, result.size());

        assertTrue(result.containsKey("xenon.adaptors.schedulers.ssh.strictHostKeyChecking"));
        assertTrue(result.containsKey("xenon.adaptors.schedulers.ssh.agentForwarding"));
        assertTrue(result.containsKey("xenon.adaptors.schedulers.ssh.sshConfigFile"));

        assertEquals("false", result.get("xenon.adaptors.schedulers.ssh.strictHostKeyChecking"));
        assertEquals("true", result.get("xenon.adaptors.schedulers.ssh.agentForwarding"));
        assertEquals("/somewhere/config", result.get("xenon.adaptors.schedulers.ssh.sshConfigFile"));
    }

}
