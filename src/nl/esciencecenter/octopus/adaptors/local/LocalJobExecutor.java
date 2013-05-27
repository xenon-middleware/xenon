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
package nl.esciencecenter.octopus.adaptors.local;

import java.io.IOException;

import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.exceptions.BadParameterException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalJobExecutor implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(LocalJobExecutor.class);
    
    private final JobImplementation job;
    
    private final int pollingDelay;

    private Streams streams;
    
    private Integer exitStatus;

    private Thread thread = null;
    
    private boolean killed = false;
    private boolean done = false;
    private boolean hasRun = false;
    
    private String state = "PENDING";
    
    private Exception error;

    public LocalJobExecutor(JobImplementation job, int pollingDelay) throws BadParameterException {
        this.job = job;
        this.pollingDelay = pollingDelay;
    }

    public synchronized boolean hasRun() {
        return hasRun;
    }
    
    public synchronized void kill() throws OctopusException {
        killed = true;
        
        if (thread != null) {
            thread.interrupt();
        }
    }

    public synchronized boolean isDone() {
        return done;
    }

    public Job getJob() {
        return job;
    }

    public synchronized JobStatus getStatus() {
        return new JobStatusImplementation(job, state, exitStatus, error, state.equals("RUNNING"), done, null);
    }

    public synchronized String getState() {
        return state;
    }

    public synchronized Exception getError() {
        return error;
    }

    private synchronized void updateState(String state, int exitStatus, Exception e) {
        
        if (state.equals("ERROR") || state.equals("KILLED")) { 
            error = e;
            done = true;        
        } else if (state.equals("DONE")) { 
            this.exitStatus = exitStatus;
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

    private synchronized void setStreams(Streams streams) { 
        this.streams = streams;
    }
    
    public synchronized Streams getStreams() throws OctopusException { 
        
        if (job.getJobDescription().isInteractive()) { 
            return streams;
        } 
        
        throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Job is not interactive!");
    }

    public synchronized void waitUntilRunning() {

        while (state.equals("PENDING")) { 
            try { 
                wait();
            } catch (InterruptedException e) { 
                // ignored
            }
        }
    }
    
    @Override
    public void run() {

        LocalProcess process = null;

        JobDescription description = job.getJobDescription();
        
        
        if (getKilled()) {
            updateState("KILLED", -1, new IOException("Process cancelled by user."));
            return;
        }
        
        
        this.thread = Thread.currentThread();
        
        long endTime = 0;
        int maxTime = description.getMaxTime();
        
        if (maxTime > 0) { 
            endTime = System.currentTimeMillis() + maxTime * 60 * 1000;
        }
        
        if (description.isInteractive()) { 
            try {             
                InteractiveProcess tmp = new InteractiveProcess(job);
                setStreams(tmp.getStreams());
                process = tmp; 
            } catch (IOException e) {
                updateState("ERROR", -1, e);
                return;
            }

        } else { 
            try {             
                process = new BatchProcess(job.getJobDescription());
            } catch (IOException e) {
                updateState("ERROR", -1, e);
                return;
            }
        }
        
        updateState("RUNNING", -1, null);
            
        while (true) { 

            if (process.isDone()) {
                updateState("DONE", process.getExitStatus(), null);
                return;
            }

            if (getKilled()) {
                updateState("KILLED", -1, new IOException("Process cancelled by user."));
                process.destroy();
                return;
            }
               
            if (maxTime > 0 && System.currentTimeMillis() > endTime) {
                updateState("KILLED", -1, new IOException("Process timed out."));
                process.destroy();
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
