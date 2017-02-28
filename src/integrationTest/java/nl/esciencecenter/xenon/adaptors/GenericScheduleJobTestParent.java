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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;

import nl.esciencecenter.xenon.JobException;
import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.XenonTestWatcher;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.OpenOption;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.jobs.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobCanceledException;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.Jobs;
import nl.esciencecenter.xenon.jobs.Scheduler;
import nl.esciencecenter.xenon.jobs.Streams;
import nl.esciencecenter.xenon.jobs.UnsupportedJobDescriptionException;
import nl.esciencecenter.xenon.util.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class GenericScheduleJobTestParent {
    private static final Logger logger = LoggerFactory.getLogger(GenericScheduleJobTestParent.class);

    private static String TEST_ROOT;

    protected static JobTestConfig config;

    protected Xenon xenon;
    protected Files files;
    protected Jobs jobs;
    protected Credentials credentials;
    protected Scheduler scheduler;
    protected Job job;

    protected Path testDir;

    @Rule
    public TestWatcher watcher = new XenonTestWatcher();
    
    public Path resolve(Path root, String path) throws XenonException { 
        return files.newPath(root.getFileSystem(), root.getRelativePath().resolve(path));
    }
    
    // MUST be invoked by a @BeforeClass method of the subclass! 
    public static void prepareClass(JobTestConfig testConfig) {
        config = testConfig;
        TEST_ROOT = "xenon_test_" + config.getAdaptorName() + "_" + System.currentTimeMillis();
    }

    // MUST be invoked by a @AfterClass method of the subclass! 
    public static void cleanupClass() throws Exception {
        logger.info("GenericJobAdaptorTest.cleanupClass() attempting to remove: " + TEST_ROOT);

        Xenon xenon = XenonFactory.newXenon(null);

        Files files = xenon.files();
        Credentials credentials = xenon.credentials();

        Path cwd = config.getWorkingDir(files, credentials);
        Path root = files.newPath(cwd.getFileSystem(), cwd.getRelativePath().resolve(TEST_ROOT));

        if (files.exists(root)) {
            Utils.recursiveDelete(files, root);
        }

        XenonFactory.endXenon(xenon);
    }

    @Before
    public void prepare() throws Exception {
        // This is not an adaptor option, so it will throw an exception!
        //Map<String, String> properties = new HashMap<>();
        //properties.put(SshAdaptor.POLLING_DELAY, "100");
        xenon = XenonFactory.newXenon(null);
        files = xenon.files();
        jobs = xenon.jobs();
        credentials = xenon.credentials();
        scheduler = config.getDefaultScheduler(jobs, credentials);
        job = null;
    }

    @After
    public void cleanup() throws XenonException {
        jobs.close(scheduler);
        // XenonFactory.endXenon(xenon);
        XenonFactory.endAll();
    }

    protected String getWorkingDir(String testName) {
        return TEST_ROOT + "/" + testName;
    }

    protected Path initJobDirectory(String workingDir) throws XenonException, Exception {
        Path cwd = config.getWorkingDir(files, credentials);
        Path root = resolve(cwd, workingDir);

        files.createDirectories(root);

        return root;
    }

    protected void checkJobDone(JobStatus status) throws JobException {
        assertNotNull(status);
        assertTrue("Job exceeded deadline!", status.isDone());

        if (status.hasException()) {
            throw new JobException("Job failed!", status.getException());
        }

        if (!status.getState().equals("unknown")) {
            assertNotNull("Job exit code is null; status: " + status, status.getExitCode());
        }
    }
    
    protected void checkJobOutput(Job job, Path root) throws XenonException, IOException {
        checkJobOutput(job, root, null, null);
    }
    protected void checkJobOutput(Job job, Path root, String expectedStdout) throws XenonException, IOException {
        checkJobOutput(job, root, expectedStdout, null);
    }
    
    protected void checkJobOutput(Job job, Path root, String expectedStdout, String expectedWindowsStdout) throws XenonException, IOException {
        if (job.getJobDescription().getStdout() != null) {
            String tmpout = readFile(root, job.getJobDescription().getStdout());

            logger.info("STDOUT: " + tmpout);
            assertNotNull(tmpout);

            if (expectedStdout != null) {
                if (config.targetIsWindows()) {
                    if (expectedWindowsStdout == null) {
                        assertFalse(tmpout.isEmpty());
                    } else {
                        assertTrue(tmpout.startsWith(expectedWindowsStdout));
                    }
                } else {
                    assertEquals(expectedStdout, tmpout);
                }
            }
        }
        if (job.getJobDescription().getStderr() != null) {
            String tmperr = readFile(root, job.getJobDescription().getStderr());
            logger.info("STDERR: " + tmperr);
            assertNotNull(tmperr);
            assertTrue(tmperr.isEmpty());
        }
    }
    
    protected String readFile(Path root, String filename) throws XenonException, IOException {
        Path filePath = resolve(root, filename);
        return readFully(filePath);
    }
    
    protected String readFully(Path p) throws XenonException, IOException {
        
        long end = System.currentTimeMillis() + 60*1000;
        
        while (!files.exists(p) && System.currentTimeMillis() < end) { 
            try { 
                Thread.sleep(1000);
            } catch (InterruptedException e) { 
                // ignore
            }
        }
        
        InputStream in = files.newInputStream(p);
                
        String result = Utils.readToString(in);
        
        try { 
            in.close();
        } catch (Exception e) {
            // ignored
        }
        
        return result;
    }

    protected void writeFully(Path p, String message) throws IOException, XenonException {
        OutputStream out = files.newOutputStream(p, OpenOption.CREATE, OpenOption.APPEND, OpenOption.WRITE);
        writeFully(out, message);
    }

    protected void writeFully(OutputStream out, String message) throws IOException {
        out.write(message.getBytes());
        out.close();
    }

    protected void cleanupJobRecursive(Path root) {
        XenonException cleanupFailed = null;
        try {
            Utils.recursiveDelete(files, root);
        } catch (XenonException ex) {
            cleanupFailed = ex;
        }
        try {
            files.close(root.getFileSystem());
        } catch (XenonException ex) {
            cleanupFailed = ex;
        }
        if (cleanupFailed != null) {
            throw new AssertionError(cleanupFailed);
        }
    }
    
    /**
     * Remove job root folder, stdout, stderr and other provided paths.
     * @param job job to cleanup files for (null if not applicable)
     * @param root job working directory to remove
     * @param otherPaths other paths to remove
     * @throws XenonException if resolving path or delete fails
     */
    protected void cleanupJob(Job job, Path root, Path... otherPaths) throws XenonException {
        XenonException cleanupFailed = null;
        Path[] allPaths = Arrays.copyOf(otherPaths, otherPaths.length + 2);
        if (job != null) {
            JobDescription description = job.getJobDescription();
            if (description.getStdout() != null)
                allPaths[otherPaths.length] = resolve(root, description.getStdout());
            if (description.getStderr() != null)
                allPaths[otherPaths.length + 1] = resolve(root, description.getStderr());
        }
        for (Path p : allPaths) {
            if (p != null) {
                try {
                    if (files.exists(p)) {
                        files.delete(p);
                    }
                } catch (XenonException ex) {
                    cleanupFailed = ex;
                    logger.warn("cleanupJob failed to delete file {}", p);
                }
            }
        }

        if (root != null) {
            try {
                files.delete(root);
            } catch (XenonException ex) {
                cleanupFailed = ex;
            }
            files.close(root.getFileSystem());
        }
        if (cleanupFailed != null) {
            throw new AssertionError(cleanupFailed);
        }
    }
    
    /** Run a job with in given directory, and compare the output with expected output.
     * @param workingDir directory to run in
     * @param description job description. Include stdout in the description if comparing with expectedOutput
     * @param expectedOutput output that stdout should match, exactly. Provide null to only check that stderr is empty and stdout exists, if provided.
     */
    protected void runJob(String workingDir, JobDescription description, String expectedOutput) throws Exception {
        Path root = initJobDirectory(workingDir);
        try {
            job = jobs.submitJob(scheduler, description);
            JobStatus status = jobs.waitUntilDone(job, config.getJobTimeout(0));
            checkJobDone(status);
            checkJobOutput(job, root, expectedOutput);
        } finally {
            cleanupJob(job, root);
        }
    }

    /**
     * Job description to set environment value and print it.
     * Does not set stderr and stdout files.
     *
     * @param workDir directory to run in
     * @param value value of the environement variable
     * @return generated job description
     */
    protected JobDescription printEnvJobDescription(String workDir, String value) {
        JobDescription description = new JobDescription();
        description.setExecutable("/usr/bin/printenv");
        description.setArguments("SOME_VARIABLE");
        description.addEnvironment("SOME_VARIABLE", value);
        description.setWorkingDirectory(workDir);
        return description;
    }

    /**
     * Job description to print a message.
     * Does not set stderr and stdout files. In Windows, this prints the hostname, not the message.
     *
     * @param workingDir directory to run in
     * @param message message to print, if not in Windows.
     * @return generated job description
     */
    protected JobDescription echoJobDescription(String workingDir, String message) {
        JobDescription description = new JobDescription();
        
        if (config.targetIsWindows()) { 
            description.setExecutable("hostname");
        } else { 
            description.setExecutable("/bin/echo");
            description.setArguments("-n", message);
        }

        description.setWorkingDirectory(workingDir);
        return description;
    }

    /**
     * Job description to echo a message.
     * Does not set stderr and stdout files. In Windows, this prints the hostname, not the message.
     *
     * @param workingDir directory to run in
     * @param message message to print, if not in Windows.
     * @return generated job description
     */
    protected JobDescription catJobDescription(String workingDir, String message) {
        JobDescription description = new JobDescription();
        
        if (config.targetIsWindows()) { 
            description.setExecutable("hostname");
        } else { 
            description.setExecutable("/bin/cat");
        }

        description.setWorkingDirectory(workingDir);
        return description;
    }

    
    /**
     * Job description that takes approximately a fixed time.
     * Does not set stderr and stdout files.
     *
     * @param workingDir directory to run in
     * @param seconds number of seconds the job should take.
     * @return generated job description
     */
    protected JobDescription timedJobDescription(String workingDir, int seconds) {
        JobDescription description = new JobDescription();
        
        if (config.targetIsWindows()) { 
            description.setExecutable("ping");
            description.setArguments("-n", Integer.toString(seconds + 1), "127.0.0.1");
        } else { 
            description.setExecutable("/bin/sleep");
            description.setArguments(Integer.toString(seconds));
        }

        description.setWorkingDirectory(workingDir);
        return description;
    }

    /**
     * Job description prints the contents of a file
     * Does not set stderr and stdout files.
     *
     * @param workingDir directory to run in
     * @param stdin path to stdin to write contents to
     * @param message message to store as contents
     * @return generated job description
     * @throws XenonException file cannot be created or written to
     * @throws IOException file stream cannot be written to
     */
    protected JobDescription catJobDescription(String workingDir, Path stdin, String message) throws XenonException, IOException {
        writeFully(stdin, message);

        JobDescription description = new JobDescription();
        if (config.targetIsWindows()) { 
            description.setExecutable("c:\\Windows\\System32\\more.com");
        } else { 
            description.setExecutable("/bin/cat");
        }
        description.setWorkingDirectory(workingDir);
        description.setStdin("stdin.txt");

        return description;
    }

    protected JobDescription nonExistingJobDescription(String workingDir) {
        JobDescription description = new JobDescription();
        description.setExecutable("non-existing-executable");
        description.setWorkingDirectory(workingDir);
        return description;
    }

    @Test
    public void test30_interactiveJobSubmit() throws Exception {
        if (!scheduler.supportsInteractive()) {
            return;
        }

        String message = "Hello World! test30";

        JobDescription description = catJobDescription(null, message);
        description.setInteractive(true);

        logger.info("Submitting interactive job to " + scheduler.getScheme() + "://" + scheduler.getLocation());

        job = jobs.submitJob(scheduler, description);

        logger.info("Interactive job submitted to " + scheduler.getScheme() + "://" + scheduler.getLocation());

        Streams streams = jobs.getStreams(job);
        
        PrintWriter w = new PrintWriter(streams.getStdin());
        w.print(message);
        w.flush();
        w.close();

        String out = Utils.readToString(streams.getStdout());
        String err = Utils.readToString(streams.getStderr());

        // Wait for 30 sec for the job to complete
        JobStatus status = jobs.waitUntilDone(job, config.getJobTimeout(30));

        checkJobDone(status);

        assertNotNull(out);
        assertNotNull(err);

        if (config.targetIsWindows()) { 
            assertTrue(out.length() > 0);
        } else { 
            assertEquals(message, out);
        }

        assertEquals(0, err.length());
    }

    @Test
    public void test31_batchJobSubmitWithPolling() throws Exception {
        String message = "Hello World! test31";
        String workingDir = getWorkingDir("test31");
        Path root = initJobDirectory(workingDir);

        try {
            JobDescription description = echoJobDescription(workingDir, message);
            description.setStdout("stdout.txt");
            description.setStderr("stderr.txt");

            job = jobs.submitJob(scheduler, description);

            long deadline = System.currentTimeMillis() + config.getJobTimeout(0);

            JobStatus status = jobs.getJobStatus(job);

            while (!status.isDone()) {
                Thread.sleep(config.getPollDelay());
                assertTrue("Job exceeded deadline!", System.currentTimeMillis() < deadline);
                status = jobs.getJobStatus(job);
            }

            checkJobDone(status);
            checkJobOutput(job, root, message);
        } finally {
            cleanupJob(job, root);
        }
    }

    @Test
    public void test32_batchJobSubmitWithWait() throws Exception {
        String message = "Hello World! test32";
        String workingDir = getWorkingDir("test32");
        Path root = initJobDirectory(workingDir);

        try {
            JobDescription description = echoJobDescription(workingDir, message);
            description.setStdout("stdout.txt");
            description.setStderr("stderr.txt");

            job = jobs.submitJob(scheduler, description);

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

    protected void submitToQueueWithPolling(String testName, String queueName, int jobCount) throws Exception {
        logger.info("STARTING TEST submitToQueueWithPolling(" + testName + ", " + queueName + ", " + jobCount);

        String workingDir = getWorkingDir(testName);
        Path root = initJobDirectory(workingDir);
        Job[] j = new Job[jobCount];

        try {
            for (int i = 0; i < j.length; i++) {
                JobDescription description = timedJobDescription(workingDir, 1);

                description.setQueueName(queueName);
                description.setStdout("stdout" + i + ".txt");
                description.setStderr("stderr" + i + ".txt");

                j[i] = jobs.submitJob(scheduler, description);
            }

            // Bit hard to determine realistic deadline here ?
            long deadline = System.currentTimeMillis() + jobCount * config.getJobTimeout(1);

            boolean done = false;

            while (!done) {
                JobStatus[] status = jobs.getJobStatuses(j);

                int count = 0;

                for (int i = 0; i < j.length; i++) {
                    if (j[i] != null) {
                        if (status[i].isDone()) {
                            if (status[i].hasException()) {
                                throw new JobException("Job " + i + " failed", status[i].getException());
                            }

                            logger.info("Job " + i + " done.");
                            j[i] = null;
                        } else {
                            count++;
                        }
                    }
                }

                if (count == 0) {
                    done = true;
                } else {
                    Thread.sleep(config.getPollDelay());
                    assertTrue("Job exceeded deadline!", System.currentTimeMillis() < deadline);
                }
            }
        } finally {
            cleanupJobRecursive(root);
        }
    }

    @Test
    public void test33a_testMultiBatchJobSubmitWithPolling() throws Exception {
        for (String queue : config.getQueueNames()) {
            submitToQueueWithPolling("test33a_" + queue, queue, 1);
        }

    }

    @Test
    public void test33b_testMultiBatchJobSubmitWithPolling() throws Exception {
        logger.info("STARTING TEST test33b");

        for (String queue : config.getQueueNames()) {
            submitToQueueWithPolling("test33b_" + queue, queue, 10);
        }
    }

    @Test
    public void test34_batchJobSubmitWithKill() throws Exception {
        String workingDir = getWorkingDir("test34");
        Path root = initJobDirectory(workingDir);

        Job[] tmpJobs = new Job[4];
        try {
            // Start uninteresting jobs, to make sure there is something on the queue.
            for (int i = 0; i < tmpJobs.length; i++) {
                tmpJobs[i] = jobs.submitJob(scheduler, timedJobDescription(null, 10));
            }

            JobDescription description = timedJobDescription(workingDir, 60);
            description.setStdout("stdout.txt");
            description.setStderr("stderr.txt");

            // We immediately kill the job. Hopefully it isn't running yet!
            job = jobs.submitJob(scheduler, description);
            JobStatus status = jobs.cancelJob(job);

            if (status.isRunning()) {
                // Wait until the job is killed.
                if (!status.isDone()) {
                    status = jobs.waitUntilDone(job, config.getUpdateTime());
                }
            }

            assertTrue("Failed to kill job! Expected status done, but job status is " + status, status.isDone());
            assertTrue("Job cancellation not registered: job status is " + status, status.hasException());
            Exception e = status.getException();
            assertTrue("Did not expect " + e + ": " + e.getMessage(), e instanceof JobCanceledException);
        } finally {
            for (Job tmpJob : tmpJobs) {
                if (tmpJob != null) jobs.cancelJob(tmpJob);
            }
            cleanupJob(job, root);
        }
    }

    @Test
    public void test35_batchJobSubmitWithKill2() throws Exception {
        String workingDir = getWorkingDir("test35");
        Path root = initJobDirectory(workingDir);

        try {
            JobDescription description = timedJobDescription(workingDir, 60);
            description.setStdout("stdout.txt");
            description.setStderr("stderr.txt");

            job = jobs.submitJob(scheduler, description);

            // Wait for job to run before killing it!
            JobStatus status = jobs.waitUntilRunning(job, config.getQueueWaitTime());

            assertTrue("Job failed to start! Expected status running, but job status is " + status, status.isRunning());

            status = jobs.cancelJob(job);

            // Wait until the job is killed. We assume it takes less than a minute!
            if (!status.isDone()) {
                status = jobs.waitUntilDone(job, config.getUpdateTime());
            }

            assertTrue("Failed to kill job! Expected status done, but job status is " + status, status.isDone());
            assertTrue("Expected status with exception, but job status is " + status, status.hasException());
            assertTrue(status.getException() instanceof JobCanceledException);
        } finally {
            cleanupJob(job, root);
        }
    }

    @Test
    public void test36a_batchJobSubmitWithInput() throws Exception {
        String message = "Hello World! test36a";
        String workingDir = getWorkingDir("test36a");
        Path root = initJobDirectory(workingDir);
        Path stdin = resolve(root, "stdin.txt");

        try {
            JobDescription description = catJobDescription(workingDir, stdin, message);
            description.setStdout("stdout.txt");
            description.setStderr("stderr.txt");

            job = jobs.submitJob(scheduler, description);
            JobStatus status = jobs.waitUntilDone(job, config.getJobTimeout(0));

            checkJobDone(status);
            checkJobOutput(job, root, message, message);
        } finally {
            cleanupJob(job, root, stdin);
        }
    }

    @Test
    public void test36b_batchJobSubmitWithInput() throws Exception {
        String message = "Hello World! test36b";
        String workingDir = getWorkingDir("test36b");
        Path root = initJobDirectory(workingDir);
        Path stdin = resolve(root, "stdin.txt");

        try {
            JobDescription description = catJobDescription(workingDir, stdin, message);
            description.setStdout("stdout.txt");
            description.setStderr("stderr.txt");

            job = jobs.submitJob(scheduler, description);

            JobStatus status = jobs.waitUntilRunning(job, config.getQueueWaitTime());

            if (status.isRunning()) {
                status = jobs.waitUntilDone(job, config.getUpdateTime());
            }

            checkJobDone(status);
            checkJobOutput(job, root, message, message);
        } finally {
            cleanupJob(job, root, stdin);
        }
    }

    @Test
    public void test37a_batchJobSubmitWithoutWorkDir() throws Exception {
        job = jobs.submitJob(scheduler, timedJobDescription(null, 1));
        JobStatus status = jobs.waitUntilDone(job, config.getJobTimeout(1));
        checkJobDone(status);
    }

    @Test
    public void test37b_batchJobSubmitWithRelativeWorkDir() throws Exception {
        String workingDir = "test37b";
        String message = "some message " + workingDir;
        JobDescription description = echoJobDescription(workingDir, message);
        description.setStdout("stdout.txt");
        runJob(workingDir, description, message);
    }

    @Test
    public void test37c_batchJobSubmitWithAbsoluteWorkDir() throws Exception {
        String workingDir = getWorkingDir("test37c");
        String message = "some message " + workingDir;
        JobDescription description = echoJobDescription(workingDir, message);
        description.setStdout("stdout.txt");
        runJob(workingDir, description, message);
    }

    @Test
    public void test37d_batchJobSubmitWithIncorrectWorkingDir() throws Exception {
        //note that we are _not_ creating this directory, making it invalid
        String workingDir = getWorkingDir("test37d");

        //submitting this job will either:
        // 1) throw an InvalidJobDescription when we submit the job
        // 2) produce an error when the job is run.
        try {
            job = jobs.submitJob(scheduler, timedJobDescription(workingDir, 1));
            // wait extra second for timed job
            JobStatus status = jobs.waitUntilDone(job, config.getJobTimeout(1));

            assertTrue("Job exceeded deadline! Expected status done, got " + status, status.isDone());

            //option (2)
            assertTrue(status.hasException());
        } catch (InvalidJobDescriptionException e) {
            //Submit failed, as expected (1)
        }
    }

    @Test
    public void test37e_batchJobSubmitWithWorkDirWithSpaces() throws Exception {
        //note the space in the path
        String workingDir = getWorkingDir("test 37b");
        String message = "some message " + workingDir;
        JobDescription description = echoJobDescription(workingDir, message);
        description.setStdout("stdout.txt");
        runJob(workingDir, description, message);
    }

    //@Test
    public void test38_multipleBatchJobSubmitWithInput() throws Exception {
        String message = "Hello World! test38";
        String workingDir = getWorkingDir("test38");
        Path root = initJobDirectory(workingDir);
        Path stdin = resolve(root, "stdin.txt");

        try {
            JobDescription description = catJobDescription(workingDir, stdin, message);
            description.setProcessesPerNode(2);

            job = jobs.submitJob(scheduler, description);
            JobStatus status = jobs.waitUntilDone(job, config.getJobTimeout(0));

            checkJobDone(status);
            for (int i = 0; i < 2; i++) {
                String outString = readFile(root, "stdout.txt." + i);
                String errString = readFile(root, "stderr.txt." + i);
                
                assertNotNull(outString);
                // Line ending may differ
                assertTrue(outString.startsWith(message));

                assertNotNull(errString);
                assertEquals(0, errString.length());
            }
        } finally {
            cleanupJobRecursive(root);
        }
    }

    @Test
    public void test39_multipleBatchJobSubmitWithExceptions() throws Exception {
        // NOTE: This test assumes that an exception is thrown when the status of a job is requested twice after the job is done!
        //       This may not be true for all schedulers.

        if (config.supportsStatusAfterDone()) {
            return;
        }

        Job[] j = new Job[] {
            jobs.submitJob(scheduler, timedJobDescription(null, 1)),
            jobs.submitJob(scheduler, timedJobDescription(null, 2)),
        };

        long deadline = System.currentTimeMillis() + config.getJobTimeout(1) + config.getJobTimeout(2);

        JobStatus[] s = null;
        while (System.currentTimeMillis() < deadline) {
            s = jobs.getJobStatuses(j);
            if (s[0].hasException() && s[1].hasException()) {
                break;
            }

            Thread.sleep(config.getPollDelay());
        }

        assertNotNull("Job status could not be retrieved", s);
        assertTrue("Job exceeded deadline!", s[0].hasException() && s[1].hasException());
    }

    @Test
    public void test40_batchJobSubmitWithExitcode() throws Exception {
        job = jobs.submitJob(scheduler, timedJobDescription(null, 1));
        JobStatus status = jobs.waitUntilDone(job, config.getJobTimeout(1));

        checkJobDone(status);

        if (!status.getState().equals("unknown")) {
            assertEquals(0, status.getExitCode().longValue());
        }
    }

    @Test
    public void test40_batchJobSubmitWithNonZeroExitcode() throws Exception {
        //run an ls with a non existing file. This should make ls return exitcode 2
        JobDescription description = new JobDescription();
        
        if (config.targetIsWindows()) { 
            // Will always exit! 
            description.setExecutable("timeout");
            description.setArguments("1");
        } else { 
            description.setExecutable("/bin/cat");
            description.setArguments("non.existing.file");
        }

        job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, config.getJobTimeout(1));

        checkJobDone(status);

        if (!status.getState().equals("unknown")) {
            assertNotEquals(0, status.getExitCode().longValue());
        }
    }

    @Test
    public void test41_batchJobSubmitWithEnvironmentVariable() throws Exception {
        if (!config.supportsEnvironmentVariables() || config.targetIsWindows()) { 
            return;
        }

        String envValue = "some_value";
        String workingDir = getWorkingDir("test41");
        Path root = initJobDirectory(workingDir);

        try {
            //echo the given variable, to see if the va
            JobDescription description = printEnvJobDescription(workingDir, envValue);
            description.setStdout("stdout.txt");

            job = jobs.submitJob(scheduler, description);
            JobStatus status = jobs.waitUntilDone(job, config.getJobTimeout(0));

            checkJobDone(status);
            checkJobOutput(job, root, envValue + "\n");
        } finally {
            cleanupJob(job, root);
        }
    }

    @Test
    public void test41b_batchJobSubmitWithEnvironmentVariable() throws Exception {
        if (config.supportsEnvironmentVariables() || config.targetIsWindows()) { 
            return;
        }

        try {
            job = jobs.submitJob(scheduler, printEnvJobDescription(null, "some_value"));
            jobs.waitUntilDone(job, config.getUpdateTime());
            fail("Job description not supposed to be supported.");
        } catch (UnsupportedJobDescriptionException e) {
            // do nothing
        }
    }

    @Test
    public void test42a_batchJob_parallel_Exception() throws Exception {
        if (config.supportsParallelJobs()) {
            return;
        }

        JobDescription description = echoJobDescription(null, "some message");
        description.setNodeCount(2);
        description.setProcessesPerNode(2);

        try {
            jobs.submitJob(scheduler, description);
            fail("Submit did not throw exception, which was expected!");
        } catch (InvalidJobDescriptionException e) {
            // do nothing
        }
    }

    @Test
    public void test43_submit_JobDescriptionShouldBeCopied_Success() throws Exception {
        String workingDir = getWorkingDir("test43");
        Path root = initJobDirectory(workingDir);

        try {
            JobDescription description = nonExistingJobDescription(workingDir);
            description.setStdout("stdout.txt");

            job = jobs.submitJob(scheduler, description);

            description.setStdout("aap.txt");

            assertNotEquals("Job description should have been copied!", job.getJobDescription().getStdout(), description.getStdout());

            JobStatus status = jobs.cancelJob(job);

            if (!status.isDone()) {
                jobs.waitUntilDone(job, config.getUpdateTime());
            }
        } finally {
            cleanupJobRecursive(root);
        }
    }

    @Test
    public void test44_submit_JobDescriptionShouldBeSame() throws Exception {
        String workingDir = getWorkingDir("test44");
        Path root = initJobDirectory(workingDir);
        Path stdin = resolve(root, "stdin.txt");

        try {
            JobDescription description = catJobDescription(workingDir, stdin, "my message");
            description.setStdout("stdout.txt");
            description.setStderr("stderr.txt");

            job = jobs.submitJob(scheduler, description);

            JobStatus status = jobs.cancelJob(job);

            if (!status.isDone()) {
                jobs.waitUntilDone(job, config.getUpdateTime());
            }

            JobDescription submitted = job.getJobDescription();
            assertEquals("stdout.txt", submitted.getStdout());
            assertEquals("stderr.txt", submitted.getStderr());
            assertEquals("stdin.txt", submitted.getStdin());
            assertEquals(workingDir, submitted.getWorkingDirectory());
        } finally {
            cleanupJob(job, root, stdin);
        }
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void test45_batchJobSubmitWithIllegalWaitUntilRunning() throws Exception {
        String workingDir = getWorkingDir("test45");
        Path root = initJobDirectory(workingDir);

        try {
            JobDescription description = timedJobDescription(workingDir, 60000);
            description.setStdout("stdout.txt");
            description.setStderr("stderr.txt");

            job = jobs.submitJob(scheduler, description);

            // Should throw exception
            jobs.waitUntilRunning(job, -1);
        } finally {
            jobs.cancelJob(job);
            jobs.waitUntilDone(job, 0);
            cleanupJob(job, root);
        }
    }

    @Test
    public void test46a_batchJobSubmitWithPollingWaitUntilDone() throws Exception {
        String workingDir = getWorkingDir("test46");
        Path root = initJobDirectory(workingDir);

        try {
            JobDescription description = timedJobDescription(workingDir, 10000);
            
            long start = System.currentTimeMillis();
            
            job = jobs.submitJob(scheduler, description);

            JobStatus status = jobs.waitUntilDone(job, 1000);
            
            while (status.isRunning()) {
                status = jobs.waitUntilDone(job, 1000);
            }
            
            long end = System.currentTimeMillis(); 
            
            checkJobDone(status);
            
            // We expect the job to have lasted at least 10000 milliseconds, which would require 9 or more times polling.
            assertTrue((end-start) >= 10000);
            
        } finally {
            cleanupJob(job, root);
        }
    }

    @Test
    public void test46b_batchJobSubmitWithPollingWaitUntilDone() throws Exception {
        String workingDir = getWorkingDir("test46");
        Path root = initJobDirectory(workingDir);

        try {
            JobDescription description = timedJobDescription(workingDir, 10000);
            
            job = jobs.submitJob(scheduler, description);

            JobStatus status = jobs.waitUntilDone(job, 1000);
            int count = 1;
            
            while (status.isRunning()) {
                status = jobs.waitUntilDone(job, 1000);
                count++;
            }
            
            checkJobDone(status);
            
            // We expect the job to have lasted at least 10000 milliseconds, which would require 9 or more times polling.
            assertTrue(count >= 9);
            
        } finally {
            cleanupJob(job, root);
        }
    }

    
    @Test
    public void test46c_batchJobSubmitWithPollingWaitUntilDone() throws Exception {
        String workingDir = getWorkingDir("test46");
        Path root = initJobDirectory(workingDir);

        try {
            JobDescription description = timedJobDescription(workingDir, 10000);
            
            job = jobs.submitJob(scheduler, description);

            JobStatus status = jobs.waitUntilDone(job, 1000);
            
            while (status.isRunning()) {
                long now = System.currentTimeMillis();
                
                status = jobs.waitUntilDone(job, 1000);
                
                long diff = System.currentTimeMillis() - now;
              
                // The wait should have lasted at least 1000 millis
                if (status.isRunning()) { 
                    assertTrue(diff >= 1000);
                }
            }
            
            checkJobDone(status);
        } finally {
            cleanupJob(job, root);
        }
    }

    
    @Test
    public void test47_batchJobSubmitWithSingleWaitUntilDone() throws Exception {
        String workingDir = getWorkingDir("test46");
        Path root = initJobDirectory(workingDir);

        try {
            JobDescription description = timedJobDescription(workingDir, 10000);
            
            long start = System.currentTimeMillis();
            
            job = jobs.submitJob(scheduler, description);

            JobStatus status = jobs.waitUntilDone(job, 0);
            // Should wait until the job is finished, however long it takes.
            
            long end = System.currentTimeMillis(); 
            
            // Job must be in done state
            checkJobDone(status);
            
            // We expect the job to have lasted at least 10000 milliseconds, which would require 9 or more times polling.
            assertTrue((end-start) >= 10000);
            
        } finally {
            cleanupJob(job, root);
        }
    }
    
    @Test
    public void test48_batchJobSubmitWithSingleWaitUntilRunning() throws Exception {
        String workingDir = getWorkingDir("test46");
        Path root = initJobDirectory(workingDir);

        try {
            JobDescription description = timedJobDescription(workingDir, 5000);
            
            job = jobs.submitJob(scheduler, description);

            JobStatus status = jobs.waitUntilRunning(job, 0);
            // Should wait until the job is finished, however long it takes.
            
            assert(status.isRunning());
            
            status = jobs.waitUntilDone(job, 0);
            
            // Job must be in done state
            checkJobDone(status);
            
        } finally {
            cleanupJob(job, root);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void test49_batchJobSubmitWithIllegalWaitUntilDone() throws Exception {
        String workingDir = getWorkingDir("test46");
        Path root = initJobDirectory(workingDir);

        try {
            JobDescription description = timedJobDescription(workingDir, 1000);
            
            job = jobs.submitJob(scheduler, description);

            // Should throw exception!
            jobs.waitUntilDone(job, -1);
            
        } finally {
            jobs.cancelJob(job);
            jobs.waitUntilDone(job, 0);
            cleanupJob(job, root);
        }
    }

    
    
}
