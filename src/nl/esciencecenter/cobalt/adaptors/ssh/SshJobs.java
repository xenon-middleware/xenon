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
package nl.esciencecenter.cobalt.adaptors.ssh;

import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.cobalt.CobaltException;
import nl.esciencecenter.cobalt.CobaltPropertyDescription.Component;
import nl.esciencecenter.cobalt.credentials.Credential;
import nl.esciencecenter.cobalt.engine.CobaltEngine;
import nl.esciencecenter.cobalt.engine.CobaltProperties;
import nl.esciencecenter.cobalt.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.cobalt.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.cobalt.engine.util.JobQueues;
import nl.esciencecenter.cobalt.files.FileSystem;
import nl.esciencecenter.cobalt.jobs.Job;
import nl.esciencecenter.cobalt.jobs.JobDescription;
import nl.esciencecenter.cobalt.jobs.JobStatus;
import nl.esciencecenter.cobalt.jobs.Jobs;
import nl.esciencecenter.cobalt.jobs.NoSuchSchedulerException;
import nl.esciencecenter.cobalt.jobs.QueueStatus;
import nl.esciencecenter.cobalt.jobs.Scheduler;
import nl.esciencecenter.cobalt.jobs.Streams;
import nl.esciencecenter.cobalt.jobs.UnsupportedJobDescriptionException;

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

    private final CobaltEngine octopusEngine;

    private final SshAdaptor adaptor;

    private final CobaltProperties properties;
    
    private long submittedJobs = 0;

    private Map<String, SchedulerInfo> schedulers = new HashMap<String, SchedulerInfo>();

    public SshJobs(CobaltProperties properties, SshAdaptor sshAdaptor, CobaltEngine octopusEngine) throws CobaltException {
        this.octopusEngine = octopusEngine;
        this.adaptor = sshAdaptor;
        this.properties = properties;
    }

    @Override
    public Scheduler newScheduler(String scheme, String location, Credential credential, Map<String, String> properties) 
            throws CobaltException {

        SshLocation sshLocation = SshLocation.parse(location);
        
        String uniqueID = getNewUniqueID();

        LOGGER.debug("Starting ssh scheduler with properties {}", properties);

        CobaltProperties p = new CobaltProperties(adaptor.getSupportedProperties(Component.SCHEDULER), properties);

        SshMultiplexedSession session = adaptor.createNewSession(sshLocation, credential, p);

        SchedulerImplementation scheduler = new SchedulerImplementation(SshAdaptor.ADAPTOR_NAME, uniqueID, scheme, location,
                new String[] { "single", "multi", "unlimited" }, credential, p, true, true, true);

        SshInteractiveProcessFactory factory = new SshInteractiveProcessFactory(session);

        // Create a file system that uses the same SSH session as the scheduler.
        SshFiles files = (SshFiles) adaptor.filesAdaptor();
        FileSystem fs = files.newFileSystem(session, "sftp", location, credential, this.properties);

        long pollingDelay = p.getLongProperty(SshAdaptor.POLLING_DELAY);
        int multiQThreads = p.getIntegerProperty(SshAdaptor.MULTIQ_MAX_CONCURRENT);

        JobQueues jobQueues = new JobQueues(SshAdaptor.ADAPTOR_NAME, octopusEngine.files(), scheduler, fs.getEntryPath(), factory,
                multiQThreads, pollingDelay);

        synchronized (this) {
            schedulers.put(uniqueID, new SchedulerInfo(session, jobQueues));
        }

        return scheduler;
    }

    private JobQueues getJobQueue(Scheduler scheduler) throws CobaltException {

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
    public Job[] getJobs(Scheduler scheduler, String... queueNames) throws CobaltException {
        return getJobQueue(scheduler).getJobs(queueNames);
    }

    private synchronized void addSubmittedJob() {
        submittedJobs++;
    }

    private synchronized long getSubmittedJobs() {
        return submittedJobs;
    }
    
    @Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws CobaltException {

        if (description.getEnvironment().size() != 0) {
            throw new UnsupportedJobDescriptionException(SshAdaptor.ADAPTOR_NAME, "Environment variables not supported!");
        }

        Job job = getJobQueue(scheduler).submitJob(description);
        
        addSubmittedJob();
        
        return job;
    }

    @Override
    public JobStatus getJobStatus(Job job) throws CobaltException {
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
            } catch (CobaltException e) {
                result[i] = new JobStatusImplementation(jobs[i], null, null, e, false, false, null);
            }
        }

        return result;
    }

    @Override
    public JobStatus waitUntilDone(Job job, long timeout) throws CobaltException {
        return getJobQueue(job.getScheduler()).waitUntilDone(job, timeout);
    }

    @Override
    public JobStatus waitUntilRunning(Job job, long timeout) throws CobaltException {
        return getJobQueue(job.getScheduler()).waitUntilRunning(job, timeout);
    }

    @Override
    public JobStatus cancelJob(Job job) throws CobaltException {
        return getJobQueue(job.getScheduler()).cancelJob(job);
    }

    public void end() {
        
        for (SchedulerInfo tmp : schedulers.values()) { 
            tmp.end();
        }
        
        schedulers.clear();
    }

    @Override
    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws CobaltException {
        return getJobQueue(scheduler).getQueueStatus(scheduler, queueName);
    }

    @Override
    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws CobaltException {
        return getJobQueue(scheduler).getQueueStatuses(scheduler, queueNames);
    }

    @Override
    public void close(Scheduler scheduler) throws CobaltException {

        if (!(scheduler instanceof SchedulerImplementation)) {
            throw new CobaltException(SshAdaptor.ADAPTOR_NAME, "Illegal scheduler type.");
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
    public boolean isOpen(Scheduler scheduler) throws CobaltException {

        if (!(scheduler instanceof SchedulerImplementation)) {
            throw new CobaltException(SshAdaptor.ADAPTOR_NAME, "Illegal scheduler type.");
        }

        return schedulers.containsKey(((SchedulerImplementation) scheduler).getUniqueID());
    }

    @Override
    public String getDefaultQueueName(Scheduler scheduler) throws CobaltException {
        return getJobQueue(scheduler).getDefaultQueueName(scheduler);
    }

    @Override
    public Streams getStreams(Job job) throws CobaltException {
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
