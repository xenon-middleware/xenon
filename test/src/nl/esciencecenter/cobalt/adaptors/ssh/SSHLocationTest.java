package nl.esciencecenter.cobalt.adaptors.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import nl.esciencecenter.cobalt.InvalidLocationException;
import nl.esciencecenter.cobalt.adaptors.ssh.SshLocation;

import org.junit.Test;

public class SSHLocationTest {
    
    public static final int DEFAULT_PORT = 22;
    
    @Test
    public void test_parse_hostOnly() throws Exception {        
        SshLocation tmp = SshLocation.parse("host");        
        assertNull(tmp.getUser());
        assertEquals(tmp.getHost(), "host");
        assertTrue(tmp.getPort() == DEFAULT_PORT);
    }
    
    @Test
    public void test_parse_userHost() throws Exception {        
        SshLocation tmp = SshLocation.parse("user@host");        
        assertEquals(tmp.getUser(), "user");
        assertEquals(tmp.getHost(), "host");
        assertTrue(tmp.getPort() == DEFAULT_PORT);
    }
    
    @Test
    public void test_parse_hostPort() throws Exception {        
        SshLocation tmp = SshLocation.parse("host:33");        
        assertNull(tmp.getUser());
        assertEquals(tmp.getHost(), "host");
        assertTrue(tmp.getPort() == 33);
    }
    
    @Test
    public void test_parse_userHostPort() throws Exception {        
        SshLocation tmp = SshLocation.parse("user@host:33");        
        assertEquals(tmp.getUser(), "user");
        assertEquals(tmp.getHost(), "host");
        assertTrue(tmp.getPort() == 33);
    }

    @Test
    public void test_parse_userHostDefaultPort1() throws Exception {        
        SshLocation tmp = SshLocation.parse("user@host:-42");        
        assertEquals(tmp.getUser(), "user");
        assertEquals(tmp.getHost(), "host");
        assertTrue(tmp.getPort() == DEFAULT_PORT);
    }

    @Test
    public void test_parse_userHostDefaultPort2() throws Exception {        
        SshLocation tmp = SshLocation.parse("user@host:0");        
        assertEquals(tmp.getUser(), "user");
        assertEquals(tmp.getHost(), "host");
        assertTrue(tmp.getPort() == DEFAULT_PORT);
    }
    
    @Test(expected = InvalidLocationException.class)
    public void test_parse_missingUser() throws Exception {        
        SshLocation.parse("@host:33");                
    }
    
    @Test(expected = InvalidLocationException.class)
    public void test_parse_missingPort() throws Exception {        
        SshLocation.parse("host:");                
    }
    
    @Test(expected = InvalidLocationException.class)
    public void test_parse_missingPort2() throws Exception {        
        SshLocation.parse("host:  ");                
    }
    
    @Test(expected = InvalidLocationException.class)
    public void test_parse_invalidPort() throws Exception {        
        SshLocation.parse("host:aap");                
    }
    
    @Test(expected = InvalidLocationException.class)
    public void test_parse_missingHost1() throws Exception {        
        SshLocation.parse(":33");                
    }
    
    @Test(expected = InvalidLocationException.class)
    public void test_parse_missingHost2() throws Exception {        
        SshLocation.parse("user@");                
    }
    
    @Test(expected = InvalidLocationException.class)
    public void test_parse_missingHost3() throws Exception {        
        SshLocation.parse("user@:33");                
    }
    
    
    
    
}