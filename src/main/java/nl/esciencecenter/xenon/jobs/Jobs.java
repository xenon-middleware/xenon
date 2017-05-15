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
package nl.esciencecenter.xenon.jobs;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.UnknownPropertyException;
import nl.esciencecenter.xenon.InvalidPropertyException;
import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.InvalidCredentialException;
import nl.esciencecenter.xenon.credentials.Credential;

/**
 * The Jobs API of Xenon.
 * 
 * This interface creates various methods for creating and closing Schedulers, submitting jobs, and retrieving information about
 * schedulers and jobs.
 * 
 * @version 1.0
 * @since 1.0
 */
public interface Jobs {

    /**
     * Create a new Scheduler that represents a (possibly remote) job 
     * scheduler at the <code>location</code>, using the <code>scheme</code>
     * and <code>credentials</code> to get access. Make sure to always close 
     * {@code Scheduler} instances by calling {@code close(Scheduler)} when
     * you no longer need them, otherwise their associated resources remain 
     * allocated.
     * 
     * @param scheme
     *            the scheme used to access the Scheduler.
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
     * @throws InvalidCredentialException
     *             If the credential is invalid to access the location.
     * 
     * @throws XenonException
     *             If the creation of the Scheduler failed.
     */
    Scheduler newScheduler(String scheme, String location, Credential credential, Map<String, String> properties) 
            throws XenonException;
    
    /**
     * Get a list of job schemes supported by Xenon. 
     * 
     * @return the list of job schemes supported by Xenon.
     * 
     * @throws XenonException
     *             If the list of schemes could not be retrieved.
     */
    String [] getSupportedSchemes() throws XenonException;
    
    /**
     * Does this scheme require an online scheduler ?
     * 
     * Online schedulers need to remain active for their jobs to run. Ending an online scheduler will kill all jobs that were
     * submitted to it.
     * 
     * Offline schedulers do not need to remains active for their jobs to run. A submitted job will typically be handed over to
     * some external server that will manage the job for the rest of its lifetime.
     * 
     * Online schedulers typically support both interactive jobs (where the user controls the standard streams) and batch jobs
     * (where the standard streams are redirected to/from files).
     * 
     * Since it is impossible to continue an interactive jobs when a scheduler ends, interactive jobs will always be killed,
     * even in an offline scheduler.
     * 
     * @return if this scheme requires an online scheduler.
     * 
     * @throws XenonException
     *             If information on the scheme could not be retrieved.
     */
    boolean isOnline(String scheme) throws XenonException;
    
    /**
     * Does this scheme supports the submission of interactive jobs ?
     * 
     * For interactive jobs the standard streams of the job must be handled by the submitting process. Failing to do so may cause
     * the job to hang indefinitely.
     * 
     * Note that it is impossible to continue an interactive jobs when its scheduler ends. This will cause the interactive job
     * to be killed,
     * 
     * @return if this scheme supports the submission of interactive jobs ?
     * 
     * @throws XenonException
     *             If information on the scheme could not be retrieved.
     */
    boolean supportsInteractive(String scheme) throws XenonException;

    /**
     * Does this scheme support the submission of batch jobs ?
     * 
     * For batch jobs the standard streams of the jobs are redirected from / to files.
     * 
     * @return if this scheme supports the submission of batch jobs ?
     * 
     * @throws XenonException
     *             If information on the scheme could not be retrieved.
     */
    boolean supportsBatch(String scheme) throws XenonException;
    
    /**
     * Close a Scheduler.
     * 
     * @param scheduler
     *            the Scheduler to close.
     * 
     * @throws NoSuchSchedulerException
     *             If the scheduler is not known.
     * @throws XenonException
     *             If the Scheduler failed to close.
     */
    void close(Scheduler scheduler) throws XenonException;

    /**
     * Test if the connection to a Scheduler is open.
     * 
     * @param scheduler
     *            the Scheduler to test.
     * 
     * @throws XenonException
     *             If the test failed.
     * @throws XenonException
     *             If an I/O error occurred.
     * @return  
     *          <code>true</code> if the connection to the Scheduler is still open, <code>false</code> otherwise.             
     *             
     */
    boolean isOpen(Scheduler scheduler) throws XenonException;

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
     * @throws XenonException
     *             If the Scheduler failed to get its status.
     * @throws XenonException
     *             If an I/O error occurred.
     */
    String getDefaultQueueName(Scheduler scheduler) throws XenonException;

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
     * @throws XenonException
     *             If the Scheduler failed to get jobs.
     */
    Job[] getJobs(Scheduler scheduler, String... queueNames) throws XenonException;

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
     * @throws XenonException
     *             If the Scheduler failed to get its status.
     */
    QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws XenonException;

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
     * @throws XenonException
     *             If the Scheduler failed to get the statusses.
     */
    QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws XenonException;

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
     * @throws IncompleteJobDescriptionException
     *             If the description did not contain the required information.
     * @throws InvalidJobDescriptionException
     *             If the description contains illegal or conflicting values.
     * @throws UnsupportedJobDescriptionException
     *             If the description is not legal for this scheduler.
     * @throws XenonException
     *             If the Scheduler failed to get submit the job.
     */
    Job submitJob(Scheduler scheduler, JobDescription description) throws XenonException;

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
     * @throws XenonException
     *             If the status of the job could not be retrieved.
     */
    JobStatus getJobStatus(Job job) throws XenonException;

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
     * @throws XenonException
     *             if the job is not interactive.
     */
    Streams getStreams(Job job) throws XenonException;

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
     * @throws XenonException
     *             If the status of the job could not be retrieved.
     */
    JobStatus cancelJob(Job job) throws XenonException;

    /**
     * Wait until a job is done or until a timeout expires.
     * <p>
     * This method will wait until a job is done (either gracefully or by being killed or producing an error), or until the 
     * timeout expires, whichever comes first. If the timeout expires, the job will continue to run.
     * </p>
     * <p>
     * The timeout is in milliseconds and must be &gt;= 0. When timeout is 0, it will be ignored and this method will wait until
     * the jobs is done.  
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
     * @throws IllegalArgumentException 
     *             If the value of timeout is negative
     * @throws NoSuchJobException
     *             If the job is not known.
     * @throws XenonException
     *             If the status of the job could not be retrieved.
     */
    JobStatus waitUntilDone(Job job, long timeout) throws XenonException;

    /**
     * Wait while a job is waiting in a queue, or until a timeout expires.
     * <p>
     * This method will return as soon as the job is no longer waiting in the queue, or when the timeout expires, whichever comes
     * first. If the job is no longer waiting in the queue, it may be running, but it may also be killed, finished or produced 
     * an error. If the timeout expires, the job will continue to be queued normally.
     * </p>
     * <p>
     * The timeout is in milliseconds and must be &gt;= 0. When timeout is 0, it will be ignored and this method will wait until
     * the job is no longer queued.  
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
     * @throws IllegalArgumentException 
     *             If the value of timeout is negative
     * @throws NoSuchJobException
     *             If the job is not known.
     * @throws XenonException
     *             If the status of the job could not be retrieved.
     */
    JobStatus waitUntilRunning(Job job, long timeout) throws XenonException;
}
