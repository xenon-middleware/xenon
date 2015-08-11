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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.OutputStream;

import nl.esciencecenter.xenon.adaptors.GenericJobAdaptorTestParent;
import nl.esciencecenter.xenon.files.OpenOption;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.Scheduler;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.jobs.JobCanceledException;
import nl.esciencecenter.xenon.util.Utils;

/**
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TorqueJobAdaptorTest extends GenericJobAdaptorTestParent {

    private static final Logger logger = LoggerFactory.getLogger(TorqueJobAdaptorTest.class);
    
    @BeforeClass
    public static void prepareGridEngineJobAdaptorTest() throws Exception {
        GenericJobAdaptorTestParent.prepareClass(new TorqueJobTestConfig(null));
    }

    @AfterClass
    public static void cleanupGridEngineJobAdaptorTest() throws Exception {
        GenericJobAdaptorTestParent.cleanupClass();
    }

    @org.junit.Test
    public void ge_test01_jobWithCustomScript() throws Exception {
        String message = "Hello World! test01\n";

        String workingDir = getWorkingDir("ge_test01");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        
        Path cwd = config.getWorkingDir(files, credentials);
        Path root = resolve(cwd, workingDir);
        
        files.createDirectories(root);

        Path script = resolve(root, "script");
        Path stdout = resolve(root, "stdout.txt");

        String scriptContent = "#!/bin/bash\n" + "#PBS -o " + stdout.getRelativePath().getAbsolutePath() + "\n" + "#PBS -e /dev/null\n" + "echo " + message;

        OutputStream out = files.newOutputStream(script, OpenOption.CREATE, OpenOption.APPEND, OpenOption.WRITE);
        writeFully(out, scriptContent);

        JobDescription description = new JobDescription();
        description.setInteractive(false);
        description.addJobOption("job.script", script.getRelativePath().getAbsolutePath());

        //the executable should be allowed to be null, as this field is not used at all. Check if this works
        description.setExecutable(null);

        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, 60000);

		if (status != null) {
			if (!status.isDone()) {
				throw new Exception("Job exceeded deadline!");
			}

			if (status.hasException()) {
				throw new Exception("Job failed!", status.getException());
			}
		}

        String outputContent = readFully(files.newInputStream(stdout));

        logger.debug("got output " + outputContent);

        files.delete(stdout);
        files.delete(script);
        files.delete(root);

        jobs.close(scheduler);
        files.close(cwd.getFileSystem());

        assertTrue(outputContent.equals(message));
    }

    @Test @Override
    public void test31_batchJobSubmitWithPolling() throws Exception {

        String message = "Hello World! test31";
        String workingDir = getWorkingDir("test31");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        Path cwd = config.getWorkingDir(files, credentials);
        Path root = resolve(cwd, workingDir);
        files.createDirectories(root);

        JobDescription description = new JobDescription();

        if (config.targetIsWindows()) {
            description.setExecutable("hostname");
        } else {
            description.setExecutable("/bin/echo");
            description.setArguments("-n", message);
        }
        description.setInteractive(false);
        description.setWorkingDirectory(workingDir);
        description.setStdin(null);

        Job job = jobs.submitJob(scheduler, description);

        long deadline = System.currentTimeMillis() + config.getQueueWaitTime() + config.getUpdateTime();
        long pollDelay = (config.getQueueWaitTime() + config.getUpdateTime()) / 10;

        JobStatus status = jobs.getJobStatus(job);

        while (!status.isDone()) {
            Thread.sleep(pollDelay);

            long now = System.currentTimeMillis();

            if (now > deadline) {
                throw new Exception("Job exceeded deadline!");
            }

            status = jobs.getJobStatus(job);
        }

        jobs.close(scheduler);

        if (status.hasException()) {
            throw new Exception("Job failed!", status.getException());
        }

        Path out = resolve(root, job.getJobDescription().getStdout());
        Path err = resolve(root, job.getJobDescription().getStderr());

        String tmpout = readFully(files.newInputStream(out));
        String tmperr = readFully(files.newInputStream(err));

        Utils.recursiveDelete(files, root);

        files.close(cwd.getFileSystem());

        System.err.println("STDOUT: " + tmpout);
        System.err.println("STDERR: " + tmperr);

        assertTrue(tmpout != null);

        if (config.targetIsWindows()) {
            assertTrue(tmpout.length() > 0);
        } else {
            assertTrue(tmpout.equals(message));
        }

        assertTrue(tmperr.length() == 0);
    }

    @Test @Override
    public void test32_batchJobSubmitWithWait() throws Exception {

        String message = "Hello World! test32";
        String workingDir = getWorkingDir("test32");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);

        Path cwd = config.getWorkingDir(files, credentials);
        Path root = resolve(cwd, workingDir);

        files.createDirectories(root);

        JobDescription description = new JobDescription();

        if (config.targetIsWindows()) {
            description.setExecutable("hostname");
        } else {
            description.setExecutable("/bin/echo");
            description.setArguments("-n", message);
        }

        description.setInteractive(false);
        description.setWorkingDirectory(workingDir);

        Job job = jobs.submitJob(scheduler, description);

        JobStatus status = jobs.waitUntilRunning(job, config.getQueueWaitTime());

        if (status.isRunning()) {
            status = jobs.waitUntilDone(job, config.getUpdateTime());
        }

        if (!status.isDone()) {
            throw new Exception("Job exceeded deadline!");
        }

        if (status.hasException()) {
            throw new Exception("Job failed!", status.getException());
        }

        jobs.close(scheduler);

        Path out = resolve(root, job.getJobDescription().getStdout());
        Path err = resolve(root, job.getJobDescription().getStderr());

        String tmpout = readFully(files.newInputStream(out));
        String tmperr = readFully(files.newInputStream(err));

        System.err.println("STDOUT: " + tmpout);
        System.err.println("STDERR: " + tmperr);

        assertNotNull(tmpout);
        assertNotNull(tmperr);

        if (config.targetIsWindows()) {
            assertTrue(tmpout.length() > 0);
        } else {
            assertTrue(tmpout.equals(message));
        }

        assertTrue(tmperr.length() == 0);

        Utils.recursiveDelete(files, root);

        files.close(cwd.getFileSystem());
    }

    @Override
    protected void submitToQueueWithPolling(String testName, String queueName, int jobCount) throws Exception {
        System.err.println("STARTING TEST submitToQueueWithPolling(" + testName + ", " + queueName + ", " + jobCount);

        String workingDir = getWorkingDir(testName);

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);

        Path cwd = config.getWorkingDir(files, credentials);
        Path root = resolve(cwd, workingDir);

        files.createDirectories(root);

        Job[] j = new Job[jobCount];

        for (int i = 0; i < j.length; i++) {
            JobDescription description = new JobDescription();

            if (config.targetIsWindows()) {
                description.setExecutable("ping");
                description.setArguments("-n", "2", "127.0.0.1");
            } else {
                description.setExecutable("/bin/sleep");
                description.setArguments("1");
            }

            description.setWorkingDirectory(workingDir);

            description.setQueueName(queueName);
            description.setInteractive(false);

            j[i] = jobs.submitJob(scheduler, description);
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
                            System.err.println("Job " + i + " failed!");
                            throw new Exception("Job " + i + " failed", status[i].getException());
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

                long now = System.currentTimeMillis();

                if (now > deadline) {
                    throw new Exception("Job exceeded deadline!");
                }
            }
        }

        for (Job actualJob : j) {
            Path out = resolve(root, actualJob.getJobDescription().getStdout());
            Path err = resolve(root, actualJob.getJobDescription().getStderr());
            String tmpout = readFully(files.newInputStream(out));
            String tmperr = readFully(files.newInputStream(err));
            assertTrue(tmpout != null);
            if (!config.targetIsWindows()) {
                assertTrue(tmpout.length() == 0);
            }
            assertTrue(tmperr != null);
            assertTrue(tmperr.length() == 0);
        }

        jobs.close(scheduler);
        Utils.recursiveDelete(files, root);
        files.close(cwd.getFileSystem());
    }

    @Test @Override
    public void test34_batchJobSubmitWithKill() throws Exception {

        String workingDir = getWorkingDir("test34");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);

        Path cwd = config.getWorkingDir(files, credentials);
        Path root = resolve(cwd, workingDir);

        files.createDirectories(root);

        JobDescription description = new JobDescription();

        if (config.targetIsWindows()) {
            description.setExecutable("ping");
            description.setArguments("-n", "61", "127.0.0.1");
        } else {
            description.setExecutable("/bin/sleep");
            description.setArguments("60");
        }

        description.setInteractive(false);
        description.setWorkingDirectory(workingDir);
        description.setStdin(null);

        // We immediately kill the job. Hopefully it isn't running yet!
        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.cancelJob(job);

        // Wait until the job is killed. We assume it takes less than a minute!
        if (!status.isDone()) {
            status = jobs.waitUntilDone(job, config.getUpdateTime());
        }

        if (!status.isDone()) {
            throw new Exception("Failed to kill job! Expected status done, but job status is " + status);
        }

        jobs.close(scheduler);

        Utils.recursiveDelete(files, root);
        files.close(cwd.getFileSystem());

        assertTrue(status.hasException());
        Exception e = status.getException();

        if (!(e instanceof JobCanceledException)) {
            throw new Exception("test34 expected JobCanceledException, not " + e.getMessage(), e);
        }
    }

    @Test @Override
    public void test35_batchJobSubmitWithKill2() throws Exception {

        String workingDir = getWorkingDir("test35");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);

        Path cwd = config.getWorkingDir(files, credentials);
        Path root = resolve(cwd, workingDir);

        files.createDirectories(root);

        JobDescription description = new JobDescription();

        if (config.targetIsWindows()) {
            description.setExecutable("ping");
            description.setArguments("-n", "61", "127.0.0.1");
        } else {
            description.setExecutable("/bin/sleep");
            description.setArguments("600");
        }

        description.setInteractive(false);
        description.setWorkingDirectory(workingDir);

        Job job = jobs.submitJob(scheduler, description);

        // Wait for job to run before killing it!
        JobStatus status = jobs.waitUntilRunning(job, config.getQueueWaitTime());

        if (!status.isRunning()) {
            throw new Exception("Job failed to start! Expected status running, but job status is " + status);
        }

        status = jobs.cancelJob(job);

        // Wait until the job is killed. We assume it takes less than a minute!
        if (!status.isDone()) {
            status = jobs.waitUntilDone(job, config.getUpdateTime());
        }

        if (!status.isDone()) {
            throw new Exception("Failed to kill job! Expected status done, but job status is " + status);
        }

        jobs.close(scheduler);

		Utils.recursiveDelete(files, root);
        files.close(cwd.getFileSystem());

        assertTrue(status.hasException());
        Exception e = status.getException();

        assertTrue(e instanceof JobCanceledException);
    }

    @Test @Override
    public void test41_batchJobSubmitWithEnvironmentVariable() throws Exception {

        if (!config.supportsEnvironmentVariables()) {
            return;
        }

        if (config.targetIsWindows()) {
            return;
        }

        String workingDir = getWorkingDir("test41");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);

        Path cwd = config.getWorkingDir(files, credentials);
        Path root = resolve(cwd, workingDir);

        files.createDirectories(root);

        //echo the given variable, to see if the va
        JobDescription description = new JobDescription();
        description.setExecutable("/usr/bin/printenv");
        description.setArguments("SOME_VARIABLE");
        description.setInteractive(false);
        description.addEnvironment("SOME_VARIABLE", "some_value");

        description.setWorkingDirectory(workingDir);

        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, 60000);

        if (!status.isDone()) {
            throw new Exception("Job exceeded deadline!");
        }

        if (status.hasException()) {
            throw new Exception("Job failed!", status.getException());
        }

        Path stdout = resolve(root, job.getJobDescription().getStdout());

        String stdoutContent = readFully(files.newInputStream(stdout));

        assertTrue(stdoutContent.equals("some_value\n"));

        Utils.recursiveDelete(files, root);
        files.close(cwd.getFileSystem());
    }

    @Test @Override
    public void test43_submit_JobDescriptionShouldBeCopied_Success() throws Exception {

        String workingDir = getWorkingDir("test43");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);

        Path cwd = config.getWorkingDir(files, credentials);
        Path root = resolve(cwd, workingDir);

        files.createDirectories(root);

        JobDescription description = new JobDescription();
        description.setExecutable("non-existing-executable");
        description.setInteractive(false);
        description.setWorkingDirectory(workingDir);

        Job job = jobs.submitJob(scheduler, description);

        description.setWorkingDirectory(workingDir + ".new");

        JobDescription original = job.getJobDescription();

        assertEquals("Job description should have been copied!", workingDir, original.getWorkingDirectory());

        JobStatus status = jobs.cancelJob(job);

        if (!status.isDone()) {
            jobs.waitUntilDone(job, 60000);
        }

		Utils.recursiveDelete(files, root);
        files.close(cwd.getFileSystem());
    }
}
