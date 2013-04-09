package nl.esciencecenter.octopus.jobs;

public interface JobStatus {

    public Job getJob();

    public String getState();

    public Integer getExitCode();

    public Exception getError();

    public boolean isDone();

    public boolean hasError();
}
