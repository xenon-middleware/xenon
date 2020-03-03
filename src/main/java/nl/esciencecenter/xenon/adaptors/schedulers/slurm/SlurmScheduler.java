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
package nl.esciencecenter.xenon.adaptors.schedulers.slurm;

import static nl.esciencecenter.xenon.adaptors.schedulers.slurm.SlurmSchedulerAdaptor.ADAPTOR_NAME;
import static nl.esciencecenter.xenon.adaptors.schedulers.slurm.SlurmSchedulerAdaptor.DISABLE_ACCOUNTING_USAGE;
import static nl.esciencecenter.xenon.adaptors.schedulers.slurm.SlurmSchedulerAdaptor.POLL_DELAY_PROPERTY;
import static nl.esciencecenter.xenon.adaptors.schedulers.slurm.SlurmSchedulerAdaptor.SLURM_UPDATE_SLEEP;
import static nl.esciencecenter.xenon.adaptors.schedulers.slurm.SlurmSchedulerAdaptor.SLURM_UPDATE_TIMEOUT;
import static nl.esciencecenter.xenon.adaptors.schedulers.slurm.SlurmUtils.generate;
import static nl.esciencecenter.xenon.adaptors.schedulers.slurm.SlurmUtils.generateInteractiveArguments;
import static nl.esciencecenter.xenon.adaptors.schedulers.slurm.SlurmUtils.getJobStatusFromSacctInfo;
import static nl.esciencecenter.xenon.adaptors.schedulers.slurm.SlurmUtils.getJobStatusFromScontrolInfo;
import static nl.esciencecenter.xenon.adaptors.schedulers.slurm.SlurmUtils.getJobStatusFromSqueueInfo;
import static nl.esciencecenter.xenon.adaptors.schedulers.slurm.SlurmUtils.getQueueStatusFromSInfo;
import static nl.esciencecenter.xenon.adaptors.schedulers.slurm.SlurmUtils.identifiersAsCSList;
import static nl.esciencecenter.xenon.adaptors.schedulers.slurm.SlurmUtils.verifyJobDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.adaptors.schedulers.RemoteCommandRunner;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingParser;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingScheduler;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingUtils;
import nl.esciencecenter.xenon.adaptors.schedulers.StreamsImplementation;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;
import nl.esciencecenter.xenon.schedulers.NoSuchJobException;
import nl.esciencecenter.xenon.schedulers.NoSuchQueueException;
import nl.esciencecenter.xenon.schedulers.QueueStatus;
import nl.esciencecenter.xenon.schedulers.Streams;

/**
 * Interface to the GridEngine command line tools. Will run commands to submit/list/cancel jobs and get the status of queues.
 *
 * @version 1.0
 * @since 1.0
 */
