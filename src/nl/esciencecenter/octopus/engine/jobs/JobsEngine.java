package nl.esciencecenter.octopus.engine.jobs;

import java.net.URI;
import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.Scheduler;

public class JobsEngine implements Jobs {

    private final OctopusEngine octopusEngine;

    public JobsEngine(OctopusEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
    }

    @Override
    public Scheduler newScheduler(URI location) throws OctopusException {
        return newScheduler(null, null, location);
    }
    
    private Adaptor getAdaptor(Scheduler scheduler) throws OctopusException {
        return octopusEngine.getAdaptor(scheduler.getAdaptorName());
    }
    
    @Override
    public Scheduler newScheduler(Properties properties, Credentials credentials, URI location) throws OctopusException {
        Adaptor adaptor = octopusEngine.getAdaptorFor(location.getScheme());

        return adaptor.jobsAdaptor().newScheduler(octopusEngine.getCombinedProperties(properties),
                location);
    }

    @Override
	public String[] getQueueNames(Scheduler scheduler) throws OctopusException {
		return getAdaptor(scheduler).jobsAdaptor().getQueueNames(scheduler);
	}

	@Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException { 
		return getAdaptor(scheduler).jobsAdaptor().submitJob(scheduler, description);
    }

	@Override
    public Job[] submitJobs(Scheduler scheduler, JobDescription... descriptions) throws OctopusException { 
		return getAdaptor(scheduler).jobsAdaptor().submitJobs(scheduler, descriptions);
    }

	@Override
	public Job[] getJobs(Scheduler scheduler, String queueName) throws OctopusException {
		return getAdaptor(scheduler).jobsAdaptor().getJobs(scheduler, queueName);
	}

	@Override
	public JobStatus getJobStatus(Job job) throws OctopusException {
		return getAdaptor(job.getScheduler()).jobsAdaptor().getJobStatus(job);
	}

	@Override
	public JobStatus[] getJobStatuses(Job... jobs) throws OctopusException {
		
		JobStatus[] result = new JobStatus[jobs.length];
		
		// FIXME: Optimize!
		
		for (int i=0;i<jobs.length;i++) { 
			result[i] = getJobStatus(jobs[i]);
		} 
		
		return result;
	}

	@Override
	public void cancelJob(Job job) throws OctopusException {
		getAdaptor(job.getScheduler()).jobsAdaptor().cancelJob(job);
	}

	@Override
	public void cancelJobs(Job... jobs) throws OctopusException {
		for (int i=0;i<jobs.length;i++) { 
			cancelJob(jobs[i]);
		} 
	}
}