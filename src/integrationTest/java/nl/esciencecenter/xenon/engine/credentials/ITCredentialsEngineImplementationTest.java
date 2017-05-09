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
package nl.esciencecenter.xenon.engine.credentials;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import nl.esciencecenter.xenon.Util;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.util.Utils;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * 
 */
public class ITCredentialsEngineImplementationTest {
    private static XenonEngine xenonEngine;

    @BeforeClass
    public static void prepare() throws Exception {
        xenonEngine = Util.createXenonEngine(null);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        Util.endXenonEngine(xenonEngine);
    }

    @org.junit.Test
    public void testDefault() throws Exception {
        CredentialsEngine ce = new CredentialsEngine(xenonEngine);
        Credential c = ce.getDefaultCredential("ssh");

        assertEquals("ssh", c.getAdaptorName());
    }

    @org.junit.Test
    public void testCertificate() throws Exception {
        CredentialsEngine ce = new CredentialsEngine(xenonEngine);
        
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
        assertEquals(new HashMap<String, String>(0), cci.getProperties());

        assertTrue(Arrays.equals(cci.getPassword(), "password".toCharArray()));
    }
}
