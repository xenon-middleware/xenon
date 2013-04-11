package nl.esciencecenter.octopus.jobs;

import java.util.Map;

public interface QueueStatus {
    
    public Scheduler getScheduler();
    
    public String getQueueName();
    
    public boolean hasException();
    
    public Exception getException();
    
    public Map<String, String> getSchedulerSpecficInformation();    
}
