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
package nl.esciencecenter.xenon.schedulers;

import java.util.Map;

/**
 * JobStatus contains status information for a specific job.
 */
public interface JobStatus {

    /**
     * Get the job identifier of the Job for which this JobStatus was created.
     * 
     * @return the Job.
     */
    String getJobIdentifier();

    /**
     * Get the state of the Job.
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
     * Get the exception produced by the Job or while retrieving the status. If a job was canceled, will return a
     * JobCanceledException.
     * 
     * @return the exception.
     */
    Exception getException();

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
    public Map<String, String> getSchedulerSpecficInformation();
}
