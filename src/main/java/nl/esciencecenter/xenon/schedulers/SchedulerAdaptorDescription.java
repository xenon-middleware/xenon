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

import nl.esciencecenter.xenon.AdaptorDescription;

/**
 *
 */
public interface SchedulerAdaptorDescription extends AdaptorDescription {

    /**
     * Is this an embedded scheduler ?
     *
     * Embedded schedulers are implemented inside the Xenon process itself. Therefore this process needs to remain active for its jobs to run. Ending an online
     * scheduler will typically orphan or kill all jobs that were submitted to it.
     *
     * Non-embedded schedulers do not need to remain active for their jobs to run. A submitted job will typically be handed over to some external server that
     * will manage the job for the rest of its lifetime.
     *
     * @return if this scheduler is embedded.
     */
    boolean isEmbedded();

    /**
     * Does this Scheduler support the submission of batch jobs ?
     *
     * For batch jobs the standard streams of the jobs are redirected from and to files.
     *
     * @return if this scheduler supports the submission of batch jobs ?
     */
    boolean supportsBatch();

    /**
     * Does this Scheduler supports the submission of interactive jobs ?
     *
     * For interactive jobs the standard streams of the job must be handled by the submitting process. Failing to do so may cause the job to hang indefinitely.
     *
     * @return if this scheduler supports the submission of interactive jobs ?
     */
    boolean supportsInteractive();

    /**
     * Does this Scheduler create a FileSystem to support the submission of jobs ?
     * 
     * Many scheduler implementations use a FileSystem internally to handle job submission, for example to store submission scripts or handle the standard I/O
     * streams of a process. This FileSystem can optionally be retrieved by the user to perform other tasks, such as staging in and output data of the job. To
     * do so use {@link nl.esciencecenter.xenon.schedulers.Scheduler#getFileSystem()} method.
     *
     * @return does this {@link Scheduler} create a FileSystem to support the submission of jobs ?
     */
    boolean usesFileSystem();
}
