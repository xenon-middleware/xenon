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
package nl.esciencecenter.octopus.jobs;

import java.util.Map;

/**
 * JobStatus contains status information for a specific job.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface JobStatus {

    /**
     * Get the job for which this JobStatus was created.
     * 
     * @return the Job.
     */
    public Job getJob();

    /**
     * Get the state of the Job.
     * 
     * @return the state of the Job.
     */
    public String getState();

    /**
     * Get the exit code for the Job.
     * 
     * @return the exit code for the Job.
     */
    public Integer getExitCode();

    /**
     * Get the exception produced by the Job or while retrieving the status. If a job was canceled, will return a
     * JobCanceledException.
     * 
     * @return the exception.
     */
    public Exception getException();

    /**
     * Is the Job running.
     * 
     * @return if the Job is running.
     */
    public boolean isRunning();

    /**
     * Is the Job done.
     * 
     * @return if the Job is done.
     */
    public boolean isDone();

    /**
     * Has the Job or job retrieval produced a exception ?
     * 
     * @return if the Job has an exception.
     */
    public boolean hasException();

    /**
     * Get scheduler specific information on the Job.
     * 
     * @return scheduler specific information on the Job.
     */
    public Map<String, String> getSchedulerSpecficInformation();
}
