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

package nl.esciencecenter.xenon.adaptors.slurm;

import static org.junit.Assert.assertEquals;

import nl.esciencecenter.xenon.adaptors.GenericJobAdaptorTestParent;
import nl.esciencecenter.xenon.jobs.Scheduler;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SlurmJobAdaptorTest extends GenericJobAdaptorTestParent {

    private static final Logger logger = LoggerFactory.getLogger(SlurmJobAdaptorTest.class);

    @BeforeClass
    public static void prepareGridEngineJobAdaptorTest() throws Exception {
        GenericJobAdaptorTestParent.prepareClass(new SlurmJobTestConfig(null));
    }

    @AfterClass
    public static void cleanupGridEngineJobAdaptorTest() throws Exception {
        GenericJobAdaptorTestParent.cleanupClass();
    }

    @Test
    public void slurm_test06_getDefaultQueue() throws Exception {
        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);

        String reportedDefaultQueueName = jobs.getDefaultQueueName(scheduler);

        assertEquals(config.getDefaultQueueName(), reportedDefaultQueueName);

        jobs.close(scheduler);
    }
}
