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
package nl.esciencecenter.xenon.adaptors.job.local;

import java.util.EnumSet;
import java.util.Map;

import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.UnknownPropertyException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.XenonPropertyDescriptionImplementation;
import nl.esciencecenter.xenon.engine.jobs.JobAdaptor;
import nl.esciencecenter.xenon.engine.jobs.JobImplementation;
import nl.esciencecenter.xenon.engine.jobs.JobsEngine;
import nl.esciencecenter.xenon.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;
import nl.esciencecenter.xenon.engine.util.InteractiveProcess;
import nl.esciencecenter.xenon.engine.util.InteractiveProcessFactory;
import nl.esciencecenter.xenon.engine.util.JobQueues;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.QueueStatus;
import nl.esciencecenter.xenon.jobs.Scheduler;
import nl.esciencecenter.xenon.jobs.Streams;
import nl.esciencecenter.xenon.util.Utils;

/**
 * LocalFiles implements an Xenon <code>Jobs</code> adaptor for local job operations.
 * 
 * @see nl.esciencecenter.xenon.jobs.Jobs
 * 
 * @version 1.0
 * @since 1.0
 */
public class LocalJobs extends JobAdaptor implements InteractiveProcessFactory {
    
    /** The parent adaptor */
    //private final LocalAdaptor localAdaptor;

    /** Name of the local adaptor is defined in the engine. */
    public static final String ADAPTOR_NAME = "local";

    /** Local properties start with this prefix. */
    public static final String PREFIX = XenonEngine.ADAPTORS_PREFIX + "local.";

    /** Description of the adaptor */
    public static final String ADAPTOR_DESCRIPTION = "The local jobs adaptor implements all functionality "
            + " by emulating a local queue.";

    /** Local queue properties start with this prefix. */
    public static final String QUEUE = PREFIX + "queue.";

    /** Property for maximum history length for finished jobs */
    public static final String MAX_HISTORY = QUEUE + "historySize";

    /** Property for maximum history length for finished jobs */
    public static final String POLLING_DELAY = QUEUE + "pollingDelay";

    /** Local multi queue properties start with this prefix. */
    public static final String MULTIQ = QUEUE + "multi.";

    /** Property for the maximum number of concurrent jobs in the multi queue. */
    public static final String MULTIQ_MAX_CONCURRENT = MULTIQ + "maxConcurrentJobs";

    /** Local queue information start with this prefix. */
    public static final String INFO = PREFIX + "info.";

    /** Local job information start with this prefix. */
    public static final String JOBS = INFO + "jobs.";
    
    /** How many jobs have been submitted locally. */
    public static final String SUBMITTED = JOBS + "submitted";
    
    /** The schemes supported by the adaptor */
    private static final ImmutableArray<String> ADAPTOR_SCHEME = new ImmutableArray<>("local");

    /** The locations supported by the adaptor */
    private static final ImmutableArray<String> ADAPTOR_LOCATIONS = new ImmutableArray<>("(null)", "(empty string)", "/");
    
    /** The properties supported by this adaptor */
    protected static final ImmutableArray<XenonPropertyDescription> VALID_PROPERTIES = 
            new ImmutableArray<XenonPropertyDescription>(
                    new XenonPropertyDescriptionImplementation(POLLING_DELAY, Type.INTEGER, EnumSet.of(Component.XENON), 
                            "1000", "The polling delay for monitoring running jobs (in milliseconds)."),
                    new XenonPropertyDescriptionImplementation(MULTIQ_MAX_CONCURRENT, Type.INTEGER, EnumSet.of(Component.XENON), 
                            "4", "The maximum number of concurrent jobs in the multiq.."));
    
    
    private final Scheduler localScheduler;
    private final JobQueues jobQueues;

