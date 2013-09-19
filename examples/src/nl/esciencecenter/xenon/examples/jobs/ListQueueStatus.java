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
import nl.esciencecenter.xenon.jobs.Jobs;
import nl.esciencecenter.xenon.jobs.QueueStatus;
import nl.esciencecenter.xenon.jobs.Scheduler;

/**
 * An example of how to retrieve the queue status.
 * 
 * This example assumes the user provides a URI with the scheduler location on the command line.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class ListQueueStatus {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Example required a scheduler URI as a parameter!");
            System.exit(1);
        }

        try {
            // Convert the command line parameter to a URI
            URI location = new URI(args[0]);

            // We create a new Cobalt using the CobaltFactory (without providing any properties).
            Xenon cobalt = XenonFactory.newXenon(null);

            // Next, we retrieve the Jobs and Credentials API
            Jobs jobs = cobalt.jobs();

            // Create a scheduler to run the job
            Scheduler scheduler = jobs.newScheduler(location.getScheme(), location.getAuthority(), null, null);

            // Retrieve the status of all queues.
            QueueStatus[] result = jobs.getQueueStatuses(scheduler);

            // Print the result
            System.out.println("The scheduler at " + location + " has " + result.length + " queues:");

            for (QueueStatus q : result) {
                System.out.println("  " + q.getQueueName());
            }

            // Close the scheduler
            jobs.close(scheduler);

            // Finally, we end Cobalt to release all resources 
            XenonFactory.endXenon(cobalt);

        } catch (URISyntaxException | XenonException e) {
            System.out.println("ListQueueStatus example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
