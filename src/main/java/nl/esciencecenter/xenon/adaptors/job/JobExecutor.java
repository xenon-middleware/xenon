/**
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
package nl.esciencecenter.xenon.adaptors.job;

import java.io.IOException;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.jobs.JobHandle;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.Streams;
//import nl.esciencecenter.xenon.util.Utils;

/**
 * 
 */
public class JobExecutor implements Runnable {

    private static final String PENDING_STATE = "PENDING"; 
    private static final String RUNNING_STATE = "RUNNING"; 
    private static final String DONE_STATE = "DONE"; 
    private static final String ERROR_STATE = "ERROR"; 
    private static final String KILLED_STATE = "KILLED";
    
    /** Polling delay in ms. */
    private static final long POLLING_DELAY = 1000L;
    
    /** Number of ms. per min. */
    private static final long MILLISECONDS_IN_MINUTE = 60L * 1000L;

    private final JobImplementation job;

    private final InteractiveProcessFactory factory;

    private final long pollingDelay;

    private final String adaptorName;

    private final FileSystem filesystem;
    private final Path workingDirectory;

    private Streams streams;

    private Integer exitStatus;

    private boolean updateSignal = false;
    private boolean isRunning = false;
    private boolean killed = false;
    private boolean done = false;
    private boolean hasRun = false;

    private String state = PENDING_STATE;

    private Exception error;

    public JobExecutor(String adaptorName, FileSystem filesystem, Path workingDirectory, InteractiveProcessFactory factory,
            JobImplementation job, long pollingDelay) {

        this.adaptorName = adaptorName;
        this.filesystem = filesystem;
        this.workingDirectory = workingDirectory;
        this.job = job;
        this.factory = factory;
        this.pollingDelay = pollingDelay;
    }

    public synchronized boolean hasRun() {
        return hasRun;
    }

    public synchronized boolean kill() {
        if (done) {
            return true;
        }

        killed = true;

        if (!isRunning) {
            updateState(KILLED_STATE, -1, new JobCanceledException(adaptorName, "Process cancelled by user."));
            return true;
        }

        return false;
    }

    public synchronized boolean isDone() {
        return done;
    }

    public JobHandle getJob() {
        return job;
    }

    public synchronized JobStatus getStatus() {
        if (!done && RUNNING_STATE.equals(state)) {
            triggerStatusUpdate();
            waitForStatusUpdate(pollingDelay);
        }

        return new JobStatus(job, state, exitStatus, error, RUNNING_STATE.equals(state), done, null);
    }

    public synchronized String getState() {
        return state;
    }

    public synchronized Exception getError() {
        return error;
    }

    private synchronized void updateState(String state, int exitStatus, Exception e) {

        if (ERROR_STATE.equals(state) || KILLED_STATE.equals(state)) {
            error = e;
            done = true;
        } else if (DONE_STATE.equals(state)) {
            this.exitStatus = exitStatus;
            done = true;
        } else if (RUNNING_STATE.equals(state)) {
            hasRun = true;
        } else {
            throw new InternalError("Illegal state: " + state);
        }

        this.state = state;
        clearUpdateRequest();
    }

    private synchronized boolean getKilled() {
        isRunning = true;
        return killed;
    }

    private synchronized void setStreams(Streams streams) {
        this.streams = streams;
    }

    public synchronized Streams getStreams() throws XenonException {

        if (job.getJobDescription().isInteractive()) {
            return streams;
        }

        throw new XenonException(adaptorName, "Job is not interactive!");
    }
        
    public synchronized JobStatus waitUntilRunning(long timeout) {

        long deadline = Deadline.getDeadline(timeout);
                
        triggerStatusUpdate();

        long leftover = deadline - System.currentTimeMillis();
        
        while (leftover > 0 && PENDING_STATE.equals(state)) {
            try {
                wait(leftover);
            } catch (InterruptedException e) {
                // We were interrupted
                Thread.currentThread().interrupt();
                break;
            }
            
            leftover = deadline - System.currentTimeMillis();
        }

        return getStatus();
    }

