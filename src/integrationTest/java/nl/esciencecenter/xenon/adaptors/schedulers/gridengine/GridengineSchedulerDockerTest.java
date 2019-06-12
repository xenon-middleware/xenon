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
package nl.esciencecenter.xenon.adaptors.schedulers.gridengine;

import static nl.esciencecenter.xenon.adaptors.schedulers.ssh.SshSchedulerAdaptor.LOAD_STANDARD_KNOWN_HOSTS;
import static nl.esciencecenter.xenon.adaptors.schedulers.ssh.SshSchedulerAdaptor.STRICT_HOST_KEY_CHECKING;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.SchedulerLocationConfig;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;
import nl.esciencecenter.xenon.schedulers.Scheduler;
import nl.esciencecenter.xenon.utils.LocalFileSystemUtils;

public class GridengineSchedulerDockerTest extends GridengineSchedulerTestParent {

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder().file("src/integrationTest/resources/docker-compose/gridengine-6.2.yml")
            .waitingForService("gridengine", HealthChecks.toHaveAllPortsOpen()).build();

    @Override
    protected SchedulerLocationConfig setupLocationConfig() {
        return new GridengineLocationConfig(docker.containers().container("gridengine").port(22).inFormat("ssh://$HOST:$EXTERNAL_PORT"), "/home/xenon");
    }

    @Override
    public Scheduler setupScheduler(SchedulerLocationConfig config) throws XenonException {
        // String location = docker.containers().container("gridengine").port(22).inFormat("ssh://$HOST:$EXTERNAL_PORT");
        PasswordCredential cred = new PasswordCredential("xenon", "javagat".toCharArray());
        Map<String, String> props = new HashMap<>();
        props.put(LOAD_STANDARD_KNOWN_HOSTS, "false");
        props.put(STRICT_HOST_KEY_CHECKING, "false");
        return Scheduler.create("gridengine", config.getLocation(), cred, props);
    }

    @Test
    public void test_submitBatch_peUsingSchedulerArg() throws XenonException {
        // This test does not run on windows.
        assumeFalse("Test only suited for linux", scheduler.getAdaptorName().equals("local") && (LocalFileSystemUtils.isWindows()));
        assumeTrue(description.supportsBatch());

        JobDescription job = new JobDescription();
        job.setExecutable("/bin/hostname");
        job.addSchedulerArgument("-pe smp 2");

        String jobID = scheduler.submitBatchJob(job);

        JobStatus status = waitUntilDone(jobID);

        assertTrue("Job is not done yet", status.isDone());
    }

    @Test
    public void test_submitBatch_peUsingCoreCount() throws XenonException {
        // This test does not run on windows.
        assumeFalse("Test only suited for linux", scheduler.getAdaptorName().equals("local") && (LocalFileSystemUtils.isWindows()));
        assumeTrue(description.supportsBatch());

        JobDescription job = new JobDescription();
        job.setExecutable("/bin/hostname");
        job.setCoresPerTask(2);
        job.setStartPerJob();

        String jobID = scheduler.submitBatchJob(job);

        JobStatus status = waitUntilDone(jobID);

        assertTrue("Job is not done yet", status.isDone());
    }

}
