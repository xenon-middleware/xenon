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
package nl.esciencecenter.octopus.adaptors.slurm;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.adaptors.scripting.RemoteCommandRunner;
import nl.esciencecenter.octopus.adaptors.scripting.SchedulerConnection;
import nl.esciencecenter.octopus.adaptors.scripting.ScriptingAdaptor;
import nl.esciencecenter.octopus.adaptors.scripting.ScriptingParser;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.QueueStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.octopus.engine.util.CommandLineUtils;
import nl.esciencecenter.octopus.exceptions.InvalidJobDescriptionException;
import nl.esciencecenter.octopus.exceptions.JobCanceledException;
import nl.esciencecenter.octopus.exceptions.NoSuchJobException;
import nl.esciencecenter.octopus.exceptions.NoSuchQueueException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface to the GridEngine command line tools. Will run commands to submit/list/cancel jobs and get the status of queues.
 * 
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class SlurmSchedulerConnection extends SchedulerConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlurmSchedulerConnection.class);

    public static final String JOB_OPTION_JOB_SCRIPT = "job.script";

    private static final String[] VALID_JOB_OPTIONS = new String[] { JOB_OPTION_JOB_SCRIPT };

    private static final String[] FAILED_STATES = new String[] { "FAILED", "CANCELLED", "NODE_FAIL", "TIMEOUT", "PREEMPTED" };

    private static final String DONE_STATE = "COMPLETED";

    //get an exit code from the scontrol "ExitCode" output field
    private static Integer exitcodeFromString(String value) throws OctopusException {
        if (value == null) {
            return null;
        }

        //the exit status may contain a ":" followed by the signal send to stop the job. Ignore
        String exitCodeString = value.split(":")[0];

        try {
            return Integer.parseInt(exitCodeString);
        } catch (NumberFormatException e) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "could not get job exit code from foudn value", e);
        }

    }

    private static JobStatus getJobStatusFromSacctInfo(Map<String, Map<String, String>> info, Job job) throws OctopusException {
        Map<String, String> jobInfo = info.get(job.getIdentifier());

        if (jobInfo == null) {
            LOGGER.debug("job {} not found in sacct output", job.getIdentifier());
            return null;
        }

        String jobID = jobInfo.get("JobID");

        if (jobID == null || !jobID.equals(job.getIdentifier())) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Invalid sacct output. Returned job id \"" + jobID
                    + "\" does not match " + job.getIdentifier());
        }

        String state = jobInfo.get("State");

        if (state == null) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Invalid sacct output. Output does not contain job state");
        }

        Integer exitcode = exitcodeFromString(jobInfo.get("ExitCode"));

        Exception exception;
        if (!isFailedState(state) || (state.equals("FAILED") && exitcode != 0)) {
            //Not a failed state (non zero exit code does not count either), no error.
            exception = null;
        } else if (state.startsWith("CANCELLED")) {
            exception = new JobCanceledException(SlurmAdaptor.ADAPTOR_NAME, "Job " + state.toLowerCase());
        } else {
            exception = new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Job failed for unknown reason");
        }

        JobStatus result = new JobStatusImplementation(job, state, exitcode, exception, state.equals("RUNNING"),
                isDoneState(state), jobInfo);

        LOGGER.debug("Got job status from sacct output {}", result);

        return result;
    }

    private static JobStatus getJobStatusFromScontrolInfo(Map<String, String> jobInfo, Job job) throws OctopusException {
        if (jobInfo == null || jobInfo.isEmpty()) {
            LOGGER.debug("job {} not found in scontrol output", job.getIdentifier());
            return null;
        }

        String jobID = jobInfo.get("JobId");

        if (jobID == null || !jobID.equals(job.getIdentifier())) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Invalid scontrol output. Returned jobid \""
                    + jobInfo.get("JobId") + "\" does not match " + job.getIdentifier());
        }

        String state = jobInfo.get("JobState");

        if (state == null) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Invalid scontrol output. Output does not contain job state");
        }

        Integer exitcode = exitcodeFromString(jobInfo.get("ExitCode"));

        String reason = jobInfo.get("Reason");

        if (reason == null) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Invalid scontrol output. Output does not contain reason");
        }

        Exception exception;
        if (!isFailedState(state) || reason.equals("NonZeroExitCode")) {
            //Not a failed state (non zero exit code does not count either), no error.
            exception = null;
        } else if (state.startsWith("CANCELLED")) {
            exception = new JobCanceledException(SlurmAdaptor.ADAPTOR_NAME, "Job " + state.toLowerCase());
        } else if (!reason.equals("None")) {
            exception = new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Slurm reported error reason: " + reason);
        } else {
            exception = new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Job failed for unknown reason");
        }

        JobStatus result = new JobStatusImplementation(job, state, exitcode, exception, state.equals("RUNNING"),
                isDoneState(state), jobInfo);

        LOGGER.debug("Got job status from scontrol output {}", result);

        return result;
    }

    private static JobStatus getJobStatusFromSqueueInfo(Map<String, Map<String, String>> info, Job job) {
        Map<String, String> jobInfo = info.get(job.getIdentifier());

        if (jobInfo == null) {
            LOGGER.debug("job {} not found in queue", job.getIdentifier());
            return null;
        }

        String state = jobInfo.get("STATE");

        return new JobStatusImplementation(job, state, null, null, state.equals("RUNNING"), false, jobInfo);
    }

    private static QueueStatus getQueueStatusFromSInfo(Map<String, Map<String, String>> info, String queueName,
            Scheduler scheduler) {
        Map<String, String> queueInfo = info.get(queueName);

        if (queueInfo == null) {
            LOGGER.debug("queue {} not found", queueName);
            return null;
        }

        return new QueueStatusImplementation(scheduler, queueName, null, queueInfo);
    }

    private static String identifiersAsCSList(Job[] jobs) {
        String result = null;
        for (Job job : jobs) {
            if (job != null) {
                if (result == null) {
                    result = job.getIdentifier();
                } else {
                    result += "," + job.getIdentifier();
                }
            }
        }
        return result;
    }

    //failed also implies done
    private static boolean isDoneState(String state) {
        return state.equals(DONE_STATE) || isFailedState(state);
    }

    private static boolean isFailedState(String state) {
        for (String validState : FAILED_STATES) {
            if (state.startsWith(validState)) {
                return true;
            }
        }
        return false;
    }

    private static void verifyJobDescription(JobDescription description) throws OctopusException {
        //check if all given job options make sense
        for (String option : description.getJobOptions().keySet()) {
            boolean found = false;
            for (String validOption : VALID_JOB_OPTIONS) {
                if (validOption.equals(option)) {
                    found = true;
                }
            }
            if (!found) {
                throw new InvalidJobDescriptionException(SlurmAdaptor.ADAPTOR_NAME, "Given Job option \"" + option
                        + "\" not supported");
            }
        }

        if (description.isInteractive()) {
            throw new InvalidJobDescriptionException(SlurmAdaptor.ADAPTOR_NAME, "Adaptor does not support interactive jobs");
        }

        //check for option that overrides job script completely.
        if (description.getJobOptions().get(JOB_OPTION_JOB_SCRIPT) != null) {
            //no other settings checked.
            return;
        }

        //Perform standard checks.
        SchedulerConnection.verifyJobDescription(description, SlurmAdaptor.ADAPTOR_NAME);
    }

    private final String[] queueNames;

    private final String defaultQueueName;

    private final Scheduler scheduler;

    private final SlurmConfig config;

    SlurmSchedulerConnection(ScriptingAdaptor adaptor, URI location, Credential credential, OctopusProperties properties,
            OctopusEngine engine) throws OctopusIOException, OctopusException {

        super(adaptor, location, credential, properties, engine, properties.getLongProperty(SlurmAdaptor.POLL_DELAY_PROPERTY));

        boolean ignoreVersion = getProperties().getBooleanProperty(SlurmAdaptor.IGNORE_VERSION_PROPERTY);

        this.config = getConfiguration(ignoreVersion);

        //Very wide partition format to compensate for bug in slurm 2.3.
        //If the size of the column is not specified the default partition does not get listed with a "*"
        String output = runCheckedCommand(null, "sinfo", "--noheader", "--format=%120P");

        String[] queueNames = ScriptingParser.parseList(output);

        String defaultQueueName = null;
        for (int i = 0; i < queueNames.length; i++) {
            String queueName = queueNames[i];
            if (queueName.endsWith("*")) {
                //cut "*" of queue name
                queueNames[i] = queueName.substring(0, queueName.length() - 1);
                defaultQueueName = queueNames[i];
            }
        }
        this.queueNames = queueNames;
        this.defaultQueueName = defaultQueueName;

        scheduler = new SchedulerImplementation(SlurmAdaptor.ADAPTOR_NAME, getID(), location, queueNames, credential,
                getProperties(), false, false, true);

        LOGGER.debug("new slurm scheduler connection {}", scheduler);
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public String[] getQueueNames() {
        return queueNames.clone();
    }

    @Override
    public String getDefaultQueueName() {
        return defaultQueueName;
    }

    private SlurmConfig getConfiguration(boolean ignoreVersion) throws OctopusIOException, OctopusException {
        String output = runCheckedCommand(null, "scontrol", "show", "config");

        //Parse output. Ignore some header and footer lines.
        Map<String, String> info = ScriptingParser.parseKeyValueLines(output, ScriptingParser.EQUALS_REGEX,
                SlurmAdaptor.ADAPTOR_NAME, "Configuration data as of", "Slurmctld(primary/backup) at");

        return new SlurmConfig(info, ignoreVersion);
    }

    @Override
    public Job submitJob(JobDescription description) throws OctopusIOException, OctopusException {
        String output;
        AbsolutePath fsEntryPath = getFsEntryPath();

        verifyJobDescription(description);

        //check for option that overrides job script completely.
        String customScriptFile = description.getJobOptions().get(JOB_OPTION_JOB_SCRIPT);

        if (customScriptFile == null) {
            checkWorkingDirectory(description.getWorkingDirectory());
            String jobScript = SlurmJobScriptGenerator.generate(description, fsEntryPath);

            output = runCheckedCommand(jobScript, "sbatch");
        } else {
            //the user gave us a job script. Pass it to sbatch as-is

            //convert to absolute path if needed
            if (!customScriptFile.startsWith("/")) {
                AbsolutePath scriptFile = fsEntryPath.resolve(new RelativePath(customScriptFile));
                customScriptFile = scriptFile.getPath();
            }

            output = runCheckedCommand(null, "sbatch", customScriptFile);
        }

        long jobID = ScriptingParser.parseJobIDFromLine(output, SlurmAdaptor.ADAPTOR_NAME, "Submitted batch job",
                "Granted job allocation");

        return new JobImplementation(getScheduler(), Long.toString(jobID), description, false, false);
    }

    @Override
    public JobStatus cancelJob(Job job) throws OctopusIOException, OctopusException {
        String identifier = job.getIdentifier();
        String output = runCheckedCommand(null, "scancel", identifier);

        if (!output.isEmpty()) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Got unexpected output on cancelling job: " + output);
        }

        return getJobStatus(job);
    }

    @Override
    public Job[] getJobs(String... queueNames) throws OctopusIOException, OctopusException {
        String output;

        if (queueNames == null || queueNames.length == 0) {
            output = runCheckedCommand(null, "squeue", "--noheader", "--format=%i");
        } else {
            checkQueueNames(queueNames);

            //add a list of all requested queues
            output = runCheckedCommand(null, "squeue", "--noheader", "--format=%i",
                    "--partitions=" + CommandLineUtils.asCSList(getQueueNames()));
        }

        //Job id's are on separate lines, on their own.
        String[] jobIdentifiers = ScriptingParser.parseList(output);

        Job[] result = new Job[jobIdentifiers.length];

        for (int i = 0; i < result.length; i++) {
            result[i] = new JobImplementation(scheduler, jobIdentifiers[i], false, false);
        }

        return result;
    }

    private Map<String, String> getSControlInfo(Job job) throws OctopusIOException, OctopusException {
        RemoteCommandRunner runner = runCommand(null, "scontrol", "show", "job", job.getIdentifier());

        if (!runner.success()) {
            LOGGER.debug("failed to get job status {}", runner);
            return null;
        }

        //tell parser to ignore lines with the WorkDir and Command, as any spaces in the working dir value will confuse the parser
        return ScriptingParser.parseKeyValuePairs(runner.getStdout(), SlurmAdaptor.ADAPTOR_NAME, "WorkDir=", "Command=");
    }

    private Map<String, Map<String, String>> getSqueueInfo(Job... jobs) throws OctopusException, OctopusIOException {
        String squeueOutput = runCheckedCommand(null, "squeue", "--format=%i %P %j %u %T %M %l %D %R", "--jobs="
                + identifiersAsCSList(jobs));

        return ScriptingParser.parseTable(squeueOutput, "JOBID", ScriptingParser.WHITESPACE_REGEX, SlurmAdaptor.ADAPTOR_NAME,
                "*", "~");
    }

    private Map<String, Map<String, String>> getSinfoInfo(String... partitions) throws OctopusIOException, OctopusException {
        String output = runCheckedCommand(null, "sinfo", "--format=%P %a %l %F %N %C %D",
                "--partition=" + CommandLineUtils.asCSList(partitions));

        return ScriptingParser.parseTable(output, "PARTITION", ScriptingParser.WHITESPACE_REGEX, SlurmAdaptor.ADAPTOR_NAME, "*",
                "~");
    }

    private Map<String, Map<String, String>> getSacctInfo(Job... jobs) throws OctopusException, OctopusIOException {
        if (!config.accountingAvailable()) {
            return new HashMap<String, Map<String, String>>();
        }

        //this command will not complain if the job given does not exist
        //but it may produce output on stderr when it finds non-standard lines in the accounting log
        RemoteCommandRunner runner = runCommand(null, "sacct", "-X", "-p", "--format=JobID,JobName,Partition,NTasks,"
                + "Elapsed,State,ExitCode,AllocCPUS,DerivedExitCode,Submit,"
                + "Suspended,Comment,Start,User,End,NNodes,Timelimit,Priority", "--jobs=" + identifiersAsCSList(jobs));

        if (runner.getExitCode() != 0) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Error in getting sacct job status: " + runner);
        }

        if (!runner.getStderr().isEmpty()) {
            LOGGER.warn("Sacct produced error output: " + runner.getStderr());
        }

        return ScriptingParser.parseTable(runner.getStdout(), "JobID", ScriptingParser.BAR_REGEX, SlurmAdaptor.ADAPTOR_NAME, "*",
                "~");
    }

    @Override
    public JobStatus getJobStatus(Job job) throws OctopusException, OctopusIOException {

        //try the queue first
        Map<String, Map<String, String>> sQueueInfo = getSqueueInfo(job);
        JobStatus result = getJobStatusFromSqueueInfo(sQueueInfo, job);

        //try the accounting (if available)
        if (result == null) {
            Map<String, Map<String, String>> sacctInfo = getSacctInfo(job);
            result = getJobStatusFromSacctInfo(sacctInfo, job);
        }
        
        //check scontrol.
        if (result == null) {
            Map<String, String> scontrolInfo = getSControlInfo(job);
            result = getJobStatusFromScontrolInfo(scontrolInfo, job);
        }

        //job not found anywhere, give up
        if (result == null) {
            throw new NoSuchJobException(SlurmAdaptor.ADAPTOR_NAME, "Unknown Job: " + job);
        }

        return result;
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) throws OctopusIOException, OctopusException {
        JobStatus[] result = new JobStatus[jobs.length];

        //fetch queue info for all jobs in one go
        Map<String, Map<String, String>> squeueInfo = getSqueueInfo(jobs);

        //fetch accounting info for all jobs in one go
        Map<String, Map<String, String>> sacctInfo = getSacctInfo(jobs);

        //loop over all jobs looking for status in info maps
        for (int i = 0; i < jobs.length; i++) {
            //job not requested at all
            if (jobs[i] == null) {
                result[i] = null;
            } else {
                //Check the squeue info.
                result[i] = getJobStatusFromSqueueInfo(squeueInfo, jobs[i]);

                //Check sacct info. (if available)
                if (result[i] == null) {
                    result[i] = getJobStatusFromSacctInfo(sacctInfo, jobs[i]);
                }
                
                //Check scontrol. Will run an additional command.
                if (result[i] == null) {
                    Map<String, String> scontrolInfo = getSControlInfo(jobs[i]);
                    result[i] = getJobStatusFromScontrolInfo(scontrolInfo, jobs[i]);
                }

                //job really does not seem to exist (anymore)
                if (result[i] == null) {
                    NoSuchJobException exception = new NoSuchJobException(SlurmAdaptor.ADAPTOR_NAME, "Unknown Job: "
                            + jobs[i].getIdentifier());
                    result[i] = new JobStatusImplementation(jobs[i], null, null, exception, false, false, null);
                }
            }
        }
        return result;
    }

    @Override
    public QueueStatus getQueueStatus(String queueName) throws OctopusIOException, OctopusException {
        Map<String, Map<String, String>> info = getSinfoInfo(queueName);

        QueueStatus result = getQueueStatusFromSInfo(info, queueName, getScheduler());

        if (result == null) {
            throw new NoSuchQueueException(SlurmAdaptor.ADAPTOR_NAME, "cannot get status of queue \"" + queueName
                    + "\" from server");
        }

        return result;
    }

    @Override
    public QueueStatus[] getQueueStatuses(String... requestedQueueNames) throws OctopusIOException, OctopusException {
        String[] queueNames;

        if (requestedQueueNames == null) {
            throw new IllegalArgumentException("list of queue names cannot be null");
        } else if (requestedQueueNames.length == 0) {
            queueNames = getQueueNames();
        } else {
            queueNames = requestedQueueNames;
        }

        Map<String, Map<String, String>> info = getSinfoInfo(queueNames);

        QueueStatus[] result = new QueueStatus[queueNames.length];

        for (int i = 0; i < queueNames.length; i++) {
            if (queueNames[i] == null) {
                result[i] = null;
            } else {
                result[i] = getQueueStatusFromSInfo(info, queueNames[i], getScheduler());

                if (result[i] == null) {
                    Exception exception = new NoSuchQueueException(SlurmAdaptor.ADAPTOR_NAME, "Cannot get status of queue \""
                            + queueNames[i] + "\" from server");
                    result[i] = new QueueStatusImplementation(getScheduler(), queueNames[i], exception, null);
                }
            }
        }
        return result;

    }

}
