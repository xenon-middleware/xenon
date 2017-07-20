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
package nl.esciencecenter.xenon.adaptors.schedulers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.schedulers.IncompleteJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;
import nl.esciencecenter.xenon.schedulers.NoSuchJobException;
import nl.esciencecenter.xenon.schedulers.NoSuchQueueException;
import nl.esciencecenter.xenon.schedulers.QueueStatus;
import nl.esciencecenter.xenon.schedulers.Scheduler;
import nl.esciencecenter.xenon.schedulers.Streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class JobQueueScheduler extends Scheduler {

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

	private static final Logger LOGGER = LoggerFactory.getLogger(JobQueueScheduler.class);

	private static final String SINGLE_QUEUE_NAME = "single";

	private static final String MULTI_QUEUE_NAME = "multi";

	private static final String UNLIMITED_QUEUE_NAME = "unlimited";

	/** The minimal allowed value for the polling delay */
	public static final int MIN_POLLING_DELAY = 100;

	/** The maximum allowed value for the polling delay */
	public static final int MAX_POLLING_DELAY = 60000;

	private final String adaptorName;

	private final FileSystem filesystem;

	private final Path workingDirectory;

	private final List<JobExecutor> singleQ = new LinkedList<>();

	private final List<JobExecutor> multiQ = new LinkedList<>();

	private final List<JobExecutor> unlimitedQ = new LinkedList<>();

	private final ExecutorService singleExecutor;

	private final ExecutorService multiExecutor;

	private final ExecutorService unlimitedExecutor;

	private final long pollingDelay;

	private final InteractiveProcessFactory factory;

	private final AtomicLong jobID = new AtomicLong(0L);

	private final ArrayList<List<JobExecutor>> queues = new ArrayList<List<JobExecutor>>(); 

	public JobQueueScheduler(String uniqueID, String adaptorName, String location, InteractiveProcessFactory factory, 
			FileSystem filesystem, Path workingDirectory, int multiQThreads, long pollingDelay, XenonProperties properties) throws BadParameterException {

		super(uniqueID, adaptorName, location, true, true, true, properties);

		LOGGER.debug("Creating JobQueueScheduler for Adaptor {} with multiQThreads: {} and pollingDelay: {}", adaptorName, multiQThreads,
				pollingDelay);

		this.adaptorName = adaptorName;
		this.filesystem = filesystem;
		this.workingDirectory = workingDirectory;
		this.factory = factory;
		this.pollingDelay = pollingDelay;

		queues.add(singleQ);
		queues.add(multiQ);
		queues.add(unlimitedQ);

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

	//    private void checkScheduler(Scheduler sched) throws XenonException {
	//
	//        if (sched == null) {
	//            throw new IllegalArgumentException("Adaptor " + adaptorName + ": Scheduler is null!");
	//        }
	//
	//        if (!equals(sched)) {
	//            throw new XenonException(adaptorName, "Scheduler mismatch! " + this + " != " + sched);
	//        }
	//    }

	private void getJobs(List<JobExecutor> list, List<String> out) {
		for (JobExecutor e : list) {
			out.add(e.getJobIdentifier());
		}
	}

	public String getDefaultQueueName() throws XenonException {
		return SINGLE_QUEUE_NAME;
	}

	public String[] getJobs(String... queueNames) throws NoSuchQueueException {

		LOGGER.debug("{}: getJobs for queues {}", adaptorName, queueNames);

		LinkedList<String> out = new LinkedList<>();

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

		return out.toArray(new String[out.size()]);
	}

	//    private List<JobExecutor> findQueue(String queueName) throws XenonException {
	//
	//        if (queueName == null || SINGLE_QUEUE_NAME.equals(queueName)) {
	//            return singleQ;
	//        } else if (MULTI_QUEUE_NAME.equals(queueName)) {
	//            return multiQ;
	//        } else if (UNLIMITED_QUEUE_NAME.equals(queueName)) {
	//            return unlimitedQ;
	//        } else {
	//            throw new XenonException(adaptorName, "Queue \"" + queueName + "\" does not exist!");
	//        }
	//    }

	private JobExecutor findJob(List<JobExecutor> queue, String jobIdentifier) throws XenonException {

		LOGGER.debug("{}: findJob for job {}", adaptorName, jobIdentifier);

		for (JobExecutor e : queue) {
			if (jobIdentifier.equals(e.getJobIdentifier())) {
				return e;
			}
		}

		return null;
	}

	private JobExecutor findJob(String jobIdentifier) throws XenonException {

		LOGGER.debug("{}: findJob for job {}", adaptorName, jobIdentifier);

		JobExecutor e = null;

		for (List<JobExecutor> queue : queues) { 
			e = findJob(queue, jobIdentifier);

			if (e != null) { 
				return e;
			}
		}

		throw new NoSuchJobException(adaptorName, "Job " + jobIdentifier + " does not exist!");
	}

	private boolean cleanupJob(List<JobExecutor> queue, String jobIdentifier) {

		LOGGER.debug("{}: cleanupJob for job {}", adaptorName, jobIdentifier);

		Iterator<JobExecutor> itt = queue.iterator();

		while (itt.hasNext()) {
			JobExecutor e = itt.next();

			if (e.getJobIdentifier().equals(jobIdentifier)) {
				itt.remove();
				return true;
			}
		}

		return false;
	}

	private void cleanupJob(String jobIdentifier) {

		for (List<JobExecutor> queue : queues) { 
			if (cleanupJob(queue, jobIdentifier)) { 
				return;
			}
		}
	}


	public JobStatus getJobStatus(String jobIdentifier) throws XenonException {
		LOGGER.debug("{}: getJobStatus for job {}", adaptorName, jobIdentifier);

		// checkScheduler(job.getScheduler());

		JobStatus status = findJob(jobIdentifier).getStatus();

		if (status.isDone()) {
			cleanupJob(jobIdentifier);
		}

		return status;
	}

	public JobStatus[] getJobStatuses(String... jobs) {

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
				result[i] = new JobStatus(jobs[i], null, null, e, false, false, null);
			}
		}

		return result;
	}

	public JobStatus waitUntilDone(String jobIdentifier, long timeout) throws XenonException {
		LOGGER.debug("{}: Waiting for job {} for {} ms.", adaptorName, jobIdentifier, timeout);

		//checkScheduler(job.getScheduler());

		JobStatus status = findJob(jobIdentifier).waitUntilDone(timeout);

		if (status.isDone()) {
			LOGGER.debug("{}: Job {} is done after {} ms.", adaptorName, jobIdentifier, timeout);
			cleanupJob(jobIdentifier);
		} else {
			LOGGER.debug("{}: Job {} is NOT done after {} ms.", adaptorName, jobIdentifier, timeout);
		}

		return status;
	}

	public JobStatus waitUntilRunning(String jobIdentifier, long timeout) throws XenonException {

		LOGGER.debug("{}: Waiting for job {} to start for {} ms.", adaptorName, jobIdentifier, timeout);

		///checkScheduler(job.getScheduler());

		JobStatus status = findJob(jobIdentifier).waitUntilRunning(timeout);

		if (status.isDone()) {
			LOGGER.debug("{}: Job {} is done within {} ms.", adaptorName, jobIdentifier, timeout);
			cleanupJob(jobIdentifier);
		} else {
			LOGGER.debug("{}: Job {} is NOT done after {} ms.", adaptorName, jobIdentifier, timeout);
		}

		return status;
	}

	@SuppressWarnings("PMD.NPathComplexity")
	private void verifyJobDescription(JobDescription description, boolean interactive) throws XenonException {

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

		if (interactive) {

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

	private JobExecutor submit(JobDescription description, boolean interactive) throws XenonException {

		LOGGER.debug("{}: Submitting job", adaptorName);

		verifyJobDescription(description, interactive);

		String jobIdentifier = adaptorName + "-" + jobID.getAndIncrement();

		LOGGER.debug("{}: Created Job {}", adaptorName, jobIdentifier);

		JobExecutor executor = new JobExecutor(adaptorName, filesystem, workingDirectory, factory, 
				new JobDescription(description), jobIdentifier, interactive, pollingDelay);

		String queueName = description.getQueueName();

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

		return executor;
	}    

	public String submitBatchJob(JobDescription description) throws XenonException {
		return submit(description, false).getJobIdentifier();
	}

	public Streams submitInteractiveJob(JobDescription description) throws XenonException {

		JobExecutor executor = submit(description, true);

		LOGGER.debug("{}: Waiting for interactive job to start.", adaptorName);

		executor.waitUntilRunning(0);

		if (executor.isDone() && !executor.hasRun()) {
			cleanupJob(executor.getJobIdentifier());
			throw new XenonException(adaptorName, "Interactive job failed to start!", executor.getError());
		}

		return executor.getStreams();
	}


	public JobStatus cancelJob(String jobIdentifier) throws XenonException {
		LOGGER.debug("{}: Cancel job {}", adaptorName, jobIdentifier);

		// checkScheduler(job.getScheduler());

		JobExecutor e = findJob(jobIdentifier);

		boolean killed = e.kill();

		JobStatus status;

		if (killed) {
			status = e.getStatus();
		} else {
			status = e.waitUntilDone(pollingDelay);
		}

		if (status.isDone()) {
			cleanupJob(jobIdentifier);
		}

		return status;
	}

	public QueueStatus getQueueStatus(String queueName) throws XenonException {

		LOGGER.debug("{}: getQueueStatus {}", adaptorName, queueName);

		if (queueName == null) {
			throw new IllegalArgumentException("Adaptor " + adaptorName + ": Queue name is null!");
		}

		if (SINGLE_QUEUE_NAME.equals(queueName)) {
			return new QueueStatus(this, SINGLE_QUEUE_NAME, null, null);
		} else if (MULTI_QUEUE_NAME.equals(queueName)) {
			return new QueueStatus(this, MULTI_QUEUE_NAME, null, null);
		} else if (UNLIMITED_QUEUE_NAME.equals(queueName)) {
			return new QueueStatus(this, UNLIMITED_QUEUE_NAME, null, null);
		} else {
			throw new NoSuchQueueException(adaptorName, "No such queue: " + queueName);
		}
	}

	public String [] getQueueNames() { 
		return new String[] { SINGLE_QUEUE_NAME, MULTI_QUEUE_NAME, UNLIMITED_QUEUE_NAME };
	}

	public QueueStatus[] getQueueStatuses(String... queueNames) throws XenonException {

		String[] names = queueNames;

		if (names == null) {
			throw new IllegalArgumentException("Adaptor " + adaptorName + ": Queue names are null!");
		}

		if (names.length == 0) {
			names = new String[] { SINGLE_QUEUE_NAME, MULTI_QUEUE_NAME, UNLIMITED_QUEUE_NAME };
		}

		QueueStatus[] result = new QueueStatus[names.length];

		for (int i = 0; i < names.length; i++) {
			try {
				result[i] = getQueueStatus(names[i]);
			} catch (XenonException e) {
				result[i] = new QueueStatus(this, names[i], e, null);
			}
		}

		return result;
	}

	//    public Streams getStreams(JobHandle job) throws XenonException {
	//        checkScheduler(job.getScheduler());
	//        return findJob(job).getStreams();
	//    }

	public void end() {
		singleExecutor.shutdownNow();
		multiExecutor.shutdownNow();
		unlimitedExecutor.shutdownNow();
	}

	@Override
	public void close() throws XenonException {
		factory.close();
	}

	@Override
	public boolean isOpen() throws XenonException {
		return factory.isOpen();
	}
}
