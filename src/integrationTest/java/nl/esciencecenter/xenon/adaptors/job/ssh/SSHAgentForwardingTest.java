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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.Jobs;
import nl.esciencecenter.xenon.jobs.Scheduler;

public class SSHAgentForwardingTest {
    public static SSHJobTestConfig config;

    protected Jobs jobs;
    protected Scheduler scheduler;

    @BeforeClass
    public static void prepareSSHConfig() throws Exception {
        config = new SSHJobTestConfig(null);
    }

    @Before
    public void setUp() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("xenon.adaptors.ssh.agent", "true");
        properties.put("xenon.adaptors.ssh.agentForwarding", "true");

        jobs = Xenon.jobs();
        String gateway = config.getPropertyOrFail("test.ssh.gateway");
        // NOTE: The incorrect password should now be ignored, since the ssh-agent takes care of this.
        Credential credential = config.getInvalidProtectedCredential();

        scheduler = jobs.newScheduler(config.getScheme(), gateway, credential, null);
    }

    @After
    public void tearDown() throws XenonException{
        jobs.close(scheduler);
    }

    @Test
    public void test_agentForwarding() throws XenonException, Exception {
        JobDescription description = new JobDescription();
        // Commands executed on gateway machine should be agent aware
        // Using `-i /dev/null` to overwrite default key locations, so keys located on gateway are not used
        description.setExecutable("/usr/bin/ssh");
        description.setArguments("-i", "/dev/null", config.getCorrectLocation(), "hostname");

        Job job = jobs.submitJob(scheduler, description);

        JobStatus status = jobs.waitUntilDone(job, config.getJobTimeout(0));

        assertEquals(0, (int) status.getExitCode());
    }
}
