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

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class PasswordCredentialTest {

    @Test
    public void test_username() throws Exception {
        PasswordCredential pwc = new PasswordCredential("username", "password".toCharArray());
        assertEquals(pwc.getUsername(), "username");
    }

    @Test
    public void test_password() throws Exception {
        PasswordCredential pwc = new PasswordCredential("username", "password".toCharArray());
        assertTrue(Arrays.equals(pwc.getPassword(), "password".toCharArray()));
    }

    @Test
    public void test_toString() throws Exception {
        PasswordCredential pwc = new PasswordCredential("username", "password".toCharArray());
        assertTrue(pwc.toString().equals("PasswordCredential [username=username]"));
    }
 
    @Test
    public void test_password_null() throws Exception {
        PasswordCredential pwc = new PasswordCredential("username", null);
        assertArrayEquals(new char[0], pwc.getPassword());
    }
 
    @Test
    public void test_hashcode() {
        PasswordCredential pwc1 = new PasswordCredential("username", "password".toCharArray());
        PasswordCredential pwc2 = new PasswordCredential("username", "password".toCharArray());
        assertEquals(pwc1.hashCode(), pwc2.hashCode());
    }

    @Test
    public void test_equals_sameobj() {
        PasswordCredential pwc = new PasswordCredential("username", "password".toCharArray());
        assertTrue(pwc.equals(pwc));
    }

    @Test
    public void test_equals() {
        PasswordCredential pwc1 = new PasswordCredential("username", "password".toCharArray());
        PasswordCredential pwc2 = new PasswordCredential("username", "password".toCharArray());
        assertTrue(pwc1.equals(pwc2));
    }

    @Test
    public void test_equals_diffclass() {
        PasswordCredential pwc1 = new PasswordCredential("username", "password".toCharArray());
        String pwc2 = "not the same class";
        assertFalse(pwc1.equals(pwc2));
    }

    @Test
    public void test_equals_null() {
        PasswordCredential pwc1 = new PasswordCredential("username", "password".toCharArray());
        assertFalse(pwc1.equals(null));
    }

    @Test
    public void test_equals_diffusername() {
        PasswordCredential pwc1 = new PasswordCredential("username1", "password".toCharArray());
        PasswordCredential pwc2 = new PasswordCredential("username2", "password".toCharArray());
        assertFalse(pwc1.equals(pwc2));
    }

    @Test
    public void test_equals_diffpassword() {
        PasswordCredential pwc1 = new PasswordCredential("username", "password1".toCharArray());
        PasswordCredential pwc2 = new PasswordCredential("username", "password2".toCharArray());
        assertFalse(pwc1.equals(pwc2));
    }
}
