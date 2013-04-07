package nl.esciencecenter.octopus.jobs;

public interface JobStateListener {
    
    JobState stateUpdated(Job job, JobState state);
}
