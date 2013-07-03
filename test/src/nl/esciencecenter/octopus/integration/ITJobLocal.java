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

package nl.esciencecenter.octopus.integration;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.Scheduler;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ITJobLocal {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    final Logger logger = LoggerFactory.getLogger(ITJobLocal.class);

    /**
     * Regression test for issue #109
     *
     * @throws Exception
     */
    @Test
    public void WorkingDirectoryRelativlyToCwd() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);
        Path testdir = testFolder.newFolder("octopustest").toPath();
        // testdir == /tmp/junit<random>/octopustest/
        logger.info("Absolute tmp dir {}", testdir);

        JobDescription description = new JobDescription();
        description.setExecutable("/usr/bin/touch");
        description.setArguments("bla");
        description.setQueueName("single");
        description.setWorkingDirectory(testdir.toString());

        Scheduler scheduler = octopus.jobs().getLocalScheduler();
        Job job = octopus.jobs().submitJob(scheduler, description);

        octopus.jobs().waitUntilDone(job, 5000);

        // Expect files in /tmp/junit<random>/octopustest/
        assertTrue(Files.exists(testdir.resolve("bla")));

        logger.info("stdout written to {}", testdir.resolve("stdout.txt"));
        assertTrue(Files.exists(testdir.resolve("stdout.txt")));
        assertTrue(Files.exists(testdir.resolve("stderr.txt")));

        OctopusFactory.endOctopus(octopus);        
    }
}