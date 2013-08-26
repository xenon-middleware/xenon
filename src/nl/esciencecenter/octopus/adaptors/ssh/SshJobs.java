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
package nl.esciencecenter.octopus.adaptors.ssh;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.OctopusPropertyDescription.Component;
import nl.esciencecenter.octopus.adaptors.local.LocalAdaptor;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.octopus.engine.util.JobQueues;
import nl.esciencecenter.octopus.exceptions.NoSuchSchedulerException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.UnsupportedJobDescriptionException;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.jobs.Streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshJobs implements Jobs {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshJobs.class);

    private static int currentID = 1;

    private static synchronized String getNewUniqueID() {
        String res = "ssh" + currentID;
        currentID++;
        return res;
    }

    /**
     * Used to store all state attached to a scheduler. This way, SchedulerImplementation is immutable.
     */
    static class SchedulerInfo {

        private final SshMultiplexedSession session;
        private final JobQueues jobQueues;

        SchedulerInfo(SshMultiplexedSession session, JobQueues jobQueues) {
            this.session = session;
            this.jobQueues = jobQueues;
        }

        SshMultiplexedSession getSession() {
            return session;
        }

        JobQueues getJobQueues() {
            return jobQueues;
        }

        void end() {
            jobQueues.end();
            session.disconnect();
        }
    }

    private final OctopusEngine octopusEngine;

    private final SshAdaptor adaptor;

    private final OctopusProperties properties;
    
    private long submittedJobs = 0;

    private Map<String, SchedulerInfo> schedulers = new HashMap<String, SchedulerInfo>();

    public SshJobs(OctopusProperties properties, SshAdaptor sshAdaptor, OctopusEngine octopusEngine) throws OctopusException {
        this.octopusEngine = octopusEngine;
        this.adaptor = sshAdaptor;
        this.properties = properties;
    }

    @Override
    public Scheduler newScheduler(URI location, Credential credential, Map<String, String> properties) throws OctopusException,
            OctopusIOException {

        adaptor.checkPath(location, "scheduler");

        String uniqueID = getNewUniqueID();

        LOGGER.debug("Starting ssh scheduler with properties {}", properties);

        OctopusProperties p = new OctopusProperties(adaptor.getSupportedProperties(Component.SCHEDULER), properties);

        SshMultiplexedSession session = adaptor.createNewSession(location, credential, p);

        SchedulerImplementation scheduler = new SchedulerImplementation(SshAdaptor.ADAPTOR_NAME, uniqueID, location,
                new String[] { "single", "multi", "unlimited" }, credential, p, true, true, true);

        SshInteractiveProcessFactory factory = new SshInteractiveProcessFactory(session);

        // Create a file system that uses the same SSH session as the scheduler.
        SshFiles files = (SshFiles) adaptor.filesAdaptor();
        FileSystem fs = files.newFileSystem(session, location, credential, this.properties);

        long pollingDelay = p.getLongProperty(SshAdaptor.POLLING_DELAY);
        int multiQThreads = p.getIntegerProperty(SshAdaptor.MULTIQ_MAX_CONCURRENT);

        JobQueues jobQueues = new JobQueues(SshAdaptor.ADAPTOR_NAME, octopusEngine.files(), scheduler, fs, factory,
                multiQThreads, pollingDelay);

        synchronized (this) {
            schedulers.put(uniqueID, new SchedulerInfo(session, jobQueues));
        }

        return scheduler;
    }

    @Override
    public Scheduler getLocalScheduler() throws OctopusException, OctopusIOException {
        throw new OctopusException(getClass().getName(), "getLocalScheduler not supported!");
    }

    private JobQueues getJobQueue(Scheduler scheduler) throws OctopusException {

        if (!(scheduler instanceof SchedulerImplementation)) {
            throw new NoSuchSchedulerException(SshAdaptor.ADAPTOR_NAME, "Illegal scheduler type.");
        }

        SchedulerImplementation s = (SchedulerImplementation) scheduler;

        SchedulerInfo info = schedulers.get(s.getUniqueID());

        if (info == null) {
            throw new NoSuchSchedulerException(SshAdaptor.ADAPTOR_NAME, "Cannot find scheduler: " + s.getUniqueID());
        }

        return info.getJobQueues();
    }

    @Override
    public Job[] getJobs(Scheduler scheduler, String... queueNames) throws OctopusException, OctopusIOException {
        return getJobQueue(scheduler).getJobs(queueNames);
    }

    private synchronized void addSubmittedJob() {
        submittedJobs++;
    }

    private synchronized long getSubmittedJobs() {
        return submittedJobs;
    }
    
    @Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException {

        if (description.getEnvironment().size() != 0) {
            throw new UnsupportedJobDescriptionException(SshAdaptor.ADAPTOR_NAME, "Environment variables not supported!");
        }

        Job job = getJobQueue(scheduler).submitJob(description);
        
        addSubmittedJob();
        
        return job;
    }

    @Override
    public JobStatus getJobStatus(Job job) throws OctopusException {
        return getJobQueue(job.getScheduler()).getJobStatus(job);
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) {
        JobStatus[] result = new JobStatus[jobs.length];

        for (int i = 0; i < jobs.length; i++) {
            try {
                if (jobs[i] != null) {
                    result[i] = getJobStatus(jobs[i]);
                } else {
                    result[i] = null;
                }
            } catch (OctopusException e) {
                result[i] = new JobStatusImplementation(jobs[i], null, null, e, false, false, null);
            }
        }

        return result;
    }

    @Override
    public JobStatus waitUntilDone(Job job, long timeout) throws OctopusException, OctopusIOException {
        return getJobQueue(job.getScheduler()).waitUntilDone(job, timeout);
    }

    @Override
    public JobStatus waitUntilRunning(Job job, long timeout) throws OctopusException, OctopusIOException {
        return getJobQueue(job.getScheduler()).waitUntilRunning(job, timeout);
    }

    @Override
    public JobStatus cancelJob(Job job) throws OctopusException {
        return getJobQueue(job.getScheduler()).cancelJob(job);
    }

    public void end() {
        
        for (SchedulerInfo tmp : schedulers.values()) { 
            tmp.end();
        }
        
        schedulers.clear();
    }

    @Override
    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws OctopusException {
        return getJobQueue(scheduler).getQueueStatus(scheduler, queueName);
    }

    @Override
    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws OctopusException {
        return getJobQueue(scheduler).getQueueStatuses(scheduler, queueNames);
    }

    @Override
    public void close(Scheduler scheduler) throws OctopusException, OctopusIOException {

        if (!(scheduler instanceof SchedulerImplementation)) {
            throw new OctopusException(SshAdaptor.ADAPTOR_NAME, "Illegal scheduler type.");
        }

        SchedulerImplementation s = (SchedulerImplementation) scheduler;

        SchedulerInfo info = null;

        synchronized (this) {
            info = schedulers.remove(s.getUniqueID());

            if (info == null) {
                throw new NoSuchSchedulerException(SshAdaptor.ADAPTOR_NAME, "Cannot find scheduler: " + s.getUniqueID());
            }
        }

        info.end();
    }

    @Override
    public boolean isOpen(Scheduler scheduler) throws OctopusException, OctopusIOException {

        if (!(scheduler instanceof SchedulerImplementation)) {
            throw new OctopusException(SshAdaptor.ADAPTOR_NAME, "Illegal scheduler type.");
        }

        return schedulers.containsKey(((SchedulerImplementation) scheduler).getUniqueID());
    }

    @Override
    public String getDefaultQueueName(Scheduler scheduler) throws OctopusException, OctopusIOException {
        return getJobQueue(scheduler).getDefaultQueueName(scheduler);
    }

    @Override
    public Streams getStreams(Job job) throws OctopusException {
        return getJobQueue(job.getScheduler()).getStreams(job);
    }

    /**
     * Add information about the ssh job adaptor to the map. 
     * 
     * @param result
     *          the map to add information to. 
     */
    public void getAdaptorSpecificInformation(Map<String, String> result) {
        result.put(SshAdaptor.SUBMITTED, Long.toString(getSubmittedJobs()));
    }
}
