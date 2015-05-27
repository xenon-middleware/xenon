package nl.esciencecenter.xenon.adaptors.ftp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import nl.esciencecenter.xenon.InvalidLocationException;

import org.junit.Test;

public class FTPLocationTest {

    public static final int DEFAULT_PORT = 21;

    @Test
    public void test_parse_hostOnly() throws Exception {
        FtpLocation tmp = FtpLocation.parse("host");
        assertNull(tmp.getUser());
        assertEquals(tmp.getHost(), "host");
        assertTrue(tmp.getPort() == DEFAULT_PORT);
    }

    @Test
    public void test_parse_userHost() throws Exception {
        FtpLocation tmp = FtpLocation.parse("user@host");
        assertEquals(tmp.getUser(), "user");
        assertEquals(tmp.getHost(), "host");
        assertTrue(tmp.getPort() == DEFAULT_PORT);
    }

    @Test
    public void test_parse_hostPort() throws Exception {
        FtpLocation tmp = FtpLocation.parse("host:33");
        assertNull(tmp.getUser());
        assertEquals(tmp.getHost(), "host");
        assertTrue(tmp.getPort() == 33);
    }

    @Test
    public void test_parse_userHostPort() throws Exception {
        FtpLocation tmp = FtpLocation.parse("user@host:33");
        assertEquals(tmp.getUser(), "user");
        assertEquals(tmp.getHost(), "host");
        assertTrue(tmp.getPort() == 33);
    }

    @Test
    public void test_parse_userHostDefaultPort1() throws Exception {
        FtpLocation tmp = FtpLocation.parse("user@host:-42");
        assertEquals(tmp.getUser(), "user");
        assertEquals(tmp.getHost(), "host");
        assertTrue(tmp.getPort() == DEFAULT_PORT);
    }

    @Test
    public void test_parse_userHostDefaultPort2() throws Exception {
        FtpLocation tmp = FtpLocation.parse("user@host:0");
        assertEquals(tmp.getUser(), "user");
        assertEquals(tmp.getHost(), "host");
        assertTrue(tmp.getPort() == DEFAULT_PORT);
    }

    @Test(expected = InvalidLocationException.class)
    public void test_parse_missingUser() throws Exception {
        FtpLocation.parse("@host:33");
    }

    @Test(expected = InvalidLocationException.class)
    public void test_parse_missingPort() throws Exception {
        FtpLocation.parse("host:");
    }

    @Test(expected = InvalidLocationException.class)
    public void test_parse_missingPort2() throws Exception {
        FtpLocation.parse("host:  ");
    }

    @Test(expected = InvalidLocationException.class)
    public void test_parse_invalidPort() throws Exception {
        FtpLocation.parse("host:aap");
    }

    @Test(expected = InvalidLocationException.class)
    public void test_parse_missingHost1() throws Exception {
        FtpLocation.parse(":33");
    }

    @Test(expected = InvalidLocationException.class)
    public void test_parse_missingHost2() throws Exception {
        FtpLocation.parse("user@");
    }

    @Test(expected = InvalidLocationException.class)
    public void test_parse_missingHost3() throws Exception {
        FtpLocation.parse("user@:33");
    }

}