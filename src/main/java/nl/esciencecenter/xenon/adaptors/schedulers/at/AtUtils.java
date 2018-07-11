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

import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingUtils;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.schedulers.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.JobDescription;

public class AtUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtUtils.class);

    public static final String JOB_OPTION_JOB_SCRIPT = "job.script";

    private static final String[] VALID_JOB_OPTIONS = new String[] { JOB_OPTION_JOB_SCRIPT };

    public AtUtils() {
        // utility class
    }

    public static void parseJobLine(String line, Set<String> queues, HashMap<String, Map<String, String>> result) {

        // Check if there is anything to parse
        if (line == null || line.trim().isEmpty()) {
            return;
        }

        String[] fields = line.trim().split("\\s+");

        // Check if we got the right number of fields
        if (fields.length != 8) {
            throw new IllegalArgumentException("Failed to parse AT job line: " + line);
        }

        // Check if we are interested in this queue. If so, add the result
        if (queues == null || queues.isEmpty() || queues.contains(fields[6])) {
            HashMap<String, String> tmp = new HashMap<>();
            tmp.put("jobID", fields[0]);
            tmp.put("startDate", fields[1] + " " + fields[2] + " " + fields[3] + " " + fields[4] + " " + fields[5]);
            tmp.put("queue", fields[6]);
            tmp.put("user", fields[7]);
            result.put(fields[0], tmp);
        }
    }

    /**
     * Parse one or more lines of queue info as produced by <code>atq</code>.
     *
     * These lines have the following syntax:
     *
     * [jobID] [weekday] [month] [dayOfMonth] [time] [year] [queue] [user]
     *
     * For example:
     *
     * 11 Mon Jul 2 10:22:00 2018 a jason
     *
     * The parsed output will be split into "jobID", "startDate", "queue" and "user" data, which is combined in a Map (each using the respective keys). Each of
     * this Maps is stored is a larger Map using the "jobID" as a key. This larger map is returned as the return value this method.
     *
     * For example, the example line above will result in the following return value: <code>
     * Map("11":Map("jobID":"11", "startDate":"Mon Jul 2 10:22:00 2018", "queue":"a", "user":"jason"))
     * </code> If a set of queue names is provided in <code>queues</code>, only jobs from a matching queue will be returned in results. If <code>queues<code> is
     * <code>null</code> or empty, all jobs from all queues will be returned.
     *
     * @param atqOutput
     *            the output as produced by atq
     * @param queues
     *            the queues to return the jobs for.
     * @return the parsed output
     */
    public static HashMap<String, Map<String, String>> parseJobInfo(String atqOutput, Set<String> queues) {

        HashMap<String, Map<String, String>> result = new HashMap<>();

        if (atqOutput == null || atqOutput.isEmpty()) {
            return result;
        }
        String[] lines = atqOutput.split("\\r?\\n");

        for (String line : lines) {
            parseJobLine(line, queues, result);
        }

        return result;
    }

    public static String[] getJobIDs(Map<String, Map<String, String>> jobs) {

        if (jobs == null || jobs.isEmpty()) {
            return new String[0];
        }

        Set<String> ids = jobs.keySet();
        return ids.toArray(new String[ids.size()]);
    }

    public static void verifyJobDescription(JobDescription description, String[] queueNames) throws XenonException {

        ScriptingUtils.verifyJobOptions(description.getJobOptions(), VALID_JOB_OPTIONS, ADAPTOR_NAME);

        // check for option that overrides job script completely.
        // if (description.getJobOptions().get(JOB_OPTION_JOB_SCRIPT) != null) {
        // no other settings checked.
        // return;
        // }

        // Perform standard checks.
        ScriptingUtils.verifyJobDescription(description, queueNames, ADAPTOR_NAME);

        // Perform at specific checks
        int nodeCount = description.getNodeCount();

        if (nodeCount > 1) {
            throw new InvalidJobDescriptionException(ADAPTOR_NAME, "Unsupported node count: " + nodeCount);
        }

        int processesPerNode = description.getProcessesPerNode();

        if (processesPerNode > 1) {
            throw new InvalidJobDescriptionException(ADAPTOR_NAME, "Unsupported processes per node count: " + processesPerNode);
        }

        int maxTime = description.getMaxRuntime();

        if (maxTime != 0) {
            throw new InvalidJobDescriptionException(ADAPTOR_NAME, "Unsupported maximum runtime: " + maxTime);
        }
    }

    private static String getStream(String target) {

        if (target == null) {
            return "/dev/null";
        }

        return target;
    }

    public static String generateJobScript(JobDescription description, Path fsEntryPath) {
        StringBuilder stringBuilder = new StringBuilder();
        Formatter script = new Formatter(stringBuilder, Locale.US);

        // script.format("%s\n", "#!/bin/sh");

        String name = description.getName();

        if (name == null || name.trim().isEmpty()) {
            name = "xenon";
        }

        // set name of job to xenon
        script.format("#AT_JOBNAME %s\n", name);

        // set working directory
        String workingDir = ScriptingUtils.getWorkingDirPath(description, fsEntryPath);
        script.format("#AT_WORKDIR %s\n", workingDir);

        if (description.getQueueName() != null) {
            script.format("#AT_QUEUE %s\n", description.getQueueName());
        }

        if (description.getStartTime() != null) {
            script.format("#AT_STARTTIME %s\n", description.getStartTime());
        }

        String stdin = getStream(description.getStdin());
        String stderr = getStream(description.getStderr());
        String stdout = getStream(description.getStdout());

        script.format("#AT_INPUT '%s'\n", stdin);
        script.format("#AT_OUTPUT '%s'\n", stdout);
        script.format("#AT_ERROR '%s'\n", stderr);

        if (!description.getEnvironment().isEmpty()) {
            script.format("\n");

            for (Map.Entry<String, String> entry : description.getEnvironment().entrySet()) {
                script.format("export %s=\"%s\"\n", entry.getKey(), entry.getValue());
            }
        }

        script.format("\n");

        script.format("cd '%s' && ", workingDir);
        script.format("%s", description.getExecutable());

        for (String argument : description.getArguments()) {
            script.format(" %s", ScriptingUtils.protectAgainstShellMetas(argument));
        }

        script.format(" < '%s' > '%s' 2> '%s'\n", stdin, stdout, stderr);
        script.close();

        LOGGER.debug("Created job script:%n{} from description {}", stringBuilder, description);

        return stringBuilder.toString();
    }

}
