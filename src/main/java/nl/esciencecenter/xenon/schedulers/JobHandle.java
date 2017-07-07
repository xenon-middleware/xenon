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

/**
 * Job represents a job that has been submitted to a {@link Scheduler}.
 * <p>
 * When a {@link JobDescription} is submitted to a {@link Scheduler} using 
 * {@link Scheduler#submitJob(JobDescription) Scheduler.submitJob}, a <code>JobHandle</code> is returned. This <code>JobHandle</code> can be 
 * used to retrieve the status of the job using {@link Scheduler#getJobStatus(JobHandle) Scheduler.getJobStatus}, cancel the job using 
 * {@link Scheduler#cancelJob(JobHandle) Scheduler.cancelJob}, or wait until the job terminates using
 * {@link Scheduler#waitUntilDone(JobHandle, long) Scheduler.waitUntilDone}.
 * </p>
 * 
 * @version 1.0
 * @since 1.0
 */
public interface JobHandle {

    /**
     * Returns the {@link JobDescription} that was used to create this Job.
     * 
     * @return the JobDescription that belongs to this Job
     */
    JobDescription getJobDescription();

    /**
     * Returns the {@link Scheduler} that was used to create this Job.
     * 
     * @return the Scheduler used to create this job.
     */
    Scheduler getScheduler();

    /**
     * Returns the identifier that was assigned to this job by the scheduler.
     * 
     * @return the identifier that was assigned to this job by the scheduler.
     */
    String getIdentifier();

    /**
     * Returns if this is an interactive job.
     * 
     * @return if this is an interactive job.
     */
    // boolean isInteractive();

    /**
     * Returns if this is an online job.
     * 
     * Online jobs will disappear when the application that submitted is exist, while offline jobs will keep running.
     * 
     * Interactive jobs are by definition online. Batch jobs will be online if the scheduler to which they were submitted is
     * online.
     * 
     * @return if this is an online job.
     */
    // boolean isOnline();
}
