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
package nl.esciencecenter.cobalt.adaptors.scripting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import nl.esciencecenter.cobalt.InvalidLocationException;
import nl.esciencecenter.cobalt.CobaltException;
import nl.esciencecenter.cobalt.adaptors.slurm.SlurmAdaptor;
import nl.esciencecenter.cobalt.adaptors.ssh.SshAdaptor;
import nl.esciencecenter.cobalt.credentials.Credential;
import nl.esciencecenter.cobalt.engine.CobaltEngine;
import nl.esciencecenter.cobalt.engine.CobaltProperties;
import nl.esciencecenter.cobalt.files.FileSystem;
import nl.esciencecenter.cobalt.files.Path;
import nl.esciencecenter.cobalt.files.RelativePath;
import nl.esciencecenter.cobalt.jobs.IncompleteJobDescriptionException;
import nl.esciencecenter.cobalt.jobs.InvalidJobDescriptionException;
import nl.esciencecenter.cobalt.jobs.Job;
import nl.esciencecenter.cobalt.jobs.JobDescription;
import nl.esciencecenter.cobalt.jobs.JobStatus;
import nl.esciencecenter.cobalt.jobs.NoSuchQueueException;
import nl.esciencecenter.cobalt.jobs.QueueStatus;
import nl.esciencecenter.cobalt.jobs.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection to a remote scheduler, implemented by calling command line commands over a ssh connection.
 * 
 * @author Niels Drost
 * 
 */
