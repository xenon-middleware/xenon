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

package nl.esciencecenter.octopus.examples.jobs;

import java.net.URI;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.util.FileUtils;

/**
 * A simple example of how to create an octopus and how to retrieve the various interfaces.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class SubmitBatchJobWithOutput {

    public static void main(String[] args) {
        try {

            // We create a new octopus using the OctopusFactory (without providing any properties).
            Octopus octopus = OctopusFactory.newOctopus(null);

            // Next, we retrieve the Files and Jobs API
            Files files = octopus.files();
            Jobs jobs = octopus.jobs();

            // We can now create a JobDescription for the job we want to run.
            JobDescription description = new JobDescription();
            description.setExecutable("/bin/uname");
            description.setArguments("-a");
            description.setStdout("stdout.txt");
            description.setStderr("stderr.txt");

            // Create a scheduler to run the job
            Scheduler scheduler = jobs.newScheduler(new URI("local:///"), null, null);

            // Submit the job
            Job job = jobs.submitJob(scheduler, description);

            // Wait for the job to finish
            JobStatus status = jobs.waitUntilDone(job, 60000);

            // Check if the job was successful. 
            if (!status.isDone()) {
                System.out.println("Job failed to run withing deadline.");
            } else if (status.hasException()) {
                Exception e = status.getException();
                System.out.println("Job produced an exception: " + e.getMessage());
                e.printStackTrace();
            } else {

                System.out.println("Job ran succesfully and produced:");

                FileSystem fs = files.getLocalCWDFileSystem();
                AbsolutePath stdout = fs.getEntryPath().resolve(new RelativePath("stdout.txt"));
                AbsolutePath stderr = fs.getEntryPath().resolve(new RelativePath("stderr.txt"));

                if (files.exists(stdout)) {
                    String output = new String(FileUtils.readAllBytes(files, stdout));
                    System.out.println(" STDOUT: " + output);
                    files.delete(stdout);
                }

                if (files.exists(stderr)) {
                    String output = new String(FileUtils.readAllBytes(files, stderr));
                    System.out.println(" STDERR: " + output);
                    files.delete(stderr);
                }
            }

            // Close the scheduler
            jobs.close(scheduler);

            // Finally, we end octopus to release all resources 
            OctopusFactory.endOctopus(octopus);

        } catch (Exception e) {
            System.out.println("SubmitBatchJob example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
