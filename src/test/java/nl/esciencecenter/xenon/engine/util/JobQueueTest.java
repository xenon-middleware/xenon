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
package nl.esciencecenter.xenon.engine.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.engine.jobs.JobImplementation;
import nl.esciencecenter.xenon.engine.jobs.StreamsImplementation;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;
import nl.esciencecenter.xenon.jobs.IncompleteJobDescriptionException;
import nl.esciencecenter.xenon.jobs.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.Scheduler;
import nl.esciencecenter.xenon.jobs.Streams;
import nl.esciencecenter.xenon.util.Utils;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 */
public class JobQueueTest {

    public static final int POLLING_DELAY = JobQueues.MIN_POLLING_DELAY;
    public static final int TEST_POLLING_DELAY = POLLING_DELAY + 10;

    static class MyProcessWrapper implements InteractiveProcess {

        private final JobImplementation job;
        private final byte[] error;
        private final byte[] output;

        private boolean destroyed = false;
        private boolean done = false;
        private int exit = -1;

        MyProcessWrapper(JobImplementation job, byte[] output, byte[] error) {
            this.job = job;
            this.output = output;
            this.error = error;
        }

        @Override
        public Streams getStreams() {
            return new StreamsImplementation(job, new ByteArrayInputStream(output), new ByteArrayOutputStream(),
                    new ByteArrayInputStream(error));
        }

        public synchronized void setDone() {
            done = true;
        }

        @Override
        public synchronized boolean isDone() {
            return done;
        }

        public synchronized void setExitStatus(int exit) {
            this.exit = exit;
        }

        @Override
        public synchronized int getExitStatus() {
            return exit;
        }

        @Override
        public synchronized void destroy() {
            destroyed = true;
        }

        public synchronized boolean isDestroyed() {
            return destroyed;
        }
    }

    static class MyFactory implements InteractiveProcessFactory {

        private boolean fail = false;

        public void setFail(boolean value) {
            fail = value;
        }

        @Override
        public InteractiveProcess createInteractiveProcess(JobImplementation job) throws XenonException {

            if (fail) {
                setCurrentWrapper(null);
                throw new XenonException("JQT", "Failed to create process!");
            }

            MyProcessWrapper wrapper = new MyProcessWrapper(job, new byte[0], new byte[0]);

            setCurrentWrapper(wrapper);

            return wrapper;
        }
    }

    private static Scheduler scheduler;
    private static Path cwd;
    private static Files files;
    private static FileSystem filesystem;
    private static JobQueues jobQueue;
    private static MyFactory myFactory;

    private static MyProcessWrapper currentWrapper;

    private synchronized static void setCurrentWrapper(MyProcessWrapper wrapper) {
        currentWrapper = wrapper;
    }

    @BeforeClass
    public static void prepare() throws Exception {
        scheduler = Xenon.jobs().newScheduler("local", "local://test", null, null);
        files = Xenon.files();
        cwd = Utils.getLocalCWD(files);
        filesystem = cwd.getFileSystem();
        myFactory = new MyFactory();
        jobQueue = new JobQueues("test", files, scheduler, cwd, myFactory, 2, POLLING_DELAY);
    }

    @AfterClass
    public static void cleanup() throws Exception {

        Job[] jobs = jobQueue.getJobs();

        if (jobs != null && jobs.length > 0) {

            System.err.println("Jobs stuck in queue: " + jobs.length);

            for (Job job : jobs) {
                System.err.println("   " + job);
            }

            throw new Exception("There are jobs stuck in the queue!");
        }
    }

    @After
    public void cleanupTest() throws Exception {

        RelativePath entryPath = filesystem.getEntryPath().getRelativePath();
        
        Path p = files.newPath(filesystem, entryPath.resolve("stderr.txt"));

        if (files.exists(p)) {
            files.delete(p);
        }

        p = files.newPath(filesystem, entryPath.resolve("stdout.txt"));

        if (files.exists(p)) {
            files.delete(p);
        }
    }

    @Test(expected = BadParameterException.class)
    public void test_constructor2() throws Exception {
        // throws exception
        new JobQueues("test", files, scheduler, cwd, myFactory, 0, POLLING_DELAY);
    }

    @Test(expected = BadParameterException.class)
    public void test_constructor3() throws Exception {
        // throws exception
        new JobQueues("test", files, scheduler, cwd, myFactory, 2, 1);
    }

