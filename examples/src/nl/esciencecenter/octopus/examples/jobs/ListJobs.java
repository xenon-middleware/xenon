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
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.Jobs;

import nl.esciencecenter.octopus.jobs.Scheduler;
/**
 * A simple example of how to retrieve the job status.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class ListJobs {

    public static void main(String [] args) {
        
        if (args.length != 1) { 
            System.out.println("Example required a scheduler URI as a parameter!");
            System.exit(1);
        }
        
        try { 
            // Convert the command line parameter to a URI
            URI location = new URI(args[0]);
            
            // We create a new octopus using the OctopusFactory (without providing any properties).
            Octopus octopus = OctopusFactory.newOctopus(null);

            // Next, we retrieve the Jobs and Credentials API
            Jobs jobs = octopus.jobs();
            
            // Create a scheduler to run the job
            Scheduler scheduler = jobs.newScheduler(location, null, null);

            // Retrieve all jobs of all queues.
            Job [] result = jobs.getJobs(scheduler);
            
            // Print the result
            System.out.println("The scheduler at " + location + " has " + result.length + " jobs:");
            
            for (Job j : result) {
                System.out.println("  " + j.getIdentifier());
            }
            
            // Close the scheduler
            jobs.close(scheduler);
            
            // Finally, we end octopus to release all resources 
            OctopusFactory.endOctopus(octopus);

        } catch (Exception e) { 
            System.out.println("ListQueueStatus example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

