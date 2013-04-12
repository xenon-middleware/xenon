package nl.esciencecenter.octopus.jobs;

import java.net.URI;
import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.UnknownPropertyException;

/**
 * Main interface to the jobs package
 * 
 */
public interface Jobs {

    /**
     * Create a new Scheduler that represents a (possibly remote) job scheduler at the <code>location</code>, using the  
     * <code>credentials</code> to get access.  
     * 
     * @param location the location of the Scheduler.
     * @param credential the Credentials to use to get access to the Scheduler. 
     * @param properties optional properties to use when creating the Scheduler.
     * 
     * @return the new Scheduler.
     *
     * @throws UnknownPropertyException If a unknown property was provided.
     * @throws InvalidPropertyException If a known property was provided with an invalid value.
     * @throws InvalidLocationException If the location was invalid.
     * @throws InvalidCredentialsException If the credentials where invalid to access the location.
     *  
     * @throws OctopusException If the creation of the FileSystem failed. 
     * @throws OctopusIOException If an I/O error occurred.
     */
    public Scheduler newScheduler(URI location, Credential credential, Properties properties) 
            throws OctopusException, OctopusIOException;

    /**
     * Get a Scheduler that represents the local machine.
     * 
     * Multiple invocations of this method may return the same Scheduler.
     * 
     * @return the resulting Scheduler.
     *  
     * @throws NoSchedulerException If the scheduler is not available.
     * @throws UnknownPropertyException If a unknown property was provided.
     * @throws InvalidPropertyException If a known property was provided with an invalid value.
     * 
     * @throws OctopusException If the creation of the FileSystem failed. 
     */
    public Scheduler getLocalScheduler() throws OctopusException, OctopusIOException;

    /** 
     * Close a Scheduler.
     * 
     * @param scheduler the Scheduler to close.
     * 
     * @throws NoSchedulerException If the scheduler is not known.
     * @throws InvalidCloseException If the Scheduler cannot be closed (for example a local Scheduler). 
     * @throws OctopusException If the Scheduler failed to close. 
     * @throws OctopusIOException If an I/O error occurred.
     */
    public void close(Scheduler scheduler) throws OctopusException, OctopusIOException;
    
    /** 
     * Test is a Scheduler is open.
     * 
     * @param scheduler the Scheduler to test.
     *
     * @throws NoSchedulerException If the scheduler is not known.
     * @throws OctopusException If the test failed. 
     * @throws OctopusIOException If an I/O error occurred.
     */
    public boolean isOpen(Scheduler scheduler) throws OctopusException, OctopusIOException;

    /** 
     * Get all jobs currently in the <code>queue</code> of <code>scheduler</code>.
     * 
     * Note that jobs submitted by other users or other schedulers may also be returned.   
     * 
     * @param scheduler the Scheduler.
     * @param queueName the name of the queue.
     * 
     * @return an array containing the resulting Jobs.
     * 
     * @throws NoSchedulerException If the scheduler is not known.
     * @throws NoSuchQueueException If the queue does not exist in the scheduler.
     * @throws OctopusException If the Scheduler failed to get jobs.
     * @throws OctopusIOException If an I/O error occurred.
     */
    public Job[] getJobs(Scheduler scheduler, String queueName) throws OctopusException, OctopusIOException;
    
    /** 
     * Get the status of the <code>queue</code> of <code>scheduler</code>.
     * 
     * @param scheduler the Scheduler. 
     * @param queueName the name of the queue.
     * 
     * @return the resulting QueueStatus.
     * 
     * @throws NoSchedulerException If the scheduler is not known.
     * @throws NoSuchQueueException If the queue does not exist in the scheduler.
     * @throws OctopusException If the Scheduler failed to get its status.
     * @throws OctopusIOException If an I/O error occurred.
     */
    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws OctopusException, OctopusIOException;
    
    /** 
     * Get the status of all <code>queues</code> of <code>scheduler</code>.
     * 
     * @param scheduler the Scheduler. 
     * @param queueNames the names of the queues.
     * 
     * @return an array containing the resulting QueueStatus.
     *
     * @throws NoSchedulerException If the scheduler is not known.
     * @throws OctopusException If the Scheduler failed to get the statusses.
     * @throws OctopusIOException If an I/O error occurred.
     */
    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws OctopusException, OctopusIOException;
    
    /**
     * Submit a job to a Scheduler.
     * 
     * @param scheduler the Scheduler. 
     * @param description the description of the job to submit.
     * 
     * @return Job representing the running job.
     *
     * @throws NoSchedulerException If the scheduler is not known.
     * @throws IncompleteJobDescriptionException If the description did not contain the required information.
     * @throws IllegalJobDescriptionException If the description is not legal for this scheduler.
     * @throws InvalidJobDescriptionException If the description contains conflicting options.  
     * @throws OctopusException If the Scheduler failed to get submit the job.
     * @throws OctopusIOException If an I/O error occurred.
     */
    public Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException, OctopusIOException;

    /**
     * Get the status of a Job.
     * 
     * @param job the job.
     * 
     * @return the status of the Job.
     * 
     * @throws NoSuchJobException If the job is not known.
     * @throws OctopusException If the status of the job could not be retrieved.
     * @throws OctopusIOException If an I/O error occurred.
     */
    public JobStatus getJobStatus(Job job) throws OctopusException, OctopusIOException;

    /**
     * Get the status of all specified <code>jobs</code>.
     * 
     * If the retrieval of the JobStatus for a specific job fails the exception will be stored in the associated JobsStatus. 
     * 
     * @param jobs the jobs for which to retrieve the status.
     * 
     * @return an array of the resulting JobStatusses.
     * 
     * @throws OctopusException If the statuses of the job could not be retrieved.
     * @throws OctopusIOException If an I/O error occurred.
     */
    public JobStatus[] getJobStatuses(Job... jobs);

    /** 
     * Cancel a job.
     *  
     * @param job the job.
     * 
     * @throws NoSuchJobException If the job is not known.
     * @throws OctopusException If the status of the job could not be retrieved.
     * @throws OctopusIOException If an I/O error occurred.
     */
    public void cancelJob(Job job) throws OctopusException, OctopusIOException;
}
