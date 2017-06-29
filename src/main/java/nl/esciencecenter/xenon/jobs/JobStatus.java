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
package nl.esciencecenter.xenon.jobs;

import java.util.Map;

/**
 * JobStatus contains status information for a specific {@link Job}.
 * 
 * @version 1.0
 * @since 1.0
 */
public class JobStatus {

    private final Job job;
    private final String state;
    private final Integer exitCode;
    private final Exception exception;
    private final boolean running;
    private final boolean done;
    private final Map<String, String> schedulerSpecificInformation;

    /**
     * Create a JobStatus. 
     * 
     * @param job
     * 		the <code>Job</code> for which this status was created.
     * @param state
     * 		the state of the <code>Job</code> at the time this status was created.
     * @param exitCode
     * 		the exit code of the <code>Job</code> (if the jobs has finished).
     * @param exception
     * 		the exception produced when running <code>Job</code> (if the jobs has failed).
     * @param running
     * 		is the <code>Job</code> running ?
     * @param done
     * 		is the <code>Job</code> finished ?
     * @param schedulerSpecificInformation
     * 		a map of scheduler implementation specific information on the job.
     */
    public JobStatus(Job job, String state, Integer exitCode, Exception exception, boolean running, boolean done,
            Map<String, String> schedulerSpecificInformation) {

        if (job == null) {
            throw new IllegalArgumentException("Job may not be null!");
        }

        this.job = job;
        this.state = state;
        this.exitCode = exitCode;
        this.exception = exception;
        this.running = running;
        this.done = done;
        this.schedulerSpecificInformation = schedulerSpecificInformation;
    }

    /**
     * Get the job for which this JobStatus was created.
     * 
     * @return the Job.
     */
    public Job getJob() {
        return job;
    }

    /**
     * Get the state of the Job.
     * 
     * @return the state of the Job.
     */
    public String getState() {
        return state;
    }

    /**
     * Get the exit code for the Job.
     * 
     * @return the exit code for the Job.
     */
    public Integer getExitCode() {
        return exitCode;
    }
    
    /**
     * Get the exception produced by the Job or while retrieving the status. If a job was canceled, will return a
     * JobCanceledException.
     * 
     * @return the exception.
     */
    public Exception getException() {
        return exception;
    }

    /**
     * Is the Job running.
     * 
     * @return if the Job is running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Is the Job done.
     * 
     * @return if the Job is done.
     */
    public boolean isDone() {
        return done;
    }

    /**
     * Has the Job or job retrieval produced a exception ?
     * 
     * @return if the Job has an exception.
     */
    public boolean hasException() {
        return (exception != null);
    }

    /**
     * Get scheduler specific information on the Job.
     * 
     * @return scheduler specific information on the Job.
     */
    public Map<String, String> getSchedulerSpecficInformation() {
        return schedulerSpecificInformation;
    }
    
    @Override
    public String toString() {
        return "JobStatus [job=" + job + ", state=" + state + ", exitCode=" + exitCode + ", exception=" + exception
                + ", running=" + running + ", done=" + done + ", schedulerSpecificInformation=" + schedulerSpecificInformation
                + "]";
    }
}
