package nl.esciencecenter.octopus.jobs;

import java.util.Map;

/**
 * QueueStatus contains status information for a specific queue.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface QueueStatus {
    
    /** 
     * Get the Scheduler that produced this QueueStatus.
     * 
     * @return the Scheduler.
     */
    public Scheduler getScheduler();
    
    /** 
     * Get the queue name.
     * 
     * @return the queue name.
     */
    public String getQueueName();
    
    /** 
     * Did the queue produce an exception ?
     * 
     * @return if the queue produced an exception ?
     */
    public boolean hasException();

    /**
     * Get the exception produced by the queue, or <code>null</code> if <code>hasEsception()</code> returns <code>false</code>. 
     * 
     * @return the exception.
     */
    public Exception getException();
    
    /** 
     * Get scheduler specific information on the queue. 
     *  
     * @return cheduler specific information on the queue. 
     */
    public Map<String, String> getSchedulerSpecficInformation();    
}
