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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.junit.Test;

import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.UnknownAdaptorException;
import nl.esciencecenter.xenon.adaptors.filesystems.sftp.SftpFileAdaptor;
import nl.esciencecenter.xenon.adaptors.schedulers.ssh.SshSchedulerAdaptor;
import nl.esciencecenter.xenon.adaptors.shared.ssh.SSHUtil.PasswordProvider;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.schedulers.Scheduler;

public class SSHUtilTest {

    @Test
    public void test_dummy_constructor() throws IOException {
        new SSHUtil();
    }

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
    public void test_translateProperties_ssh_sftp() throws InvalidLocationException, UnknownAdaptorException {

        Map<String, String> prop = new HashMap<>();
        prop.put("xenon.adaptors.schedulers.ssh.strictHostKeyChecking", "false");
        prop.put("xenon.adaptors.schedulers.ssh.agentForwarding", "true");
        // prop.put("xenon.adaptors.schedulers.ssh.sshConfigFile", "/somewhere/config");
        Map<String, String> result = SSHUtil.translateProperties(prop, SshSchedulerAdaptor.PREFIX,
                FileSystem.getAdaptorDescription("sftp").getSupportedProperties(), SftpFileAdaptor.PREFIX);

        assertEquals(2, result.size());

        assertTrue(result.containsKey("xenon.adaptors.filesystems.sftp.strictHostKeyChecking"));
        assertTrue(result.containsKey("xenon.adaptors.filesystems.sftp.agentForwarding"));
        // assertTrue(result.containsKey("xenon.adaptors.filesystems.sftp.sshConfigFile"));

        assertEquals("false", result.get("xenon.adaptors.filesystems.sftp.strictHostKeyChecking"));
        assertEquals("true", result.get("xenon.adaptors.filesystems.sftp.agentForwarding"));
        // assertEquals("/somewhere/config", result.get("xenon.adaptors.filesystems.sftp.sshConfigFile"));
    }

    @Test
    public void test_translateProperties_sftp_ssh() throws InvalidLocationException, UnknownAdaptorException {

        Map<String, String> prop = new HashMap<>();
        prop.put("xenon.adaptors.filesystems.sftp.strictHostKeyChecking", "false");
        prop.put("xenon.adaptors.filesystems.sftp.agentForwarding", "true");
        // prop.put("xenon.adaptors.filesystems.sftp.sshConfigFile", "/somewhere/config");

        Map<String, String> result = SSHUtil.translateProperties(prop, SftpFileAdaptor.PREFIX, Scheduler.getAdaptorDescription("ssh").getSupportedProperties(),
                SshSchedulerAdaptor.PREFIX);

        assertEquals(2, result.size());

        assertTrue(result.containsKey("xenon.adaptors.schedulers.ssh.strictHostKeyChecking"));
        assertTrue(result.containsKey("xenon.adaptors.schedulers.ssh.agentForwarding"));
        // assertTrue(result.containsKey("xenon.adaptors.schedulers.ssh.sshConfigFile"));

        assertEquals("false", result.get("xenon.adaptors.schedulers.ssh.strictHostKeyChecking"));
        assertEquals("true", result.get("xenon.adaptors.schedulers.ssh.agentForwarding"));
        // assertEquals("/somewhere/config", result.get("xenon.adaptors.schedulers.ssh.sshConfigFile"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_extractLocations_null() throws InvalidLocationException {
        SSHUtil.extractLocations("Test", null);
    }

    @Test
    public void test_extractLocations_singleLocation() throws InvalidLocationException {
        SshdSocketAddress[] expected = new SshdSocketAddress[] { new SshdSocketAddress("localhost", 22) };
        SshdSocketAddress[] result = SSHUtil.extractLocations("Test", "localhost:22/tmp");

        assertArrayEquals(expected, result);
    }

    @Test
    public void test_extractLocations_dualLocation() throws InvalidLocationException {
        SshdSocketAddress[] expected = new SshdSocketAddress[] { new SshdSocketAddress("somehost", 33), new SshdSocketAddress("localhost", 22) };
        SshdSocketAddress[] result = SSHUtil.extractLocations("Test", "localhost:22/tmp via:somehost:33");

        assertArrayEquals(expected, result);
    }

    @Test
    public void test_extractLocations_quadLocation() throws InvalidLocationException {
        SshdSocketAddress[] expected = new SshdSocketAddress[] { new SshdSocketAddress("cloudhost", 55), new SshdSocketAddress("myhost", 44),
                new SshdSocketAddress("somehost", 33), new SshdSocketAddress("localhost", 22) };

        SshdSocketAddress[] result = SSHUtil.extractLocations("Test", "localhost:22/tmp via:somehost:33 via:myhost:44 via:cloudhost:55");

        assertArrayEquals(expected, result);
    }

    @Test
    public void test_extractLocations_dualLocationWhitespace() throws InvalidLocationException {
        SshdSocketAddress[] expected = new SshdSocketAddress[] { new SshdSocketAddress("somehost", 33), new SshdSocketAddress("localhost", 22) };
        SshdSocketAddress[] result = SSHUtil.extractLocations("Test", "localhost:22/tmp via:    somehost:33");

        assertArrayEquals(expected, result);
    }

    @Test(expected = InvalidLocationException.class)
    public void test_extractLocations_dualLocation_wrong() throws InvalidLocationException {
        SSHUtil.extractLocations("Test", " via:localhost:22/tmp via:somehost:33");
    }

}
