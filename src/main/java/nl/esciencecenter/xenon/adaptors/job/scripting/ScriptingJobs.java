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
package nl.esciencecenter.xenon.adaptors.job.scripting;

import java.util.Collection;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonRuntimeException;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.jobs.JobAdaptor;
import nl.esciencecenter.xenon.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.xenon.engine.jobs.JobsEngine;
import nl.esciencecenter.xenon.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.NoSuchSchedulerException;
import nl.esciencecenter.xenon.jobs.QueueStatus;
import nl.esciencecenter.xenon.jobs.Scheduler;
import nl.esciencecenter.xenon.jobs.Streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Implementation of Xenon Jobs interface using ssh and scripts. What scripts to run and how to parse the results is implemented
 * in the adaptors.
 * 
 */
public class ScriptingJobs extends JobAdaptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptingJobs.class);

    //private final ScriptingAdaptor adaptor;
    //private final XenonEngine xenonEngine;
    //private final String adaptorName;
    private final SchedulerConnectionFactory connectionFactory;

    private final Map<String, SchedulerConnection> connections;

    public ScriptingJobs(JobsEngine jobsEngine, String name, String description, ImmutableArray<String> supportedSchemes,
            ImmutableArray<String> supportedLocations, ImmutableArray<XenonPropertyDescription> validProperties,
            XenonProperties properties, SchedulerConnectionFactory connectionFactory) {
        
        super(jobsEngine, name, description, supportedSchemes, supportedLocations, validProperties, properties);
        
        this.connectionFactory = connectionFactory;

        connections = new HashMap<>();
    }

    private synchronized SchedulerConnection getConnection(Scheduler scheduler) throws NoSuchSchedulerException {
        if (!(scheduler instanceof SchedulerImplementation)) {
            throw new XenonRuntimeException(getName(), "scheduler " + scheduler.toString() + " not created by this adaptor");
        }

        String schedulerID = ((SchedulerImplementation) scheduler).getUniqueID();

        if (!connections.containsKey(schedulerID)) {
            throw new NoSuchSchedulerException(getName(), "cannot find scheduler " + scheduler.toString()
                    + " did you close it already?");
        }

        return connections.get(schedulerID);
    }

    private synchronized void addConnection(SchedulerConnection connection) {
        connections.put(connection.getID(), connection);
    }

    private synchronized void removeConnection(SchedulerConnection connection) {
        connections.remove(connection.getID());
    }

    @Override
    public synchronized boolean isOpen(Scheduler scheduler) throws XenonException {
        if (!(scheduler instanceof SchedulerImplementation)) {
            throw new XenonRuntimeException(getName(), "scheduler " + scheduler.toString() + " not created by this adaptor");
        }

        String schedulerID = ((SchedulerImplementation) scheduler).getUniqueID();

        return connections.containsKey(schedulerID);
    }

    @Override
    public Scheduler newScheduler(String location, Credential credential, Map<String, String> properties) 
            throws XenonException {

        XenonProperties p = new XenonProperties(validProperties, properties);

        SchedulerConnection connection = connectionFactory.newSchedulerConnection(location, credential, p, 
                getJobEngine().getXenonEngine());

        addConnection(connection);

        return connection.getScheduler();
    }

    @Override
    public String getDefaultQueueName(Scheduler scheduler) throws XenonException {
        //find connection
        SchedulerConnection connection = getConnection(scheduler);

        //fetch and parse info
        return connection.getDefaultQueueName();
    }

    @Override
    public void close(Scheduler scheduler) throws XenonException {
        SchedulerConnection connection = getConnection(scheduler);

        connection.close();

        removeConnection(connection);
    }

    @Override
    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws XenonException {
        //find connection
        SchedulerConnection connection = getConnection(scheduler);

        //fetch and parse info
        return connection.getQueueStatus(queueName);
    }

    @Override
    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws XenonException {
        //find connection
        SchedulerConnection connection = getConnection(scheduler);

        //fetch and parse info
        return connection.getQueueStatuses(queueNames);
    }

    @Override
    public Job[] getJobs(Scheduler scheduler, String... queueNames) throws XenonException {
        //find connection
        SchedulerConnection connection = getConnection(scheduler);

        //fetch and parse info
        return connection.getJobs(queueNames);
    }

    @Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws XenonException {
        // Copy the JobDescription to ensure the user doesn't change it after we return.
        return getConnection(scheduler).submitJob(new JobDescription(description));
    }

    @Override
    public JobStatus getJobStatus(Job job) throws XenonException {
        SchedulerConnection connection = getConnection(job.getScheduler());

        return connection.getJobStatus(job);
    }

    private SchedulerConnection[] getConnections(Job[] jobs) {
        Map<String, SchedulerConnection> result = new HashMap<>();

        for (Job job : jobs) {
            if (job != null) {
                Scheduler scheduler = job.getScheduler();
                if (!(scheduler instanceof SchedulerImplementation)) {
                    throw new XenonRuntimeException(getName(), "scheduler " + scheduler.toString()
                            + " not created by this adaptor");
                }

                String schedulerID = ((SchedulerImplementation) scheduler).getUniqueID();

                if (connections.containsKey(schedulerID)) {
                    result.put(schedulerID, connections.get(schedulerID));
                }
            }
        }

        Collection<SchedulerConnection> tmp = result.values();
        return tmp.toArray(new SchedulerConnection[tmp.size()]);
    }

    private void selectJobs(SchedulerConnection connection, Job[] in, Job[] out) {
        for (int i = 0; i < in.length; i++) {
            if (in[i] != null && connection.getScheduler().equals(in[i].getScheduler())) {
                out[i] = in[i];
            } else {
                out[i] = null;
            }
        }
    }

    private void getJobStatus(SchedulerConnection connection, Job[] in, JobStatus[] out) {
        JobStatus[] result = null;
        Exception exception = null;

        try {
            result = connection.getJobStatuses(in);
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
        //implementation inspired by / copy pasted from JobsEngine.getJobStatuses()

        // If we have more than one job, we first collect all connections
        SchedulerConnection[] connectionsCopy = getConnections(jobs);

        JobStatus[] result = new JobStatus[jobs.length];
        Job[] tmp = new Job[jobs.length];

        // Next we iterate over the connections, and get the JobStatus for each scheduler individually, merging the result into
        // the overall result on the fly.
        for (SchedulerConnection connection : connectionsCopy) {
            selectJobs(connection, jobs, tmp);
            getJobStatus(connection, tmp, result);
        }

        // Fill in all empty results with exceptions.
        for (int i = 0; i < jobs.length; i++) {
            if (jobs[i] != null && result[i] == null) {
                Exception exception = new Exception("cannot find scheduler for job");
                result[i] = new JobStatusImplementation(jobs[i], null, null, exception, false, false, null);
            }
        }

        return result;
    }

    @Override
    public JobStatus cancelJob(Job job) throws XenonException {
        SchedulerConnection connection = getConnection(job.getScheduler());

        return connection.cancelJob(job);
    }

    public void end() {
        SchedulerConnection[] currentConnections;

        synchronized (this) {
            Collection<SchedulerConnection> tmp = connections.values();
            currentConnections = tmp.toArray(new SchedulerConnection[tmp.size()]);
        }

        for (SchedulerConnection connection : currentConnections) {
            try {
                connection.close();
            } catch (XenonException e) {
                LOGGER.error("Error on closing connection to server", e);
            }
        }
    }

    @Override
    public Streams getStreams(Job job) throws XenonException {
        SchedulerConnection connection = getConnection(job.getScheduler());

        return connection.getStreams(job);
    }

    @Override
    public JobStatus waitUntilDone(Job job, long timeout) throws XenonException {
        SchedulerConnection connection = getConnection(job.getScheduler());

        return connection.waitUntilDone(job, timeout);
    }

    @Override
    public JobStatus waitUntilRunning(Job job, long timeout) throws XenonException {
        SchedulerConnection connection = getConnection(job.getScheduler());

        return connection.waitUntilRunning(job, timeout);
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
