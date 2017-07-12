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
package nl.esciencecenter.xenon.adaptors.schedulers.gridengine;

import static nl.esciencecenter.xenon.adaptors.schedulers.gridengine.GridEngineSchedulerAdaptor.ACCOUNTING_GRACE_TIME_PROPERTY;
import static nl.esciencecenter.xenon.adaptors.schedulers.gridengine.GridEngineSchedulerAdaptor.ADAPTOR_NAME;
import static nl.esciencecenter.xenon.adaptors.schedulers.gridengine.GridEngineSchedulerAdaptor.IGNORE_VERSION_PROPERTY;
import static nl.esciencecenter.xenon.adaptors.schedulers.gridengine.GridEngineSchedulerAdaptor.POLL_DELAY_PROPERTY;
import static nl.esciencecenter.xenon.adaptors.schedulers.gridengine.GridEngineSchedulerAdaptor.VALID_PROPERTIES;
import static nl.esciencecenter.xenon.adaptors.schedulers.gridengine.GridEngineUtils.JOB_OPTION_JOB_SCRIPT;
import static nl.esciencecenter.xenon.adaptors.schedulers.gridengine.GridEngineUtils.QACCT_HEADER;
import static nl.esciencecenter.xenon.adaptors.schedulers.gridengine.GridEngineUtils.generate;
import static nl.esciencecenter.xenon.adaptors.schedulers.gridengine.GridEngineUtils.getJobStatusFromQacctInfo;
import static nl.esciencecenter.xenon.adaptors.schedulers.gridengine.GridEngineUtils.getJobStatusFromQstatInfo;
import static nl.esciencecenter.xenon.adaptors.schedulers.gridengine.GridEngineUtils.verifyJobDescription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.JobCanceledException;
import nl.esciencecenter.xenon.adaptors.schedulers.JobImplementation;
import nl.esciencecenter.xenon.adaptors.schedulers.RemoteCommandRunner;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingParser;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingScheduler;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobHandle;
import nl.esciencecenter.xenon.schedulers.JobStatus;
import nl.esciencecenter.xenon.schedulers.NoSuchJobException;
import nl.esciencecenter.xenon.schedulers.NoSuchQueueException;
import nl.esciencecenter.xenon.schedulers.QueueStatus;
import nl.esciencecenter.xenon.schedulers.Streams;

/**
 * Interface to the GridEngine command line tools. Will run commands to submit/list/cancel jobs and get the status of queues.
 * 
 */
