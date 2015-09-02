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
package nl.esciencecenter.xenon.adaptors.ssh;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.xenon.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.xenon.engine.util.JobQueues;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.Jobs;
import nl.esciencecenter.xenon.jobs.NoSuchSchedulerException;
import nl.esciencecenter.xenon.jobs.QueueStatus;
import nl.esciencecenter.xenon.jobs.Scheduler;
import nl.esciencecenter.xenon.jobs.Streams;
import nl.esciencecenter.xenon.jobs.UnsupportedJobDescriptionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class SshJobs implements Jobs {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshJobs.class);

    private static final AtomicInteger currentID = new AtomicInteger(1);

    private static String getNewUniqueID() {
        return "ssh" + currentID.getAndIncrement();
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

    private final XenonEngine xenonEngine;

    private final SshAdaptor adaptor;

    private final XenonProperties properties;
    
    private long submittedJobs = 0;

    private boolean isEnded;

    private final Map<String, SchedulerInfo> schedulers;

    public SshJobs(XenonProperties properties, SshAdaptor sshAdaptor, XenonEngine xenonEngine) throws XenonException {
        this.xenonEngine = xenonEngine;
        this.adaptor = sshAdaptor;
        this.properties = properties;
        this.schedulers = new HashMap<>();
        this.isEnded = false;
    }

    @Override
    public Scheduler newScheduler(String scheme, String location, Credential credential, Map<String, String> properties) 
            throws XenonException {

        SshLocation sshLocation = SshLocation.parse(location, adaptor.getSshConfig());
        
        String uniqueID = getNewUniqueID();

        LOGGER.debug("Starting ssh scheduler with properties {}", properties);

        XenonProperties p = new XenonProperties(adaptor.getSupportedProperties(Component.SCHEDULER), properties);

        SshMultiplexedSession session = adaptor.createNewSession(sshLocation, credential, p);

        SchedulerImplementation scheduler = new SchedulerImplementation(SshAdaptor.ADAPTOR_NAME, uniqueID, scheme, location,
                new String[] { "single", "multi", "unlimited" }, credential, p, true, true, true);

        SshInteractiveProcessFactory factory = new SshInteractiveProcessFactory(session);

        // Create a file system that uses the same SSH session as the scheduler.
        SshFiles files = (SshFiles) adaptor.filesAdaptor();
        FileSystem fs = files.newFileSystem(session, "sftp", location, credential, this.properties);

        long pollingDelay = p.getLongProperty(SshAdaptor.POLLING_DELAY);
        int multiQThreads = p.getIntegerProperty(SshAdaptor.MULTIQ_MAX_CONCURRENT);

        JobQueues jobQueues = new JobQueues(SshAdaptor.ADAPTOR_NAME, xenonEngine.files(), scheduler, fs.getEntryPath(), factory,
                multiQThreads, pollingDelay);

        synchronized (this) {
            schedulers.put(uniqueID, new SchedulerInfo(session, jobQueues));
        }

        return scheduler;
    }

    private JobQueues getJobQueue(Scheduler scheduler) throws XenonException {

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
    public Job[] getJobs(Scheduler scheduler, String... queueNames) throws XenonException {
        return getJobQueue(scheduler).getJobs(queueNames);
    }

    private synchronized void addSubmittedJob() {
        submittedJobs++;
    }

    private synchronized long getSubmittedJobs() {
        return submittedJobs;
    }
    
    @Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws XenonException {

        if (!description.getEnvironment().isEmpty()) {
            throw new UnsupportedJobDescriptionException(SshAdaptor.ADAPTOR_NAME, "Environment variables not supported!");
        }

        Job job = getJobQueue(scheduler).submitJob(description);
        
        addSubmittedJob();
        
        return job;
    }

    @Override
    public JobStatus getJobStatus(Job job) throws XenonException {
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
            } catch (XenonException e) {
                result[i] = new JobStatusImplementation(jobs[i], null, null, e, false, false, null);
            }
        }

        return result;
    }

    @Override
    public JobStatus waitUntilDone(Job job, long timeout) throws XenonException {
        return getJobQueue(job.getScheduler()).waitUntilDone(job, timeout);
    }

    @Override
    public JobStatus waitUntilRunning(Job job, long timeout) throws XenonException {
        return getJobQueue(job.getScheduler()).waitUntilRunning(job, timeout);
    }

    @Override
    public JobStatus cancelJob(Job job) throws XenonException {
        return getJobQueue(job.getScheduler()).cancelJob(job);
    }

    public void end() {
        isEnded = true;

        for (SchedulerInfo tmp : schedulers.values()) { 
            tmp.end();
        }
        
        schedulers.clear();
    }

    @Override
    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws XenonException {
        return getJobQueue(scheduler).getQueueStatus(scheduler, queueName);
    }

    @Override
    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws XenonException {
        return getJobQueue(scheduler).getQueueStatuses(scheduler, queueNames);
    }

    @Override
    public void close(Scheduler scheduler) throws XenonException {

        if (!(scheduler instanceof SchedulerImplementation)) {
            throw new XenonException(SshAdaptor.ADAPTOR_NAME, "Illegal scheduler type.");
        }

        SchedulerImplementation s = (SchedulerImplementation) scheduler;

        SchedulerInfo info;
        synchronized (this) {
            info = schedulers.remove(s.getUniqueID());
        }

        if (info == null) {
            if (!isEnded) {
                throw new NoSuchSchedulerException(SshAdaptor.ADAPTOR_NAME, "Cannot find scheduler: " + s.getUniqueID());
            }
        } else {
            info.end();
        }
    }

    @Override
    public boolean isOpen(Scheduler scheduler) throws XenonException {

        if (!(scheduler instanceof SchedulerImplementation)) {
            throw new XenonException(SshAdaptor.ADAPTOR_NAME, "Illegal scheduler type.");
        }

        return schedulers.containsKey(((SchedulerImplementation) scheduler).getUniqueID());
    }

    @Override
    public String getDefaultQueueName(Scheduler scheduler) throws XenonException {
        return getJobQueue(scheduler).getDefaultQueueName(scheduler);
    }

    @Override
    public Streams getStreams(Job job) throws XenonException {
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
