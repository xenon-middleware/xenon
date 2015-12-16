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
package nl.esciencecenter.xenon.adaptors.slurm;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.scripting.RemoteCommandRunner;
import nl.esciencecenter.xenon.adaptors.scripting.SchedulerConnection;
import nl.esciencecenter.xenon.adaptors.scripting.ScriptingAdaptor;
import nl.esciencecenter.xenon.adaptors.scripting.ScriptingParser;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.jobs.JobImplementation;
import nl.esciencecenter.xenon.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.xenon.engine.jobs.QueueStatusImplementation;
import nl.esciencecenter.xenon.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.xenon.engine.util.CommandLineUtils;
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
 * @version 1.0
 * @since 1.0
 */
public class SlurmSchedulerConnection extends SchedulerConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlurmSchedulerConnection.class);
    
    private static final long SLURM_UPDATE_TIMEOUT = 60*1000; // 30 second update timeout
    
    private static final long SLURM_UPDATE_SLEEP = 1000; // 1 second update sleep
    
    public static final String JOB_OPTION_JOB_SCRIPT = "job.script";

    private static final String[] VALID_JOB_OPTIONS = new String[] { JOB_OPTION_JOB_SCRIPT };

    /**
     * These are the states a job can be in when it has failed:
     * FAILED:    the job terminated with non-zero exit code or other failure condition. 
     * CANCELLED: the job was explicitly cancelled by the user or system administrator.
     * NODE_FAIL: the job terminated due to failure of one or more allocated nodes. 
     * TIMEOUT:   the job terminated upon reaching its time limit.
     * PREEMPTED: the job terminated due to preemption (a more important job took its place). 
     * BOOT_FAIL: the job terminated due to a launch failure (typically a hardware failure).
     */
    private static final String[] FAILED_STATES = new String[] { "FAILED", "CANCELLED", "NODE_FAIL", "TIMEOUT", "PREEMPTED", "BOOT_FAIL" };

    /**
     * These are the states a job can be in when it is running:
     * 
     * CONFIGURING: the resources are available and being preparing to run the job (for example by booting).
     * RUNNING:     the resources are running the job.
     * COMPLETING:  the job is in process of completing. Some processes may have completed, others may still be running.
     */
    private static final String[] RUNNING_STATES = new String[] { "CONFIGURING", "RUNNING", "COMPLETING" };

    /**
     * These are the states a job can be in when it is pending:
     * 
     * PENDING:      the job is awaiting resource allocation. 
     * STOPPED:      the job has an allocation, but execution has been stopped with SIGSTOP signal (allocation is retained).
     * SUSPENDED:    the job has an allocation, but execution has been suspended (resources have been released for other jobs). 
     * SPECIAL_EXIT: The job was requeued in a special state. 
     */
    private static final String[] PENDING_STATES = new String[] { "PENDING", "STOPPED", "SUSPENDED", "SPECIAL_EXIT" };
    
    /** In completed state, the job has terminated and all processes have returned exit code 0. */ 
    private static final String DONE_STATE = "COMPLETED";
    
    // Retrieve an exit code from the "ExitCode" output field of scontrol
    protected static Integer exitcodeFromString(String value) throws XenonException {
        if (value == null) {
            return null;
        }

        //the exit status may contain a ":" followed by the signal send to stop the job. Ignore
        String exitCodeString = value.split(":")[0];

        try {
            return Integer.parseInt(exitCodeString);
        } catch (NumberFormatException e) {
            throw new XenonException(SlurmAdaptor.ADAPTOR_NAME, "job exit code \"" + exitCodeString + "\" is not a number", e);
        }

    }

    protected static JobStatus getJobStatusFromSacctInfo(Map<String, Map<String, String>> info, Job job) throws XenonException {
        Map<String, String> jobInfo = info.get(job.getIdentifier());

        if (jobInfo == null) {
            LOGGER.debug("job {} not found in sacct output", job.getIdentifier());
            return null;
        }

        //also checks if the job id is correct
        SchedulerConnection.verifyJobInfo(jobInfo, job, SlurmAdaptor.ADAPTOR_NAME, "JobID", "State", "ExitCode");

        String state = jobInfo.get("State");

        Integer exitcode = exitcodeFromString(jobInfo.get("ExitCode"));

        Exception exception;
        if (!isFailedState(state) || (state.equals("FAILED") && (exitcode != null && exitcode != 0))) {
            //Not a failed state (non zero exit code does not count either), no error.
            exception = null;
        } else if (state.startsWith("CANCELLED")) {
            exception = new JobCanceledException(SlurmAdaptor.ADAPTOR_NAME, "Job " + state.toLowerCase(Locale.getDefault()));
        } else {
            exception = new XenonException(SlurmAdaptor.ADAPTOR_NAME, "Job failed for unknown reason");
        }

        JobStatus result = new JobStatusImplementation(job, state, exitcode, exception, isRunningState(state),
                isDoneOrFailedState(state), jobInfo);

        LOGGER.debug("Got job status from sacct output {}", result);

        return result;
    }

    protected static JobStatus getJobStatusFromScontrolInfo(Map<String, String> jobInfo, Job job) throws XenonException {
        if (jobInfo == null) {
            LOGGER.debug("job {} not found in scontrol output", job.getIdentifier());
            return null;
        }

        //also checks if the job id is correct
        SchedulerConnection.verifyJobInfo(jobInfo, job, SlurmAdaptor.ADAPTOR_NAME, "JobId", "JobState", "ExitCode", "Reason");

        String state = jobInfo.get("JobState");
        Integer exitcode = exitcodeFromString(jobInfo.get("ExitCode"));
        String reason = jobInfo.get("Reason");

        Exception exception;
        if (!isFailedState(state) || state.equals("FAILED") && reason.equals("NonZeroExitCode")) {
            //Not a failed state (non zero exit code does not count either), no error.
            exception = null;
        } else if (state.startsWith("CANCELLED")) {
            exception = new JobCanceledException(SlurmAdaptor.ADAPTOR_NAME, "Job " + state.toLowerCase(Locale.getDefault()));
        } else if (!reason.equals("None")) {
            exception = new XenonException(SlurmAdaptor.ADAPTOR_NAME, "Job failed with state \"" + state + "\" and reason: "
                    + reason);
        } else {
            exception = new XenonException(SlurmAdaptor.ADAPTOR_NAME, "Job failed with state \"" + state
                    + "\" for unknown reason");
        }

        JobStatus result = new JobStatusImplementation(job, state, exitcode, exception, isRunningState(state),
                isDoneOrFailedState(state), jobInfo);

        LOGGER.debug("Got job status from scontrol output {}", result);

        return result;
    }

    protected static JobStatus getJobStatusFromSqueueInfo(Map<String, Map<String, String>> info, Job job) throws XenonException {

        Map<String, String> jobInfo = info.get(job.getIdentifier());

        if (jobInfo == null) {
            LOGGER.debug("job {} not found in queue", job.getIdentifier());
            return null;
        }

        //also checks if the job id is correct
        SchedulerConnection.verifyJobInfo(jobInfo, job, SlurmAdaptor.ADAPTOR_NAME, "JOBID", "STATE");

        String state = jobInfo.get("STATE");

        return new JobStatusImplementation(job, state, null, null, isRunningState(state), false, jobInfo);
    }

    protected static QueueStatus getQueueStatusFromSInfo(Map<String, Map<String, String>> info, String queueName, Scheduler scheduler) {
        Map<String, String> queueInfo = info.get(queueName);

        if (queueInfo == null) {
            LOGGER.debug("queue {} not found", queueName);
            return null;
        }

        return new QueueStatusImplementation(scheduler, queueName, null, queueInfo);
    }
    
    /**
     * Is the given state a running state ? 
     * 
     * @param state the state to check
     * @return if the state is a running state.
     */
    protected static boolean isRunningState(String state) {
        for (String validState : RUNNING_STATES) {
            if (state.startsWith(validState)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is the given state a pending state ? 
     * 
     * @param state the state to check
     * @return if the state is a pending state.
     */
    protected static boolean isPendingState(String state) {
        for (String validState : PENDING_STATES) {
            if (state.startsWith(validState)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Is the given state a done or failed state ? 
     * 
     * @param state the state to check
     * @return if the state is a done or failed state.
     */
    protected static boolean isDoneOrFailedState(String state) {
        return isDoneState(state) || isFailedState(state);
    }

    /**
     * Is the given state a done state ? 
     * 
     * @param state the state to check
     * @return if the state is a done state.
     */
    protected static boolean isDoneState(String state) {
        return state.equals(DONE_STATE);
    }
    
    /**
     * Is the given state a failed state ? 
     * 
     * @param state the state to check
     * @return if the state is failed state.
     */    
    protected static boolean isFailedState(String state) {
        for (String validState : FAILED_STATES) {
            if (state.startsWith(validState)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("PMD.NPathComplexity")
    protected  static void verifyJobDescription(JobDescription description) throws XenonException {
        SchedulerConnection.verifyJobOptions(description.getJobOptions(), VALID_JOB_OPTIONS, SlurmAdaptor.ADAPTOR_NAME);

        if (description.isInteractive()) {
            if (description.getJobOptions().get(JOB_OPTION_JOB_SCRIPT) != null) {
                throw new InvalidJobDescriptionException(SlurmAdaptor.ADAPTOR_NAME,
                        "Custom job script not supported in interactive mode");
            }
            
            if (description.isStartSingleProcess()) {
                throw new InvalidJobDescriptionException(SlurmAdaptor.ADAPTOR_NAME,
                        "StartSingleProcess option not supported in interactive mode");
            }
            
            if (description.getStdin() != null) {
                throw new InvalidJobDescriptionException(SlurmAdaptor.ADAPTOR_NAME, "Stdin redirect not supported in interactive mode");
            }

            if (description.getStdout() != null && !description.getStdout().equals("stdout.txt")) {
                throw new InvalidJobDescriptionException(SlurmAdaptor.ADAPTOR_NAME, "Stdout redirect not supported in interactive mode");
            }

            if (description.getStderr() != null && !description.getStderr().equals("stderr.txt")) {
                throw new InvalidJobDescriptionException(SlurmAdaptor.ADAPTOR_NAME, "Stderr redirect not supported in interactive mode");
            }
            
            if (description.getEnvironment().size() != 0) {
                throw new InvalidJobDescriptionException(SlurmAdaptor.ADAPTOR_NAME, "Environment variables not supported in interactive mode");
            }
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

    private final SlurmSetup config;

    private final Map<String, Job> interactiveJobs;

    protected SlurmSchedulerConnection(ScriptingAdaptor adaptor, String location, Credential credential, XenonProperties properties,
            XenonEngine engine) throws XenonException {

        super(adaptor, "slurm", location, credential, properties, engine, properties
                .getLongProperty(SlurmAdaptor.POLL_DELAY_PROPERTY));

        //map containing references to interactive jobs (normally ssh jobs)
        interactiveJobs = new HashMap<>();

        boolean ignoreVersion = getProperties().getBooleanProperty(SlurmAdaptor.IGNORE_VERSION_PROPERTY);
        boolean disableAccounting = getProperties().getBooleanProperty(SlurmAdaptor.DISABLE_ACCOUNTING_USAGE);

        this.config = getConfiguration(ignoreVersion, disableAccounting);

        //Very wide partition format to compensate for bug in slurm 2.3.
        //If the size of the column is not specified the default partition does not get listed with a "*"
        String output = runCheckedCommand(null, "sinfo", "--noheader", "--format=%120P");

        String[] foundQueueNames = ScriptingParser.parseList(output);

        String foundDefaultQueueName = null;
        for (int i = 0; i < foundQueueNames.length; i++) {
            String queueName = foundQueueNames[i];
            if (queueName.endsWith("*")) {
                //cut "*" of queue name
                foundQueueNames[i] = queueName.substring(0, queueName.length() - 1);
                foundDefaultQueueName = foundQueueNames[i];
            }
        }
        this.queueNames = foundQueueNames;
        this.defaultQueueName = foundDefaultQueueName;

        scheduler = new SchedulerImplementation(SlurmAdaptor.ADAPTOR_NAME, getID(), "slurm", location, foundQueueNames,
                credential, getProperties(), false, true, true);

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

    private SlurmSetup getConfiguration(boolean ignoreVersion, boolean disableAccounting) throws XenonException {

        String output = runCheckedCommand(null, "scontrol", "show", "config");

        //Parse output. Ignore some header and footer lines.
        Map<String, String> info = ScriptingParser.parseKeyValueLines(output, ScriptingParser.EQUALS_REGEX,
                SlurmAdaptor.ADAPTOR_NAME, "Configuration data as of", "Slurmctld(primary/backup) at", "Account Gather");

        return new SlurmSetup(info, ignoreVersion, disableAccounting);
    }

    @Override
    public Job submitJob(JobDescription description) throws XenonException {

        verifyJobDescription(description);

        if (description.isInteractive()) {
            return submitInteractiveJob(description);
        } else {
            return submitBatchJob(description);
        }
    }

    private Job submitBatchJob(JobDescription description) throws XenonException {
        String output;
        RelativePath fsEntryPath = getFsEntryPath().getRelativePath();

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
                RelativePath scriptFile = fsEntryPath.resolve(customScriptFile);
                customScriptFile = scriptFile.getAbsolutePath();
            }

            output = runCheckedCommand(null, "sbatch", customScriptFile);
        }

        String jobID = ScriptingParser.parseJobIDFromLine(output, SlurmAdaptor.ADAPTOR_NAME, "Submitted batch job",
                "Granted job allocation");

        return new JobImplementation(getScheduler(), jobID, description, false, false);
    }

    private Job findInteractiveJobInMap(Map<String, Map<String, String>> queueInfo, String tag, JobDescription description, 
            Job interactiveJob) throws XenonException {

        //find job with "tag" as a comment in the job info
        for (Map.Entry<String, Map<String, String>> entry : queueInfo.entrySet()) {
            if (entry.getValue().containsKey("COMMENT") && entry.getValue().get("COMMENT").equals(tag) ||
                entry.getValue().containsKey("Comment") && entry.getValue().get("Comment").equals(tag)) {
                
                String jobID = entry.getKey();
                
                LOGGER.debug("Found interactive job ID: " + jobID);

                Job result = new JobImplementation(getScheduler(), jobID, description, true, true);
                
                synchronized (this) {
                    //add to set of interactive jobs so we can find it
                    interactiveJobs.put(result.getIdentifier(), interactiveJob);
                }
                
                return result;
            }
        }

        return null;        
    }

    
    private Job findInteractiveJob(String tag, JobDescription description, Job interactiveJob) throws XenonException {

        // See if the job can be found in the queue.
        Job result = findInteractiveJobInMap(getSqueueInfo(), tag, description, interactiveJob);

        if (result != null) { 
            return result;
        }
        
        // See if the job can be found in the accounting.                
        return findInteractiveJobInMap(getSacctInfo(), tag, description, interactiveJob);
    }
    
    private Job submitInteractiveJob(JobDescription description) throws XenonException {
        RelativePath fsEntryPath = getFsEntryPath().getRelativePath();
        
        checkWorkingDirectory(description.getWorkingDirectory());

        UUID tag = UUID.randomUUID();

        String[] arguments = SlurmJobScriptGenerator.generateInteractiveArguments(description, fsEntryPath, tag);

        // There is a two step job submission here, since we submit a job to via a subscheduler (typically SSH). 
        // So the job we get back here is the local SSH job that connects to the remote machine running slurm. 
        Job interactiveJob = startInteractiveCommand("salloc", arguments);
        
        // Next we try to find information on the remote slurm job. Note that this job may not be visible in the queue yet, or
        // if may already have finished. 
        Job result = findInteractiveJob(tag.toString(), description, interactiveJob);

        long end = System.currentTimeMillis() + SLURM_UPDATE_TIMEOUT; 
        
        while (result == null && System.currentTimeMillis() < end) {
            
            try { 
                Thread.sleep(SLURM_UPDATE_SLEEP);
            } catch (InterruptedException e) { 
                // ignored
            }
            
            result = findInteractiveJob(tag.toString(), description, interactiveJob);
        }
        
        if (result != null) { 
            return result;
        }
        
        // Failed to find job within timeout. Fetch status of interactive job to return as an error.
        JobStatus status;
        
        try {
            status = engine.jobs().getJobStatus(interactiveJob);
        } catch (XenonException e) {
            throw new XenonException(SlurmAdaptor.ADAPTOR_NAME, "Failed to submit interactive job");
        }

        if (status.isDone() && status.hasException()) { 
            throw new XenonException(SlurmAdaptor.ADAPTOR_NAME, "Failed to submit interactive job", status.getException());               
        }
        
        if (status.getExitCode() != null && status.getExitCode().equals(1)) {
            throw new XenonException(SlurmAdaptor.ADAPTOR_NAME, "Failed to submit interactive job, perhaps some job options are invalid? (e.g. too many nodes, or invalid partition name)");
        }
            
        throw new XenonException(SlurmAdaptor.ADAPTOR_NAME, "Failed to submit interactive job. Interactive job status is "
                    + status.getState() + " exit code = " + status.getExitCode() + " tag=" + tag.toString(), status.getException());
    }

    @Override
    public JobStatus cancelJob(Job job) throws XenonException {
        String identifier = job.getIdentifier();
        String output = runCheckedCommand(null, "scancel", identifier);

        if (!output.isEmpty()) {
            throw new XenonException(SlurmAdaptor.ADAPTOR_NAME, "Got unexpected output on cancelling job: " + output);
        }

        return getJobStatus(job);
    }

    @Override
    public Job[] getJobs(String... queueNames) throws XenonException {
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

    private Map<String, String> getSControlInfo(Job job) throws XenonException {
        RemoteCommandRunner runner = runCommand(null, "scontrol", "show", "job", job.getIdentifier());

        if (!runner.success()) {
            LOGGER.debug("failed to get job status {}", runner);
            return null;
        }

        //tell parser to ignore lines with the WorkDir and Command, as any spaces in the working dir value will confuse the parser
        return ScriptingParser.parseKeyValuePairs(runner.getStdout(), SlurmAdaptor.ADAPTOR_NAME, "WorkDir=", "Command=");
    }

    private Map<String, Map<String, String>> getSqueueInfo(Job... jobs) throws XenonException {
        
        String squeueOutput;
        
        if (jobs == null || jobs.length == 0)  {
            squeueOutput = runCheckedCommand(null, "squeue", "--format=%i %P %j %u %T %M %l %D %R %k");
        } else { 
            squeueOutput = runCheckedCommand(null, "squeue", "--format=%i %P %j %u %T %M %l %D %R %k", "--jobs="
                + SchedulerConnection.identifiersAsCSList(jobs));
        }

        return ScriptingParser.parseTable(squeueOutput, "JOBID", ScriptingParser.WHITESPACE_REGEX, SlurmAdaptor.ADAPTOR_NAME,
                "*", "~");
    }

    private Map<String, Map<String, String>> getSinfoInfo(String... partitions) throws XenonException {
        String output = runCheckedCommand(null, "sinfo", "--format=%P %a %l %F %N %C %D",
                "--partition=" + CommandLineUtils.asCSList(partitions));

        return ScriptingParser.parseTable(output, "PARTITION", ScriptingParser.WHITESPACE_REGEX, SlurmAdaptor.ADAPTOR_NAME, "*",
                "~");
    }

    private Map<String, Map<String, String>> getSacctInfo(Job... jobs) throws XenonException {
        if (!config.accountingAvailable()) {
            return new HashMap<>();
        }

        RemoteCommandRunner runner;
        
        // This command will not complain if the job given does not exist
        // but it may produce output on stderr when it finds non-standard lines in the accounting log        
        if (jobs == null || jobs.length == 0) { 
            runner = runCommand(null, "sacct", "-X", "-p", "--format=JobID,JobName,Partition,NTasks,"
                    + "Elapsed,State,ExitCode,AllocCPUS,DerivedExitCode,Submit,"
                    + "Suspended,Start,User,End,NNodes,Timelimit,Comment,Priority");
        } else { 
            runner = runCommand(null, "sacct", "-X", "-p", "--format=JobID,JobName,Partition,NTasks,"
                    + "Elapsed,State,ExitCode,AllocCPUS,DerivedExitCode,Submit,"
                    + "Suspended,Start,User,End,NNodes,Timelimit,Comment,Priority",
                    "--jobs=" + SchedulerConnection.identifiersAsCSList(jobs));
        }

        if (runner.getExitCode() != 0) {
            throw new XenonException(SlurmAdaptor.ADAPTOR_NAME, "Error in getting sacct job status: " + runner);
        }

        if (!runner.getStderr().isEmpty()) {
            LOGGER.warn("Sacct produced error output: " + runner.getStderr());
        }
 
        return ScriptingParser.parseTable(runner.getStdout(), "JobID", ScriptingParser.BAR_REGEX, SlurmAdaptor.ADAPTOR_NAME, "*",
                "~");
    }

    @Override
    public JobStatus getJobStatus(Job job) throws XenonException {

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
    public JobStatus[] getJobStatuses(Job... jobs) throws XenonException {
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
    public QueueStatus getQueueStatus(String queueName) throws XenonException {
        Map<String, Map<String, String>> info = getSinfoInfo(queueName);

        QueueStatus result = getQueueStatusFromSInfo(info, queueName, getScheduler());

        if (result == null) {
            throw new NoSuchQueueException(SlurmAdaptor.ADAPTOR_NAME, "cannot get status of queue \"" + queueName
                    + "\" from server");
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

        QueueStatus[] result = new QueueStatus[targetQueueNames.length];

        for (int i = 0; i < targetQueueNames.length; i++) {
            if (targetQueueNames[i] == null) {
                result[i] = null;
            } else {
                result[i] = getQueueStatusFromSInfo(info, targetQueueNames[i], getScheduler());

                if (result[i] == null) {
                    Exception exception = new NoSuchQueueException(SlurmAdaptor.ADAPTOR_NAME, "Cannot get status of queue \""
                            + targetQueueNames[i] + "\" from server");
                    result[i] = new QueueStatusImplementation(getScheduler(), targetQueueNames[i], exception, null);
                }
            }
        }
        return result;

    }

    @Override
    public Streams getStreams(Job job) throws XenonException {
        Job interactiveJob;
        
        synchronized (this) {
            interactiveJob = interactiveJobs.get(job.getIdentifier());
        }
        
        if (interactiveJob == null) {
            throw new NoSuchJobException(SlurmAdaptor.ADAPTOR_NAME, "Unknown Job, or not an interactive job: " + job);
        }
        
        return engine.jobs().getStreams(interactiveJob);
    }

}
