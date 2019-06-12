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

import static nl.esciencecenter.xenon.adaptors.schedulers.ScriptingParser.NEWLINE_REGEX;
import static nl.esciencecenter.xenon.adaptors.schedulers.at.AtSchedulerAdaptor.ADAPTOR_NAME;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingParser;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingUtils;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.schedulers.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.JobDescription;

public class AtUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtUtils.class);

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

            // Note: some installations of at seem to show jobs both in the queue they where submitted to (ie. "a"), and the running queue "=", while others
            // remove the jobs from the submission queue at soon as they start running. If we see both, we prefer the running queue.

            Map<String, String> tmp = result.get(fields[0]);

            if (tmp == null) {
                tmp = new HashMap<>();
                tmp.put("jobID", fields[0]);
                tmp.put("startDate", fields[1] + " " + fields[2] + " " + fields[3] + " " + fields[4] + " " + fields[5]);
                tmp.put("queue", fields[6]);
                tmp.put("user", fields[7]);
                result.put(fields[0], tmp);
            } else {
                // Job seen twice, so check the current queue. If the this queue is running "=" we overwrite the job info.
                if (fields[6].equals("=")) {
                    tmp.put("queue", fields[6]);
                }
            }
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
     * </code> If a set of queue names is provided in <code>queues</code>, only jobs from a matching queue will be returned in results. If <code>queues</code>
     * is <code>null</code> or empty, all jobs from all queues will be returned.
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

    public static String parseSubmitOutput(String output) throws XenonException {

        String[] lines = NEWLINE_REGEX.split(output);

        if (lines == null || lines.length != 2) {
            throw new XenonException(ADAPTOR_NAME, "Failed to get jobID from output: \"" + output + "\"");
        }

        return ScriptingParser.parseJobIDFromLine(lines[1], ADAPTOR_NAME, "job ");
    }

    public static String[] getJobIDs(Map<String, Map<String, String>> jobs) {

        if (jobs == null || jobs.isEmpty()) {
            return new String[0];
        }

        Set<String> ids = jobs.keySet();
        return ids.toArray(new String[ids.size()]);
    }

    public static void verifyJobDescription(JobDescription description, String[] queueNames) throws XenonException {
        // check for option that overrides job script completely.
        // if (description.getJobOptions().get(JOB_OPTION_JOB_SCRIPT) != null) {
        // no other settings checked.
        // return;
        // }

        // Perform standard checks.
        ScriptingUtils.verifyJobDescription(description, queueNames, ADAPTOR_NAME);

        // Perform at specific checks
        int tasks = description.getTasks();

        if (tasks > 1) {
            throw new InvalidJobDescriptionException(ADAPTOR_NAME, "Unsupported task count: " + tasks);
        }

        int tasksPerNode = description.getTasksPerNode();

        if (tasksPerNode > 1) {
            throw new InvalidJobDescriptionException(ADAPTOR_NAME, "Unsupported task per node count: " + tasksPerNode);
        }

        int maxTime = description.getMaxRuntime();

        if (maxTime > 0) {
            throw new InvalidJobDescriptionException(ADAPTOR_NAME, "Unsupported maximum runtime: " + maxTime);
        }
    }

    private static String getStream(String target) {

        if (target == null) {
            return "/dev/null";
        }

        return target;
    }

    private static void echo(Formatter script, String label, String message, String file) {
        script.format("echo \"#AT_%s %s\" >> %s\n", label, message, file);
    }

    protected static String substituteJobID(String path, String jobID) {

        if (path == null) {
            return null;
        }

        return path.replace("%j", jobID);
    }

    public static String generateJobScript(JobDescription description, Path fsEntryPath, String tmpID) {
        StringBuilder stringBuilder = new StringBuilder();
        Formatter script = new Formatter(stringBuilder, Locale.US);

        // script.format("%s\n", "#!/bin/sh");
        String name = description.getName();

        if (name == null || name.trim().isEmpty()) {
            name = "xenon";
        }

        String workingDir = ScriptingUtils.getWorkingDirPath(description, fsEntryPath);

        String tmpFile = "/tmp/xenon.at." + tmpID;

        // Save some info on the job to the tmpFile so we can reconstruct it later.

        // set name of job to xenon
        echo(script, "JOBNAME", name, tmpFile);
        echo(script, "WORKDIR", workingDir, tmpFile);

        if (description.getQueueName() != null) {
            echo(script, "QUEUE", description.getQueueName(), tmpFile);
        }

        if (description.getStartTime() != null) {
            echo(script, "STARTTIME", description.getStartTime(), tmpFile);
        }

        String stdin = getStream(description.getStdin());
        String stderr = getStream(substituteJobID(description.getStderr(), tmpID));
        String stdout = getStream(substituteJobID(description.getStdout(), tmpID));

        echo(script, "INPUT", stdin, tmpFile);
        echo(script, "OUTPUT", stdout, tmpFile);
        echo(script, "ERROR", stderr, tmpFile);

        if (!description.getEnvironment().isEmpty()) {
            for (Map.Entry<String, String> entry : description.getEnvironment().entrySet()) {
                echo(script, "ENV", entry.getKey() + "=" + entry.getValue(), tmpFile);
            }
        }

        echo(script, "EXEC", description.getExecutable(), tmpFile);

        for (String argument : description.getArguments()) {
            echo(script, "EXEC_PARAM", ScriptingUtils.protectAgainstShellMetas(argument), tmpFile);
        }

        if (!description.getEnvironment().isEmpty()) {
            for (Map.Entry<String, String> entry : description.getEnvironment().entrySet()) {
                script.format("export %s=\"%s\"\n", entry.getKey(), entry.getValue());
            }
        }

        script.format("cd '%s' && ", workingDir);
        script.format("%s", description.getExecutable());

        for (String argument : description.getArguments()) {
            script.format(" %s", ScriptingUtils.protectAgainstShellMetas(argument));
        }

        script.format(" < '%s' > '%s' 2> '%s' &\n", stdin, stdout, stderr);

        script.format("PID=$!\n");

        echo(script, "PID", "$PID", tmpFile);
        script.format("wait $PID\n");

        script.format("EXIT_CODE=$?\n");

        echo(script, "EXIT", "$EXIT_CODE", tmpFile);
        script.close();

        LOGGER.debug("Created job script:{} from description {}", stringBuilder, description);

        return stringBuilder.toString();
    }

}
