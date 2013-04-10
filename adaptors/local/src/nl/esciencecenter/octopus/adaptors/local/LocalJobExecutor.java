package nl.esciencecenter.octopus.adaptors.local;

import java.io.IOException;

import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.exceptions.BadParameterException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalJobExecutor implements Runnable {

    protected static Logger logger = LoggerFactory.getLogger(LocalJobExecutor.class);

    private final Job job;

    private Integer exitCode;

    private Thread thread = null;

    private boolean killed = false;

    private boolean done = false;

    private String state = "INITIAL";

    private Exception error;

    public LocalJobExecutor(Job job) throws BadParameterException {

        this.job = job;

        if (job.getJobDescription().getProcessesPerNode() <= 0) {
            throw new BadParameterException("number of processes cannot be negative or 0", LocalAdaptor.ADAPTOR_NAME, null);
        }

        if (job.getJobDescription().getNodeCount() != 1) {
            throw new BadParameterException("number of nodes must be 1", LocalAdaptor.ADAPTOR_NAME, null);
        }

        // thread will be started by local scheduler
    }

    public synchronized int getExitStatus() throws OctopusException {
        if (!isDone()) {
            throw new OctopusException("Cannot get state, job not done yet", LocalAdaptor.ADAPTOR_NAME, null);
        }
        return exitCode;
    }

    private synchronized void setExitStatus(int exitStatus) {
        this.exitCode = exitStatus;
    }

    public synchronized void kill() throws OctopusException {
        killed = true;

        if (thread != null) {
            thread.interrupt();
        }
    }

    private synchronized void updateState(String state) {
        this.state = state;
    }

    private synchronized void setError(Exception e) {
        error = e;
    }

    public synchronized boolean isDone() {
        return done;
    }

    public Job getJob() {
        return job;
    }

    public synchronized JobStatus getStatus() {
        return new JobStatusImplementation(job, state, exitCode, error, done, null);
    }
    
    public synchronized String getState() {
        return state;
    }

    public synchronized Exception getError() {
        return error;
    }

    @Override
    public void run() {

        try {
            synchronized (this) {
                if (killed) {
                    updateState("KILLED");
                    throw new IOException("Job killed");
                }
                this.thread = Thread.currentThread();
            }

            updateState("INITIAL");

            if (Thread.currentThread().isInterrupted()) {
                updateState("KILLED");
                throw new IOException("Job killed");
            }

            JobDescription description = job.getJobDescription();

            ParallelProcess parallelProcess =
                    new ParallelProcess(description.getProcessesPerNode(), description.getExecutable(),
                            description.getArguments(), description.getEnvironment(), description.getWorkingDirectory(),
                            description.getStdin(), description.getStdout(), description.getStderr());

            updateState("RUNNING");

            try {
                setExitStatus(parallelProcess.waitFor());
            } catch (InterruptedException e) {
                parallelProcess.destroy();
            }

        } catch (IOException e) {
            setError(e);
            updateState("ERROR");
        }
    }
}
