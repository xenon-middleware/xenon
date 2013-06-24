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

package nl.esciencecenter.octopus.engine.credentials;

import static org.junit.Assert.*;

import java.util.Arrays;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class CertificateCredentialImplementationTest {
    
    @org.junit.Test
    public void test_getters_and_setters() throws Exception {
        CertificateCredentialImplementation pwc = new CertificateCredentialImplementation("test", "id1", null, "keyfile", 
                "certfile", "username", "password".toCharArray());
        
        assertEquals(pwc.getAdaptorName(), "test");
        assertEquals(pwc.getUniqueID(), "id1");
        assertEquals(pwc.getUsername(), "username");
        assertEquals(pwc.getKeyfile(), "keyfile");
        assertEquals(pwc.getCertfile(), "certfile");
        assertEquals(pwc.getProperties(), null);

        assertTrue(Arrays.equals(pwc.getPassword(), "password".toCharArray()));
        assertTrue(pwc.toString().equals("CertificateCredentialImplementation [adaptorName=test, username=username, keyfile=keyfile, certfile=certfile]"));
    }
}
