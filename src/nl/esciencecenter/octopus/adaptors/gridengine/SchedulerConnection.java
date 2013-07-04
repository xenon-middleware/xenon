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
package nl.esciencecenter.octopus.adaptors.gridengine;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.util.RemoteCommandRunner;
import nl.esciencecenter.octopus.exceptions.IncompleteJobDescriptionException;
import nl.esciencecenter.octopus.exceptions.InvalidJobDescriptionException;
import nl.esciencecenter.octopus.exceptions.InvalidLocationException;
import nl.esciencecenter.octopus.exceptions.NoSuchQueueException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
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

    protected static final Logger logger = LoggerFactory.getLogger(SchedulerConnection.class);

    private static int schedulerID = 0;

    protected static synchronized int getNextSchedulerID() {
        return schedulerID++;
    }
    
    private final String adaptorName;
    private final String[] adaptorSchemes;
    private final String id;
    private final OctopusEngine engine;
    private final Scheduler sshScheduler;
    private final FileSystem sshFileSystem;
    
    private final int pollDelay;

    private final String[][] defaultProperties;

    private final Map<String, String> supportedProperties;

    private final OctopusProperties properties;

    protected SchedulerConnection(URI location, Credential credential, Properties properties, OctopusEngine engine,
            String[][] defaultProperties, String adaptorName, String[] adaptorSchemes) throws OctopusIOException,
            OctopusException {
        this.engine = engine;
        this.adaptorName = adaptorName;
        this.adaptorSchemes = adaptorSchemes;

        checkLocation(location);

        //FIXME: duplicate from Adaptor class
        
        this.defaultProperties = (defaultProperties == null ? new String[0][0] : defaultProperties);

        Map<String, String> tmp = new HashMap<String, String>();

        if (defaultProperties != null) {
            for (int i = 0; i < defaultProperties.length; i++) {
                tmp.put(defaultProperties[i][0], defaultProperties[i][2]);
            }
        }

        this.supportedProperties = Collections.unmodifiableMap(tmp);
        this.properties = processProperties(properties);

        this.pollDelay = this.properties.getIntProperty(GridEngineSchedulerConnection.POLL_DELAY_PROPERTY);
        
        try {
            id = adaptorName + "-" + getNextSchedulerID();
            URI actualLocation = new URI("ssh", location.getSchemeSpecificPart(), location.getFragment());

            if (location.getHost() == null || location.getHost().length() == 0) {
                //FIXME: check if this works for encode uri's, illegal characters, fragments, etc..
                actualLocation = new URI("local:///");
            }

            logger.debug("creating ssh scheduler for {} adaptor at {}", adaptorName, actualLocation);
            sshScheduler = engine.jobs().newScheduler(actualLocation, credential, null);

            logger.debug("creating file system for {} adaptor at {}", adaptorName, actualLocation);
            sshFileSystem = engine.files().newFileSystem(actualLocation, credential, null);

        } catch (URISyntaxException e) {
            throw new OctopusException(adaptorName, "cannot create ssh uri from given location " + location, e);
        }
    }

    //FIXME:ALMOST duplicated from Adaptor, replace with generic properties handling, see #132
    private OctopusProperties processProperties(Properties properties) throws OctopusException {

        Set<String> validSet = new HashSet<String>();

        for (int i = 0; i < defaultProperties.length; i++) {
            validSet.add(defaultProperties[i][0]);
        }

        OctopusProperties p = null;

        if (properties == null) {
            p = new OctopusProperties();
        } else {
            p = new OctopusProperties(properties);
        }

        for (Map.Entry<Object, Object> entry : p.entrySet()) {
            if (!validSet.contains(entry.getKey())) {
                throw new OctopusException(adaptorName, "Unknown property " + entry);
            }
        }

        return new OctopusProperties(defaultProperties, p);
    }

    void checkLocation(URI location) throws InvalidLocationException {
        //only null or "/" are allowed as paths
        if (!(location.getPath() == null || location.getPath().length() == 0 || location.getPath().equals("/"))) {
            throw new InvalidLocationException(adaptorName, "Paths are not allowed in a uri for this scheduler, uri given: "
                    + location);
        }

        if (location.getFragment() != null && location.getFragment().length() > 0) {
            throw new InvalidLocationException(adaptorName, "Fragments are not allowed in a uri for this scheduler, uri given: "
                    + location);
        }

        for (String scheme : adaptorSchemes) {
            if (scheme.equals(location.getScheme())) {
                //alls-well
                return;
            }
        }
        throw new InvalidLocationException(adaptorName, "Adaptor does not support scheme: " + location.getScheme());
    }

    String runCommand(String stdin, String executable, String... arguments) throws OctopusException, OctopusIOException {
        JobDescription description = new JobDescription();
        description.setInteractive(true);
        description.setExecutable(executable);
        description.setArguments(arguments);

        RemoteCommandRunner runner = new RemoteCommandRunner(engine, sshScheduler, stdin, description);

        String stderr = runner.getStderr();

        if (runner.getExitCode() != 0 || !stderr.isEmpty()) {
            throw new CommandFailedException(adaptorName, "could not run command \"" + executable + "\" with arguments \""
                    + Arrays.toString(arguments) + "\" at \"" + sshScheduler + "\". Exit code = " + runner.getExitCode()
                    + " Error output: " + stderr, runner.getExitCode(), runner.getStderr());
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
            throw new NoSuchQueueException(adaptorName, "Invalid queues given: "
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

    protected AbsolutePath getFsEntryPath() {
        return sshFileSystem.getEntryPath();
    }

    //implemented by sub-class

    /**
     * As the SchedulerImplementation contains the list of queues, the subclass is responsible of implementing this function
     */
    abstract Scheduler getScheduler();

    abstract String[] getQueueNames();
    
    abstract QueueStatus getQueueStatus(String queueName) throws OctopusIOException, OctopusException;

    abstract QueueStatus[] getQueueStatuses(String... queueNames) throws OctopusIOException, OctopusException;

    abstract Job[] getJobs(String... queueNames) throws OctopusIOException, OctopusException;

    abstract Job submitJob(JobDescription description) throws OctopusIOException, OctopusException;

    abstract JobStatus cancelJob(Job job) throws OctopusIOException, OctopusException;

    abstract JobStatus getJobStatus(Job job) throws OctopusException, OctopusIOException;

    abstract JobStatus[] getJobStatuses(Job... jobs) throws OctopusIOException, OctopusException;

}