    @Test(expected = BadParameterException.class)
    public void test_constructor4() throws Exception {
        // throws exception
        new JobQueues("test", files, scheduler, cwd, myFactory, 2, 100000);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_invalidJobDescription1() throws Exception {
        JobDescription d = new JobDescription();
        d.setQueueName("aap");
        jobQueue.submitJob(d);
    }

    @Test(expected = IncompleteJobDescriptionException.class)
    public void test_incompleteJobDescription1() throws Exception {
        jobQueue.submitJob(new JobDescription());
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_invalidJobDescription2() throws Exception {
        JobDescription d = new JobDescription();
        d.setExecutable("exec_invalidJobDescription2");
        d.setNodeCount(42);
        jobQueue.submitJob(d);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_invalidJobDescription3() throws Exception {
        JobDescription d = new JobDescription();
        d.setExecutable("exec_invalidJobDescription3");
        d.setProcessesPerNode(0);
        jobQueue.submitJob(d);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_invalidJobDescription4() throws Exception {
        JobDescription d = new JobDescription();
        d.setExecutable("exec_invalidJobDescription4");
        d.setMaxTime(-1);
        jobQueue.submitJob(d);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_invalidJobDescription5() throws Exception {
        JobDescription d = new JobDescription();
        d.setExecutable("exec_invalidJobDescription5");
        d.setInteractive(true);
        d.setStdin("aap");
        jobQueue.submitJob(d);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_invalidJobDescription6() throws Exception {
        JobDescription d = new JobDescription();
        d.setExecutable("exec_invalidJobDescription6");
        d.setInteractive(true);
        d.setStdout("aap");
        jobQueue.submitJob(d);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_invalidJobDescription7() throws Exception {
        JobDescription d = new JobDescription();
        d.setExecutable("exec_invalidJobDescription7");
        d.setInteractive(true);
        d.setStderr("aap");
        jobQueue.submitJob(d);
    }

    //    @Test(expected = InvalidJobDescriptionException.class)
    //    public void test_invalidJobDescription8() throws Exception {
    //        JobDescription d = new JobDescription();
    //        d.setExecutable("exec_invalidJobDescription8");
    //        d.setStdout(null);
    //        jobQueue.submitJob(d);
    //    }
    //
    //    @Test(expected = InvalidJobDescriptionException.class)
    //    public void test_invalidJobDescription9() throws Exception {
    //        JobDescription d = new JobDescription();
    //        d.setExecutable("exec_invalidJobDescription9");
    //        d.setStderr(null);
    //        jobQueue.submitJob(d);
    //    }

    @Test(expected = XenonException.class)
    public void test_failingInteractiveJob1() throws Exception {
        JobDescription d = new JobDescription();
        d.setExecutable("exec_failingInteractiveJob1");
        d.setInteractive(true);
        d.setQueueName("unlimited");

        myFactory.setFail(true);

        try {
            // throws exception
            jobQueue.submitJob(d);
        } finally {
            myFactory.setFail(false);
        }
    }

    @Test(expected = XenonException.class)
    public void test_failingInteractiveJob2() throws Exception {
        JobDescription d = new JobDescription();
        d.setExecutable("exec_failingInteractiveJob1");
        d.setInteractive(true);
        d.setQueueName("unlimited");
        d.setStderr(null);
        d.setStdout(null);

        myFactory.setFail(true);

        try {
            // throws exception
            jobQueue.submitJob(d);
        } finally {
            myFactory.setFail(false);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_invalidWaitTimeout() throws Exception {
        // throws exception
        jobQueue.waitUntilDone(mock(Job.class), -42);
    }

    @Test(expected = XenonException.class)
    public void test_invalidScheduler() throws Exception {
        Scheduler s = mock(Scheduler.class);
        Job job = mock(Job.class);
        when(job.getScheduler()).thenReturn(s);

        // throws exception
        jobQueue.getJobStatus(job);
    }

    @Test(expected = XenonException.class)
    public void test_invalidQueueName() throws Exception {

        JobDescription d = new JobDescription();
        d.setQueueName("test");

        Job job = mock(Job.class);
        when(job.getScheduler()).thenReturn(scheduler);
        when(job.getJobDescription()).thenReturn(d);

        // throws exception
        jobQueue.getJobStatus(job);
    }

    @Test(expected = XenonException.class)
    public void test_invalidJob() throws Exception {

        JobDescription d = new JobDescription();
        d.setQueueName("unlimited");

        Job job = mock(Job.class);
        when(job.getScheduler()).thenReturn(scheduler);
        when(job.getJobDescription()).thenReturn(d);

        // throws exception
        jobQueue.getJobStatus(job);
    }

    @Test
    public void test_cancelJob1() throws Exception {

        JobDescription d = new JobDescription();
        d.setExecutable("exec_cancelJob1");
        d.setQueueName("unlimited");

        Job job = jobQueue.submitJob(d);

        // Wait for at least the polling delay!
        try {
            Thread.sleep(TEST_POLLING_DELAY);
        } catch (InterruptedException e) {
            // ignored
        }

        JobStatus status = jobQueue.cancelJob(job);

        assertNotNull(status);
        assertTrue(status.isDone());
        assertTrue(status.hasException());
    }

    @Test
    public void test_cancelJob2() throws Exception {

        JobDescription d = new JobDescription();
        d.setExecutable("exec_cancelJob2");
        d.setQueueName("unlimited");

        Job job = jobQueue.submitJob(d);

        // Wait for at least the polling delay!
        try {
            Thread.sleep(TEST_POLLING_DELAY);
        } catch (InterruptedException e) {
            // ignored
        }

        currentWrapper.setExitStatus(42);
        currentWrapper.setDone();

        JobStatus status = jobQueue.cancelJob(job);

        assertNotNull(status);
        assertTrue(status.isDone());
        assertFalse(status.hasException());
        assertTrue(status.getExitCode() == 42);
    }

    @Test
    public void test_pollJob() throws Exception {

        JobDescription d = new JobDescription();
        d.setExecutable("exec_pollJob");
        d.setQueueName("unlimited");

        Job job = jobQueue.submitJob(d);

        // Wait for at least the polling delay!
        try {
            Thread.sleep(TEST_POLLING_DELAY);
        } catch (InterruptedException e) {
            // ignored
        }

        JobStatus status = jobQueue.getJobStatus(job);

        assertNotNull(status);
        assertFalse(status.isDone());

        currentWrapper.setExitStatus(42);
        currentWrapper.setDone();

        // Wait for at least the polling delay!
        try {
            Thread.sleep(TEST_POLLING_DELAY);
        } catch (InterruptedException e) {
            // ignored
        }

        status = jobQueue.getJobStatus(job);

        assertNotNull(status);
        assertTrue(status.isDone());
        assertFalse(status.hasException());
        assertTrue(status.getExitCode() == 42);
    }

    @Test
    public void test_waitForJob() throws Exception {
        JobDescription d = new JobDescription();
        d.setExecutable("exec_waitForJob");
        d.setQueueName("unlimited");

        Job job = jobQueue.submitJob(d);

        // Give the job 1 sec. to start.
        JobStatus status = jobQueue.waitUntilDone(job, TEST_POLLING_DELAY);

        assertNotNull(status);

        if (status.isDone()) {
            Exception e = status.getException();
            System.err.println("EEP: " + status.getException());
            e.printStackTrace(System.err);
        }

        assertFalse(status.isDone());

        currentWrapper.setExitStatus(42);
        currentWrapper.setDone();

        status = jobQueue.waitUntilDone(job, TEST_POLLING_DELAY);

        assertNotNull(status);
        assertTrue(status.isDone());
        assertFalse(status.hasException());
        assertTrue(status.getExitCode() == 42);
    }

    @Test
    public void test_getJobs1() throws Exception {

        JobDescription d = new JobDescription();
        d.setExecutable("exec_getJobs");
        d.setQueueName("unlimited");

        Job job = jobQueue.submitJob(d);

        Job[] status = jobQueue.getJobs();

        assertNotNull(status);
        assertTrue(status.length == 1);

        status = jobQueue.getJobs((String[]) null);

        assertNotNull(status);
        assertTrue(status.length == 1);

        jobQueue.cancelJob(job);
    }

    @Test
    public void test_getJobs2() throws Exception {

        JobDescription d = new JobDescription();
        d.setExecutable("exec_getJobs");
        d.setQueueName("unlimited");

        Job job = jobQueue.submitJob(d);

        Job[] status = jobQueue.getJobs("unlimited");

        assertNotNull(status);
        assertTrue(status.length == 1);

        status = jobQueue.getJobs("single");

        assertNotNull(status);
        assertTrue(status.length == 0);

        jobQueue.cancelJob(job);
    }

}
