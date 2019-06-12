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
package nl.esciencecenter.xenon.adaptors.schedulers.gridengine;

import static nl.esciencecenter.xenon.adaptors.schedulers.gridengine.GridEngineSchedulerAdaptor.ADAPTOR_NAME;

import java.util.Formatter;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.JobCanceledException;
import nl.esciencecenter.xenon.adaptors.schedulers.JobStatusImplementation;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingUtils;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.schedulers.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;

/**
 * Generator for GridEngine job script.
 */
final class GridEngineUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(GridEngineUtils.class);

    public static final String QACCT_HEADER = "==============================================================";

    private static final int MINUTES_PER_HOUR = 60;

    protected static void generateParallelEnvironmentSpecification(JobDescription description, GridEngineSetup setup, Formatter script) throws XenonException {
        if (description.getTasks() == 1 && description.getCoresPerTask() == 1) {
            // Single task + single core
            // nothing to do
        } else if (description.getTasks() == 1 && description.getCoresPerTask() > 1) {
            // Single node + multi core
            Optional<ParallelEnvironmentInfo> pe = setup.getSingleNodeParallelEnvironment(description.getCoresPerTask(), description.getQueueName());
            if (pe.isPresent()) {
                script.format("#$ -pe %s %d\n", pe.get().getName(), description.getCoresPerTask());
            } else {
                throw new InvalidJobDescriptionException(ADAPTOR_NAME,
                        "Unable to find a parallel environment for multi core on single node, replace node count and cores per node with scheduler.addSchedulerArgument(\"-pe <name of parallel environment (qconf -spl)> <number of slots>\")");
            }
        } else {
            // Multi node + multi core
            Optional<ParallelEnvironmentInfo> pe = setup.getMultiNodeParallelEnvironment(description.getCoresPerTask(), description.getTasks(),
                    description.getQueueName());
            if (pe.isPresent()) {
                script.format("#$ -pe %s %d\n", pe.get().getName(), description.getCoresPerTask() * description.getTasks());
            } else {
                throw new InvalidJobDescriptionException(ADAPTOR_NAME,
                        "Unable to find a parallel environment for multiple nodes, replace node count and cores per node with scheduler.addSchedulerArgument(\"-pe <name of parallel environment (qconf -spl)> <number of slots>\")");
            }
        }
    }

    protected static void generateSerialScriptContent(JobDescription description, Formatter script) {
        script.format("%s", description.getExecutable());

        for (String argument : description.getArguments()) {
            script.format(" %s", ScriptingUtils.protectAgainstShellMetas(argument));
        }
        script.format("\n");
    }

    protected static void generateParallelScriptContent(JobDescription description, Formatter script) {
        script.format("%s\n", "for host in `cat $PE_HOSTFILE | cut -d \" \" -f 1` ; do");

        // TODO: the PE_HOSTFILE seems to contain "<host> <slots> <queue> <processor range>"
        //
        // What happens if we want 2x2 slots on a PE with 4 slots per host? Will we get 1 or 2 lines in the PE_HOSTFILE???

        // for (int i = 0; i < 1; i++) {
        script.format("%s", "  ssh -o StrictHostKeyChecking=false $host \"cd `pwd` && ");
        script.format("%s", description.getExecutable());
        for (String argument : description.getArguments()) {
            script.format(" %s", ScriptingUtils.protectAgainstShellMetas(argument));
        }
        script.format("%c&\n", '"');
        // }

        // wait for all ssh connections to finish
        script.format("%s\n\n", "done");
        script.format("%s\n", "wait");
        script.format("%s\n\n", "exit 0");
    }

    private GridEngineUtils() {
        throw new IllegalStateException("Utility class");
    }

    @SuppressWarnings("PMD.NPathComplexity")
    protected static String generate(JobDescription description, Path fsEntryPath, GridEngineSetup setup) throws XenonException {

        StringBuilder stringBuilder = new StringBuilder();
        Formatter script = new Formatter(stringBuilder, Locale.US);

        script.format("%s\n", "#!/bin/sh");

        // set shell to sh
        script.format("%s\n", "#$ -S /bin/sh");

        String name = description.getName();

        if (name == null || name.trim().isEmpty()) {
            name = "xenon";
        }

        // set name of job to xenon
        script.format("%s\n", "#$ -N " + name);

        // set working directory
        if (description.getWorkingDirectory() != null) {
            String path = ScriptingUtils.getWorkingDirPath(description, fsEntryPath);
            script.format("#$ -wd '%s'\n", path);
        }

        if (description.getQueueName() != null) {
            script.format("#$ -q %s\n", description.getQueueName());
        }

        // parallel environment and slot count (if needed)
        if (description.getSchedulerArguments().stream().noneMatch(d -> d.equals("-pe"))) {
            generateParallelEnvironmentSpecification(description, setup, script);
        }

        // add maximum runtime in hour:minute:second format (converted from minutes in description)
        int runtime = description.getMaxRuntime();

        if (runtime == -1) {
            runtime = setup.getDefaultRuntime();
        }

        script.format("#$ -l h_rt=%02d:%02d:00\n", runtime / MINUTES_PER_HOUR, runtime % MINUTES_PER_HOUR);

        // the max amount of memory per node.
        if (description.getMaxMemory() > 0) {
            script.format("#$ -l mem_free=%dM,h_vmem=%dM\n", description.getMaxMemory(), description.getMaxMemory());
        }

        if (description.getTempSpace() > 0) {
            script.format("#$ -l tmpspace=%dM\n", description.getTempSpace());
        }

        for (String argument : description.getSchedulerArguments()) {
            script.format("#$ %s\n", argument);
        }

        if (description.getStdin() != null) {
            script.format("#$ -i '%s'\n", description.getStdin());
        }

        if (description.getStdout() == null) {
            script.format("#$ -o /dev/null\n");
        } else {
            script.format("#$ -o '%s'\n", substituteJobID(description.getStdout()));
        }

        if (description.getStderr() == null) {
            script.format("#$ -e /dev/null\n");
        } else {
            script.format("#$ -e '%s'\n", substituteJobID(description.getStderr()));
        }

        for (Map.Entry<String, String> entry : description.getEnvironment().entrySet()) {
            script.format("export %s=\"%s\"\n", entry.getKey(), entry.getValue());
        }

        script.format("\n");

        if (description.isStartPerTask() && description.getTasks() > 1) {
            generateParallelScriptContent(description, script);
        } else {
            generateSerialScriptContent(description, script);
        }

        script.close();

        LOGGER.debug("Created job script:%n{}", stringBuilder);

        return stringBuilder.toString();
    }

    protected static String substituteJobID(String path) {

        if (path == null) {
            return null;
        }

        return path.replace("%j", "$JOB_ID");
    }

    protected static void verifyJobDescription(JobDescription description, String[] queueNames) throws XenonException {
        // perform standard checks.
        ScriptingUtils.verifyJobDescription(description, queueNames, ADAPTOR_NAME);

        // Check if the maxRuntimeTime is not set to infinite.
        if (description.getMaxRuntime() == 0) {
            throw new InvalidJobDescriptionException(ADAPTOR_NAME, "Invalid runtime: 0");
        }
    }

    @SuppressWarnings("PMD.EmptyIfStmt")
    protected static JobStatus getJobStatusFromQacctInfo(Map<String, String> info, String jobIdentifier) throws XenonException {
        Integer exitcode;
        XenonException exception = null;
        String state = "done";

        if (info == null) {
            return null;
        }

        ScriptingUtils.verifyJobInfo(info, jobIdentifier, ADAPTOR_NAME, "jobnumber", "jobname", "exit_status", "failed");

        String name = info.get("jobname");
        String exitcodeString = info.get("exit_status");
        String failedString = info.get("failed");

        try {
            exitcode = Integer.parseInt(info.get("exit_status"));
        } catch (NumberFormatException e) {
            throw new XenonException(ADAPTOR_NAME, "cannot parse exit code of job " + jobIdentifier + " from string " + exitcodeString, e);
        }

        if (failedString.equals("0")) {
            // Success!
        } else if (failedString.startsWith("100")) {
            // error code for killed jobs
            exception = new JobCanceledException(ADAPTOR_NAME, "Job killed by signal");
        } else {
            // unknown error code
            exception = new XenonException(ADAPTOR_NAME, "Job reports error: " + failedString);
        }

        return new JobStatusImplementation(jobIdentifier, name, state, exitcode, exception, false, true, info);
    }

    protected static JobStatus getJobStatusFromQstatInfo(Map<String, Map<String, String>> info, String jobIdentifier) throws XenonException {
        boolean done = false;
        Map<String, String> jobInfo = info.get(jobIdentifier);

        if (jobInfo == null) {
            return null;
        }

        ScriptingUtils.verifyJobInfo(jobInfo, jobIdentifier, ADAPTOR_NAME, "JB_job_number", "JB_name", "state", "long_state");

        String name = jobInfo.get("JB_name");
        String longState = jobInfo.get("long_state");
        String stateCode = jobInfo.get("state");

        XenonException exception = null;
        if (stateCode.contains("E")) {
            exception = new XenonException(ADAPTOR_NAME, "Job reports error state: " + stateCode);
            done = true;
        }

        return new JobStatusImplementation(jobIdentifier, name, longState, null, exception, "running".equals(longState), done, jobInfo);
    }
}
