package nl.esciencecenter.octopus.adaptors.ssh;

import java.net.URI;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.octopus.ImmutableTypedProperties;
import nl.esciencecenter.octopus.adaptors.local.LocalAdaptor;
import nl.esciencecenter.octopus.adaptors.local.LocalJobExecutor;
import nl.esciencecenter.octopus.adaptors.local.LocalJobs;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.jobs.JobsAdaptor;
import nl.esciencecenter.octopus.exceptions.BadParameterException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.security.Credentials;

public class SshJobs implements JobsAdaptor {

    private static final Logger logger = LoggerFactory.getLogger(LocalJobs.class);
    
    private final OctopusEngine octopusEngine;

    private final int maxQSize;
    
    private final SshAdaptor sshAdaptor;
    
    private final ImmutableTypedProperties properties;

    public SshJobs(ImmutableTypedProperties properties, SshAdaptor sshAdaptor, OctopusEngine octopusEngine)
            throws OctopusException {
        
        this.octopusEngine = octopusEngine;
        this.sshAdaptor = sshAdaptor;
        this.properties = properties;

        maxQSize = properties.getIntProperty(LocalAdaptor.LOCAL_Q_HISTORY_SIZE, LocalAdaptor.DEFAULT_LOCAL_Q_HISTORY_SIZE);
        
        if (maxQSize < 0 && maxQSize != -1) {
            throw new BadParameterException("max q size cannot be negative (excluding -1 for unlimited)", "local", null);
        }
    }
    
    @Override
    public Scheduler newScheduler(ImmutableTypedProperties properties, Credentials credentials, URI location)
            throws OctopusException {
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
