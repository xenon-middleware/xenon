package nl.esciencecenter.octopus.engine.jobs;

import java.net.URI;
import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;

public class JobsEngine implements Jobs {

    private final OctopusEngine octopusEngine;

    public JobsEngine(OctopusEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
    }

    private Adaptor getAdaptor(Scheduler scheduler) throws OctopusException {
        return octopusEngine.getAdaptor(scheduler.getAdaptorName());
    }

    public Scheduler newScheduler(URI location, Credential credential, Properties properties) 
            throws OctopusException, OctopusIOException { 
        
        Adaptor adaptor = octopusEngine.getAdaptorFor(location.getScheme());
        return adaptor.jobsAdaptor().newScheduler(location, credential, properties);
    }

    @Override
    public Scheduler getLocalScheduler() throws OctopusException, OctopusIOException {
        Adaptor adaptor = octopusEngine.getAdaptorFor(OctopusEngine.LOCAL_ADAPTOR_NAME);
        return adaptor.jobsAdaptor().getLocalScheduler();
    }
    
    @Override
    public JobStatus getJobStatus(Job job) throws OctopusException {
        return getAdaptor(job.getScheduler()).jobsAdaptor().getJobStatus(job);
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) {

        // FIXME: Optimize!

        JobStatus[] result = new JobStatus[jobs.length];
        
        for (int i = 0; i < jobs.length; i++) {
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
        getAdaptor(job.getScheduler()).jobsAdaptor().cancelJob(job);
    }

    @Override
    public JobDescription newJobDescription() {
        return new JobDescriptionImplementation();
    }

    @Override
    public Job[] getJobs(Scheduler scheduler, String queueName) throws OctopusException, OctopusIOException {
        return getAdaptor(scheduler).jobsAdaptor().getJobs(scheduler, queueName);
    }
    
    @Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException {
        return getAdaptor(scheduler).jobsAdaptor().submitJob(scheduler, description);
    }

    @Override
    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws OctopusException {
        return getAdaptor(scheduler).jobsAdaptor().getQueueStatus(scheduler, queueName);
    }

    @Override
    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws OctopusException {
        return getAdaptor(scheduler).jobsAdaptor().getQueueStatuses(scheduler, queueNames);
    }

    

}
