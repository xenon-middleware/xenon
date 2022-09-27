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
package nl.esciencecenter.xenon.adaptors.schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.UnsupportedOperationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;
import nl.esciencecenter.xenon.schedulers.NoSuchJobException;
import nl.esciencecenter.xenon.schedulers.NoSuchQueueException;
import nl.esciencecenter.xenon.schedulers.QueueStatus;
import nl.esciencecenter.xenon.schedulers.Scheduler;
import nl.esciencecenter.xenon.schedulers.SchedulerAdaptorDescription;
import nl.esciencecenter.xenon.schedulers.Streams;
import nl.esciencecenter.xenon.utils.LocalFileSystemUtils;
import nl.esciencecenter.xenon.utils.OutputReader;

public abstract class SchedulerTestParent {

    public static final Logger LOGGER = LoggerFactory.getLogger(SchedulerTestParent.class);

    protected Scheduler scheduler;
    protected SchedulerAdaptorDescription description;
    private SchedulerLocationConfig locationConfig;

    @Before
    public void setup() throws XenonException {
        locationConfig = setupLocationConfig();
        scheduler = setupScheduler(locationConfig);
        description = setupDescription();
    }

    protected abstract SchedulerLocationConfig setupLocationConfig();

    @After
    public void cleanup() throws XenonException {
        if (scheduler != null && scheduler.isOpen()) {
            scheduler.close();
        }
    }

    public abstract Scheduler setupScheduler(SchedulerLocationConfig config) throws XenonException;

    private SchedulerAdaptorDescription setupDescription() throws XenonException {
        String name = scheduler.getAdaptorName();
        return Scheduler.getAdaptorDescription(name);
    }

    @Test
    public void test_close() throws XenonException {
        scheduler.close();
        assertFalse(scheduler.isOpen());
    }

    @Test
    public void test_getLocation() throws XenonException {

        String location = scheduler.getLocation();

        assertEquals(locationConfig.getLocation(), location);
    }

    @Test
    public void test_isEmbedded() throws XenonException {
        Assert.assertEquals(locationConfig.isEmbedded(), description.isEmbedded());
    }

    @Test
    public void test_supportsBatch() throws XenonException {
        Assert.assertEquals(locationConfig.supportsBatch(), description.supportsBatch());
    }

    @Test
    public void test_supportsInteractive() throws XenonException {
        Assert.assertEquals(locationConfig.supportsInteractive(), description.supportsInteractive());
    }

