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
import java.util.UUID;

import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.Scheduler;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class JobStatusImplementationTest {
    
    
    @org.junit.Test
    public void test_constructor0() throws Exception {
        
        JobDescription desc = new JobDescription();

        Scheduler s = new SchedulerImplementation("test", "id1", new URI("test:///"), new String [] { "testq" }, null, 
                null, true, true, true);     
        
        UUID uuid = UUID.randomUUID();
        
        Job j = new JobImplementation(desc, s, uuid, "id1", true, true);
        
        Exception e = new Exception("Test Exception");
        
        Map<String,String> info = new HashMap<>();
        info.put("key1", "value1");
        
        new JobStatusImplementation(j, "STATE", 42, e, true, false, info);
    }
        
    @org.junit.Test(expected = IllegalArgumentException.class)
    public void test_constructor1() throws Exception {
        new JobStatusImplementation(null, "STATE", 42, null, true, false, null);
    }
    
    @org.junit.Test
    public void test_getters_and_setters() throws Exception {

        JobDescription desc = new JobDescription();

        Scheduler s = new SchedulerImplementation("test", "id1", new URI("test:///"), new String [] { "testq" }, null, 
                null, true, true, true);     
        
        UUID uuid = UUID.randomUUID();
        
        Job j = new JobImplementation(desc, s, uuid, "id1", true, true);
        
        Exception e = new Exception("Test Exception");
        
        Map<String,String> info = new HashMap<>();
        info.put("key1", "value1");
        
        JobStatusImplementation jsi = new JobStatusImplementation(j, "STATE", 42, e, true, false, info);
        
        assertEquals(j, jsi.getJob());
        assertEquals("STATE", jsi.getState());
        assertEquals(new Integer(42), jsi.getExitCode());
        assertEquals(e, jsi.getException());
        assertEquals(info, jsi.getSchedulerSpecficInformation());
        assertTrue(jsi.isRunning());
        assertFalse(jsi.isDone());                
    } 
    
    @org.junit.Test
    public void test_toString() throws Exception {

        JobDescription desc = new JobDescription();

        Scheduler s = new SchedulerImplementation("test", "id1", new URI("test:///"), new String [] { "testq" }, null, 
                null, true, true, true);     
        
        UUID uuid = UUID.randomUUID();
        
        Job j = new JobImplementation(desc, s, uuid, "id1", true, true);
        
        Exception e = new Exception("Test Exception");
        
        Map<String,String> info = new HashMap<>();
        info.put("key1", "value1");
        
        JobStatusImplementation jsi = new JobStatusImplementation(j, "STATE", 42, e, true, false, info);
                    
        assertTrue(jsi.toString().equals("JobStatusImplementation [job=" + j + ", state=STATE, exitCode=42, exception=" + e
                + ", done=false, schedulerSpecificInformation=" + info + "]"));
    }
}
