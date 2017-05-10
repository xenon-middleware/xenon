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
package nl.esciencecenter.xenon.adaptors.gridengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.scripting.RemoteCommandRunner;
import nl.esciencecenter.xenon.adaptors.scripting.SchedulerConnection;
import nl.esciencecenter.xenon.adaptors.scripting.ScriptingAdaptor;
import nl.esciencecenter.xenon.adaptors.scripting.ScriptingParser;
import nl.esciencecenter.xenon.adaptors.slurm.SlurmAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.jobs.JobImplementation;
import nl.esciencecenter.xenon.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.xenon.engine.jobs.QueueStatusImplementation;
import nl.esciencecenter.xenon.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.xenon.files.RelativePath;
import nl.esciencecenter.xenon.jobs.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobCanceledException;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.NoSuchJobException;
import nl.esciencecenter.xenon.jobs.NoSuchQueueException;
import nl.esciencecenter.xenon.jobs.QueueStatus;
import nl.esciencecenter.xenon.jobs.Scheduler;
import nl.esciencecenter.xenon.jobs.Streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface to the GridEngine command line tools. Will run commands to submit/list/cancel jobs and get the status of queues.
 * 
 */
public class GridEngineSchedulerConnection extends SchedulerConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(GridEngineSchedulerConnection.class);

    public static final String JOB_OPTION_JOB_SCRIPT = "job.script";

    public static final String JOB_OPTION_PARALLEL_ENVIRONMENT = "parallel.environment";

    public static final String JOB_OPTION_PARALLEL_SLOTS = "parallel.slots";
    
    public static final String JOB_OPTION_RESOURCES = "resources";

    private static final String[] VALID_JOB_OPTIONS = new String[] { JOB_OPTION_JOB_SCRIPT, JOB_OPTION_PARALLEL_ENVIRONMENT,
            JOB_OPTION_PARALLEL_SLOTS, JOB_OPTION_RESOURCES };

    private static final String QACCT_HEADER = "==============================================================";

    protected static void verifyJobDescription(JobDescription description) throws XenonException {
        SchedulerConnection.verifyJobOptions(description.getJobOptions(), VALID_JOB_OPTIONS, GridEngineAdaptor.ADAPTOR_NAME);

        if (description.isInteractive()) {
            throw new InvalidJobDescriptionException(GridEngineAdaptor.ADAPTOR_NAME, "Adaptor does not support interactive jobs");
        }
        
        if (description.isStartSingleProcess()) {
            throw new InvalidJobDescriptionException(SlurmAdaptor.ADAPTOR_NAME,
                    "StartSingleProcess option not supported");
        }

        //check for option that overrides job script completely.
        if (description.getJobOptions().get(JOB_OPTION_JOB_SCRIPT) != null) {
            //no remaining settings checked.
            return;
        }

        //perform standard checks.
        SchedulerConnection.verifyJobDescription(description, GridEngineAdaptor.ADAPTOR_NAME);

        //check if the parallel environment and queue are specified.
        if (description.getNodeCount() != 1) {
            if (!description.getJobOptions().containsKey(JOB_OPTION_PARALLEL_ENVIRONMENT)) {
                throw new InvalidJobDescriptionException(GridEngineAdaptor.ADAPTOR_NAME,
                        "Parallel job requested but mandatory parallel.environment option not specificied.");
            }
            if (description.getQueueName() == null && !description.getJobOptions().containsKey(JOB_OPTION_PARALLEL_SLOTS)) {
                throw new InvalidJobDescriptionException(GridEngineAdaptor.ADAPTOR_NAME,
                        "Parallel job requested but neither queue nor number of slots specificied (at least one is required)");
            }
        }
    }

    @SuppressWarnings("PMD.EmptyIfStmt")
    protected static JobStatus getJobStatusFromQacctInfo(Map<String, String> info, Job job) throws XenonException {
        Integer exitcode;
        Exception exception = null;
        String state = "done";

        if (info == null) {
            return null;
        }

        SchedulerConnection.verifyJobInfo(info, job, GridEngineAdaptor.ADAPTOR_NAME, "jobnumber", "exit_status", "failed");

        String exitcodeString = info.get("exit_status");
        String failedString = info.get("failed");

        try {
            exitcode = Integer.parseInt(info.get("exit_status"));
        } catch (NumberFormatException e) {
            throw new XenonException(GridEngineAdaptor.ADAPTOR_NAME, "cannot parse exit code of job " + job.getIdentifier()
                    + " from string " + exitcodeString, e);
        }

        if (failedString.equals("0")) {
            //Success!
        } else if (failedString.startsWith("100")) {
            //error code for killed jobs
            exception = new JobCanceledException(GridEngineAdaptor.ADAPTOR_NAME, "Job killed by signal");
        } else {
            //unknown error code
            exception = new XenonException(GridEngineAdaptor.ADAPTOR_NAME, "Job reports error: " + failedString);
        }

        return new JobStatusImplementation(job, state, exitcode, exception, false, true, info);
    }

    protected static JobStatus getJobStatusFromQstatInfo(Map<String, Map<String, String>> info, Job job) throws XenonException {
        boolean done = false;
        Map<String, String> jobInfo = info.get(job.getIdentifier());

        if (jobInfo == null) {
            return null;
        }

        SchedulerConnection.verifyJobInfo(jobInfo, job, GridEngineAdaptor.ADAPTOR_NAME, "JB_job_number", "state", "long_state");

        String longState = jobInfo.get("long_state");
        String stateCode = jobInfo.get("state");

        Exception exception = null;
        if (stateCode.contains("E")) {
            exception = new XenonException(GridEngineAdaptor.ADAPTOR_NAME, "Job reports error state: " + stateCode);
            done = true;
        }

        return new JobStatusImplementation(job, longState, null, exception, "running".equals(longState), done, jobInfo);
    }

    private final long accountingGraceTime;

    /**
     * Map with the last seen time of jobs. There is a delay between jobs disappearing from the qstat queue output, and
     * information about this job appearing in the qacct output. Instead of throwing an exception, we allow for a certain grace
     * time. Jobs will report the status "pending" during this time. Typical delays are in the order of seconds.
     */
    private final Map<String, Long> lastSeenMap;

    //list of jobs we have killed before they even started. These will not end up in qacct, so we keep them here.
    private final Set<Long> deletedJobs;

    private final Scheduler scheduler;

    private final GridEngineXmlParser parser;

    private final GridEngineSetup setupInfo;

    GridEngineSchedulerConnection(ScriptingAdaptor adaptor, String scheme, String location, Credential credential, 
            XenonProperties properties, XenonEngine engine) throws XenonException {

        super(adaptor, scheme, location, credential, properties, engine, properties
                .getLongProperty(GridEngineAdaptor.POLL_DELAY_PROPERTY));

        boolean ignoreVersion = properties.getBooleanProperty(GridEngineAdaptor.IGNORE_VERSION_PROPERTY);
        accountingGraceTime = properties.getLongProperty(GridEngineAdaptor.ACCOUNTING_GRACE_TIME_PROPERTY);

        parser = new GridEngineXmlParser(ignoreVersion);

        lastSeenMap = new HashMap<>();
        deletedJobs = new HashSet<>();

        //will run a few commands to fetch info
        setupInfo = new GridEngineSetup(this);

        scheduler = new SchedulerImplementation(GridEngineAdaptor.ADAPTOR_NAME, getID(), scheme, location, 
                setupInfo.getQueueNames(), credential, getProperties(), false, false, true);
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public String[] getQueueNames() {
        return setupInfo.getQueueNames();
    }

    @Override
    public String getDefaultQueueName() {
        return null;
    }

    private synchronized void updateJobsSeenMap(Set<String> identifiers) {
        long currentTime = System.currentTimeMillis();

        for (String identifier : identifiers) {
            lastSeenMap.put(identifier, currentTime);
        }

        long expiredTime = currentTime + accountingGraceTime;

        Iterator<Entry<String, Long>> iterator = lastSeenMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<String, Long> entry = iterator.next();

            if (entry.getValue() > expiredTime) {
                iterator.remove();
            }
        }

    }

    private synchronized boolean haveRecentlySeen(String identifier) {
        if (!lastSeenMap.containsKey(identifier)) {
            return false;
        }

        return (lastSeenMap.get(identifier) + accountingGraceTime) > System.currentTimeMillis();
    }

    private synchronized void addDeletedJob(Job job) {
        deletedJobs.add(Long.parseLong(job.getIdentifier()));
    }

    /*
     * Note: Works exactly once per job.
     */
    private synchronized boolean jobWasDeleted(Job job) {
        //optimization of common case
        if (deletedJobs.isEmpty()) {
            return false;
        }
        return deletedJobs.remove(Long.parseLong(job.getIdentifier()));
    }

    private void jobsFromStatus(String statusOutput, Scheduler scheduler, List<Job> result) throws XenonException {
        Map<String, Map<String, String>> status = parser.parseJobInfos(statusOutput);

        updateJobsSeenMap(status.keySet());

        for (String jobID : status.keySet()) {
            result.add(new JobImplementation(scheduler, jobID, false, false));
        }

    }

    @Override
    public Job[] getJobs(String... queueNames) throws XenonException {
        ArrayList<Job> result = new ArrayList<>();

        if (queueNames == null || queueNames.length == 0) {
            String statusOutput = runCheckedCommand(null, "qstat", "-xml");

            jobsFromStatus(statusOutput, getScheduler(), result);
        } else {
            for (String queueName : queueNames) {
                RemoteCommandRunner runner = runCommand(null, "qstat", "-xml", "-q", queueName);

                if (runner.success()) {
                    jobsFromStatus(runner.getStdout(), getScheduler(), result);
                } else if (runner.getExitCode() == 1) {
                    //sge returns "1" as the exit code if there is something wrong with the queue, ignore
                    LOGGER.warn("Failed to get queue status for queue " + runner);
                    throw new NoSuchQueueException(GridEngineAdaptor.ADAPTOR_NAME, "Failed to get queue status for queue \""
                            + queueName + "\": " + runner);                    
                } else {
                    throw new XenonException(GridEngineAdaptor.ADAPTOR_NAME, "Failed to get queue status for queue \""
                            + queueName + "\": " + runner);
                }
            }
        }

        return result.toArray(new Job[result.size()]);
    }

    @Override
    public QueueStatus getQueueStatus(String queueName) throws XenonException {
        String qstatOutput = runCheckedCommand(null, "qstat", "-xml", "-g", "c");

        Map<String, Map<String, String>> allMap = parser.parseQueueInfos(qstatOutput);

        Map<String, String> map = allMap.get(queueName);

        if (map == null || map.isEmpty()) {
            throw new NoSuchQueueException(GridEngineAdaptor.ADAPTOR_NAME, "Cannot get status of queue \"" + queueName
                    + "\" from server, perhaps it does not exist?");
        }

        return new QueueStatusImplementation(getScheduler(), queueName, null, map);
    }

    @Override
    public QueueStatus[] getQueueStatuses(String... queueNames) throws XenonException {
        if (queueNames == null) {
            throw new IllegalArgumentException("Queue names cannot be null");
        }

        if (queueNames.length == 0) {
            queueNames = getQueueNames();
        }

        QueueStatus[] result = new QueueStatus[queueNames.length];

        String qstatOutput = runCheckedCommand(null, "qstat", "-xml", "-g", "c");

        Map<String, Map<String, String>> allMap = parser.parseQueueInfos(qstatOutput);

        for (int i = 0; i < queueNames.length; i++) {
            if (queueNames[i] == null) {
                result[i] = null;
            } else {
                //state for only the requested queue
                Map<String, String> map = allMap.get(queueNames[i]);

                if (map == null || map.isEmpty()) {
                    Exception exception = new NoSuchQueueException(GridEngineAdaptor.ADAPTOR_NAME,
                            "Cannot get status of queue \"" + queueNames[i] + "\" from server, perhaps it does not exist?");
                    result[i] = new QueueStatusImplementation(getScheduler(), queueNames[i], exception, null);
                } else {
                    result[i] = new QueueStatusImplementation(getScheduler(), queueNames[i], null, map);
                }
            }
        }

        return result;

    }

    @Override
    public Job submitJob(JobDescription description) throws XenonException {
        String output;
        RelativePath fsEntryPath = getFsEntryPath().getRelativePath();
        
        verifyJobDescription(description);

        //check for option that overrides job script completely.
        String customScriptFile = description.getJobOptions().get(JOB_OPTION_JOB_SCRIPT);

        if (customScriptFile == null) {
            String jobScript = GridEngineJobScriptGenerator.generate(description, fsEntryPath, setupInfo);

            output = runCheckedCommand(jobScript, "qsub");
        } else {
            //the user gave us a job script. Pass it to qsub as-is

            //convert to absolute path if needed
            if (!customScriptFile.startsWith("/")) {
                RelativePath scriptFile = fsEntryPath.resolve(customScriptFile);
                customScriptFile = scriptFile.getAbsolutePath();
            }

            output = runCheckedCommand(null, "qsub", customScriptFile);
        }

        String identifier = ScriptingParser.parseJobIDFromLine(output, GridEngineAdaptor.ADAPTOR_NAME, "Your job");

        updateJobsSeenMap(Collections.singleton(identifier));

        return new JobImplementation(getScheduler(), identifier, description, false, false);
    }

    @Override
    public JobStatus cancelJob(Job job) throws XenonException {
        String identifier = job.getIdentifier();
        String qdelOutput = runCheckedCommand(null, "qdel", identifier);

        String killedOutput = "has registered the job " + identifier + " for deletion";
        String deletedOutput = "has deleted job " + identifier;

        int matched = ScriptingParser.checkIfContains(qdelOutput, GridEngineAdaptor.ADAPTOR_NAME, killedOutput, deletedOutput);

        //keep track of the deleted jobs.
        if (matched == 1) {
            addDeletedJob(job);
        } else {
            //it will take a while to get this job to the accounting. Remember it existed for now
            updateJobsSeenMap(Collections.singleton(identifier));
        }

        return getJobStatus(job);
    }

    private Map<String, Map<String, String>> getQstatInfo() throws XenonException {
        RemoteCommandRunner runner = runCommand(null, "qstat", "-xml");

        if (!runner.success()) {
            LOGGER.debug("failed to get job status {}", runner);
            return new HashMap<>(0);
        }

        Map<String, Map<String, String>> result = parser.parseJobInfos(runner.getStdout());

        //mark jobs we found as seen, in case they disappear from the queue
        updateJobsSeenMap(result.keySet());

        return result;
    }

    private Map<String, String> getQacctInfo(Job job) throws XenonException {
        RemoteCommandRunner runner = runCommand(null, "qacct", "-j", job.getIdentifier());

        if (!runner.success()) {
            LOGGER.debug("failed to get job status {}", runner);
            return null;
        }

        return ScriptingParser.parseKeyValueLines(runner.getStdout(), ScriptingParser.WHITESPACE_REGEX,
                GridEngineAdaptor.ADAPTOR_NAME, QACCT_HEADER);
    }

    /**
     * Get job status. First checks given qstat info map, but also runs additional qacct and qdel commands if needed.
     * 
     * @param qstatInfo
     *            the info to get the job status from.
     * @param job
     *            the job to get the status for.
     * @return the JobStatus of the job.
     * @throws XenonException
     *             in case the info is not valid.
     * @throws XenonException
     *             in case an additional command fails to run.
     */
    private JobStatus getJobStatus(Map<String, Map<String, String>> qstatInfo, Job job) throws XenonException {

        if (job == null) {
            return null;
        }

        JobStatus status = getJobStatusFromQstatInfo(qstatInfo, job);

        if (status != null && status.hasException()) {
            cancelJob(job);
            status = null;
        }

        if (status == null) {
            Map<String, String> qacctInfo = getQacctInfo(job);
            status = getJobStatusFromQacctInfo(qacctInfo, job);
        }

        //perhaps the job was killed while it was not running yet ("deleted", in sge speak). This will make it disappear from
        //qstat/qacct output completely
        if (status == null && jobWasDeleted(job)) {
            Exception exception = new JobCanceledException(GridEngineAdaptor.ADAPTOR_NAME, "Job " + job.getIdentifier()
                    + " deleted by user while still pending");
            status = new JobStatusImplementation(job, "killed", null, exception, false, true, null);
        }

        //this job is neither in qstat nor qacct output. we assume it is "in between" for a certain grace time.
        if (status == null && haveRecentlySeen(job.getIdentifier())) {
            status = new JobStatusImplementation(job, "unknown", null, null, false, false, new HashMap<String, String>());
        }

        return status;
    }

    @Override
    public JobStatus getJobStatus(Job job) throws XenonException {
        
        if (job == null) { 
            throw new NoSuchJobException(GridEngineAdaptor.ADAPTOR_NAME, "Job <null> not found on server");
        }
        
        Map<String, Map<String, String>> info = getQstatInfo();

        JobStatus result = getJobStatus(info, job);

        //this job really does not exist. throw an exception
        if (result == null) {
            throw new NoSuchJobException(GridEngineAdaptor.ADAPTOR_NAME, "Job " + job.getIdentifier() + " not found on server");
        }

        return result;
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) throws XenonException {
        Map<String, Map<String, String>> info = getQstatInfo();

        JobStatus[] result = new JobStatus[jobs.length];

        for (int i = 0; i < result.length; i++) {
            if (jobs[i] == null) {
                result[i] = null;
            } else {
                result[i] = getJobStatus(info, jobs[i]);

                //this job really does not exist. set it to an error state.
                if (result[i] == null) {
                    Exception exception = new NoSuchJobException(GridEngineAdaptor.ADAPTOR_NAME, "Job " + jobs[i].getIdentifier()
                            + " not found on server");
                    result[i] = new JobStatusImplementation(jobs[i], null, null, exception, false, false, null);
                }
            }
        }
        return result;
    }

    @Override
    public Streams getStreams(Job job) throws XenonException {
        throw new XenonException(GridEngineAdaptor.ADAPTOR_NAME, "does not support interactive jobs");
    }

}
