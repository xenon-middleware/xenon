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

package nl.esciencecenter.octopus.engine.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.StreamsImplementation;
import nl.esciencecenter.octopus.exceptions.BadParameterException;
import nl.esciencecenter.octopus.exceptions.IncompleteJobDescriptionException;
import nl.esciencecenter.octopus.exceptions.InvalidJobDescriptionException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.Pathname;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.jobs.Streams;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class JobQueueTest {

    public static final int POLLING_DELAY = 250;

    static class MyProcessWrapper implements InteractiveProcess {

        final JobImplementation job;
        final byte[] output;
        final byte[] error;

        boolean destoyed = false;
        boolean done = false;
        int exit = -1;

        public MyProcessWrapper(JobImplementation job, byte[] output, byte[] error) {
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
            destoyed = true;
        }

        public synchronized boolean isDestroyed() {
            return destoyed;
        }
    }

    static class MyFactory implements InteractiveProcessFactory {

        private boolean fail = false;

        public void setFail(boolean value) {
            fail = value;
        }

        @Override
        public InteractiveProcess createInteractiveProcess(JobImplementation job) throws IOException {

            if (fail) {
                setCurrentWrapper(null);
                throw new IOException("Failed to create process!");
            }

            MyProcessWrapper wrapper = new MyProcessWrapper(job, new byte[0], new byte[0]);

            setCurrentWrapper(wrapper);

            return wrapper;
        }
    }

    public static Scheduler scheduler;
    public static Octopus octopus;
    public static Files files;
    public static FileSystem filesystem;
    public static JobQueues jobQueue;
    public static MyFactory myFactory;

    public static MyProcessWrapper currentWrapper;

    public synchronized static MyProcessWrapper getCurrentWrapper() {
        return currentWrapper;
    }

    public synchronized static void setCurrentWrapper(MyProcessWrapper wrapper) {
        currentWrapper = wrapper;
    }

    @BeforeClass
    public static void prepare() throws Exception {
        octopus = OctopusFactory.newOctopus(null);
        scheduler = octopus.jobs().getLocalScheduler();
        files = octopus.files();
        filesystem = files.getLocalCWDFileSystem();
        myFactory = new MyFactory();
        jobQueue = new JobQueues("test", files, scheduler, filesystem, myFactory, 2, POLLING_DELAY);
    }

    @AfterClass
    public static void cleanup() throws Exception {

        Job[] jobs = jobQueue.getJobs();

        if (jobs != null && jobs.length > 0) {

            System.err.println("Jobs stuck in queue: " + jobs.length);

            for (int i = 0; i < jobs.length; i++) {
                System.err.println("   " + jobs[i]);
            }

            throw new Exception("There are jobs stuck in the queue!");
        }
    }

    @After
    public void cleanupTest() throws Exception {

        Pathname entryPath = filesystem.getEntryPath().getPathname();
        
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
        new JobQueues("test", files, scheduler, filesystem, myFactory, 0, POLLING_DELAY);
    }

    @Test(expected = BadParameterException.class)
    public void test_constructor3() throws Exception {
        // throws exception
        new JobQueues("test", files, scheduler, filesystem, myFactory, 2, 1);
    }

    @Test(expected = BadParameterException.class)
    public void test_constructor4() throws Exception {
        // throws exception
        new JobQueues("test", files, scheduler, filesystem, myFactory, 2, 100000);
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

    @Test(expected = OctopusException.class)
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

    @Test(expected = OctopusException.class)
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

    @Test(expected = OctopusException.class)
    public void test_invalidWaitTimeout() throws Exception {
        // throws exception
        jobQueue.waitUntilDone(mock(Job.class), -42);
    }

    @Test(expected = OctopusException.class)
    public void test_invalidScheduler() throws Exception {
        Scheduler s = mock(Scheduler.class);
        Job job = mock(Job.class);
        when(job.getScheduler()).thenReturn(s);

        // throws exception
        jobQueue.getJobStatus(job);
    }

    @Test(expected = OctopusException.class)
    public void test_invalidQueueName() throws Exception {

        JobDescription d = new JobDescription();
        d.setQueueName("test");

        Job job = mock(Job.class);
        when(job.getScheduler()).thenReturn(scheduler);
        when(job.getJobDescription()).thenReturn(d);

        // throws exception
        jobQueue.getJobStatus(job);
    }

    @Test(expected = OctopusException.class)
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
            Thread.sleep(POLLING_DELAY * 2);
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
            Thread.sleep(POLLING_DELAY * 2);
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
            Thread.sleep(POLLING_DELAY * 2);
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
            Thread.sleep(POLLING_DELAY * 2);
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
        JobStatus status = jobQueue.waitUntilDone(job, 1000);

        assertNotNull(status);

        if (status.isDone()) {
            Exception e = status.getException();
            System.err.println("EEP: " + status.getException());
            e.printStackTrace(System.err);
        }

        assertFalse(status.isDone());

        currentWrapper.setExitStatus(42);
        currentWrapper.setDone();

        status = jobQueue.waitUntilDone(job, POLLING_DELAY * 3);

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
