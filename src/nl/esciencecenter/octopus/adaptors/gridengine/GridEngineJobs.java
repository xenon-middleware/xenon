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
package nl.esciencecenter.octopus.adaptors.gridengine;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.QueueStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.octopus.exceptions.NoSuchQueueException;
import nl.esciencecenter.octopus.exceptions.NoSuchSchedulerException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.OctopusRuntimeException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.jobs.Streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GridEngineJobs implements Jobs {

    private static final Logger logger = LoggerFactory.getLogger(GridEngineJobs.class);

    private final OctopusProperties properties;

    private final OctopusEngine octopusEngine;

    private final Map<String, SchedulerConnection> connections;

    public GridEngineJobs(OctopusProperties properties, OctopusEngine octopusEngine) {
        this.properties = properties;
        this.octopusEngine = octopusEngine;

        connections = new HashMap<String, SchedulerConnection>();

    }

    private synchronized SchedulerConnection getConnection(Scheduler scheduler) throws OctopusRuntimeException,
            NoSuchSchedulerException {
        if (!(scheduler instanceof SchedulerImplementation)) {
            throw new OctopusRuntimeException(GridengineAdaptor.ADAPTOR_NAME, "scheduler " + scheduler.toString()
                    + " not created by this adaptor");
        }

        String schedulerID = ((SchedulerImplementation) scheduler).getUniqueID();

        if (!connections.containsKey(schedulerID)) {
            throw new NoSuchSchedulerException(GridengineAdaptor.ADAPTOR_NAME, "cannot find scheduler " + scheduler.toString()
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
    public Scheduler newScheduler(URI location, Credential credential, Properties properties) throws OctopusException,
            OctopusIOException {
        SchedulerConnection connection = new SchedulerConnection(location, credential, properties);

        Scheduler result =
                new SchedulerImplementation(GridengineAdaptor.ADAPTOR_NAME, connection.getID(), location,
                        connection.getQueueNames(), credential, connection.getProperties(), false, false, true);

        addConnection(connection);

        return result;
    }

    @Override
    public synchronized boolean isOpen(Scheduler scheduler) throws OctopusException, OctopusIOException {
        return connections.containsKey(scheduler);
    }

    @Override
    public void close(Scheduler scheduler) throws OctopusException, OctopusIOException {
        SchedulerConnection connection = getConnection(scheduler);

        connection.close();

        removeConnection(connection);
    }

    @Override
    public Scheduler getLocalScheduler() throws OctopusException, OctopusIOException {
        throw new OctopusRuntimeException(GridengineAdaptor.ADAPTOR_NAME,
                "Error in engine: getLocalScheduler() should not be called on this adaptor");
    }

    private QueueStatus getQueueStatusFromMap(Map<String, Map<String, String>> allMap, Scheduler scheduler, String queue) {
        if (allMap == null || allMap.isEmpty()) {
            Exception exception =
                    new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Failed to get status of queues on server");
            return new QueueStatusImplementation(scheduler, queue, exception, null);
        }

        //state for only the requested job
        Map<String, String> map = allMap.get(queue);

        if (map == null || map.isEmpty()) {
            Exception exception =
                    new NoSuchQueueException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get status of queue " + queue
                            + " from server");
            return new QueueStatusImplementation(scheduler, queue, exception, null);
        }

        return new QueueStatusImplementation(scheduler, queue, null, map);
    }

    @Override
    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws OctopusException, OctopusIOException {
        SchedulerConnection connection = getConnection(scheduler);

        Map<String, Map<String, String>> queueStatusMap = connection.getQueueStatus();

        return getQueueStatusFromMap(queueStatusMap, scheduler, queueName);
    }

    @Override
    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws OctopusException, OctopusIOException {
        SchedulerConnection connection = getConnection(scheduler);

        Map<String, Map<String, String>> queueStatusMap = connection.getQueueStatus();

        QueueStatus[] result = new QueueStatus[queueNames.length];

        for (int i = 0; i < queueNames.length; i++) {
            result[i] = getQueueStatusFromMap(queueStatusMap, scheduler, queueNames[i]);
        }

        return result;
    }

    @Override
    public Job[] getJobs(Scheduler scheduler, String... queueName) throws OctopusException, OctopusIOException {
        //FIXME: returns all jobs in the queue, not just those from the current user.
        SchedulerConnection connection = getConnection(scheduler);
        Map<String, Map<String, String>> status = connection.getJobStatus();

        String[] jobIDs = status.keySet().toArray(new String[0]);

        Job[] result = new Job[jobIDs.length];

        for (int i = 0; i < result.length; i++) {
            result[i] = new JobImplementation(null, scheduler, OctopusEngine.getNextUUID(), jobIDs[i], false, false);
        }

        return result;
    }

    private JobStatus getJobStatusFromMap(Map<String, Map<String, String>> allMap, Job job) {
        if (allMap == null || allMap.isEmpty()) {
            Exception exception =
                    new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Failed to get status of jobs on server");
            return new JobStatusImplementation(job, null, null, exception, false, false, null);
        }

        //state for only the requested job
        Map<String, String> map = allMap.get(job.getIdentifier());

        if (map == null || map.isEmpty()) {
            Exception exception =
                    new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Job " + job.getIdentifier() + " not found on server");
            return new JobStatusImplementation(job, null, null, exception, false, false, null);
        }

        String state = map.get("state");

        if (state == null || state.length() == 0) {
            Exception exception =
                    new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "State for job " + job.getIdentifier()
                            + " not found on server");
            return new JobStatusImplementation(job, null, null, exception, false, false, map);
        }

        //FIXME: add isDone and exitcode for job
        return new JobStatusImplementation(job, state, null, null, state.equals("running"), false, map);
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) {
        JobStatus[] result = new JobStatus[jobs.length];

        //FIXME: perhaps we should do this collectively per scheduler
        for (int i = 0; i < jobs.length; i++) {
            try {
                result[i] = getJobStatus(jobs[i]);
            } catch (OctopusIOException | OctopusException e) {
                result[i] = new JobStatusImplementation(jobs[i], null, null, e, false, false, null);
            }
        }

        return result;
    }

    @Override
    public JobStatus getJobStatus(Job job) throws OctopusException, OctopusIOException {
        SchedulerConnection connection = getConnection(job.getScheduler());

        Map<String, Map<String, String>> allMap = connection.getJobStatus();

        Map<String, String> map = allMap.get(job.getIdentifier());

        if (map == null || map.isEmpty()) {
            //perhaps the job is already finished?
            Map<String, String> accountingInfo = connection.getJobAccountingInfo(job.getIdentifier());

            if (accountingInfo != null) {
                Integer exitCode = null;

                String exitCodeString = accountingInfo.get("exit_status");

                try {
                    if (exitCodeString != null) {
                        exitCode = Integer.parseInt(exitCodeString);
                    }
                } catch (NumberFormatException e) {
                    throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "cannot parse exit code of job "
                            + job.getIdentifier() + " from string " + exitCodeString, e);

                }

                return new JobStatusImplementation(job, "done", exitCode, null, false, true, accountingInfo);

            } else {
                Exception exception =
                        new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Job " + job.getIdentifier()
                                + " not found on server");
                return new JobStatusImplementation(job, null, null, exception, false, false, null);
            }
        }

        String state = map.get("state");

        if (state == null || state.length() == 0) {
            Exception exception =
                    new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "State for job " + job.getIdentifier()
                            + " not found on server");
            return new JobStatusImplementation(job, null, null, exception, false, false, map);
        }

        //FIXME: add isDone and exitcode for job
        return new JobStatusImplementation(job, state, null, null, state.equals("RUNNING"), false, map);
    }

    @Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException, OctopusIOException {
        SchedulerConnection connection = getConnection(scheduler);

        String jobScript = JobScriptGenerator.generate(description);

        String identifier = connection.submitJob(jobScript);

        return new JobImplementation(description, scheduler, OctopusEngine.getNextUUID(), identifier, false, false);
    }

    @Override
    public void cancelJob(Job job) throws OctopusException, OctopusIOException {
        SchedulerConnection connection = getConnection(job.getScheduler());

        connection.cancelJob(job);
    }

    public void end() {
        // TODO Auto-generated method stub

    }

    @Override
    public Streams getStreams(Job job) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }
}
