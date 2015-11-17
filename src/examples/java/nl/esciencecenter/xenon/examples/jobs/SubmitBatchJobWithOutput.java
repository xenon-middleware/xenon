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

package nl.esciencecenter.xenon.examples.jobs;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.Jobs;
import nl.esciencecenter.xenon.jobs.Scheduler;
import nl.esciencecenter.xenon.util.Utils;

/**
 * An example of how to create and submit a batch job that produces output. 
 * 
 * This example assumes the user provides a URI with the scheduler location on the command line.
 * 
 * Note: this example assumes the job is submitted to a machine Linux machine, as it tries to run "/bin/hostname".
 * 
 * @version 1.0
 * @since 1.0
 */
public class SubmitBatchJobWithOutput {

    public static void main(String[] args) {
        try {
            // Convert the command line parameter to a URI
            URI location = new URI(args[0]);

            // We create a new Xenon using the XenonFactory (without providing any properties).
            Xenon xenon = XenonFactory.newXenon(null);

            // Next, we retrieve the Files and Jobs API
            Files files = xenon.files();
            Jobs jobs = xenon.jobs();

            // We can now create a JobDescription for the job we want to run.
            JobDescription description = new JobDescription();
            description.setExecutable("/bin/hostname");
            description.setArguments("--long");
            description.setStdout("stdout.txt");
            description.setStderr("stderr.txt");

            // Create a scheduler to run the job
            Scheduler scheduler = jobs.newScheduler(location.getScheme(), location.getAuthority(), null, null);

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
                
                // Access output using local or SSH as a scheme.
                Path workingDir;
                
                if (location.getScheme().equals("local") || location.getAuthority() == null) { 
                    workingDir = Utils.getLocalCWD(files);
                } else { 
                    FileSystem fs = files.newFileSystem("ssh", location.getAuthority(), null, null);
                    workingDir = fs.getEntryPath(); 
                }
               
                System.out.println("Job ran succesfully and produced:");

                Path stdout = Utils.resolveWithRoot(files, workingDir, "stdout.txt");
                Path stderr = Utils.resolveWithRoot(files, workingDir, "stderr.txt");

                if (files.exists(stdout)) {
                    String output = Utils.readToString(files, stdout, Charset.defaultCharset());
                    System.out.println(" STDOUT: " + output);
                    files.delete(stdout);
                }

                if (files.exists(stderr)) {
                    String output = Utils.readToString(files, stderr, Charset.defaultCharset());
                    System.out.println(" STDERR: " + output);
                    files.delete(stderr);
                }
            }

            // Close the scheduler
            jobs.close(scheduler);

            // Finally, we end Xenon to release all resources 
            XenonFactory.endXenon(xenon);

        } catch (URISyntaxException | XenonException e)  {
            System.out.println("SubmitBatchJob example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