    private boolean contains(String[] options, String expected) {

        if (options == null || options.length == 0) {
            return false;
        }

        for (String s : options) {
            if (expected == null) {
                if (s == null) {
                    return true;
                }
            } else {
                if (expected.equals(s)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean unorderedEquals(String[] expected, String[] actual) {

        if (expected.length != actual.length) {
            return false;
        }

        for (String s : expected) {
            if (!contains(actual, s)) {
                return false;
            }
        }

        for (String s : actual) {
            if (!contains(expected, s)) {
                return false;
            }
        }

        return true;
    }

    private JobStatus waitUntilRunning(String jobID) throws XenonException {
        return waitUntilRunning(jobID, locationConfig.getMaxWaitUntilRunning());
    }

    private JobStatus waitUntilRunning(String jobID, long maxTime) throws XenonException {

        JobStatus status = scheduler.getJobStatus(jobID);

        long deadline = System.currentTimeMillis() + maxTime;

        while (!status.isRunning() && !status.isDone() && System.currentTimeMillis() < deadline) {
            // System.out.println("Current jobs: " + Arrays.toString(scheduler.getJobs()));
            status = scheduler.waitUntilRunning(jobID, 1000);
        }

        return status;
    }

    protected JobStatus waitUntilDone(String jobID) throws XenonException {
        return waitUntilDone(jobID, locationConfig.getMaxWaintUntilDone());
    }

    private JobStatus waitUntilDone(String jobID, long maxTime) throws XenonException {

        JobStatus status = scheduler.getJobStatus(jobID);

        long deadline = System.currentTimeMillis() + maxTime;

        while (!status.isDone() && System.currentTimeMillis() < deadline) {
            // System.out.println("Current jobs: " + Arrays.toString(scheduler.getJobs()));
            status = scheduler.waitUntilDone(jobID, 1000);
        }

        return status;
    }

    private void cleanupJobs(String... jobIDs) throws XenonException {

        if (jobIDs == null || jobIDs.length == 0) {
            return;
        }

        JobStatus[] stats = new JobStatus[jobIDs.length];

        for (int i = 0; i < jobIDs.length; i++) {
            if (jobIDs[i] != null) {
                stats[i] = scheduler.cancelJob(jobIDs[i]);
            }
        }

        for (int i = 0; i < jobIDs.length; i++) {
            if (stats[i] != null) {
                if (!stats[i].isDone()) {
                    stats[i] = waitUntilDone(jobIDs[i]);
                }
            }
        }

        for (int i = 0; i < jobIDs.length; i++) {
            if (stats[i] != null && !stats[i].isDone()) {
                throw new XenonException("TEST", "Job " + jobIDs[i] + " not done yet!");
            }
        }
    }

    private void cleanupJob(String jobID) throws XenonException {

        JobStatus status = null;

        // Clean up the mess..
        status = scheduler.cancelJob(jobID);

        if (!status.isDone()) {
            status = waitUntilDone(jobID);
        }

        if (!status.isDone()) {
            throw new XenonException("TEST", "Job " + jobID + " not done yet!");
        }
    }

    @Test
    public void test_getQueueNames() throws XenonException {
        assertTrue(unorderedEquals(locationConfig.getQueueNames(), scheduler.getQueueNames()));
    }

    @Test
    public void test_getDefaultQueueNames() throws XenonException {
        Assert.assertEquals(locationConfig.getDefaultQueueName(), scheduler.getDefaultQueueName());
    }

    private JobDescription getSleepJob(String queue, int time) {

        JobDescription job = new JobDescription();

        if (scheduler.getAdaptorName().equals("local") && LocalFileSystemUtils.isWindows()) {
            // We are testing on a local windows scheduler
            job.setExecutable("C:\\Windows\\System32\\timeout.exe");
        } else {
            // Assume linux / mac target
            job.setExecutable("/bin/sleep");
        }

        job.setArguments("" + time);

        if (queue != null) {
            job.setQueueName(queue);
        }

        return job;
    }

    @Test
    public void test_sleep() throws XenonException {

        assumeTrue(description.supportsBatch());
        String jobID = scheduler.submitBatchJob(getSleepJob(null, 1));

        JobStatus status = waitUntilDone(jobID);

        assertTrue("Job is not done yet", status.isDone());
    }

    @Test
    public void test_cancel() throws XenonException {

        assumeTrue(description.supportsBatch());

        String jobID = scheduler.submitBatchJob(getSleepJob(null, 240));

        // Wait until the job is running.
        JobStatus status = waitUntilRunning(jobID);

        assertFalse("Job is already done", status.isDone());
        assertTrue("Job is not running yet", status.isRunning());

        status = scheduler.cancelJob(jobID);

        if (!status.isDone()) {
            // Wait up until the job is completely done
            status = waitUntilDone(jobID);
        }

        assertTrue(status.isDone());
    }

    @Test
    public void test_getJobsQueueNameEmpty() throws XenonException {

        assumeTrue(description.supportsBatch());

        // Get the available queues
        String[] queueNames = locationConfig.getQueueNames();

        assumeTrue(queueNames != null);
        assumeTrue(queueNames.length > 0);

        // Submit job of 5 seconds to first queue
        String jobID = scheduler.submitBatchJob(getSleepJob(queueNames[0], 5));

        // Retrieve all jobs
        String[] jobs = scheduler.getJobs();

        // Our job should be part of this
        assertTrue(contains(jobs, jobID));

        // Clean up the mess.
        cleanupJob(jobID);
    }

    @Test
    public void test_getJobsQueueNameNull() throws XenonException {

        assumeTrue(description.supportsBatch());

        // Get the available queues
        String[] queueNames = locationConfig.getQueueNames();

        assumeTrue(queueNames != null);
        assumeTrue(queueNames.length > 0);

        // Submit job of 5 seconds to first queue
        String jobID = scheduler.submitBatchJob(getSleepJob(queueNames[0], 5));

        // Retrieve all jobs
        String[] jobs = scheduler.getJobs(new String[0]);

        // Our job should be part of this
        assertTrue(contains(jobs, jobID));

        // Clean up the mess...
        cleanupJob(jobID);
    }

    @Test
    public void test_getJobsQueueNameCorrect() throws XenonException {

        assumeTrue(description.supportsBatch());

        // Get the available queues
        String[] queueNames = locationConfig.getQueueNames();

        assumeTrue(queueNames != null);
        assumeTrue(queueNames.length > 0);

        // Submit it
        String jobID = scheduler.submitBatchJob(getSleepJob(queueNames[0], 5));

        // Retrieve all jobs
        String[] jobs = scheduler.getJobs(queueNames[0]);

        // Our job should be part of this
        assertTrue(contains(jobs, jobID));

        // Clean up the mess...
        cleanupJob(jobID);
    }

    @Test
    public void test_getJobsQueueNameOtherQueue() throws XenonException {

        assumeTrue(description.supportsBatch());

        // Get the available queues
        String[] queueNames = locationConfig.getQueueNames();

        assumeTrue(queueNames != null);
        assumeTrue(queueNames.length > 1);

        // Submit job to one queue
        String jobID = scheduler.submitBatchJob(getSleepJob(queueNames[0], 5));

        // Retrieve all jobs for other queue
        String[] jobs = scheduler.getJobs(queueNames[1]);

        // Our job should NOT be part of this
        assertFalse(contains(jobs, jobID));

        // Clean up the mess...
        cleanupJob(jobID);
    }

    @Test(expected = NoSuchQueueException.class)
    public void test_getJobsQueueNameInCorrect() throws XenonException {
        scheduler.getJobs("foobar");
    }

    @Test(expected = NoSuchJobException.class)
    public void test_getJobStatus_unknownJob() throws XenonException {
        scheduler.getJobStatus("aap");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_getJobStatus_null() throws XenonException {
        scheduler.getJobStatus(null);
    }

    @Test
    public void test_getJobStatus_knownJob() throws XenonException {

        assumeTrue(description.supportsBatch());

        // Get the available queues
        String[] queueNames = locationConfig.getQueueNames();

        assumeTrue(queueNames != null);
        assumeTrue(queueNames.length >= 1);

        // Submit job to one queue
        String jobID = scheduler.submitBatchJob(getSleepJob(queueNames[0], 1));

        JobStatus status = scheduler.getJobStatus(jobID);

        assertNotNull(status);
        Assert.assertEquals(jobID, status.getJobIdentifier());
        assertFalse(status.isDone());

        // Clean up the mess...
        cleanupJob(jobID);
    }

    @Test
    public void test_getJobStatus_finishedJob() throws XenonException {

        assumeFalse(description.isEmbedded());
        assumeTrue(description.supportsBatch());

        // Get the available queues
        String[] queueNames = locationConfig.getQueueNames();

        assumeTrue(queueNames != null);
        assumeTrue(queueNames.length >= 1);

        // Submit job to one queue
        String jobID = scheduler.submitBatchJob(getSleepJob(queueNames[0], 1));

        JobStatus status = scheduler.waitUntilDone(jobID, 5000);
        assertNotNull(status);
        Assert.assertEquals(jobID, status.getJobIdentifier());

        // NOTE: Some schedulers have issues changing the job status to "finished" quickly enough. Therefore, we retry a number of times.
        int retry = 1;

        while (retry < 12 && !status.isDone()) {
            retry++;
            status = scheduler.waitUntilDone(jobID, 5000);
            assertNotNull(status);
            Assert.assertEquals(jobID, status.getJobIdentifier());
        }

        assertTrue(status.isDone());

        // Wait for a while and see if we can still get the job info.
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            // ignored
        }

        status = scheduler.getJobStatus(jobID);

        assertNotNull(status);
        Assert.assertEquals(jobID, status.getJobIdentifier());
        assertTrue(status.isDone());
    }

    @Test
    public void test_getJobStatuses_noJobs() throws XenonException {

        // Get the status of no jobs
        JobStatus[] result = scheduler.getJobStatuses();
        assertNotNull(result);
        assertTrue(result.length == 0);
    }

    @Test
    public void test_getJobStatuses_nonExistingJobs() throws XenonException {

        // Get the status of no jobs
        JobStatus[] result = scheduler.getJobStatuses("aap", "noot");
        assertNotNull(result);
        assertTrue(result.length == 2);

        assertNotNull(result[0]);
        Assert.assertEquals("aap", result[0].getJobIdentifier());
        assertTrue(result[0].hasException());

        assertNotNull(result[1]);
        Assert.assertEquals("noot", result[1].getJobIdentifier());
        assertTrue(result[1].hasException());
    }

    @Test
    public void test_getJobStatuses_nonExistingJobsWithNull() throws XenonException {

        // Get the status of no jobs
        JobStatus[] result = scheduler.getJobStatuses("aap", null, "noot");
        assertNotNull(result);
        assertTrue(result.length == 3);

        assertNotNull(result[0]);
        Assert.assertEquals("aap", result[0].getJobIdentifier());
        assertTrue(result[0].hasException());

        assertNull(result[1]);

        assertNotNull(result[2]);
        Assert.assertEquals("noot", result[2].getJobIdentifier());
        assertTrue(result[2].hasException());
    }

    @Test
    public void test_getJobStatuses_existingJobs() throws XenonException {

        assumeTrue(description.supportsBatch());

        // Get the available queues
        String[] queueNames = locationConfig.getQueueNames();

        assumeTrue(queueNames != null);
        assumeTrue(queueNames.length >= 1);

        // Submit two jobs to queue
        String jobID1 = scheduler.submitBatchJob(getSleepJob(queueNames[0], 5));
        String jobID2 = scheduler.submitBatchJob(getSleepJob(queueNames[0], 5));

        // Get the status of no jobs
        JobStatus[] result = scheduler.getJobStatuses(jobID1, jobID2);

        assertNotNull(result);
        assertTrue(result.length == 2);

        assertNotNull(result[0]);
        Assert.assertEquals(jobID1, result[0].getJobIdentifier());
        assertFalse(result[0].isDone());

        assertNotNull(result[1]);
        Assert.assertEquals(jobID2, result[1].getJobIdentifier());
        assertFalse(result[1].isDone());

        // Clean up the mess...
        cleanupJobs(jobID1, jobID2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_getQueueStatus_null() throws XenonException {
        scheduler.getQueueStatus(null);
    }

    @Test(expected = NoSuchQueueException.class)
    public void test_getQueueStatus_unknownQueue() throws XenonException {
        scheduler.getQueueStatus("aap");
    }

    @Test
    public void test_getQueueStatus_knownQueue() throws XenonException {

        // Get the available queues
        String[] queueNames = locationConfig.getQueueNames();

        assumeTrue(queueNames != null);
        assumeTrue(queueNames.length >= 1);

        QueueStatus status = scheduler.getQueueStatus(queueNames[0]);

        assertNotNull(status);
        Assert.assertEquals(queueNames[0], status.getQueueName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_getQueueStatuses_null() throws XenonException {
        scheduler.getQueueStatuses((String[]) null);
    }

    @Test
    public void test_getQueueStatuses_empty() throws XenonException {
        String[] queueNames = locationConfig.getQueueNames();

        assumeTrue(queueNames != null);
        assumeTrue(queueNames.length >= 1);

        QueueStatus[] result = scheduler.getQueueStatuses(new String[0]);

        assertNotNull(result);
        assertTrue(queueNames.length == result.length);

        for (int i = 0; i < queueNames.length; i++) {
            assertNotNull(result[i]);
            Assert.assertEquals(queueNames[i], result[i].getQueueName());
            assertFalse(result[i].hasException());
        }
    }

    @Test
    public void test_getQueueStatuses_allQueues() throws XenonException {
        String[] queueNames = locationConfig.getQueueNames();

        assumeTrue(queueNames != null);
        assumeTrue(queueNames.length >= 1);

        QueueStatus[] result = scheduler.getQueueStatuses(queueNames);

        assertNotNull(result);
        assertTrue(queueNames.length == result.length);

        for (int i = 0; i < queueNames.length; i++) {
            assertNotNull(result[i]);
            Assert.assertEquals(queueNames[i], result[i].getQueueName());
            assertFalse(result[i].hasException());
        }
    }

    @Test
    public void test_getQueueStatuses_withNull() throws XenonException {
        String[] queueNames = locationConfig.getQueueNames();

        assumeTrue(queueNames != null);
        assumeTrue(queueNames.length > 1);

        String[] alt = new String[queueNames.length + 1];

        alt[0] = queueNames[0];
        alt[1] = null;

        for (int i = 1; i < queueNames.length; i++) {
            alt[i + 1] = queueNames[i];
        }

        QueueStatus[] result = scheduler.getQueueStatuses(alt);

        assertNotNull(result);
        assertTrue(alt.length == result.length);

        assertNotNull(result[0]);
        Assert.assertEquals(queueNames[0], result[0].getQueueName());

        assertNull(result[1]);

        for (int i = 1; i < queueNames.length; i++) {
            assertNotNull(result[i + 1]);
            Assert.assertEquals(queueNames[i], result[i + 1].getQueueName());
            assertFalse(result[i + 1].hasException());
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_interactiveJob_notSupported_throwsException() throws XenonException {

        assumeFalse(description.supportsInteractive());

        JobDescription job = new JobDescription();
        job.setExecutable("/bin/cat");

        scheduler.submitInteractiveJob(job);
    }

    @Test(expected = NoSuchQueueException.class)
    public void test_interactiveJobToNonExistantQueue() throws Exception {

        assumeTrue(description.supportsInteractive());

        // Do not run this test on the local adaptor, but only remote.
        assumeFalse(scheduler.getAdaptorName().equals("local"));

        JobDescription job = new JobDescription();
        job.setExecutable("/bin/cat");
        job.setQueueName("noSuchQueue");

        Streams streams = scheduler.submitInteractiveJob(job);

        OutputReader out = new OutputReader(streams.getStdout());
        OutputReader err = new OutputReader(streams.getStderr());

        OutputStream stdin = streams.getStdin();

        stdin.write("Hello World\n".getBytes());
        stdin.write("Goodbye World\n".getBytes());
        stdin.close();

        out.waitUntilFinished();
        err.waitUntilFinished();

        Assert.assertEquals("Hello World\nGoodbye World\n", out.getResultAsString());

        cleanupJob(streams.getJobIdentifier());
    }

    @Test
    public void test_interactiveJob() throws Exception {

        assumeTrue(description.supportsInteractive());

        // Do not run this test on windows, as /bin/cat does not exist
        assumeFalse(scheduler.getAdaptorName().equals("local") && LocalFileSystemUtils.isWindows());

        JobDescription job = new JobDescription();
        job.setExecutable("/bin/cat");

        Streams streams = scheduler.submitInteractiveJob(job);

        OutputReader out = new OutputReader(streams.getStdout());
        OutputReader err = new OutputReader(streams.getStderr());

        OutputStream stdin = streams.getStdin();

        stdin.write("Hello World\n".getBytes());
        stdin.write("Goodbye World\n".getBytes());
        stdin.close();

        out.waitUntilFinished();
        err.waitUntilFinished();

        Assert.assertEquals("Hello World\nGoodbye World\n", out.getResultAsString());

        cleanupJob(streams.getJobIdentifier());
    }

    @Test
    public void test_interactiveJob_windows() throws Exception {

        assumeTrue(description.supportsInteractive());

        // Only runthis test on windows and in the local adaptor
        assumeTrue(scheduler.getAdaptorName().equals("local") && LocalFileSystemUtils.isWindows());

        JobDescription job = new JobDescription();
        job.setExecutable("C:\\Windows\\System32\\more.com");

        Streams streams = scheduler.submitInteractiveJob(job);

        OutputReader out = new OutputReader(streams.getStdout());
        OutputReader err = new OutputReader(streams.getStderr());

        OutputStream stdin = streams.getStdin();

        stdin.write("Hello World\r\n".getBytes());
        stdin.write("Goodbye World\r\n".getBytes());
        stdin.close();

        out.waitUntilFinished();
        err.waitUntilFinished();

        // Note that more add an extra newlin on windows
        Assert.assertEquals("Hello World\r\nGoodbye World\r\n\r\n", out.getResultAsString());

        cleanupJob(streams.getJobIdentifier());
    }

    @Test
    public void test_workdir() throws Exception {

        // We need a scheduler that actually has a filesystem underneath.
        assumeTrue(description.usesFileSystem());

        FileSystem fs = scheduler.getFileSystem();
        Path p = fs.getWorkingDirectory();

        Assert.assertEquals(locationConfig.getWorkdir(), p.toString());
    }

    @Test
    public void test_workdir_usage_linux() throws Exception {

        // We need a scheduler that actually has a filesystem underneath.
        assumeTrue("Scheduler does not use filesystem", description.usesFileSystem());

        FileSystem fs = scheduler.getFileSystem();

        // This test does not run on windows.
        assumeFalse("Test only suited for linux", scheduler.getAdaptorName().equals("local") && (LocalFileSystemUtils.isWindows()));

        String command = "/bin/touch";

        if (LocalFileSystemUtils.isOSX() && scheduler.getAdaptorName().equals("local")) {
            command = "/usr/bin/touch";
        }

        assertTrue("Working dir does not exist", fs.exists(fs.getWorkingDirectory()));

        Path testDir = fs.getWorkingDirectory().resolve(new Path(false, "test_workdir_usage"));

        assertFalse("Test dir already exists", fs.exists(testDir));

        try {
            fs.createDirectory(testDir);

            assertTrue("Failed to create test directory", fs.exists(testDir));

            JobDescription job = new JobDescription();
            job.setExecutable(command);
            job.setArguments("test_file");
            job.setWorkingDirectory("test_workdir_usage");

            String id = scheduler.submitBatchJob(job);

            // Torque seems slow (occasionally) to update the status, so we need a long timeout for a short job...
            JobStatus status = scheduler.waitUntilDone(id, 20 * 1000);

            assertTrue("Job is not done after timeout", status.isDone());

            if (status.hasException()) {
                throw status.getException();
            }

            assertTrue("Exit code not 0 but " + status.getExitCode(), status.getExitCode() == 0);
            assertTrue("Test output was not found in expected location",
                    fs.exists(fs.getWorkingDirectory().resolve(new Path(false, "test_workdir_usage", "test_file"))));

        } finally {
            try {
                fs.delete(testDir, true);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Test
    public void test_stdout_usage_linux() throws Exception {

        // We need a scheduler that actually has a filesystem underneath.
        assumeTrue("Scheduler does not use filesystem", description.usesFileSystem());

        FileSystem fs = scheduler.getFileSystem();

        // This test does not run on windows.
        assumeTrue("Test only suited for linux", scheduler.getAdaptorName().equals("local") && (LocalFileSystemUtils.isLinux()));

        String command = "/bin/echo";

        if (LocalFileSystemUtils.isOSX() && scheduler.getAdaptorName().equals("local")) {
            command = "/usr/bin/echo";
        }

        assertTrue("Working dir does not exist", fs.exists(fs.getWorkingDirectory()));

        Path testDir = fs.getWorkingDirectory().resolve(new Path(false, "test_workdir_usage"));

        assertFalse("Test dir already exists", fs.exists(testDir));

        try {
            fs.createDirectory(testDir);

            assertTrue("Failed to create test directory", fs.exists(testDir));

            JobDescription job = new JobDescription();
            job.setExecutable(command);
            job.setArguments("Hello World");
            job.setWorkingDirectory("test_workdir_usage");
            job.setStdout("test_out.txt");

            String id = scheduler.submitBatchJob(job);

            // Torque seems slow (occasionally) to update the status, so we need a long timeout for a short job...
            JobStatus status = scheduler.waitUntilDone(id, 20 * 1000);

            assertTrue("Job is not done after timeout", status.isDone());

            if (status.hasException()) {
                throw status.getException();
            }

            assertTrue("Exit code not 0 but " + status.getExitCode(), status.getExitCode() == 0);

            Path out = fs.getWorkingDirectory().resolve(new Path(false, "test_workdir_usage", "test_out.txt"));

            assertTrue("Test output was not found in expected location", fs.exists(out));

            BufferedReader r = new BufferedReader(new InputStreamReader(fs.readFromFile(out)));

            String str = r.readLine();

            assertEquals("Test output dit not match expected value", "Hello World", str);

            str = r.readLine();

            assertNull("Test output contained more data than expected: " + str, str);

            r.close();

        } finally {
            try {
                fs.delete(testDir, true);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Test
    public void test_stdin_stdout_redirect_linux() throws Exception {

        // We need a scheduler that actually has a filesystem underneath.
        assumeTrue("Scheduler does not use filesystem", description.usesFileSystem());

        FileSystem fs = scheduler.getFileSystem();

        // This test does not run on windows.
        assumeTrue("Test only suited for linux", scheduler.getAdaptorName().equals("local") && (LocalFileSystemUtils.isLinux()));

        String command = "/bin/cat";

        if (LocalFileSystemUtils.isOSX() && scheduler.getAdaptorName().equals("local")) {
            command = "/usr/bin/cat";
        }

        assertTrue("Working dir does not exist", fs.exists(fs.getWorkingDirectory()));

        Path testDir = fs.getWorkingDirectory().resolve(new Path(false, "test_workdir_usage"));

        assertFalse("Test dir already exists", fs.exists(testDir));

        try {
            fs.createDirectory(testDir);

            assertTrue("Failed to create test directory", fs.exists(testDir));

            Path in = fs.getWorkingDirectory().resolve(new Path(false, "test_workdir_usage", "test_in.txt"));

            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(fs.writeToFile(in)));

            w.write("Hello World");
            w.close();

            JobDescription job = new JobDescription();
            job.setExecutable(command);
            job.setWorkingDirectory("test_workdir_usage");
            job.setStdin("test_in.txt");
            job.setStdout("test_out.txt");

            String id = scheduler.submitBatchJob(job);

            // Torque seems slow (occasionally) to update the status, so we need a long timeout for a short job...
            JobStatus status = scheduler.waitUntilDone(id, 20 * 1000);

            assertTrue("Job is not done after timeout", status.isDone());

            if (status.hasException()) {
                throw status.getException();
            }

            assertTrue("Exit code not 0 but " + status.getExitCode(), status.getExitCode() == 0);

            Path out = fs.getWorkingDirectory().resolve(new Path(false, "test_workdir_usage", "test_out.txt"));

            assertTrue("Test output was not found in expected location", fs.exists(out));

            BufferedReader r = new BufferedReader(new InputStreamReader(fs.readFromFile(out)));

            String str = r.readLine();

            assertEquals("Test output dit not match expected value", "Hello World", str);

            str = r.readLine();

            assertNull("Test output contained more data than expected: " + str, str);

            r.close();

        } finally {
            try {
                fs.delete(testDir, true);
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
