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
package nl.esciencecenter.octopus.jobs;

import java.net.URI;
import java.util.Map;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

/**
 * Jobs represents the Jobs interface of Octopus.
 * 
 * This interface creates various methods for creating and closing Schedulers, submitting jobs, and retrieving information about
 * schedulers and jobs.
 * 
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface Jobs {

    /**
     * Create a new Scheduler that represents a (possibly remote) job scheduler at the <code>location</code>, using the
     * <code>credentials</code> to get access.
     * 
     * @param location
     *            the location of the Scheduler.
     * @param credential
     *            the Credentials to use to get access to the Scheduler.
     * @param properties
     *            optional properties to configure the Scheduler when it is created.
     * 
     * @return the new Scheduler.
     * 
     * @throws UnknownPropertyException
     *             If a unknown property was provided.
     * @throws InvalidPropertyException
     *             If a known property was provided with an invalid value.
     * @throws InvalidLocationException
     *             If the location was invalid.
     * @throws InvalidCredentialsException
     *             If the credentials where invalid to access the location.
     * 
     * @throws OctopusException
     *             If the creation of the Scheduler failed.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    Scheduler newScheduler(URI location, Credential credential, Map<String, String> properties) throws OctopusException,
            OctopusIOException;

    /**
     * Get a Scheduler that represents the local machine.
     * 
     * Multiple invocations of this method may return the same Scheduler.
     * 
     * @return the resulting Scheduler.
     * 
     * @throws NoSchedulerException
     *             If the scheduler is not available.
     * @throws UnknownPropertyException
     *             If a unknown property was provided.
     * @throws InvalidPropertyException
     *             If a known property was provided with an invalid value.
     * 
     * @throws OctopusException
     *             If the creation of the Scheduler failed.
     */
    Scheduler getLocalScheduler() throws OctopusException, OctopusIOException;

    /**
     * Close a Scheduler.
     * 
     * @param scheduler
     *            the Scheduler to close.
     * 
     * @throws NoSuchSchedulerException
     *             If the scheduler is not known.
     * @throws OctopusException
     *             If the Scheduler failed to close.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    void close(Scheduler scheduler) throws OctopusException, OctopusIOException;

    /**
     * Test if a Scheduler is open.
     * 
     * @param scheduler
     *            the Scheduler to test.
     * 
     * @throws OctopusException
     *             If the test failed.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    boolean isOpen(Scheduler scheduler) throws OctopusException, OctopusIOException;

    /**
     * Get the name of the default queue for the <code>scheduler</code>.
     * 
     * @param scheduler
     *            the Scheduler.
     * 
     * @return the name of the default queue for the scheduler, or <code>null</code> if no default queue is available.
     * 
     * @throws NoSuchSchedulerException
     *             If the scheduler is not known.
     * @throws OctopusException
     *             If the Scheduler failed to get its status.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    String getDefaultQueueName(Scheduler scheduler) throws OctopusException, OctopusIOException;

    /**
     * Get all jobs currently in (one ore more) queues of <code>scheduler</code>.
     * 
     * If no queue names are specified, the jobs for all queues are returned.
     * 
     * Note that jobs submitted by other users or other schedulers may also be returned.
     * 
     * @param scheduler
     *            the Scheduler.
     * @param queueNames
     *            the names of the queues.
     * 
     * @return an array containing the resulting Jobs.
     * 
     * @throws NoSuchSchedulerException
     *             If the scheduler is not known.
     * @throws NoSuchQueueException
     *             If the queue does not exist in the scheduler.
     * @throws OctopusException
     *             If the Scheduler failed to get jobs.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    Job[] getJobs(Scheduler scheduler, String... queueNames) throws OctopusException, OctopusIOException;

    /**
     * Get the status of the <code>queue</code> of <code>scheduler</code>.
     * 
     * @param scheduler
     *            the Scheduler.
     * @param queueName
     *            the name of the queue.
     * 
     * @return the resulting QueueStatus.
     * 
     * @throws NoSuchSchedulerException
     *             If the scheduler is not known.
     * @throws NoSuchQueueException
     *             If the queue does not exist in the scheduler.
     * @throws OctopusException
     *             If the Scheduler failed to get its status.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws OctopusException, OctopusIOException;

    /**
     * Get the status of all <code>queues</code> of <code>scheduler</code>.
     * 
     * Note that this method will only throw an exception when this exception will influence all status requests. For example, if
     * the scheduler is invalid or not reachable.
     * 
     * Exceptions that only refer to a single queue are returned in the QueueStatus returned for that queue.
     * 
     * @param scheduler
     *            the Scheduler.
     * @param queueNames
     *            the names of the queues.
     * 
     * @return an array containing the resulting QueueStatus.
     * 
     * @throws NoSuchSchedulerException
     *             If the scheduler is not known.
     * @throws OctopusException
     *             If the Scheduler failed to get the statusses.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws OctopusException, OctopusIOException;

    /**
     * Submit a job to a Scheduler.
     * 
     * @param scheduler
     *            the Scheduler.
     * @param description
     *            the description of the job to submit.
     * 
     * @return Job representing the running job.
     * 
     * @throws NoSchedulerException
     *             If the scheduler is not known.
     * @throws IncompleteJobDescriptionException
     *             If the description did not contain the required information.
     * @throws InvalidJobDescriptionException
     *             If the description contains illegal or conflicting values.
     * @throws UnsupportedJobDescriptionException
     *             If the description is not legal for this scheduler.
     * @throws OctopusException
     *             If the Scheduler failed to get submit the job.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException, OctopusIOException;

    /**
     * Get the status of a Job.
     * 
     * @param job
     *            the job.
     * 
     * @return the status of the Job.
     * 
     * @throws NoSuchJobException
     *             If the job is not known.
     * @throws OctopusException
     *             If the status of the job could not be retrieved.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    JobStatus getJobStatus(Job job) throws OctopusException, OctopusIOException;

    /**
     * Get the status of all specified <code>jobs</code>.
     * <p>
     * The array of <code>JobStatus</code> contains one entry for each of the <code>jobs</code>. The order of the elements in the
     * returned <code>JobStatus</code> array corresponds to the order in which the <code>jobs</code> are passed as parameters. If
     * a <code>job</code> is <code>null</code>, the corresponding entry in the <code>JobStatus</code> array will also be
     * <code>null</code>. If the retrieval of the <code>JobStatus</code> fails for a job, the exception will be stored in the
     * corresponding <code>JobsStatus</code> entry.
     * </p>
     * @param jobs
     *            the jobs for which to retrieve the status.
     * 
     * @return an array of the resulting JobStatusses.
     * 
     * @throws OctopusException
     *             If the statuses of the job could not be retrieved.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    JobStatus[] getJobStatuses(Job... jobs);

    /**
     * Returns the standard streams of a job.
     * 
     * The standard streams can only be retrieved if it is an interactive job.
     * 
     * @param job
     *            the interactive job for which to retrieve the streams.
     * @return the streams of the job.
     * 
     * @throws OctopusException
     *             if the job is not interactive.
     */
    Streams getStreams(Job job) throws OctopusException;

    /**
     * Cancel a job.
     * <p>
     * A status is returned that indicates the state of the job after the cancel. If the job was already done it cannot be 
     * cancelled.
     * </p>
     * <p>
     * A {@link JobStatus} is returned that can be used to determine the state of the job after cancelJob returns. Note that it 
     * may take some time before the job has actually terminated. The {@link #waitUntilDone(Job, long) waitUntilDone} method can 
     * be used to wait until the job is terminated.
     * </p>
     * @param job
     *            the job to kill.
     * @return the status of the Job.
     * 
     * @throws NoSuchJobException
     *             If the job is not known.
     * @throws OctopusException
     *             If the status of the job could not be retrieved.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    JobStatus cancelJob(Job job) throws OctopusException, OctopusIOException;

    /**
     * Wait until a job is done or until a timeout expires.
     * <p>
     * This method will wait until a job is done, killed, or produces an error, or until a timeout expires. If the timeout
     * expires, the job will continue to run normally.
     * </p>
     * <p>
     * The timeout is in milliseconds and must be >= 0, where 0 means an infinite timeout.
     * </p>
     * <p>
     * A JobStatus is returned that can be used to determine why the call returned.
     * </p>
     * @param job
     *            the job.
     * @param timeout
     *            the maximum time to wait for the job in milliseconds.
     * @return the status of the Job.
     * 
     * @throws NoSuchJobException
     *             If the job is not known.
     * @throws OctopusException
     *             If the status of the job could not be retrieved.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    JobStatus waitUntilDone(Job job, long timeout) throws OctopusException, OctopusIOException;

    /**
     * Wait for as long a job is waiting in a queue, or until a timeout expires.
     * <p>
     * This method will return as soon as the job is no longer waiting in the queue. This is generally the case when it starts
     * running, but it may also be killed or produce an error. If the timeout expires, the job will continue to be queued
     * normally.
     * </p>
     * <p>
     * The timeout is in milliseconds and must be >= 0, where 0 means an infinite timeout.
     * </p>
     * <p>
     * A JobStatus is returned that can be used to determine why the call returned.
     * </p>
     * @param job
     *            the job.
     * @param timeout
     *            the maximum time to wait in milliseconds.
     * @return the status of the Job.
     * 
     * @throws NoSuchJobException
     *             If the job is not known.
     * @throws OctopusException
     *             If the status of the job could not be retrieved.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    JobStatus waitUntilRunning(Job job, long timeout) throws OctopusException, OctopusIOException;
}
