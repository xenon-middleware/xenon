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
import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class CredentialsEngineImplementationTest {

    
    private static OctopusEngine octopusEngine;
    
    @BeforeClass
    public static void prepare() throws OctopusException { 
        octopusEngine = new OctopusEngine(new Properties());
    }
    
    @AfterClass
    public static void cleanup() { 
        octopusEngine.end();
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
        Credential c = ce.newCertificateCredential("ssh", null, "keyfile", "certfile", "username", "password".toCharArray());
        
        assertTrue(c instanceof CertificateCredentialImplementation);
        
        CertificateCredentialImplementation cci = (CertificateCredentialImplementation) c;
        
        assertEquals("ssh", cci.getAdaptorName());
        assertEquals("username", cci.getUsername());
        assertEquals( "keyfile", cci.getKeyfile());
        assertEquals("certfile", cci.getCertfile());
        assertEquals(new Properties(), cci.getProperties());

        assertTrue(Arrays.equals(cci.getPassword(), "password".toCharArray()));
    }
    
    @org.junit.Test
    public void testPassword() throws Exception { 

        CredentialsEngineImplementation ce = new CredentialsEngineImplementation(octopusEngine); 
        Credential c = ce.newPasswordCredential("ssh", null, "username", "password".toCharArray());
        
        assertTrue(c instanceof PasswordCredentialImplementation);
        
        PasswordCredentialImplementation pci = (PasswordCredentialImplementation) c;

        assertEquals("ssh", pci.getAdaptorName());
        assertEquals("username", pci.getUsername());
        assertEquals(new Properties(), pci.getProperties());

        assertTrue(Arrays.equals(pci.getPassword(), "password".toCharArray()));
    }
    
    @org.junit.Test
    public void testProxy() throws Exception { 
        
        CredentialsEngineImplementation ce = new CredentialsEngineImplementation(octopusEngine); 
        Credential c = ce.newProxyCredential("ssh", null, "host", 42, "username", "password".toCharArray());
        
        assertTrue(c instanceof ProxyCredentialImplementation);
        
        ProxyCredentialImplementation pci = (ProxyCredentialImplementation) c;

        assertEquals("ssh", pci.getAdaptorName());
        assertEquals("username", pci.getUsername());
        assertEquals(pci.getHost(), "host");
        assertEquals(pci.getPort(), 42);
        assertEquals(new Properties(), pci.getProperties());
        
        assertTrue(Arrays.equals(pci.getPassword(), "password".toCharArray()));
    }
    
}
