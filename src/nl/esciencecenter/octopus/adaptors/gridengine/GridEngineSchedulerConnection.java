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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.QueueStatusImplementation;
import nl.esciencecenter.octopus.exceptions.NoSuchQueueException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.QueueStatus;

public class GridEngineSchedulerConnection extends AbstractSchedulerConnection {

    private final QstatOutputParser parser;

    public static final int QACCT_GRACE_TIME = 60000; //ms, 1 minute

    /**
     * Map with the last seen time of jobs. There is a short but noticeable delay between jobs disappearing from the qstat queue
     * output, and information about this job appearing in the qacct output. Instead of throwing an exception, we allow for a
     * certain grace time. Jobs will report the status "pending" during this time.
     */
    private final HashMap<String, Long> lastSeenMap;

    public GridEngineSchedulerConnection(URI location, Credential credential, Properties properties, OctopusEngine engine)
            throws OctopusIOException, OctopusException {
        super(location, credential, properties, engine);

        parser = new QstatOutputParser(this.properties);

        lastSeenMap = new HashMap<String, Long>();
    }

    private synchronized void markJobSeen(String identifier) {
        long current = System.currentTimeMillis();

        lastSeenMap.put(identifier, current);
    }

    private synchronized void markJobsSeen(Set<String> identifiers) {
        long current = System.currentTimeMillis();

        for (String identifier : identifiers) {
            lastSeenMap.put(identifier, current);
        }
    }

    private synchronized void clearJobSeen(String identifier) {
        lastSeenMap.remove(identifier);
    }

    private synchronized boolean haveRecentlySeen(String identifier) {
        if (!lastSeenMap.containsKey(identifier)) {
            return false;
        }
        return System.currentTimeMillis() < (lastSeenMap.get(identifier) + QACCT_GRACE_TIME);
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) throws OctopusException, OctopusIOException {
        JobStatus[] result = new JobStatus[jobs.length];

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

        String statusOutput = runCommand(null, "qstat", "-xml");

        Map<String, Map<String, String>> allMap = parser.parseJobInfos(statusOutput);

        markJobsSeen(allMap.keySet());

        Map<String, String> map = allMap.get(job.getIdentifier());

        if (map == null || map.isEmpty()) {
            //perhaps the job is already finished?
            String output = runCommand(null, "qacct", "-j", job.getIdentifier());

            Map<String, String> accountingInfo = QAcctOutputParser.getJobAccountingInfo(output);

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

                clearJobSeen(job.getIdentifier());
                return new JobStatusImplementation(job, "done", exitCode, null, false, true, accountingInfo);
            } else if (haveRecentlySeen(job.getIdentifier())) {
                return new JobStatusImplementation(job, "pending", null, null, false, true, accountingInfo);
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

        markJobSeen(job.getIdentifier());
        return new JobStatusImplementation(job, state, null, null, state.equals("running"), false, map);
    }

    /**
     * Submit a job to a GridEngine machine. Mostly involves parsing output
     * 
     * @param description
     *            the script to submit
     * @return the id of the job
     * @throws OctopusException
     *             if the qsub command could not be run
     * @throws OctopusIOException
     */
    @Override
    public Job submitJob(JobDescription description) throws OctopusException, OctopusIOException {
        String jobScript = JobScriptGenerator.generate(description);

        String output = runCommand(jobScript, "qsub");

        String identifier = QsubOutputParser.checkSubmitJobResult(output);

        markJobSeen(identifier);
        return new JobImplementation(schedulerImplementation, identifier, description, false, false);
    }

    /**
     * Cancel a job.
     * 
     * @param job
     *            the job to cancel
     * @return
     * @throws OctopusException
     *             if the qsub command could not be run
     * @throws OctopusIOException
     *             if the qsub command could not be run, or the jobID could not be read from the output
     */
    @Override
    public JobStatus cancelJob(Job job) throws OctopusIOException, OctopusException {
        String output = runCommand(null, "qdel", job.getIdentifier());

        QdelOutputParser.checkCancelJobResult(job, output);

        return null;
    }

    @Override
    String[] getQueueNamesFromServer() throws OctopusIOException, OctopusException {
        String qstatOutput = runCommand(null, "qstat", "-xml", "-g", "c");

        //temporary parser to get around bootstrapping problem.
        QstatOutputParser parser = new QstatOutputParser(getProperties());

        Map<String, Map<String, String>> allMap = parser.parseQueueInfos(qstatOutput);

        return allMap.keySet().toArray(new String[0]);
    }

    @Override
    QueueStatus[] getQueueStatusesFromServer(String[] queueNames) throws OctopusIOException, OctopusException {

        QueueStatus[] result = new QueueStatus[queueNames.length];

        String qstatOutput = runCommand(null, "qstat", "-xml", "-g", "c");

        Map<String, Map<String, String>> allMap = parser.parseQueueInfos(qstatOutput);

        for (int i = 0; i < queueNames.length; i++) {
            if (allMap == null || allMap.isEmpty()) {
                Exception exception =
                        new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Failed to get status of queues on server");
                result[i] = new QueueStatusImplementation(getScheduler(), queueNames[i], exception, null);
            }

            //state for only the requested job
            Map<String, String> map = allMap.get(queueNames[i]);

            if (map == null || map.isEmpty()) {
                Exception exception =
                        new NoSuchQueueException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get status of queue " + queueNames[i]
                                + " from server");
                result[i] = new QueueStatusImplementation(getScheduler(), queueNames[i], exception, null);
            }

            result[i] = new QueueStatusImplementation(getScheduler(), queueNames[i], null, map);
        }

        return result;
    }

    @Override
    public Job[] getJobsFromServer(String[] queueNames) throws OctopusIOException, OctopusException {
        String statusOutput = runCommand(null, "qstat", "-xml");

        Map<String, Map<String, String>> status = parser.parseJobInfos(statusOutput);

        String[] jobIDs = status.keySet().toArray(new String[0]);

        Job[] result = new Job[jobIDs.length];

        for (int i = 0; i < result.length; i++) {
            markJobSeen(jobIDs[i]);
            result[i] = new JobImplementation(schedulerImplementation, jobIDs[i], null, false, false);
        }

        return result;
    }

};
