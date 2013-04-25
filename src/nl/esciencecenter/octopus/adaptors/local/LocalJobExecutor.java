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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalJobExecutor implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(LocalJobExecutor.class);
    
    private final JobImplementation job;
    
    private final int pollingDelay;

    private Integer exitStatus;

    private boolean killed = false;

    private boolean done = false;

    private String state = "INITIAL";

    private Exception error;

    public LocalJobExecutor(JobImplementation job, int pollingDelay) throws BadParameterException {
        this.job = job;
        this.pollingDelay = pollingDelay;
    }

    public synchronized void kill() throws OctopusException {
        killed = true;
    }

    public synchronized boolean isDone() {
        return done;
    }

    public Job getJob() {
        return job;
    }

    public synchronized JobStatus getStatus() {
        return new JobStatusImplementation(job, state, exitStatus, error, done, null);
    }

    public synchronized String getState() {
        return state;
    }

    public synchronized Exception getError() {
        return error;
    }

    private synchronized void updateState(String state) {
        this.state = state;
    }
    
    private synchronized void setError(Exception e) {
        state = "ERROR";
        error = e;
        done = true;
    }

    private synchronized boolean getKilled() { 
        return killed;
    }

    private synchronized void setDone(int exitStatus) {
        state = "DONE";
        this.exitStatus = exitStatus; 
        done = true;
    }
    
    private synchronized void setKilled(Exception e) {
        state = "KILLED";
        error = e;
        done = true;
    }
    
    @Override
    public void run() {

        LocalProcess process = null;

        JobDescription description = job.getJobDescription();
        
        long endTime = 0;
        int maxTime = description.getMaxTime();
        
        if (getKilled()) {
            setKilled(new IOException("Process cancelled by user."));
            return;
        }

        updateState("INITIAL");

        if (maxTime > 0) { 
            endTime = System.currentTimeMillis() + maxTime * 60 * 1000;
        }
        
        if (description.isInteractive()) { 
            try {             
                process = new InteractiveProcess(job);
            } catch (IOException e) {
                setError(e);
                return;
            }

        } else { 
            try {             
                process = new BatchProcess(job.getJobDescription());
            } catch (IOException e) {
                setError(e);
                return;
            }
        }
        
        updateState("RUNNING");
            
        while (true) { 

            if (process.isDone()) {
                setDone(process.getExitStatus());
                return;
            }

            if (getKilled()) {
                setKilled(new IOException("Process cancelled by user."));
                process.destroy();
                return;
            }
               
            if (maxTime > 0 && System.currentTimeMillis() > endTime) {
                setKilled(new IOException("Process timed out."));
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
