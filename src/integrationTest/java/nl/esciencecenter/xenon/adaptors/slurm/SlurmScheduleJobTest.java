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
package nl.esciencecenter.xenon.adaptors.slurm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.JobException;
import nl.esciencecenter.xenon.adaptors.GenericScheduleJobTestParent;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.Scheduler;
import nl.esciencecenter.xenon.jobs.Streams;
import nl.esciencecenter.xenon.util.Utils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SlurmScheduleJobTest extends GenericScheduleJobTestParent {

    private static final Logger logger = LoggerFactory.getLogger(SlurmScheduleJobTest.class);

    @BeforeClass
    public static void prepareSlurmScheduleJobTest() throws Exception {
        GenericScheduleJobTestParent.prepareClass(new SlurmJobTestConfig(null));
    }

    @AfterClass
    public static void cleanupSlurmScheduleJobTest() throws Exception {
        GenericScheduleJobTestParent.cleanupClass();
    }

    @org.junit.Test
    public void slurm_test01_jobWithCustomScript() throws Exception {
        String message = "Hello World! test01\n";
        String workingDir = getWorkingDir("slurm_test01");
        Path root = initJobDirectory(workingDir);
        Path script = resolve(root, "script");
        Path stdout = resolve(root, "stdout.txt");

        try {
            String scriptContent = "#!/bin/bash\n" + "#SBATCH -o " + stdout.getRelativePath().getAbsolutePath() + "\n" + "#SBATCH -e /dev/null\n" + "echo "
                    + message;

            writeFully(script, scriptContent);

            JobDescription description = new JobDescription();
            description.setInteractive(false);
            description.addJobOption("job.script", script.getRelativePath().getAbsolutePath());

            //the executable should be allowed to be null, as this field is not used at all. Check if this works
            description.setExecutable(null);

            job = jobs.submitJob(scheduler, description);
            JobStatus status = jobs.waitUntilDone(job, config.getJobTimeout(0));

            checkJobDone(status);

            job.getJobDescription().setStdout("stdout.txt");
            checkJobOutput(job, root, message);
        } finally {
            cleanupJob(job, root, script);
        }
    }

    @Test
    public void slurm_test04_parallel_batchJob1() throws Exception {
        String message = "Hello World! Test Slurm 04";
        String workingDir = getWorkingDir("slurm_test04");
        Path root = initJobDirectory(workingDir);
        Path stdout = resolve(root, "stdout.txt");

        try {
            JobDescription description = new JobDescription();
            description.setWorkingDirectory(workingDir);
            description.setExecutable("/bin/echo");
            description.setArguments(message);
            description.setNodeCount(2);
            description.setProcessesPerNode(1);
            description.setStdout("stdout.txt");
            description.setStderr("stderr.txt");

            job = jobs.submitJob(scheduler, description);
            JobStatus status = jobs.waitUntilDone(job, config.getJobTimeout(66));
            checkJobDone(status);

            String outputContent = readFully(stdout);
            logger.debug("got back result: {}", outputContent);

            String[] lines = outputContent.split("\\r?\\n");

            assertEquals(2, lines.length);
            for (String line : lines) {
                assertEquals(message, line);
            }
        } finally {
            cleanupJob(job, root);            
        }
    }

    @Test
    public void slurm_test04_parallel_batchJob2() throws Exception {
        String message = "Hello World! Test Slurm 04";
        String workingDir = getWorkingDir("slurm_test04");
        Path root = initJobDirectory(workingDir);
        Path stdout = resolve(root, "stdout.txt");

        try {
            JobDescription description = new JobDescription();
            description.setWorkingDirectory(workingDir);
            description.setExecutable("/bin/echo");
            description.setArguments(message);
            description.setNodeCount(1);
            description.setProcessesPerNode(2);
            description.setStdout("stdout.txt");
            description.setStderr("stderr.txt");

            job = jobs.submitJob(scheduler, description);
            JobStatus status = jobs.waitUntilDone(job, config.getJobTimeout(66));
            checkJobDone(status);

            String outputContent = readFully(stdout);
            logger.debug("got back result: {}", outputContent);

            String[] lines = outputContent.split("\\r?\\n");

            assertEquals(2, lines.length);
            for (String line : lines) {
                assertEquals(message, line);
            }
        } finally {
            cleanupJob(job, root);            
        }
    }

    @Test
    public void slurm_test04_parallel_batchJob3() throws Exception {
        String message = "Hello World! Test Slurm 04";
        String workingDir = getWorkingDir("slurm_test04");
        Path root = initJobDirectory(workingDir);
        Path stdout = resolve(root, "stdout.txt");

        try {
            JobDescription description = new JobDescription();
            description.setWorkingDirectory(workingDir);
            description.setExecutable("/bin/echo");
            description.setArguments(message);
            description.setNodeCount(2);
            description.setProcessesPerNode(2);
            description.setStdout("stdout.txt");
            description.setStderr("stderr.txt");

            job = jobs.submitJob(scheduler, description);
            JobStatus status = jobs.waitUntilDone(job, config.getJobTimeout(66));
            checkJobDone(status);

            String outputContent = readFully(stdout);
            logger.debug("got back result: {}", outputContent);

            String[] lines = outputContent.split("\\r?\\n");

            assertEquals(4, lines.length);
            for (String line : lines) {
                assertEquals(message, line);
            }
        } finally {
            cleanupJob(job, root);            
        }
    }

    
    
    @org.junit.Test
    public void slurm_test05_jobStatusWithAccountingDisabled() throws Exception {
        String message = "Hello World! test05";
        String workingDir = getWorkingDir("slurm_test05");
        Path root = initJobDirectory(workingDir);

        Scheduler s = null;
        try {
            Map<String, String> properties = new HashMap<>(2);
            properties.put(SlurmAdaptor.DISABLE_ACCOUNTING_USAGE, "true");
            s = jobs.newScheduler(config.getScheme(), config.getCorrectLocation(), 
                config.getDefaultCredential(credentials), properties);

            JobDescription description = echoJobDescription(workingDir, message);
            description.setStdout("stdout.txt");
            description.setStderr("stderr.txt");

            job = jobs.submitJob(s, description);

            JobStatus status = jobs.waitUntilRunning(job, config.getQueueWaitTime());

            if (status.isRunning()) {
                status = jobs.waitUntilDone(job, config.getUpdateTime());
            }

            checkJobDone(status);
            checkJobOutput(job, root, message);
        } finally {
            if (s != null) jobs.close(s);
            cleanupJob(job, root);
        }
    }

    @org.junit.Test
    public void slurm_test06_multiJobWithAccountingDisabled() throws Exception {
        String workingDir = getWorkingDir("slurm_test06");
        Path root = initJobDirectory(workingDir);

        Scheduler s = null;
        Job[] j = new Job[5];
        Job[] jCopy = new Job[5];
        try {
            //custom scheduler with accounting disabled
            Map<String, String> properties = new HashMap<>(2);
            properties.put(SlurmAdaptor.DISABLE_ACCOUNTING_USAGE, "true");
            s = jobs.newScheduler(config.getScheme(), config.getCorrectLocation(),
                    config.getDefaultCredential(credentials), properties);

            for (int i = 0; i < j.length; i++) {
                JobDescription description = timedJobDescription(workingDir, 1);
                description.setQueueName(config.getDefaultQueueName());
                description.setStdout("stdout" + i + ".txt");
                description.setStderr("stderr" + i + ".txt");

                jCopy[i] = j[i] = jobs.submitJob(s, description);
            }

            // Bit hard to determine realistic deadline here ?
            long deadline = System.currentTimeMillis() + config.getQueueWaitTime() + (5 * config.getUpdateTime());

            boolean done = false;

            while (!done) {
                JobStatus[] status = jobs.getJobStatuses(j);

                int count = 0;

                for (int i = 0; i < j.length; i++) {
                    if (j[i] != null) {
                        if (status[i].isDone()) {
                            if (status[i].hasException()) {
                                throw new JobException("Job " + i + " failed", status[i].getException());
                            }

                            logger.info("Job " + i + " done.");
                            j[i] = null;
                        } else {
                            count++;
                        }
                    }
                }

                if (count == 0) {
                    done = true;
                } else {
                    Thread.sleep(1000);

                    long now = System.currentTimeMillis();

                    if (now > deadline) {
                        throw new Exception("Job exceeded deadline!");
                    }
                }
            }
            for (Job j1 : jCopy) {
                checkJobOutput(j1, root);
            }
        } finally {
            if (s != null) jobs.close(s);

            cleanupJobRecursive(root);
        }
    }
/*
    @Test
    public void slurm_test07_interactiveJob() throws Exception {
        String message = "Hello World! Test Slurm 07";
        String workingDir = getWorkingDir("slurm_test07");
        Path root = initJobDirectory(workingDir);

        try {
            JobDescription description = new JobDescription();
            description.setInteractive(true);
            description.setWorkingDirectory(workingDir);
            description.setExecutable("/bin/echo");
            description.setArguments(message);
            description.setNodeCount(2);
            description.setProcessesPerNode(2);

            job = jobs.submitJob(scheduler, description);
            
            if (job == null) { 
                throw new Exception("Job submission returned null!");
            }
            
            Streams streams = jobs.getStreams(job);
            streams.getStdin().close();

            String out = Utils.readToString(streams.getStdout());
            String err = Utils.readToString(streams.getStderr());

            logger.debug("got back stdout : {}", out);
            logger.debug("got back stderr : {}", err);

            String[] lines = out.split("\\r?\\n");

            assertTrue(lines.length == 1);
            for (String line : lines) {
                assertTrue(line.equals(message));
            }

            JobStatus status = jobs.waitUntilDone(job, config.getJobTimeout(30));
            checkJobDone(status);
        } finally {
            try {
                files.delete(root);
            } finally {
                files.close(root.getFileSystem());
            }
        }
    }
*/
    @Test
    public void slurm_test07_interactiveJob() throws Exception {
        String message = "Hello World! Test Slurm 07b";
        String workingDir = getWorkingDir("slurm_test07");
        Path root = initJobDirectory(workingDir);

        try {
            JobDescription description = new JobDescription();
            description.setInteractive(true);
            description.setWorkingDirectory(workingDir);
            description.setExecutable("/bin/cat");
            description.setNodeCount(2);
            description.setProcessesPerNode(2);

            job = jobs.submitJob(scheduler, description);

            Streams streams = jobs.getStreams(job);
            
            PrintWriter w = new PrintWriter(streams.getStdin());
            w.println(message);
            w.flush();
            w.close();

            String out = Utils.readToString(streams.getStdout());
            String err = Utils.readToString(streams.getStderr());

            logger.debug("got back stdout : {}", out);
            logger.debug("got back stderr : {}", err);

            String[] lines = out.split("\\r?\\n");

            assertTrue(lines.length == 1);
            for (String line : lines) {
                assertTrue(line.equals(message));
            }

            JobStatus status = jobs.waitUntilDone(job, config.getJobTimeout(30));
            checkJobDone(status);
        } finally {
            try {
                files.delete(root);
            } finally {
                files.close(root.getFileSystem());
            }
        }
    }

    
    
    @Test
    public void slurm_test08_parallel_batchJob_singleProcess() throws Exception {
        String message = "Hello World! Test Slurm 04";
        String workingDir = getWorkingDir("slurm_test04");
        Path root = initJobDirectory(workingDir);

        try {
            JobDescription description = new JobDescription();
            description.setWorkingDirectory(workingDir);
            description.setExecutable("/bin/echo");
            description.setArguments(message);
            description.setNodeCount(2);
            description.setProcessesPerNode(2);
            description.setStdout("stdout.txt");
            description.setStderr("stderr.txt");

            description.setStartSingleProcess(true);

            job = jobs.submitJob(scheduler, description);
            JobStatus status = jobs.waitUntilDone(job, config.getJobTimeout(30));
            checkJobDone(status);
            
            String outputContent = readFully(resolve(root, "stdout.txt"));
            logger.debug("got back result: {}", outputContent);

            String[] lines = outputContent.split("\\r?\\n");
            //make sure we only have a single line of output, not 4
            assertEquals(1, lines.length);
            for (String line : lines) {
                assertTrue(line.equals(message));
            }
        } finally {
            cleanupJob(job, root);
        }
    }
}
