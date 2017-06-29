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
package nl.esciencecenter.xenon.jobs;

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import nl.esciencecenter.xenon.Xenon;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ITJobLocal {
    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    final Logger logger = LoggerFactory.getLogger(ITJobLocal.class);

    /**
     * Regression test for issue #109
     * 
     * @throws Exception
     */
    @Test
    public void WorkingDirectoryRelativlyToCwd() throws Exception {
        Path testdir = testFolder.newFolder("xenontest").toPath();
        // testdir == /tmp/junit<random>/xenontest/
        logger.info("Absolute tmp dir {}", testdir);

        JobDescription description = new JobDescription();
        description.setExecutable("/usr/bin/touch");
        description.setArguments("bla");
        description.setQueueName("single");
        description.setWorkingDirectory(testdir.toString());
        description.setStdout("stdout.txt");
        description.setStderr("stderr.txt");

        Scheduler scheduler = Xenon.jobs().newScheduler("local", null, null, null);
        Job job = Xenon.jobs().submitJob(scheduler, description);

        Xenon.jobs().waitUntilDone(job, 5000);

        // Expect files in /tmp/junit<random>/xenontest/
        assertTrue(Files.exists(testdir.resolve("bla")));

        logger.info("stdout written to {}", testdir.resolve("stdout.txt"));
        assertTrue(Files.exists(testdir.resolve("stdout.txt")));
        assertTrue(Files.exists(testdir.resolve("stderr.txt")));

        Xenon.endAll();
    }
}