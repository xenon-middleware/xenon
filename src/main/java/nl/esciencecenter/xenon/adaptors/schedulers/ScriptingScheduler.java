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
package nl.esciencecenter.xenon.adaptors.schedulers;

import static nl.esciencecenter.xenon.adaptors.schedulers.slurm.SlurmSchedulerAdaptor.ADAPTOR_NAME;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.adaptors.schedulers.local.LocalSchedulerAdaptor;
import nl.esciencecenter.xenon.adaptors.schedulers.ssh.SshSchedulerAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.schedulers.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;
import nl.esciencecenter.xenon.schedulers.NoSuchQueueException;
import nl.esciencecenter.xenon.schedulers.QueueStatus;
import nl.esciencecenter.xenon.schedulers.Scheduler;
import nl.esciencecenter.xenon.schedulers.Streams;

/**
 * Connection to a remote scheduler, implemented by calling command line commands over a ssh connection.
 *
 */
public abstract class ScriptingScheduler extends Scheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptingScheduler.class);

    protected final Scheduler subScheduler;
    protected final FileSystem subFileSystem;

    protected final long pollDelay;

    protected ScriptingScheduler(String uniqueID, String adaptor, String location, Credential credential, Map<String, String> prop,
            XenonPropertyDescription[] validProperties, String pollDelayProperty) throws XenonException {

        super(uniqueID, adaptor, location, ScriptingUtils.getProperties(validProperties, location, prop));

        this.pollDelay = properties.getLongProperty(pollDelayProperty);

        String subSchedulerAdaptor;
        // String subFileSystemAdaptor;
        String subLocation;
        Map<String, String> subSchedulerProperties;

        if (ScriptingUtils.isLocal(location)) {
            subSchedulerAdaptor = "local";
            // subFileSystemAdaptor = "file";

            if (location.startsWith("local://")) {
                subLocation = location.substring("local://".length());
            } else {
                subLocation = "";
            }
            subSchedulerProperties = properties.filter(LocalSchedulerAdaptor.PREFIX).toMap();
        } else if (ScriptingUtils.isSSH(location)) {
            subSchedulerAdaptor = "ssh";
            // subFileSystemAdaptor = "sftp";
            subLocation = location.substring("ssh://".length());
            subSchedulerProperties = properties.filter(SshSchedulerAdaptor.PREFIX).toMap();

            // since we expect commands to be done almost instantaneously, we
            // poll quite frequently (local operation anyway)
            subSchedulerProperties.put(SshSchedulerAdaptor.POLLING_DELAY, "100");
        } else {
            throw new InvalidLocationException(getAdaptorName(), "Invalid location: " + location);
        }

        LOGGER.debug("creating sub scheduler for {} adaptor at {}://{}", adaptor, subSchedulerAdaptor, subLocation);

        subScheduler = Scheduler.create(subSchedulerAdaptor, subLocation, credential, subSchedulerProperties);

        // LOGGER.debug("creating file system for {} adaptor at {}://{}", adaptor, subFileSystemAdaptor, subLocation);
        subFileSystem = subScheduler.getFileSystem();

        // FileSystem.create(subFileSystemAdaptor, subLocation, credential, null);
    }

    protected Path getWorkingDirectory() {
        return subFileSystem.getWorkingDirectory();
    }

    protected QueueStatus[] getQueueStatuses(Map<String, Map<String, String>> all, String... queueNames) {

        QueueStatus[] result = new QueueStatus[queueNames.length];

        for (int i = 0; i < queueNames.length; i++) {
            if (queueNames[i] == null) {
                result[i] = null;
            } else {
                // state for only the requested queuee
                Map<String, String> map = all.get(queueNames[i]);

                if (map == null) {
                    XenonException exception = new NoSuchQueueException(getAdaptorName(),
                            "Cannot get status of queue \"" + queueNames[i] + "\" from server, perhaps it does not exist?");
                    result[i] = new QueueStatusImplementation(this, queueNames[i], exception, null);
                } else {
                    result[i] = new QueueStatusImplementation(this, queueNames[i], null, map);
                }
            }
        }

        return result;
    }

    /**
     * Run a command on the remote scheduler machine.
     *
     * @param stdin
     *            the text to write to the input of the executable.
     * @param executable
     *            the executable to run
     * @param arguments
     *            the arguments to the executable
     * @return a {@link RemoteCommandRunner} that can be used to monitor the running command
     * @throws XenonException
     *             if an error occurs
     */
    public RemoteCommandRunner runCommand(String stdin, String executable, String... arguments) throws XenonException {
        return new RemoteCommandRunner(subScheduler, stdin, executable, arguments);
    }

    // Subclasses can override this method to produce more specified exceptions
    protected void translateError(RemoteCommandRunner runner, String stdin, String executable, String... arguments) throws XenonException {
        throw new XenonException(getAdaptorName(),
                "could not run command \"" + executable + "\" with stdin \"" + stdin + "\" arguments \"" + Arrays.toString(arguments) + "\" using scheduler \""
                        + subScheduler.getAdaptorName() + "\". Exit code = " + runner.getExitCode() + " Output: " + runner.getStdout() + " Error output: "
                        + runner.getStderr());
    }

    /**
     * Run a command until completion. Throw an exception if the command returns a non-zero exit code, or prints to stderr.
     *
     * @param stdin
     *            the text to write to the input of the executable.
     * @param executable
     *            the executable to run
     * @param arguments
     *            the arguments to the executable
     * @return the text produced by the executable on the stdout stream.
     * @throws XenonException
     *             if an error occurred
     */
    public String runCheckedCommand(String stdin, String executable, String... arguments) throws XenonException {
        RemoteCommandRunner runner = new RemoteCommandRunner(subScheduler, stdin, executable, arguments);

        if (!runner.success()) {
            translateError(runner, stdin, executable, arguments);
        }

        return runner.getStdout();
    }

    /**
     * Start an interactive command on the remote machine (usually via ssh).
     *
     * @param executable
     *            the executable to start
     * @param arguments
     *            the arguments to pass to the executable
     * @return the job identifier that represents the interactive command
     * @throws XenonException
     *             if an error occurred
     */
    public Streams startInteractiveCommand(String executable, String... arguments) throws XenonException {
        JobDescription description = new JobDescription();
        description.setQueueName("unlimited");
        description.setExecutable(executable);
        description.setArguments(arguments);

        return subScheduler.submitInteractiveJob(description);
    }

    /**
     * Checks if the queue names given are valid, and throw an exception otherwise. Checks against the list of queues when the scheduler was created.
     *
     * @param givenQueueNames
     *            the queue names to check for validity
     * @throws NoSuchQueueException
     *             if one or more of the queue names is not known in the scheduler
     */
    protected void checkQueueNames(String[] givenQueueNames) throws XenonException {

        // create a hash set with all given queues
        HashSet<String> invalidQueues = new HashSet<>(Arrays.asList(givenQueueNames));

        // remove all valid queues from the set
        invalidQueues.removeAll(Arrays.asList(getQueueNames()));

        // if anything remains, these are invalid. throw an exception with the
        // invalid queues
        if (!invalidQueues.isEmpty()) {
            throw new NoSuchQueueException(getAdaptorName(),
                    "Invalid queues given: " + Arrays.toString(invalidQueues.toArray(new String[invalidQueues.size()])));
        }
    }

    protected boolean sleep(long pollDelay) {
        try {
            Thread.sleep(pollDelay);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        }
    }

    /**
     * Wait until a Job is done, or until the give timeout expires (whichever comes first).
     *
     * A timeout of 0 will result in an infinite timeout, a negative timeout will result in an exception.
     *
     * @param jobIdentifier
     *            the Job to wait for
     * @param timeout
     *            the maximum number of milliseconds to wait, 0 to wait forever, or negative to return immediately.
     * @return the status of the job
     * @throws IllegalArgumentException
     *             if the value to timeout is negative
     * @throws XenonException
     *             if an error occurs
     */
    @Override
    public JobStatus waitUntilDone(String jobIdentifier, long timeout) throws XenonException {

        assertNonNullOrEmpty(jobIdentifier, "Job identifier cannot be null or empty");

        long deadline = Deadline.getDeadline(timeout);

        JobStatus status = getJobStatus(jobIdentifier);

        // wait until we are done, or the timeout expires
        while (!status.isDone() && System.currentTimeMillis() < deadline) {

            if (sleep(pollDelay)) {
                return status;
            }

            status = getJobStatus(jobIdentifier);
        }

        return status;
    }

    /**
     * Wait until a Job is running (or already done), or until the given timeout expires, whichever comes first.
     *
     * A timeout of 0 will result in an infinite timeout. A negative timeout will result in an exception.
     *
     * @param jobIdentifier
     *            the Job to wait for
     * @param timeout
     *            the maximum number of milliseconds to wait, 0 to wait forever, or negative to return immediately.
     * @return the status of the job
     * @throws IllegalArgumentException
     *             if the value of timeout was negative
     * @throws XenonException
     *             if an error occurs
     */
    @Override
    public JobStatus waitUntilRunning(String jobIdentifier, long timeout) throws XenonException {

        assertNonNullOrEmpty(jobIdentifier, "Job identifier cannot be null or empty");

        long deadline = Deadline.getDeadline(timeout);

        JobStatus status = getJobStatus(jobIdentifier);

        // wait until we are done, or the timeout expires
        while (!(status.isRunning() || status.isDone()) && System.currentTimeMillis() < deadline) {

            if (sleep(pollDelay)) {
                return status;
            }

            status = getJobStatus(jobIdentifier);
        }

        return status;
    }

    /**
     * Check if the given <code>queueName</code> is presents in <code>queueNames</code>.
     *
     * If <code>queueName</code> is <code>null</code> or <code>queueName</code> is present in <code>queueNames</code> this method will return. Otherwise it will
     * throw a <code>NoSuchQueueException</code>.
     *
     * @param queueNames
     *            the valid queue names.
     * @param queueName
     *            the queueName to check.
     * @throws NoSuchQueueException
     *             if workingDirectory does not exist, or an error occurred.
     */

    protected void checkQueue(String[] queueNames, String queueName) throws NoSuchQueueException {
        if (queueName == null) {
            return;
        }

        for (String q : queueNames) {
            if (queueName.equals(q)) {
                return;
            }
        }

        throw new NoSuchQueueException(ADAPTOR_NAME, "Queue does not exist: " + queueName);
    }

    /**
     * Check if the given working directory exists. Useful for schedulers that do not check this (like Slurm)
     *
     * @param workingDirectory
     *            the working directory (either absolute or relative) as given by the user.
     * @throws XenonException
     *             if workingDirectory does not exist, or an error occurred.
     */
    protected void checkWorkingDirectory(String workingDirectory) throws XenonException {
        if (workingDirectory == null) {
            return;
        }

        Path path;

        if (workingDirectory.startsWith("/")) {
            path = new Path(workingDirectory);
        } else {
            // make relative path absolute
            path = getWorkingDirectory().resolve(workingDirectory);
        }
        if (!subFileSystem.exists(path)) {
            throw new InvalidJobDescriptionException(getAdaptorName(), "Working directory does not exist: " + path);
        }
    }

    @Override
    public boolean isOpen() throws XenonException {
        return subScheduler.isOpen() && subFileSystem.isOpen();
    }

    @Override
    public void close() throws XenonException {
        subScheduler.close();
        subFileSystem.close();
    }

    public FileSystem getFileSystem() throws XenonException {
        return subFileSystem;
    }
}
