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

package nl.esciencecenter.xenon.engine.jobs;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.Adaptor;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobAdaptorDescription;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.QueueStatus;
import nl.esciencecenter.xenon.jobs.Scheduler;
import nl.esciencecenter.xenon.jobs.Streams;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class JobAdaptor extends Adaptor {

   // private static final Logger LOGGER = LoggerFactory.getLogger(JobAdaptor.class);
    
    private final JobsEngine jobsEngine;

    /**
     * @param xenonEngine
     * @param name
     * @param description
     * @param supportedSchemes
     * @param supportedLocations
     * @param validProperties
     * @param properties
     */
    protected JobAdaptor(JobsEngine jobsEngine, String name, String description, ImmutableArray<String> supportedSchemes,
            ImmutableArray<String> supportedLocations, ImmutableArray<XenonPropertyDescription> validProperties) {
        super(name, description, supportedSchemes, supportedLocations, validProperties);
    
        this.jobsEngine = jobsEngine;
    }
    
    protected JobsEngine getJobEngine() { 
        return jobsEngine;
    }
    
    
    /**
     * @return
     */
    public JobAdaptorDescription getAdaptorDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    public abstract Scheduler newScheduler(String location, Credential credential, Map<String, String> properties)
            throws XenonException;
    
    public abstract void close(Scheduler scheduler) throws XenonException;

    public abstract boolean isOpen(Scheduler scheduler) throws XenonException;

    public abstract String getDefaultQueueName(Scheduler scheduler) throws XenonException;

    public abstract Job[] getJobs(Scheduler scheduler, String... queueNames) throws XenonException;

    public abstract QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws XenonException;

    public abstract QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws XenonException;
    
    public abstract Job submitJob(Scheduler scheduler, JobDescription description) throws XenonException;

    public abstract JobStatus getJobStatus(Job job) throws XenonException;

    public abstract JobStatus[] getJobStatuses(Job... jobs);

    public abstract Streams getStreams(Job job) throws XenonException;

    public abstract JobStatus cancelJob(Job job) throws XenonException;
    
    public abstract JobStatus waitUntilDone(Job job, long timeout) throws XenonException;

    public abstract JobStatus waitUntilRunning(Job job, long timeout) throws XenonException;

    public abstract Map<String, String> getAdaptorSpecificInformation();

    public abstract void end();
    
}
