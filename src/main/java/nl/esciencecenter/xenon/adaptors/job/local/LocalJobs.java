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

import static nl.esciencecenter.xenon.adaptors.job.local.LocalProperties.ADAPTOR_DESCRIPTION;
import static nl.esciencecenter.xenon.adaptors.job.local.LocalProperties.ADAPTOR_LOCATIONS;
import static nl.esciencecenter.xenon.adaptors.job.local.LocalProperties.ADAPTOR_NAME;
import static nl.esciencecenter.xenon.adaptors.job.local.LocalProperties.ADAPTOR_SCHEME;
import static nl.esciencecenter.xenon.adaptors.job.local.LocalProperties.MULTIQ_MAX_CONCURRENT;
import static nl.esciencecenter.xenon.adaptors.job.local.LocalProperties.POLLING_DELAY;
import static nl.esciencecenter.xenon.adaptors.job.local.LocalProperties.SUBMITTED;
import static nl.esciencecenter.xenon.adaptors.job.local.LocalProperties.VALID_PROPERTIES;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.sshd.client.session.ClientSession;

import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.UnknownPropertyException;
import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.jobs.JobAdaptor;
import nl.esciencecenter.xenon.engine.jobs.JobImplementation;
import nl.esciencecenter.xenon.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.xenon.engine.jobs.JobsEngine;
import nl.esciencecenter.xenon.engine.jobs.SchedulerImplementation;
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
    
	static class SchedulerInfo {

		private final String name; 
		private final JobQueues jobQueues;
		
        private SchedulerInfo(String name, JobQueues jobQueues) {
            this.name = name;
            this.jobQueues = jobQueues;
        }

        private JobQueues getJobQueues() {
            return jobQueues;
        }

        private void end() throws IOException {
            jobQueues.end();
        }
    }
	
	//private final Scheduler localScheduler;
    //private final JobQueues jobQueues;

    private final Map<String, SchedulerInfo> schedulers = new ConcurrentHashMap<>();
    
    public LocalJobs(JobsEngine jobsEngine) throws XenonException {
    	super(jobsEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME, ADAPTOR_LOCATIONS, VALID_PROPERTIES);
    }
        
    @Override
    public InteractiveProcess createInteractiveProcess(JobImplementation job) throws XenonException {
        return new LocalInteractiveProcess(job);
    }

    @Override
    public Scheduler newScheduler(String location, Credential credential, Map<String, String> properties) 
            throws XenonException {

    	// Location should be: local://<name> 
    	
        if (location == null || location.isEmpty() || !location.startsWith("local://") || location.equals("local://")) {
            throw new InvalidLocationException(ADAPTOR_NAME, "Cannot create local scheduler with location: " 
                    + location);
        }
        
        String schedulerID = location.substring("local://".length()).trim();
        
        if (schedulerID.isEmpty()) { 
        	throw new InvalidLocationException(ADAPTOR_NAME, "Cannot create local scheduler without a unique name"); 
        }
     
        if (schedulers.containsKey(schedulerID)) { 
        	throw new InvalidLocationException(ADAPTOR_NAME, "Local scheduler " + schedulerID + " already exists!"); 
        }
        
        XenonProperties xp = new XenonProperties(VALID_PROPERTIES, properties);

        LocalUtils.checkCredential(ADAPTOR_NAME, credential);

        if (properties != null && properties.size() > 0) {
            throw new UnknownPropertyException(ADAPTOR_NAME, "Cannot create local scheduler with additional properties!");
        }
        
        int processors = Runtime.getRuntime().availableProcessors();
        int multiQThreads = xp.getIntegerProperty(MULTIQ_MAX_CONCURRENT, processors);
        int pollingDelay = xp.getIntegerProperty(POLLING_DELAY);
        
        Scheduler s = new SchedulerImplementation(ADAPTOR_NAME, schedulerID, location, 
                new String[] { "single", "multi", "unlimited" }, null, xp, true, true, true);
        
        Files files = Xenon.files();
        
        Path cwd = Utils.getLocalCWD(files);
        
        JobQueues jobQueues = new JobQueues(ADAPTOR_NAME, files, s, cwd, this, multiQThreads, pollingDelay);
   
        schedulers.put(schedulerID, new SchedulerInfo(schedulerID, jobQueues));
        
        return s;
    }

    private SchedulerInfo getSchedulerInfo(Scheduler s) throws XenonException {
    
    	if (s == null) { 
    		throw new IllegalArgumentException("Scheduler may not be null");
    	}
    	
    	SchedulerImplementation si = (SchedulerImplementation)s;
    	
    	if (!ADAPTOR_NAME.equals(si.getAdaptorName())) { 
    		throw new XenonException(ADAPTOR_NAME, "Scheduler was not created by this adaptor!");
    	}
    	
    	String id = si.getUniqueID();
    	
    	SchedulerInfo info = schedulers.get(id);
    	
    	if (info == null) { 
    		throw new XenonException(ADAPTOR_NAME, "Scheduler " + id + " is no longer active");
    	}
    	
    	return info;
    }
    
    @Override
    public Job[] getJobs(Scheduler scheduler, String... queueNames) throws XenonException {
    	return getSchedulerInfo(scheduler).jobQueues.getJobs(queueNames);
    }

    @Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws XenonException {
        return getSchedulerInfo(scheduler).jobQueues.submitJob(description);
    }

    @Override
    public JobStatus getJobStatus(Job job) throws XenonException {
        return getSchedulerInfo(job.getScheduler()).jobQueues.getJobStatus(job);
    }

    @Override
    public JobStatus waitUntilDone(Job job, long timeout) throws XenonException {
        return getSchedulerInfo(job.getScheduler()).jobQueues.waitUntilDone(job, timeout);
    }

    @Override
    public JobStatus waitUntilRunning(Job job, long timeout) throws XenonException {
        return getSchedulerInfo(job.getScheduler()).jobQueues.waitUntilRunning(job, timeout);
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) {
    	
    	JobStatus [] result = new JobStatus[jobs.length];
    	
    	for (int i=0;i<jobs.length;i++) {
    		
    		if (jobs[i] == null) { 
    			result[i] = null;
    		} else { 
    			try { 
    				result[i] = getJobStatus(jobs[i]);
    			} catch (XenonException e) {
    				result[i] = new JobStatusImplementation(jobs[i], null, null, e, false, false, null);
    			}
    		}
    	}
    	
    	return result;
    }

    @Override
    public JobStatus cancelJob(Job job) throws XenonException {
        return getSchedulerInfo(job.getScheduler()).jobQueues.cancelJob(job);
    }

    public void end() {
        // TODO: jobQueues.end();
    }

    @Override
    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws XenonException {
        return getSchedulerInfo(scheduler).jobQueues.getQueueStatus(scheduler, queueName);
    }

    @Override
    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws XenonException {
        return getSchedulerInfo(scheduler).jobQueues.getQueueStatuses(scheduler, queueNames);
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
        return getSchedulerInfo(scheduler).jobQueues.getDefaultQueueName(scheduler);
    }

    @Override
    public Streams getStreams(Job job) throws XenonException {
        return getSchedulerInfo(job.getScheduler()).jobQueues.getStreams(job);
    }

    /**
     * Add information about the local job adaptor to the map. 
     * 
     * @param result
     *          the map to add information to. 
     */
//    public void getAdaptorSpecificInformation(Map<String, String> result) {
//        result.put(SUBMITTED, Long.toString(jobQueues.getCurrentJobID()));
//    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.xenon.engine.jobs.JobAdaptor#getAdaptorSpecificInformation()
     */
    @Override
    public Map<String, String> getAdaptorSpecificInformation() {
        // TODO Auto-generated method stub
        return null;
    }
}