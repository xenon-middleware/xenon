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
package nl.esciencecenter.xenon.adaptors;

import static org.junit.Assert.fail;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.XenonTestWatcher;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.Credentials;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;

/**
 * @version 1.0
 * @since 1.0
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class GenericCredentialsAdaptorTestParent {
    private static CredentialTestConfig config;
    
    // MUST be invoked by a @BeforeClass method of the subclass! 
    public static void prepareClass(CredentialTestConfig testConfig) {
        config = testConfig;
    }
    
    @Rule
    public TestWatcher watcher = new XenonTestWatcher();
    
    @Before
    public void prepare() throws Exception {
        // This is not an adaptor option, so it will throw an exception!
        //Map<String, String> properties = new HashMap<>();
        //properties.put(SshAdaptor.POLLING_DELAY, "100");
        xenon = XenonFactory.newXenon(null);
        credentials = xenon.credentials();
    }

    @After
    public void cleanup() throws Exception {
        XenonFactory.endAll();
    }
   
    protected Xenon xenon;
    protected Credentials credentials;
    
    @Test
    public void test00_newCertificateCredential_OK() throws Exception {
        if (config.supportsCertificateCredentials()) { 
            String [] schemes = config.supportedSchemes();
            String certfile = config.getCorrectCertFile();
            String username = config.getUserName();
            char [] password = config.getPassword();
                        
            for (String scheme : schemes) {
                Credential c = credentials.newCertificateCredential(scheme, certfile, username, password, null);
                credentials.close(c);
            }
        }
    }
    
    @Test
    public void test01_newCertificateCredential_WrongCertificate() throws Exception {
        if (config.supportsCertificateCredentials()) { 
            String [] schemes = config.supportedSchemes();
            String certfile = config.getIncorrectCertFile();
            String username = config.getUserName();
            char [] password = config.getPassword();
                        
            for (String scheme : schemes) {
                try {
                    credentials.newCertificateCredential(scheme, certfile, username, password, null);
                    fail("Expected exception for incorrect certificate file!");
                } catch (XenonException e) { 
                    // expected
                }
            }
        }
    }

    @Test
    public void test02_newCertificateCredential_Unsupported() throws Exception {
        if (!config.supportsCertificateCredentials()) {
            String [] schemes = config.supportedSchemes();
            
            for (String scheme : schemes) {
                try {
                    credentials.newCertificateCredential(scheme, "cert", "username", "password".toCharArray(), null);
                    fail("Expected exception for unsupported newCertificateCredential!");
                } catch (XenonException e) { 
                    // expected
                }
            }
        }
    }

    @Test
    public void test04_newPasswordCredential_OK() throws Exception {
        if (config.supportsPasswordCredentials()) { 
            String [] schemes = config.supportedSchemes();
            String username = config.getUserName();
            char [] password = config.getPassword();
                        
            for (String scheme : schemes) {
                Credential c = credentials.newPasswordCredential(scheme, username, password, null);
                credentials.close(c);
            }
        }
    }
    
    
    @Test
    public void test05_newPasswordCredential_Unsupported() throws Exception {
        if (!config.supportsPasswordCredentials()) {
            String [] schemes = config.supportedSchemes();
            
            for (String scheme : schemes) {
                try {
                    credentials.newPasswordCredential(scheme, "username", "password".toCharArray(), null);
                    fail("Expected exception for unsupported newPasswordCredential!");
                } catch (XenonException e) { 
                    // expected
                }
            }
        }
    }

    @Test
    public void test06_newDefaultCredential() throws Exception {
        String [] schemes = config.supportedSchemes();

        for (String scheme : schemes) {
            Credential c = credentials.getDefaultCredential(scheme);
            credentials.close(c);
        }
    }
}
