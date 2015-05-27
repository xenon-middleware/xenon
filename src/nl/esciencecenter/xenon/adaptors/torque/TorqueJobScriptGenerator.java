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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Formatter;
import java.util.Locale;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.engine.util.CommandLineUtils;
import nl.esciencecenter.xenon.files.RelativePath;
import nl.esciencecenter.xenon.jobs.JobDescription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    static void generateSerialScriptContent(JobDescription description, Formatter script) {
        script.format("%s", description.getExecutable());

        for (String argument : description.getArguments()) {
            script.format(" %s", CommandLineUtils.protectAgainstShellMetas(argument));
        }
        script.format("\n");
    }

    static void generateParallelScriptContent(JobDescription description, Formatter script) {
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

    static String generate(JobDescription description, RelativePath fsEntryPath, TorqueSetup setup)
            throws XenonException {
        
        StringBuilder stringBuilder = new StringBuilder(500);
        Formatter script = new Formatter(stringBuilder, Locale.US);

        script.format("#!/bin/sh\n");

        //set shell to sh
        script.format("#PBS -S /bin/sh\n");

        //set name of job to xenon 
        script.format("#PBS -N xenon\n");

        //set working directory
        if (description.getWorkingDirectory() != null) {
            if (description.getWorkingDirectory().startsWith("/")) {
                script.format("#PBS -w '%s'\n", description.getWorkingDirectory());
            } else {
                //make relative path absolute
                RelativePath workingDirectory = fsEntryPath.resolve(description.getWorkingDirectory());
                script.format("#PBS -w '%s'\n", workingDirectory.getAbsolutePath());
            }
        }

        if (description.getQueueName() != null) {
            script.format("#PBS -q %s\n", description.getQueueName());
        }

        //number of nodes and processes per node
        script.format("#PBS -lnodes=%d,ppn=%d\n", description.getNodeCount(), description.getProcessesPerNode());
        
        //add maximum runtime in hour:minute:second format (converted from minutes in description)
        script.format("#PBS -lwalltime=%02d:%02d:00\n",
                description.getMaxTime() / MINUTES_PER_HOUR,
                description.getMaxTime() % MINUTES_PER_HOUR);

        if (description.getStdin() != null) {
            script.format("#PBS -i '%s'\n", description.getStdin());
        }

        if (description.getStdout() == null) {
            script.format("#PBS -o /dev/null\n");
        } else {
            script.format("#PBS -o '%s'\n", description.getStdout());
        }

        if (description.getStderr() == null) {
            script.format("#PBS -e /dev/null\n");
        } else {
            script.format("#PBS -e '%s'\n", description.getStderr());
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
