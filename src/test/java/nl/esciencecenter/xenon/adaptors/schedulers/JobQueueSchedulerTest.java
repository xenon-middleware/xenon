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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.filesystems.MockFileSystem;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.schedulers.IncompleteJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;
import nl.esciencecenter.xenon.schedulers.NoSuchJobException;
import nl.esciencecenter.xenon.schedulers.NoSuchQueueException;
import nl.esciencecenter.xenon.schedulers.QueueStatus;
import nl.esciencecenter.xenon.schedulers.Streams;
import nl.esciencecenter.xenon.utils.OutputReader;

public class JobQueueSchedulerTest {

    @Test
    public void test_create() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {
            assertEquals("MockS", s.getAdaptorName());
        }
    }

    @Test(expected = BadParameterException.class)
    public void test_create_invalidMultiQThreads() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 0, 100, 10000L, null);
    }

    @Test(expected = BadParameterException.class)
    public void test_create_invalidPollTimeMin() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 1, 10000L, null);
    }

    @Test(expected = BadParameterException.class)
    public void test_create_invalidPollTimeMax() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, Integer.MAX_VALUE, 10000L, null);
    }

    @Test
    public void test_getDefaultQueue() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {
            assertEquals("single", s.getDefaultQueueName());
        }
    }

    @Test
    public void test_getQueues() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {
            assertArrayEquals(new String[] { "single", "multi", "unlimited" }, s.getQueueNames());
        }
    }

    @Test
    public void test_getCurrentJobID() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {
            s.getCurrentJobID();
        }
    }

    @Test(expected = NoSuchQueueException.class)
    public void test_verifyJobDescription_invalidQueue() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            JobDescription job = new JobDescription();
            job.setExecutable("/bin/aap");
            job.setQueueName("foobar");
            s.submitBatchJob(job);
        }
    }

    @Test(expected = IncompleteJobDescriptionException.class)
    public void test_verifyJobDescription_noExecutable() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            JobDescription job = new JobDescription();
            s.submitBatchJob(job);
        }
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_verifyJobDescription_invalidNodeCount() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {
            JobDescription job = new JobDescription();
            job.setExecutable("/bin/aap");
            job.setTasks(42);
            s.submitBatchJob(job);
        }
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_verifyJobDescription_invalidTasksPerNode() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {
            JobDescription job = new JobDescription();
            job.setExecutable("/bin/aap");
            job.setTasksPerNode(42);
            s.submitBatchJob(job);
        }
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_verifyJobDescription_invalidMaxTime() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            JobDescription job = new JobDescription();
            job.setExecutable("/bin/aap");
            job.setMaxRuntime(-3);
            s.submitBatchJob(job);
        }
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_verifyJobDescription_invalidStdin() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            JobDescription job = new JobDescription();
            job.setExecutable("/bin/aap");
            job.setStdin("/tmp/stdin.txt");
            s.submitInteractiveJob(job);
        }
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_verifyJobDescription_invalidStdout() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            JobDescription job = new JobDescription();
            job.setExecutable("/bin/aap");
            job.setStdout("/tmp/stdout.txt");
            s.submitInteractiveJob(job);
        }
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_verifyJobDescription_invalidStderr() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            JobDescription job = new JobDescription();
            job.setExecutable("/bin/aap");
            job.setStderr("/tmp/stderr.txt");
            s.submitInteractiveJob(job);
        }
    }

    @Test
    public void test_getJobs_empty() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            String[] result = s.getJobs();
            assertNotNull(result);
            assertTrue(result.length == 0);
        }
    }

    @Test
    public void test_getJobs_null() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            String[] queues = null;
            String[] result = s.getJobs(queues);

            assertNotNull(result);
            assertTrue(result.length == 0);
        }
    }

    @Test
    public void test_getJobs_notEmpty() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            JobDescription job = new JobDescription();
            job.setExecutable("/bin/aap");

            String jobID = s.submitBatchJob(job);

            String[] result = s.getJobs(new String[0]);

            assertNotNull(result);
            assertTrue(result.length == 1);
            assertEquals(jobID, result[0]);
        }
    }

    @Test(expected = NoSuchQueueException.class)
    public void test_getJobs_invalidQueue() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {
            s.getJobs("foobar");
        }
    }

    @Test
    public void test_getJobs_specificQueueEmpty() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            JobDescription job = new JobDescription();
            job.setExecutable("/bin/aap");
            job.setQueueName("multi");

            String jobID = s.submitBatchJob(job);

            String[] result = s.getJobs("single");

            assertNotNull(result);
            assertTrue(result.length == 0);

            result = s.getJobs("multi");

            assertNotNull(result);
            assertTrue(result.length == 1);
            assertEquals(jobID, result[0]);

            result = s.getJobs("unlimited");

            assertNotNull(result);
            assertTrue(result.length == 0);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_getJobStatus_null() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {
            s.getJobStatus(null);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_getJobStatus_empty() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {
            s.getJobStatus(null);
        }
    }

    @Test(expected = NoSuchJobException.class)
    public void test_getJobStatus_unknownJob() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {
            s.getJobStatus("foo");
        }
    }

    @Test
    public void test_getJobStatus_knownJob() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory(100);

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            JobDescription job = new JobDescription();
            job.setExecutable("/bin/aap");
            job.setQueueName("unlimited");

            String jobID = s.submitBatchJob(job);

            JobStatus status = s.getJobStatus(jobID);

            status = s.waitUntilDone(jobID, 1000);

            assertTrue(status.isDone());
        }
    }

    @Test
    public void test_getJobStatus_afterDone() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory(50);

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            JobDescription job = new JobDescription();
            job.setExecutable("/bin/aap");
            job.setQueueName("unlimited");

            // Submit 50 ms job.
            String jobID = s.submitBatchJob(job);

            // Wait for 250 ms for the job to finish.
            Thread.sleep(250);

            JobStatus status = s.getJobStatus(jobID);
            assertTrue(status.isDone());
        }
    }

    @Test
    public void test_waitUntilRunning() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory(100);

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            JobDescription job = new JobDescription();
            job.setExecutable("/bin/aap");
            job.setQueueName("single");

            // Submit 2 100 ms. jobs that will be run sequentially
            String jobID1 = s.submitBatchJob(job);
            String jobID2 = s.submitBatchJob(job);

            // Wait up to 1000 ms for second job to start.
            JobStatus status = s.waitUntilRunning(jobID2, 1000);

            // Second job should have started after this time.
            assertTrue(status.isRunning());

            // Wait up to 1000 ms for second job to finish.
            status = s.waitUntilDone(jobID2, 1000);

            // Second job should have finished after this time.
            assertTrue(status.isDone());
        }
    }

    @Test
    public void test_waitUntilDone() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory(100);

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            JobDescription job = new JobDescription();
            job.setExecutable("/bin/aap");
            job.setQueueName("single");

            // Submit 2 100 ms. jobs that will be run sequentially
            String jobID1 = s.submitBatchJob(job);
            String jobID2 = s.submitBatchJob(job);

            // Wait up to 1000 ms for second job to start.
            JobStatus status = s.waitUntilDone(jobID2, 1000);

            // Second job should have finished after this time.
            assertTrue(status.isDone());

            // Wait up to 1000 ms for first job to start (should have been done?)
            status = s.waitUntilDone(jobID1, 1000);

            // First job should have finished after this time.
            assertTrue(status.isDone());

        }
    }

    @Test
    public void test_waitUntilRunningWhenDone() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory(100);

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            JobDescription job = new JobDescription();
            job.setExecutable("/bin/aap");
            job.setQueueName("single");

            // Submit 2 100 ms. jobs that will be run sequentially
            String jobID1 = s.submitBatchJob(job);
            String jobID2 = s.submitBatchJob(job);

            // Wait up to 1000 ms for second job to start.
            JobStatus status = s.waitUntilDone(jobID2, 1000);

            // Both jobs should have finished after this time.
            assertTrue(status.isDone());

            // First job should already be done!
            status = s.waitUntilRunning(jobID1, 1000);

            assertTrue(status.isDone());
        }
    }

    @Test
    public void test_waitUntilDoneWhenNotDone() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory(500);

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            JobDescription job = new JobDescription();
            job.setExecutable("/bin/aap");
            job.setQueueName("single");

            // Submit a 500 ms. job
            String jobID = s.submitBatchJob(job);

            // Wait up to 100 ms for job to start.
            JobStatus status = s.waitUntilRunning(jobID, 100);

            // Job should be running now
            assertTrue(status.isRunning());

            // Wait up to 100 ms for job to finish.
            status = s.waitUntilDone(jobID, 100);

            // Job should not be done
            assertFalse(status.isDone());

            // Now wait up to 1000 ms for job to finish
            status = s.waitUntilDone(jobID, 1000);

            // And now it should be done!
            assertTrue(status.isDone());
        }
    }

    @Test
    public void test_cancel_immediately() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory(500);

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            JobDescription job = new JobDescription();
            job.setExecutable("/bin/aap");
            job.setQueueName("unlimited");

            // Submit 50 ms job.
            String jobID = s.submitBatchJob(job);

            JobStatus status = s.cancelJob(jobID);

            assertTrue(status.isDone());
        }
    }

    @Test
    public void test_cancel_whenRunning() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory(500);

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            JobDescription job = new JobDescription();
            job.setExecutable("/bin/aap");
            job.setQueueName("unlimited");

            // Submit 50 ms job.
            String jobID = s.submitBatchJob(job);

            JobStatus status = s.waitUntilRunning(jobID, 250);

            assertTrue(status.isRunning());

            status = s.cancelJob(jobID);

            assertTrue(status.isDone());
        }
    }

    @Test
    public void test_cancel_afterDone() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory(100);

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            JobDescription job = new JobDescription();
            job.setExecutable("/bin/aap");
            job.setQueueName("unlimited");

            // Submit 100 ms job.
            String jobID = s.submitBatchJob(job);

            JobStatus status = s.waitUntilRunning(jobID, 500);

            assertTrue(status.isRunning());

            Thread.sleep(200);

            status = s.cancelJob(jobID);

            assertTrue(status.isDone());
        }
    }

    @Test
    public void test_submitInteractive() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory();

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            JobDescription job = new JobDescription();
            job.setExecutable("/bin/aap");

            // Submit 100 ms job.
            Streams streams = s.submitInteractiveJob(job);

            OutputReader stdout = new OutputReader(streams.getStdout());
            OutputReader stderr = new OutputReader(streams.getStderr());

            streams.getStdin().close();

            stderr.waitUntilFinished();
            stdout.waitUntilFinished();

            assertEquals("Hello World\n", stdout.getResultAsString());

            JobStatus status = s.cancelJob(streams.getJobIdentifier());

            if (!status.isDone()) {
                status = s.waitUntilDone(streams.getJobIdentifier(), 1000);
            }

            assertTrue(status.isDone());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_getQueueStatus_null() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory(100);

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {
            s.getQueueStatus(null);
        }
    }

    @Test(expected = NoSuchQueueException.class)
    public void test_getQueueStatus_unknownQueue() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory(100);

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {
            s.getQueueStatus("foobar");
        }
    }

    @Test
    public void test_getQueueStatus_validQueues() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory(100);

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            String[] queueNames = s.getQueueNames();

            for (String q : queueNames) {
                QueueStatus status = s.getQueueStatus(q);
                assertEquals(q, status.getQueueName());
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_getQueueStatuses_null() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory(100);

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            String[] q = null;
            s.getQueueStatuses(q);
        }
    }

    @Test
    public void test_getQueueStatuses_empty() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory(100);

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            QueueStatus[] status = s.getQueueStatuses();

            assertNotNull(status);
            assertTrue(status.length == 3);
            assertEquals("single", status[0].getQueueName());
            assertEquals("multi", status[1].getQueueName());
            assertEquals("unlimited", status[2].getQueueName());
        }
    }

    @Test
    public void test_getQueueStatuses() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory(100);

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            QueueStatus[] status = s.getQueueStatuses(s.getQueueNames());

            assertNotNull(status);
            assertTrue(status.length == 3);
            assertEquals("single", status[0].getQueueName());
            assertEquals("multi", status[1].getQueueName());
            assertEquals("unlimited", status[2].getQueueName());
        }
    }

    @Test
    public void test_getQueueStatuses_withNull() throws Exception {

        MockFileSystem fs = new MockFileSystem("FID", "MockFS", "local://", new Path("/home/xenon"));

        MockInteractiveProcessFactory factory = new MockInteractiveProcessFactory(100);

        try (JobQueueScheduler s = new JobQueueScheduler("SID", "MockS", "location", new DefaultCredential(), factory, fs, new Path("/home/xenon"), 2, 100,
                10000L, null)) {

            QueueStatus[] status = s.getQueueStatuses(new String[] { "multi", null, "single" });

            assertNotNull(status);
            assertTrue(status.length == 3);
            assertEquals("multi", status[0].getQueueName());
            assertNull(status[1]);
            assertEquals("single", status[2].getQueueName());
        }
    }

}
