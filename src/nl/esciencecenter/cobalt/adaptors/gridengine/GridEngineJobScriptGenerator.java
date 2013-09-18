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
package nl.esciencecenter.cobalt.adaptors.gridengine;

import java.util.Formatter;
import java.util.Locale;
import java.util.Map;

import nl.esciencecenter.cobalt.CobaltException;
import nl.esciencecenter.cobalt.engine.util.CommandLineUtils;
import nl.esciencecenter.cobalt.files.RelativePath;
import nl.esciencecenter.cobalt.jobs.InvalidJobDescriptionException;
import nl.esciencecenter.cobalt.jobs.JobDescription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Generator for GridEngine job script.
 * 
 * @author Niels Drost
 * 
 */
@SuppressFBWarnings(value = "VA_FORMAT_STRING_USES_NEWLINE", justification = "Script generated is a Unix script.")
final class GridEngineJobScriptGenerator {

    private GridEngineJobScriptGenerator() {
        //DO NOT USE
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GridEngineJobScriptGenerator.class);

    private static final int MINUTES_PER_HOUR = 60;

    protected static void generateParallelEnvironmentSpecification(JobDescription description, GridEngineSetup setup,
            Formatter script) throws CobaltException {
        Map<String, String> options = description.getJobOptions();

        String pe = options.get(GridEngineSchedulerConnection.JOB_OPTION_PARALLEL_ENVIRONMENT);

        //determine the number of slots we need. Can be overridden by the user
        int slots;
        String slotsString = options.get(GridEngineSchedulerConnection.JOB_OPTION_PARALLEL_SLOTS);

        if (slotsString == null) {
            slots = setup.calculateSlots(pe, description.getQueueName(), description.getNodeCount());
        } else {
            try {
                slots = Integer.parseInt(slotsString);
            } catch (NumberFormatException e) {
                throw new InvalidJobDescriptionException(GridEngineAdaptor.ADAPTOR_NAME,
                        "Error in parsing parallel slots option \"" + slotsString + "\"", e);
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

    protected static String generate(JobDescription description, RelativePath fsEntryPath, GridEngineSetup setup)
            throws CobaltException {
        
        StringBuilder stringBuilder = new StringBuilder();
        Formatter script = new Formatter(stringBuilder, Locale.US);

        script.format("#!/bin/sh\n");

        //set shell to sh
        script.format("#$ -S /bin/sh\n");

        //set name of job to cobalt
        script.format("#$ -N cobalt\n");

        //set working directory
        if (description.getWorkingDirectory() != null) {
            if (description.getWorkingDirectory().startsWith("/")) {
                script.format("#$ -wd '%s'\n", description.getWorkingDirectory());
            } else {
                //make relative path absolute
                RelativePath workingDirectory = fsEntryPath.resolve(description.getWorkingDirectory());
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
}
