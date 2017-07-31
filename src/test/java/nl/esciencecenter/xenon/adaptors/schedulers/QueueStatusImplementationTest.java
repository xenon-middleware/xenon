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
package nl.esciencecenter.xenon.adaptors.schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import nl.esciencecenter.xenon.schedulers.MockScheduler;
import nl.esciencecenter.xenon.schedulers.QueueStatus;

public class QueueStatusImplementationTest {

    @Test
    public void test_scheduler() throws Exception {
        MockScheduler s = new MockScheduler("ID", "TEST", "MEM", true, true, true, null);
        QueueStatus stat = new QueueStatusImplementation(s, "Q", null, null);
        assertEquals(s,  stat.getScheduler());
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_schedulerFailsNull() throws Exception {
        new QueueStatusImplementation(null, "Q", null, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_queueNameFailsNull() throws Exception {
        MockScheduler s = new MockScheduler("ID", "TEST", "MEM", true, true, true, null);
        new QueueStatusImplementation(s, null, null, null);
    }

    @Test
    public void test_queue() throws Exception {
        MockScheduler s = new MockScheduler("ID", "TEST", "MEM", true, true, true, null);
        QueueStatus stat = new QueueStatusImplementation(s, "Q", null, null);
        assertEquals("Q",  stat.getQueueName());
    }

    @Test
    public void test_exeption() throws Exception {
        MockScheduler s = new MockScheduler("ID", "TEST", "MEM", true, true, true, null);
        Exception e = new NullPointerException("aap");
        QueueStatus stat = new QueueStatusImplementation(s, "Q", e, null);
        assertEquals(e,  stat.getException());
    }

    @Test
    public void test_info() throws Exception {
        HashMap<String, String> tmp = new HashMap<>();
        tmp.put("key", "value");

        MockScheduler s = new MockScheduler("ID", "TEST", "MEM", true, true, true, null);
        Exception e = new NullPointerException("aap");
        QueueStatus stat = new QueueStatusImplementation(s, "Q", e, tmp);
        assertEquals(tmp,  stat.getSchedulerSpecficInformation());
    }

    @Test
    public void test_hasExeptionTrue() throws Exception {
        MockScheduler s = new MockScheduler("ID", "TEST", "MEM", true, true, true, null);
        Exception e = new NullPointerException("aap");
        QueueStatus stat = new QueueStatusImplementation(s, "Q", e, null);
        assertTrue(stat.hasException());
    }

    @Test
    public void test_hasExeptionFalse() throws Exception {
        MockScheduler s = new MockScheduler("ID", "TEST", "MEM", true, true, true, null);
        QueueStatus stat = new QueueStatusImplementation(s, "Q", null, null);
        assertFalse(stat.hasException());
    }

    @Test
    public void test_toString() throws Exception {
        HashMap<String, String> tmp = new HashMap<>();
        tmp.put("key", "value");
        MockScheduler s = new MockScheduler("ID", "TEST", "MEM", true, true, true, null);
        Exception e = new NullPointerException("aap");
        QueueStatus stat = new QueueStatusImplementation(s, "Q", e, tmp);

        String expected = "QueueStatus [scheduler=" + s + ", queueName=" + "Q" + ", exception=" + e
                 + ", schedulerSpecificInformation=" + tmp + "]";

        assertEquals(expected, stat.toString());
    }



}
