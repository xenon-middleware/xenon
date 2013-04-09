package nl.esciencecenter.octopus.jobs;

import java.net.URI;
import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.exceptions.OctopusException;

/**
 * Main interface to the jobs package
 *
 */
public interface Jobs {

    public Scheduler newScheduler(URI location) throws OctopusException;

    public Scheduler newScheduler(Properties properties, Credentials credentials, URI location) throws OctopusException;
   
    public String[] getQueueNames(Scheduler scheduler) throws OctopusException;
        
    public Job[] getJobs(Scheduler scheduler, String queueName) throws OctopusException;
        
    public Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException;
    
    public Job[] submitJobs(Scheduler scheduler, JobDescription... descriptions) throws OctopusException;
    
    /**
     * This method returns the state of the Job.
     * 
     * @return This method returns the state of the associated Job
     */
    public JobStatus getJobStatus(Job job) throws OctopusException;

    /**
     * This method returns the state of the Jobs.
     * 
     * @return This method returns the states of the provided jobs.
     */
    public JobStatus[] getJobStatuses(Job... jobs) throws OctopusException;
    
    /**
     * Will forcibly stop a job.
     */
    public void cancelJob(Job job) throws OctopusException;
    
    /**
     * Will forcibly stop a job.
     */
    public void cancelJobs(Job... jobs) throws OctopusException;
    
}
