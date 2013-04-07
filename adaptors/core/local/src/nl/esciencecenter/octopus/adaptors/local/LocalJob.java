package nl.esciencecenter.octopus.adaptors.local;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.jobs.JobCpi;
import nl.esciencecenter.octopus.engine.jobs.Sandbox;
import nl.esciencecenter.octopus.exceptions.BadParameterException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobState;
import nl.esciencecenter.octopus.jobs.JobStateListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalJob extends JobCpi implements Runnable {

    protected static Logger logger = LoggerFactory.getLogger(LocalJob.class);
    
    private final OctopusEngine engine;

    private int exitStatus;

    private Thread thread = null;

    private boolean killed = false;

    public LocalJob(JobDescription description, JobStateListener listener, OctopusEngine engine) throws BadParameterException {
        super(description, listener);
        this.engine = engine;

        if (description.getProcessesPerNode() <= 0) {
            throw new BadParameterException("number of processes cannot be negative or 0", "local", null);
        }
        
        if (description.getNodeCount() != 1) {
            throw new BadParameterException("number of nodes must be 1", "local", null);
        }

        // thread will be started by local scheduler
    }

    @Override
    public synchronized int getExitStatus() throws OctopusException {
        if (!isDone()) {
            throw new OctopusException("Cannot get state, job not done yet", "local", null);
        }
        return exitStatus;
    }

    private synchronized void setExitStatus(int exitStatus) {
        this.exitStatus = exitStatus;
    }

    @Override
    public synchronized void kill() throws OctopusException {
        killed = true;

        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        Sandbox sandbox = null;
        try {
            this.updateState(JobState.INITIAL);

            synchronized (this) {
                if (killed) {
                    throw new IOException("Job killed");
                }
                this.thread = Thread.currentThread();
            }

            this.updateState(JobState.PRE_STAGING);

            JobDescription description = this.getJobDescription();
            
            Path sandboxRoot = engine.files().newPath(new URI("local://~/.deploy-sandboxes/"));

            sandbox = new Sandbox(description, engine, sandboxRoot);

            sandbox.preStage();

            this.updateState(JobState.SCHEDULED);

            if (Thread.currentThread().isInterrupted()) {
                throw new IOException("Job killed");
            }

            ParallelProcess parallelProcess = new ParallelProcess(description.getProcessesPerNode(),
                    description.getExecutable(), description.getArguments(), description.getEnvironment(),
                    sandbox.getWorkingDirectory(), sandbox.getStdin(), sandbox.getStdout(), sandbox.getStderr(), engine);

            this.updateState(JobState.RUNNING);

            try {
                setExitStatus(parallelProcess.waitFor());
            } catch (InterruptedException e) {
                parallelProcess.destroy();
            }

            sandbox.postState();

        } catch (IOException | URISyntaxException e) {
            setError(e);

            if (sandbox != null) {
                try {
                    sandbox.postState();
                } catch (OctopusException e1) {
                    e.addSuppressed(e1);
                }
            }

            updateState(JobState.ERROR);
        }
    }
}
