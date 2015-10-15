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

package nl.esciencecenter.xenon.adaptors.gridengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.io.OutputStream;

import nl.esciencecenter.xenon.adaptors.GenericScheduleJobTestParent;
import nl.esciencecenter.xenon.files.OpenOption;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;

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
public class GridEngineScheduleJobTest extends GenericScheduleJobTestParent {
    private static final Logger logger = LoggerFactory.getLogger(GridEngineScheduleJobTest.class);
    
    @BeforeClass
    public static void prepareGridEngineJobAdaptorTest() throws Exception {
        GenericScheduleJobTestParent.prepareClass(new GridEngineJobTestConfig(null));
    }

    @AfterClass
    public static void cleanupGridEngineJobAdaptorTest() throws Exception {
        GenericScheduleJobTestParent.cleanupClass();
    }

    @Test
    public void ge_test01_jobWithCustomScript() throws Exception {
        String message = "Hello World! test01\n";
        String workingDir = getWorkingDir("ge_test01");
        Path root = initJobDirectory(workingDir);
        Path script = resolve(root, "script");
        Path stdout = resolve(root, "stdout.txt");
        
        try {
            String scriptContent = "#!/bin/bash\n" + "#$ -o " + stdout.getRelativePath().getAbsolutePath() + "\n" + "#$ -e /dev/null\n" + "echo " + message;

            OutputStream out = files.newOutputStream(script, OpenOption.CREATE, OpenOption.APPEND, OpenOption.WRITE);
            writeFully(out, scriptContent);

            JobDescription description = new JobDescription();
            description.setInteractive(false);
            description.addJobOption("job.script", script.getRelativePath().getAbsolutePath());

            //the executable should be allowed to be null, as this field is not used at all. Check if this works
            description.setExecutable(null);

            job = jobs.submitJob(scheduler, description);
            JobStatus status = jobs.waitUntilDone(job, config.getJobTimeout(0));

            checkJobDone(status);

            // check output file
            job.getJobDescription().setStdout("stdout.txt");
            checkJobOutput(job, root, message);
        } finally {
            cleanupJob(job, root, stdout, script);
        }
    }

    @Test
    public void ge_test04_parallel_batchJob() throws Exception {
        String parallelEnvironment = ((GridEngineJobTestConfig) config).getParallelEnvironment();
        
        //skip test if no parallel environment configured
        assumeTrue(parallelEnvironment != null);
        
        String message = "Hello World! Test GE 04";
        String workingDir = getWorkingDir("ge_test04");
        Path root = initJobDirectory(workingDir);
        
        try {
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

            job = jobs.submitJob(scheduler, description);

            // the job stays running for a minute using the docker container nlesc/xenon-gridengine
            // wait little bit more than a minute so the job is done
            JobStatus status = jobs.waitUntilDone(job, config.getJobTimeout(66));

            checkJobDone(status);

            String outputContent = readFully(resolve(root, "stdout.txt"));
            logger.info("got back result: {}", outputContent);

            String[] lines = outputContent.split("\\r?\\n");

            assertEquals("output should be exactly 4 lines", 4, lines.length);

            for (String line : lines) {
                assertEquals(message, line);
            }
        } finally {
            cleanupJob(job, root);
        }
    }
}
