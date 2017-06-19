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
package nl.esciencecenter.xenon.adaptors.job.ssh;

import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.jobs.Jobs;
import nl.esciencecenter.xenon.jobs.Scheduler;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class SSHRemoteAccessTestWithoutAgent {

    public static SSHJobTestConfig config;
    
    protected Xenon xenon;
    protected Jobs jobs;
    
    @BeforeClass
    public static void prepareSSHConfig() throws Exception {
        config = new SSHJobTestConfig(null);    
    }

    @Before
    public void prepare() throws Exception {
        // This is not an adaptor option, so it will throw an exception!
        //properties.put(SshAdaptor.POLLING_DELAY, "100");

        Map<String, String> properties = new HashMap<>();
        properties.put("xenon.adaptors.ssh.agent", "false");
        
        xenon = XenonFactory.newXenon(properties);
        jobs = xenon.jobs();
    }
    
    @Test
    public void test01_defaultAccess() throws Exception {
        Scheduler s = jobs.newScheduler(config.getScheme(), config.getCorrectLocation(), 
                config.getDefaultCredential(), null);
        jobs.close(s);
    }

    @Test
    public void test02_PasswordAccess() throws Exception {
        Scheduler s = jobs.newScheduler(config.getScheme(), config.getCorrectLocation(), 
                config.getPasswordCredential(), null);
        jobs.close(s);
    }
    
    @Test
    public void test03_OpenCertificateAccess() throws Exception {
        Scheduler s = jobs.newScheduler(config.getScheme(), config.getCorrectLocation(), 
                config.getOpenCredential(), null);
        jobs.close(s);
    }
    
    @Test
    public void test04_ProtectedCertificateAccess() throws Exception {
        Scheduler s = jobs.newScheduler(config.getScheme(), config.getCorrectLocation(), 
                config.getProtectedCredential(), null);
        jobs.close(s);
    }

    @Test(expected = XenonException.class)
    public void test05_FailedProtectedCertificateAccess() throws Exception {
        Scheduler s = jobs.newScheduler(config.getScheme(), config.getCorrectLocation(), 
                config.getInvalidProtectedCredential(), null);
        jobs.close(s);
    }

    @Test(expected = XenonException.class)
    public void test06_InvalidPasswordAccess() throws Exception {
        Scheduler s = jobs.newScheduler(config.getScheme(), config.getCorrectLocation(), 
                config.getInvalidCredential(), null);
        jobs.close(s);
    }
}
