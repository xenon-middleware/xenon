package nl.esciencecenter.octopus.engine.jobs;

import java.net.URI;

import nl.esciencecenter.octopus.ImmutableTypedProperties;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.security.Credentials;

public interface JobsAdaptor {
    
    public Scheduler newScheduler(ImmutableTypedProperties properties, Credentials credentials, URI location)
            throws OctopusException;
    
    public String[] getQueueNames(Scheduler scheduler) throws OctopusException;
        
    public Job[] getJobs(Scheduler scheduler, String queueName) throws OctopusException;
    
    public JobStatus getJobStatus(Job job) throws OctopusException;
    
    public JobStatus[] getJobStatuses(Job... jobs) throws OctopusException;

    public void cancelJob(Job job) throws OctopusException;
    
    public void cancelJobs(Job... jobs) throws OctopusException;
 
    public Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException;
    
    public Job[] submitJobs(Scheduler scheduler, JobDescription... descriptions) throws OctopusException;    
}
