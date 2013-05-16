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

import java.net.URI;
import java.util.Properties;

/**
 * Scheduler represents a (possibly remote) scheduler that can be used to submit jobs and retrieve queue information.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface Scheduler {

    /**
     * Get the name of the adaptor attached to this Scheduler.
     * 
     * @return the name of the adaptor.
     */
    public String getAdaptorName();

    /**
     * Get the URI representing the location of the Scheduler.
     * 
     * @return the location of the Scheduler.
     */
    public URI getUri();

    /**
     * Get the properties used to create this Scheduler.
     * 
     * @return the properties used to create this Scheduler.
     */
    public Properties getProperties();

    /**
     * Get the queue names supported by this Scheduler.
     * 
     * @return the queue names supported by this Scheduler.
     */
    public String[] getQueueNames();

    /**
     * Does this Scheduler supports the submission of interactive jobs ?   
     * 
     * @return if this scheduler supports the submission of interactive jobs ? 
     */    
    public boolean supportsInteractive();
        
    /**
     * Does this Scheduler support the submission of batch jobs ?   
     * 
     * @return if this scheduler supports the submission of batch jobs ? 
     */    
    public boolean supportsBatch();
    
    
    /**
     * Is this an online scheduler ? 
     * 
     * Online schedulers need to remain active for their jobs to run. Ending an online scheduler will kill all jobs that were 
     * submitted to it. 
     * 
     * Offline schedulers do not need to remains active for their jobs to run. A submitted job will typically be handed over to 
     * some external server that will manage the job for the rest of its lifetime.
     * 
     * @return if this scheduler is online. 
     */    
    public boolean isOnline();

    
    /**
     * Are the standard streams (stdin, stdout, stderr) local ?
     * 
     * @return If the standard streams are local.
     */
//    public boolean hasLocalStandardStreams();

    /**
     * Are the jobs of this scheduler detached ?
     * 
     * @return if the jobs of this scheduler are detached.
     */
    //public boolean hasDetachedJobs();

}
