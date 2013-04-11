package nl.esciencecenter.octopus.adaptors.gridengine;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GridEngineJobsTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testNewScheduler() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);
        URI location = new URI("ge://fs0.das4.cs.vu.nl/");

        Scheduler scheduler = octopus.jobs().newScheduler(location, null, null);
    }

    @Test
    public void testGetJobs() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetQueueStatus() throws OctopusIOException, OctopusException, URISyntaxException {
        Octopus octopus = OctopusFactory.newOctopus(null);
        URI location = new URI("ge://fs0.das4.cs.vu.nl/");

        Scheduler scheduler = octopus.jobs().newScheduler(location, null, null);

        QueueStatus status = octopus.jobs().getQueueStatus(scheduler, "all.q");

        System.out.println(status.getSchedulerSpecficInformation());

    }

    @Test
    public void testGetQueueStatuses() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);
        URI location = new URI("ge://fs0.das4.cs.vu.nl/");

        Scheduler scheduler = octopus.jobs().newScheduler(location, null, null);

        QueueStatus[] statuses = octopus.jobs().getQueueStatuses(scheduler, scheduler.getQueueNames());

        System.out.println("Status of the queue on " + location + ":");
        System.out.printf("%-15s %-10s     %-10s\n", "name","total","available");
        for (QueueStatus status : statuses) {
            System.out.printf("%-15s %-10s     %-10s\n", status.getQueueName(), status.getSchedulerSpecficInformation().get("total"),
                    status.getSchedulerSpecficInformation().get("available"));
        }
    }

    @Test
    public void testSubmitJob() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetJobStatus() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetJobStatuses() {
        fail("Not yet implemented");
    }

    @Test
    public void testCancelJob() {
        fail("Not yet implemented");
    }

    @Test
    public void testEnd() {
        fail("Not yet implemented");
    }

    @Test
    public void testNewJobDescription() {
        fail("Not yet implemented");
    }

}
