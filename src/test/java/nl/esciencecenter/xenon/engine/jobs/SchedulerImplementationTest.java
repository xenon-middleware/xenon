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
package nl.esciencecenter.xenon.engine.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;

import nl.esciencecenter.xenon.engine.jobs.SchedulerImplementation;

/**
 * 
 */
public class SchedulerImplementationTest {

    @org.junit.Test
    public void test_constructor0() throws Exception {
        new SchedulerImplementation("test", "id1", "", new String[] { "aap", "noot" }, null, null, true, true,
                true);
    }

    @org.junit.Test(expected = IllegalArgumentException.class)
    public void test_constructor1() throws Exception {
        new SchedulerImplementation(null, "id1", "", new String[] { "aap", "noot" }, null, null, true, true,
                true);
    }

    @org.junit.Test(expected = IllegalArgumentException.class)
    public void test_constructor2() throws Exception {
        new SchedulerImplementation("test", null, "", new String[] { "aap", "noot" }, null, null, true, true,
                true);
    }

    @org.junit.Test
    public void test_getters_and_setters() throws Exception {

        SchedulerImplementation si = new SchedulerImplementation("test", "id1", "",
                new String[] { "aap", "noot" }, null, null, true, true, true);

        assertEquals("test", si.getAdaptorName());
        assertEquals("id1", si.getUniqueID());
        assertEquals(null, si.getCredential());
        assertEquals(new HashMap<String, String>(), si.getProperties());
        assertEquals("", si.getLocation());
        
        assertTrue(Arrays.equals(new String[] { "aap", "noot" }, si.getQueueNames()));

        assertTrue(si.isOnline());
        assertTrue(si.supportsInteractive());
        assertTrue(si.supportsBatch());
    }

    @org.junit.Test
    public void test_toString() throws Exception {

        SchedulerImplementation si = new SchedulerImplementation("test", "id1", "",
                new String[] { "aap", "noot" }, null, null, true, true, true);

        System.err.println("SI: " + si.toString());

        assertTrue(si.toString().equals(
                "SchedulerImplementation [uniqueID=id1, adaptorName=test, location=, properties={}, " +
                "queueNames=[aap, noot], isOnline=true, supportsInteractive=true, supportsBatch=true]"));
    }

    @org.junit.Test
    public void test_hashcode_equals() throws Exception {

        SchedulerImplementation si = new SchedulerImplementation("test", "id1", "",
                new String[] { "aap", "noot" }, null, null, true, true, true);

        int hash = (31 + "test".hashCode()) * 31 + "id1".hashCode();

        assertTrue(hash == si.hashCode());

        assertTrue(si.equals(si));
        assertFalse(si.equals(null));
        assertFalse(si.equals("AAP"));

        SchedulerImplementation si2 = new SchedulerImplementation("test2", "id1", "", new String[] { "aap",
                "noot" }, null, null, true, true, true);

        assertFalse(si.equals(si2));

        SchedulerImplementation si3 = new SchedulerImplementation("test", "id2", "", new String[] { "aap",
                "noot" }, null, null, true, true, true);

        assertFalse(si.equals(si3));

        SchedulerImplementation si4 = new SchedulerImplementation("test", "id1", "", new String[] { "aap",
                "noot" }, null, null, true, true, true);

        assertTrue(si.equals(si4));
    }
}