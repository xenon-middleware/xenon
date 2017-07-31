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

/**
 *
 */
public class CertificateCredentialTest {

    @Test
    public void test_username() throws Exception {
        CertificateCredential cc = new CertificateCredential("username", "certfile", "password".toCharArray());
        assertEquals(cc.getUsername(), "username");
    }

    @Test
    public void test_certfile() throws Exception {
        CertificateCredential cc = new CertificateCredential("username", "certfile", "password".toCharArray());
        assertEquals(cc.getCertificateFile(), "certfile");
    }

    @Test
    public void test_password() throws Exception {
        CertificateCredential cc = new CertificateCredential("username", "certfile", "password".toCharArray());
        assertTrue(Arrays.equals(cc.getPassword(), "password".toCharArray()));
    }

    @Test
    public void test_toString() throws Exception {
        CertificateCredential cc = new CertificateCredential("username", "certfile", "password".toCharArray());
        assertTrue(cc.toString().equals("CertificateCredential [username=username, certfile=certfile]"));
    }

    @Test
    public void test_hashcode() {
        CertificateCredential cc1 = new CertificateCredential("username", "certfile", "password".toCharArray());
        CertificateCredential cc2 = new CertificateCredential("username", "certfile", "password".toCharArray());
        assertEquals(cc1.hashCode(), cc2.hashCode());
    }

    @Test
    public void test_equals_sameobj() {
        CertificateCredential cc = new CertificateCredential("username", "certfile", "password".toCharArray());
        assertTrue(cc.equals(cc));
    }

    @Test
    public void test_equals() {
        CertificateCredential cc1 = new CertificateCredential("username", "certfile", "password".toCharArray());
        CertificateCredential cc2 = new CertificateCredential("username", "certfile", "password".toCharArray());
        assertTrue(cc1.equals(cc2));
    }

    @Test
    public void test_equals_diffclass() {
        CertificateCredential cc1 = new CertificateCredential("username", "certfile", "password".toCharArray());
        String cc2 = "not the same class";
        assertFalse(cc1.equals(cc2));
    }

    @Test
    public void test_equals_null() {
        CertificateCredential cc1 = new CertificateCredential("username", "certfile", "password".toCharArray());
        assertFalse(cc1.equals(null));
    }

    @Test
    public void test_equals_diffusername() {
        CertificateCredential cc1 = new CertificateCredential("username1", "certfile", "password".toCharArray());
        CertificateCredential cc2 = new CertificateCredential("username2", "certfile", "password".toCharArray());
        assertFalse(cc1.equals(cc2));
    }

    @Test
    public void test_equals_diffcertfile() {
        CertificateCredential cc1 = new CertificateCredential("username", "certfile1", "password".toCharArray());
        CertificateCredential cc2 = new CertificateCredential("username", "certfile2", "password".toCharArray());
        assertFalse(cc1.equals(cc2));
    }

    @Test
    public void test_equals_diffpassword() {
        CertificateCredential cc1 = new CertificateCredential("username", "certfile", "password1".toCharArray());
        CertificateCredential cc2 = new CertificateCredential("username", "certfile", "password2".toCharArray());
        assertFalse(cc1.equals(cc2));
    }
}


