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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.OutputStream;
import java.net.URI;

import nl.esciencecenter.octopus.adaptors.GenericJobAdaptorTestParent;
import nl.esciencecenter.octopus.exceptions.InvalidLocationException;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.Pathname;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GridEngineJobAdaptorTest extends GenericJobAdaptorTestParent {

    private static final Logger logger = LoggerFactory.getLogger(GridEngineJobAdaptorTest.class);

    @BeforeClass
    public static void prepareGridEngineJobAdaptorTest() throws Exception {
        GenericJobAdaptorTestParent.prepareClass(new GridEngineJobTestConfig(null));
    }

    @AfterClass
    public static void cleanupGridEngineJobAdaptorTest() throws Exception {
        GenericJobAdaptorTestParent.cleanupClass();
    }

    @org.junit.Test
    public void ge_test01_jobWithCustomScript() throws Exception {
        String message = "Hello World! test01\n";

        String workingDir = getWorkingDir("ge_test01");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);

        Path root = filesystem.getEntryPath().resolve(new Pathname(workingDir));
        files.createDirectories(root);

        Path script = root.resolve(new Pathname("script"));
        Path stdout = root.resolve(new Pathname("stdout.txt"));

        String scriptContent = "#!/bin/bash\n" + "#$ -o " + stdout.getPath() + "\n" + "#$ -e /dev/null\n" + "echo " + message;

        OutputStream out = files.newOutputStream(script, OpenOption.CREATE, OpenOption.APPEND, OpenOption.WRITE);
        writeFully(out, scriptContent);

        JobDescription description = new JobDescription();
        description.setInteractive(false);
        description.addJobOption("job.script", script.getPath());

        //the executable should be allowed to be null, as this field is not used at all. Check if this works
        description.setExecutable(null);

        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, 60000);

        if (!status.isDone()) {
            throw new Exception("Job exceeded deadline!");
        }

        if (status.hasException()) {
            throw new Exception("Job failed!", status.getException());
        }

        String outputContent = readFully(files.newInputStream(stdout));

        logger.debug("got output " + outputContent);

        files.delete(stdout);
        files.delete(script);
        files.delete(root);

        jobs.close(scheduler);
        files.close(filesystem);

        assertTrue(outputContent.equals(message));
    }

    @Test
    public void ge_test02_newScheduler_pathWithSlash() throws Exception {

        URI uriWithSlash = new URI(config.getCorrectURI().toString() + "/");

        Scheduler s = jobs.newScheduler(uriWithSlash, null, null);
        jobs.close(s);
    }

    @Test(expected = InvalidLocationException.class)
    public void ge_test03_newScheduler_pathWithFragment_Exception() throws Exception {

        URI uriWithFragment = new URI(config.getCorrectURI().toString() + "#somefragment");

        Scheduler s = jobs.newScheduler(uriWithFragment, null, null);
        jobs.close(s);
    }

    @Test
    public void ge_test04_parallel_batchJob() throws Exception {
        String parallelEnvironment = ((GridEngineJobTestConfig) config).getParallelEnvironment();
        
        //skip test if no parallel environment configured
        assumeTrue(parallelEnvironment != null);
        
        String message = "Hello World! Test GE 04";

        String workingDir = getWorkingDir("ge_test04");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);

        Path root = filesystem.getEntryPath().resolve(new Pathname(workingDir));
        files.createDirectories(root);

        Path stdout = root.resolve(new Pathname("stdout.txt"));
        Path stderr = root.resolve(new Pathname("stderr.txt"));

        JobDescription description = new JobDescription();
        description.setStdout("stdout.txt");
        description.setStderr("stderr.txt");
        description.setWorkingDirectory(workingDir);
        description.setExecutable("/bin/echo");
        description.setArguments(message);
        description.setNodeCount(2);
        description.setProcessesPerNode(2);
        description.addJobOption("parallel.environment", parallelEnvironment);
        description.setQueueName(config.getDefaultQueueName());

        Job job = jobs.submitJob(scheduler, description);

        JobStatus status = jobs.waitUntilDone(job, config.getQueueWaitTime() + config.getUpdateTime());

        if (!status.isDone()) {
            throw new Exception("Job not finished");
        }

        if (status.hasException()) {
            throw new Exception("Job did not finish properly", status.getException());
        }

        String outputContent = readFully(files.newInputStream(stdout));

        files.delete(stdout);
        files.delete(stderr);
        files.delete(root);

        jobs.close(scheduler);
        files.close(filesystem);

        logger.debug("got back result: {}", outputContent);

        String[] lines = outputContent.split("\\r?\\n");

        assertEquals("output should be exactly 4 lines", 4, lines.length);
        
        for (String line : lines) {
            assertTrue(line.equals(message));
        }
    }

}
