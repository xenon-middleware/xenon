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
package nl.esciencecenter.octopus.engine.jobs;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.jobs.Streams;

public class JobsEngine implements Jobs {

    private final OctopusEngine octopusEngine;

    public JobsEngine(OctopusEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
    }

    private Adaptor getAdaptor(Scheduler scheduler) throws OctopusException {
        return octopusEngine.getAdaptor(scheduler.getAdaptorName());
    }

    public Scheduler newScheduler(URI location, Credential credential, Properties properties) throws OctopusException,
            OctopusIOException {

        Adaptor adaptor = octopusEngine.getAdaptorFor(location.getScheme());
        return adaptor.jobsAdaptor().newScheduler(location, credential, properties);
    }

    @Override
    public Scheduler getLocalScheduler() throws OctopusException, OctopusIOException {
        Adaptor adaptor = octopusEngine.getAdaptorFor(OctopusEngine.LOCAL_ADAPTOR_NAME);
        return adaptor.jobsAdaptor().getLocalScheduler();
    }

    @Override
    public void close(Scheduler scheduler) throws OctopusException, OctopusIOException {
        getAdaptor(scheduler).jobsAdaptor().close(scheduler);
    }

    @Override
    public boolean isOpen(Scheduler scheduler) throws OctopusException, OctopusIOException {
        return getAdaptor(scheduler).jobsAdaptor().isOpen(scheduler);
    }

    @Override
    public JobStatus getJobStatus(Job job) throws OctopusException, OctopusIOException {
        return getAdaptor(job.getScheduler()).jobsAdaptor().getJobStatus(job);
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) {

        // FIXME: Optimize!

        JobStatus[] result = new JobStatus[jobs.length];

        for (int i = 0; i < jobs.length; i++) {
            try {
                result[i] = getJobStatus(jobs[i]);
            } catch (OctopusException | OctopusIOException e) {
                result[i] = new JobStatusImplementation(jobs[i], null, null, e, false, false, null);
            }
        }

        return result;
    }

    @Override
    public JobStatus waitUntilDone(Job job, long timeout) throws OctopusException, OctopusIOException {      
        return getAdaptor(job.getScheduler()).jobsAdaptor().waitUntilDone(job, timeout);
    }
    
    @Override
    public void cancelJob(Job job) throws OctopusException, OctopusIOException {
        getAdaptor(job.getScheduler()).jobsAdaptor().cancelJob(job);
    }

    @Override
    public Job[] getJobs(Scheduler scheduler, String... queueNames) throws OctopusException, OctopusIOException {
        return getAdaptor(scheduler).jobsAdaptor().getJobs(scheduler, queueNames);
    }

    @Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException, OctopusIOException {
        return getAdaptor(scheduler).jobsAdaptor().submitJob(scheduler, description);
    }

    @Override
    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws OctopusException, OctopusIOException {
        return getAdaptor(scheduler).jobsAdaptor().getQueueStatus(scheduler, queueName);
    }

    @Override
    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws OctopusException, OctopusIOException {
        return getAdaptor(scheduler).jobsAdaptor().getQueueStatuses(scheduler, queueNames);
    }

    
    @Override
    public Streams getStreams(Job job) throws OctopusException {
        return getAdaptor(job.getScheduler()).jobsAdaptor().getStreams(job);
    }

    @Override
    public String toString() {
        return "JobsEngine [octopusEngine=" + octopusEngine + "]";
    }

}
