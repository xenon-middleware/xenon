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
package nl.esciencecenter.xenon.credentials;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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

    @Test(expected = IllegalArgumentException.class)
    public void test_put() {
        PasswordCredential p = new PasswordCredential("test", "foo".toCharArray());

        CredentialMap m = new CredentialMap();
        m.put(null, p);
    }

    @Test
    public void test_equals_empty() {
        assertEquals(new CredentialMap(), new CredentialMap());
    }

    @Test
    public void test_equals_same() {
        CredentialMap c = new CredentialMap();
        assertTrue(c.equals(c));
    }

    @Test
    public void test_not_equals_null() {
        CredentialMap c = new CredentialMap();
        assertFalse(c.equals(null));
    }

    @Test
    public void test_not_equals_different_type() {
        CredentialMap c = new CredentialMap();
        assertFalse(c.equals("Hello"));
    }

    @Test
    public void test_not_equals_different_default_null() {
        CredentialMap c1 = new CredentialMap();
        CredentialMap c2 = new CredentialMap(new DefaultCredential());

        assertFalse(c1.equals(c2));
        assertFalse(c2.equals(c1));
    }

    @Test
    public void test_not_equals_different_default_type() {
        CredentialMap c1 = new CredentialMap(new PasswordCredential("aap", "noot".toCharArray()));
        CredentialMap c2 = new CredentialMap(new DefaultCredential());

        assertFalse(c1.equals(c2));
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

    @Test
    public void test_hashcode_equal() {

        PasswordCredential p = new PasswordCredential("test", "foo".toCharArray());

        CredentialMap m = new CredentialMap();
        m.put("key", p);

        PasswordCredential p2 = new PasswordCredential("test", "foo".toCharArray());

        assertEquals(p.hashCode(), p2.hashCode());
    }

    @Test
    public void test_keySet() {
        CredentialMap m = new CredentialMap();
        PasswordCredential p = new PasswordCredential("test", "foo".toCharArray());
        m.put("key", p);

        Set<String> ks = m.keySet();

        Set<String> expected = new HashSet<>();
        expected.add("key");
        assertEquals(expected, ks);
    }

    @Test
    public void test_getDefault_noDefault_null() {
        CredentialMap m = new CredentialMap();

        UserCredential d = m.getDefault();

        assertNull(d);
    }

    @Test
    public void test_getDefault_Password() {
        PasswordCredential p = new PasswordCredential("test", "foo".toCharArray());
        CredentialMap m = new CredentialMap(p);

        UserCredential d = m.getDefault();

        assertEquals(p, d);
    }

    private int hashCode(Credential defaultCredential, HashMap<String, UserCredential> map) {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((defaultCredential == null) ? 0 : defaultCredential.hashCode());
        result = prime * result + map.hashCode();
        return result;
    }

    @Test
    public void test_hashcode1() {
        PasswordCredential p = new PasswordCredential("test", "foo".toCharArray());
        CredentialMap m = new CredentialMap(p);

        HashMap<String, UserCredential> map = new HashMap<>();

        assertEquals(hashCode(p, map), m.hashCode());
    }

    @Test
    public void test_hashcode2() {
        PasswordCredential p = new PasswordCredential("test", "foo".toCharArray());
        CredentialMap m = new CredentialMap();
        m.put("test", p);

        HashMap<String, UserCredential> map = new HashMap<>();
        map.put("test", p);

        assertEquals(hashCode(null, map), m.hashCode());
    }

    @Test
    public void test_equals1() {
        PasswordCredential p = new PasswordCredential("test", "foo".toCharArray());
        CredentialMap m = new CredentialMap(p);

        PasswordCredential p2 = new PasswordCredential("test", "foo".toCharArray());
        CredentialMap m2 = new CredentialMap(p2);

        assertEquals(m, m2);
    }

    @Test
    public void test_equals2() {
        PasswordCredential p = new PasswordCredential("test", "foo".toCharArray());
        CredentialMap m = new CredentialMap();
        m.put("test", p);

        PasswordCredential p2 = new PasswordCredential("test", "foo".toCharArray());
        CredentialMap m2 = new CredentialMap();
        m2.put("test", p2);

        assertEquals(m, m2);
    }

}
