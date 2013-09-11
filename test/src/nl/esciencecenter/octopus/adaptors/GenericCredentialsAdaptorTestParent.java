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

package nl.esciencecenter.octopus.adaptors;

import static org.junit.Assert.fail;
import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusException;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 *
 */
public class GenericCredentialsAdaptorTestParent {

    private static final Logger logger = LoggerFactory.getLogger(GenericJobAdaptorTestParent.class);

    private static CredentialTestConfig config;
    
    // MUST be invoked by a @BeforeClass method of the subclass! 
    public static void prepareClass(CredentialTestConfig testConfig) {
        config = testConfig;
    }
    
    @Rule
    public TestWatcher watcher = new TestWatcher() {

        @Override
        public void starting(Description description) {
            logger.info("Running test {}", description.getMethodName());
        }

        @Override
        public void failed(Throwable reason, Description description) {
            logger.info("Test {} failed due to exception", description.getMethodName(), reason);
        }

        @Override
        public void succeeded(Description description) {
            logger.info("Test {} succeeded", description.getMethodName());
        }

        @Override
        public void skipped(AssumptionViolatedException reason, Description description) {
            logger.info("Test {} skipped due to failed assumption", description.getMethodName(), reason);
        }

    };
    
    @Before
    public void prepare() throws Exception {
        // This is not an adaptor option, so it will throw an exception!
        //Map<String, String> properties = new HashMap<>();
        //properties.put(SshAdaptor.POLLING_DELAY, "100");
        octopus = OctopusFactory.newOctopus(null);
        credentials = octopus.credentials();
    }

    @After
    public void cleanup() throws Exception {
        OctopusFactory.endAll();
    }
   
    protected Octopus octopus;
    protected Credentials credentials;
    
    @Test
    public void test00_newCertificateCredential_OK() throws Exception {
        if (config.supportsCertificateCredentials()) { 
            String [] schemes = config.supportedSchemes();
            String certfile = config.getCorrectCertFile();
            String username = config.getUserName();
            char [] password = config.getPassword();
                        
            for (int i=0;i<schemes.length;i++) { 
                Credential c = credentials.newCertificateCredential(schemes[i], certfile, username, password, null);
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
                        
            for (int i=0;i<schemes.length;i++) { 
                try { 
                    credentials.newCertificateCredential(schemes[i], certfile, username, password, null);
                    fail("Expected exception for incorrect certificate file!");
                } catch (OctopusException e) { 
                    // expected
                }
            }
        }
    }

    @Test
    public void test02_newCertificateCredential_Unsupported() throws Exception {
        if (!config.supportsCertificateCredentials()) {
            String [] schemes = config.supportedSchemes();
            
            for (int i=0;i<schemes.length;i++) { 
                try { 
                    credentials.newCertificateCredential(schemes[i], "cert", "username", "password".toCharArray(), null);
                    fail("Expected exception for unsupported newCertificateCredential!");
                } catch (OctopusException e) { 
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
                        
            for (int i=0;i<schemes.length;i++) { 
                Credential c = credentials.newPasswordCredential(schemes[i], username, password, null);
                credentials.close(c);
            }
        }
    }
    
    
    @Test
    public void test05_newPasswordCredential_Unsupported() throws Exception {
        if (!config.supportsPasswordCredentials()) {
            String [] schemes = config.supportedSchemes();
            
            for (int i=0;i<schemes.length;i++) { 
                try { 
                    credentials.newPasswordCredential(schemes[i], "username", "password".toCharArray(), null);
                    fail("Expected exception for unsupported newPasswordCredential!");
                } catch (OctopusException e) { 
                    // expected
                }
            }
        }
    }

    @Test
    public void test06_newDefaultCredential() throws Exception {
        String [] schemes = config.supportedSchemes();
            
        for (int i=0;i<schemes.length;i++) { 
            Credential c = credentials.getDefaultCredential(schemes[i]);
            credentials.close(c);
        }
    }
}
