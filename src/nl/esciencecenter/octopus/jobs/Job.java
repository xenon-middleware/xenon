package nl.esciencecenter.octopus.jobs;

import nl.esciencecenter.octopus.exceptions.OctopusException;

public interface Job {

    /**
     * Returns the {@link JobDescription} that was used to create this Job.
     * 
     * @return the JobDescription that belongs to this Job
     */
    public JobDescription getJobDescription();

    /**
     * This method returns the state of the Job.
     * 
     * @return This method returns the state of the associated Job
     */
    public JobState getState();

    /**
     * Register a state listener for this job. Whenever the state of this job is
     * updated, the listener will be called.
     * 
     * @param listener
     */
    public void registerStateLister(JobStateListener listener);
    
    public void unRegisterStateLister(JobStateListener listener);

    /**
     * Returns the exit status of a job.
     * 
     * @return the exit status of a job.
     * 
     */
    public int getExitStatus() throws OctopusException;

    /**
     * In case of an error while running a job, this function will return the
     * error.
     * 
     */
    public Exception getError();

    /**
     * Will forcibly stop a job.
     */
    public void kill() throws OctopusException;

    public boolean isDone() throws OctopusException;
}