public abstract class SchedulerConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerConnection.class);

    private static int schedulerID = 0;

    protected static synchronized int getNextSchedulerID() {
        return schedulerID++;
    }

    private final ScriptingAdaptor adaptor;
    private final String id;
    private final CobaltEngine engine;
    private final Scheduler subScheduler;
    private final FileSystem subFileSystem;

    private final CobaltProperties properties;

    private final long pollDelay;

    protected static boolean supportsScheme(String scheme, String[] supportedSchemes) {
        for (String validScheme : supportedSchemes) {
            if (validScheme.equalsIgnoreCase(scheme)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Do some checks on a job description.
     * 
     * @param description
     *            the job description to check
     * @param adaptorName
     *            the name of the adaptor. Used when an exception is thrown
     * @throws IncompleteJobDescription
     *             if the description is missing a mandatory value.
     * @throws InvalidJobDescription
     *             if the description contains illegal values.
     */
    protected static void verifyJobDescription(JobDescription description, String adaptorName) throws CobaltException {
        String executable = description.getExecutable();

        if (executable == null) {
            throw new IncompleteJobDescriptionException(adaptorName, "Executable missing in JobDescription!");
        }

        int nodeCount = description.getNodeCount();

        if (nodeCount < 1) {
            throw new InvalidJobDescriptionException(adaptorName, "Illegal node count: " + nodeCount);
        }

        int processesPerNode = description.getProcessesPerNode();

        if (processesPerNode < 1) {
            throw new InvalidJobDescriptionException(adaptorName, "Illegal processes per node count: " + processesPerNode);
        }

        int maxTime = description.getMaxTime();

        if (maxTime <= 0) {
            throw new InvalidJobDescriptionException(adaptorName, "Illegal maximum runtime: " + maxTime);
        }

        if (description.isInteractive()) {
            throw new InvalidJobDescriptionException(adaptorName, "Adaptor does not support interactive jobs");
        }

    }

    protected static void verifyJobOptions(Map<String, String> options, String[] validOptions, String adaptorName)
            throws InvalidJobDescriptionException {

        //check if all given job options are valid
        for (String option : options.keySet()) {
            boolean found = false;
            for (String validOption : validOptions) {
                if (validOption.equals(option)) {
                    found = true;
                }
            }
            if (!found) {
                throw new InvalidJobDescriptionException(adaptorName, "Given Job option \"" + option + "\" not supported");
            }
        }
    }

    /**
     * Check if the info map for a job exists, contains the expected job ID, and contains the given additional fields
     * 
     * @param info
     *            the map the job info should be .
     * @param job
     *            the job to check the presence for.
     * @param jobIDField
     *            the field which contains the job id.
     * @param additionalFields
     *            any additional fields to check the presence of.
     * @throws CobaltException
     *             if any fields are missing or incorrect
     */
    protected static void verifyJobInfo(Map<String, String> jobInfo, Job job, String adaptorName, String jobIDField,
            String... additionalFields) throws CobaltException {
        if (jobInfo == null) {
            //redundant check, calling functions usually already check for this and return null.
            throw new CobaltException(adaptorName, "Job " + job.getIdentifier() + " not found in job info");
        }

        String jobID = jobInfo.get(jobIDField);

        if (jobID == null) {
            throw new CobaltException(adaptorName, "Invalid job info. Info does not contain job id");
        }

        if (!jobID.equals(job.getIdentifier())) {
            throw new CobaltException(adaptorName, "Invalid job info. Found job id \"" + jobID + "\" does not match "
                    + job.getIdentifier());
        }

        for (String field : additionalFields) {
            if (!jobInfo.containsKey(field)) {
                throw new CobaltException(adaptorName, "Invalid job info. Info does not contain mandatory field \"" + field
                        + "\"");
            }
        }
    }

    protected static String identifiersAsCSList(Job[] jobs) {
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

    protected SchedulerConnection(ScriptingAdaptor adaptor, String scheme, String location, Credential credential, 
            CobaltProperties properties, CobaltEngine engine, long pollDelay) throws CobaltException {

        this.adaptor = adaptor;
        this.engine = engine;
        this.properties = properties;
        this.pollDelay = pollDelay;

        if (!supportsScheme(scheme, adaptor.getSupportedSchemes())) {
            throw new InvalidLocationException(adaptor.getName(), "Adaptor does not support scheme \"" + scheme + "\"");
        }
        
        id = adaptor.getName() + "-" + getNextSchedulerID();

        String subJobScheme = null;
        String subFileScheme = null;
        String subLocation = null;
        
        if (location == null|| location.length() == 0 || location.equals("/")) {
            subJobScheme = "local";
            subFileScheme = "file";
            subLocation = "/";
        } else { 
            subJobScheme = "ssh";
            subFileScheme = "sftp";
            subLocation = location;
        }
 
        LOGGER.debug("creating sub scheduler for {} adaptor at {}", adaptor.getName(), (subJobScheme + "://" + subLocation));
        Map<String, String> subSchedulerProperties = new HashMap<String, String>();

        //since we expect commands to be done almost instantaneously, we poll quite frequently (local operation anyway)
        if (subJobScheme.equals("ssh")) {
            subSchedulerProperties.put(SshAdaptor.POLLING_DELAY, "100");
        }
        subScheduler = engine.jobs().newScheduler(subJobScheme, subLocation, credential, subSchedulerProperties);

        LOGGER.debug("creating file system for {} adaptor at {}", adaptor.getName(), (subFileScheme + "://" + subLocation));
        subFileSystem = engine.files().newFileSystem(subFileScheme, subLocation, credential, null);
    }

    protected Path getFsEntryPath() {
        return subFileSystem.getEntryPath();
    }

    public CobaltProperties getProperties() {
        return properties;
    }

    public String getID() {
        return id;
    }

    /**
     * Run a command on the remote scheduler machine.
     */
    public RemoteCommandRunner runCommand(String stdin, String executable, String... arguments) throws CobaltException {
        return new RemoteCommandRunner(engine, subScheduler, adaptor.getName(), stdin, executable, arguments);
    }

    /**
     * Run a command. Throw an exception if the command returns a non-zero exit code, or prints to stderr.
     */
    public String runCheckedCommand(String stdin, String executable, String... arguments) throws CobaltException { 
        RemoteCommandRunner runner = new RemoteCommandRunner(engine, subScheduler, adaptor.getName(), stdin, executable,
                arguments);

        if (!runner.success()) {
            throw new CobaltException(adaptor.getName(), "could not run command \"" + executable + "\" with arguments \""
                    + Arrays.toString(arguments) + "\" at \"" + subScheduler + "\". Exit code = " + runner.getExitCode()
                    + " Output: " + runner.getStdout() + " Error output: " + runner.getStderr());
        }

        return runner.getStdout();
    }

    /**
     * Checks if the queue names given are valid, and throw an exception otherwise. Checks against the list of queues when the
     * scheduler was created.
     */
    protected void checkQueueNames(String[] givenQueueNames) throws NoSuchQueueException {
        //create a hash set with all given queues
        HashSet<String> invalidQueues = new HashSet<String>(Arrays.asList(givenQueueNames));

        //remove all valid queues from the set
        invalidQueues.removeAll(Arrays.asList(getQueueNames()));

        //if anything remains, these are invalid. throw an exception with the invalid queues
        if (!invalidQueues.isEmpty()) {
            throw new NoSuchQueueException(adaptor.getName(), "Invalid queues given: "
                    + Arrays.toString(invalidQueues.toArray(new String[invalidQueues.size()])));
        }
    }

    public JobStatus waitUntilDone(Job job, long timeout) throws CobaltException {
        long deadline = System.currentTimeMillis() + timeout;

        if (timeout == 0) {
            deadline = Long.MAX_VALUE;
        }

        JobStatus status = null;

        //make sure status is retrieved at least once
        while (status == null || System.currentTimeMillis() < deadline) {
            status = getJobStatus(job);

            if (status.isDone()) {
                return status;
            }

            try {
                Thread.sleep(pollDelay);
            } catch (InterruptedException e) {
                return status;
            }
        }

        return status;
    }

    public JobStatus waitUntilRunning(Job job, long timeout) throws CobaltException {
        long deadline = System.currentTimeMillis() + timeout;

        if (timeout == 0) {
            deadline = Long.MAX_VALUE;
        }

        JobStatus status = null;

        //make sure status is retrieved at least once
        while (status == null || System.currentTimeMillis() < deadline) {
            status = getJobStatus(job);

            if (status.isRunning() || status.isDone()) {
                return status;
            }

            try {
                Thread.sleep(pollDelay);
            } catch (InterruptedException e) {
                return status;
            }
        }

        return status;
    }

    /**
     * check if the given working directory exists. Useful for schedulers that do not check this (like Slurm)
     * 
     * @param workingDirectory
     *            the working directory (either absolute or relative) as given by the user.
     */
    protected void checkWorkingDirectory(String workingDirectory) throws CobaltException {
        if (workingDirectory == null) {
            return;
        }

        Path path;
        if (workingDirectory.startsWith("/")) {
            path = engine.files().newPath(subFileSystem, new RelativePath(workingDirectory));
        } else {
            //make relative path absolute
            Path fsEntryPath = getFsEntryPath();
            path = engine.files().newPath(fsEntryPath.getFileSystem(), fsEntryPath.getRelativePath().resolve(workingDirectory));
        }
        if (!engine.files().exists(path)) {
            throw new InvalidJobDescriptionException(SlurmAdaptor.ADAPTOR_NAME, "Working directory does not exist: " + path);
        }
    }

    public void close() throws CobaltException {
        engine.jobs().close(subScheduler);
    }

    //implemented by sub-class

    /**
     * As the SchedulerImplementation contains the list of queues, the subclass is responsible of implementing this function
     */
    public abstract Scheduler getScheduler();

    public abstract String[] getQueueNames();

    public abstract String getDefaultQueueName();

    public abstract QueueStatus getQueueStatus(String queueName) throws CobaltException;

    public abstract QueueStatus[] getQueueStatuses(String... queueNames) throws CobaltException;

    public abstract Job[] getJobs(String... queueNames) throws CobaltException;

    public abstract Job submitJob(JobDescription description) throws CobaltException;

    public abstract JobStatus cancelJob(Job job) throws CobaltException;

    public abstract JobStatus getJobStatus(Job job) throws CobaltException;

    public abstract JobStatus[] getJobStatuses(Job... jobs) throws CobaltException;

}