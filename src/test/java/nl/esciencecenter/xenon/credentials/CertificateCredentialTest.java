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

import java.util.Arrays;

/**
 * 
 */
public class CertificateCredentialTest {

    @org.junit.Test
    public void test_username() throws Exception { 
        CertificateCredential cc = new CertificateCredential("username", "certfile", "password".toCharArray());
        assertEquals(cc.getUsername(), "username");
    }


    @org.junit.Test
    public void test_certfile() throws Exception { 
        CertificateCredential cc = new CertificateCredential("username", "certfile", "password".toCharArray());
        assertEquals(cc.getCertificateFile(), "certfile");
    }
    
    @org.junit.Test
    public void test_password() throws Exception { 
        CertificateCredential cc = new CertificateCredential("username", "certfile", "password".toCharArray());
        assertTrue(Arrays.equals(cc.getPassword(), "password".toCharArray()));
    }
    
    @org.junit.Test
    public void test_toString() throws Exception { 
        CertificateCredential cc = new CertificateCredential("username", "certfile", "password".toCharArray());
        assertTrue(cc.toString().equals("CertificateCredential [username=username, certfile=certfile]"));
    }
}


