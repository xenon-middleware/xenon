package nl.esciencecenter.octopus.adaptors.gridengine;

import java.net.URI;
import java.net.URISyntaxException;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GridEngineJobsTest {

    private URI location;

    @Before
    public void setUp() throws Exception {
        location = new URI("ge://fs1.das4.liacs.nl/");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testNewScheduler() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);

        Scheduler scheduler = octopus.jobs().newScheduler(location, null, null);
        
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

        for (int i = 0; i < 300; i++) {
            JobStatus status = octopus.jobs().getJobStatus(job);

            System.out.println(job.getIdentifier() + " has state " + status.getState());

            if (status.getState() == null) {
                return;
            }
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
