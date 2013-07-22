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
package nl.esciencecenter.octopus.adaptors.gridengine;

import java.util.Formatter;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.octopus.engine.util.CommandLineUtils;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.jobs.JobDescription;

/**
 * Generator for GridEngine job script.
 * 
 * @author Niels Drost
 * 
 */
public class GridEngineJobScriptGenerator {

    private static final Logger logger = LoggerFactory.getLogger(GridEngineJobScriptGenerator.class);

    private static int parseIntOption(String string) throws OctopusException {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            throw new OctopusException(GridEngineAdaptor.ADAPTOR_NAME, "Error in parsing integer option", e);
        }
    }

    private static void generateParallelEnvironmentSpecification(JobDescription description, GridEngineSetup setupInfo,
            Formatter script) throws OctopusException {
        Map<String, String> options = description.getJobOptions();

        String pe = options.get(GridEngineSchedulerConnection.JOB_OPTION_PARALLEL_ENVIRONMENT);

        //determine the number of slots we need. Can be overriden by the user
        int slots;
        if (options.containsKey(GridEngineSchedulerConnection.JOB_OPTION_PARALLEL_SLOTS)) {
            slots = parseIntOption(options.get(GridEngineSchedulerConnection.JOB_OPTION_PARALLEL_SLOTS));
        } else {
            slots = setupInfo.calculateSlots(pe, description.getQueueName(), description.getNodeCount());
        }

        script.format("#$ -pe %s %d\n", pe, slots);
    }

    public static String generate(JobDescription description, AbsolutePath fsEntryPath, GridEngineSetup setup)
            throws OctopusException {
        StringBuilder stringBuilder = new StringBuilder();
        Formatter script = new Formatter(stringBuilder, Locale.US);

        script.format("#!/bin/sh\n");

        //set shell to sh
        script.format("#$ -S /bin/sh\n");

        //set name of job to octopus
        script.format("#$ -N octopus\n");

        //set working directory
        if (description.getWorkingDirectory() != null) {
            if (description.getWorkingDirectory().startsWith("/")) {
                script.format("#$ -wd %s\n", description.getWorkingDirectory());
            } else {
                //make relative path absolute
                AbsolutePath workingDirectory = fsEntryPath.resolve(new RelativePath(description.getWorkingDirectory()));
                script.format("#$ -wd %s\n", workingDirectory.getPath());
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
        script.format("#$ -l h_rt=%02d:%02d:00\n", description.getMaxTime() / 60, description.getMaxTime() % 60);

        if (description.getStdin() != null) {
            script.format("#$ -i %s\n", description.getStdin());
        }

        if (description.getStdout() == null) {
            script.format("#$ -o /dev/null\n");
        } else {
            script.format("#$ -o %s\n", description.getStdout());
        }

        if (description.getStderr() == null) {
            script.format("#$ -e /dev/null\n");
        } else {
            script.format("#$ -e %s\n", description.getStderr());
        }

        if (description.getEnvironment() != null) {
            for (Map.Entry<String, String> entry : description.getEnvironment().entrySet()) {
                script.format("export %s=\"%s\"\n", entry.getKey(), entry.getValue());
            }
        }

        script.format("\n");

        if (description.getNodeCount() == 1 && description.getProcessesPerNode() == 1) {

            script.format("%s", description.getExecutable());

            for (String argument : description.getArguments()) {
                script.format(" %s", CommandLineUtils.protectAgainstShellMetas(argument));
            }
            script.format("\n");

        } else {
            generateParallelScriptContent(description, script);
        }

        script.close();

        logger.debug("Created job script:\n{}", stringBuilder);

        return stringBuilder.toString();
    }

    private static void generateParallelScriptContent(JobDescription description, Formatter script) {
        script.format("for host in `cat $PE_HOSTFILE | cut -d \" \" -f 1` ; do\n");

        for (int i = 0; i < description.getProcessesPerNode(); i++) {
            script.format("\tssh -o StrictHostKeyChecking=false $host \"cd `pwd` && ");
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
        //FIXME: return an exit code here.
    }

}
