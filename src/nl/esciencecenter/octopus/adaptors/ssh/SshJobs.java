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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.QueueStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.octopus.engine.util.JobQueues;
import nl.esciencecenter.octopus.exceptions.BadParameterException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.OctopusRuntimeException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.jobs.Streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Session;

public class SshJobs implements Jobs {

    private static final Logger logger = LoggerFactory.getLogger(SshJobs.class);

    private static int currentID = 1;

    private static synchronized String getNewUniqueID() {
        String res = "ssh" + currentID;
        currentID++;
        return res;
    }

    /**
     * Used to store all state attached to a scheduler. This way, SchedulerImplementation is immutable.
     * 
     */
    class SchedulerInfo {
        
        private final SchedulerImplementation impl;
        private final Session session;
        private final JobQueues jobQueues; 
        
        SchedulerInfo(SchedulerImplementation impl, Session session, JobQueues jobQueues) {
            this.impl = impl;
            this.session = session;
            this.jobQueues = jobQueues;
        }
    }

    
    @SuppressWarnings("unused")
    private final OctopusEngine octopusEngine;

    private final SshAdaptor adaptor;

    private final OctopusProperties properties;

//    private final LinkedList<SshJobExecutor> singleQ;

//    private final ExecutorService singleExecutor;
    
    private final int maxQSize;
    private final int pollingDelay;
    private final int multiQThreads;
    
//    private final JobQueues jobQueues; 
    
    private HashMap<String, SchedulerInfo> schedulers = new HashMap<String, SchedulerInfo>();
    
    private static int jobID = 0;

    private static synchronized int getNextJobID() {
        return jobID++;
    }

    public SshJobs(OctopusProperties properties, SshAdaptor sshAdaptor, OctopusEngine octopusEngine) throws OctopusException {
        this.octopusEngine = octopusEngine;
        this.adaptor = sshAdaptor;
        this.properties = properties;
        
//        singleQ = new LinkedList<SshJobExecutor>();
//        singleExecutor = Executors.newSingleThreadExecutor();

        multiQThreads = properties.getIntProperty(SshAdaptor.MULTIQ_MAX_CONCURRENT, 1);
        
        maxQSize = properties.getIntProperty(SshAdaptor.MAX_HISTORY);
        
        pollingDelay = properties.getIntProperty(SshAdaptor.POLLING_DELAY);
        
        if (maxQSize < 0 && maxQSize != -1) {
            throw new BadParameterException(adaptor.getName(), "Maximum queue size cannot be negative (excluding -1 for unlimited)");
        }

        if (multiQThreads <= 1) {
            throw new BadParameterException(adaptor.getName(), "Number of slots for the multi queue cannot be smaller than one!");
        }
        
        if (pollingDelay < 100 ||  pollingDelay > 60000) {
            throw new BadParameterException(adaptor.getName(), "Polling delay must be between 100 and 60000!");
        }
    }

    @Override
    public Scheduler newScheduler(URI location, Credential credential, Properties properties) throws OctopusException,
            OctopusIOException {

        adaptor.checkURI(location);

        String path = location.getPath();

        if (path != null && !path.equals("") && !path.equals("/")) {
            throw new OctopusException(adaptor.getName(), "Cannot create ssh scheduler with path! (path = " + path + ")");
        }

        if (properties != null && properties.size() > 0) { // TODO why not?
            throw new OctopusException(adaptor.getName(), "Cannot create ssh scheduler with additional properties!");
        }

        String uniqueID = getNewUniqueID();

        Session session = adaptor.createNewSession(uniqueID, location, credential, this.properties);

        SchedulerImplementation scheduler = new SchedulerImplementation(adaptor.getName(), uniqueID, location, 
                new String[] { "single", "multi", "unlimited" }, credential, 
                new OctopusProperties(properties), true, true, true);

        SSHProcessWrapperFactory factory = new SSHProcessWrapperFactory(session);
        
        // TODO: use the properties to change the JobQueue settings!
        JobQueues jobQueues = new JobQueues(adaptor, scheduler, factory, multiQThreads, maxQSize, pollingDelay);
        
        synchronized (this) {
            schedulers.put(uniqueID, new SchedulerInfo(scheduler, session, jobQueues));
        }
        
        return scheduler;
    }

