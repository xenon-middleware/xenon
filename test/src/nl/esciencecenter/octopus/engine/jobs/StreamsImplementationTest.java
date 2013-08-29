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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.Scheduler;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class StreamsImplementationTest {

    @org.junit.Test
    public void test_getters() throws Exception {

        JobDescription desc = new JobDescription();

        Scheduler s = new SchedulerImplementation("test", "id1", "test", "", new String[] { "testq" }, null, null, true,
                true, true);

        Job j = new JobImplementation(s, "id1", desc, true, true);

        OutputStream stdin = new ByteArrayOutputStream();
        InputStream stdout = new ByteArrayInputStream(new byte[42]);
        InputStream stderr = new ByteArrayInputStream(new byte[42]);

        StreamsImplementation si = new StreamsImplementation(j, stdout, stdin, stderr);

        assertTrue(si.getStderr() == stderr);
        assertTrue(si.getStdout() == stdout);
        assertTrue(si.getStdin() == stdin);

        assertEquals(j, si.getJob());
    }

}
