/**
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

/**
 * 
 */
public class PasswordCredentialTest {

    @org.junit.Test
    public void test_username() throws Exception {
        PasswordCredential pwc = new PasswordCredential("username", "password".toCharArray());
        assertEquals(pwc.getUsername(), "username");
    }
    
    @org.junit.Test
    public void test_password() throws Exception {
        PasswordCredential pwc = new PasswordCredential("username", "password".toCharArray());
        assertTrue(Arrays.equals(pwc.getPassword(), "password".toCharArray()));
    }

    
    @org.junit.Test
    public void test_toString() throws Exception {
        PasswordCredential pwc = new PasswordCredential("username", "password".toCharArray());
        assertTrue(pwc.toString().equals("PasswordCredential [username=username]"));
    }
 
    @org.junit.Test
    public void test_password_null() throws Exception {
        PasswordCredential pwc = new PasswordCredential("username", null);
        assertArrayEquals(new char[0], pwc.getPassword());
    }
 
    
}
