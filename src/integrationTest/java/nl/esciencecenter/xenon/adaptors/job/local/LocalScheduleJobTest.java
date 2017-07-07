/**
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
package nl.esciencecenter.xenon.adaptors.job.local;

import static org.junit.Assert.assertTrue;

import nl.esciencecenter.xenon.adaptors.GenericScheduleJobTestParent;
import nl.esciencecenter.xenon.adaptors.schedulers.local.LocalJobTestConfig;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 */
public class LocalScheduleJobTest extends GenericScheduleJobTestParent {

    @BeforeClass
    public static void prepareLocalJobAdaptorTest() throws Exception {
        GenericScheduleJobTestParent.prepareClass(new LocalJobTestConfig());
    }

    @AfterClass
    public static void cleanupAltLocalJobsTest() throws Exception {
        GenericScheduleJobTestParent.cleanupClass();
    }

    @Test
    public void test_jobTimeout() throws Exception {
        job = jobs.submitJob(scheduler, timedJobDescription(null, 10));
        long time = System.currentTimeMillis();

        JobStatus status = jobs.waitUntilDone(job, 3 * 10 * 1000);
        // check if the job terminated within 30 seconds
        checkJobDone(status);

        // check what the timeout was
        long deltat = System.currentTimeMillis() - time;
        assertTrue("Job terminated after " + (deltat / 1000.0) + " seconds (not 10)", deltat <= 15 * 1000);
    }
}