public class SlurmScheduler extends ScriptingScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlurmScheduler.class);

    private final String[] queueNames;

    private final String defaultQueueName;

    private final SlurmSetup setup;

    protected SlurmScheduler(String uniqueID, String location, Credential credential, XenonPropertyDescription[] valid, Map<String, String> prop)
            throws XenonException {

        super(uniqueID, ADAPTOR_NAME, location, credential, prop, valid, POLL_DELAY_PROPERTY);

        boolean disableAccounting = properties.getBooleanProperty(DISABLE_ACCOUNTING_USAGE);

        // Get some version information from slurm
        String output = runCheckedCommand(null, "scontrol", "show", "config");

        // Parse output. Ignore some header and footer lines.
        // See:
        //  - https://github.com/SchedMD/slurm/blob/slurm-20-02-0-1/src/api/config_info.c#L420
        //  - https://github.com/SchedMD/slurm/blob/slurm-20-02-0-1/src/api/config_info.c#L443
        Map<String, String> info = ScriptingParser.parseKeyValueLines(output, ScriptingParser.EQUALS_REGEX, ADAPTOR_NAME,
                "Configuration data as of",
                "Slurmctld(primary",                            // Slurmctld(primary/backup) or Slurmctld(primary)
                "Account Gather",
                "Cgroup Support Configuration",
                "External Sensors Configuration",
                "Node Features Configuration",
                "Slurmctld Plugstack Plugins Configuration",
                "-----"                                         // When printing plugin configuration (sub-heading)
        );

        setup = new SlurmSetup(info, disableAccounting);

        // Very wide partition format to compensate for bug in slurm 2.3.
        // If the size of the column is not specified the default partition does not get listed with a "*"
        output = runCheckedCommand(null, "sinfo", "--noheader", "--format=%120P");

        String[] foundQueueNames = ScriptingParser.parseList(output);

        String foundDefaultQueueName = null;
        for (int i = 0; i < foundQueueNames.length; i++) {
            String queueName = foundQueueNames[i];
            if (queueName.endsWith("*")) {
                // cut "*" of queue name
                foundQueueNames[i] = queueName.substring(0, queueName.length() - 1);
                foundDefaultQueueName = foundQueueNames[i];
            }
        }
        this.queueNames = foundQueueNames;
        this.defaultQueueName = foundDefaultQueueName;

        LOGGER.debug("Created new SlurmConfig. version = \"{}\", accounting available: {}", setup.version(), setup.accountingAvailable());
    }

    @Override
    public String[] getQueueNames() {
        return queueNames.clone();
    }

    @Override
    public String getDefaultQueueName() {
        return defaultQueueName;
    }

    @Override
    protected void translateError(RemoteCommandRunner runner, String stdin, String executable, String... arguments) throws XenonException {

        String error = runner.getStderr();

        if (error.contains("Invalid job id")) {
            throw new NoSuchJobException(ADAPTOR_NAME, "Invalid job ID");
        }

        throw new XenonException(ADAPTOR_NAME,
                "Could not run command \"" + executable + "\" with stdin \"" + stdin + "\" arguments \"" + Arrays.toString(arguments) + "\" at \""
                        + subScheduler + "\". Exit code = " + runner.getExitCode() + " Output: " + runner.getStdout() + " Error output: " + runner.getStderr());
    }

    @Override
    public String submitBatchJob(JobDescription description) throws XenonException {

        String output;
        Path fsEntryPath = getWorkingDirectory();

        verifyJobDescription(description, queueNames, false);

        checkWorkingDirectory(description.getWorkingDirectory());
        String jobScript = generate(description, fsEntryPath, getDefaultRuntime());

        output = runCheckedCommand(jobScript, "sbatch");

        return ScriptingParser.parseJobIDFromLine(output, ADAPTOR_NAME, "Submitted batch job", "Granted job allocation");
    }

    private String findInteractiveJobInMap(Map<String, Map<String, String>> queueInfo, String tag) {

        // find job with "tag" as a job name in the job info. NAME is produced by squeue, JobName by sacct
        for (Map.Entry<String, Map<String, String>> entry : queueInfo.entrySet()) {
            if (entry.getValue().containsKey("NAME") && entry.getValue().get("NAME").equals(tag)
                    || entry.getValue().containsKey("JobName") && entry.getValue().get("JobName").equals(tag)) {

                String jobID = entry.getKey();

                LOGGER.debug("Found interactive job ID: %s", jobID);

                // synchronized (this) {
                // //add to set of interactive jobs so we can find it
                // interactiveJobs.put(jobID, interactiveJobID);
                // }

                return jobID;
            }
        }

        return null;
    }

    private String findInteractiveJob(String tag) throws XenonException {

        // See if the job can be found in the queue.
        String result = findInteractiveJobInMap(getSqueueInfo(), tag);

        if (result != null) {
            return result;
        }

        // See if the job can be found in the accounting.
        return findInteractiveJobInMap(getSacctInfo(), tag);
    }

    @Override
    public Streams submitInteractiveJob(JobDescription description) throws XenonException {

        Path fsEntryPath = getWorkingDirectory();

        verifyJobDescription(description, queueNames, true);

        checkWorkingDirectory(description.getWorkingDirectory());

        UUID tag = UUID.randomUUID();

        String[] arguments = generateInteractiveArguments(description, fsEntryPath, tag, getDefaultRuntime());

        // There is a two step job submission here, since we submit a job to via a subscheduler (typically SSH).
        // So the job we get back here is the local SSH job that connects to the remote machine running slurm.
        Streams interactiveJob = startInteractiveCommand("salloc", arguments);

        // Note that it is hard to check if the salloc failed. If it fails it may return an error code of it's own, but it may also succeed and
        // return an error code because the application fails.....

        // Next we try to find information on the remote slurm job. Note that this job may not be visible in the queue yet, or
        // if may already have finished.
        String result = findInteractiveJob(tag.toString());

        // If we cannot find the job, the salloc may have failed. We should also check for that!
        long end = System.currentTimeMillis() + SLURM_UPDATE_TIMEOUT;

        while (result == null && System.currentTimeMillis() < end) {

            try {
                Thread.sleep(SLURM_UPDATE_SLEEP);
            } catch (InterruptedException e) {
                LOGGER.warn("Interrupted!", e);
                Thread.currentThread().interrupt();
            }

            result = findInteractiveJob(tag.toString());
        }

        if (result != null) {
            return new StreamsImplementation(result, interactiveJob.getStdout(), interactiveJob.getStdin(), interactiveJob.getStderr());
        }

        // Failed to find job within timeout. Fetch status of interactive job to return as an error.
        JobStatus status;

        try {
            status = subScheduler.getJobStatus(interactiveJob.getJobIdentifier());
        } catch (XenonException e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to submit interactive job");
        }

        if (status.isDone() && status.hasException()) {
            throw new XenonException(ADAPTOR_NAME, "Failed to submit interactive job", status.getException());
        }

        if (status.getExitCode() != null && status.getExitCode().equals(1)) {
            throw new XenonException(ADAPTOR_NAME,
                    "Failed to submit interactive job, perhaps some job options are invalid? (e.g. too many nodes, or invalid partition name)");
        }

        throw new XenonException(ADAPTOR_NAME, "Failed to submit interactive job. Interactive job status is " + status.getState() + " exit code = "
                + status.getExitCode() + " tag=" + tag.toString(), status.getException());
    }

    @Override
    public JobStatus cancelJob(String jobIdentifier) throws XenonException {

        assertNonNullOrEmpty(jobIdentifier, "Job identifier cannot be null or empty");

        try {
            String output = runCheckedCommand(null, "scancel", jobIdentifier);
            if (!output.isEmpty()) {
                throw new XenonException(ADAPTOR_NAME, "Got unexpected output on cancelling job: " + output);
            }
        } catch (XenonException e) {
            // ignore when job has already completed
            if (!e.getMessage().contains("Job/step already completing or completed")) {
                throw e;
            }
        }

        return getJobStatus(jobIdentifier);
    }

    @Override
    public String[] getJobs(String... queueNames) throws XenonException {
        String output;

        if (queueNames == null || queueNames.length == 0) {
            output = runCheckedCommand(null, "squeue", "--noheader", "--format=%i");
        } else {
            checkQueueNames(queueNames);

            // add a list of all requested queues
            output = runCheckedCommand(null, "squeue", "--noheader", "--format=%i", "--partitions=" + ScriptingUtils.asCSList(queueNames));
        }

        // Job id's are on separate lines, on their own.
        return ScriptingParser.parseList(output);
    }

    private Map<String, String> getSControlInfo(String jobIdentifier) throws XenonException {
        RemoteCommandRunner runner = runCommand(null, "scontrol", "show", "job", jobIdentifier);

        if (!runner.success()) {
            LOGGER.debug("failed to get job status {}", runner);
            return null;
        }

        // tell parser to ignore lines with the WorkDir and Command, as any spaces in the working dir value will confuse the parser
        return ScriptingParser.parseKeyValuePairs(runner.getStdout(), ADAPTOR_NAME, "WorkDir=", "Command=");
    }

    private Map<String, Map<String, String>> getSqueueInfo(String... jobs) throws XenonException {

        String squeueOutput = "";

        if (jobs == null || jobs.length == 0) {
            squeueOutput = runCheckedCommand(null, "squeue", "--format=%i %P %j %u %T %M %l %D %R %k");
        } else {
            squeueOutput = runCheckedCommand(null, "squeue", "--format=%i %P %j %u %T %M %l %D %R %k", "--jobs=" + identifiersAsCSList(jobs));
        }

        return ScriptingParser.parseTable(squeueOutput, "JOBID", ScriptingParser.WHITESPACE_REGEX, ADAPTOR_NAME, "*", "~");
    }

    private Map<String, Map<String, String>> getSinfoInfo(String... partitions) throws XenonException {
        String output = runCheckedCommand(null, "sinfo", "--format=%P %a %l %F %N %C %D", "--partition=" + ScriptingUtils.asCSList(partitions));

        return ScriptingParser.parseTable(output, "PARTITION", ScriptingParser.WHITESPACE_REGEX, ADAPTOR_NAME, "*", "~");
    }

    private Map<String, Map<String, String>> getSacctInfo(String... jobs) throws XenonException {
        if (!setup.accountingAvailable()) {
            return new HashMap<>();
        }

        RemoteCommandRunner runner;

        // This command will not complain if the job given does not exist
        // but it may produce output on stderr when it finds non-standard lines in the accounting log
        if (jobs == null || jobs.length == 0) {
            runner = runCommand(null, "sacct", "-X", "-p", "--format=JobID,JobName,Partition,NTasks,"
                    + "Elapsed,State,ExitCode,AllocCPUS,DerivedExitCode,Submit," + "Suspended,Start,User,End,NNodes,Timelimit,Comment,Priority");
        } else {
            runner = runCommand(null, "sacct", "-X", "-p", "--format=JobID,JobName,Partition,NTasks,"
                    + "Elapsed,State,ExitCode,AllocCPUS,DerivedExitCode,Submit," + "Suspended,Start,User,End,NNodes,Timelimit,Comment,Priority",
                    "--jobs=" + identifiersAsCSList(jobs));
        }

        if (runner.getExitCode() != 0) {
            throw new XenonException(ADAPTOR_NAME, "Error in getting sacct job status: " + runner);
        }

        if (!runner.getStderr().isEmpty()) {
            LOGGER.warn("Sacct produced error output: " + runner.getStderr());
        }

        return ScriptingParser.parseTable(runner.getStdout(), "JobID", ScriptingParser.BAR_REGEX, ADAPTOR_NAME, "*", "~");
    }

    @Override
    public JobStatus getJobStatus(String jobIdentifier) throws XenonException {

        assertNonNullOrEmpty(jobIdentifier, "Job identifier cannot be null or empty");

        // try the queue first
        Map<String, Map<String, String>> sQueueInfo = null;
        JobStatus result = null;

        try {
            sQueueInfo = getSqueueInfo(jobIdentifier);
        } catch (Exception e) {
            // The call to squeue may fail if we request a specific job that no longer exist.
            LOGGER.debug("Squeue produced error output", e);
        }

        result = getJobStatusFromSqueueInfo(sQueueInfo, jobIdentifier);

        // try the accounting (if available)
        if (result == null) {
            Map<String, Map<String, String>> sacctInfo = null;

            try {
                sacctInfo = getSacctInfo(jobIdentifier);
            } catch (Exception e) {
                // The call to squeue may fail if we request a specific job that no longer exist.
                LOGGER.debug("Sacct produced error output", e);
            }

            result = getJobStatusFromSacctInfo(sacctInfo, jobIdentifier);
        }

        // check scontrol.
        if (result == null) {
            Map<String, String> scontrolInfo = null;

            try {
                scontrolInfo = getSControlInfo(jobIdentifier);
            } catch (Exception e) {
                // The call to squeue may fail if we request a specific job that no longer exist.
                LOGGER.debug("Scontrol produced error output", e);
            }
            result = getJobStatusFromScontrolInfo(scontrolInfo, jobIdentifier);
        }

        // job not found anywhere, give up
        if (result == null) {
            throw new NoSuchJobException(ADAPTOR_NAME, "Unknown Job: " + jobIdentifier);
        }

        return result;
    }

    @Override
    public QueueStatus getQueueStatus(String queueName) throws XenonException {

        assertNonNullOrEmpty(queueName, "Queue name cannot be null or empty");

        Map<String, Map<String, String>> info = getSinfoInfo(queueName);

        QueueStatus result = getQueueStatusFromSInfo(info, queueName, this);

        if (result == null) {
            throw new NoSuchQueueException(ADAPTOR_NAME, "cannot get status of queue \"" + queueName + "\" from server");
        }

        return result;
    }

    @Override
    public QueueStatus[] getQueueStatuses(String... requestedQueueNames) throws XenonException {
        String[] targetQueueNames;

        if (requestedQueueNames == null) {
            throw new IllegalArgumentException("list of queue names cannot be null");
        } else if (requestedQueueNames.length == 0) {
            targetQueueNames = getQueueNames();
        } else {
            targetQueueNames = requestedQueueNames;
        }

        Map<String, Map<String, String>> info = getSinfoInfo(targetQueueNames);

        return getQueueStatuses(info, targetQueueNames);
    }
}
