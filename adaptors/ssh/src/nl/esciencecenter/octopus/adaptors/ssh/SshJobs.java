package nl.esciencecenter.octopus.adaptors.ssh;

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
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshJobs implements Jobs {

    private static final Logger logger = LoggerFactory.getLogger(SshJobs.class);

    private final OctopusEngine octopusEngine;

    private final SshAdaptor sshAdaptor;

    private final OctopusProperties properties;

    public SshJobs(OctopusProperties properties, SshAdaptor sshAdaptor, OctopusEngine octopusEngine) throws OctopusException {

        this.octopusEngine = octopusEngine;
        this.sshAdaptor = sshAdaptor;
        this.properties = properties;
    }

    protected void end() {

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

    @Override
    public Scheduler getLocalScheduler() throws OctopusException, OctopusIOException {
        throw new OctopusException(getClass().getName(), "getLocalScheduler not supported!");
    }

    @Override
    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close(Scheduler scheduler) throws OctopusException, OctopusIOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isOpen(Scheduler scheduler) throws OctopusException, OctopusIOException {
        // TODO Auto-generated method stub
        return false;
    }
}
