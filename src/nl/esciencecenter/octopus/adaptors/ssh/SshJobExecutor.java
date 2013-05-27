/*
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.esciencecenter.octopus.adaptors.ssh;

import java.io.IOException;

import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.octopus.exceptions.BadParameterException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Session;

public class SshJobExecutor implements Runnable {

    protected static Logger logger = LoggerFactory.getLogger(SshJobExecutor.class);

    private final JobImplementation job;

    private final int pollingDelay;
    
    private Integer exitCode;
    
    private Thread thread = null;

    private boolean killed = false;
    private boolean done = false;
    private boolean hasRun = false;
    
    private String state = "PENDING";

    private Exception error;

    private SshAdaptor adaptor;
    private SchedulerImplementation scheduler;
    private Session session;

    private Streams streams;
    
    public SshJobExecutor(SshAdaptor adaptor, SchedulerImplementation scheduler, Session session, JobImplementation job, 
            int pollingDelay) throws BadParameterException {
        
        this.job = job;
        this.adaptor = adaptor;
        this.scheduler = scheduler;
        this.session = session;
        this.pollingDelay = pollingDelay;

        if (job.getJobDescription().getProcessesPerNode() != 1) {
            throw new BadParameterException(adaptor.getName(), "number of processes can only be 1");
        }

        if (job.getJobDescription().getNodeCount() != 1) {
            throw new BadParameterException(adaptor.getName(), "number of nodes must be 1");
        }

        if (job.getJobDescription().getWorkingDirectory() != null && !job.getJobDescription().getWorkingDirectory().equals("")) {
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

    public synchronized void kill() throws OctopusException {
        killed = true;

        if (thread != null) {
            thread.interrupt();
        }
    }
    
    private synchronized void updateState(String state, int exitCode, Exception e) {
        
        if (state.equals("ERROR") || state.equals("KILLED")) { 
            error = e;
            done = true;        
        } else if (state.equals("DONE")) { 
            this.exitCode = exitCode;
            done = true;
        } else if (state.equals("RUNNING")) { 
            hasRun = true;
        } else { 
            throw new RuntimeException("INTERNAL ERROR: Illegal state: " + state);
        }
        
        this.state = state;
        notifyAll();
    }
    
    private synchronized boolean getKilled() {
        return killed;
    }

    public synchronized boolean isDone() {
        return done;
    }
    
    public synchronized boolean hasRun() {
        return hasRun;
    }
    
    public Job getJob() {
        return job;
    }

    public synchronized JobStatus getStatus() {
        return new JobStatusImplementation(job, state, exitCode, error, state.equals("RUNNING"), done, null);
    }

    public synchronized String getState() {
        return state;
    }

    public synchronized Exception getError() {
        return error;
    }

    private synchronized void setStreams(Streams streams) {
        this.streams = streams; 
    }
    
    public synchronized Streams getStreams() throws OctopusException { 
        
        if (job.getJobDescription().isInteractive()) { 
            return streams;
        } 
        
        throw new OctopusException(adaptor.getName(), "Job is not interactive!");
    }
    
    @Override
    public void run() {

        if (getKilled()) { 
            updateState("KILLED", -1, new IOException("Job killed"));
            return;
        }

        this.thread = Thread.currentThread();

        JobDescription description = job.getJobDescription();

        long endTime = 0;
        int maxTime = description.getMaxTime();
        
        if (maxTime > 0) { 
            endTime = System.currentTimeMillis() + maxTime * 60 * 1000;
        }
        
        SshProcess sshProcess =
                new SshProcess(adaptor, scheduler, session, job, description.getExecutable(), description.getArguments(),
                        description.getEnvironment(), description.getStdin(), description.getStdout(),
                        description.getStderr(), description.isInteractive());

        if (description.isInteractive()) { 
            setStreams(sshProcess.getStreams());
        }

        updateState("RUNNING", -1, null);

        while (true) { 

            if (sshProcess.isDone()) {
                updateState("DONE", sshProcess.getExitStatus(), null);
                return;
            }

            if (getKilled()) {
                updateState("KILLED", -1, new IOException("Process cancelled by user."));
                sshProcess.destroy();
                return;
            }
               
            if (maxTime > 0 && System.currentTimeMillis() > endTime) {
                updateState("KILLED", -1, new IOException("Process timed out."));
                sshProcess.destroy();
                return;
            }
                
            try { 
                Thread.sleep(pollingDelay);
            } catch (InterruptedException e) { 
                // ignored
            }
        }
    }
}
