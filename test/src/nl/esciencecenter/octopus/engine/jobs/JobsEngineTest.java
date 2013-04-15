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
package nl.esciencecenter.octopus.engine.jobs;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;

import java.net.URI;
import java.net.URISyntaxException;

import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.Jobs;

import org.junit.Test;

public class JobsEngineTest {

    @Test
    public void testJobsEngine() {
        fail("Not yet implemented");
    }

    @Test
    public void testNewScheduler() {
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
    public void testNewJobDescription() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetQueueNames() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetJobs() {
        fail("Not yet implemented");
    }

    @Test
    public void testSubmitJob_StubbedLocalAdaptor_LocalJob() throws OctopusIOException, OctopusException, URISyntaxException {
        URI sheduler_location = new URI("local:///");
        JobDescription job_description = new JobDescription();
        SchedulerImplementation scheduler =
                new SchedulerImplementation("local", "1", sheduler_location, new String[] { "single" }, null, null, true, false);
        // stub adaptor
        OctopusEngine octopus = mock(OctopusEngine.class);
        Adaptor adaptor = mock(Adaptor.class);
        Jobs job_adaptor = mock(Jobs.class);
        Job expected_job = new JobImplementation(job_description, scheduler, null);
        when(octopus.getAdaptorFor("local")).thenReturn(adaptor);
        when(octopus.getAdaptor("local")).thenReturn(adaptor);
        when(adaptor.jobsAdaptor()).thenReturn(job_adaptor);
        when(job_adaptor.newScheduler(sheduler_location, null, null)).thenReturn(scheduler);
        when(job_adaptor.submitJob(scheduler, job_description)).thenReturn(expected_job);
        JobsEngine engine = new JobsEngine(octopus);

        Job job = engine.submitJob(scheduler, job_description);

        assertThat(job, is(expected_job));
    }
}
