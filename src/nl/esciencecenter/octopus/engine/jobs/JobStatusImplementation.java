package nl.esciencecenter.octopus.engine.jobs;

import java.util.Map;

import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobStatus;

public final class JobStatusImplementation implements JobStatus {

    private final Job job;
    private final String state;
    private final Integer exitCode;
    private final Exception exception;
    private final boolean done;
    private final Map<String, String> schedulerSpecificInformation;
    
    public JobStatusImplementation(Job job, String state, Integer exitCode, Exception error, boolean done, 
            Map<String, String> schedulerSpecificInformation) {
        
        super();
    
        this.job = job;
        this.state = state;
        this.exitCode = exitCode;
        this.exception = error;
        this.done = done;
        this.schedulerSpecificInformation = schedulerSpecificInformation;
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
    public boolean isDone() {
        return done;
    }

    @Override
    public Exception getException() {
        return exception;
    }

    @Override
    public boolean hasException() {
        return (exception != null);
    }

    @Override
    public Map<String, String> getSchedulerSpecficInformation() {
        return schedulerSpecificInformation;
    }
}
