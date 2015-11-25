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
package nl.esciencecenter.xenon.adaptors.local;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.UnknownPropertyException;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.jobs.JobImplementation;
import nl.esciencecenter.xenon.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.xenon.engine.util.InteractiveProcess;
import nl.esciencecenter.xenon.engine.util.InteractiveProcessFactory;
import nl.esciencecenter.xenon.engine.util.JobQueues;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.Jobs;
import nl.esciencecenter.xenon.jobs.QueueStatus;
import nl.esciencecenter.xenon.jobs.Scheduler;
import nl.esciencecenter.xenon.jobs.Streams;

/**
 * LocalFiles implements an Xenon <code>Jobs</code> adaptor for local job operations.
 * 
 * @see nl.esciencecenter.xenon.jobs.Jobs
 * 
 * @version 1.0
 * @since 1.0
 */
public class LocalJobs implements Jobs, InteractiveProcessFactory {
    
    /** The parent adaptor */
    private final LocalAdaptor localAdaptor;

    private final Scheduler localScheduler;
    private final JobQueues jobQueues;

    public LocalJobs(LocalAdaptor localAdaptor, XenonProperties properties, Path cwd, XenonEngine engine)
            throws XenonException {
        
        this.localAdaptor = localAdaptor;

        localScheduler = new SchedulerImplementation(LocalAdaptor.ADAPTOR_NAME, "LocalScheduler", "local", "/", 
                new String[] { "single", "multi", "unlimited" }, null, properties, true, true, true);

        int processors = Runtime.getRuntime().availableProcessors();
        int multiQThreads = properties.getIntegerProperty(LocalAdaptor.MULTIQ_MAX_CONCURRENT, processors);
        int pollingDelay = properties.getIntegerProperty(LocalAdaptor.POLLING_DELAY);

        jobQueues = new JobQueues(LocalAdaptor.ADAPTOR_NAME, engine.files(), localScheduler, cwd, this, multiQThreads,
                pollingDelay);
    }

    @Override
    public InteractiveProcess createInteractiveProcess(JobImplementation job) throws XenonException {
        return new LocalInteractiveProcess(job);
    }

    @Override
    public Scheduler newScheduler(String scheme, String location, Credential credential, Map<String, String> properties) 
            throws XenonException {

        if (!(location == null || location.isEmpty() || location.equals("/"))) {
            throw new InvalidLocationException(LocalAdaptor.ADAPTOR_NAME, "Cannot create local scheduler with location: " 
                    + location);
        }
        
        localAdaptor.checkCredential(credential);

        if (properties != null && properties.size() > 0) {
            throw new UnknownPropertyException(LocalAdaptor.ADAPTOR_NAME, "Cannot create local scheduler with additional properties!");
        }

        return localScheduler;
    }

    @Override
    public Job[] getJobs(Scheduler scheduler, String... queueNames) throws XenonException {
        return jobQueues.getJobs(queueNames);
    }

    @Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws XenonException {
        return jobQueues.submitJob(description);
    }

    @Override
    public JobStatus getJobStatus(Job job) throws XenonException {
        return jobQueues.getJobStatus(job);
    }

    @Override
    public JobStatus waitUntilDone(Job job, long timeout) throws XenonException {
        return jobQueues.waitUntilDone(job, timeout);
    }

    @Override
    public JobStatus waitUntilRunning(Job job, long timeout) throws XenonException {
        return jobQueues.waitUntilRunning(job, timeout);
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) {
        return jobQueues.getJobStatuses(jobs);
    }

    @Override
    public JobStatus cancelJob(Job job) throws XenonException {
        return jobQueues.cancelJob(job);
    }

    public void end() {
        jobQueues.end();
    }

    @Override
    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws XenonException {
        return jobQueues.getQueueStatus(scheduler, queueName);
    }

    @Override
    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws XenonException {
        return jobQueues.getQueueStatuses(scheduler, queueNames);
    }

    @Override
    public void close(Scheduler scheduler) throws XenonException {
        // ignored
    }

    @Override
    public boolean isOpen(Scheduler scheduler) throws XenonException {
        return true;
    }

    @Override
    public String getDefaultQueueName(Scheduler scheduler) throws XenonException {
        return jobQueues.getDefaultQueueName(scheduler);
    }

    @Override
    public Streams getStreams(Job job) throws XenonException {
        return jobQueues.getStreams(job);
    }

    /**
     * Add information about the local job adaptor to the map. 
     * 
     * @param result
     *          the map to add information to. 
     */
    public void getAdaptorSpecificInformation(Map<String, String> result) {
        result.put(LocalAdaptor.SUBMITTED, Long.toString(jobQueues.getCurrentJobID()));
    }
}