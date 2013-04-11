package nl.esciencecenter.octopus.adaptors.gridengine;

import java.net.URI;

import nl.esciencecenter.octopus.OctopusProperties;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.jobs.JobsAdaptor;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.security.Credentials;

public class GeJobsAdaptor implements JobsAdaptor {

    public GeJobsAdaptor(OctopusProperties properties, OctopusEngine octopusEngine) {
        // TODO Auto-generated constructor stub
    }

    public void end() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String[] getQueueNames(Scheduler scheduler) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Job[] getJobs(Scheduler scheduler, String queueName) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JobStatus getJobStatus(Job job) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void cancelJob(Job job) throws OctopusException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void cancelJobs(Job... jobs) throws OctopusException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Job[] submitJobs(Scheduler scheduler, JobDescription... descriptions) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Scheduler newScheduler(OctopusProperties properties, URI location) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

}
