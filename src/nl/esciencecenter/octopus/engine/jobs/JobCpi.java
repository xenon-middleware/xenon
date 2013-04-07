package nl.esciencecenter.octopus.engine.jobs;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobState;
import nl.esciencecenter.octopus.jobs.JobStateListener;

public abstract class JobCpi implements Job {
    
    protected static Logger logger = LoggerFactory.getLogger(JobCpi.class);

    private final JobDescription description;

    private JobState state;
    
    private final Set<JobStateListener> listeners;
    
    private Exception error = null;
    
    public JobCpi(JobDescription description, JobStateListener listener) {
        this.description = description;
        
        this.state = JobState.INITIAL;
        
        listeners = new HashSet<JobStateListener>();
    }

    @Override
    public JobDescription getJobDescription() {
        return description;
    }

    @Override
    public synchronized JobState getState() {
        return state;
    }
    
    @Override
    public synchronized boolean isDone() {
        return state.ordinal() >= JobState.DONE.ordinal();
    }

    protected void updateState(JobState state) {
        JobStateListener[] listeners;
        
        synchronized (this) {
            if (this.state == state) {
                return;
            }

            if (state.ordinal() < this.state.ordinal()) {
                logger.error("Warning: cannot set state backwards, ignoring");
                return;
            }

            this.state = state;
            listeners = this.listeners.toArray(new JobStateListener[0]);
        }
        
        for(JobStateListener listener: listeners) {
            listener.stateUpdated(this, state);
        }
    }

    @Override
    public synchronized void registerStateLister(JobStateListener listener) {
        listeners.add(listener);
    }

    @Override
    public synchronized void unRegisterStateLister(JobStateListener listener) {
        listeners.remove(listener);
    }
    
    @Override
    public synchronized Exception getError() {
        return error;
    }
    
    protected synchronized void setError(Exception error) {
        this.error = error;
    }
}
