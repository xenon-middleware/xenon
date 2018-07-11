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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.UnsupportedOperationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.adaptors.schedulers.JobSeenMap;
import nl.esciencecenter.xenon.adaptors.schedulers.QueueStatusImplementation;
import nl.esciencecenter.xenon.adaptors.schedulers.RemoteCommandRunner;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingParser;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingScheduler;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;
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

        if (queueName == null || queueName.isEmpty()) {
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

        RemoteCommandRunner runner = runCommand(null, "atq", new String[0]);

        if (runner.success()) {
            return AtUtils.parseJobInfo(runner.getStdout(), queues);
        } else {
            throw new XenonException(getAdaptorName(), "Failed to get queue status using atq: (" + runner.getExitCode() + ") " + runner.getStderr());
        }
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

            try {
                checkQueue(q);
                result[index++] = new QueueStatusImplementation(this, q, null, null);
            } catch (XenonException e) {
                result[index++] = new QueueStatusImplementation(this, q, e, null);
            }
        }

        return result;
    }

    @Override
    public String submitBatchJob(JobDescription description) throws XenonException {

        // Verify the job description
        AtUtils.verifyJobDescription(description, QNAMES);

        // Generate a job script.
        String script = AtUtils.generateJobScript(description, getWorkingDirectory());

        // Submit it.
        RemoteCommandRunner runner = runCommand(script, "at", new String[] { description.getStartTime() });

        // Retrieve the job ID from the output and return it.
        if (runner.success()) {
            return ScriptingParser.parseJobIDFromLine(runner.getStderr(), ADAPTOR_NAME, "job ");
        } else {
            throw new XenonException(getAdaptorName(), "Failed to submit job using at: (" + runner.getExitCode() + ") " + runner.getStderr());
        }
    }

    @Override
    public Streams submitInteractiveJob(JobDescription description) throws XenonException {
        throw new UnsupportedOperationException(getAdaptorName(), "Interactive jobs not supported");
    }

    @Override
    public JobStatus getJobStatus(String jobIdentifier) throws XenonException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JobStatus cancelJob(String jobIdentifier) throws XenonException {
        // TODO Auto-generated method stub
        return null;
    }

}
