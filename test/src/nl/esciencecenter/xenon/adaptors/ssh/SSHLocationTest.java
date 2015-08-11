package nl.esciencecenter.xenon.adaptors.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import nl.esciencecenter.xenon.InvalidLocationException;

import org.junit.Test;

import com.jcraft.jsch.ConfigRepository;
import com.jcraft.jsch.OpenSSHConfig;

public class SSHLocationTest {

    public static final int DEFAULT_PORT = 22;

    @Test
    public void test_parse_hostOnly() throws Exception {
        SshLocation tmp = SshLocation.parse("host", ConfigRepository.nullConfig);
        assertNull(tmp.getUser());
        assertEquals("host", tmp.getHost());
        assertEquals(DEFAULT_PORT, tmp.getPort());
    }

    @Test
    public void test_parse_sshConfig() throws Exception {
        OpenSSHConfig sshConfig = OpenSSHConfig.parse("Host host\nHostName host.local\nPort 2030\nUser user\n");
        ConfigRepository.Config sshHostConfig = sshConfig.getConfig("host");
        // Unfortunately, JSch does not recognize OpenSSH standard HostName,
        // only Hostname.
        String hostname = sshHostConfig.getHostname();
        if (hostname == null) {
            hostname = sshHostConfig.getValue("HostName");
        }
        assertEquals("host.local", hostname);
        assertEquals("user", sshHostConfig.getUser());
        assertEquals(2030, sshHostConfig.getPort());
    }

    @Test
    public void test_parse_emptySshConfig() throws Exception {
        OpenSSHConfig sshConfig = OpenSSHConfig.parse("Host host\n");
        ConfigRepository.Config sshHostConfig = sshConfig.getConfig("host");
        assertNull(sshHostConfig.getValue("HostName"));
        assertNull(sshHostConfig.getHostname());
        assertNull(sshHostConfig.getUser());
        assertEquals(-1, sshHostConfig.getPort());
    }
    
    @Test
    public void test_parse_hostOnlySsh() throws Exception {
        OpenSSHConfig sshConfig = OpenSSHConfig.parse("Host host\nHostName host.local\n");
        SshLocation tmp = SshLocation.parse("host", sshConfig);
        assertNull(tmp.getUser());
        assertEquals("host.local", tmp.getHost());
        assertEquals(DEFAULT_PORT, tmp.getPort());
    }

    @Test
    public void test_parse_userHost() throws Exception {
        SshLocation tmp = SshLocation.parse("user@host", ConfigRepository.nullConfig);
        assertEquals("user", tmp.getUser());
        assertEquals("host", tmp.getHost());
        assertEquals(DEFAULT_PORT, tmp.getPort());
    }

    @Test
    public void test_parse_hostPort() throws Exception {
        SshLocation tmp = SshLocation.parse("host:33", ConfigRepository.nullConfig);
        assertNull(tmp.getUser());
        assertEquals("host", tmp.getHost());
        assertEquals(33, tmp.getPort());
    }

    @Test
    public void test_parse_hostPortSshDefault() throws Exception {
        OpenSSHConfig sshConfig = OpenSSHConfig.parse("Host host\nPort 50022\n");
        SshLocation tmp = SshLocation.parse("host", sshConfig);
        assertNull(tmp.getUser());
        assertEquals("host", tmp.getHost());
        assertEquals(50022, tmp.getPort());
    }

    @Test
    public void test_parse_hostPortSsh() throws Exception {
        OpenSSHConfig sshConfig = OpenSSHConfig.parse("Host host\nPort 50022\n");
        SshLocation tmp = SshLocation.parse("host:33", sshConfig);
        assertNull(tmp.getUser());
        assertEquals("host", tmp.getHost());
        assertEquals(33, tmp.getPort());
    }

    @Test
    public void test_parse_userHostPort() throws Exception {
        SshLocation tmp = SshLocation.parse("user@host:33", ConfigRepository.nullConfig);
        assertEquals("user", tmp.getUser());
        assertEquals("host", tmp.getHost());
        assertEquals(33, tmp.getPort());
    }

    @Test
    public void test_parse_userHostPortSsh() throws Exception {
        OpenSSHConfig sshConfig = OpenSSHConfig.parse("Host host\nUser otheruser\nPort 50022\n");
        SshLocation tmp = SshLocation.parse("user@host:33", sshConfig);
        assertEquals("user", tmp.getUser());
        assertEquals("host", tmp.getHost());
        assertEquals(33, tmp.getPort());
    }

    @Test
    public void test_parse_userHostPortSshDefault() throws Exception {
        OpenSSHConfig sshConfig = OpenSSHConfig.parse("Host host\nUser otheruser\nPort 50022\n");
        SshLocation tmp = SshLocation.parse("host", sshConfig);
        assertEquals("otheruser", tmp.getUser());
        assertEquals("host", tmp.getHost());
        assertEquals(50022, tmp.getPort());
    }

    @Test
    public void test_parse_userHostPortSshAllDefault() throws Exception {
        OpenSSHConfig sshConfig = OpenSSHConfig.parse("Host host\nHostName host.local\nUser otheruser\nPort 50022\n");
        SshLocation tmp = SshLocation.parse("host", sshConfig);
        assertEquals("otheruser", tmp.getUser());
        assertEquals("host.local", tmp.getHost());
        assertEquals(50022, tmp.getPort());
    }

    @Test
    public void test_parse_userHostDefaultPort1() throws Exception {
        SshLocation tmp = SshLocation.parse("user@host:-42", ConfigRepository.nullConfig);
        assertEquals("user", tmp.getUser());
        assertEquals("host", tmp.getHost());
        assertEquals(DEFAULT_PORT, tmp.getPort());
    }

    @Test
    public void test_parse_userHostDefaultPort2() throws Exception {
        SshLocation tmp = SshLocation.parse("user@host:0", ConfigRepository.nullConfig);
        assertEquals("user", tmp.getUser());
        assertEquals("host", tmp.getHost());
        assertEquals(DEFAULT_PORT, tmp.getPort());
    }

    @Test(expected = InvalidLocationException.class)
    public void test_parse_missingUser() throws Exception {
        SshLocation.parse("@host:33", ConfigRepository.nullConfig);
    }

    @Test(expected = InvalidLocationException.class)
    public void test_parse_missingPort() throws Exception {
        SshLocation.parse("host:", ConfigRepository.nullConfig);
    }

    @Test(expected = InvalidLocationException.class)
    public void test_parse_missingPort2() throws Exception {
        SshLocation.parse("host:  ", ConfigRepository.nullConfig);
    }

    @Test(expected = InvalidLocationException.class)
    public void test_parse_invalidPort() throws Exception {
        SshLocation.parse("host:aap", ConfigRepository.nullConfig);
    }

    @Test(expected = InvalidLocationException.class)
    public void test_parse_missingHost1() throws Exception {
        SshLocation.parse(":33", ConfigRepository.nullConfig);
    }

    @Test(expected = InvalidLocationException.class)
    public void test_parse_missingHost2() throws Exception {
        SshLocation.parse("user@", ConfigRepository.nullConfig);
    }

    @Test(expected = InvalidLocationException.class)
    public void test_parse_missingHost3() throws Exception {
        SshLocation.parse("user@:33", ConfigRepository.nullConfig);
    }
}