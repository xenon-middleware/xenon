package nl.esciencecenter.octopus.adaptors.ssh;

import java.net.URI;

import nl.esciencecenter.octopus.OctopusProperties;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.jobs.JobsAdaptor;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshJobs implements JobsAdaptor {

    private static final Logger logger = LoggerFactory.getLogger(SshJobs.class);

    private final OctopusEngine octopusEngine;

    private final SshAdaptor sshAdaptor;

    private final OctopusProperties properties;

    public SshJobs(OctopusProperties properties, SshAdaptor sshAdaptor, OctopusEngine octopusEngine)
            throws OctopusException {

        this.octopusEngine = octopusEngine;
        this.sshAdaptor = sshAdaptor;
        this.properties = properties;
    }
    
    @Override
    public Scheduler newScheduler(OctopusProperties properties, URI location) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
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

    protected void end() {

    }
}
