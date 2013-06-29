package nl.esciencecenter.octopus.adaptors.gridengine;

import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.QueueStatus;

public interface CommandLineInterface {

    public String[] getQueueNames(SchedulerConnection connection) throws OctopusIOException, OctopusException;

    public QueueStatus getQueueStatus(SchedulerConnection connection, String queueName) throws OctopusIOException, OctopusException;

    public QueueStatus[] getQueueStatuses(SchedulerConnection connection, String... queueNames)
            throws OctopusIOException, OctopusException;

    public Job[] getJobs(SchedulerConnection connection, String... queueNames) throws OctopusIOException,
    OctopusException;

    public Job submitJob(SchedulerConnection connection, JobDescription description) throws OctopusIOException,
            OctopusException;

    public JobStatus cancelJob(SchedulerConnection connection, Job job) throws OctopusIOException, OctopusException;

    public JobStatus getJobStatus(SchedulerConnection connection, Job job) throws OctopusException, OctopusIOException;

    public JobStatus[] getJobStatuses(SchedulerConnection connection, Job... jobs) throws OctopusIOException,
            OctopusException;

}