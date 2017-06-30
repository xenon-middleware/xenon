/**
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
package nl.esciencecenter.xenon.adaptors.job.gridengine;

import static nl.esciencecenter.xenon.adaptors.job.gridengine.GridEngineSchedulerAdaptor.ADAPTOR_NAME;

import java.util.Formatter;
import java.util.Locale;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.job.CommandLineUtils;
import nl.esciencecenter.xenon.adaptors.job.JobCanceledException;
import nl.esciencecenter.xenon.adaptors.job.ScriptingScheduler;
import nl.esciencecenter.xenon.adaptors.job.ScriptingUtils;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.jobs.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.jobs.JobHandle;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Generator for GridEngine job script.
 */
@SuppressFBWarnings(value = "VA_FORMAT_STRING_USES_NEWLINE", justification = "Script generated is a Unix script.")
final class GridEngineUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(GridEngineUtils.class);
    
    public static final String JOB_OPTION_JOB_SCRIPT = "job.script";

    public static final String JOB_OPTION_PARALLEL_ENVIRONMENT = "parallel.environment";

    public static final String JOB_OPTION_PARALLEL_SLOTS = "parallel.slots";
    
    public static final String JOB_OPTION_RESOURCES = "resources";

    private static final String[] VALID_JOB_OPTIONS = new String[] { JOB_OPTION_JOB_SCRIPT, JOB_OPTION_PARALLEL_ENVIRONMENT,
            JOB_OPTION_PARALLEL_SLOTS, JOB_OPTION_RESOURCES };

    public static final String QACCT_HEADER = "==============================================================";
    
    private static final int MINUTES_PER_HOUR = 60;
    
    protected static void generateParallelEnvironmentSpecification(JobDescription description, GridEngineSetup setup,
            Formatter script) throws XenonException {
        Map<String, String> options = description.getJobOptions();

        String pe = options.get(JOB_OPTION_PARALLEL_ENVIRONMENT);

        //determine the number of slots we need. Can be overridden by the user
        int slots;
        String slotsString = options.get(JOB_OPTION_PARALLEL_SLOTS);

        if (slotsString == null) {
            slots = setup.calculateSlots(pe, description.getQueueName(), description.getNodeCount());
        } else {
            try {
                slots = Integer.parseInt(slotsString);
            } catch (NumberFormatException e) {
                throw new InvalidJobDescriptionException(ADAPTOR_NAME, "Error in parsing parallel slots option \"" + slotsString 
                        + "\"", e);
            }
        }

        script.format("#$ -pe %s %d\n", pe, slots);
    }

    protected static void generateSerialScriptContent(JobDescription description, Formatter script) {
        script.format("%s", description.getExecutable());

        for (String argument : description.getArguments()) {
            script.format(" %s", CommandLineUtils.protectAgainstShellMetas(argument));
        }
        script.format("\n");
    }

    protected static void generateParallelScriptContent(JobDescription description, Formatter script) {
        script.format("for host in `cat $PE_HOSTFILE | cut -d \" \" -f 1` ; do\n");

        for (int i = 0; i < description.getProcessesPerNode(); i++) {
            script.format("  ssh -o StrictHostKeyChecking=false $host \"cd `pwd` && ");
            script.format("%s", description.getExecutable());
            for (String argument : description.getArguments()) {
                script.format(" %s", CommandLineUtils.protectAgainstShellMetas(argument));
            }
            script.format("\"&\n");
        }
        //wait for all ssh connections to finish
        script.format("done\n\n");
        script.format("wait\n");
        script.format("exit 0\n");
        script.format("\n");
    }

    @SuppressWarnings("PMD.NPathComplexity")
    protected static String generate(JobDescription description, Path fsEntryPath, GridEngineSetup setup)
            throws XenonException {
        
        StringBuilder stringBuilder = new StringBuilder();
        Formatter script = new Formatter(stringBuilder, Locale.US);

        script.format("#!/bin/sh\n");

        //set shell to sh
        script.format("#$ -S /bin/sh\n");

        //set name of job to xenon 
        script.format("#$ -N xenon\n");

        //set working directory
        if (description.getWorkingDirectory() != null) {
            if (description.getWorkingDirectory().startsWith("/")) {
                script.format("#$ -wd '%s'\n", description.getWorkingDirectory());
            } else {
                //make relative path absolute
                Path workingDirectory = fsEntryPath.resolve(description.getWorkingDirectory());
                script.format("#$ -wd '%s'\n", workingDirectory.getAbsolutePath());
            }
        }

        if (description.getQueueName() != null) {
            script.format("#$ -q %s\n", description.getQueueName());
        }

        //parallel environment and slot count (if needed)
        if (description.getNodeCount() > 1) {
            generateParallelEnvironmentSpecification(description, setup, script);
        }

        //add maximum runtime in hour:minute:second format (converted from minutes in description)
        script.format("#$ -l h_rt=%02d:%02d:00\n", description.getMaxTime() / MINUTES_PER_HOUR, description.getMaxTime()
                % MINUTES_PER_HOUR);
        
        String resources = description.getJobOptions().get(JOB_OPTION_RESOURCES);
        
        if (resources != null) {
            script.format("#$ -l %s\n", resources);
        }
        
        if (description.getStdin() != null) {
            script.format("#$ -i '%s'\n", description.getStdin());
        }

        if (description.getStdout() == null) {
            script.format("#$ -o /dev/null\n");
        } else {
            script.format("#$ -o '%s'\n", description.getStdout());
        }

        if (description.getStderr() == null) {
            script.format("#$ -e /dev/null\n");
        } else {
            script.format("#$ -e '%s'\n", description.getStderr());
        }

        for (Map.Entry<String, String> entry : description.getEnvironment().entrySet()) {
            script.format("export %s=\"%s\"\n", entry.getKey(), entry.getValue());
        }

        script.format("\n");

        if (description.getNodeCount() == 1 && description.getProcessesPerNode() == 1) {
            generateSerialScriptContent(description, script);
        } else {
            generateParallelScriptContent(description, script);
        }

        script.close();

        LOGGER.debug("Created job script:\n{}", stringBuilder);

        return stringBuilder.toString();
    }
    
    protected static void verifyJobDescription(JobDescription description) throws XenonException {
        ScriptingUtils.verifyJobOptions(description.getJobOptions(), VALID_JOB_OPTIONS, ADAPTOR_NAME);

        if (description.isInteractive()) {
            throw new InvalidJobDescriptionException(ADAPTOR_NAME, "Adaptor does not support interactive jobs");
        }
        
        if (description.isStartSingleProcess()) {
            throw new InvalidJobDescriptionException(ADAPTOR_NAME, "StartSingleProcess option not supported");
        }

        //check for option that overrides job script completely.
        if (description.getJobOptions().get(JOB_OPTION_JOB_SCRIPT) != null) {
            //no remaining settings checked.
            return;
        }

        //perform standard checks.
        ScriptingUtils.verifyJobDescription(description, ADAPTOR_NAME);

        //check if the parallel environment and queue are specified.
        if (description.getNodeCount() != 1) {
            if (!description.getJobOptions().containsKey(JOB_OPTION_PARALLEL_ENVIRONMENT)) {
                throw new InvalidJobDescriptionException(ADAPTOR_NAME,
                        "Parallel job requested but mandatory parallel.environment option not specificied.");
            }
            if (description.getQueueName() == null && !description.getJobOptions().containsKey(JOB_OPTION_PARALLEL_SLOTS)) {
                throw new InvalidJobDescriptionException(ADAPTOR_NAME,
                        "Parallel job requested but neither queue nor number of slots specificied (at least one is required)");
            }
        }
    }

    @SuppressWarnings("PMD.EmptyIfStmt")
    protected static JobStatus getJobStatusFromQacctInfo(Map<String, String> info, JobHandle job) throws XenonException {
        Integer exitcode;
        Exception exception = null;
        String state = "done";

        if (info == null) {
            return null;
        }

        ScriptingUtils.verifyJobInfo(info, job, ADAPTOR_NAME, "jobnumber", "exit_status", "failed");

        String exitcodeString = info.get("exit_status");
        String failedString = info.get("failed");

        try {
            exitcode = Integer.parseInt(info.get("exit_status"));
        } catch (NumberFormatException e) {
            throw new XenonException(ADAPTOR_NAME, "cannot parse exit code of job " + job.getIdentifier()
                    + " from string " + exitcodeString, e);
        }

        if (failedString.equals("0")) {
            //Success!
        } else if (failedString.startsWith("100")) {
            //error code for killed jobs
            exception = new JobCanceledException(ADAPTOR_NAME, "Job killed by signal");
        } else {
            //unknown error code
            exception = new XenonException(ADAPTOR_NAME, "Job reports error: " + failedString);
        }

        return new JobStatus(job, state, exitcode, exception, false, true, info);
    }

    protected static JobStatus getJobStatusFromQstatInfo(Map<String, Map<String, String>> info, JobHandle job) throws XenonException {
        boolean done = false;
        Map<String, String> jobInfo = info.get(job.getIdentifier());

        if (jobInfo == null) {
            return null;
        }

        ScriptingUtils.verifyJobInfo(jobInfo, job, ADAPTOR_NAME, "JB_job_number", "state", "long_state");

        String longState = jobInfo.get("long_state");
        String stateCode = jobInfo.get("state");

        Exception exception = null;
        if (stateCode.contains("E")) {
            exception = new XenonException(ADAPTOR_NAME, "Job reports error state: " + stateCode);
            done = true;
        }

        return new JobStatus(job, longState, null, exception, "running".equals(longState), done, jobInfo);
    }
}
