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
 * Scheduler represents a (possibly remote) scheduler that can be used to submit jobs and retrieve queue information.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface Scheduler {

    /**
     * Get the name of the adaptor that created this Scheduler.
     * 
     * @return the name of the adaptor.
     */
    String getAdaptorName();

    /**
     * Get the location of this Scheduler.
     * 
     * @return the location of the Scheduler.
     */
    String getLocation();

    /**
     * Get the scheme used to access the Scheduler.
     * 
     * @return the scheme used to access the Scheduler.
     */
    String getScheme();

    /**
     * Get the properties used to create this Scheduler.
     * 
     * @return the properties used to create this Scheduler.
     */
    Map<String, String> getProperties();

    /**
     * Get the queue names supported by this Scheduler.
     * 
     * @return the queue names supported by this Scheduler.
     */
    String[] getQueueNames();

    /**
     * Does this Scheduler supports the submission of interactive jobs ?
     * 
     * For interactive jobs the standard streams of the job must be handled by the submitting process. Failing to do so may cause
     * the job to hang indefinitely.
     * 
     * @return if this scheduler supports the submission of interactive jobs ?
     */
    boolean supportsInteractive();

    /**
     * Does this Scheduler support the submission of batch jobs ?
     * 
     * For batch jobs the standard streams of the jobs are redirected from / to files.
     * 
     * @return if this scheduler supports the submission of batch jobs ?
     */
    boolean supportsBatch();

    /**
     * Is this an online scheduler ?
     * 
     * Online schedulers need to remain active for their jobs to run. Ending an online scheduler will kill all jobs that were
     * submitted to it.
     * 
     * Offline schedulers do not need to remains active for their jobs to run. A submitted job will typically be handed over to
     * some external server that will manage the job for the rest of its lifetime.
     * 
     * Online schedulers typically support both interactive jobs (where the user controls the standard streams) and batch jobs
     * (where the standard streams are redirected to/from files).
     * 
     * Since it is impossible to continue an interactive jobs when a scheduler ends, interactive jobs will always be killed,
     * even in an offline scheduler.
     * 
     * @return if this scheduler is online.
     */
    boolean isOnline();
}
