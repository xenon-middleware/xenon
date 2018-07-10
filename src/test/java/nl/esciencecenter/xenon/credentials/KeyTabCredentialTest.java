package nl.esciencecenter.xenon.credentials;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Objects;

import org.junit.Test;

public class KeyTabCredentialTest {

    @Test
    public void test_username() throws Exception {
        KeytabCredential cc = new KeytabCredential("username", "file");
        assertEquals(cc.getUsername(), "username");
    }

    @Test
    public void test_keytabfile() throws Exception {
        KeytabCredential cc = new KeytabCredential("username", "file");
        assertEquals(cc.getKeytabFile(), "file");
    }

    @Test
    public void test_hashCode() throws Exception {
        KeytabCredential cc = new KeytabCredential("username", "file");
        assertEquals(Objects.hash("username", "file"), cc.hashCode());
    }

    @Test
    public void test_equals() throws Exception {
        KeytabCredential cc = new KeytabCredential("username", "file");
        KeytabCredential cc2 = new KeytabCredential("username", "file");

        assertTrue(cc.equals(cc2));
    }

    @Test
    public void test_equals2() throws Exception {
        KeytabCredential cc = new KeytabCredential("username", "file");
        assertTrue(cc.equals(cc));
    }

    @Test
    public void test_equals3() throws Exception {
        KeytabCredential cc = new KeytabCredential("username", "file");
        assertFalse(cc.equals("Hello"));
    }

    @Test
    public void test_equals4() throws Exception {
        KeytabCredential cc = new KeytabCredential("username", "file");
        assertFalse(cc.equals(null));
    }

    @Test
    public void test_equals5() throws Exception {
        KeytabCredential cc = new KeytabCredential("username", "file");
        KeytabCredential cc2 = new KeytabCredential("username", "file1");
        assertFalse(cc.equals(cc2));
    }

    @Test
    public void test_equals6() throws Exception {
        KeytabCredential cc = new KeytabCredential("username", "file");
        KeytabCredential cc2 = new KeytabCredential("username1", "file");
        assertFalse(cc.equals(cc2));
    }

}
