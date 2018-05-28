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
package nl.esciencecenter.xenon.adaptors.schedulers.torque;

import static nl.esciencecenter.xenon.adaptors.schedulers.torque.TorqueSchedulerAdaptor.ADAPTOR_NAME;

import java.util.Formatter;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.CommandLineUtils;
import nl.esciencecenter.xenon.adaptors.schedulers.JobStatusImplementation;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingUtils;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.schedulers.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;

/**
 * Generator for GridEngine job script.
 *
 */
final class TorqueUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TorqueUtils.class);

    private static final int MINUTES_PER_HOUR = 60;

    public static final Pattern QUEUE_INFO_NAME = Pattern.compile("^Queue: ([a-zA-Z_]+)$");

    public static final String JOB_OPTION_JOB_SCRIPT = "job.script";
    public static final String JOB_OPTION_JOB_CONTENTS = "job.contents";
    public static final String JOB_OPTION_RESOURCES = "job.resources";

    private static final String[] VALID_JOB_OPTIONS = new String[] { JOB_OPTION_JOB_SCRIPT, JOB_OPTION_RESOURCES };

    private TorqueUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void verifyJobDescription(JobDescription description) throws XenonException {
        ScriptingUtils.verifyJobOptions(description.getJobOptions(), VALID_JOB_OPTIONS, ADAPTOR_NAME);

        // check for option that overrides job script completely.
        if (description.getJobOptions().containsKey(JOB_OPTION_JOB_SCRIPT)) {
            if (description.getJobOptions().containsKey(JOB_OPTION_JOB_CONTENTS)) {
                throw new InvalidJobDescriptionException(ADAPTOR_NAME, "Adaptor cannot process job script and job contents simultaneously.");
            }

            // no remaining settings checked.
            return;
        }

        // perform standard checks.
        ScriptingUtils.verifyJobDescription(description, ADAPTOR_NAME);
    }

    protected static JobStatus getJobStatusFromQstatInfo(Map<String, Map<String, String>> info, String jobIdentifier) throws XenonException {
        boolean done = false;
        Map<String, String> jobInfo = info.get(jobIdentifier);

        if (jobInfo == null) {
            return null;
        }

        // System.out.println("TORQUE STATUS: ");
        // System.out.println("--------------------------------");
        // System.out.println(info.toString());
        // System.out.println("--------------------------------");

        ScriptingUtils.verifyJobInfo(jobInfo, jobIdentifier, ADAPTOR_NAME, "Job_Id", "Job_Name", "job_state");

        String name = jobInfo.get("Job_Name");
        String stateCode = jobInfo.get("job_state");

        XenonException exception = null;
        if (stateCode.equals("E")) {
            exception = new XenonException(ADAPTOR_NAME, "Job reports error state: " + stateCode);
            done = true;
        }
        if (stateCode.equals("C")) {
            done = true;
        }
        Integer exitStatus = null;
        String exitStatusStr = jobInfo.get("exit_status");
        if (exitStatusStr != null) {
            exitStatus = Integer.valueOf(exitStatusStr);
        }

        return new JobStatusImplementation(jobIdentifier, name, stateCode, exitStatus, exception, stateCode.equals("R"), done, jobInfo);
    }

    public static void generateScriptContent(JobDescription description, Formatter script) {
        script.format("%s", description.getExecutable());

        for (String argument : description.getArguments()) {
            script.format(" %s", CommandLineUtils.protectAgainstShellMetas(argument));
        }

        String stdin = description.getStdin();

        if (stdin != null) {
            script.format(" < %s", stdin);
        }

        script.format("\n");
    }

    public static String generate(JobDescription description, Path workdir) {
        StringBuilder stringBuilder = new StringBuilder(500);
        Formatter script = new Formatter(stringBuilder, Locale.US);

        script.format("%s\n", "#!/bin/sh");

        // set shell to sh
        script.format("%s\n", "#PBS -S /bin/sh");

        String name = description.getName();

        if (name == null || name.trim().isEmpty()) {
            name = "xenon";
        }

        // set name of job to xenon
        script.format("#PBS -N %s\n", name);

        // set working directory
        String workingDirectory = description.getWorkingDirectory();

        if (workingDirectory != null) {
            if (!workingDirectory.startsWith("/")) {
                // make relative path absolute
                workingDirectory = workdir.resolve(workingDirectory).toString();
            }
            script.format("#PBS -d %s\n", workingDirectory);
        }

        String stdout = description.getStdout();

        if (stdout != null) {
            if (workingDirectory != null) {
                stdout = workingDirectory + "/" + stdout;
            }

            script.format("#PBS -o %s\n", stdout);
        }

        String stderr = description.getStderr();

        if (stderr != null) {
            if (workingDirectory != null) {
                stderr = workingDirectory + "/" + stderr;
            }

            script.format("#PBS -e %s\n", stderr);
        }

        if (description.getQueueName() != null) {
            script.format("#PBS -q %s\n", description.getQueueName());
        }

        String resources = description.getJobOptions().get(JOB_OPTION_RESOURCES);

        // TODO: check if resources clash with nodes or walltime ?

        if (resources != null) {
            script.format("#PBS -l %s\n", resources);
        }

        int processorsPerNode = description.getProcessesPerNode();

        int threads = description.getThreadsPerProcess();

        if (threads > 1) {
            processorsPerNode = processorsPerNode * threads;
        }

        // number of nodes and processes per node
        script.format("#PBS -l nodes=%d:ppn=%d\n", description.getNodeCount(), processorsPerNode);

        // the max amount of memory per node.
        if (description.getMaxMemory() > 0) {
            script.format("#PBS -l mem=%d\n", description.getMaxMemory());
        }

        // add maximum runtime in hour:minute:second format (converted from minutes in description)
        script.format("#PBS -l walltime=%02d:%02d:00\n", description.getMaxRuntime() / MINUTES_PER_HOUR, description.getMaxRuntime() % MINUTES_PER_HOUR);

        for (String argument : description.getSchedulerArguments()) {
            script.format("#PBS %s\n", argument);
        }

        for (Map.Entry<String, String> entry : description.getEnvironment().entrySet()) {
            script.format("export %s=\"%s\"\n", entry.getKey(), entry.getValue());
        }

        script.format("\n");

        String customContents = description.getJobOptions().get(JOB_OPTION_JOB_CONTENTS);

        if (customContents == null) {
            generateScriptContent(description, script);
        } else {
            script.format("%s\n", customContents);
        }

        script.close();

        LOGGER.debug("Created job script:%n{}", stringBuilder);

        return stringBuilder.toString();
    }
}
