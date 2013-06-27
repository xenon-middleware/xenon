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

import org.junit.Test;

public class ITJobLocal {

    @Test
    public void WorkingDirectoryRelativlyToCwd() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);
        Path testdir = Files.createTempDirectory("octopustest");
        System.out.println("Absolute tmp dir "+testdir.toString());

        JobDescription description = new JobDescription();
        description.setExecutable("/usr/bin/touch");
        description.setArguments("bla");
        description.setQueueName("single");
        // testdir == /tmp/octopustest<random>/
        description.setWorkingDirectory(testdir.toString());

        Scheduler scheduler = octopus.jobs().getLocalScheduler();
        Job job = octopus.jobs().submitJob(scheduler, description);

        octopus.jobs().waitUntilDone(job, 5000);

        //Expected files in /tmp/octopustest<random>/
        assertTrue(Files.exists(testdir.resolve("bla")));

        //Assertions below fail, as files are in $PWD/tmp/octopustest<random>/
        System.out.println("stdout written to "+testdir.resolve("stdout.txt"));
        assertTrue(Files.exists(testdir.resolve("stdout.txt")));
        assertTrue(Files.exists(testdir.resolve("stderr.txt")));

        // TODO clean up
        octopus.end();
    }
}