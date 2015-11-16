package nl.esciencecenter.xenon.adaptors.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import nl.esciencecenter.xenon.InvalidLocationException;

import org.junit.Test;

import com.jcraft.jsch.ConfigRepository;

public class SSHLocationTest {

    public static final int DEFAULT_PORT = 22;

    @Test
    public void test_parse_hostOnly() throws Exception {
        SshLocation tmp = SshLocation.parse("host", ConfigRepository.nullConfig);
        assertNull(tmp.getUser());
        assertEquals(tmp.getHost(), "host");
        assertTrue(tmp.getPort() == DEFAULT_PORT);
    }

    /** Test if empty values are updated with SSH config settings. */
    @Test
    public void test_parse_hostOnlyWithConfig() throws Exception {
        String confString = "Host host\nHostName host.fqdn\nUser myuser\nPort 42";
        OpenSSHConfig config = OpenSSHConfig.parse(confString);
        SshLocation tmp = SshLocation.parse("host", config);
        assertEquals("myuser", tmp.getUser());
        assertEquals("host.fqdn", tmp.getHost());
        assertEquals(42, tmp.getPort());
    }

    @Test
    public void test_parse_userHost() throws Exception {
        SshLocation tmp = SshLocation.parse("user@host", ConfigRepository.nullConfig);
        assertEquals(tmp.getUser(), "user");
        assertEquals(tmp.getHost(), "host");
        assertTrue(tmp.getPort() == DEFAULT_PORT);
    }

    @Test
    public void test_parse_hostPort() throws Exception {
        SshLocation tmp = SshLocation.parse("host:33", ConfigRepository.nullConfig);
        assertNull(tmp.getUser());
        assertEquals(tmp.getHost(), "host");
        assertTrue(tmp.getPort() == 33);
    }

    @Test
    public void test_parse_userHostPort() throws Exception {
        SshLocation tmp = SshLocation.parse("user@host:33", ConfigRepository.nullConfig);
        assertEquals(tmp.getUser(), "user");
        assertEquals(tmp.getHost(), "host");
        assertTrue(tmp.getPort() == 33);
    }

    /** Test if filled values are not overwritten by SSH config settings. */
    @Test
    public void test_parse_userHostPortConfig() throws Exception {
        String confString = "Host host\nHostName host.fqdn\nUser myuser\nPort 42";
        OpenSSHConfig config = OpenSSHConfig.parse(confString);
        SshLocation tmp = SshLocation.parse("user@host:33", config);
        assertEquals("user", tmp.getUser());
        assertEquals("host.fqdn", tmp.getHost());
        assertEquals(33, tmp.getPort());
    }

    @Test
    public void test_parse_withScheme_correctScheme() throws InvalidLocationException {
        SshLocation tmp = SshLocation.parse("ssh://host", ConfigRepository.nullConfig);
        assertEquals("ssh", tmp.getScheme());
    }

    @Test
    public void test_parse_withScheme_correctHost() throws InvalidLocationException {
        SshLocation tmp = SshLocation.parse("ssh://host", ConfigRepository.nullConfig);
        assertEquals("host", tmp.getHost());
    }

    @Test
    public void test_parse_withScheme_correctPort() throws InvalidLocationException {
        SshLocation tmp = SshLocation.parse("ssh://host:777", ConfigRepository.nullConfig);
        assertEquals(777, tmp.getPort());
    }

    @Test
    public void test_parseToString_withOutScheme() throws InvalidLocationException {
        String url = "user@host:777";
        SshLocation tmp = SshLocation.parse(url, ConfigRepository.nullConfig);
        assertEquals("ssh://" + url, tmp.toString());
    }

    @Test
    public void test_parseToString_withScheme() throws InvalidLocationException {
        String url = "ssh://user@host:777";
        SshLocation tmp = SshLocation.parse(url, ConfigRepository.nullConfig);
        assertEquals(url, tmp.toString());
    }

    @Test(expected = InvalidLocationException.class)
    public void test_parse_negativePort() throws Exception {
        SshLocation.parse("user@host:-42", ConfigRepository.nullConfig);
    }

    @Test(expected = InvalidLocationException.class)
    public void test_parse_zeroPort() throws Exception {
        SshLocation.parse("user@host:0", ConfigRepository.nullConfig);
    }

    @Test
    public void test_parse_missingUser() throws Exception {
        SshLocation tmp = SshLocation.parse("@host:33", ConfigRepository.nullConfig);
        assertNull(tmp.getUser());
    }

    public void test_parse_missingPort() throws Exception {
        SshLocation tmp = SshLocation.parse("host:", ConfigRepository.nullConfig);
        assertTrue(tmp.usesDefaultPort());
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