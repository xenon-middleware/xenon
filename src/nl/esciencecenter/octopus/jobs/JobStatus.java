package nl.esciencecenter.octopus.jobs;

import java.util.Map;

/** 
 * JobStatus contains status information for a specific job.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0 
 */
public interface JobStatus {

    /** 
     * Get the job for which this JobStatus was created.
     * 
     * @return the Job.
     */
    public Job getJob();

    /** 
     * Get the state of the Job.  
     *   
     * @return the state of the Job. 
     */
    public String getState();

    /**
     * Get the exit code for the Job.
     * 
     * @return the exit code for the Job.
     */
    public Integer getExitCode();

    /** 
     * Get the exception produced by the Job or while retrieving the status.  
     * 
     * @return the exception.
     */
    public Exception getException();

    /** 
     * Is the Job done.
     * 
     * @return if the Job is done.
     */
    public boolean isDone();

    /** 
     * Has the Job or job retrieval produced a exception ? 
     * 
     * @return if the Job has an exception.
     */
    public boolean hasException();

    /** 
     * Get scheduler specific information on the Job. 
     * 
     * @return scheduler specific information on the Job.
     */
    public Map<String, String> getSchedulerSpecficInformation();    
}
