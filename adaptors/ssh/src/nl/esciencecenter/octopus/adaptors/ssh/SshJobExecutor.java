package nl.esciencecenter.octopus.adaptors.ssh;

import java.io.IOException;

import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.octopus.exceptions.BadParameterException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Session;

public class SshJobExecutor implements Runnable {

    protected static Logger logger = LoggerFactory.getLogger(SshJobExecutor.class);

    private final Job job;

    private Integer exitCode;

    private Thread thread = null;

    private boolean killed = false;

    private boolean done = false;

    private String state = "INITIAL";

    private Exception error;

    private SshAdaptor adaptor;
    private SchedulerImplementation scheduler;
    private Session session;

    public SshJobExecutor(SshAdaptor adaptor, SchedulerImplementation scheduler, Session session, Job job)
            throws BadParameterException {
        this.job = job;
        this.adaptor = adaptor;
        this.scheduler = scheduler;
        this.session = session;

        if (job.getJobDescription().getProcessesPerNode() != 1) {
            throw new BadParameterException(adaptor.getName(), "number of processes can only be 1");
        }

        if (job.getJobDescription().getNodeCount() != 1) {
            throw new BadParameterException(adaptor.getName(), "number of nodes must be 1");
        }

        if(job.getJobDescription().getWorkingDirectory() != null && !job.getJobDescription().getWorkingDirectory().equals("")) {
            throw new BadParameterException(adaptor.getName(), "cannot set working directory");
        }
            
        
        // thread will be started by local scheduler
    }

    public synchronized int getExitStatus() throws OctopusException {
        if (!isDone()) {
            throw new OctopusException("Cannot get state, job not done yet", adaptor.getName(), null);
        }
        return exitCode;
    }

    private synchronized void setExitStatus(int exitStatus) {
        this.exitCode = exitStatus;
        done = true;
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
        done = true;
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

            SshProcess sshProcess =
                    new SshProcess(adaptor, scheduler, session, description.getExecutable(),
                            description.getArguments(), description.getEnvironment(), 
                            description.getStdin(), description.getStdout(), description.getStderr());

            updateState("RUNNING");

            try {
                setExitStatus(sshProcess.waitFor());
            } catch (InterruptedException e) {
                sshProcess.destroy();
            }

            updateState("DONE");

        } catch (IOException e) {
            setError(e);
            updateState("ERROR");
        }
    }
}
