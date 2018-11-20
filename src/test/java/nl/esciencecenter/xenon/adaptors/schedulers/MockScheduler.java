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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;
import nl.esciencecenter.xenon.schedulers.NoSuchJobException;
import nl.esciencecenter.xenon.schedulers.QueueStatus;
import nl.esciencecenter.xenon.schedulers.Scheduler;
import nl.esciencecenter.xenon.schedulers.Streams;

public class MockScheduler extends Scheduler {

    private HashSet<String> jobs = new HashSet<>();

    private int nextJob = 0;

    private boolean shouldFail;
    private String toStderr;
    private int exitCode;

    protected MockScheduler(String uniqueID, String adaptor, String location, Credential credential, XenonProperties properties) {
        super(uniqueID, adaptor, location, credential, properties);
    }

    protected MockScheduler(boolean shouldFail, String toStderr, int exitCode) {
        super("TEST0", "TEST", "location", new DefaultCredential(), null);
        this.shouldFail = shouldFail;
        this.toStderr = toStderr;
        this.exitCode = exitCode;
    }

    @Override
    public String[] getQueueNames() throws XenonException {
        return new String[] { "unlimited" };
    }

    @Override
    public void close() throws XenonException {
    }

    @Override
    public boolean isOpen() throws XenonException {
        return true;
    }

    @Override
    public String getDefaultQueueName() throws XenonException {
        return "unlimited";
    }

    @Override
    public int getDefaultRuntime() {
        return 0;
    }

    @Override
    public String[] getJobs(String... queueNames) throws XenonException {
        return jobs.toArray(new String[jobs.size()]);
    }

    @Override
    public QueueStatus getQueueStatus(String queueName) throws XenonException {
        return null;
    }

    @Override
    public QueueStatus[] getQueueStatuses(String... queueNames) throws XenonException {
        return null;
    }

    @Override
    public String submitBatchJob(JobDescription description) throws XenonException {
        String id = "ID-" + nextJob++;
        jobs.add(id);
        return id;
    }

    @Override
    public Streams submitInteractiveJob(JobDescription description) throws XenonException {

        String id = "ID-" + nextJob++;
        jobs.add(id);

        ByteArrayInputStream stdout = new ByteArrayInputStream("Hello stdout".getBytes());
        ByteArrayInputStream stderr;

        if (toStderr != null) {
            stderr = new ByteArrayInputStream("Hello stderr".getBytes());
        } else {
            stderr = new ByteArrayInputStream(new byte[0]);
        }

        ByteArrayOutputStream stdin = new ByteArrayOutputStream();

        return new StreamsImplementation(id, stdout, stdin, stderr);
    }

    @Override
    public JobStatus getJobStatus(String jobIdentifier) throws XenonException {

        if (jobs.contains(jobIdentifier)) {
            return new JobStatusImplementation(jobIdentifier, "name", "RUNNING", -1, null, true, false, null);
        }

        throw new NoSuchJobException("TEST", "No such job " + jobIdentifier);
    }

    @Override
    public JobStatus cancelJob(String jobIdentifier) throws XenonException {

        if (jobs.contains(jobIdentifier)) {
            jobs.remove(jobIdentifier);
            return new JobStatusImplementation(jobIdentifier, "name", "CANCELLED", 1, new JobCanceledException("TEST", "Cancelled"), false, true, null);
        }

        throw new NoSuchJobException("TEST", "No such job " + jobIdentifier);
    }

    @Override
    public JobStatus waitUntilDone(String jobIdentifier, long timeout) throws XenonException {

        if (jobs.contains(jobIdentifier)) {
            jobs.remove(jobIdentifier);

            if (shouldFail) {
                return new JobStatusImplementation(jobIdentifier, "name", "ERROR", exitCode, new XenonException(getAdaptorName(), "FAIL"), false, true, null);
            } else {
                return new JobStatusImplementation(jobIdentifier, "name", "DONE", exitCode, null, false, true, null);
            }
        }

        throw new NoSuchJobException("TEST", "No such job " + jobIdentifier);
    }

    @Override
    public JobStatus waitUntilRunning(String jobIdentifier, long timeout) throws XenonException {

        if (jobs.contains(jobIdentifier)) {
            return new JobStatusImplementation(jobIdentifier, "name", "RUNNING", -1, null, true, false, null);
        }

        throw new NoSuchJobException("TEST", "No such job " + jobIdentifier);
    }

    public boolean usesFileSystem() {
        return false;
    }

    public FileSystem getFileSystem() throws XenonException {
        throw new XenonException("TEST", "No FileSystem used");
    }

}