public class GridEngineScheduler extends ScriptingScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GridEngineScheduler.class);
   
    private final long accountingGraceTime;

    /**
     * Map with the last seen time of jobs. There is a delay between jobs disappearing from the qstat queue output, and
     * information about this job appearing in the qacct output. Instead of throwing an exception, we allow for a certain grace
     * time. Jobs will report the status "pending" during this time. Typical delays are in the order of seconds.
     */
    private final Map<String, Long> lastSeenMap;

    //list of jobs we have killed before they even started. These will not end up in qacct, so we keep them here.
    private final Set<Long> deletedJobs;

    private final GridEngineXmlParser parser;

    private final GridEngineSetup setupInfo;

    protected GridEngineScheduler(String uniqueID, String location, Credential credential, Map<String,String> prop) 
            throws XenonException {

        super(uniqueID, ADAPTOR_NAME, location, credential, true, false, prop, VALID_PROPERTIES, POLL_DELAY_PROPERTY);

        boolean ignoreVersion = properties.getBooleanProperty(IGNORE_VERSION_PROPERTY);
        accountingGraceTime = properties.getLongProperty(ACCOUNTING_GRACE_TIME_PROPERTY);

        parser = new GridEngineXmlParser(ignoreVersion);

        lastSeenMap = new HashMap<>();
        deletedJobs = new HashSet<>();
        
        // Run a few commands to fetch info about the queue
        this.setupInfo = new GridEngineSetup(this);
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

    private synchronized void addDeletedJob(JobHandle job) {
        deletedJobs.add(Long.parseLong(job.getIdentifier()));
    }

    /*
     * Note: Works exactly once per job.
     */
    private synchronized boolean jobWasDeleted(JobHandle job) {
        //optimization of common case
        if (deletedJobs.isEmpty()) {
            return false;
        }
        return deletedJobs.remove(Long.parseLong(job.getIdentifier()));
    }

    private void jobsFromStatus(String statusOutput, List<JobHandle> result) throws XenonException {
        Map<String, Map<String, String>> status = parser.parseJobInfos(statusOutput);

        updateJobsSeenMap(status.keySet());

        for (String jobID : status.keySet()) {
            result.add(new JobImplementation(this, jobID));
        }

    }

    @Override
    public JobHandle[] getJobs(String... queueNames) throws XenonException {
        ArrayList<JobHandle> result = new ArrayList<>();

        if (queueNames == null || queueNames.length == 0) {
            String statusOutput = runCheckedCommand(null, "qstat", "-xml");

            jobsFromStatus(statusOutput, result);
        } else {
            for (String queueName : queueNames) {
                RemoteCommandRunner runner = runCommand(null, "qstat", "-xml", "-q", queueName);

                if (runner.success()) {
                    jobsFromStatus(runner.getStdout(), result);
                } else if (runner.getExitCode() == 1) {
                    //sge returns "1" as the exit code if there is something wrong with the queue, ignore
                    LOGGER.warn("Failed to get queue status for queue " + runner);
                    throw new NoSuchQueueException(ADAPTOR_NAME, "Failed to get queue status for queue \""
                            + queueName + "\": " + runner);                    
                } else {
                    throw new XenonException(ADAPTOR_NAME, "Failed to get queue status for queue \""
                            + queueName + "\": " + runner);
                }
            }
        }

        return result.toArray(new JobHandle[result.size()]);
    }

    @Override
    public QueueStatus getQueueStatus(String queueName) throws XenonException {
        String qstatOutput = runCheckedCommand(null, "qstat", "-xml", "-g", "c");

        Map<String, Map<String, String>> allMap = parser.parseQueueInfos(qstatOutput);

        Map<String, String> map = allMap.get(queueName);

        if (map == null || map.isEmpty()) {
            throw new NoSuchQueueException(ADAPTOR_NAME, "Cannot get status of queue \"" + queueName
                    + "\" from server, perhaps it does not exist?");
        }

        return new QueueStatus(this, queueName, null, map);
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
                    Exception exception = new NoSuchQueueException(ADAPTOR_NAME,
                            "Cannot get status of queue \"" + queueNames[i] + "\" from server, perhaps it does not exist?");
                    result[i] = new QueueStatus(this, queueNames[i], exception, null);
                } else {
                    result[i] = new QueueStatus(this, queueNames[i], null, map);
                }
            }
        }

        return result;

    }

    @Override
    public JobHandle submitJob(JobDescription description) throws XenonException {
        String output;
        Path fsEntryPath = getFsEntryPath();
        
        verifyJobDescription(description);

        //check for option that overrides job script completely.
        String customScriptFile = description.getJobOptions().get(JOB_OPTION_JOB_SCRIPT);

        if (customScriptFile == null) {
            String jobScript = generate(description, fsEntryPath, setupInfo);

            output = runCheckedCommand(jobScript, "qsub");
        } else {
            //the user gave us a job script. Pass it to qsub as-is

            //convert to absolute path if needed
            if (!customScriptFile.startsWith("/")) {
                Path scriptFile = fsEntryPath.resolve(customScriptFile);
                customScriptFile = scriptFile.getAbsolutePath();
            }

            output = runCheckedCommand(null, "qsub", customScriptFile);
        }

        String identifier = ScriptingParser.parseJobIDFromLine(output, ADAPTOR_NAME, "Your job");

        updateJobsSeenMap(Collections.singleton(identifier));

        return new JobImplementation(this, identifier, description);
    }

    @Override
    public JobStatus cancelJob(JobHandle job) throws XenonException {
        String identifier = job.getIdentifier();
        String qdelOutput = runCheckedCommand(null, "qdel", identifier);

        String killedOutput = "has registered the job " + identifier + " for deletion";
        String deletedOutput = "has deleted job " + identifier;

        int matched = ScriptingParser.checkIfContains(qdelOutput, ADAPTOR_NAME, killedOutput, deletedOutput);

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

    private Map<String, String> getQacctInfo(JobHandle job) throws XenonException {
        RemoteCommandRunner runner = runCommand(null, "qacct", "-j", job.getIdentifier());

        if (!runner.success()) {
            LOGGER.debug("failed to get job status {}", runner);
            return null;
        }

        return ScriptingParser.parseKeyValueLines(runner.getStdout(), ScriptingParser.WHITESPACE_REGEX,
                ADAPTOR_NAME, QACCT_HEADER);
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
    private JobStatus getJobStatus(Map<String, Map<String, String>> qstatInfo, JobHandle job) throws XenonException {

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
            Exception exception = new JobCanceledException(ADAPTOR_NAME, "Job " + job.getIdentifier()
                    + " deleted by user while still pending");
            status = new JobStatus(job, "killed", null, exception, false, true, null);
        }

        //this job is neither in qstat nor qacct output. we assume it is "in between" for a certain grace time.
        if (status == null && haveRecentlySeen(job.getIdentifier())) {
            status = new JobStatus(job, "unknown", null, null, false, false, new HashMap<String, String>());
        }

        return status;
    }

    @Override
    public JobStatus getJobStatus(JobHandle job) throws XenonException {
        
        if (job == null) { 
            throw new NoSuchJobException(ADAPTOR_NAME, "Job <null> not found on server");
        }
        
        Map<String, Map<String, String>> info = getQstatInfo();

        JobStatus result = getJobStatus(info, job);

        //this job really does not exist. throw an exception
        if (result == null) {
            throw new NoSuchJobException(ADAPTOR_NAME, "Job " + job.getIdentifier() + " not found on server");
        }

        return result;
    }

    @Override
    public JobStatus[] getJobStatuses(JobHandle... jobs) throws XenonException {
        Map<String, Map<String, String>> info = getQstatInfo();

        JobStatus[] result = new JobStatus[jobs.length];

        for (int i = 0; i < result.length; i++) {
            if (jobs[i] == null) {
                result[i] = null;
            } else {
                result[i] = getJobStatus(info, jobs[i]);

                //this job really does not exist. set it to an error state.
                if (result[i] == null) {
                    Exception exception = new NoSuchJobException(ADAPTOR_NAME, "Job " + jobs[i].getIdentifier()
                            + " not found on server");
                    result[i] = new JobStatus(jobs[i], null, null, exception, false, false, null);
                }
            }
        }
        return result;
    }

    @Override
    public Streams getStreams(JobHandle job) throws XenonException {
        throw new XenonException(ADAPTOR_NAME, "does not support interactive jobs");
    }

	@Override
	public boolean isOpen() throws XenonException {
		// TODO Auto-generated method stub
		return false;
	}

}
