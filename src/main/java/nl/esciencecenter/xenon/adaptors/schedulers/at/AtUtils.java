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
import nl.esciencecenter.xenon.schedulers.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.JobDescription;

public class AtUtils {

    private static final String FORMAT_VERSION = "1.0.0";

    private static final String INFO_FILE_NAME = "/tmp/xenon.at.info.";
    private static final String STATS_FILE_NAME = "/tmp/xenon.at.stats.";
    private static final String JOBID_FILE_NAME = "/tmp/xenon.at.jobid.";

    private static final String TIMESTAMP_COMMAND = "`/usr/bin/date --iso-8601=sec`";

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
    public static HashMap<String, Map<String, String>> parseATQJobInfo(String atqOutput, Set<String> queues) {

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

    /**
     * Parse one or more records of job info as produced by the <code>generateListingScript<code> script.
     *
     * These lines have the following syntax:
     *
     * AT JOB LIST AT JOB: /tmp/xenon.at.jobid.{atID} XENON_JOB_ID: {xenonID} XENON_INFO_FILE: /tmp/xenon.at.info.{xenonID} [content of
     * /tmp/xenon.at.info.{xenonID}] END_XENON_INFO_FILE: /tmp/xenon.at.info.{xenonID} XENON_STATS_FILE: /tmp/xenon.at.stats.{xenonID} [content of
     * /tmp/xenon.at.stats.{xenonID}] END_XENON_STATS_FILE: /tmp/xenon.at.stats.{xenonID}
     *
     * The parsed output will be split into "jobID", "jobStatus", "JobName", "exitCode" and "Misc" data, which is combined in a Map (each using the respective
     * keys). Each of this Maps is stored is a larger Map using the "jobID" as a key. This larger map is returned as the return value this method.
     *
     * @param output
     *            the output as produced by atq
     * @param queues
     *            the queues to return the jobs for.
     * @return the parsed output
     */
    public static HashMap<String, Map<String, String>> parseFileDumpJobInfo(String output, Set<String> queues) {

        HashMap<String, Map<String, String>> result = new HashMap<>();

        if (output == null || output.isEmpty()) {
            return result;
        }

        String[] lines = output.split("\\r?\\n");

        if (lines == null || lines.length == 0) {
            LOGGER.warn("Failed to parse script output.");
            return result;
        }

        if (!"AT JOB LIST".equals(lines[0])) {
            LOGGER.warn("Failed to parse script output. Unexpected header: " + lines[0]);
            return result;
        }

        int index = 1;

        while (index < lines.length) {
            HashMap<String, String> jobInfo = new HashMap<>();

            index = parseJob(lines, index, jobInfo);

            String id = jobInfo.get("at.ID");

            if (id != null) {

                if (queues != null) {
                    String q = jobInfo.get("xenon.QUEUE");

                    if (q != null && queues.contains(q)) {
                        result.put(id, jobInfo);
                    }
                } else {
                    result.put(id, jobInfo);
                }
            }
        }

        return result;
    }

    private static String parseJobID(String line) {

        // The expect line looks like this:
        //
        // AT JOB: /tmp/xenon.at.jobid.24
        //
        // We check the prefix "AT JOB: " and then extract the postfix (in this case ".24") which is the job ID number as assigned by at.
        // Whenever we run into problems, we simply print a warning and give up.

        if (line == null || !line.startsWith("AT JOB: ")) {
            LOGGER.warn("Failed to parse job. Unexpected line: " + line);
            return null;
        }

        int dot = line.lastIndexOf(".");

        if (dot == -1) {
            LOGGER.warn("Failed to parse job. JobID not found: " + line);
            return null;
        }

        String jobID = line.substring(dot + 1);

        if (jobID == null || line.isEmpty()) {
            LOGGER.warn("Failed to parse job. JobID not valid: " + line);
            return null;
        }

        return jobID;
    }

    private static String parseXenonID(String line) {

        // The expect line looks like this:
        //
        // XENON_JOB_ID: 18087e0a-1b63-444a-b0eb-aab37068426c.0
        //
        // We check the prefix "XENON_JOB_ID: " and then extract the postfix after ": ". This is the ID assigned by Xenon.
        // Whenever we run into problems, we simply print a warning and give up.

        if (line == null || !line.startsWith("XENON_JOB_ID: ")) {
            LOGGER.warn("Failed to parse job. Unexpected line: " + line);
            return null;
        }

        String xenonID = line.substring("XENON_JOB_ID: ".length());

        if (xenonID == null || line.isEmpty()) {
            LOGGER.warn("Failed to parse job. XenonID not valid: " + line);
            return null;
        }

        return xenonID;
    }

    private static int parseInfoFile(String[] lines, int index, HashMap<String, String> result) {

        // The expect input looks like this:
        //
        // XENON_INFO_FILE: /tmp/xenon.at.info.18087e0a-1b63-444a-b0eb-aab37068426c.0
        // #AT_JOBNAME xenon
        // #AT_WORKDIR /home/jason/test_at
        // #AT_STARTTIME now
        // #AT_INPUT /dev/null
        // #AT_OUTPUT /dev/null
        // #AT_ERROR /dev/null
        // #AT_EXEC /bin/sleep
        // #AT_EXEC_PARAM 'aap'
        // #AT_SUBMITTED 2021-09-15T14:03:33+02:00
        // #AT_STARTING 2021-09-15T14:03:33+02:00
        // #AT_PID 14073
        // #AT_RUNNING 2021-09-15T14:03:33+02:00
        // #AT_DONE 2021-09-15T14:03:33+02:00
        // #AT_EXIT 0
        // END_XENON_INFO_FILE: /tmp/xenon.at.info.18087e0a-1b63-444a-b0eb-aab37068426c.0
        //
        // We check if the first line starts with "XENON_INFO_FILE:" and then parse everything until we encounter "END_XENON_INFO_FILE:".
        // Whenever we run into problems, we simply print a warning and give up.

        if (index >= lines.length) {
            return index;
        }

        String line = lines[index++];

        if (line == null || !line.startsWith("START_XENON_INFO_FILE: ")) {
            LOGGER.warn("Failed to parse job info. Expected \"XENON_INFO_FILE\", not: " + line);
            return index;
        }

        result.put("xenon.info.file", line.substring("START_XENON_INFO_FILE: ".length()));

        while (index < lines.length) {

            line = lines[index++];

            if (line != null && line.startsWith("END_XENON_INFO_FILE: ")) {
                // we are done with this info block
                break;
            }

            if (line != null && line.startsWith("#AT_")) {
                // we found info about the job. Let's record it
                int split = line.indexOf(" ");

                String tag = line.substring("#AT_".length(), split);
                String value = line.substring(split + 1).trim();

                result.put("xenon." + tag, value);
            } else {
                LOGGER.warn("Failed to parse job info. Unexpected line: " + line);
            }
        }

        return index;
    }

    private static int parseStatsFile(String[] lines, int index, HashMap<String, String> result) {

        // The expect input looks like this:
        //
        // XENON_STATS_FILE: /tmp/xenon.at.stats.81f476c5-7ae7-4e86-83f9-0bd9d13066dd.0
        // Command being timed: "/bin/sleep 10"
        // User time (seconds): 0.00
        // System time (seconds): 0.00
        // Percent of CPU this job got: 0%
        // Elapsed (wall clock) time (h:mm:ss or m:ss): 0:10.00
        // Average shared text size (kbytes): 0
        // Average unshared data size (kbytes): 0
        // Average stack size (kbytes): 0
        // Average total size (kbytes): 0
        // Maximum resident set size (kbytes): 1976
        // Average resident set size (kbytes): 0
        // Major (requiring I/O) page faults: 0
        // Minor (reclaiming a frame) page faults: 73
        // Voluntary context switches: 1
        // Involuntary context switches: 1
        // Swaps: 0
        // File system inputs: 0
        // File system outputs: 0
        // Socket messages sent: 0
        // Socket messages received: 0
        // Signals delivered: 0
        // Page size (bytes): 4096
        // Exit status: 0
        // END_XENON_STATS_FILE: /tmp/xenon.at.stats.81f476c5-7ae7-4e86-83f9-0bd9d13066dd.0
        //
        // We check if the first line starts with "XENON_INFO_FILE:" and then parse everything until we encounter "END_XENON_INFO_FILE:".
        // Whenever we run into problems, we simply print a warning and give up.

        if (index >= lines.length) {
            return index;
        }

        String line = lines[index++];

        if (line == null || !line.startsWith("START_XENON_STATS_FILE: ")) {
            LOGGER.warn("Failed to parse job stats. Expected \"XENON_STATS_FILE\", not: " + line);
            return index;
        }

        result.put("xenon.stats.file", line.substring("START_XENON_STATS_FILE: ".length()));

        while (index < lines.length) {

            line = lines[index++];

            if (line != null && line.startsWith("END_XENON_STATS_FILE: ")) {
                // we are done with this info block
                break;
            }

            if (line != null && !line.startsWith("Command exited with non-zero status")) {
                // we found info about the job. Let's record it
                int split = line.indexOf(": ");

                String tag = line.substring(0, split).trim();
                String value = line.substring(split + 1).trim();

                result.put(tag, value);
            } else {
                LOGGER.warn("Failed to parse job stats. Unexpected line: " + line);
            }
        }

        return index;
    }

    private static int parseJob(String[] lines, int index, HashMap<String, String> jobInfo) {

        if (index >= lines.length) {
            return index;
        }

        // Try to retrieve the jobID;
        String jobID = parseJobID(lines[index++]);

        if (jobID == null) {
            return index;
        }

        jobInfo.put("at.ID", jobID);

        String xenonID = parseXenonID(lines[index++]);

        jobInfo.put("xenon.ID", xenonID);

        index = parseInfoFile(lines, index, jobInfo);

        index = parseStatsFile(lines, index, jobInfo);

        return index;
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

    private static String getInfoFile(String uniqueID) {
        return INFO_FILE_NAME + uniqueID;
    }

    private static String getStatsFile(String uniqueID) {
        return STATS_FILE_NAME + uniqueID;
    }

    private static String getJobIDFile(String uniqueID) {
        return JOBID_FILE_NAME + uniqueID;
    }

    public static String generateJobScript(JobDescription description, String workingDir, String tmpID) {
        StringBuilder stringBuilder = new StringBuilder();
        Formatter script = new Formatter(stringBuilder, Locale.US);

        String infoFile = getInfoFile(tmpID);
        String statsFile = getStatsFile(tmpID);

        // String workingDir = ScriptingUtils.getWorkingDirPath(description, fsEntryPath);

        String stdin = getStream(description.getStdin());
        String stderr = getStream(substituteJobID(description.getStderr(), tmpID));
        String stdout = getStream(substituteJobID(description.getStdout(), tmpID));

        if (!description.getEnvironment().isEmpty()) {
            for (Map.Entry<String, String> entry : description.getEnvironment().entrySet()) {
                script.format("export %s=\"%s\"\n", entry.getKey(), entry.getValue());
            }
        }

        // We would like to use /bin/time or /usr/bin/time to get some statistics on the process. Check if either exists
        script.format("if [ -x /bin/time ]; then\n" +
                "    TIME=\"/bin/time -v -o %s\"\n" +
                "elif [ -x /usr/bin/time ]; then\n" +
                "    TIME=\"/usr/bin/time -v -o %s\"\n" +
                "else\n" +
                "    TIME=\"\"\n" +
                "fi\n",
                statsFile, statsFile);

        echo(script, "STARTING", "`/usr/bin/date --iso-8601=sec`", infoFile);

        // TODO: it may either be /bin/time or /usr/bin/time? We should check for this!
        script.format("cd '%s' && ", workingDir);
        script.format("$TIME %s ", description.getExecutable());

        for (String argument : description.getArguments()) {
            script.format(" %s", ScriptingUtils.protectAgainstShellMetas(argument));
        }

        script.format(" < '%s' > '%s' 2> '%s' &\n", stdin, stdout, stderr);

        script.format("PID=$!\n");

        echo(script, "PID", "$PID", infoFile);
        echo(script, "RUNNING", "`/usr/bin/date --iso-8601=sec`", infoFile);

        script.format("wait $PID\n");
        script.format("EXIT_CODE=$?\n");

        echo(script, "DONE", "`/usr/bin/date --iso-8601=sec`", infoFile);
        echo(script, "EXIT", "$EXIT_CODE", infoFile);
        script.close();

        LOGGER.debug("Created job script from tmpID {} description {} and workdir {}:\n--script below this line--\n{}\n--end of script--\n", tmpID, description,
                workingDir, stringBuilder);

        return stringBuilder.toString();
    }

    public static String generateJobInfoScript(JobDescription description, String workingDir, String uniqueID) {

        StringBuilder stringBuilder = new StringBuilder();
        Formatter script = new Formatter(stringBuilder, Locale.US);

        String file = getInfoFile(uniqueID);

        echo(script, "INFO_VERSION", FORMAT_VERSION, file);

        String name = description.getName();

        if (name == null || name.isEmpty()) {
            name = "xenon";
        }

        echo(script, "JOBNAME", name, file);
        echo(script, "WORKDIR", workingDir, file);

        if (description.getQueueName() != null) {
            echo(script, "QUEUE", description.getQueueName(), file);
        }

        if (description.getStartTime() != null) {
            echo(script, "STARTTIME", description.getStartTime(), file);
        }

        String stdin = getStream(description.getStdin());
        String stderr = getStream(substituteJobID(description.getStderr(), uniqueID));
        String stdout = getStream(substituteJobID(description.getStdout(), uniqueID));

        echo(script, "INPUT", stdin, file);
        echo(script, "OUTPUT", stdout, file);
        echo(script, "ERROR", stderr, file);

        if (!description.getEnvironment().isEmpty()) {
            for (Map.Entry<String, String> entry : description.getEnvironment().entrySet()) {
                echo(script, "ENV", entry.getKey() + "=" + entry.getValue(), file);
            }
        }

        echo(script, "EXEC", description.getExecutable(), file);

        for (String argument : description.getArguments()) {
            echo(script, "EXEC_PARAM", ScriptingUtils.protectAgainstShellMetas(argument), file);
        }

        echo(script, "SUBMITTED", "`/usr/bin/date --iso-8601=sec`", file);
        script.close();

        LOGGER.debug("Created job info script from description {} and workdir {}:\n--script below this line--\n{}\n--end of script--\n", description,
                workingDir, stringBuilder);

        return stringBuilder.toString();
    }

    public static String generateJobErrorScript(String uniqueID, int exit, String out, String err) {

        StringBuilder stringBuilder = new StringBuilder();
        Formatter script = new Formatter(stringBuilder, Locale.US);

        String file = getInfoFile(uniqueID);

        echo(script, "FAILED", "`/usr/bin/date --iso-8601=sec`", file);
        echo(script, "EXIT", Integer.toString(exit), file);
        echo(script, "STDOUT", out, file);
        echo(script, "STDERR", err, file);

        script.close();

        LOGGER.debug("Created job error script for id {}:\n--script below this line--\n{}\n--end of script--\n", uniqueID, stringBuilder);

        return stringBuilder.toString();
    }

    public static String generateJobIDScript(String jobID, String uniqueID) {

        StringBuilder stringBuilder = new StringBuilder();
        Formatter script = new Formatter(stringBuilder, Locale.US);

        String file = getJobIDFile(jobID);

        script.format("echo %s > %s\n", uniqueID, file);
        script.close();

        LOGGER.debug("Created job id script for id {} -> {}:\n--script below this line--\n{}\n--end of script--\n", jobID, uniqueID, stringBuilder);

        return stringBuilder.toString();
    }

    public static String generateListingScript() {

        StringBuilder stringBuilder = new StringBuilder();
        Formatter script = new Formatter(stringBuilder, Locale.US);

        String jobIDFiles = getJobIDFile("*");
        String infoFiles = getInfoFile("");
        String statsFiles = getStatsFile("");

        script.format("XENONJOBS=`ls %s 2> /dev/null`\n\n", jobIDFiles);
        script.format("EXIT_CODE=$?\n\n");
        script.format("echo AT JOB LIST\n\n");
        script.format("if [ $EXIT_CODE -ne 0 ]; then\n");
        script.format("    exit 0\n");
        script.format("fi\n\n");
        script.format("for XENONJOB in $XENONJOBS\n");
        script.format("do\n");
        script.format("   XENON_INFO_FILE=\"%s\"`cat $XENONJOB`\n", infoFiles);
        script.format("   XENON_STATS_FILE=\"%s\"`cat $XENONJOB`\n", statsFiles);
        script.format("   echo \"AT JOB:\" $XENONJOB\n");
        script.format("   echo \"XENON_JOB_ID:\" `cat $XENONJOB`\n");
        script.format("   echo \"START_XENON_INFO_FILE:\" $XENON_INFO_FILE\n");
        script.format("   if [ -f \"$XENON_INFO_FILE\" ]; then\n");
        script.format("      cat $XENON_INFO_FILE\n");
        script.format("   fi\n");
        script.format("   echo \"END_XENON_INFO_FILE:\" $XENON_INFO_FILE\n");
        script.format("   echo \"START_XENON_STATS_FILE:\" $XENON_STATS_FILE\n");
        script.format("   if [ -f \"$XENON_STATS_FILE\" ]; then\n");
        script.format("      cat $XENON_STATS_FILE\n");
        script.format("   fi\n");
        script.format("   echo \"END_XENON_STATS_FILE:\" $XENON_STATS_FILE\n");
        script.format("done\n");

        script.close();

        LOGGER.debug("Created job listing script:\n--script below this line--\n{}\n--end of script--\n", stringBuilder);

        return stringBuilder.toString();

    }
}
