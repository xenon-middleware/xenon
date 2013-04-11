package nl.esciencecenter.octopus.adaptors.gridengine;

import java.net.URI;
import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.Scheduler;

public class GridEngineJobs implements Jobs {

    public GridEngineJobs(OctopusProperties properties, OctopusEngine octopusEngine) {
        // TODO Auto-generated constructor stub
    }

    @Override
    public Scheduler newScheduler(URI location, Credential credential, Properties properties) throws OctopusException,
            OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JobDescription newJobDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getQueueNames(Scheduler scheduler) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Job[] getJobs(Scheduler scheduler, String queueName) throws OctopusException, OctopusIOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JobStatus getJobStatus(Job job) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void cancelJob(Job job) throws OctopusException {
        // TODO Auto-generated method stub
        
    }

    public void end() {
        // TODO Auto-generated method stub
        
    }



}
