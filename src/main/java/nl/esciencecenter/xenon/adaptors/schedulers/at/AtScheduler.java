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
package nl.esciencecenter.xenon.adaptors.schedulers.at;

import static nl.esciencecenter.xenon.adaptors.schedulers.at.AtSchedulerAdaptor.ADAPTOR_NAME;
import static nl.esciencecenter.xenon.adaptors.schedulers.at.AtSchedulerAdaptor.POLL_DELAY_PROPERTY;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.UnsupportedOperationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.adaptors.schedulers.JobSeenMap;
import nl.esciencecenter.xenon.adaptors.schedulers.JobStatusImplementation;
import nl.esciencecenter.xenon.adaptors.schedulers.QueueStatusImplementation;
import nl.esciencecenter.xenon.adaptors.schedulers.RemoteCommandRunner;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingScheduler;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingUtils;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;
import nl.esciencecenter.xenon.schedulers.NoSuchJobException;
import nl.esciencecenter.xenon.schedulers.NoSuchQueueException;
import nl.esciencecenter.xenon.schedulers.QueueStatus;
import nl.esciencecenter.xenon.schedulers.Streams;

public class AtScheduler extends ScriptingScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtScheduler.class);

    // At queues are PER USER. Queue names are:
    //
    // a...z, higher queues have a higher niceness. Default is "a"
    // A...Z, higher queues have a higher niceness, and the "batch" rules apply, so jobs a only started if the machine load is below "1.5"
    // Once a job starts is disappears from the queue
    // a...z queues start jobs on their given time, regardless of the number running
    // "=" is the queue containing the running jobs
    // atq list all queues of this user
    // atq -q <name> list only the given queue
    // at -c <number> print the job content on stdout
    // submitting requires a time and optional queue
    //
    // submitting:
    // echo "/bin/sleep 60" | at -t 07021022
    //
    // prints output:
    //
    // warning: commands will be executed using /bin/sh
    // job 11 at Mon Jul 2 10:22:00 2018
    //
    // atq then prints:
    //
    // 11 Mon Jul 2 10:22:00 2018 = jason
    //
    // More complex timespecs are also supported:
    //
    // echo "/bin/sleep 60" | at now
    // echo "/bin/sleep 60" | at 5pm
    // echo "/bin/sleep 60" | at 5pm tomorrow
    // echo "/bin/sleep 60" | at now + 1 minute
    // echo "/bin/sleep 60" | at teatime
    //
    // When specifying something like "5pm" or "teatime", the job will be scheduled for the next occurrence of that moment of the day (which may be tomorrow).
    // Providing a detailed time in the past starts the job ASAP. For example "-t 07021022" for "July 2nd 10:22".
    //
    // we can limit the time used by a process with the /usr/bin/timeout tool like this:
    //
    // timeout [--preserve-status] 15m command
    //

    private static final String[] QNAMES = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x",
            "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "T", "U", "V", "W", "X", "Y", "Z", "=" };

    private static final String DEFAULT_QUEUE = "a";

    private final JobSeenMap jobSeenMap;

    private final String uniqueIDBase = UUID.randomUUID().toString() + ".";

    private long nextUniqueID = 0;

    public AtScheduler(String uniqueID, String location, Credential credential, XenonPropertyDescription[] validProperties, Map<String, String> prop)
            throws XenonException {

        super(uniqueID, ADAPTOR_NAME, location, credential, prop, validProperties, POLL_DELAY_PROPERTY);
        // TODO Auto-generated constructor stub

        jobSeenMap = new JobSeenMap(0);
    }

    /**
     * Checks if the provided queue name is valid, and throws an exception otherwise.
     *
     * Checks against a predefined list of queue names that at uses: a-z, A-Z, and =.
     *
     * @param queueName
     *            the queue name to check for validity
     * @throws NoSuchQueueException
     *             if the queue name is not valid
     */
    private void checkQueue(String queueName) throws XenonException {

        if (queueName == null) {
            throw new IllegalArgumentException("Queue name may not be null");
        }

        if (queueName.isEmpty()) {
            throw new NoSuchQueueException(getAdaptorName(), "No queue provided");
        }

        if (queueName.length() == 1) {
            char c = queueName.charAt(0);

            if (c == '=' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                return;
            }
        }
        throw new NoSuchQueueException(getAdaptorName(), "Queue " + queueName + " does not exist");
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
        for (String q : givenQueueNames) {
            checkQueue(q);
        }
    }

    @Override
    public String[] getQueueNames() throws XenonException {
        return QNAMES.clone();
    }

    @Override
    public String getDefaultQueueName() throws XenonException {
        return DEFAULT_QUEUE;
    }

    private Map<String, Map<String, String>> getJobInfo(String[] queueNames) throws XenonException {

        HashSet<String> queues = null;

        if (queueNames != null && queueNames.length > 0) {
            checkQueueNames(queueNames);
            queues = new HashSet<>(Arrays.asList(queueNames));
        }

        String script = AtUtils.generateListingScript();

        RemoteCommandRunner runner = runCommand(script, "/bin/bash");

        if (!runner.successIgnoreError()) {
            throw new XenonException(getAdaptorName(),
                    "Failed to retrieve job info: (" + runner.getExitCode() + ") " + runner.getStderr());
        }

        return AtUtils.parseFileDumpJobInfo(runner.getStdout(), queues);

        /*
         * RemoteCommandRunner runner = runCommand(null, "atq", new String[0]);
         *
         * if (runner.success()) { return AtUtils.parseATQJobInfo(runner.getStdout(), queues); } else { throw new XenonException(getAdaptorName(),
         * "Failed to get queue status using atq: (" + runner.getExitCode() + ") " + runner.getStderr()); }
         *
         */

    }

    @Override
    public String[] getJobs(String... queueNames) throws XenonException {
        return AtUtils.getJobIDs(getJobInfo(queueNames));
    }

    @Override
    public QueueStatus getQueueStatus(String queueName) throws XenonException {
        checkQueue(queueName);
        return new QueueStatusImplementation(this, queueName, null, null);
    }

    @Override
    public QueueStatus[] getQueueStatuses(String... queueNames) throws XenonException {

        String[] targetQueueNames;

        if (queueNames == null) {
            throw new IllegalArgumentException("list of queue names cannot be null");
        } else if (queueNames.length == 0) {
            targetQueueNames = getQueueNames();
        } else {
            targetQueueNames = queueNames;
        }

        QueueStatus[] result = new QueueStatus[targetQueueNames.length];

        int index = 0;

        for (String q : targetQueueNames) {

            if (q == null) {
                result[index++] = null;
            } else {
                try {
                    checkQueue(q);
                    result[index++] = new QueueStatusImplementation(this, q, null, null);
                } catch (XenonException e) {
                    result[index++] = new QueueStatusImplementation(this, q, e, null);
                }
            }
        }

        return result;
    }

    private synchronized String getUniqueID() {
        return uniqueIDBase + Long.toString(nextUniqueID++);
    }

    @Override
    public String submitBatchJob(JobDescription description) throws XenonException {

        // Verify the job description
        AtUtils.verifyJobDescription(description, QNAMES);

        String uniqueID = getUniqueID();

        String workingDir = ScriptingUtils.getWorkingDirPath(description, getWorkingDirectory());

        // Generate a script to store the essential info about the job and run it wherever we will run the job
        String script = AtUtils.generateJobInfoScript(description, workingDir, uniqueID);
        RemoteCommandRunner runner = runCommand(script, "/bin/bash");

        // Check if we where succesfull
        if (!runner.successIgnoreError()) {
            throw new XenonException(getAdaptorName(),
                    "Failed to submit job using at while storing job info: (" + runner.getExitCode() + ") " + runner.getStderr());
        }

        // Generate script which will start the job and submit this to 'at'
        script = AtUtils.generateJobScript(description, workingDir, uniqueID);
        runner = runCommand(script, "at", new String[] { description.getStartTime() });

        // Retrieve the job ID from the output and return it.
        if (!runner.successIgnoreError()) {
            // Try to store the error in the job info file
            int exit = runner.getExitCode();
            String out = runner.getStderr();
            String err = runner.getStderr();

            script = AtUtils.generateJobErrorScript(uniqueID, exit, out, err);
            runner = runCommand(script, "/bin/bash");

            // NOTE: we intentionally ignore the status of running the job error script.
            throw new XenonException(getAdaptorName(), "Failed to submit job using at: (" + exit + ") " + err);
        }

        String jobID = AtUtils.parseSubmitOutput(runner.getStderr());
        jobSeenMap.updateRecentlySeen(jobID);

        // Write a file to store the relation between the xenon uniqueID and at job ID. This allows us to retrieve the status and statistics about the
        // job even after it has finished and 'at' has forgotten about it.

        // Retrieve script.
        script = AtUtils.generateJobIDScript(jobID, uniqueID);
        runner = runCommand(script, "/bin/bash");

        if (!runner.successIgnoreError()) {
            LOGGER.warn("Failed to store job mapping for AT job " + jobID + " / " + uniqueID + ": (" + runner.getExitCode() + ") " + runner.getStderr());
        }

        return jobID;
    }

    @Override
    public Streams submitInteractiveJob(JobDescription description) throws XenonException {
        throw new UnsupportedOperationException(getAdaptorName(), "Interactive jobs not supported");
    }

    @Override
    public JobStatus getJobStatus(String jobIdentifier) throws XenonException {

        assertNonNullOrEmpty(jobIdentifier, "Job identifier cannot be null or empty");

        Map<String, Map<String, String>> info = getJobInfo(null);

        Map<String, String> tmp = info.get(jobIdentifier);

        if (tmp == null) {
            throw new NoSuchJobException(ADAPTOR_NAME, "Job " + jobIdentifier + " could not be found!");
        }

        String state = "UNKNOWN";
        int exit = 0;

        if (tmp.containsKey("xenon.DONE")) {
            state = "DONE";

            if (tmp.containsKey("xenon.EXIT")) {
                exit = Integer.parseInt(tmp.get("xenon.EXIT"));
            }
        } else if (tmp.containsKey("xenon.RUNNING")) {
            state = "RUNNING";
        } else if (tmp.containsKey("xenon.STARTING")) {
            state = "STARTING";
        } else if (tmp.containsKey("xenon.SUBMITTED")) {
            state = "WAITING";
        }

        String name = "unknown";

        if (tmp.containsKey("xenon.JOBNAME")) {
            name = tmp.get("xenon.JOBNAME");
        }

        return new JobStatusImplementation(jobIdentifier, name, state, exit, null, state.equals("RUNNING"), state.equals("DONE"), tmp);

        /*
         * String state = "UNKNOWN";
         *
         * if (tmp != null) { String queue = tmp.get("queue");
         *
         * if (queue != null) { if (queue.equals("=")) { state = "RUNNING"; } else { state = "PENDING"; } } } else if
         * (jobSeenMap.haveRecentlySeen(jobIdentifier)) { state = "DONE";
         *
         * // TODO: scan target to find the output of the job? } else { throw new NoSuchJobException(ADAPTOR_NAME, "Job " + jobIdentifier +
         * " could not be found!"); }
         *
         * return new JobStatusImplementation(jobIdentifier, "unknown", state, 0, null, state.equals("RUNNING"), state.equals("DONE"), tmp);
         */
    }

    @Override
    public JobStatus cancelJob(String jobIdentifier) throws XenonException {

        JobStatus s = getJobStatus(jobIdentifier);

        if (s.isDone()) {
            return s;
        }

        // Submit the cancel.
        RemoteCommandRunner runner = runCommand(null, "atrm", new String[] { jobIdentifier });

        // Check the error code to see if the job was found. Note that some info may be printed on stderr if the job was running.
        if (!runner.successIgnoreError()) {
            throw new NoSuchJobException(ADAPTOR_NAME, "Job " + jobIdentifier + " could not be found!");
        }

        // The job should have been cancelled now.
        jobSeenMap.addDeletedJob(jobIdentifier);

        return new JobStatusImplementation(jobIdentifier, "unknown", "CANCELLED", 0, null, false, true, s.getSchedulerSpecificInformation());
    }

    @Override
    public int getDefaultRuntime() {
        return 0;
    }
}
