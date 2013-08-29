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

package nl.esciencecenter.octopus.adaptors.slurm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.adaptors.GenericJobAdaptorTestParent;
import nl.esciencecenter.octopus.exceptions.InvalidLocationException;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.Pathname;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
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
public class SlurmJobAdaptorTest extends GenericJobAdaptorTestParent {

    private static final Logger logger = LoggerFactory.getLogger(SlurmJobAdaptorTest.class);

    @BeforeClass
    public static void prepareGridEngineJobAdaptorTest() throws Exception {
        GenericJobAdaptorTestParent.prepareClass(new SlurmJobTestConfig(null));
    }

    @AfterClass
    public static void cleanupGridEngineJobAdaptorTest() throws Exception {
        GenericJobAdaptorTestParent.cleanupClass();
    }

    @org.junit.Test
    public void slurm_test01_jobWithCustomScript() throws Exception {
        String message = "Hello World! test01\n";

        String workingDir = getWorkingDir("slurm_test01");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);

        Path root = resolve(filesystem, workingDir);
        files.createDirectories(root);

        Path script = resolve(root, "script");
        Path stdout = resolve(root, "stdout.txt");

        String scriptContent = "#!/bin/bash\n" + "#SBATCH -o " + stdout.getPathname().getAbsolutePath() + "\n" + "#SBATCH -e /dev/null\n" + "echo "
                + message;

        OutputStream out = files.newOutputStream(script, OpenOption.CREATE, OpenOption.APPEND, OpenOption.WRITE);
        writeFully(out, scriptContent);

        JobDescription description = new JobDescription();
        description.setInteractive(false);
        description.addJobOption("job.script", script.getPathname().getAbsolutePath());

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

//    @Test
//    public void slurm_test02_newScheduler_pathWithSlash() throws Exception {
//
//        URI uriWithSlash = new URI(config.getCorrectURI().toString() + "/");
//
//        Scheduler s = jobs.newScheduler(uriWithSlash, null, null);
//        jobs.close(s);
//    }
//
//    @Test(expected = InvalidLocationException.class)
//    public void slurm_test03_newScheduler_pathWithFragment_Exception() throws Exception {
//
//        URI uriWithFragment = new URI(config.getCorrectURI().toString() + "#somefragment");
//
//        Scheduler s = jobs.newScheduler(uriWithFragment, null, null);
//        jobs.close(s);
//    }

    @Test
    public void slurm_test04_parallel_batchJob() throws Exception {
        String message = "Hello World! Test Slurm 04";

        String workingDir = getWorkingDir("slurm_test04");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);

        Path root = resolve(filesystem, workingDir);
        files.createDirectories(root);

        Path stdout = resolve(root, "stdout.txt");
        Path stderr = resolve(root, "stderr.txt");

        JobDescription description = new JobDescription();
        description.setWorkingDirectory(workingDir);
        description.setExecutable("/bin/echo");
        description.setArguments(message);
        description.setNodeCount(2);
        description.setProcessesPerNode(2);
        description.setStdout("stdout.txt");
        description.setStderr("stderr.txt");

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

        assertTrue(lines.length == 4);
        for (String line : lines) {
            assertTrue(line.equals(message));
        }
    }

    @org.junit.Test
    public void slurm_test05_jobStatusWithAccountingDisabled() throws Exception {

        String message = "Hello World! test05";
        String workingDir = getWorkingDir("slurm_test05");

        Map<String, String> properties = new HashMap<String, String>();
        properties.put(SlurmAdaptor.DISABLE_ACCOUNTING_USAGE, "true");

        Scheduler scheduler = jobs.newScheduler(config.getScheme(), config.getCorrectLocation(), 
                config.getDefaultCredential(credentials), properties);

        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);

        Path root = resolve(filesystem, workingDir);
        files.createDirectories(root);

        JobDescription description = new JobDescription();
        description.setExecutable("/bin/echo");
        description.setArguments("-n", message);
        description.setInteractive(false);
        description.setWorkingDirectory(workingDir);
        description.setStdin(null);
        description.setStdout("stdout.txt");
        description.setStderr("stderr.txt");

        Job job = jobs.submitJob(scheduler, description);

        JobStatus status = jobs.waitUntilRunning(job, config.getQueueWaitTime());

        if (status.isRunning()) {
            status = jobs.waitUntilDone(job, config.getUpdateTime());
        }

        if (!status.isDone()) {
            throw new Exception("Job exceeded deadline!");
        }

        if (status.hasException()) {
            throw new Exception("Job failed!", status.getException());
        }

        jobs.close(scheduler);

        Path out = resolve(root, "stdout.txt");
        Path err = resolve(root, "stderr.txt");

        String tmpout = readFully(files.newInputStream(out));
        String tmperr = readFully(files.newInputStream(err));

        files.delete(out);
        files.delete(err);
        files.delete(root);

        files.close(filesystem);

        System.err.println("STDOUT: " + tmpout);
        System.err.println("STDERR: " + tmperr);

        assertTrue(tmpout != null);
        assertTrue(tmpout.length() > 0);
        assertTrue(tmpout.equals(message));
        assertTrue(tmperr.length() == 0);
    }

    @org.junit.Test
    public void slurm_test06_multiJobWithAccountingDisabled() throws Exception {

        String workingDir = getWorkingDir("slurm_test06");

        //custom scheduler with accounting disabled
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(SlurmAdaptor.DISABLE_ACCOUNTING_USAGE, "true");
        Scheduler scheduler = jobs.newScheduler(config.getScheme(), config.getCorrectLocation(),
                config.getDefaultCredential(credentials), properties);

        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);

        Path root = resolve(filesystem, workingDir);
        files.createDirectories(root);

        Path[] out = new Path[5];
        Path[] err = new Path[5];

        Jobs jobs = octopus.jobs();

        Job[] j = new Job[5];

        for (int i = 0; i < j.length; i++) {

            out[i] = resolve(root, "stdout" + i + ".txt");
            err[i] = resolve(root, "stderr" + i + ".txt");

            JobDescription description = new JobDescription();
            description.setExecutable("/bin/sleep");
            description.setArguments("1");
            description.setWorkingDirectory(workingDir);

            description.setQueueName(config.getDefaultQueueName());
            description.setInteractive(false);
            description.setStdin(null);
            description.setStdout("stdout" + i + ".txt");
            description.setStderr("stderr" + i + ".txt");

            j[i] = jobs.submitJob(scheduler, description);
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
                            System.err.println("Job " + i + " failed!");
                            throw new Exception("Job " + i + " failed", status[i].getException());
                        }

                        System.err.println("Job " + i + " done.");
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

        for (int i = 0; i < j.length; i++) {

            String tmpout = readFully(files.newInputStream(out[i]));
            String tmperr = readFully(files.newInputStream(err[i]));

            assertTrue(tmpout != null);
            assertTrue(tmpout.length() == 0);

            assertTrue(tmperr != null);
            assertTrue(tmperr.length() == 0);

            files.delete(out[i]);
            files.delete(err[i]);
        }

        jobs.close(scheduler);
        files.delete(root);
        files.close(filesystem);
    }

    @org.junit.Test
    public void slurm_test06_getDefaultQueue() throws Exception {
        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);

        String reportedDefaultQueueName = jobs.getDefaultQueueName(scheduler);

        assertEquals(config.getDefaultQueueName(), reportedDefaultQueueName);
    }

}
