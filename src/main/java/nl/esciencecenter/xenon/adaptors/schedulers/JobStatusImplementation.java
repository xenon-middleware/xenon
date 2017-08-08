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
package nl.esciencecenter.xenon.adaptors.schedulers;

import java.util.Map;
import java.util.Objects;

import nl.esciencecenter.xenon.schedulers.JobStatus;

/**
 * JobStatus contains status information for a specific job.
 *
 * @version 1.0
 * @since 1.0
 */
public class JobStatusImplementation implements JobStatus {

    private final String jobIdentifier;
    private final String state;
    private final Integer exitCode;
    private final Exception exception;
    private final boolean running;
    private final boolean done;
    private final Map<String, String> schedulerSpecificInformation;

    /**
     * Create a JobStatus.
     *
     * @param jobIdentifier
     * 		the identifier of the job for which this status was created.
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
    public JobStatusImplementation(String jobIdentifier, String state, Integer exitCode, Exception exception, boolean running, boolean done,
            Map<String, String> schedulerSpecificInformation) {

        if (jobIdentifier == null) {
            throw new IllegalArgumentException("Job may not be null!");
        }

        this.jobIdentifier = jobIdentifier;
        this.state = state;
        this.exitCode = exitCode;
        this.exception = exception;
        this.running = running;
        this.done = done;
        this.schedulerSpecificInformation = schedulerSpecificInformation;
    }

    /**
     * Get the job identifier of the Job for which this JobStatus was created.
     *
     * @return the Job.
     */
    public String getJobIdentifier() {
        return jobIdentifier;
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
        return "JobStatus [jobIdentifier=" + jobIdentifier + ", state=" + state + ", exitCode=" + exitCode + ", exception=" + exception
                + ", running=" + running + ", done=" + done + ", schedulerSpecificInformation=" + schedulerSpecificInformation
                + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobStatusImplementation that = (JobStatusImplementation) o;
        return running == that.running &&
                done == that.done &&
                Objects.equals(jobIdentifier, that.jobIdentifier) &&
                Objects.equals(state, that.state) &&
                Objects.equals(exitCode, that.exitCode) &&
                Objects.equals(exception, that.exception) &&
                Objects.equals(schedulerSpecificInformation, that.schedulerSpecificInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobIdentifier, state, exitCode, exception, running, done, schedulerSpecificInformation);
    }
}
