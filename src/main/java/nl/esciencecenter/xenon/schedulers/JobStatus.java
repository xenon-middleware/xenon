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
package nl.esciencecenter.xenon.schedulers;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.JobCanceledException;

/**
 * JobStatus contains status information for a specific job.
 */
public interface JobStatus {

    /**
     * Get the job identifier of the Job for which this JobStatus was created.
     *
     * @return the identifier of the Job.
     */
    String getJobIdentifier();

    /**
     * Get the name of the Job for which this JobStatus was created.
     *
     * @return the name of the Job.
     */
    String getName();

    /**
     * Get the state of the Job.
     *
     * The state is a scheduler specific string, generally intended to be human readable. Very different state strings can be returned depending on which
     * scheduler is used. Therefore, this method should only be used to provide feedback to the user. To programmatically inspect the state of the job use
     * {{@link #isRunning()}, {@link JobStatus#isDone()} or {@link #hasException()} instead.
     *
     * @return the state of the Job.
     */
    String getState();

    /**
     * Get the exit code for the Job.
     *
     * @return the exit code for the Job.
     */
    Integer getExitCode();

    /**
     * Get the exception produced by the Job or while retrieving the status. If no exception occurred, <code>null</code> will be returned.
     *
     * See {@link #maybeThrowException()} for the possible exceptions.
     *
     *
     * @return the exception.
     */
    XenonException getException();

    /**
     * Throws the exception produced by the Job or while retrieving the status, if it exists. Otherwise continue.
     *
     * @throws JobCanceledException
     *             if the job was cancelled
     * @throws NoSuchJobException
     *             if the job of which the status was requested does not exist
     * @throws XenonException
     *             if an I/O error occurred.
     */
    void maybeThrowException() throws XenonException;

    /**
     * Is the Job running.
     *
     * @return if the Job is running.
     */
    boolean isRunning();

    /**
     * Is the Job done.
     *
     * @return if the Job is done.
     */
    boolean isDone();

    /**
     * Has the Job or job retrieval produced a exception ?
     *
     * @return if the Job has an exception.
     */
    boolean hasException();

    /**
     * Get scheduler specific information on the Job.
     *
     * @return scheduler specific information on the Job.
     */
    public Map<String, String> getSchedulerSpecificInformation();
}
