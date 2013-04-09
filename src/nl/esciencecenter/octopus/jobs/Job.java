package nl.esciencecenter.octopus.jobs;

public interface Job {

    /**
     * Returns the {@link JobDescription} that was used to create this Job.
     * 
     * @return the JobDescription that belongs to this Job
     */
    public JobDescription getJobDescription();

    /**
     * Returns the {@link Scheduler} that was used to create this Job.
     * 
     * @return the Scheduler used to create this job.
     */
    public Scheduler getScheduler();

    /**
     * Returns the identifier that was assigned to this job by the scheduler.
     * 
     * @return the identifier that was assigned to this job by the scheduler.
     */
    public String getIdentifier();
}
