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
package nl.esciencecenter.octopus.adaptors.scripting;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import nl.esciencecenter.octopus.adaptors.slurm.SlurmAdaptor;
import nl.esciencecenter.octopus.adaptors.ssh.SshAdaptor;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.exceptions.IncompleteJobDescriptionException;
import nl.esciencecenter.octopus.exceptions.InvalidJobDescriptionException;
import nl.esciencecenter.octopus.exceptions.InvalidLocationException;
import nl.esciencecenter.octopus.exceptions.NoSuchQueueException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;

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
    private final OctopusEngine engine;
    private final Scheduler subScheduler;
    private final FileSystem subFileSystem;

    private final OctopusProperties properties;

    private final long pollDelay;

    private static boolean supportsScheme(String scheme, String[] supportedSchemes) {
        for (String validScheme : supportedSchemes) {
            if (validScheme.equalsIgnoreCase(scheme)) {
                return true;
            }
        }

        return false;
    }

    protected static URI getSubSchedulerLocation(URI location, String adaptorName, String... supportedSchemes)
            throws InvalidLocationException {
        if (!supportsScheme(location.getScheme(), supportedSchemes)) {
            throw new InvalidLocationException(adaptorName, "Adaptor does not support scheme \"" + location.getScheme() + "\"");
        }

        //only null or "/" are allowed as paths
        if (!(location.getPath() == null || location.getPath().length() == 0 || location.getPath().equals("/"))) {
            throw new InvalidLocationException(adaptorName, "Paths are not allowed in a uri for this scheduler, uri given: "
                    + location);
        }

        if (location.getFragment() != null && location.getFragment().length() > 0) {
            throw new InvalidLocationException(adaptorName, "Fragments are not allowed in a uri for this scheduler, uri given: "
                    + location);
        }

        try {
            if (location.getHost() == null || location.getHost().length() == 0) {
                return new URI("local:///");
            }

            return new URI("ssh", location.getAuthority(), null, null, null);
        } catch (URISyntaxException e) {
            throw new InvalidLocationException(adaptorName, "Failed to create URI for scheduler connection", e);
        }
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
    protected static void verifyJobDescription(JobDescription description, String adaptorName) throws OctopusException {
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

    protected SchedulerConnection(ScriptingAdaptor adaptor, URI location, Credential credential, OctopusProperties properties,
            OctopusEngine engine, long pollDelay) throws OctopusIOException, OctopusException {

        this.adaptor = adaptor;
        this.engine = engine;
        this.properties = properties;
        this.pollDelay = pollDelay;

        id = adaptor.getName() + "-" + getNextSchedulerID();

        URI subSchedulerLocation = getSubSchedulerLocation(location, adaptor.getName(), adaptor.getSupportedSchemes());

        LOGGER.debug("creating sub scheduler for {} adaptor at {}", adaptor.getName(), subSchedulerLocation);
        Map<String, String> subSchedulerProperties = new HashMap<String, String>();

        //since we expect commands to be done almost instantaneously, we poll quite frequently (local operation anyway)
        if (subSchedulerLocation.getScheme().equals("ssh")) {
            subSchedulerProperties.put(SshAdaptor.POLLING_DELAY, "100");
        }
        subScheduler = engine.jobs().newScheduler(subSchedulerLocation, credential, subSchedulerProperties);

        LOGGER.debug("creating file system for {} adaptor at {}", adaptor.getName(), subSchedulerLocation);
        subFileSystem = engine.files().newFileSystem(subSchedulerLocation, credential, null);
    }

    protected AbsolutePath getFsEntryPath() {
        return subFileSystem.getEntryPath();
    }

    public OctopusProperties getProperties() {
        return properties;
    }

    public String getID() {
        return id;
    }

    /**
     * Run a command on the remote scheduler machine.
     */
    public RemoteCommandRunner runCommand(String stdin, String executable, String... arguments) throws OctopusException,
            OctopusIOException {
        return new RemoteCommandRunner(engine, subScheduler, adaptor.getName(), stdin, executable, arguments);
    }

    /**
     * Run a command. Throw an exception if the command returns a non-zero exit code, or prints to stderr.
     */
    public String runCheckedCommand(String stdin, String executable, String... arguments) throws OctopusException,
            OctopusIOException {
        RemoteCommandRunner runner = new RemoteCommandRunner(engine, subScheduler, adaptor.getName(), stdin, executable,
                arguments);

        if (!runner.success()) {
            throw new OctopusException(adaptor.getName(), "could not run command \"" + executable + "\" with arguments \""
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
        //create a hashset with all given queues
        HashSet<String> invalidQueues = new HashSet<String>(Arrays.asList(givenQueueNames));

        //remove all valid queues from the set
        invalidQueues.removeAll(Arrays.asList(getQueueNames()));

        //if anything remains, these are invalid. throw an exception with the invalid queues
        if (!invalidQueues.isEmpty()) {
            throw new NoSuchQueueException(adaptor.getName(), "Invalid queues given: "
                    + Arrays.toString(invalidQueues.toArray(new String[0])));
        }
    }

    public JobStatus waitUntilDone(Job job, long timeout) throws OctopusIOException, OctopusException {
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

    public JobStatus waitUntilRunning(Job job, long timeout) throws OctopusIOException, OctopusException {
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
     * check if the given working directory exists. Useful for schedulers that do not check this (like slurm)
     * 
     * @param workingDirectory
     *            the working directory (either absolute or relative) as given by the user.
     */
    protected void checkWorkingDirectory(String workingDirectory) throws OctopusIOException, OctopusException {
        if (workingDirectory == null) {
            return;
        }

        AbsolutePath path;
        if (workingDirectory.startsWith("/")) {
            path = engine.files().newPath(subFileSystem, new RelativePath(workingDirectory));
        } else {
            //make relative path absolute
            path = getFsEntryPath().resolve(new RelativePath(workingDirectory));
        }
        if (!engine.files().exists(path)) {
            throw new InvalidJobDescriptionException(SlurmAdaptor.ADAPTOR_NAME, "Working directory does not exist: " + path);
        }
    }

    public void close() throws OctopusIOException, OctopusException {
        engine.jobs().close(subScheduler);
    }

    //implemented by sub-class

    /**
     * As the SchedulerImplementation contains the list of queues, the subclass is responsible of implementing this function
     */
    public abstract Scheduler getScheduler();

    public abstract String[] getQueueNames();

    public abstract String getDefaultQueueName();

    public abstract QueueStatus getQueueStatus(String queueName) throws OctopusIOException, OctopusException;

    public abstract QueueStatus[] getQueueStatuses(String... queueNames) throws OctopusIOException, OctopusException;

    public abstract Job[] getJobs(String... queueNames) throws OctopusIOException, OctopusException;

    public abstract Job submitJob(JobDescription description) throws OctopusIOException, OctopusException;

    public abstract JobStatus cancelJob(Job job) throws OctopusIOException, OctopusException;

    public abstract JobStatus getJobStatus(Job job) throws OctopusException, OctopusIOException;

    public abstract JobStatus[] getJobStatuses(Job... jobs) throws OctopusIOException, OctopusException;

}