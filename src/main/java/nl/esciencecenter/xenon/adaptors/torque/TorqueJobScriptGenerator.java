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
package nl.esciencecenter.xenon.adaptors.torque;

import java.util.Formatter;
import java.util.Locale;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.engine.util.CommandLineUtils;
import nl.esciencecenter.xenon.files.RelativePath;
import nl.esciencecenter.xenon.jobs.JobDescription;

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
final class TorqueJobScriptGenerator {

    private TorqueJobScriptGenerator() {
        //DO NOT USE
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TorqueJobScriptGenerator.class);

    private static final int MINUTES_PER_HOUR = 60;

    public static void generateScriptContent(JobDescription description, Formatter script) {
        script.format("%s", description.getExecutable());

        for (String argument : description.getArguments()) {
            script.format(" %s", CommandLineUtils.protectAgainstShellMetas(argument));
        }
        script.format("\n");
    }

    public static String generate(JobDescription description, RelativePath fsEntryPath) throws XenonException {
        
        StringBuilder stringBuilder = new StringBuilder(500);
        Formatter script = new Formatter(stringBuilder, Locale.US);

        script.format("#!/bin/sh\n");

        //set shell to sh
        script.format("#PBS -S /bin/sh\n");

        //set name of job to xenon 
        script.format("#PBS -N xenon\n");

        //set working directory
        if (description.getWorkingDirectory() != null) {
            String workingDirectory = description.getWorkingDirectory();
            if (!workingDirectory.startsWith("/")) {
                //make relative path absolute
                workingDirectory = fsEntryPath.resolve(workingDirectory).getAbsolutePath();
            }
            script.format("#PBS -w '%s'\n", workingDirectory);
        }

        if (description.getQueueName() != null) {
            script.format("#PBS -q %s\n", description.getQueueName());
        }

        String resources = description.getJobOptions().get(TorqueSchedulerConnection.JOB_OPTION_RESOURCES);
        if (resources != null) {
            script.format("#PBS -l %s\n", resources);
        }

        //number of nodes and processes per node
        script.format("#PBS -l nodes=%d:ppn=%d\n", description.getNodeCount(), description.getProcessesPerNode());
        
        //add maximum runtime in hour:minute:second format (converted from minutes in description)
        script.format("#PBS -l walltime=%02d:%02d:00\n",
                description.getMaxTime() / MINUTES_PER_HOUR,
                description.getMaxTime() % MINUTES_PER_HOUR);

        for (Map.Entry<String, String> entry : description.getEnvironment().entrySet()) {
            script.format("export %s=\"%s\"\n", entry.getKey(), entry.getValue());
        }

        script.format("\n");

        String customContents = description.getJobOptions().get(TorqueSchedulerConnection.JOB_OPTION_JOB_CONTENTS);
        if (customContents == null) {
            generateScriptContent(description, script);
        } else {
            script.format("%s\n", customContents);
        }

        script.close();

        LOGGER.debug("Created job script:\n{}", stringBuilder);

        return stringBuilder.toString();
    }
}
