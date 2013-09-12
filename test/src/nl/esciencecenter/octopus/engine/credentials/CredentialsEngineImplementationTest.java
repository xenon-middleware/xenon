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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import nl.esciencecenter.octopus.Util;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.util.Utils;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class CredentialsEngineImplementationTest {

    private static OctopusEngine octopusEngine;

    @BeforeClass
    public static void prepare() throws Exception {
        octopusEngine = Util.createOctopusEngine(null);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        Util.endOctopusEngine(octopusEngine);
    }

    @org.junit.Test
    public void testDefault() throws Exception {

        CredentialsEngineImplementation ce = new CredentialsEngineImplementation(octopusEngine);
        Credential c = ce.getDefaultCredential("ssh");

        assertEquals("ssh", c.getAdaptorName());
    }

    @org.junit.Test
    public void testCertificate() throws Exception {

        CredentialsEngineImplementation ce = new CredentialsEngineImplementation(octopusEngine);
        
        String certfile = Utils.getHome() + Utils.getLocalSeparator() + ".ssh" + Utils.getLocalSeparator() + "id_rsa";  
        
        if (!new File(certfile).exists()) { 
            certfile = Utils.getHome() + Utils.getLocalSeparator() + ".ssh" + Utils.getLocalSeparator() + "id_dsa";
        }
        
        if (!new File(certfile).exists()) { 
            fail("Failed to find valid certificate file!");
        }
        
        Credential c = ce.newCertificateCredential("ssh", certfile, "username", "password".toCharArray(), null);

        assertTrue(c instanceof CertificateCredentialImplementation);

        CertificateCredentialImplementation cci = (CertificateCredentialImplementation) c;

        assertEquals("ssh", cci.getAdaptorName());
        assertEquals("username", cci.getUsername());
        assertEquals(certfile, cci.getCertfile());
        assertEquals(new HashMap<String, String>(), cci.getProperties());

        assertTrue(Arrays.equals(cci.getPassword(), "password".toCharArray()));
    }

    @org.junit.Test
    public void testPassword() throws Exception {

        CredentialsEngineImplementation ce = new CredentialsEngineImplementation(octopusEngine);
        Credential c = ce.newPasswordCredential("ssh", "username", "password".toCharArray(), null);

        assertTrue(c instanceof PasswordCredentialImplementation);

        PasswordCredentialImplementation pci = (PasswordCredentialImplementation) c;

        assertEquals("ssh", pci.getAdaptorName());
        assertEquals("username", pci.getUsername());
        assertEquals(new HashMap<String, String>(), pci.getProperties());

        assertTrue(Arrays.equals(pci.getPassword(), "password".toCharArray()));
    }
}
