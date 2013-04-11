package nl.esciencecenter.octopus.jobs;

import java.net.URI;
import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

/**
 * Main interface to the jobs package
 * 
 */
public interface Jobs {

    public Scheduler newScheduler(URI location, Credential credential, Properties properties) 
            throws OctopusException, OctopusIOException;

    public Scheduler getLocalScheduler() throws OctopusException, OctopusIOException;

    public void close(Scheduler scheduler) throws OctopusException, OctopusIOException;
    
    public boolean isOpen(Scheduler scheduler) throws OctopusException, OctopusIOException;
    
    public JobDescription newJobDescription();
    
    public Job[] getJobs(Scheduler scheduler, String queueName) throws OctopusException, OctopusIOException;

    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws OctopusException;
    
    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws OctopusException;
    
    /**
     * Submit a job. 
     * 
     * @param description the description of the job to submit.
     * 
     * @return Job representing the running job. 
     * @throws OctopusException if the Job failed to submit. 
     */
    public Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException;

    /**
     * This method returns the state of the Job.
     * 
     * @return This method returns the state of the associated Job
     */
    public JobStatus getJobStatus(Job job) throws OctopusException;

    /**
     * This method returns the state of a set of Jobs.
     * 
     * @return the states of the provided jobs.
     * 
     * @throws OctopusException if the the Job failed to submit. 
     */
    public JobStatus[] getJobStatuses(Job... jobs);

    /**
     * Will forcibly stop a job.
     */
    public void cancelJob(Job job) throws OctopusException;
}
