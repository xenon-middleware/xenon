package nl.esciencecenter.octopus.engine.jobs;

import java.util.Map;

import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;

public class QueueStatusImplementation implements QueueStatus {

    private final Scheduler scheduler;
    private final String queueName;
    private final Exception exception;
    private final Map<String, String> schedulerSpecificInformation;
    
    public QueueStatusImplementation(Scheduler scheduler, String queueName, Exception exception, 
            Map<String, String> schedulerSpecificInformation) {
        
        super();
        
        this.scheduler = scheduler;
        this.queueName = queueName;
        this.exception = exception;
        this.schedulerSpecificInformation = schedulerSpecificInformation;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public String getQueueName() {
        return queueName;
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
