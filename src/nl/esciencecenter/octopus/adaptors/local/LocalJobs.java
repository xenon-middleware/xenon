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
package nl.esciencecenter.octopus.adaptors.local;

import java.io.IOException;
import java.util.Map;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.octopus.engine.util.InteractiveProcess;
import nl.esciencecenter.octopus.engine.util.InteractiveProcessFactory;
import nl.esciencecenter.octopus.engine.util.JobQueues;
import nl.esciencecenter.octopus.exceptions.InvalidLocationException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.jobs.Streams;

/**
 * LocalFiles implements an Octopus <code>Jobs</code> adaptor for local job operations.
 * 
 * @see nl.esciencecenter.octopus.jobs.Jobs
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class LocalJobs implements Jobs, InteractiveProcessFactory {

    private final Scheduler localScheduler;
    private final JobQueues jobQueues;

    public LocalJobs(OctopusProperties properties, Path cwd, OctopusEngine engine)
            throws OctopusException {

        localScheduler = new SchedulerImplementation(LocalAdaptor.ADAPTOR_NAME, "LocalScheduler", "local", "/", 
                new String[] { "single", "multi", "unlimited" }, null, properties, true, true, true);

        int processors = Runtime.getRuntime().availableProcessors();
        int multiQThreads = properties.getIntegerProperty(LocalAdaptor.MULTIQ_MAX_CONCURRENT, processors);
        int pollingDelay = properties.getIntegerProperty(LocalAdaptor.POLLING_DELAY);

        jobQueues = new JobQueues(LocalAdaptor.ADAPTOR_NAME, engine.files(), localScheduler, cwd, this, multiQThreads,
                pollingDelay);
    }

    @Override
    public InteractiveProcess createInteractiveProcess(JobImplementation job) throws IOException {
        return new LocalInteractiveProcess(job);
    }

    @Override
    public Scheduler newScheduler(String scheme, String location, Credential credential, Map<String, String> properties) 
            throws OctopusException, OctopusIOException {

        if (!(location == null || location.isEmpty() || location.equals("/"))) {
            throw new InvalidLocationException(LocalAdaptor.ADAPTOR_NAME, "Cannot create local scheduler with location: " 
                    + location);
        }

        if (credential != null) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Cannot create local scheduler with credentials!");
        }

        if (properties != null && properties.size() > 0) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Cannot create local scheduler with additional properties!");
        }

        return localScheduler;
    }

    @Override
    public Job[] getJobs(Scheduler scheduler, String... queueNames) throws OctopusException, OctopusIOException {
        return jobQueues.getJobs(queueNames);
    }

    @Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException {
        return jobQueues.submitJob(description);
    }

    @Override
    public JobStatus getJobStatus(Job job) throws OctopusException {
        return jobQueues.getJobStatus(job);
    }

    @Override
    public JobStatus waitUntilDone(Job job, long timeout) throws OctopusException, OctopusIOException {
        return jobQueues.waitUntilDone(job, timeout);
    }

    @Override
    public JobStatus waitUntilRunning(Job job, long timeout) throws OctopusException, OctopusIOException {
        return jobQueues.waitUntilRunning(job, timeout);
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) {
        return jobQueues.getJobStatuses(jobs);
    }

    @Override
    public JobStatus cancelJob(Job job) throws OctopusException {
        return jobQueues.cancelJob(job);
    }

    public void end() {
        jobQueues.end();
    }

    @Override
    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws OctopusException {
        return jobQueues.getQueueStatus(scheduler, queueName);
    }

    @Override
    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws OctopusException {
        return jobQueues.getQueueStatuses(scheduler, queueNames);
    }

    @Override
    public void close(Scheduler scheduler) throws OctopusException, OctopusIOException {
        // ignored
    }

    @Override
    public boolean isOpen(Scheduler scheduler) throws OctopusException, OctopusIOException {
        return true;
    }

    @Override
    public String getDefaultQueueName(Scheduler scheduler) throws OctopusException, OctopusIOException {
        return jobQueues.getDefaultQueueName(scheduler);
    }

    @Override
    public Streams getStreams(Job job) throws OctopusException {
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