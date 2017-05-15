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

import java.util.HashSet;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.Adaptor;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.Jobs;
import nl.esciencecenter.xenon.jobs.QueueStatus;
import nl.esciencecenter.xenon.jobs.Scheduler;
import nl.esciencecenter.xenon.jobs.Streams;

public class JobsEngine implements Jobs {

    private final XenonEngine xenonEngine;

    public JobsEngine(XenonEngine xenonEngine) {
        this.xenonEngine = xenonEngine;
    }

    private Adaptor getAdaptor(Scheduler scheduler) throws XenonException {
        return xenonEngine.getAdaptor(scheduler.getAdaptorName());
    }

    @Override
    public Scheduler newScheduler(String scheme, String location, Credential credential, Map<String, String> properties) 
            throws XenonException {

        Adaptor adaptor = xenonEngine.getAdaptorFor(scheme);
        return adaptor.jobsAdaptor().newScheduler(scheme, location, credential, properties);
    }

    @Override
    public String [] getSupportedSchemes() { 
        return xenonEngine.getSupportedJobsSchemes();
    }
    
    @Override
    public boolean isOnline(String scheme) throws XenonException { 
        return xenonEngine.getAdaptorFor(scheme).jobsAdaptor().isOnline(scheme);
    }
    
    @Override
    public boolean supportsInteractive(String scheme) throws XenonException { 
        return xenonEngine.getAdaptorFor(scheme).jobsAdaptor().supportsInteractive(scheme);
    }

    @Override
    public  boolean supportsBatch(String scheme) throws XenonException { 
        return xenonEngine.getAdaptorFor(scheme).jobsAdaptor().supportsBatch(scheme);    
    }
    
    @Override
    public void close(Scheduler scheduler) throws XenonException {
        getAdaptor(scheduler).jobsAdaptor().close(scheduler);
    }

    @Override
    public boolean isOpen(Scheduler scheduler) throws XenonException {
        return getAdaptor(scheduler).jobsAdaptor().isOpen(scheduler);
    }

    @Override
    public String getDefaultQueueName(Scheduler scheduler) throws XenonException {
        return getAdaptor(scheduler).jobsAdaptor().getDefaultQueueName(scheduler);
    }

    @Override
    public JobStatus getJobStatus(Job job) throws XenonException {
        return getAdaptor(job.getScheduler()).jobsAdaptor().getJobStatus(job);
    }

    private String[] getAdaptors(Job[] in) {

        HashSet<String> result = new HashSet<>();

        for (Job job : in) {
            if (job != null) {
                result.add(job.getScheduler().getAdaptorName());
            }
        }

        return result.toArray(new String[result.size()]);
    }

    private void selectJobs(String adaptorName, Job[] in, Job[] out) {
        for (int i = 0; i < in.length; i++) {
            if (in[i] != null && adaptorName.equals(in[i].getScheduler().getAdaptorName())) {
                out[i] = in[i];
            } else {
                out[i] = null;
            }
        }
    }

    private void getJobStatus(String adaptor, Job[] in, JobStatus[] out) {

        JobStatus[] result = null;
        XenonException exception = null;

        try {
            result = xenonEngine.getAdaptor(adaptor).jobsAdaptor().getJobStatuses(in);
        } catch (XenonException e) {
            exception = e;
        }

        for (int i = 0; i < in.length; i++) {
            if (in[i] != null) {
                if (result != null) {
                    out[i] = result[i];
                } else {
                    out[i] = new JobStatusImplementation(in[i], null, null, exception, false, false, null);
                }
            }
        }
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) {

        // First check for the three simple cases; null, no jobs or 1 job.
        if (jobs == null || jobs.length == 0) {
            return new JobStatus[0];
        }

        if (jobs.length == 1) {

            if (jobs[0] == null) {
                return new JobStatus[1];
            }

            try {
                return new JobStatus[] { getJobStatus(jobs[0]) };
            } catch (Exception e) {
                return new JobStatus[] { new JobStatusImplementation(jobs[0], null, null, e, false, false, null) };
            }
        }

        // If we have more than one job, we first collect all adaptor names. 
        String[] adaptors = getAdaptors(jobs);

        // Next we traverse over the names, and get the JobStatus for each adaptor individually, merging the result into the 
        // overall result on the fly.
        JobStatus[] result = new JobStatus[jobs.length];
        Job[] tmp = new Job[jobs.length];

        for (String adaptor : adaptors) {
            selectJobs(adaptor, jobs, tmp);
            getJobStatus(adaptor, tmp, result);
        }

        return result;
    }

    @Override
    public JobStatus waitUntilDone(Job job, long timeout) throws XenonException {
        return getAdaptor(job.getScheduler()).jobsAdaptor().waitUntilDone(job, timeout);
    }

    @Override
    public JobStatus waitUntilRunning(Job job, long timeout) throws XenonException {
        return getAdaptor(job.getScheduler()).jobsAdaptor().waitUntilRunning(job, timeout);
    }

    @Override
    public JobStatus cancelJob(Job job) throws XenonException {
        return getAdaptor(job.getScheduler()).jobsAdaptor().cancelJob(job);
    }

    @Override
    public Job[] getJobs(Scheduler scheduler, String... queueNames) throws XenonException {
        return getAdaptor(scheduler).jobsAdaptor().getJobs(scheduler, queueNames);
    }

    @Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws XenonException {
        return getAdaptor(scheduler).jobsAdaptor().submitJob(scheduler, description);
    }

    @Override
    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws XenonException {
        return getAdaptor(scheduler).jobsAdaptor().getQueueStatus(scheduler, queueName);
    }

    @Override
    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws XenonException {
        return getAdaptor(scheduler).jobsAdaptor().getQueueStatuses(scheduler, queueNames);
    }

    @Override
    public Streams getStreams(Job job) throws XenonException {
        return getAdaptor(job.getScheduler()).jobsAdaptor().getStreams(job);
    }

    @Override
    public String toString() {
        return "JobsEngine [xenonEngine=" + xenonEngine + "]";
    }

}