    public synchronized JobStatus waitUntilDone(long timeout) {

        long deadline = Deadline.getDeadline(timeout);
        
        triggerStatusUpdate();

        long leftover = deadline - System.currentTimeMillis();
        
        while (leftover > 0 && !done) {
            
            try {
                wait(leftover);
            } catch (InterruptedException e) {
                // We were interrupted
                Thread.currentThread().interrupt();
                break;
            }
            
            leftover = deadline - System.currentTimeMillis();

        }

        return getStatus();
    }

    /**
     * Signal the polling thread to produce a status update.
     */
    private synchronized void triggerStatusUpdate() {
        if (done) {
            return;
        }

        updateSignal = true;
        notifyAll();
    }

    /**
     * Wait for a certain amount of time for an update.
     * 
     * @param maxDelay
     *            the maximum time to wait
     */
    private synchronized void waitForStatusUpdate(long maxDelay) {

        if (done || !updateSignal) {
            return;
        }
        
        long deadline = Deadline.getDeadline(maxDelay);
        
        long left = maxDelay > 0 ? maxDelay : POLLING_DELAY; 
        
        while (!done && updateSignal && left > 0) { 
            try {
                wait(left);
            } catch (InterruptedException e) {
                // We were interrupted
                Thread.currentThread().interrupt();
                break;
            }

            if (maxDelay > 0) { 
                left = deadline - System.currentTimeMillis();
            } 
        }
    }
    
    /**
     * Clear the update signal and wake up any waiting threads
     */
    private synchronized void clearUpdateRequest() {
        updateSignal = false;
        notifyAll();
    }

    /**
     * Sleep for a certain amount of time, provide the job is not done, and no one requested an update.
     * 
     * @param maxDelay
     *            the maximum amount of time to wait
     */
    private synchronized void sleep(long maxDelay) {

        if (done || updateSignal || maxDelay <= 0) {
            return;
        }
        
        long deadline = Deadline.getDeadline(maxDelay);
        
        long left = deadline - System.currentTimeMillis();
        
        while (!done && !updateSignal && left > 0) { 
            try {
                wait(left);
            } catch (InterruptedException e) {
                // We were interrupted
                Thread.currentThread().interrupt();
                break;
            }

            left = deadline - System.currentTimeMillis();
        }
    }

    @Override
    public void run() {
        Process process;

        JobDescription description = job.getJobDescription();

        if (getKilled()) {
            updateState(KILLED_STATE, -1, new JobCanceledException(adaptorName, "Process cancelled by user."));
            return;
        }

        long endTime = 0;
        int maxTime = description.getMaxTime();

        if (maxTime > 0) {
            endTime = System.currentTimeMillis() + maxTime * MILLISECONDS_IN_MINUTE;
        }

        try {
            if (description.isInteractive()) {
                InteractiveProcess p = factory.createInteractiveProcess(job);
                setStreams(p.getStreams());
                process = p; 
            } else {
                process = new BatchProcess(filesystem, workingDirectory, job, factory);
            }
        } catch (IOException | XenonException e) {
            updateState(ERROR_STATE, -1, e);
            return;
        }

        updateState(RUNNING_STATE, -1, null);

        while (true) {

            if (process.isDone()) {
                updateState(DONE_STATE, process.getExitStatus(), null);
                return;
            }

            if (getKilled()) {
                // Destroy first, update state last, otherwise we have a race condition!
                process.destroy();
                updateState(KILLED_STATE, -1, new JobCanceledException(adaptorName, "Process cancelled by user."));                
                return;
            }

            if (maxTime > 0 && System.currentTimeMillis() > endTime) {
                // Destroy first, update state last, otherwise we have a race condition!
                process.destroy();
                updateState(KILLED_STATE, -1, new JobCanceledException(adaptorName, "Process timed out."));
                return;
            }

            clearUpdateRequest();

            sleep(pollingDelay);
        }
    }

}
