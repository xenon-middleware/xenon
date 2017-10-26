package nl.esciencecenter.xenon.credentials;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CredentialMapTest {

    @Test
    public void test_get_without_default() {
        CredentialMap m = new CredentialMap();
        assertNull(m.get("key"));
    }

    @Test
    public void test_get_with_default() {
        PasswordCredential p = new PasswordCredential("test", "foo".toCharArray());
        CredentialMap m = new CredentialMap(p);
        assertEquals(p, m.get("key"));
    }

    @Test
    public void test_contains() {
        PasswordCredential p = new PasswordCredential("test", "foo".toCharArray());

        CredentialMap m = new CredentialMap();
        m.put("key", p);

        assertTrue(m.containsCredential("key"));
    }

    @Test
    public void test_get() {
        PasswordCredential p = new PasswordCredential("test", "foo".toCharArray());

        CredentialMap m = new CredentialMap();
        m.put("key", p);

        assertEquals(p, m.get("key"));
    }

    @Test
    public void test_equals_empty() {
        assertEquals(new CredentialMap(), new CredentialMap());
    }

    @Test
    public void test_equals_full() {

        PasswordCredential p = new PasswordCredential("test", "foo".toCharArray());

        CredentialMap m = new CredentialMap();
        m.put("key", p);

        PasswordCredential p2 = new PasswordCredential("test", "foo".toCharArray());

        CredentialMap m2 = new CredentialMap();
        m2.put("key", p2);

        assertEquals(m, m2);
    }

    @Test
    public void test_not_equals_values() {

        PasswordCredential p = new PasswordCredential("test", "foo".toCharArray());

        CredentialMap m = new CredentialMap();
        m.put("key", p);

        PasswordCredential p2 = new PasswordCredential("bar", "foo".toCharArray());

        CredentialMap m2 = new CredentialMap();
        m2.put("key", p2);

        assertFalse(m.equals(m2));
    }

    @Test
    public void test_not_equals_keys() {

        PasswordCredential p = new PasswordCredential("test", "foo".toCharArray());

        CredentialMap m = new CredentialMap();
        m.put("key", p);

        PasswordCredential p2 = new PasswordCredential("test", "foo".toCharArray());

        CredentialMap m2 = new CredentialMap();
        m2.put("key2", p2);

        assertFalse(m.equals(m2));
    }

}
