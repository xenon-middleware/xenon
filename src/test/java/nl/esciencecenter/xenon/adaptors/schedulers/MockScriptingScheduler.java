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

import java.util.HashMap;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;
import nl.esciencecenter.xenon.schedulers.QueueStatus;
import nl.esciencecenter.xenon.schedulers.Streams;

public class MockScriptingScheduler extends ScriptingScheduler {

    /** Local properties start with this prefix. */
    public static final String PREFIX = SchedulerAdaptor.ADAPTORS_PREFIX + "local.";

    /** Local queue properties start with this prefix. */
    public static final String QUEUE = PREFIX + "queue.";

    /** Property for maximum history length for finished jobs */
    public static final String POLLING_DELAY = QUEUE + "pollingDelay";

    /** Local multi queue properties start with this prefix. */
    public static final String MULTIQ = QUEUE + "multi.";

    /** Property for the maximum number of concurrent jobs in the multi queue. */
    public static final String MULTIQ_MAX_CONCURRENT = MULTIQ + "maxConcurrentJobs";

    /** The properties supported by this adaptor */
    private static final XenonPropertyDescription[] VALID_PROPERTIES = new XenonPropertyDescription[] {
            new XenonPropertyDescription(POLLING_DELAY, Type.LONG, "1000", "The polling delay for monitoring running jobs (in milliseconds)."),
            new XenonPropertyDescription(MULTIQ_MAX_CONCURRENT, Type.INTEGER, "4", "The maximum number of concurrent jobs in the multiq.") };

    MockScriptingScheduler() throws XenonException {
        super("test1", "TEST", "", null, new HashMap<String, String>(), VALID_PROPERTIES, "xenon.adaptors.schedulers.local.queue.pollingDelay");
    }

    @Override
    public String[] getQueueNames() throws XenonException {
        return new String[] { "queue" };
    }

    @Override
    public boolean isOpen() throws XenonException {
        return true;
    }

    @Override
    public String getDefaultQueueName() throws XenonException {
        return "queue";
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

        return null;
    }

    @Override
    public JobStatus cancelJob(String jobIdentifier) throws XenonException {

        return null;
    }

}