    public LocalJobs(JobsEngine jobsEngine, Map<String, String> properties) throws XenonException {
        
      //  this.localAdaptor = localAdaptor;
        super(jobsEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME, ADAPTOR_LOCATIONS, VALID_PROPERTIES,
                new XenonProperties(VALID_PROPERTIES, Component.XENON, properties));
        
        localScheduler = new SchedulerImplementation(ADAPTOR_NAME, "LocalScheduler", "/", 
                new String[] { "single", "multi", "unlimited" }, null, getProperties(), true, true, true);

        int processors = Runtime.getRuntime().availableProcessors();
        int multiQThreads = getProperties().getIntegerProperty(LocalJobs.MULTIQ_MAX_CONCURRENT, processors);
        int pollingDelay = getProperties().getIntegerProperty(LocalJobs.POLLING_DELAY);
        
        
        Files files = jobsEngine.getXenonEngine().files();
        Files localFiles = files; // TODO: is this correct ?
        
        Path cwd = Utils.getLocalCWD(localFiles);
        
        jobQueues = new JobQueues(ADAPTOR_NAME, files, localScheduler, cwd, this, multiQThreads,
                pollingDelay);
    }

    @Override
    public InteractiveProcess createInteractiveProcess(JobImplementation job) throws XenonException {
        return new LocalInteractiveProcess(job);
    }

    @Override
    public Scheduler newScheduler(String location, Credential credential, Map<String, String> properties) 
            throws XenonException {

        if (!(location == null || location.isEmpty() || location.equals("/"))) {
            throw new InvalidLocationException(ADAPTOR_NAME, "Cannot create local scheduler with location: " 
                    + location);
        }
        
        LocalUtils.checkCredential(ADAPTOR_NAME, credential);

        if (properties != null && properties.size() > 0) {
            throw new UnknownPropertyException(ADAPTOR_NAME, "Cannot create local scheduler with additional properties!");
        }

        return localScheduler;
    }

    @Override
    public Job[] getJobs(Scheduler scheduler, String... queueNames) throws XenonException {
        return jobQueues.getJobs(queueNames);
    }

    @Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws XenonException {
        return jobQueues.submitJob(description);
    }

    @Override
    public JobStatus getJobStatus(Job job) throws XenonException {
        return jobQueues.getJobStatus(job);
    }

    @Override
    public JobStatus waitUntilDone(Job job, long timeout) throws XenonException {
        return jobQueues.waitUntilDone(job, timeout);
    }

    @Override
    public JobStatus waitUntilRunning(Job job, long timeout) throws XenonException {
        return jobQueues.waitUntilRunning(job, timeout);
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) {
        return jobQueues.getJobStatuses(jobs);
    }

    @Override
    public JobStatus cancelJob(Job job) throws XenonException {
        return jobQueues.cancelJob(job);
    }

    public void end() {
        jobQueues.end();
    }

    @Override
    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws XenonException {
        return jobQueues.getQueueStatus(scheduler, queueName);
    }

    @Override
    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws XenonException {
        return jobQueues.getQueueStatuses(scheduler, queueNames);
    }

    @Override
    public void close(Scheduler scheduler) throws XenonException {
        // ignored
    }

    @Override
    public boolean isOpen(Scheduler scheduler) throws XenonException {
        return true;
    }

    @Override
    public String getDefaultQueueName(Scheduler scheduler) throws XenonException {
        return jobQueues.getDefaultQueueName(scheduler);
    }

    @Override
    public Streams getStreams(Job job) throws XenonException {
        return jobQueues.getStreams(job);
    }

    /**
     * Add information about the local job adaptor to the map. 
     * 
     * @param result
     *          the map to add information to. 
     */
    public void getAdaptorSpecificInformation(Map<String, String> result) {
        result.put(LocalJobs.SUBMITTED, Long.toString(jobQueues.getCurrentJobID()));
    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.xenon.engine.jobs.JobAdaptor#getAdaptorSpecificInformation()
     */
    @Override
    public Map<String, String> getAdaptorSpecificInformation() {
        // TODO Auto-generated method stub
        return null;
    }
}