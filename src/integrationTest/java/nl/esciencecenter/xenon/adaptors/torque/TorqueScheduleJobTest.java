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

package nl.esciencecenter.xenon.adaptors.torque;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import nl.esciencecenter.xenon.JobException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.GenericScheduleJobTestParent;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.jobs.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobCanceledException;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TorqueScheduleJobTest extends GenericScheduleJobTestParent {
    private static final Logger logger = LoggerFactory.getLogger(TorqueScheduleJobTest.class);
    
    @BeforeClass
    public static void prepareTorqueScheduleJobTest() throws Exception {
        GenericScheduleJobTestParent.prepareClass(new TorqueJobTestConfig(null));
    }

    @AfterClass
    public static void cleanupTorqueScheduleJobTest() throws Exception {
        GenericScheduleJobTestParent.cleanupClass();
    }

    protected JobDescription simpleJobDescription(String workingDirectory) {
        JobDescription description = new JobDescription();
        
        if (config.targetIsWindows()) { 
            description.setExecutable("hostname");
        } else { 
            description.setExecutable("echo");
            description.setArguments("some message");
        }

        description.setWorkingDirectory(workingDirectory);
        return description;
    }

    @org.junit.Test
    public void ge_test01_jobWithCustomScript() throws Exception {
        String message = "Hello World! test01\n";
        String workingDir = getWorkingDir("ge_test01");
        Path root = initJobDirectory(workingDir);
        Path script = resolve(root, "script");
        
        try {
            String scriptContent = "#!/bin/bash\n" + "\necho " + message;
            writeFully(script, scriptContent);

            JobDescription description = new JobDescription();
            description.addJobOption("job.script", script.getRelativePath().getAbsolutePath());

            //the executable should be allowed to be null, as this field is not used at all. Check if this works
            description.setExecutable(null);

            job = jobs.submitJob(scheduler, description);
            JobStatus status = jobs.waitUntilDone(job, 60000);

            checkJobDone(status);
            checkJobOutput(job, root, message);
        } finally {
            cleanupJob(job, root, script);
        }
    }

    @Test @Override
    public void test31_batchJobSubmitWithPolling() throws Exception {
        String message = "Hello World! test31";
        String workingDir = getWorkingDir("test31");
        Path root = initJobDirectory(workingDir);
        
        try {
            JobDescription description = echoJobDescription(workingDir, message);
            job = jobs.submitJob(scheduler, description);

            long deadline = System.currentTimeMillis() + config.getQueueWaitTime() + config.getUpdateTime();
            long pollDelay = (config.getQueueWaitTime() + config.getUpdateTime()) / 10;

            JobStatus status = jobs.getJobStatus(job);

            while (!status.isDone()) {
                Thread.sleep(pollDelay);
                assertTrue("Job exceeded deadline!", System.currentTimeMillis() < deadline);
                status = jobs.getJobStatus(job);
            }

            if (status.hasException()) {
                throw new JobException("Job failed!", status.getException());
            }

            checkJobOutput(job, root, message);
        } finally {
            cleanupJob(job, root);
        }
    }

    @Test @Override
    public void test32_batchJobSubmitWithWait() throws Exception {
        String message = "Hello World! test32";
        String workingDir = getWorkingDir("test32");
        Path root = initJobDirectory(workingDir);

        try {
            job = jobs.submitJob(scheduler, echoJobDescription(workingDir, message));
            JobStatus status = jobs.waitUntilRunning(job, config.getQueueWaitTime());

            if (status.isRunning()) {
                status = jobs.waitUntilDone(job, config.getUpdateTime());
            }
            
            checkJobDone(status);
            checkJobOutput(job, root, message);
        } finally {
            cleanupJob(job, root);
        }
    }

    @Override
    protected void submitToQueueWithPolling(String testName, String queueName, int jobCount) throws Exception {
        logger.info("STARTING TEST submitToQueueWithPolling(" + testName + ", " + queueName + ", " + jobCount);

        String workingDir = getWorkingDir(testName);
        Path root = initJobDirectory(workingDir);
        Job[] j = new Job[jobCount];
        Job[] jCopy = new Job[jobCount];

        try {
            for (int i = 0; i < j.length; i++) {
                JobDescription description = simpleJobDescription(workingDir);
                description.setQueueName(queueName);

                jCopy[i] = j[i] = jobs.submitJob(scheduler, description);
            }

            // Bit hard to determine realistic deadline here ?
            long deadline = System.currentTimeMillis() + config.getQueueWaitTime() + (jobCount * config.getUpdateTime());

            boolean done = false;
            Job[] jUpdate = new Job[j.length];
            System.arraycopy(j, 0, jUpdate, 0, j.length);

            while (!done) {
                JobStatus[] status = jobs.getJobStatuses(j);

                int count = 0;

                for (int i = 0; i < j.length; i++) {
                    if (jUpdate[i] != null) {
                        if (status[i].isDone()) {
                            if (status[i].hasException()) {
                                throw new JobException("Job " + i + " failed", status[i].getException());
                            }

                            System.err.println("Job " + i + " done.");
                            jUpdate[i] = null;
                        } else {
                            count++;
                        }
                    }
                }

                if (count == 0) {
                    done = true;
                } else {
                    Thread.sleep(1000);
                    assertTrue("Job exceeded deadline!", System.currentTimeMillis() < deadline);
                }
            }
            for (Job actualJob : jCopy) {
                checkJobOutput(actualJob, root);
            }
        } finally {
            cleanupJobRecursive(root);
        }
    }

    @Test @Override
    public void test34_batchJobSubmitWithKill() throws Exception {

        String workingDir = getWorkingDir("test34");
        Path root = initJobDirectory(workingDir);

        try {
            // We immediately kill the job. Hopefully it isn't running yet!
            job = jobs.submitJob(scheduler, simpleJobDescription(workingDir));
            JobStatus status = jobs.cancelJob(job);

            // Wait until the job is killed. We assume it takes less than a minute!
            if (!status.isDone()) {
                status = jobs.waitUntilDone(job, config.getUpdateTime());
            }

            assertTrue("Failed to kill job! Expected status done, but job status is " + status, status.isDone());
            assertTrue(status.hasException());
            Exception e = status.getException();

            if (!(e instanceof JobCanceledException)) {
                throw new XenonException(TorqueAdaptor.ADAPTOR_NAME, "test34 expected JobCanceledException, not " + e.getMessage(), e);
            }
        } finally {
            cleanupJobRecursive(root);
        }
    }

    @Test @Override
    public void test35_batchJobSubmitWithKill2() throws Exception {
        String workingDir = getWorkingDir("test35");
        Path root = initJobDirectory(workingDir);
        
        try {
            job = jobs.submitJob(scheduler, timedJobDescription(workingDir, 15));

            // Wait for job to run before killing it!
            JobStatus status = jobs.waitUntilRunning(job, config.getQueueWaitTime());

            assertTrue("Job failed to start! Expected status running, but job status is " + status, status.isRunning());

            status = jobs.cancelJob(job);

            // Wait until the job is killed. We assume it takes less than a minute!
            if (!status.isDone()) {
                status = jobs.waitUntilDone(job, config.getUpdateTime());
            }

            assertTrue("Failed to kill job! Expected status done, but job status is " + status, status.isDone());
            assertTrue(status.hasException());
            Exception e = status.getException();
            assertTrue(e instanceof JobCanceledException);
        } finally {
            cleanupJob(job, root);
        }
    }

    @Test(expected=InvalidJobDescriptionException.class) @Override
    public void test36a_batchJobSubmitWithInput() throws Exception {
        super.test36a_batchJobSubmitWithInput();
    }

    @Test(expected=InvalidJobDescriptionException.class) @Override
    public void test36b_batchJobSubmitWithInput() throws Exception {
        super.test36b_batchJobSubmitWithInput();
    }

    @Test @Override
    public void test37b_batchJobSubmitWithRelativeWorkDir() throws Exception {
        String workingDir = "test37b";
        runJob(workingDir, simpleJobDescription(workingDir), null);
    }

    @Test @Override
    public void test37c_batchJobSubmitWithAbsoluteWorkDir() throws Exception {
        String workingDir = getWorkingDir("test37c");
        runJob(workingDir, simpleJobDescription(workingDir), null);
    }

    @Test @Override
    public void test37e_batchJobSubmitWithWorkDirWithSpaces() throws Exception {
        //note the space in the path
        String workingDir = getWorkingDir("test 37b");
        runJob(workingDir, simpleJobDescription(workingDir), null);
    }

    @Test @Override
    public void test41_batchJobSubmitWithEnvironmentVariable() throws Exception {
        if (!config.supportsEnvironmentVariables() || config.targetIsWindows()) {
            return;
        }
        
        String workingDir = getWorkingDir("test41");
        JobDescription description = printEnvJobDescription(workingDir, "some_value");
        runJob(workingDir, description, "some_value\n");
    }

    @Test @Override
    public void test43_submit_JobDescriptionShouldBeCopied_Success() throws Exception {
        String workingDir = getWorkingDir("test43");
        Path root = initJobDirectory(workingDir);

        try {
            JobDescription description = new JobDescription();
            description.setExecutable("non-existing-executable");
            description.setWorkingDirectory(workingDir);

            job = jobs.submitJob(scheduler, description);

            description.setWorkingDirectory(workingDir + ".new");

            JobDescription original = job.getJobDescription();

            assertEquals("Job description should have been copied!", workingDir, original.getWorkingDirectory());

            JobStatus status = jobs.cancelJob(job);

            if (!status.isDone()) {
                jobs.waitUntilDone(job, 60000);
            }
        } finally {
            cleanupJob(job, root);
        }
    }
}
