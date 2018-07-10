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
package nl.esciencecenter.xenon.schedulers;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.schedulers.JobStatusImplementation;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;

public class MockScheduler extends Scheduler {

    private boolean open = true;

    public MockScheduler() {
        this("ID", "ADAPTOR", "LOC", new DefaultCredential(), null);
    }

    public MockScheduler(String uniqueID, String adaptor, String location, Credential credential, XenonProperties properties) {
        super(uniqueID, adaptor, location, credential, properties);
    }

    @Override
    public String[] getQueueNames() throws XenonException {
        return null;
    }

    @Override
    public String getDefaultQueueName() throws XenonException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getJobs(String... queueNames) throws XenonException {
        return null;
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
        return null;
    }

    @Override
    public Streams submitInteractiveJob(JobDescription description) throws XenonException {
        return null;
    }

    @Override
    public JobStatus getJobStatus(String jobIdentifier) throws XenonException {

        if (jobIdentifier.equals("UNKNOWN")) {
            throw new NoSuchJobException("test", "unknown");
        }

        if (jobIdentifier.equals("ERROR")) {
            throw new XenonException("test", "error");
        }

        if (jobIdentifier.equals("RUNNING")) {
            return new JobStatusImplementation(jobIdentifier, "test", "running", -1, null, true, false, null);
        }

        if (jobIdentifier.equals("DONE")) {
            return new JobStatusImplementation(jobIdentifier, "test", "done", 0, null, false, true, null);
        }

        if (jobIdentifier.equals("CRASH")) {
            return new JobStatusImplementation(jobIdentifier, "test", "crash", 42, null, false, true, null);
        }

        if (jobIdentifier.equals("EXCEPTION")) {
            return new JobStatusImplementation(jobIdentifier, "test", "fail", -1, new XenonException("test", "test"), false, true, null);
        }

        return new JobStatusImplementation(jobIdentifier, "test", "done", 0, null, false, true, null);
    }

    @Override
    public JobStatus cancelJob(String jobIdentifier) throws XenonException {
        return null;
    }

    @Override
    public JobStatus waitUntilDone(String jobIdentifier, long timeout) throws XenonException {
        return null;
    }

    @Override
    public JobStatus waitUntilRunning(String jobIdentifier, long timeout) throws XenonException {
        return null;
    }

    @Override
    public FileSystem getFileSystem() throws XenonException {
        throw new XenonException("TEST", "No FileSystem used");
    }

    @Override
    public void close() throws XenonException {
        this.open = false;
    }

    @Override
    public boolean isOpen() throws XenonException {
        return open;
    }
}
