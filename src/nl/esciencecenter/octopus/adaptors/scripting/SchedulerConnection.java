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
    private final Scheduler sshScheduler;
    private final FileSystem sshFileSystem;
    
    private final OctopusProperties properties;

    private final long pollDelay;
    
    protected SchedulerConnection(ScriptingAdaptor adaptor, URI location, Credential credential, OctopusProperties properties, 
            OctopusEngine engine, long pollDelay) throws OctopusIOException, OctopusException {
    
        this.adaptor = adaptor;
        this.engine = engine;
        this.properties = properties;
        this.pollDelay = pollDelay;
        
        adaptor.checkLocation(location);
        
        try {
            id = adaptor.getName() + "-" + getNextSchedulerID();
            //FIXME: check if this works for encode uri's, illegal characters, fragments, etc..
            URI actualLocation = new URI("ssh", location.getSchemeSpecificPart(), location.getFragment());

            if (location.getHost() == null || location.getHost().length() == 0) {
                actualLocation = new URI("local:///");
            }

            LOGGER.debug("creating ssh scheduler for {} adaptor at {}", adaptor.getName(), actualLocation);
            Map<String, String> sshProperties = new HashMap<String, String>();
            //FIXME: compensating for ssh performance bug #143
            sshProperties.put(SshAdaptor.POLLING_DELAY, "100");
            sshScheduler = engine.jobs().newScheduler(actualLocation, credential, sshProperties);

            LOGGER.debug("creating file system for {} adaptor at {}", adaptor.getName(), actualLocation);
            sshFileSystem = engine.files().newFileSystem(actualLocation, credential, null);

        } catch (URISyntaxException e) {
            throw new OctopusException(adaptor.getName(), "Cannot create SSH URI from location " + location, e);
        }
    }

   

    /**
     * Run a command on the remote scheduler machine.
     */
    public RemoteCommandRunner runCommand(String stdin, String executable, String... arguments) throws OctopusException,
            OctopusIOException {
        return new RemoteCommandRunner(engine, sshScheduler, adaptor.getName(), stdin, executable, arguments);
    }

    /**
     * Run a command. Throw an exception if the command returns a non-zero exit code, or prints to stderr.
     */
    public String runCheckedCommand(String stdin, String executable, String... arguments) throws OctopusException,
            OctopusIOException {
        RemoteCommandRunner runner = new RemoteCommandRunner(engine, sshScheduler, adaptor.getName(), stdin, executable, arguments);

        if (!runner.success()) {
            throw new OctopusException(adaptor.getName(), "could not run command \"" + executable + "\" with arguments \""
                    + Arrays.toString(arguments) + "\" at \"" + sshScheduler + "\". Exit code = " + runner.getExitCode()
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

    public OctopusProperties getProperties() {
        return properties;
    }

    public String getID() {
        return id;
    }

    public void close() throws OctopusIOException, OctopusException {
        engine.jobs().close(sshScheduler);
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

    //do some checks on the job description. subclass could perform additional checks
    protected void verifyJobDescription(JobDescription description) throws OctopusException {
        String executable = description.getExecutable();

        if (executable == null) {
            throw new IncompleteJobDescriptionException(adaptor.getName(), "Executable missing in JobDescription!");
        }

        int nodeCount = description.getNodeCount();

        if (nodeCount < 1) {
            throw new InvalidJobDescriptionException(adaptor.getName(), "Illegal node count: " + nodeCount);
        }

        int processesPerNode = description.getProcessesPerNode();

        if (processesPerNode < 1) {
            throw new InvalidJobDescriptionException(adaptor.getName(), "Illegal processes per node count: " + processesPerNode);
        }

        int maxTime = description.getMaxTime();

        if (maxTime <= 0) {
            throw new InvalidJobDescriptionException(adaptor.getName(), "Illegal maximum runtime: " + maxTime);
        }

        if (description.isInteractive()) {
            throw new InvalidJobDescriptionException(adaptor.getName(), "Adaptor does not support interactive jobs");
        }

    }

    protected AbsolutePath getFsEntryPath() {
        return sshFileSystem.getEntryPath();
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
            path = engine.files().newPath(sshFileSystem, new RelativePath(workingDirectory));
        } else {
            //make relative path absolute
            path = getFsEntryPath().resolve(new RelativePath(workingDirectory));
        }
        if (!engine.files().exists(path)) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Working directory does not exist: " + path);
        }
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