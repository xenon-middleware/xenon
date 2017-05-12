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
package nl.esciencecenter.xenon.engine.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.engine.jobs.JobImplementation;
import nl.esciencecenter.xenon.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.xenon.engine.jobs.QueueStatusImplementation;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.jobs.IncompleteJobDescriptionException;
import nl.esciencecenter.xenon.jobs.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.NoSuchQueueException;
import nl.esciencecenter.xenon.jobs.QueueStatus;
import nl.esciencecenter.xenon.jobs.Scheduler;
import nl.esciencecenter.xenon.jobs.Streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class JobQueues {

    /**
     * Simple thread factory which returns daemon threads instead of normal threads
     *
     */
    private class DaemonThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable runnable) {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setDaemon(true);
            return thread;
        }
    }

    private static final String SINGLE_QUEUE_NAME = "single";
    
    private static final String MULTI_QUEUE_NAME = "multi";
    
    private static final String UNLIMITED_QUEUE_NAME = "unlimited";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JobQueues.class);

    /** The minimal allowed value for the polling delay */
    public static final int MIN_POLLING_DELAY = 100;

    /** The maximum allowed value for the polling delay */
    public static final int MAX_POLLING_DELAY = 60000;

    private final String adaptorName;

    private final Files myFiles;

    private final Scheduler myScheduler;

    private final Path workingDirectory;

    private final List<JobExecutor> singleQ;

    private final List<JobExecutor> multiQ;

    private final List<JobExecutor> unlimitedQ;

    private final ExecutorService singleExecutor;

    private final ExecutorService multiExecutor;

    private final ExecutorService unlimitedExecutor;

    private final long pollingDelay;

    private final InteractiveProcessFactory factory;

    private final AtomicLong jobID = new AtomicLong(0L);

    public JobQueues(String adaptorName, Files myFiles, Scheduler myScheduler, Path workingDirectory,
            InteractiveProcessFactory factory, int multiQThreads, long pollingDelay) throws BadParameterException {

        LOGGER.debug("Creating JobQueues for Adaptor {} with multiQThreads: {} and pollingDelay: {}", adaptorName, multiQThreads,
                pollingDelay);

        this.adaptorName = adaptorName;
        this.myFiles = myFiles;
        this.myScheduler = myScheduler;
        this.workingDirectory = workingDirectory;
        this.factory = factory;
        this.pollingDelay = pollingDelay;
        

        singleQ = new LinkedList<>();
        multiQ = new LinkedList<>();
        unlimitedQ = new LinkedList<>();

        if (multiQThreads < 1) {
            throw new BadParameterException(adaptorName, "Number of slots for the multi queue cannot be smaller than one!");
        }

        if (pollingDelay < MIN_POLLING_DELAY || pollingDelay > MAX_POLLING_DELAY) {
            throw new BadParameterException(adaptorName, "Polling delay must be between " + MIN_POLLING_DELAY + " and "
                    + MAX_POLLING_DELAY + "!");
        }

        ThreadFactory threadFactory = new DaemonThreadFactory();

        unlimitedExecutor = Executors.newCachedThreadPool(threadFactory);
        singleExecutor = Executors.newSingleThreadExecutor(threadFactory);
        multiExecutor = Executors.newFixedThreadPool(multiQThreads, threadFactory);
    }

    public long getCurrentJobID() {
        return jobID.get();
    }
	
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private void checkScheduler(Scheduler scheduler) throws XenonException {

        if (scheduler == null) {
            throw new IllegalArgumentException("Adaptor " + adaptorName + ": Scheduler is null!");
        }

        if (scheduler != myScheduler) {
            throw new XenonException(adaptorName, "Scheduler mismatch! " + scheduler + " != " + myScheduler);
        }
    }

    private void getJobs(List<JobExecutor> list, List<Job> out) {
        for (JobExecutor e : list) {
            out.add(e.getJob());
        }
    }

    public String getDefaultQueueName(Scheduler scheduler) throws XenonException {
        return SINGLE_QUEUE_NAME;
    }

    public Job[] getJobs(String... queueNames) throws NoSuchQueueException {

        LOGGER.debug("{}: getJobs for queues {}", adaptorName, queueNames);

        LinkedList<Job> out = new LinkedList<>();

        if (queueNames == null || queueNames.length == 0) {
            getJobs(singleQ, out);
            getJobs(multiQ, out);
            getJobs(unlimitedQ, out);
        } else {
            for (String name : queueNames) {
                if (SINGLE_QUEUE_NAME.equals(name)) {
                    getJobs(singleQ, out);
                } else if (MULTI_QUEUE_NAME.equals(name)) {
                    getJobs(multiQ, out);
                } else if (UNLIMITED_QUEUE_NAME.equals(name)) {
                    getJobs(unlimitedQ, out);
                } else {
                    throw new NoSuchQueueException(adaptorName, "Queue \"" + name + "\" does not exist");
                }
            }
        }

        LOGGER.debug("{}: getJobs for queues {} returns {}", adaptorName, queueNames, out);

        return out.toArray(new Job[out.size()]);
    }

    private List<JobExecutor> findQueue(String queueName) throws XenonException {

        if (queueName == null || SINGLE_QUEUE_NAME.equals(queueName)) {
            return singleQ;
        } else if (MULTI_QUEUE_NAME.equals(queueName)) {
            return multiQ;
        } else if (UNLIMITED_QUEUE_NAME.equals(queueName)) {
            return unlimitedQ;
        } else {
            throw new XenonException(adaptorName, "Queue \"" + queueName + "\" does not exist!");
        }
    }

    private JobExecutor findJob(List<JobExecutor> queue, Job job) throws XenonException {
        
        LOGGER.debug("{}: findJob for job {}", adaptorName, job.getIdentifier());
        
        for (JobExecutor e : queue) {
            if (e.getJob().equals(job)) {
                return e;
            }
        }

        throw new XenonException(adaptorName, "Job not found: " + job.getIdentifier());
    }

    private JobExecutor findJob(Job job) throws XenonException {

        LOGGER.debug("{}: findJob for job {}", adaptorName, job.getIdentifier());

        return findJob(findQueue(job.getJobDescription().getQueueName()), job);
    }

    private void cleanupJob(List<JobExecutor> queue, Job job) {

        LOGGER.debug("{}: cleanupJob for job {}", adaptorName, job.getIdentifier());

        Iterator<JobExecutor> itt = queue.iterator();

        while (itt.hasNext()) {
            JobExecutor e = itt.next();

            if (e.getJob().equals(job)) {
                itt.remove();
                return;
            }
        }
    }

    public JobStatus getJobStatus(Job job) throws XenonException {
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
            } catch (XenonException e) {
                result[i] = new JobStatusImplementation(jobs[i], null, null, e, false, false, null);
            }
        }

        return result;
    }

    public JobStatus waitUntilDone(Job job, long timeout) throws XenonException {
        LOGGER.debug("{}: Waiting for job {} for {} ms.", adaptorName, job.getIdentifier(), timeout);

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

    public JobStatus waitUntilRunning(Job job, long timeout) throws XenonException {

        LOGGER.debug("{}: Waiting for job {} to start for {} ms.", adaptorName, job.getIdentifier(), timeout);

        checkScheduler(job.getScheduler());

        List<JobExecutor> queue = findQueue(job.getJobDescription().getQueueName());
        JobStatus status = findJob(queue, job).waitUntilRunning(timeout);

        if (status.isDone()) {
            LOGGER.debug("{}: Job {} is done within {} ms.", adaptorName, job.getIdentifier(), timeout);
            cleanupJob(queue, job);
        } else {
            LOGGER.debug("{}: Job {} is NOT done after {} ms.", adaptorName, job.getIdentifier(), timeout);
        }

        return status;
    }

    @SuppressWarnings("PMD.NPathComplexity")
    private void verifyJobDescription(JobDescription description) throws XenonException {

        String queue = description.getQueueName();

        if (queue == null) {
            queue = SINGLE_QUEUE_NAME;
            description.setQueueName(SINGLE_QUEUE_NAME);
        }

        if (!(SINGLE_QUEUE_NAME.equals(queue) || MULTI_QUEUE_NAME.equals(queue) || UNLIMITED_QUEUE_NAME.equals(queue))) {
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

        if (maxTime < 0) {
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
        }
    }

    public Job submitJob(JobDescription description) throws XenonException {

        LOGGER.debug("{}: Submitting job", adaptorName);

        verifyJobDescription(description);

        // Copy the JobDescription to ensure that the user doesn't change it after we return. 
        JobDescription copyOfDescription = new JobDescription(description);

        LOGGER.debug("{}: JobDescription verified OK", adaptorName);

        JobImplementation result = new JobImplementation(myScheduler, adaptorName + "-" + jobID.getAndIncrement(), copyOfDescription,
                copyOfDescription.isInteractive(), true);

        LOGGER.debug("{}: Created Job {}", adaptorName, result.getIdentifier());

        JobExecutor executor = new JobExecutor(adaptorName, myFiles, workingDirectory, factory, result, pollingDelay);

        String queueName = copyOfDescription.getQueueName();

        LOGGER.debug("{}: Submitting job to queue {}", adaptorName, queueName);

        // NOTE: the verifyJobDescription ensures that the queueName has a valid value!
        if (UNLIMITED_QUEUE_NAME.equals(queueName)) {
            unlimitedQ.add(executor);
            unlimitedExecutor.execute(executor);
        } else if (MULTI_QUEUE_NAME.equals(queueName)) {
            multiQ.add(executor);
            multiExecutor.execute(executor);
        } else { // queueName must be SINGLE_QUEUE_NAME
            singleQ.add(executor);
            singleExecutor.execute(executor);
        }

        if (copyOfDescription.isInteractive()) {
        
            LOGGER.debug("{}: Waiting for interactive job in queue {} to start.", adaptorName, queueName);

            executor.waitUntilRunning(0);

            if (executor.isDone() && !executor.hasRun()) {
                cleanupJob(findQueue(queueName), result);
                throw new XenonException(adaptorName, "Job failed to start!", executor.getError());
            }
        }

        return result;
    }

    public JobStatus cancelJob(Job job) throws XenonException {
        LOGGER.debug("{}: Cancel job {}", adaptorName, job);

        checkScheduler(job.getScheduler());

        List<JobExecutor> queue = findQueue(job.getJobDescription().getQueueName());

        JobExecutor e = findJob(queue, job);

        boolean killed = e.kill();

        JobStatus status;

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

    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws XenonException {

        LOGGER.debug("{}: getQueueStatus {}:{}", adaptorName, scheduler, queueName);

        if (queueName == null) {
            throw new IllegalArgumentException("Adaptor " + adaptorName + ": Queue name is null!");
        }

        checkScheduler(scheduler);

        if (SINGLE_QUEUE_NAME.equals(queueName)) {
            return new QueueStatusImplementation(scheduler, SINGLE_QUEUE_NAME, null, null);
        } else if (MULTI_QUEUE_NAME.equals(queueName)) {
            return new QueueStatusImplementation(scheduler, MULTI_QUEUE_NAME, null, null);
        } else if (UNLIMITED_QUEUE_NAME.equals(queueName)) {
            return new QueueStatusImplementation(scheduler, UNLIMITED_QUEUE_NAME, null, null);
        } else {
            throw new NoSuchQueueException(adaptorName, "No such queue: " + queueName);
        }
    }

    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws XenonException {

        String[] names = queueNames;

        if (names == null) {
            throw new IllegalArgumentException("Adaptor " + adaptorName + ": Queue names are null!");
        }

        if (names.length == 0) {
            names = new String[] { SINGLE_QUEUE_NAME, MULTI_QUEUE_NAME, UNLIMITED_QUEUE_NAME };
        }

        checkScheduler(scheduler);

        QueueStatus[] result = new QueueStatus[names.length];

        for (int i = 0; i < names.length; i++) {
            try {
                result[i] = getQueueStatus(scheduler, names[i]);
            } catch (XenonException e) {
                result[i] = new QueueStatusImplementation(scheduler, names[i], e, null);
            }
        }

        return result;
    }

    public Streams getStreams(Job job) throws XenonException {
        checkScheduler(job.getScheduler());
        return findJob(job).getStreams();
    }

    public void end() {
        singleExecutor.shutdownNow();
        multiExecutor.shutdownNow();
        unlimitedExecutor.shutdownNow();
    }
}
