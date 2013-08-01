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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.QueueStatusImplementation;
import nl.esciencecenter.octopus.exceptions.BadParameterException;
import nl.esciencecenter.octopus.exceptions.IncompleteJobDescriptionException;
import nl.esciencecenter.octopus.exceptions.InvalidJobDescriptionException;
import nl.esciencecenter.octopus.exceptions.NoSuchQueueException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.jobs.Streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class JobQueues {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobQueues.class);
    
    /** The minimal allowed value for the polling delay */
    private static final int MIN_POLLING_DELAY = 100;
    
    /** The maximum allowed value for the polling delay */
    private static final int MAX_POLLING_DELAY = 60000;
    
    private final String adaptorName;

    private final Octopus myOctopus;

    private final Scheduler myScheduler;

    private final FileSystem myFileSystem;

    private final List<JobExecutor> singleQ;

    private final List<JobExecutor> multiQ;

    private final List<JobExecutor> unlimitedQ;

    private final ExecutorService singleExecutor;

    private final ExecutorService multiExecutor;

    private final ExecutorService unlimitedExecutor;

    private final long pollingDelay;

    private final InteractiveProcessFactory factory;

    private int jobID = 0;

    public JobQueues(String adaptorName, Octopus myOctopus, Scheduler myScheduler, FileSystem myFileSystem,
            InteractiveProcessFactory factory, int multiQThreads, long pollingDelay) throws BadParameterException {

        LOGGER.debug("Creating JobQueues for Adaptor {} with multiQThreads: {} and pollingDelay: {}",
                adaptorName, multiQThreads, pollingDelay);

        this.adaptorName = adaptorName;
        this.myOctopus = myOctopus;
        this.myScheduler = myScheduler;
        this.myFileSystem = myFileSystem;
        this.factory = factory;
        this.pollingDelay = pollingDelay;

        singleQ = new LinkedList<JobExecutor>();
        multiQ = new LinkedList<JobExecutor>();
        unlimitedQ = new LinkedList<JobExecutor>();

        if (multiQThreads < 1) {
            throw new BadParameterException(adaptorName, "Number of slots for the multi queue cannot be smaller than one!");
        }

        if (pollingDelay < MIN_POLLING_DELAY || pollingDelay > MAX_POLLING_DELAY) {
            throw new BadParameterException(adaptorName, "Polling delay must be between " + MIN_POLLING_DELAY + " and " 
                    + MAX_POLLING_DELAY + "!");
        }

        unlimitedExecutor = Executors.newCachedThreadPool();
        singleExecutor = Executors.newSingleThreadExecutor();
        multiExecutor = Executors.newFixedThreadPool(multiQThreads);
    }

    private synchronized int getNextJobID() {
        return jobID++;
    }

    private void checkScheduler(Scheduler scheduler) throws OctopusException {

        if (scheduler == null) {
            throw new IllegalArgumentException("Adaptor " + adaptorName + ": Scheduler is null!");
        }

        if (scheduler != myScheduler) {
            throw new OctopusException(adaptorName, "Scheduler mismatch! " + scheduler + " != " + myScheduler);
        }
    }

    private void getJobs(List<JobExecutor> list, List<Job> out) {
        for (JobExecutor e : list) {
            out.add(e.getJob());
        }
    }

    public String getDefaultQueueName(Scheduler scheduler) throws OctopusException, OctopusIOException {
        return "single";
    }

    public Job[] getJobs(String... queueNames) throws NoSuchQueueException {

        LOGGER.debug("{}: getJobs for queues {}", adaptorName, queueNames);

        LinkedList<Job> out = new LinkedList<Job>();

        if (queueNames == null || queueNames.length == 0) {
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
                    throw new NoSuchQueueException(adaptorName, "Queue \"" + name + "\" does not exist");
                }
            }
        }
        
        LOGGER.debug("{}: getJobs for queues {} returns {}", adaptorName, queueNames, out);
        
        return out.toArray(new Job[out.size()]);
    }

    private List<JobExecutor> findQueue(String queueName) throws OctopusException {

        if (queueName == null || queueName.equals("single")) {
            return singleQ;
        } else if (queueName.equals("multi")) {
            return multiQ;
        } else if (queueName.equals("unlimited")) {
            return unlimitedQ;
        } else {
            throw new OctopusException(adaptorName, "Queue \"" + queueName + "\" does not exist!");
        }
    }

    private JobExecutor findJob(List<JobExecutor> queue, Job job) throws OctopusException {

        for (JobExecutor e : queue) {
            if (e.getJob().equals(job)) {
                return e;
            }
        }

        throw new OctopusException(adaptorName, "Job not found: " + job.getIdentifier());
    }

    private JobExecutor findJob(Job job) throws OctopusException {
        return findJob(findQueue(job.getJobDescription().getQueueName()), job);
    }

    private void cleanupJob(List<JobExecutor> queue, Job job) {

        Iterator<JobExecutor> itt = queue.iterator();

        while (itt.hasNext()) {
            JobExecutor e = itt.next();

            if (e.getJob().equals(job)) {
                itt.remove();
                return;
            }
        }
    }

    public JobStatus getJobStatus(Job job) throws OctopusException {

        LOGGER.debug("{}: getJobStatus for job {}", adaptorName, job.getIdentifier());

        checkScheduler(job.getScheduler());

        List<JobExecutor> queue = findQueue(job.getJobDescription().getQueueName());
        JobStatus status = findJob(queue, job).getStatus();

        if (status.isDone()) {
            cleanupJob(queue, job);
        }

        return status;
    }

    public JobStatus[] getJobStatuses(Job... jobs) {

        LOGGER.debug("{}: getJobStatuses for jobs {}", adaptorName, jobs);

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

    public JobStatus waitUntilDone(Job job, long timeout) throws OctopusException, OctopusIOException {

        LOGGER.debug("{}: Waiting for job {} for {} ms.", adaptorName, job.getIdentifier(), timeout);

        if (timeout < 0) {
            throw new OctopusException(adaptorName, "Illegal timeout " + timeout);
        }

        checkScheduler(job.getScheduler());

        List<JobExecutor> queue = findQueue(job.getJobDescription().getQueueName());
        JobStatus status = findJob(queue, job).waitUntilDone(timeout);

        if (status.isDone()) {
            LOGGER.debug("{}: Job {} is done after {} ms.", adaptorName, job.getIdentifier(), timeout);
            cleanupJob(queue, job);
        } else {
            LOGGER.debug("{}: Job {} is NOT done after {} ms.", adaptorName, job.getIdentifier(), timeout);
        }

        return status;
    }

    public JobStatus waitUntilRunning(Job job, long timeout) throws OctopusException, OctopusIOException {

        LOGGER.debug("{}: Waiting for job {} to start for {} ms.", adaptorName, job.getIdentifier(), timeout);

        if (timeout < 0) {
            throw new OctopusException(adaptorName, "Illegal timeout " + timeout);
        }

        checkScheduler(job.getScheduler());

        List<JobExecutor> queue = findQueue(job.getJobDescription().getQueueName());
        JobStatus status = findJob(queue, job).waitUntilRunning(timeout);

        if (status.isDone()) {
            LOGGER.debug("{}: Job {} is done after {} ms.", adaptorName, job.getIdentifier(), timeout);
            cleanupJob(queue, job);
        } else {
            LOGGER.debug("{}: Job {} is NOT done after {} ms.", adaptorName, job.getIdentifier(), timeout);
        }

        return status;
    }

    private void verifyJobDescription(JobDescription description) throws OctopusException {

        String queue = description.getQueueName();

        if (queue == null) {
            queue = "single";
            description.setQueueName("single");
        }

        if (!(queue.equals("single") || queue.equals("multi") || queue.equals("unlimited"))) {
            throw new InvalidJobDescriptionException(adaptorName, "Queue " + queue + " not available locally!");
        }

        String executable = description.getExecutable();

        if (executable == null) {
            throw new IncompleteJobDescriptionException(adaptorName, "Executable missing in JobDescription!");
        }

        int nodeCount = description.getNodeCount();

        if (nodeCount != 1) {
            throw new InvalidJobDescriptionException(adaptorName, "Illegal node count: " + nodeCount);
        }

        int processesPerNode = description.getProcessesPerNode();

        if (processesPerNode != 1) {
            throw new InvalidJobDescriptionException(adaptorName, "Illegal processes per node count: " + processesPerNode);
        }

        int maxTime = description.getMaxTime();

        if (maxTime <= 0) {
            throw new InvalidJobDescriptionException(adaptorName, "Illegal maximum runtime: " + maxTime);
        }

        if (description.isInteractive()) {

            if (description.getStdin() != null) {
                throw new InvalidJobDescriptionException(adaptorName, "Illegal stdin redirect for interactive job!");
            }

            if (description.getStdout() != null && !description.getStdout().equals("stdout.txt")) {
                throw new InvalidJobDescriptionException(adaptorName, "Illegal stdout redirect for interactive job!");
            }

            if (description.getStderr() != null && !description.getStderr().equals("stderr.txt")) {
                throw new InvalidJobDescriptionException(adaptorName, "Illegal stderr redirect for interactive job!");
            }
        } else {
            //            
            //            if (description.getStdout() == null) { 
            //                throw new InvalidJobDescriptionException(adaptorName, 
            //                        "Missing stdout redirect for batch job!");            
            //            }
            //            
            //            if (description.getStderr() == null) { 
            //                throw new InvalidJobDescriptionException(adaptorName, 
            //                        "Missing stderr redirect for batch job!");            
            //            }            
        }
    }

    public Job submitJob(JobDescription description) throws OctopusException {

        LOGGER.debug("{}: Submitting job", adaptorName);

        verifyJobDescription(description);

        LOGGER.debug("{}: JobDescription verified OK", adaptorName);

        JobImplementation result =
                new JobImplementation(myScheduler, adaptorName + "-" + getNextJobID(), description, description.isInteractive(),
                        true);

        LOGGER.debug("{}: Created Job {}", adaptorName, result.getIdentifier());

        JobExecutor executor = new JobExecutor(adaptorName, myOctopus.files(), myFileSystem, factory, result, pollingDelay);

        String queueName = description.getQueueName();

        LOGGER.debug("{}: Submitting job to queue {}", adaptorName, queueName);

        // NOTE: the verifyJobDescription ensures that the queueName has a valid value!
        if (queueName.equals("unlimited")) {
            unlimitedQ.add(executor);
            unlimitedExecutor.execute(executor);
        } else if (queueName.equals("multi")) {
            multiQ.add(executor);
            multiExecutor.execute(executor);
        } else { // queueName == "single"  
            singleQ.add(executor);
            singleExecutor.execute(executor);
        }

        if (description.isInteractive()) {
            executor.waitUntilRunning(0);

            if (executor.isDone() && !executor.hasRun()) {
                cleanupJob(findQueue(queueName), result);
                throw new OctopusException(adaptorName, "Job failed to start!", executor.getError());
            }
        }

        return result;
    }

    public JobStatus cancelJob(Job job) throws OctopusException {

        LOGGER.debug("{}: Cancel job {}", adaptorName, job);

        checkScheduler(job.getScheduler());

        List<JobExecutor> queue = findQueue(job.getJobDescription().getQueueName());

        JobExecutor e = findJob(queue, job);
        
        boolean killed = e.kill();
        
        JobStatus status = null;
        
        if (killed) {
            status = e.getStatus();
        } else { 
            status = e.waitUntilDone(pollingDelay);
        }

        if (status.isDone()) {
            cleanupJob(queue, job);
        }
        
        return status;
    }

    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws OctopusException {

        LOGGER.debug("{}: getQueueStatus {}:{}", adaptorName, scheduler, queueName);

        if (queueName == null) {
            throw new IllegalArgumentException("Adaptor " + adaptorName + ": Queue name is null!");
        }

        checkScheduler(scheduler);

        if (queueName.equals("single")) {
            return new QueueStatusImplementation(scheduler, "single", null, null);
        } else if (queueName.equals("multi")) {
            return new QueueStatusImplementation(scheduler, "multi", null, null);
        } else if (queueName.equals("unlimited")) {
            return new QueueStatusImplementation(scheduler, "unlimited", null, null);
        } else {
            throw new NoSuchQueueException(adaptorName, "No such queue: " + queueName);
        }
    }

    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws OctopusException {

        String[] names = queueNames;

        if (names == null) {
            throw new IllegalArgumentException("Adaptor " + adaptorName + ": Queue names are null!");
        }

        if (names.length == 0) {
            names = new String[] { "single", "multi", "unlimited" };
        }

        checkScheduler(scheduler);

        QueueStatus[] result = new QueueStatus[names.length];

        for (int i = 0; i < names.length; i++) {
            try {
                result[i] = getQueueStatus(scheduler, names[i]);
            } catch (OctopusException e) {
                result[i] = new QueueStatusImplementation(scheduler, names[i], e, null);
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