    @Override
    public Scheduler getLocalScheduler() throws OctopusException, OctopusIOException {
        throw new OctopusException(getClass().getName(), "getLocalScheduler not supported!");
    }
        
    private JobQueues getJobQueue(Scheduler scheduler) throws OctopusException { 
        
        if (!(scheduler instanceof SchedulerImplementation)) {
            throw new OctopusException(adaptor.getName(), "Illegal scheduler type.");
        }

        SchedulerImplementation s = (SchedulerImplementation) scheduler;
        
        SchedulerInfo info = schedulers.get(s.getUniqueID());

        if (info == null) {
            throw new OctopusException(adaptor.getName(), "Cannot find scheduler: " + s.getUniqueID());
        }

        return info.jobQueues;
    }

//    
//    
//    private void getJobs(LinkedList<SshJobExecutor> list, LinkedList<Job> out) {
//        
//        if (list == null) { 
//            return;
//        }
//        
//        for (SshJobExecutor e : list) {
//            out.add(e.getJob());
//        }
//    }

    @Override
    public Job[] getJobs(Scheduler scheduler, String... queueNames) throws OctopusException, OctopusIOException {
        return getJobQueue(scheduler).getJobs(queueNames);
//        
//        
//        LinkedList<Job> out = new LinkedList<Job>();
//        
//        if (queueNames == null) {
//            getJobs(singleQ, out);
//        } else {              
//            for (String name : queueNames) { 
//                if (name.equals("single")) { 
//                    getJobs(singleQ, out);
//                } else { 
//                    throw new BadParameterException(adaptor.getName(), "Queue \"" + name + "\" does not exist");                    
//                }
//            }
//        }
//        
//        return out.toArray(new Job[out.size()]);
    }

    @Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException {
        return getJobQueue(scheduler).submitJob(scheduler, description);
    }
        
//        
//        
//        
//        JobImplementation result = new JobImplementation(description, scheduler, OctopusEngine.getNextUUID(), 
//                "sshjob-" + getNextJobID(), description.isInteractive(), true /* online */);
//     
//        SshJobExecutor executor = new SshJobExecutor(adaptor, (SchedulerImplementation) scheduler, info.getSession(), result, 
//                pollingDelay);
//
//        String queueName = description.getQueueName();
//
//        if (queueName == null || queueName.equals("single")) {
//            singleQ.add(executor);
//            singleExecutor.execute(executor);
//        } else {
//            throw new BadParameterException(adaptor.getName(), "queue \"" + queueName + "\" does not exist");
//        }
//
//        //purge jobs from q if needed (will not actually cancel execution of jobs)
//        purgeQ(singleQ);
//
//        return result;
//    }

