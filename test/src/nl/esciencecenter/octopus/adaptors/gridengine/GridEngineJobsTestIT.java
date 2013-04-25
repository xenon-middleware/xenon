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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.exceptions.InvalidLocationException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.UnknownPropertyException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;

import org.junit.Test;

public class GridEngineJobsTestIT {

    private final URI location;

    public GridEngineJobsTestIT() throws URISyntaxException {
        location = new URI("ge://fs1.das4.liacs.nl");
    }

    @Test
    public void testNewScheduler() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);

        Scheduler scheduler = octopus.jobs().newScheduler(location, null, null);

        octopus.jobs().close(scheduler);

        OctopusFactory.endOctopus(octopus);
    }

    @Test
    public void testNewScheduler_emptyPath() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);

        URI withoutSlashLocation = new URI("ge://fs1.das4.liacs.nl");

        Scheduler scheduler = octopus.jobs().newScheduler(withoutSlashLocation, null, null);

        octopus.jobs().close(scheduler);

        OctopusFactory.endOctopus(octopus);
    }

    @Test
    public void testNewScheduler_singleSlashPath() throws Exception {

        Octopus octopus = OctopusFactory.newOctopus(null);

        //path consisting of a single slash (tolerated, ignored)
        URI slashLocation = new URI("ge://fs1.das4.liacs.nl/");

        Scheduler scheduler = octopus.jobs().newScheduler(slashLocation, null, null);

        octopus.jobs().close(scheduler);

        OctopusFactory.endOctopus(octopus);
    }

    @Test(expected = InvalidLocationException.class)
    public void testNewScheduler_invalidPath_InvalidLocationException() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);

        URI brokenLocation = new URI("ge://host/some/path");

        Scheduler scheduler = octopus.jobs().newScheduler(brokenLocation, null, null);

        octopus.jobs().close(scheduler);

        OctopusFactory.endOctopus(octopus);
    }

    @Test(expected = InvalidLocationException.class)
    public void testNewScheduler_uriWithFragment_InvalidLocationException() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);

        URI brokenLocation = new URI("ge://host#the-fragment");

        Scheduler scheduler = octopus.jobs().newScheduler(brokenLocation, null, null);

        octopus.jobs().close(scheduler);

        OctopusFactory.endOctopus(octopus);
    }

    @Test(expected = UnknownPropertyException.class)
    public void testNewScheduler_someProperty_UnknownPropertyException() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);

        Properties properties = new Properties();

        properties.put("some.property", "some.value");

        Scheduler scheduler = octopus.jobs().newScheduler(location, null, properties);

        octopus.jobs().close(scheduler);

        OctopusFactory.endOctopus(octopus);
    }

    @Test
    public void testGetJobs() throws Throwable {
        Octopus octopus = OctopusFactory.newOctopus(null);

        Scheduler scheduler = octopus.jobs().newScheduler(location, null, null);

        Job[] jobs = octopus.jobs().getJobs(scheduler, "all.q");

        for (Job job : jobs) {
            System.out.println(job.getIdentifier());
        }
    }

    @Test
    public void testGetQueueStatus() throws OctopusIOException, OctopusException, URISyntaxException {
        Octopus octopus = OctopusFactory.newOctopus(null);

        Scheduler scheduler = octopus.jobs().newScheduler(location, null, null);

        QueueStatus status = octopus.jobs().getQueueStatus(scheduler, "all.q");

        System.out.println(status.getSchedulerSpecficInformation());

    }

    @Test
    public void testGetQueueStatuses() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);

        Scheduler scheduler = octopus.jobs().newScheduler(location, null, null);

        QueueStatus[] statuses = octopus.jobs().getQueueStatuses(scheduler, scheduler.getQueueNames());

        System.out.println("Status of the queue on " + location + ":");
        System.out.printf("%-15s %-10s     %-10s\n", "name", "total", "available");
        for (QueueStatus status : statuses) {
            System.out.printf("%-15s %-10s     %-10s\n", status.getQueueName(),
                    status.getSchedulerSpecficInformation().get("total"), status.getSchedulerSpecficInformation()
                            .get("available"));
        }
    }

    @Test
    public void testSubmitJob() throws Throwable {
        Octopus octopus = OctopusFactory.newOctopus(null);

        Scheduler scheduler = octopus.jobs().newScheduler(location, null, null);

        JobDescription jobDescription = new JobDescription();

        jobDescription.setExecutable("/bin/sleep");

        jobDescription.setArguments("60");

        //jobDescription.setArguments("this", "and", "that");

        Job job = octopus.jobs().submitJob(scheduler, jobDescription);

        for (int i = 0; i < 100; i++) {
            try {
                JobStatus status = octopus.jobs().getJobStatus(job);

                System.out.println(job.getIdentifier() + " has state " + status.getState());

                if (status.isDone()) {
                    System.out.println("job exit status code was: " + status.getExitCode());
                    System.out.println("job detailed status: " + status.getSchedulerSpecficInformation());
                    return;
                }
            } catch (Exception e) {
                //IGNORE
            }
            Thread.sleep(1000);
        }
    }

    @Test
    public void testGetJobStatus() throws Throwable {
        Octopus octopus = OctopusFactory.newOctopus(null);

        Scheduler scheduler = octopus.jobs().newScheduler(location, null, null);

        Job[] jobs = octopus.jobs().getJobs(scheduler, "all.q");

        for (Job job : jobs) {
            System.out.println(job.getIdentifier() + " has state " + octopus.jobs().getJobStatus(job).getState());
        }
    }

    @Test
    public void testGetJobStatuses() throws Throwable {
        Octopus octopus = OctopusFactory.newOctopus(null);

        Scheduler scheduler = octopus.jobs().newScheduler(location, null, null);

        Job[] jobs = octopus.jobs().getJobs(scheduler, "all.q");

        JobStatus[] statuses = octopus.jobs().getJobStatuses(jobs);

        for (JobStatus status : statuses) {
            System.out.println(status.getJob().getIdentifier() + " has state " + status.getState());
        }
    }

    @Test
    public void testCancelJob() throws Throwable {
        Octopus octopus = OctopusFactory.newOctopus(null);

        Scheduler scheduler = octopus.jobs().newScheduler(location, null, null);

        JobDescription jobDescription = new JobDescription();

        jobDescription.setExecutable("/bin/hostname");

        //jobDescription.setArguments("this", "and", "that");

        Job job = octopus.jobs().submitJob(scheduler, jobDescription);

        octopus.jobs().cancelJob(job);
    }

    @Test
    public void testEnd() throws Throwable {
        Octopus octopus = OctopusFactory.newOctopus(null);

        Scheduler scheduler = octopus.jobs().newScheduler(location, null, null);

        octopus.jobs().close(scheduler);
    }
}
