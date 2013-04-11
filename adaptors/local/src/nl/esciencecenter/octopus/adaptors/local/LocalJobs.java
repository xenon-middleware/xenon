package nl.esciencecenter.octopus.adaptors.local;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalJobs implements Jobs {

    private static final Logger logger = LoggerFactory.getLogger(LocalJobs.class);

    private final OctopusEngine octopusEngine;

    private final Map<Scheduler, LocalScheduler> schedulers = new HashMap<Scheduler, LocalScheduler>();

    private final LocalAdaptor localAdaptor;

    private static int jobID = 0;

    private static synchronized int getNextSchedulerID() {
        return jobID++;
    }

    public LocalJobs(OctopusProperties properties, LocalAdaptor localAdaptor, OctopusEngine octopusEngine)
            throws OctopusException {
        this.octopusEngine = octopusEngine;
        this.localAdaptor = localAdaptor;
    }
      
    @Override
    public Scheduler newScheduler(URI location, Credential credential, Properties properties) throws OctopusException,
            OctopusIOException {

        localAdaptor.checkURI(location);

        if (location.getPath() != null && location.getPath().length() > 0) {
            throw new OctopusException("local", "Non-empty path in a local scheduler URI is not allowed");
        }

        int id = getNextSchedulerID();
        
        OctopusProperties p = new OctopusProperties(properties);
        
        Scheduler scheduler = new SchedulerImplementation("LocalScheduler" + id, LocalAdaptor.ADAPTOR_NAME, location, p);
        
        LocalScheduler local = new LocalScheduler(scheduler);        
        schedulers.put(scheduler, local);
        return scheduler;
    }

    @Override
    public JobDescription newJobDescription() {
        // FIXME: Not needed ? 
        return null;
    }

    private LocalScheduler getLocalScheduler(Scheduler scheduler) throws OctopusException { 
        
        if (scheduler == null) { 
            throw new OctopusException("Scheduler is null!", LocalAdaptor.ADAPTOR_NAME);
        }
        
        LocalScheduler tmp = schedulers.get(scheduler);
        
        if (tmp == null) { 
            throw new OctopusException("Scheduler not found " + scheduler.getAdaptorName(), 
                    LocalAdaptor.ADAPTOR_NAME);
        }
        
        return tmp;
    }
    
    @Override
    public String[] getQueueNames(Scheduler scheduler) throws OctopusException {
        return getLocalScheduler(scheduler).getQueueNames();
    }

    @Override
    public Job[] getJobs(Scheduler scheduler, String queueName) throws OctopusException, OctopusIOException {
        return getLocalScheduler(scheduler).getJobs(queueName);
    }

    @Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException {
        return getLocalScheduler(scheduler).submitJob(description);
    }

    @Override
    public JobStatus getJobStatus(Job job) throws OctopusException {
        return getLocalScheduler(job.getScheduler()).getJobStatus(job);
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) {

        // FIXME: optimize
        JobStatus[] result = new JobStatus[jobs.length];
        
        for (int i=0;i<jobs.length;i++) { 
            try {
                result[i] = getJobStatus(jobs[i]);
            } catch (OctopusException e) {
                result[i] = new JobStatusImplementation(jobs[i], null, null, e, false, null); 
            }
        }
        
        return result;
    }

    @Override
    public void cancelJob(Job job) throws OctopusException {
        getLocalScheduler(job.getScheduler()).cancelJob(job);
    }
}