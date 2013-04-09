package nl.esciencecenter.octopus.engine.jobs;

import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.Scheduler;

public class JobImplementation implements Job {

    private final JobDescription description;

    private final Scheduler scheduler;

    private final String identifier;

    public JobImplementation(JobDescription description, Scheduler scheduler, String identifier) {
        this.description = description;
        this.scheduler = scheduler;
        this.identifier = identifier;
    }

    @Override
    public JobDescription getJobDescription() {
        return description;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }
}
