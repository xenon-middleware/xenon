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

package nl.esciencecenter.octopus.integration;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;

import org.junit.Test;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class JobTimeoutTest {

    @Test
    public void test() throws Exception {

        JobDescription description = new JobDescription();
        description.setExecutable("/bin/sleep");
        description.setArguments("60s");
        description.setQueueName("single");
        description.setMaxTime(1);
        description.setWorkingDirectory(System.getProperty("java.io.tmpdir"));

        Octopus octopus = OctopusFactory.newOctopus(null);
        Jobs jobs = octopus.jobs();

        Job job = jobs.submitJob(jobs.getLocalScheduler(), description);

        long time = System.currentTimeMillis();

        JobStatus status = jobs.getJobStatus(job);

        while (!status.isDone()) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignored
            }

            status = jobs.getJobStatus(job);

            if ((System.currentTimeMillis() - time) > 3 * 60 * 1000) {
                throw new Exception("Job failed to terminate within 3 minutes!");
            }
        }

        long deltat = System.currentTimeMillis() - time;

        System.out.println("Job terminated after " + (deltat / 1000.0) + " sec.");

        if (deltat > 65 * 1000) {
            throw new Exception("Job terminated after " + (deltat / 1000.0) + " seconds (not 60)");
        }

        OctopusFactory.endAll();
    }
}
