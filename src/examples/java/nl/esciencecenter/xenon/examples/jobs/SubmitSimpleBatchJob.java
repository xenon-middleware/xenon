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

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.Jobs;
import nl.esciencecenter.xenon.jobs.Scheduler;

/**
 * An example of how to create and submit a simple batch job that does not produce output. 
 * 
 * This example assumes the user provides a URI with the scheduler location on the command line.
 * 
 * Note: this example assumes the job is submitted to a machine Linux machine, as it tries to run "/bin/sleep".
 * 
 * @version 1.0
 * @since 1.0
 */
public class SubmitSimpleBatchJob {

    public static void main(String[] args) {
        try {
            // Convert the command line parameter to a URI
            URI location = new URI(args[0]);

            // We create a new Xenon using the XenonFactory (without providing any properties).
            Xenon xenon = XenonFactory.newXenon(null);

            // Next, we retrieve the Jobs API
            Jobs jobs = xenon.jobs();

            // We can now create a JobDescription for the job we want to run.
            JobDescription description = new JobDescription();
            description.setExecutable("/bin/sleep");
            description.setArguments("5");

            // Create a scheduler to run the job
            Scheduler scheduler = jobs.newScheduler(location.getScheme(), location.getAuthority(), null, null);

            // Submit the job
            Job job = jobs.submitJob(scheduler, description);

            // Wait for the job to finish
            JobStatus status = jobs.waitUntilDone(job, 60000);

            // Check if the job was successful. 
            if (!status.isDone()) {
                System.out.println("Job failed to run within deadline.");
            } else if (status.hasException()) {
                Exception e = status.getException();
                System.out.println("Job produced an exception: " + e.getMessage());
                e.printStackTrace();
            } else {
                System.out.println("Job ran succesfully!");
            }

            // Close the scheduler
            jobs.close(scheduler);

            // Finally, we end Xenon to release all resources 
            XenonFactory.endXenon(xenon);

        } catch (URISyntaxException | XenonException e) {
            System.out.println("SubmitBatchJob example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
