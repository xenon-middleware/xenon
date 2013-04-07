package nl.esciencecenter.octopus.jobs;

import nl.esciencecenter.octopus.exceptions.OctopusException;

public interface Scheduler {

    public String[] getQueueNames() throws OctopusException;
    
    public Job[] getJobs(String queueName) throws OctopusException;
    
    public Job submitJob(JobDescription description) throws OctopusException;

    public Job submitJob(JobDescription description, JobStateListener listener)
            throws OctopusException;

}
