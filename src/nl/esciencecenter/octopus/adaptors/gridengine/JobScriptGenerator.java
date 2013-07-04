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

import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.jobs.JobDescription;

public class JobScriptGenerator {

    private static final Logger logger = LoggerFactory.getLogger(JobScriptGenerator.class);

    public static String generate(JobDescription description, AbsolutePath fsEntryPath) {
        StringBuilder stringBuilder = new StringBuilder();
        Formatter script = new Formatter(stringBuilder, Locale.US);

        script.format("#!/bin/sh\n");
        script.format("#$ -N octopus\n");

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

        //add maximum runtime in hour:minute:second format (converted from minutes in description)
        script.format("#$ -l h_rt=%02d:%02d:00\n", description.getMaxTime() / 60, description.getMaxTime() % 60);

        if (description.getEnvironment() != null) {
            for (Map.Entry<String, String> entry : description.getEnvironment().entrySet()) {
                script.format("export %s=\"%s\"\n", entry.getKey(), entry.getValue());
            }
        }

        script.format("\n");

        script.format("%s", description.getExecutable());

        for (String argument : description.getArguments()) {
            script.format(" %s", argument);
        }
        script.format("\n");

        //script.format("exit 22\n");

        script.close();

        logger.debug("Created job script {}", stringBuilder);

        return stringBuilder.toString();
    }
}
