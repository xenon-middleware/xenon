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

package nl.esciencecenter.octopus.engine.util;

import java.io.IOException;

import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.exceptions.BadParameterException;
import nl.esciencecenter.octopus.exceptions.JobCanceledException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Streams;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class JobExecutor implements Runnable {

    private final JobImplementation job;

    private final InteractiveProcessFactory factory;

    private final int pollingDelay;

    private final String adaptorName;

    private final Files files;
    private final FileSystem filesytem;

    private Streams streams;

    private Integer exitStatus;

    private Thread thread = null;

    private boolean killed = false;
    private boolean done = false;
    private boolean hasRun = false;

    private String state = "PENDING";

    private Exception error;

    public JobExecutor(String adaptorName, Files files, FileSystem filesytem, InteractiveProcessFactory factory,
            JobImplementation job, int pollingDelay) throws BadParameterException {

        this.adaptorName = adaptorName;
        this.files = files;
        this.filesytem = filesytem;
        this.job = job;
        this.factory = factory;
        this.pollingDelay = pollingDelay;
    }

    public synchronized boolean hasRun() {
        return hasRun;
    }

    public synchronized void kill() throws OctopusException {

        if (done) {
            return;
        }

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

        throw new OctopusException(adaptorName, "Job is not interactive!");
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

    /**
     * @param timeout
     * @return
     */
    public synchronized JobStatus waitUntilDone(long timeout) {

        long deadline = System.currentTimeMillis() + timeout;
        long leftover = timeout;

        while (!done) {

            try {
                wait(leftover);
            } catch (InterruptedException e) {
                // ignored
            }

            long now = System.currentTimeMillis();

            if (now >= deadline) {
                break;
            }

            leftover = deadline - now;
        }

        return getStatus();
    }

    @Override
    public void run() {

        InteractiveProcess process = null;

        JobDescription description = job.getJobDescription();

        if (getKilled()) {
            updateState("KILLED", -1, new JobCanceledException(adaptorName, "Process cancelled by user."));
            return;
        }

        this.thread = Thread.currentThread();

        long endTime = 0;
        int maxTime = description.getMaxTime();

        if (maxTime > 0) {
            endTime = System.currentTimeMillis() + maxTime * 60 * 1000;
        }

        try {
            if (description.isInteractive()) {
                process = factory.createInteractiveProcess(job);
                setStreams(process.getStreams());
            } else {
                process = new BatchProcess(files, filesytem, job, factory);
            }
        } catch (Exception e) {
            updateState("ERROR", -1, e);
            return;
        }

        updateState("RUNNING", -1, null);

        while (true) {

            if (process.isDone()) {
                updateState("DONE", process.getExitStatus(), null);
                return;
            }

            if (getKilled()) {
                updateState("KILLED", -1, new JobCanceledException(adaptorName, "Process cancelled by user."));
                process.destroy();
                return;
            }

            if (maxTime > 0 && System.currentTimeMillis() > endTime) {
                updateState("KILLED", -1, new JobCanceledException(adaptorName, "Process timed out."));
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