    @Override
    public JobStatus getJobStatus(Job job) throws OctopusException {        
        return getJobQueue(job.getScheduler()).getJobStatus(job);
//        
//        
//        
//        LinkedList<SshJobExecutor> tmp = findQueue(job.getJobDescription().getQueueName());
//        SshJobExecutor executor = findJob(tmp, job);
//        return executor.getStatus();
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) {
        JobStatus[] result = new JobStatus[jobs.length];

        for (int i = 0; i < jobs.length; i++) {
            try {
                result[i] = getJobStatus(jobs[i]);
            } catch (OctopusException e) {
                result[i] = new JobStatusImplementation(jobs[i], null, null, e, false, false, null);
            }
        }

        return result;
    }

//    private synchronized void purgeQ(LinkedList<SshJobExecutor> q) {
//        if (maxQSize == -1) {
//            return;
//        }
//
//        //how many jobs do we need to remove
//        int purgeCount = q.size() - maxQSize;
//
//        if (purgeCount <= 0) {
//            return;
//        }
//
//        Iterator<SshJobExecutor> iterator = q.iterator();
//
//        while (iterator.hasNext() && purgeCount > 0) {
//            if (iterator.next().isDone()) {
//                iterator.remove();
//                purgeCount--;
//            }
//        }
//    }
//
//    private LinkedList<SshJobExecutor> findQueue(String queueName) throws OctopusException {
//
//        if (queueName == null || queueName.equals("single")) {
//            return singleQ;
//        } else {
//            throw new OctopusException(adaptor.getName(), "queue \"" + queueName + "\" does not exist");
//        }
//    }
//
//    private SshJobExecutor findJob(LinkedList<SshJobExecutor> queue, Job job) throws OctopusException {
//        for (SshJobExecutor e : queue) {
//            if (e.getJob().equals(job)) {
//                return e;
//            }
//        }
//
//        throw new OctopusException(adaptor.getName(), "Job not found: " + job.getIdentifier());
//    }

    @Override
    public void cancelJob(Job job) throws OctopusException {
        getJobQueue(job.getScheduler()).cancelJob(job);
//        
//        
//        
//        // FIXME: What if job is already gone?
//        LinkedList<SshJobExecutor> tmp = findQueue(job.getJobDescription().getQueueName());
//        findJob(tmp, job).kill();
    }

    public void end() {
        
        
        
        
        
        // FIXME!
        // singleExecutor.shutdownNow();
    }

    @Override
    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws OctopusException {
        return getJobQueue(scheduler).getQueueStatus(scheduler, queueName);
//        
//        
//        
//        
//        if (queueName == null || queueName.equals("single")) {
//            return new QueueStatusImplementation(scheduler, queueName, null, null);
//        } else {
//            throw new OctopusException(adaptor.getName(), "No such queue: " + queueName);
//        }
    }

    @Override
    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws OctopusException {
        return getJobQueue(scheduler).getQueueStatuses(scheduler, queueNames);
//        
//        QueueStatus[] result = new QueueStatus[queueNames.length];
//
//        for (int i = 0; i < queueNames.length; i++) {
//            try {
//                result[i] = getQueueStatus(scheduler, queueNames[i]);
//            } catch (OctopusException e) {
//                result[i] = new QueueStatusImplementation(null, queueNames[i], e, null);
//            }
//        }
//
//        return result;
    }

    @Override
    public void close(Scheduler scheduler) throws OctopusException, OctopusIOException {
        
        if (!(scheduler instanceof SchedulerImplementation)) {
            throw new OctopusException(adaptor.getName(), "Illegal scheduler type.");
        }

        SchedulerImplementation s = (SchedulerImplementation) scheduler;

        SchedulerInfo info = null;
        
        synchronized (this) {
            info = schedulers.remove(s.getUniqueID());

            if (info == null) {
                throw new OctopusException(adaptor.getName(), "Cannot find scheduler: " + s.getUniqueID());
            }
        } 
            
        info.jobQueues.end();
        info.session.disconnect();
    }

    @Override
    public boolean isOpen(Scheduler scheduler) throws OctopusException, OctopusIOException {
        
        if (!(scheduler instanceof SchedulerImplementation)) {
            throw new OctopusException(adaptor.getName(), "Illegal scheduler type.");
        }

        return schedulers.containsKey(((SchedulerImplementation) scheduler).getUniqueID());
    }

    @Override
    public Streams getStreams(Job job) throws OctopusException {
        return getJobQueue(job.getScheduler()).getStreams(job);
//        
//        
//        LinkedList<SshJobExecutor> tmp = findQueue(job.getJobDescription().getQueueName());
//        return findJob(tmp, job).getStreams();
    }
}
