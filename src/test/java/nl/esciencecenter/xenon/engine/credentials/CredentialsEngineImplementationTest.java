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

package nl.esciencecenter.xenon.engine.credentials;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;

import nl.esciencecenter.xenon.Util;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonEngine;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class CredentialsEngineImplementationTest {

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
    public void testPassword() throws Exception {

        CredentialsEngineImplementation ce = new CredentialsEngineImplementation(xenonEngine);
        Credential c = ce.newPasswordCredential("ssh", "username", "password".toCharArray(), null);

        assertTrue(c instanceof PasswordCredentialImplementation);

        PasswordCredentialImplementation pci = (PasswordCredentialImplementation) c;

        assertEquals("ssh", pci.getAdaptorName());
        assertEquals("username", pci.getUsername());
        assertEquals(new HashMap<String, String>(0), pci.getProperties());

        assertTrue(Arrays.equals(pci.getPassword(), "password".toCharArray()));
    }
}
