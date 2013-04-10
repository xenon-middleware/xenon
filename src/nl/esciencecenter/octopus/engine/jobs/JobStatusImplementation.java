package nl.esciencecenter.octopus.engine.jobs;

import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobStatus;

public class JobStatusImplementation implements JobStatus {

    private final Job job;
    private final String state;
    private final Integer exitCode;
    private final Exception error;
    private final boolean done;
    
    public JobStatusImplementation(Job job, String state, Integer exitCode, Exception error, boolean done) {
        super();
    
        this.job = job;
        this.state = state;
        this.exitCode = exitCode;
        this.error = error;
        this.done = done;
    }

    @Override
    public Job getJob() {
        return job;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public Integer getExitCode() {
        return exitCode;
    }

    @Override
    public Exception getError() {
        return error;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean hasError() {
        return (error != null);
    }
}
