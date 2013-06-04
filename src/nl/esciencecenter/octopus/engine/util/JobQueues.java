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

package nl.esciencecenter.octopus.engine.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.octopus.exceptions.BadParameterException;
import nl.esciencecenter.octopus.exceptions.IncompleteJobDescriptionException;
import nl.esciencecenter.octopus.exceptions.InvalidJobDescriptionException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.jobs.Streams;

import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.QueueStatusImplementation;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class JobQueues {
    
    private static final Logger logger = LoggerFactory.getLogger(JobQueues.class);
    
    private final Scheduler myScheduler;
    
    private final LinkedList<JobExecutor> singleQ;

    private final LinkedList<JobExecutor> multiQ;

    private final LinkedList<JobExecutor> unlimitedQ;

    private final ExecutorService singleExecutor;

    private final ExecutorService multiExecutor;

    private final ExecutorService unlimitedExecutor;

    private final int maxQSize;

    private final int pollingDelay;
    
    private final Adaptor adaptor;

    private final ProcessWrapperFactory factory;
    
    private int jobID = 0;
    
    public JobQueues(Adaptor adaptor, Scheduler myScheduler, ProcessWrapperFactory factory, int multiQThreads, int maxQSize, 
            int pollingDelay) throws BadParameterException { 
    
        this.adaptor = adaptor;
        this.myScheduler = myScheduler;
        this.factory = factory;
        this.maxQSize = maxQSize;
        this.pollingDelay = pollingDelay;
        
        singleQ = new LinkedList<JobExecutor>();
        multiQ = new LinkedList<JobExecutor>();
        unlimitedQ = new LinkedList<JobExecutor>();

        if (maxQSize < 0 && maxQSize != -1) {
            throw new BadParameterException(adaptor.getName(), "Maximum queue size cannot be negative (excluding -1 for unlimited)");
        }

        if (multiQThreads < 1) {
            throw new BadParameterException(adaptor.getName(), "Number of slots for the multi queue cannot be smaller than one!");
        }
        
        if (pollingDelay < 100 ||  pollingDelay > 60000) {
            throw new BadParameterException(adaptor.getName(), "Polling delay must be between 100 and 60000!");
        }

        unlimitedExecutor = Executors.newCachedThreadPool();
        singleExecutor = Executors.newSingleThreadExecutor();
        multiExecutor = Executors.newFixedThreadPool(multiQThreads);
    }
    
    private synchronized int getNextJobID() {
        return jobID++;
    }
    
    private void checkScheduler(Scheduler scheduler) throws OctopusException { 
        if (scheduler != myScheduler) {
            throw new OctopusException(adaptor.getName(), "Scheduler mismatch! " + scheduler + " != " + myScheduler) ;
        }
    }
    
    private void getJobs(LinkedList<JobExecutor> list, LinkedList<Job> out) {
        
        if (list == null) { 
            return;
        }
        
        for (JobExecutor e : list) {
            out.add(e.getJob());
        }
    }
    
    public Job[] getJobs(String... queueNames) throws OctopusException, OctopusIOException {
        
        LinkedList<Job> out = new LinkedList<Job>();
        
        if (queueNames == null) {
            getJobs(singleQ, out);
            getJobs(multiQ, out);
            getJobs(unlimitedQ, out);
        } else {              
            for (String name : queueNames) { 
                if (name.equals("single")) { 
                    getJobs(singleQ, out);
                } else if (name.equals("multi")) { 
                    getJobs(multiQ, out);
                } else if (name.equals("unlimited")) {
                    getJobs(unlimitedQ, out);
                } else { 
                    throw new BadParameterException(adaptor.getName(), "Queue \"" + name + "\" does not exist");                    
                }
            }
        }
        
        return out.toArray(new Job[out.size()]);
    }

    private synchronized void purgeQ(LinkedList<JobExecutor> q) {
        
        if (maxQSize == -1) {
            return;
        }

        //how many jobs do we need to remove
        int purgeCount = q.size() - maxQSize;

        if (purgeCount <= 0) {
            return;
        }

        Iterator<JobExecutor> iterator = q.iterator();

        while (iterator.hasNext() && purgeCount > 0) {
            if (iterator.next().isDone()) {
                iterator.remove();
                purgeCount--;
            }
        }
    }

    private LinkedList<JobExecutor> findQueue(String queueName) throws OctopusException {

        if (queueName == null || queueName.equals("single")) {
            return singleQ;
        } else if (queueName.equals("multi")) {
            return multiQ;
        } else if (queueName.equals("unlimited")) {
            return unlimitedQ;
        } else {
            throw new OctopusException(adaptor.getName(), "Queue \"" + queueName + "\" does not exist!");
        }
    }

    private JobExecutor findJob(LinkedList<JobExecutor> queue, Job job) throws OctopusException {

        for (JobExecutor e : queue) {
            if (e.getJob().equals(job)) {
                return e;
            }
        }

        throw new OctopusException(adaptor.getName(), "Job not found: " + job.getIdentifier());
    }

    private JobExecutor findJob(Job job) throws OctopusException {
        return findJob(findQueue(job.getJobDescription().getQueueName()), job);
    }
    
    public JobStatus getJobStatus(Job job) throws OctopusException {
        checkScheduler(job.getScheduler());
        return findJob(job).getStatus();
    }
    
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
    
    private void verifyJobDescription(JobDescription description) throws OctopusException { 
        
        String queue = description.getQueueName();
              
        if (queue == null) { 
            queue = "single";
            description.setQueueName("single");
        }
        
        if (!(queue.equals("single") || queue.equals("multi") || queue.equals("unlimited"))) {
            throw new InvalidJobDescriptionException(adaptor.getName(), "Queue " + queue + " not available locally!");
        }

        String executable = description.getExecutable(); 
        
        if (executable == null) { 
            throw new IncompleteJobDescriptionException(adaptor.getName(), "Executable missing in JobDescription!");
        }
        
        int nodeCount = description.getNodeCount();
        
        if (nodeCount != 1) { 
            throw new InvalidJobDescriptionException(adaptor.getName(), "Illegal node count: " + nodeCount);
        }
        
        int processesPerNode = description.getProcessesPerNode();
        
        if (processesPerNode <= 0) { 
            throw new InvalidJobDescriptionException(adaptor.getName(), "Illegal processes per node count: " + 
                    processesPerNode);
        }

        int maxTime = description.getMaxTime();
        
        if (maxTime <= 0) { 
            throw new InvalidJobDescriptionException(adaptor.getName(), "Illegal maximum runtime: " + maxTime);
        }

        if (description.isInteractive()) { 

            if (description.getStdin() != null) { 
                throw new InvalidJobDescriptionException(adaptor.getName(), 
                        "Illegal stdin redirect for interactive job!");            
            }
            
            if (description.getStdout() != null && !description.getStdout().equals("stdout.txt")) { 
                throw new InvalidJobDescriptionException(adaptor.getName(), 
                        "Illegal stdout redirect for interactive job!");            
            }
            
            if (description.getStderr() != null && !description.getStderr().equals("stderr.txt")) { 
                throw new InvalidJobDescriptionException(adaptor.getName(), 
                        "Illegal stderr redirect for interactive job!");            
            }
        } else {             
            
            if (description.getStdout() == null) { 
                throw new InvalidJobDescriptionException(adaptor.getName(), 
                        "Missing stdout redirect for interactive job!");            
            }
            
            if (description.getStderr() == null) { 
                throw new InvalidJobDescriptionException(adaptor.getName(), 
                        "Missing stderr redirect for interactive job!");            
            }            
        }
    }
    
    public void submitJob(JobExecutor executor, String queueName, boolean isInteractive) throws OctopusException {
        
        if (queueName == null || queueName.equals("single")) {
            singleQ.add(executor);
            singleExecutor.execute(executor);
        } else if (queueName.equals("multi")) {
            multiQ.add(executor);
            multiExecutor.execute(executor);
        } else if (queueName.equals("unlimited")) {
            unlimitedQ.add(executor);
            unlimitedExecutor.execute(executor);
        } else {
            throw new OctopusException(adaptor.getName(), "INTERNAL ERROR: failed to submit job!");
        }
        
        //purge jobs from q if needed (will not actually cancel execution of jobs)
        purgeQ(singleQ);
        purgeQ(multiQ);
        purgeQ(unlimitedQ);

        if (isInteractive) {
            executor.waitUntilRunning();
            
            if (executor.isDone() && !executor.hasRun()) { 
                Exception e = executor.getError();
                throw new OctopusException(adaptor.getName(), "Job failed to start!", e);
            } 
        }
    }
    
    public Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException {
        
        if (logger.isDebugEnabled()) { 
            logger.debug(adaptor.getName() + ": Submitting job");
        }
        
        checkScheduler(scheduler);
        
        verifyJobDescription(description);

        if (logger.isDebugEnabled()) { 
            logger.debug(adaptor.getName() + ": JobDescription verified OK");
        }

        JobImplementation result = new JobImplementation(description, scheduler, OctopusEngine.getNextUUID(), 
                adaptor.getName() + "-" + getNextJobID(), description.isInteractive(), true);

        if (logger.isDebugEnabled()) { 
            logger.debug(adaptor.getName() + ": Created Job " + result.getIdentifier());
        }
        
        JobExecutor executor = new JobExecutor(adaptor, result, factory, pollingDelay);
        
        String queueName = description.getQueueName();
       
        if (logger.isDebugEnabled()) { 
            logger.debug(adaptor.getName() + ": Submitting job to queue " + queueName);
        }
       
        if (queueName == null || queueName.equals("single")) {
            singleQ.add(executor);
            singleExecutor.execute(executor);
        } else if (queueName.equals("multi")) {
            multiQ.add(executor);
            multiExecutor.execute(executor);
        } else if (queueName.equals("unlimited")) {
            unlimitedQ.add(executor);
            unlimitedExecutor.execute(executor);
        } else {
            throw new OctopusException(adaptor.getName(), "INTERNAL ERROR: failed to submit job!");
        }
        
        // Purge jobs from the queue if needed (will not actually cancel execution of jobs)
        purgeQ(singleQ);
        purgeQ(multiQ);
        purgeQ(unlimitedQ);

        if (description.isInteractive()) {
            executor.waitUntilRunning();
            
            if (executor.isDone() && !executor.hasRun()) { 
                Exception e = executor.getError();
                throw new OctopusException(adaptor.getName(), "Job failed to start!", e);
            } 
        }
        
        return result;
    }
    
    public void cancelJob(Job job) throws OctopusException {
        checkScheduler(job.getScheduler());
        findJob(job).kill();
    }

    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws OctopusException {
     
        checkScheduler(scheduler);
        
        if (queueName == null || queueName.equals("single")) {
            return new QueueStatusImplementation(scheduler, queueName, null, null);
        } else if (queueName.equals("multi")) {
            return new QueueStatusImplementation(scheduler, queueName, null, null);
        } else if (queueName.equals("unlimited")) {
            return new QueueStatusImplementation(scheduler, queueName, null, null);
        } else {
            throw new OctopusException(adaptor.getName(), "No such queue: " + queueName);
        }
    }
    
    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws OctopusException {
        
        QueueStatus[] result = new QueueStatus[queueNames.length];

        for (int i = 0; i < queueNames.length; i++) {
            try {
                result[i] = getQueueStatus(scheduler, queueNames[i]);
            } catch (OctopusException e) {
                result[i] = new QueueStatusImplementation(null, queueNames[i], e, null);
            }
        }

        return result;        
    }
    
    public Streams getStreams(Job job) throws OctopusException {
        checkScheduler(job.getScheduler());
        return findJob(job).getStreams();
    }
    
    public void end() { 
        singleExecutor.shutdownNow();
        multiExecutor.shutdownNow();
        unlimitedExecutor.shutdownNow();
    }
}
