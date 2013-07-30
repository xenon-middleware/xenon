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
import java.util.Map;

import nl.esciencecenter.octopus.adaptors.scripting.RemoteCommandRunner;
import nl.esciencecenter.octopus.adaptors.scripting.SchedulerConnection;
import nl.esciencecenter.octopus.adaptors.scripting.ScriptingAdaptor;
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
    
    private static final String[] FAILED_STATES = new String[] {"FAILED", "CANCELLED", "NODE_FAIL","TIMEOUT", "PREEMPTED" };

    private static final String DONE_STATE = "COMPLETED";
    
    private final String[] queueNames;
    private final String defaultQueueName;

    private final Scheduler scheduler;

    private final SlurmConfig config;

    SlurmSchedulerConnection(ScriptingAdaptor adaptor, URI location, Credential credential, OctopusProperties properties, 
            OctopusEngine engine) throws OctopusIOException, OctopusException {
        
        super(adaptor, location, credential, properties, engine, properties.getLongProperty(SlurmAdaptor.POLL_DELAY_PROPERTY));

        boolean ignoreVersion = getProperties().getBooleanProperty(SlurmAdaptor.IGNORE_VERSION_PROPERTY);

        this.config = fetchConfiguration(ignoreVersion);

        this.queueNames = fetchQueueNames();
        this.defaultQueueName = findDefaultQueue();

        scheduler = new SchedulerImplementation(SlurmAdaptor.ADAPTOR_NAME, getID(), location, queueNames, credential, 
                getProperties(), false, false, true);

        LOGGER.debug("new slurm scheduler connection {}", scheduler);
    }

    private SlurmConfig fetchConfiguration(boolean ignoreVersion) throws OctopusIOException, OctopusException {
        String output = runCheckedCommand(null, "scontrol", "show", "config");

        Map<String, String> info = SlurmOutputParser.parseScontrolConfigOutput(output);

        return new SlurmConfig(info, ignoreVersion);
    }

    private String[] fetchQueueNames() throws OctopusIOException, OctopusException {
        //Very wide partition format to compensate for bug in slurm 2.3.
        //If the size of the column is not specified the default partition does not get listed with a "*"
        String output = runCheckedCommand(null, "sinfo", "--noheader", "--format=%120P");

        String[] queues = output.split(SlurmOutputParser.WHITESPACE_REGEX);

        return queues;
    }

    private String findDefaultQueue() {
        for (int i = 0; i < queueNames.length; i++) {
            String queueName = queueNames[i];
            if (queueName.endsWith("*")) {
                //cut "*" of queue name
                queueNames[i] = queueName.substring(0, queueName.length() - 1);
                return queueNames[i];
            }
        }
        //no default queue found
        return null;
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

    @Override
    protected void verifyJobDescription(JobDescription description) throws OctopusException {
        //check if all given job options make sense
        //FIXME: this should be build on top of OctopusProperties, see #132
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
            //no remaining settings checked.
            return;
        }

        //perform standard checks.
        super.verifyJobDescription(description);
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

        String identifier = SlurmOutputParser.parseSbatchOutput(output);

        return new JobImplementation(getScheduler(), identifier, description, false, false);
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
            output =
                    runCheckedCommand(null, "squeue", "--noheader", "--format=%i",
                            "--partitions=" + CommandLineUtils.asCSList(getQueueNames()));
        }

        //Job id's are on separate lines, on their own.
        String[] jobIdentifiers = output.split(SlurmOutputParser.WHITESPACE_REGEX);

        Job[] result = new Job[jobIdentifiers.length];

        for (int i = 0; i < result.length; i++) {
            result[i] = new JobImplementation(scheduler, jobIdentifiers[i], false, false);
        }

        return result;
    }

    @Override
    public QueueStatus getQueueStatus(String queueName) throws OctopusIOException, OctopusException {
        String output = runCheckedCommand(null, "sinfo", "--format=%P %a %l %F %N %C %D", "--partition=" + queueName);

        Map<String, Map<String, String>> infoMaps =
                SlurmOutputParser.parseInfoOutput(output, "PARTITION", SlurmOutputParser.WHITESPACE_REGEX);

        if (!infoMaps.containsKey(queueName)) {
            throw new NoSuchQueueException(SlurmAdaptor.ADAPTOR_NAME, "cannot get status of requested queue: \"" + queueName
                    + "\"");
        }

        return new QueueStatusImplementation(getScheduler(), queueName, null, infoMaps.get(queueName));
    }

    @Override
    public QueueStatus[] getQueueStatuses(String... queueNames) throws OctopusIOException, OctopusException {
        if (queueNames.length == 0) {
            queueNames = getQueueNames();
        }

        QueueStatus[] result = new QueueStatus[queueNames.length];

        //get all partitions, filter out requested statuses
        String output = runCheckedCommand(null, "sinfo", "--format=%P %a %l %F %N %C %D");

        Map<String, Map<String, String>> allMap =
                SlurmOutputParser.parseInfoOutput(output, "PARTITION", SlurmOutputParser.WHITESPACE_REGEX);

        for (int i = 0; i < queueNames.length; i++) {
            if (queueNames[i] == null) {
                //skip null values
                continue;
            }
            if (allMap == null || allMap.isEmpty()) {
                Exception exception =
                        new OctopusIOException(SlurmAdaptor.ADAPTOR_NAME, "Failed to get status of queues on server");
                result[i] = new QueueStatusImplementation(getScheduler(), queueNames[i], exception, null);
            } else {
                //state for only the requested queue
                Map<String, String> map = allMap.get(queueNames[i]);

                if (map == null || map.isEmpty()) {
                    Exception exception =
                            new NoSuchQueueException(SlurmAdaptor.ADAPTOR_NAME, "Cannot get status of queue " + queueNames[i]
                                    + " from server");
                    result[i] = new QueueStatusImplementation(getScheduler(), queueNames[i], exception, null);
                } else {
                    result[i] = new QueueStatusImplementation(getScheduler(), queueNames[i], null, map);
                }
            }
        }

        return result;

    }

    private JobStatus getJobStatusFromSqueueMap(Map<String, String> info, Job job) {
        Exception exception = null;

        if (info == null || info.isEmpty()) {
            LOGGER.debug("job {} not found in queue", job.getIdentifier());
            return null;
        }

        String state = info.get("STATE");

        return new JobStatusImplementation(job, state, null, exception, state.equals("RUNNING"), false, info);
    }

    //get an exit code from the scontrol "ExitCode" output field
    private static Integer exitcodeFromString(String value) throws OctopusException {
        if (value == null) {
            return null;
        }

        //the exit status may contain a ":" followed by the signal send to stop the job. Ignore
        if (value.contains(":")) {
            value = value.split(":")[0];
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "could not get job exit code from foudn value", e);
        }

    }
    
    private static boolean isFailedState(String state) {
        for(String validState: FAILED_STATES) {
            if (state.startsWith(validState)) {
                return true;
            }
        }
        return false;
    }
    
    //failed also implies done
    private static boolean isDoneState(String state) {
        return state.equals(DONE_STATE) || isFailedState(state);
    }

    private JobStatus getJobFromSControl(Job job) throws OctopusIOException, OctopusException {
        RemoteCommandRunner runner = runCommand(null, "scontrol", "-o", "show", "job", job.getIdentifier());

        if (!runner.success()) {
            LOGGER.debug("failed to get job status {}", runner);
            return null;
        }

        Map<String, String> info = SlurmOutputParser.parseScontrolOutput(runner.getStdout());

        return getJobStatusFromMap(info, job);
    }

    private JobStatus getJobStatusFromMap(Map<String, String> info, Job job) throws OctopusException {
        if (info == null || info.isEmpty()) {
            LOGGER.debug("job {} not found in scontrol/sacct output", job.getIdentifier());
            return null;
        }

        //compensate for two different cases for job id key
        String jobID = info.get("JobId");
        if (jobID == null) {
            jobID = info.get("JobID");
        }

        if (jobID == null || !jobID.equals(job.getIdentifier())) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Invalid scontrol/sacct output. Returned jobid \""
                    + info.get("JobId") + "\" does not match " + job.getIdentifier());
        }

        //compensate for two different cases for job state key
        String state = info.get("JobState");
        if (state == null) {
            state = info.get("State");
        }

        if (state == null) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Invalid scontrol output. Output does not contain job state");
        }

        Integer exitcode = exitcodeFromString(info.get("ExitCode"));

        String reason = info.get("Reason");

        Exception exception = null;
        if (reason != null && !reason.equalsIgnoreCase("none")) {
            //exclude non zero exit code from errors
            if (!reason.equals("NonZeroExitCode")) {
                exception = new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Slurm reported error reason: " + reason);
            }
        } else if (state.startsWith("CANCELLED")) {
            exception = new JobCanceledException(SlurmAdaptor.ADAPTOR_NAME, "Job " + state.toLowerCase());
        } else if (state.equals("FAILED") && exitcode != 0) {
            //non zero exit code (but no reason, as in sacct output), ignore
            exception = null;
        } else if (isFailedState(state)) {
            exception = new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Job failed for unknown reason");
        }

        JobStatus result =
                new JobStatusImplementation(job, state, exitcode, exception, state.equals("RUNNING"), isDoneState(state), info);

        LOGGER.debug("Got job status from scontrol/sacct output {}", result);

        return result;
    }

    @Override
    public JobStatus getJobStatus(Job job) throws OctopusException, OctopusIOException {
        String sQueueOutput = runCheckedCommand(null, "squeue", "--format=%i %P %j %u %T %M %l %D %R", "--jobs=" + job.getIdentifier());

        Map<String, Map<String, String>> sQueueMap =
                SlurmOutputParser.parseInfoOutput(sQueueOutput, "JOBID", SlurmOutputParser.WHITESPACE_REGEX);

        Map<String, String> info = sQueueMap.get(job.getIdentifier());

        JobStatus result = getJobStatusFromSqueueMap(info, job);

        //this job is not in the queue check sacct next (if available)
        if (result == null && config.accountingAvailable()) {
            //this job is not in the queue not scontrol, check sacct last (if available)
            RemoteCommandRunner runner = 
                    runCommand(null, "sacct", "-X", "-p", "--format=JobID,JobName,Partition,NTasks,"
                            + "Elapsed,State,ExitCode,AllocCPUS,DerivedExitCode,Submit,"
                            + "Suspended,Comment,Start,User,End,NNodes,Timelimit,Priority", "--jobs=" + job.getIdentifier());
            
            if (runner.getExitCode() != 0) {
                throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Error in getting sacct job status: " + runner);
            }
            
            if (!runner.getStderr().isEmpty()) {
                LOGGER.warn("Sacct produced error output: " + runner.getStderr());
            }
            
            Map<String, Map<String, String>> sacctMap =
                    SlurmOutputParser.parseInfoOutput(runner.getStdout(), "JobID", SlurmOutputParser.BAR_REGEX);

            result = getJobStatusFromMap(sacctMap.get(job.getIdentifier()), job);
        }

        //this job is not in the queue nor sacct, check scontrol last.
        if (result == null) {
            result = getJobFromSControl(job);
        }

        if (result == null) {
            throw new NoSuchJobException(SlurmAdaptor.ADAPTOR_NAME, "Unknown Job: " + job);
        }

        return result;
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) throws OctopusIOException, OctopusException {
        String[] jobIdentifiers = new String[jobs.length];
        for (int i = 0; i < jobIdentifiers.length; i++) {
            if (jobs[i] != null) {
                jobIdentifiers[i] = jobs[i].getIdentifier();
            }
        }

        //first attempt: see if it is in the queue
        //we get all jobs in one go, saving time
        String squeueOutput =
                runCheckedCommand(null, "squeue", "--format=%i %P %j %u %T %M %l %D %R",
                        "--jobs=" + CommandLineUtils.asCSList(jobIdentifiers));

        Map<String, Map<String, String>> sQueueMap =
                SlurmOutputParser.parseInfoOutput(squeueOutput, "JOBID", SlurmOutputParser.WHITESPACE_REGEX);

        Map<String, Map<String, String>> sacctMap = null;

        if (config.accountingAvailable()) {
            RemoteCommandRunner runner = 
                    runCommand(null, "sacct", "-X", "-p", "--format=JobID,JobName,Partition,NTasks,"
                            + "Elapsed,State,ExitCode,AllocCPUS,DerivedExitCode,Submit,"
                            + "Suspended,Comment,Start,User,End,NNodes,Timelimit,Priority","--jobs=" +
                            CommandLineUtils.asCSList(jobIdentifiers));
            
            if (runner.getExitCode() != 0) {
                throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Error in getting sacct job status: " + runner);
            }
            
            if (!runner.getStderr().isEmpty()) {
                LOGGER.warn("Sacct produced error output: " + runner.getStderr());
            }

            sacctMap = SlurmOutputParser.parseInfoOutput(runner.getStdout(), "JobID", SlurmOutputParser.BAR_REGEX);
        }

        JobStatus[] result = new JobStatus[jobs.length];

        for (int i = 0; i < jobs.length; i++) {
            //job not requested at all
            if (jobs[i] == null) {
                continue;
            }
            result[i] = getJobStatusFromSqueueMap(sQueueMap.get(jobIdentifiers[i]), jobs[i]);

            //this job is not in the queue check sacct next (if available)
            if (result[i] == null && sacctMap != null) {
                result[i] = getJobStatusFromMap(sacctMap.get(jobIdentifiers[i]), jobs[i]);
            }

            //this job is not in the queue nor sacct, check scontrol last.
            if (result[i] == null) {
                result[i] = getJobFromSControl(jobs[i]);
            }

            //job really does not seem to exist (anymore)
            if (result[i] == null) {
                NoSuchJobException exception =
                        new NoSuchJobException(SlurmAdaptor.ADAPTOR_NAME, "Unknown Job: " + jobIdentifiers[i]);
                result[i] = new JobStatusImplementation(jobs[i], null, null, exception, false, false, null);
            }
        }
        return result;
    }

}
