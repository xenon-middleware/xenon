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
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;

public class MockDefaultScheduler extends Scheduler {

    public MockDefaultScheduler(String uniqueID, String adaptor, String location, Credential credential, XenonProperties properties) {
        super(uniqueID, adaptor, location, credential, properties);
    }

    public MockDefaultScheduler(String uniqueID, String adaptor, String location, XenonProperties properties) {
        super(uniqueID, adaptor, location, new DefaultCredential(), properties);
    }

    @Override
    public String[] getQueueNames() throws XenonException {

        return null;
    }

    @Override
    public void close() throws XenonException {

    }

    @Override
    public boolean isOpen() throws XenonException {

        return false;
    }

    @Override
    public String getDefaultQueueName() throws XenonException {

        return null;
    }

    @Override
    public int getDefaultRuntime() {
        return -1;
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
    public JobStatus getJobStatus(String job) throws XenonException {

        return null;
    }

    @Override
    public JobStatus[] getJobStatuses(String... jobs) throws XenonException {

        return null;
    }

    @Override
    public JobStatus cancelJob(String job) throws XenonException {

        return null;
    }

    @Override
    public JobStatus waitUntilDone(String job, long timeout) throws XenonException {

        return null;
    }

    @Override
    public JobStatus waitUntilRunning(String job, long timeout) throws XenonException {

        return null;
    }

    public boolean usesFileSystem() {
        return false;
    }

    public FileSystem getFileSystem() throws XenonException {
        throw new XenonException("TEST", "No FileSystem used");
    }
}
