package nl.esciencecenter.octopus.jobs;

import java.net.URI;
import java.util.Properties;

/**
 * Scheduler represents a (possibly remote) scheduler that can be used to submit jobs and retrieve queue information.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface Scheduler {

    /** 
     * Get the name of the adaptor attached to this Scheduler.
     * 
     * @return the name of the adaptor.
     */
    public String getAdaptorName();
    
    /**
     * Get the URI representing the location of the Scheduler.
     *  
     * @return the location of the Scheduler.
     */
    public URI getUri();

    /** 
     * Get the properties used to create this Scheduler.  
     * 
     * @return the properties used to create this Scheduler. 
     */
    public Properties getProperties();

    /** 
     * Get the queue names supported by this Scheduler.  
     *  
     * @return the queue names supported by this Scheduler.
     */
    public String[] getQueueNames();
}
