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
