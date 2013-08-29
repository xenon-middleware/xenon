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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.jobs.Scheduler;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class QueueStatusImplementationTest {

    @org.junit.Test
    public void test_constructor0() throws Exception {

        Scheduler s = new SchedulerImplementation("test", "id1", "test", "", new String[] { "testq" }, null, null, true,
                true, true);

        new QueueStatusImplementation(s, "testq", null, null);
    }

    @org.junit.Test(expected = IllegalArgumentException.class)
    public void test_constructor1() throws Exception {
        new QueueStatusImplementation(null, "testq", null, null);
    }

    @org.junit.Test(expected = IllegalArgumentException.class)
    public void test_constructor2() throws Exception {

        Scheduler s = new SchedulerImplementation("test", "id1", "test", "", new String[] { "testq" }, null, null, true,
                true, true);

        new QueueStatusImplementation(s, null, null, null);
    }

    @org.junit.Test
    public void test_getters_and_setters() throws Exception {

        Scheduler s = new SchedulerImplementation("test", "id1", "test", "", new String[] { "testq" }, null, null, true,
                true, true);

        Exception e = new Exception("Test Exception");

        Map<String, String> info = new HashMap<>();
        info.put("key1", "value1");

        QueueStatusImplementation qsi = new QueueStatusImplementation(s, "testq", e, info);

        assertEquals(s, qsi.getScheduler());
        assertEquals("testq", qsi.getQueueName());
        assertEquals(e, qsi.getException());
        assertEquals(info, qsi.getSchedulerSpecficInformation());

        assertTrue(qsi.hasException());
    }

    @org.junit.Test
    public void test_hasException() throws Exception {

        Scheduler s = new SchedulerImplementation("test", "id1", "test", "", new String[] { "testq" }, null, null, true,
                true, true);

        Exception e = new Exception("Test Exception");

        Map<String, String> info = new HashMap<>();
        info.put("key1", "value1");

        QueueStatusImplementation qsi = new QueueStatusImplementation(s, "testq", e, info);

        assertTrue(qsi.hasException());

        qsi = new QueueStatusImplementation(s, "testq", null, info);

        assertFalse(qsi.hasException());
    }

    @org.junit.Test
    public void test_toString() throws Exception {

        Scheduler s = new SchedulerImplementation("test", "id1", "test", "", new String[] { "testq" }, null, null, true,
                true, true);

        Exception e = new Exception("Test Exception");

        Map<String, String> info = new HashMap<>();
        info.put("key1", "value1");

        QueueStatusImplementation qsi = new QueueStatusImplementation(s, "testq", e, info);

        assertTrue(qsi.toString().equals(
                "QueueStatusImplementation [scheduler=" + s.toString() + ", queueName=testq, " + "exception=" + e.toString()
                        + ", schedulerSpecificInformation=" + info.toString() + "]"));
    }
}
