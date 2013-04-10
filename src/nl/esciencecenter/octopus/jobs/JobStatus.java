package nl.esciencecenter.octopus.jobs;

import java.util.Map;

public interface JobStatus {

    public Job getJob();

    public String getState();

    public Integer getExitCode();

    public Exception getException();

    public boolean isDone();

    public boolean hasException();
    
    public Map<String, String> getSchedulerSpecficInformation();    
}
