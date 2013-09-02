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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.exceptions.InvalidCredentialsException;
import nl.esciencecenter.octopus.exceptions.InvalidJobDescriptionException;
import nl.esciencecenter.octopus.exceptions.InvalidLocationException;
import nl.esciencecenter.octopus.exceptions.InvalidPropertyException;
import nl.esciencecenter.octopus.exceptions.JobCanceledException;
import nl.esciencecenter.octopus.exceptions.NoSuchQueueException;
import nl.esciencecenter.octopus.exceptions.NoSuchSchedulerException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.UnknownPropertyException;
import nl.esciencecenter.octopus.exceptions.UnsupportedJobDescriptionException;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.Pathname;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.jobs.Streams;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class GenericJobAdaptorTestParent {

    private static final Logger logger = LoggerFactory.getLogger(GenericJobAdaptorTestParent.class);

    private static String TEST_ROOT;

    protected static JobTestConfig config;

    protected Octopus octopus;
    protected Files files;
    protected Jobs jobs;
    protected Credentials credentials;

    protected Path testDir;

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
    
    public Path resolve(Path root, String path) throws OctopusIOException { 
        return files.newPath(root.getFileSystem(), root.getPathname().resolve(path));
    }

    public Path resolve(FileSystem fs, String path) throws OctopusIOException {
        return resolve(fs.getEntryPath(), path);
    }

    
    // MUST be invoked by a @BeforeClass method of the subclass! 
    public static void prepareClass(JobTestConfig testConfig) {
        config = testConfig;
        TEST_ROOT = "octopus_test_" + config.getAdaptorName() + "_" + System.currentTimeMillis();
    }

    // MUST be invoked by a @AfterClass method of the subclass! 
    public static void cleanupClass() throws Exception {

        System.err.println("GenericJobAdaptorTest.cleanupClass() attempting to remove: " + TEST_ROOT);

        Octopus octopus = OctopusFactory.newOctopus(null);

        Files files = octopus.files();
        Credentials credentials = octopus.credentials();

        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);
        Pathname entryPath = filesystem.getEntryPath().getPathname();
        Path root = files.newPath(filesystem, entryPath.resolve(TEST_ROOT));

        if (files.exists(root)) {
            files.delete(root);
        }

        OctopusFactory.endOctopus(octopus);
    }

    @Before
    public void prepare() throws OctopusException {
        // This is not an adaptor option, so it will throw an exception!
        //Map<String, String> properties = new HashMap<>();
        //properties.put(SshAdaptor.POLLING_DELAY, "100");
        octopus = OctopusFactory.newOctopus(null);
        files = octopus.files();
        jobs = octopus.jobs();
        credentials = octopus.credentials();
    }

    @After
    public void cleanup() throws OctopusException {
        // OctopusFactory.endOctopus(octopus);
        OctopusFactory.endAll();
    }

    protected String getWorkingDir(String testName) {
        return TEST_ROOT + "/" + testName;
    }

    // TEST: newScheduler
    //
    // location: null / valid URI / invalid URI 
    // credential: null / default / set / wrong
    // properties: null / empty / set / wrong

    @Test(expected = OctopusException.class)
    public void test00_newScheduler() throws Exception {
        jobs.newScheduler(null, null, null, null);
    }

    @Test(expected = InvalidLocationException.class)
    public void test01a_newScheduler() throws Exception {
        if (!config.supportsNullLocation()) { 
            Scheduler s = jobs.newScheduler(config.getScheme(), null, null, null);
            jobs.close(s);
        }
    }

    @Test
    public void test01b_newScheduler() throws Exception {
        if (config.supportsNullLocation()) { 
            Scheduler s = jobs.newScheduler(config.getScheme(), null, null, null);
            jobs.close(s);
        }
    }
    
    @Test
    public void test02a_newScheduler() throws Exception {
        Scheduler s = jobs.newScheduler(config.getScheme(), config.getCorrectLocation(), null, null);
        jobs.close(s);
    }
    
    @Test(expected = OctopusException.class)
    public void test02b_newScheduler() throws Exception {
        jobs.newScheduler(config.getScheme(), config.getWrongLocation(), null, null);
    }

    @Test
    public void test03_newScheduler() throws Exception {
        Scheduler s = jobs.newScheduler(config.getScheme(), config.getCorrectLocation(), config.getDefaultCredential(credentials),
                null);
        jobs.close(s);
    }

    @Test
    public void test04a_newScheduler() throws Exception {
        if (config.supportsCredentials()) {
            try {
                Scheduler s = jobs.newScheduler(config.getScheme(), config.getCorrectLocation(), 
                        config.getInvalidCredential(credentials), null);
                
                jobs.close(s);
                throw new Exception("newScheduler did NOT throw InvalidCredentialsException");
            } catch (InvalidCredentialsException e) {
                // expected
            } catch (OctopusException e) {
                // allowed
            }
        }
    }

    @Test
    public void test04b_newScheduler() throws Exception {
        if (!config.supportsCredentials()) {
            try {
                Credential c = new Credential() {
                    @Override
                    public Map<String, String> getProperties() {
                        return null;
                    }

                    @Override
                    public String getAdaptorName() {
                        return "local";
                    }
                };

                Scheduler s = jobs.newScheduler(config.getScheme(), config.getCorrectLocation(), c, null);
                jobs.close(s);

                throw new Exception("newScheduler did NOT throw OctopusException");
            } catch (OctopusException e) {
                // expected
            }
        }
    }

    @Test
    public void test04c_newScheduler() throws Exception {
        if (config.supportsCredentials()) {
            Scheduler s = jobs.newScheduler(config.getScheme(), config.getCorrectLocation(), 
                    config.getPasswordCredential(credentials), config.getDefaultProperties());
            jobs.close(s);
        }
    }

    @Test
    public void test05_newScheduler() throws Exception {
        Scheduler s = jobs.newScheduler(config.getScheme(), config.getCorrectLocation(), 
                config.getDefaultCredential(credentials), new HashMap<String, String>());
        jobs.close(s);
    }

    @Test
    public void test06_newScheduler() throws Exception {
        Scheduler s = jobs.newScheduler(config.getScheme(), config.getCorrectLocation(), 
                config.getDefaultCredential(credentials), config.getDefaultProperties());
        jobs.close(s);
    }

    @Test
    public void test07_newScheduler() throws Exception {
        if (config.supportsProperties()) {

            Map<String, String>[] tmp = config.getInvalidProperties();

            for (Map<String, String> p : tmp) {
                try {
                    Scheduler s = jobs.newScheduler(config.getScheme(), config.getCorrectLocation(), 
                            config.getDefaultCredential(credentials), p);
                    jobs.close(s);
                    throw new Exception("newScheduler did NOT throw InvalidPropertyException");
                } catch (InvalidPropertyException e) {
                    // expected
                }
            }
        }
    }

    @Test
    public void test08_newScheduler() throws Exception {
        if (config.supportsProperties()) {
            try {
                Scheduler s = jobs.newScheduler(config.getScheme(), config.getCorrectLocation(), 
                        config.getDefaultCredential(credentials), config.getUnknownProperties());
                jobs.close(s);

                throw new Exception("newScheduler did NOT throw UnknownPropertyException");
            } catch (UnknownPropertyException e) {
                // expected
            }
        }
    }

    @Test
    public void test09_newScheduler() throws Exception {
        if (!config.supportsProperties()) {
            try {
                Map<String, String> p = new HashMap<>();
                p.put("aap", "noot");
                Scheduler s = jobs.newScheduler(config.getScheme(), config.getCorrectLocation(), 
                        config.getDefaultCredential(credentials), p);
                jobs.close(s);

                throw new Exception("newScheduler did NOT throw OctopusException");
            } catch (OctopusException e) {
                // expected
            }
        }
    }

    @Test
    public void test10_getLocalScheduler() throws Exception {

        Scheduler s = null;

        try {
            s = jobs.getLocalScheduler();
            assertTrue(s != null);
            assertTrue(s.getAdaptorName().equals("local"));
        } finally {
            if (s != null) {
                jobs.close(s);
            }
        }
    }

    @Test
    public void test11_open_close() throws Exception {
        if (config.supportsClose()) {
            Scheduler s = config.getDefaultScheduler(jobs, credentials);

            assertTrue(jobs.isOpen(s));

            jobs.close(s);

            assertFalse(jobs.isOpen(s));
        }
    }

    @Test
    public void test12_open_close() throws Exception {
        if (!config.supportsClose()) {
            Scheduler s = config.getDefaultScheduler(jobs, credentials);

            assertTrue(jobs.isOpen(s));

            jobs.close(s);

            assertTrue(jobs.isOpen(s));
        }
    }

    @Test
    public void test13_open_close() throws Exception {
        if (config.supportsClose()) {
            Scheduler s = config.getDefaultScheduler(jobs, credentials);
            jobs.close(s);

            try {
                jobs.close(s);
                throw new Exception("close did NOT throw NoSuchSchedulerException");
            } catch (NoSuchSchedulerException e) {
                // expected
            }
        }
    }

    @Test
    public void test14a_getJobs() throws Exception {
        Scheduler s = config.getDefaultScheduler(jobs, credentials);
        jobs.getJobs(s, s.getQueueNames());
        jobs.close(s);
    }

    @Test
    public void test14b_getJobs() throws Exception {
        Scheduler s = config.getDefaultScheduler(jobs, credentials);
        jobs.getJobs(s);
        jobs.close(s);
    }

    @Test
    public void test15_getJobs() throws Exception {
        Scheduler s = config.getDefaultScheduler(jobs, credentials);

        try {
            jobs.getJobs(s, config.getInvalidQueueName());
            throw new Exception("getJobs did NOT throw NoSuchQueueException");
        } catch (NoSuchQueueException e) {
            // expected
        } finally {
            jobs.close(s);
        }
    }

    @Test
    public void test16_getJobs() throws Exception {

        if (config.supportsClose()) {

            Scheduler s = config.getDefaultScheduler(jobs, credentials);

            jobs.close(s);

            try {
                jobs.getJobs(s, s.getQueueNames());
                throw new Exception("close did NOT throw NoSuchSchedulerException");
            } catch (NoSuchSchedulerException e) {
                // expected
            }
        }
    }

    @Test
    public void test17_getQueueStatus() throws Exception {
        Scheduler s = config.getDefaultScheduler(jobs, credentials);
        jobs.getQueueStatus(s, s.getQueueNames()[0]);
        jobs.close(s);
    }

    @Test(expected = NoSuchQueueException.class)
    public void test18a_getQueueStatus() throws Exception {
        Scheduler s = config.getDefaultScheduler(jobs, credentials);
        try {
            jobs.getQueueStatus(s, config.getInvalidQueueName());
        } finally {
            jobs.close(s);
        }
    }

    @Test
    public void test18b_getQueueStatus() throws Exception {
        Scheduler s = config.getDefaultScheduler(jobs, credentials);
        String queueName = config.getDefaultQueueName();

        try {
            jobs.getQueueStatus(s, queueName);
        } finally {
            jobs.close(s);
        }
    }

    @Test(expected = NullPointerException.class)
    public void test19_getQueueStatus() throws Exception {
        jobs.getQueueStatus(null, null);
    }

    @Test
    public void test20_getQueueStatus() throws Exception {

        if (config.supportsClose()) {

            Scheduler s = config.getDefaultScheduler(jobs, credentials);
            jobs.close(s);

            try {
                jobs.getQueueStatus(s, s.getQueueNames()[0]);
                throw new Exception("getQueueStatus did NOT throw NoSuchSchedulerException");
            } catch (NoSuchSchedulerException e) {
                // expected
            }
        }
    }

    @Test
    public void test21a_getQueueStatuses() throws Exception {
        Scheduler s = config.getDefaultScheduler(jobs, credentials);
        QueueStatus[] tmp = jobs.getQueueStatuses(s, s.getQueueNames());
        jobs.close(s);

        String[] names = s.getQueueNames();

        assertTrue(tmp != null);
        assertTrue(tmp.length == names.length);

        for (int i = 0; i < tmp.length; i++) {
            assertTrue(tmp[i].getQueueName().equals(names[i]));
        }
    }

    @Test
    public void test21b_getQueueStatuses() throws Exception {
        Scheduler s = config.getDefaultScheduler(jobs, credentials);
        QueueStatus[] tmp = jobs.getQueueStatuses(s);
        jobs.close(s);

        String[] names = s.getQueueNames();

        assertTrue(tmp != null);
        assertTrue(tmp.length == names.length);

        for (int i = 0; i < tmp.length; i++) {
            assertTrue(tmp[i].getQueueName().equals(names[i]));
        }

    }

    @Test
    public void test22a_getQueueStatuses() throws Exception {
        Scheduler s = config.getDefaultScheduler(jobs, credentials);
        try {
            QueueStatus[] tmp = jobs.getQueueStatuses(s, config.getInvalidQueueName());

            assertTrue(tmp != null);
            assertTrue(tmp.length == 1);
            assertTrue(tmp[0].hasException());
            assertTrue(tmp[0].getException() instanceof NoSuchQueueException);

        } finally {
            jobs.close(s);
        }
    }

    @Test(expected = NullPointerException.class)
    public void test22b_getQueueStatuses() throws Exception {
        jobs.getQueueStatuses(null, config.getDefaultQueueName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test22c_getQueueStatuses() throws Exception {
        Scheduler s = config.getDefaultScheduler(jobs, credentials);
        jobs.getQueueStatuses(s, (String[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void test23_getQueueStatuses() throws Exception {
        jobs.getQueueStatuses(null);
    }

    @Test
    public void test24_getQueueStatuses() throws Exception {

        if (config.supportsClose()) {
            Scheduler s = config.getDefaultScheduler(jobs, credentials);
            jobs.close(s);

            try {
                jobs.getQueueStatuses(s, s.getQueueNames());
                throw new Exception("getQueueStatuses did NOT throw NoSuchSchedulerException");
            } catch (NoSuchSchedulerException e) {
                // expected
            }
        }
    }

    @Test
    public void test25a_getJobStatuses() throws Exception {

        JobStatus[] tmp = jobs.getJobStatuses(new Job[0]);

        assertTrue(tmp != null);
        assertTrue(tmp.length == 0);
    }

    @Test
    public void test25b_getJobStatuses() throws Exception {

        JobStatus[] tmp = jobs.getJobStatuses((Job[]) null);

        assertTrue(tmp != null);
        assertTrue(tmp.length == 0);
    }

    @Test
    public void test25c_getJobStatuses() throws Exception {

        JobStatus[] tmp = jobs.getJobStatuses(new Job[1]);

        assertTrue(tmp != null);
        assertTrue(tmp.length == 1);
        assertTrue(tmp[0] == null);
    }

    protected String readFully(InputStream in) throws IOException {

        byte[] buffer = new byte[1024];

        int offset = 0;

        int tmp = in.read(buffer, 0, buffer.length - offset);

        while (tmp != -1) {

            offset += tmp;

            if (offset == buffer.length) {
                buffer = Arrays.copyOf(buffer, buffer.length * 2);
            }

            tmp = in.read(buffer, offset, buffer.length - offset);
        }

        in.close();
        return new String(buffer, 0, offset);
    }

    protected void writeFully(OutputStream out, String message) throws IOException {
        out.write(message.getBytes());
        out.close();
    }

    @org.junit.Test
    public void test30_interactiveJobSubmit() throws Exception {

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);

        if (scheduler.isOnline()) {

            String message = "Hello World! test30";

            JobDescription description = new JobDescription();
            description.setExecutable("/bin/echo");
            description.setArguments("-n", message);
            description.setInteractive(true);

            System.err.println("Submitting interactive job to " + scheduler.getScheme() + "://" + scheduler.getLocation());

            Job job = jobs.submitJob(scheduler, description);

            System.err.println("Interactive job submitted to " + scheduler.getScheme() + "://" + scheduler.getLocation());

            Streams streams = jobs.getStreams(job);
            streams.getStdin().close();

            String out = readFully(streams.getStdout());
            String err = readFully(streams.getStderr());

            // NOTE: Job should already be done here!
            JobStatus status = jobs.waitUntilDone(job, 5000);

            if (!status.isDone()) {
                throw new Exception("Job exceeded dealine!");
            }

            if (status.hasException()) {
                throw new Exception("Job failed!", status.getException());
            }

            assertTrue(out.equals(message));
            assertTrue(err.length() == 0);
        }

        jobs.close(scheduler);
    }

    @org.junit.Test
    public void test31_batchJobSubmitWithPolling() throws Exception {

        String message = "Hello World! test31";
        String workingDir = getWorkingDir("test31");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);

        Path root = resolve(filesystem, workingDir);
        files.createDirectories(root);

        JobDescription description = new JobDescription();
        description.setExecutable("/bin/echo");
        description.setArguments("-n", message);
        description.setInteractive(false);
        description.setWorkingDirectory(workingDir);
        description.setStdin(null);
        description.setStdout("stdout.txt");
        description.setStderr("stderr.txt");

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

        Path out = resolve(root, "stdout.txt");
        Path err = resolve(root, "stderr.txt");

        String tmpout = readFully(files.newInputStream(out));
        String tmperr = readFully(files.newInputStream(err));

        files.delete(out);
        files.delete(err);
        files.delete(root);

        files.close(filesystem);

        System.err.println("STDOUT: " + tmpout);
        System.err.println("STDERR: " + tmperr);

        assertTrue(tmpout != null);
        assertTrue(tmpout.length() > 0);
        assertTrue(tmpout.equals(message));
        assertTrue(tmperr.length() == 0);
    }

    @org.junit.Test
    public void test32_batchJobSubmitWithWait() throws Exception {

        String message = "Hello World! test32";
        String workingDir = getWorkingDir("test32");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);

        Path root = resolve(filesystem, workingDir);
        
        files.createDirectories(root);

        JobDescription description = new JobDescription();
        description.setExecutable("/bin/echo");
        description.setArguments("-n", message);
        description.setInteractive(false);
        description.setWorkingDirectory(workingDir);
        description.setStdin(null);
        description.setStdout("stdout.txt");
        description.setStderr("stderr.txt");

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

        Path out = resolve(root, "stdout.txt");
        Path err = resolve(root, "stderr.txt");

        String tmpout = readFully(files.newInputStream(out));
        String tmperr = readFully(files.newInputStream(err));

        System.err.println("STDOUT: " + tmpout);
        System.err.println("STDERR: " + tmperr);

        assertTrue(tmpout != null);
        assertTrue(tmpout.length() > 0);
        assertTrue(tmpout.equals(message));
        assertTrue(tmperr.length() == 0);
        
        files.delete(out);
        files.delete(err);
        files.delete(root);

        files.close(filesystem);
    }

    private void submitToQueueWithPolling(String testName, String queueName, int jobCount) throws Exception {

        System.err.println("STARTING TEST submitToQueueWithPolling(" + testName + ", " + queueName + ", " + jobCount);

        String workingDir = getWorkingDir(testName);

        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);
        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);

        Path root = resolve(filesystem, workingDir);
        files.createDirectories(root);

        Path[] out = new Path[jobCount];
        Path[] err = new Path[jobCount];

        Jobs jobs = octopus.jobs();

        Job[] j = new Job[jobCount];

        for (int i = 0; i < j.length; i++) {

            out[i] = resolve(root, "stdout" + i + ".txt");
            err[i] = resolve(root, "stderr" + i + ".txt");

            JobDescription description = new JobDescription();
            description.setExecutable("/bin/sleep");
            description.setArguments("1");
            description.setWorkingDirectory(workingDir);

            description.setQueueName(queueName);
            description.setInteractive(false);
            description.setStdin(null);
            description.setStdout("stdout" + i + ".txt");
            description.setStderr("stderr" + i + ".txt");

            j[i] = jobs.submitJob(scheduler, description);
        }

        // Bit hard to determine realistic deadline here ?
        long deadline = System.currentTimeMillis() + config.getQueueWaitTime() + (jobCount * config.getUpdateTime());

        boolean done = false;

        while (!done) {
            JobStatus[] status = jobs.getJobStatuses(j);

            int count = 0;

            for (int i = 0; i < j.length; i++) {
                if (j[i] != null) {
                    if (status[i].isDone()) {
                        if (status[i].hasException()) {
                            System.err.println("Job " + i + " failed!");
                            throw new Exception("Job " + i + " failed", status[i].getException());
                        }

                        System.err.println("Job " + i + " done.");
                        j[i] = null;
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

        for (int i = 0; i < j.length; i++) {

            String tmpout = readFully(files.newInputStream(out[i]));
            String tmperr = readFully(files.newInputStream(err[i]));

            assertTrue(tmpout != null);
            assertTrue(tmpout.length() == 0);

            assertTrue(tmperr != null);
            assertTrue(tmperr.length() == 0);

            files.delete(out[i]);
            files.delete(err[i]);
        }

        jobs.close(scheduler);
        files.delete(root);
        files.close(filesystem);
    }

    @org.junit.Test
    public void test33a_testMultiBatchJobSubmitWithPolling() throws Exception {
        for (String queue : config.getQueueNames()) {
            submitToQueueWithPolling("test33a_" + queue, queue, 1);
        }

    }

    @org.junit.Test
    public void test33b_testMultiBatchJobSubmitWithPolling() throws Exception {

        System.err.println("STARTING TEST test33b");

        for (String queue : config.getQueueNames()) {
            submitToQueueWithPolling("test33b_" + queue, queue, 10);
        }
    }

    @org.junit.Test
    public void test34_batchJobSubmitWithKill() throws Exception {

        String workingDir = getWorkingDir("test34");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);

        Path root = resolve(filesystem, workingDir);
        files.createDirectories(root);

        JobDescription description = new JobDescription();
        description.setExecutable("/bin/sleep");
        description.setArguments("60");
        description.setInteractive(false);
        description.setWorkingDirectory(workingDir);
        description.setStdin(null);
        description.setStdout("stdout.txt");
        description.setStderr("stderr.txt");

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

        Path out = resolve(root, description.getStdout());
        Path err = resolve(root, description.getStderr());

        if (files.exists(out)) {
            files.delete(out);
        }

        if (files.exists(err)) {
            files.delete(err);
        }

        files.delete(root);
        files.close(filesystem);

        assertTrue(status.hasException());
        Exception e = status.getException();

        if (!(e instanceof JobCanceledException)) {
            throw new Exception("test34 expected JobCanceledException, not " + e.getMessage(), e);
        }
    }

    @org.junit.Test
    public void test35_batchJobSubmitWithKill2() throws Exception {

        String workingDir = getWorkingDir("test35");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);

        Path root = resolve(filesystem, workingDir);
        files.createDirectories(root);

        JobDescription description = new JobDescription();
        description.setExecutable("/bin/sleep");
        description.setArguments("60");
        description.setInteractive(false);
        description.setWorkingDirectory(workingDir);
        description.setStdin(null);
        description.setStdout("stdout.txt");
        description.setStderr("stderr.txt");

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

        Path out = resolve(root, description.getStdout());
        Path err = resolve(root, description.getStderr());

        if (files.exists(out)) {
            files.delete(out);
        }

        if (files.exists(err)) {
            files.delete(err);
        }

        files.delete(root);
        files.close(filesystem);

        assertTrue(status.hasException());
        Exception e = status.getException();

        assertTrue(e instanceof JobCanceledException);
    }

    @org.junit.Test
    public void test36a_batchJobSubmitWithInput() throws Exception {

        String message = "Hello World! test36a";
        String workingDir = getWorkingDir("test36a");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);

        Path root = resolve(filesystem, workingDir);
        files.createDirectories(root);

        Path stdin = resolve(root, "stdin.txt");

        OutputStream out = files.newOutputStream(stdin, OpenOption.CREATE, OpenOption.APPEND, OpenOption.WRITE);
        writeFully(out, message);

        JobDescription description = new JobDescription();
        description.setExecutable("/bin/cat");
        description.setInteractive(false);
        description.setWorkingDirectory(workingDir);
        description.setStdin("stdin.txt");
        description.setStdout("stdout.txt");
        description.setStderr("stderr.txt");

        Job job = jobs.submitJob(scheduler, description);

        JobStatus status = jobs.waitUntilDone(job, config.getQueueWaitTime() + config.getUpdateTime());

        if (!status.isDone()) {
            throw new Exception("Job exceeded deadline! status = " + status);
        }

        if (status.hasException()) {
            throw new Exception("Job failed! exception is = ", status.getException());
        }

        jobs.close(scheduler);

        Path stdout = resolve(root, "stdout.txt");
        Path stderr = resolve(root, "stderr.txt");

        String tmpout = readFully(files.newInputStream(stdout));
        String tmperr = readFully(files.newInputStream(stderr));

        files.delete(stdin);
        files.delete(stdout);
        files.delete(stderr);
        files.delete(root);
        files.close(filesystem);

        System.err.println("STDOUT: " + tmpout);
        System.err.println("STDERR: " + tmperr);

        assertTrue(tmpout != null);
        assertTrue(tmpout.length() > 0);
        assertTrue(tmpout.equals(message));
        assertTrue(tmperr.length() == 0);
    }

    @org.junit.Test
    public void test36b_batchJobSubmitWithInput() throws Exception {

        String message = "Hello World! test36b";
        String workingDir = getWorkingDir("test36b");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);

        Path root = resolve(filesystem, workingDir);
        files.createDirectories(root);

        Path stdin = resolve(root, "stdin.txt");

        OutputStream out = files.newOutputStream(stdin, OpenOption.CREATE, OpenOption.APPEND, OpenOption.WRITE);
        writeFully(out, message);

        JobDescription description = new JobDescription();
        description.setExecutable("/bin/cat");
        description.setInteractive(false);
        description.setWorkingDirectory(workingDir);
        description.setStdin("stdin.txt");
        description.setStdout("stdout.txt");
        description.setStderr("stderr.txt");

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

        Path stdout = resolve(root, "stdout.txt");
        Path stderr = resolve(root, "stderr.txt");

        String tmpout = readFully(files.newInputStream(stdout));
        String tmperr = readFully(files.newInputStream(stderr));

        System.err.println("STDOUT: " + tmpout);
        System.err.println("STDERR: " + tmperr);

        files.delete(stdin);
        files.delete(stdout);
        files.delete(stderr);
        files.delete(root);
        files.close(filesystem);

        assertTrue(tmpout != null);
        assertTrue(tmpout.length() > 0);
        assertTrue(tmpout.equals(message));
        assertTrue(tmperr.length() == 0);
    }

    @org.junit.Test
    public void test37a_batchJobSubmitWithoutWorkDir() throws Exception {
        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);

        JobDescription description = new JobDescription();
        description.setExecutable("/bin/sleep");
        description.setArguments("1");
        description.setInteractive(false);
        description.setWorkingDirectory(null);
        description.setStdout(null);
        description.setStderr(null);

        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, 60000);

        if (!status.isDone()) {
            throw new Exception("Job exceeded deadline!");
        }

        if (status.hasException()) {
            throw new Exception("Job failed!", status.getException());
        }
        jobs.close(scheduler);
    }

    @org.junit.Test
    public void test37b_batchJobSubmitWithRelativeWorkDir() throws Exception {
        String workingDir = getWorkingDir("test37b");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);

        Path root = resolve(filesystem, workingDir);
        files.createDirectories(root);

        JobDescription description = new JobDescription();
        description.setExecutable("/bin/sleep");
        description.setArguments("1");
        description.setInteractive(false);
        description.setStdout(null);
        description.setStderr(null);
        //relative working dir name used
        description.setWorkingDirectory(workingDir);

        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, 60000);

        if (!status.isDone()) {
            throw new Exception("Job exceeded deadline!");
        }

        if (status.hasException()) {
            throw new Exception("Job failed!", status.getException());
        }

        files.delete(root);
        jobs.close(scheduler);
        files.close(filesystem);
    }

    @org.junit.Test
    public void test37c_batchJobSubmitWithAbsoluteWorkDir() throws Exception {
        String workingDir = getWorkingDir("test37c");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);

        Path root = resolve(filesystem, workingDir);
        files.createDirectories(root);

        JobDescription description = new JobDescription();
        description.setExecutable("/bin/sleep");
        description.setArguments("1");
        description.setInteractive(false);
        description.setStdout(null);
        description.setStderr(null);

        //absolute working dir name used
        description.setWorkingDirectory(root.getPathname().getAbsolutePath());

        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, 60000);

        if (!status.isDone()) {
            throw new Exception("Job exceeded deadline!");
        }

        if (status.hasException()) {
            throw new Exception("Job failed!", status.getException());
        }

        files.delete(root);
        jobs.close(scheduler);
        files.close(filesystem);
    }

    @org.junit.Test
    public void test37d_batchJobSubmitWithIncorrectWorkingDir() throws Exception {
        //note that we are _not_ creating this directory, making it invalid
        String workingDir = getWorkingDir("test37d");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);

        JobDescription description = new JobDescription();
        description.setExecutable("/bin/sleep");
        description.setArguments("1");
        description.setInteractive(false);
        description.setStdout(null);
        description.setStderr(null);

        //incorrect working dir used
        description.setWorkingDirectory(workingDir);

        //submitting this job will either:
        // 1) throw an InvalidJobDescription when we submit the job
        // 2) produce an error when the job is run.

        Job job = null;
        try {
            job = jobs.submitJob(scheduler, description);
        } catch (InvalidJobDescriptionException e) {
            //Submit failed, as expected (1)
            jobs.close(scheduler);
            return;
        }

        JobStatus status = jobs.waitUntilDone(job, 60000);

        if (!status.isDone()) {
            fail("Job exceeded deadline! Expected status done, got " + status);
        }

        //option (2)
        assertTrue(status.hasException());
        jobs.close(scheduler);
    }

    @org.junit.Test
    public void test37e_batchJobSubmitWithWorkDirWithSpaces() throws Exception {
        //note the space in the path
        String workingDir = getWorkingDir("test 37b");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);

        Path root = resolve(filesystem, workingDir);
        files.createDirectories(root);

        JobDescription description = new JobDescription();
        description.setExecutable("/bin/sleep");
        description.setArguments("1");
        description.setInteractive(false);
        description.setStdout(null);
        description.setStderr(null);
        //relative working dir name used
        description.setWorkingDirectory(workingDir);

        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, 60000);

        if (!status.isDone()) {
            throw new Exception("Job exceeded deadline!");
        }

        if (status.hasException()) {
            throw new Exception("Job failed!", status.getException());
        }

        files.delete(root);
        jobs.close(scheduler);
        files.close(filesystem);
    }

    //@org.junit.Test
    public void test38_multipleBatchJobSubmitWithInput() throws Exception {
        String message = "Hello World! test38";
        String workingDir = getWorkingDir("test38");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);

        Path root = resolve(filesystem, workingDir);
        files.createDirectories(root);

        Path stdin = resolve(root, "stdin.txt");

        OutputStream out = files.newOutputStream(stdin, OpenOption.CREATE, OpenOption.APPEND, OpenOption.WRITE);
        writeFully(out, message);

        JobDescription description = new JobDescription();
        description.setExecutable("/bin/cat");
        description.setInteractive(false);
        description.setProcessesPerNode(2);
        description.setWorkingDirectory(workingDir);
        description.setStdin("stdin.txt");

        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, 60000);

        if (!status.isDone()) {
            throw new Exception("Job exceeded deadline!");
        }

        if (status.hasException()) {
            throw new Exception("Job failed!", status.getException());
        }

        for (int i = 0; i < 2; i++) {

            Path stdoutTmp = resolve(root, "stdout.txt." + i);
            Path stderrTmp = resolve(root, "stderr.txt." + i);

            String tmpout = readFully(files.newInputStream(stdoutTmp));
            String tmperr = readFully(files.newInputStream(stderrTmp));

            System.err.println("STDOUT: " + tmpout);
            System.err.println("STDERR: " + tmperr);

            assertTrue(tmpout != null);
            assertTrue(tmpout.length() > 0);
            assertTrue(tmpout.equals(message));
            assertTrue(tmperr.length() == 0);

            files.delete(stdoutTmp);
            files.delete(stderrTmp);
        }

        files.delete(stdin);
        files.delete(root);

        jobs.close(scheduler);
        files.close(filesystem);
    }

    @org.junit.Test
    public void test39_multipleBatchJobSubmitWithExceptions() throws Exception {

        // NOTE: This test assumes that an exception is thrown when the status of a job is requested twice after the job is done!
        //       This may not be true for all schedulers.

        if (config.supportsStatusAfterDone()) {
            return;
        }

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);

        JobDescription description = new JobDescription();
        description.setExecutable("/bin/sleep");
        description.setArguments("1");
        description.setInteractive(false);
        description.setWorkingDirectory(null);

        Job[] j = new Job[2];

        j[0] = jobs.submitJob(scheduler, description);

        description = new JobDescription();
        description.setExecutable("/bin/sleep");
        description.setArguments("2");
        description.setInteractive(false);
        description.setWorkingDirectory(null);

        j[1] = jobs.submitJob(scheduler, description);

        long now = System.currentTimeMillis();
        long deadline = now + 10000;

        JobStatus[] s = null;

        while (now < deadline) {

            s = jobs.getJobStatuses(j);

            if (s[0].hasException() && s[1].hasException()) {
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignored
            }

            now = System.currentTimeMillis();
        }

        if (s == null || !(s[0].hasException() && s[1].hasException())) {
            throw new Exception("Job exceeded deadline!");
        }

        jobs.close(scheduler);
    }

    @org.junit.Test
    public void test40_batchJobSubmitWithExitcode() throws Exception {

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);

        JobDescription description = new JobDescription();
        description.setExecutable("/bin/sleep");
        description.setArguments("1");
        description.setInteractive(false);

        description.setWorkingDirectory(null);
        description.setStderr(null);
        description.setStdout(null);
        description.setStdin(null);

        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, 60000);

        if (!status.isDone()) {
            throw new Exception("Job exceeded deadline!");
        }

        if (status.hasException()) {
            throw new Exception("Job failed!", status.getException());
        }

        jobs.close(scheduler);

        assertTrue(status.getExitCode() == 0);
    }

    @org.junit.Test
    public void test40_batchJobSubmitWithNoneZeroExitcode() throws Exception {

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);

        //run an ls with a non existing file. This should make ls return exitcode 2
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/ls");
        description.setArguments("non.existing.file");
        description.setInteractive(false);

        description.setWorkingDirectory(null);
        description.setStderr(null);
        description.setStdout(null);
        description.setStdin(null);

        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, 60000);

        if (!status.isDone()) {
            throw new Exception("Job exceeded deadline!");
        }

        if (status.hasException()) {
            throw new Exception("Job failed!", status.getException());
        }

        jobs.close(scheduler);

        assertTrue(status.getExitCode() == 2);
    }

    @org.junit.Test
    public void test41_batchJobSubmitWithEnvironmentVariable() throws Exception {

        if (!config.supportsEnvironmentVariables()) {
            return;
        }

        String workingDir = getWorkingDir("test41");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);

        Path root = resolve(filesystem, workingDir);
        files.createDirectories(root);

        //echo the given variable, to see if the va
        JobDescription description = new JobDescription();
        description.setExecutable("/usr/bin/printenv");
        description.setArguments("SOME_VARIABLE");
        description.setInteractive(false);
        description.addEnvironment("SOME_VARIABLE", "some_value");

        description.setWorkingDirectory(workingDir);
        description.setStderr(null);
        description.setStdout("stdout.txt");
        description.setStdin(null);

        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, 60000);

        if (!status.isDone()) {
            throw new Exception("Job exceeded deadline!");
        }

        if (status.hasException()) {
            throw new Exception("Job failed!", status.getException());
        }

        Path stdout = resolve(root, "stdout.txt");

        String stdoutContent = readFully(files.newInputStream(stdout));

        assertTrue(stdoutContent.equals("some_value\n"));

        files.delete(stdout);
        files.delete(root);
        files.close(filesystem);
    }

    @org.junit.Test
    public void test41b_batchJobSubmitWithEnvironmentVariable() throws Exception {

        if (config.supportsEnvironmentVariables()) {
            return;
        }

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);

        // echo the given variable, to see if the va
        JobDescription description = new JobDescription();
        description.setExecutable("/usr/bin/printenv");
        description.setArguments("SOME_VARIABLE");
        description.setInteractive(false);
        description.addEnvironment("SOME_VARIABLE", "some_value");

        description.setWorkingDirectory(null);
        description.setStderr(null);
        description.setStdin(null);

        boolean gotException = false;

        try {
            Job job = jobs.submitJob(scheduler, description);
            jobs.waitUntilDone(job, config.getUpdateTime());
        } catch (UnsupportedJobDescriptionException e) {
            gotException = true;
        }

        jobs.close(scheduler);

        if (!gotException) {
            throw new Exception("Submit did not throw exception, which was expected!");
        }
    }

    @Test
    public void test42a_batchJob_parallel_Exception() throws Exception {

        if (config.supportsParallelJobs()) {
            return;
        }

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);

        JobDescription description = new JobDescription();
        description.setExecutable("/bin/echo");
        description.setNodeCount(2);
        description.setProcessesPerNode(2);

        boolean gotException = false;
        try {
            jobs.submitJob(scheduler, description);
        } catch (InvalidJobDescriptionException e) {
            gotException = true;
        } finally {
            jobs.close(scheduler);
        }

        if (!gotException) {
            throw new Exception("Submit did not throw exception, which was expected!");
        }
    }

    @org.junit.Test
    public void test43_submit_JobDescriptionShouldBeCopied_Success() throws Exception {

        String workingDir = getWorkingDir("test43");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);

        Path root = resolve(filesystem, workingDir);
        files.createDirectories(root);

        JobDescription description = new JobDescription();
        description.setExecutable("non-existing-executable");
        description.setInteractive(false);
        description.setWorkingDirectory(workingDir);
        description.setStdout("stdout.txt");

        Job job = jobs.submitJob(scheduler, description);

        description.setStdout("aap.txt");

        JobDescription original = job.getJobDescription();

        assertEquals("Job description should have been copied!", "stdout.txt", original.getStdout());

        JobStatus status = jobs.cancelJob(job);

        if (!status.isDone()) {
            jobs.waitUntilDone(job, 60000);
        }

        Path out = resolve(root, "stdout.txt");

        if (files.exists(out)) {
            files.delete(out);
        }

        files.delete(root);
        files.close(filesystem);
    }
}
