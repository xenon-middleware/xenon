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
import java.util.Properties;

import nl.esciencecenter.octopus.adaptors.scripting.RemoteCommandRunner;
import nl.esciencecenter.octopus.adaptors.scripting.SchedulerConnection;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
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
 * @author Niels Drost
 * 
 */
public class SlurmSchedulerConnection extends SchedulerConnection {

    private static final Logger logger = LoggerFactory.getLogger(SlurmSchedulerConnection.class);

    public static final String PROPERTY_PREFIX = OctopusEngine.ADAPTORS + SlurmAdaptor.ADAPTOR_NAME + ".";
    
    public static final String IGNORE_VERSION_PROPERTY = PROPERTY_PREFIX + "ignore.version";

    /** List of {NAME, DEFAULT_VALUE, DESCRIPTION} for properties. */
    private static final String[][] validPropertiesList = new String[][] {
        {
            IGNORE_VERSION_PROPERTY,
            "false",
            "Boolean: If true, the version check is skipped when connecting to remote machines. "
                    + "WARNING: it is not recommended to use this setting in production environments" },
    };

    public static final String JOB_OPTION_JOB_SCRIPT = "job.script";

    public static final String[] VALID_JOB_OPTIONS = new String[] { JOB_OPTION_JOB_SCRIPT };

    private final String[] queueNames;
    private final String defaultQueueName;

    private final Scheduler scheduler;

    private final SlurmConfig config;

    SlurmSchedulerConnection(URI location, Credential credential, Properties properties, OctopusEngine engine)
            throws OctopusIOException, OctopusException {
        super(location, credential, properties, engine, validPropertiesList, SlurmAdaptor.ADAPTOR_NAME,
                SlurmAdaptor.ADAPTOR_SCHEMES);

        boolean ignoreVersion = getProperties().getBooleanProperty(IGNORE_VERSION_PROPERTY);
        
        this.config = fetchConfiguration(ignoreVersion);
        
        this.queueNames = fetchQueueNames();
        this.defaultQueueName = findDefaultQueue();

        scheduler =
                new SchedulerImplementation(SlurmAdaptor.ADAPTOR_NAME, getID(), location, queueNames, credential,
                        getProperties(), false, false, true);

        logger.debug("new slurm scheduler connection {}", scheduler);
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

    private boolean checkAccountingAvailable() throws OctopusIOException, OctopusException {
        RemoteCommandRunner runner = runCommand(null, "sacct");

        boolean result = runner.success();

        logger.debug("accounting available {}, command output {}", result, runner);

        return result;
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

        Map<String, Map<String, String>> infoMaps = SlurmOutputParser.parseInfoOutput(output, "PARTITION");

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

        Map<String, Map<String, String>> allMap = SlurmOutputParser.parseInfoOutput(output, "PARTITION");

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

    private JobStatus jobStatusFromSqueueMap(Map<String, String> info, Job job) {
        Exception exception = null;

        if (info == null || info.isEmpty()) {
            logger.debug("job {} not found in queue", job.getIdentifier());
            return null;
        }

        String state = info.get("STATE");

        return new JobStatusImplementation(job, state, null, exception, state.equals("RUNNING"), false, info);
    }

    /**
     * @param job
     * @return
     */
    private JobStatus getJobFromSAcct(Job job) {
        // TODO Auto-generated method stub
        return null;
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

    private static boolean isDoneState(String state) {
        return state.equals("COMPLETED") || state.equals("FAILED") || state.equals("CANCELLED") || state.equals("NODE_FAIL")
                || state.equals("TIMEOUT") || state.equals("PREEMPTED");
    }
    
    private static boolean isFailedState(String state) {
        return state.equals("FAILED") || state.equals("CANCELLED") || state.equals("NODE_FAIL")
                || state.equals("TIMEOUT") || state.equals("PREEMPTED");
    }

    private JobStatus getJobFromSControl(Job job) throws OctopusIOException, OctopusException {
        RemoteCommandRunner runner = runCommand(null, "scontrol", "-o", "show", "job", job.getIdentifier());

        if (!runner.success()) {
            logger.debug("failed to get job status {}", runner);
            return null;
        }

        Map<String, String> info = SlurmOutputParser.parseScontrolOutput(runner.getStdout());

        if (!info.containsKey("JobId") || !info.get("JobId").equals(job.getIdentifier())) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Invalid scontrol output. Returned jobid \""
                    + info.get("JobId") + "\" does not match " + job.getIdentifier());
        }

        String state = info.get("JobState");

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
        } else if (state.equals("CANCELLED")) {
            exception = new JobCanceledException(SlurmAdaptor.ADAPTOR_NAME, "Job canceled");
        } else if (isFailedState(state)) {
            exception = new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Job failed for unknown reason");
        }
        
        JobStatus result = new JobStatusImplementation(job, state, exitcode, exception, state.equals("RUNNING"), isDoneState(state), info);

        logger.debug("Got job status from scontrol output {}", result);
        
        return result;
    }

    @Override
    public JobStatus getJobStatus(Job job) throws OctopusException, OctopusIOException {
        //String output = runCheckedCommand(null, "squeue", "--format=%i %P %j %u %T %M %l %D %R", "--jobs=" + job.getIdentifier());
        String output = runCheckedCommand(null, "squeue", "--format=%i %P %j %u %T %M %l %D %R");

        Map<String, Map<String, String>> allMap = SlurmOutputParser.parseInfoOutput(output, "JOBID");

        Map<String, String> info = allMap.get(job.getIdentifier());

        JobStatus result = jobStatusFromSqueueMap(info, job);

        if (result == null) {
            //this job is not in the queue, check scontrol next
            result = getJobFromSControl(job);
        }

        if (result == null && config.getAccountingAvailable()) {
            //this job is not in the queue not scontrol, check sacct last (if available)
            result = getJobFromSAcct(job);
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
        String output =
                runCheckedCommand(null, "squeue", "--format=%i %P %j %u %T %M %l %D %R",
                        "--jobs=" + CommandLineUtils.asCSList(jobIdentifiers));

        Map<String, Map<String, String>> allMap = SlurmOutputParser.parseInfoOutput(output, "JOBID");

        JobStatus[] result = new JobStatus[jobs.length];

        for (int i = 0; i < jobs.length; i++) {
            if (jobs[i] == null) {
                continue;
            }
            result[i] = jobStatusFromSqueueMap(allMap.get(jobIdentifiers[i]), jobs[i]);

            if (result[i] == null) {
                //this job is not in the queue, check scontrol next
                result[i] = getJobFromSControl(jobs[i]);
            }

            if (result[i] == null && config.getAccountingAvailable()) {
                //this job is not in the queue not scontrol, check sacct last (if available)
                result[i] = getJobFromSAcct(jobs[i]);
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
