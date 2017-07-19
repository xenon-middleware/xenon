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
package nl.esciencecenter.xenon.adaptors.schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;
import nl.esciencecenter.xenon.schedulers.NoSuchJobException;
import nl.esciencecenter.xenon.schedulers.Scheduler;
import nl.esciencecenter.xenon.schedulers.SchedulerAdaptorDescription;

public abstract class SchedulerTestParent {

	private Scheduler scheduler;
	private SchedulerAdaptorDescription description;
	private SchedulerLocationConfig locationConfig;

	@Before
	public void setup() throws XenonException {
		scheduler = setupScheduler();
		description = setupDescription();
		locationConfig = setupLocationConfig();
	}

	protected abstract SchedulerLocationConfig setupLocationConfig();

    @After
    public void cleanup() throws XenonException {
    	if (scheduler.isOpen()) { 
    		scheduler.close();
    	}
    }

    public abstract Scheduler setupScheduler() throws XenonException;

    private SchedulerAdaptorDescription setupDescription() throws XenonException {
        String name = scheduler.getAdaptorName();
        return Scheduler.getAdaptorDescription(name);
    }

    @Test
    public void test_close() throws XenonException {
    	scheduler.close();
    	assertFalse(scheduler.isOpen());
    }
    	
    @Test
    public void test_getLocation() throws XenonException {
    	
    	String location = scheduler.getLocation();
    	
    	assertEquals(locationConfig.getLocation(), location);
    }
    
    private boolean contains(String expected, String [] options) { 
    
    	if (options == null || options.length == 0) { 
    		return false;
    	}
    	
		for (String s : options) { 
			if (expected == null) { 
				if (s == null) { 
					return true;
				}
			} else { 
				if (expected.equals(s)) { 
					return true;
				}
			}
		}
    	
		return false;
    }
    
    private boolean unorderedEquals(String [] expected, String [] actual) { 
    	
    	if (expected.length != actual.length) { 
    		return false;
    	}
    	
    	for (String s : expected) {
    		if (!contains(s, actual)) { 
    			return false;
    		}
    	}
    		
    	for (String s : actual) {
     		if (!contains(s, expected)) { 
    			return false;
    		}
    	}
    	
    	return true;
    }
    
    @Test
    public void test_getQueueNames() throws XenonException {
    	assertTrue(unorderedEquals(locationConfig.getQueueNames(), scheduler.getQueueNames()));
    }
  
    
    @Test
    public void test_getDefaultQueueNames() throws XenonException {
    	assertEquals(locationConfig.getDefaultQueueName(), scheduler.getDefaultQueueName());
    }
  
    
    @Test
    public void test_sleep() throws XenonException {

    	JobDescription job = new JobDescription();
    	job.setExecutable("/bin/sleep");
    	job.setArguments("1");
    	
    	String jobID = scheduler.submitBatchJob(job);
    	JobStatus status = scheduler.waitUntilDone(jobID, 5*1000);
    	assertTrue(status.isDone());
    }
    
    @Test
    public void test_cancel() throws XenonException {

    	JobDescription job = new JobDescription();
    	job.setExecutable("/bin/sleep");
    	job.setArguments("120");
    	
    	String jobID = scheduler.submitBatchJob(job);

    	// Wait up to 5 seconds until the job is running
    	JobStatus status = scheduler.waitUntilRunning(jobID, 5*1000);
    	
     	assertTrue(status.isRunning());
     	assertFalse(status.isDone());
         	
     	status = scheduler.cancelJob(jobID);

     	if (!status.isDone()) {
     		// Wait up to 5 seconds until the job is completely done
     		status = scheduler.waitUntilDone(jobID, 5*1000);
     	}
     	
    	System.out.println(status);
     	
    	assertTrue(status.isDone());
    }
    
    /*
    @Test(expected=XenonException.class)
    public void test_waitUntilDone_unknownJobID_throwsException() throws XenonException {
    	scheduler.waitUntilDone("foobar", 5*1000);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void test_waitUntilDone_nullJobID_throwsException() throws XenonException {
    	scheduler.waitUntilDone(null, 5*1000);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void test_waitUntilDone_emptyJobID_throwsException() throws XenonException {
    	scheduler.waitUntilDone("", 5*1000);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void test_waitUntilDone_invalidTimeout_throwsException() throws XenonException {
    	scheduler.waitUntilDone("foobar", -1000);
    }
   
    @Test(expected=XenonException.class)
    public void test_waitUntilRunning_unknownJobID_throwsException() throws XenonException {
    	scheduler.waitUntilRunning("foobar", 5*1000);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void test_waitUntilRunning_nullJobID_throwsException() throws XenonException {
    	scheduler.waitUntilRunning(null, 5*1000);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void test_waitUntilRunning_emptyJobID_throwsException() throws XenonException {
    	scheduler.waitUntilRunning("", 5*1000);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void test_waitUntilRunning_invalidTimeout_throwsException() throws XenonException {
    	scheduler.waitUntilRunning("foobar", -1000);
    }
    
    @Test(expected=XenonException.class)
    public void test_cancel_unknownJobID_throwsException() throws XenonException {
    	scheduler.cancelJob("foobar");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void test_cancel_nullJobID_throwsException() throws XenonException {
    	scheduler.cancelJob(null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void test_cancel_emptyJobID_throwsException() throws XenonException {
    	scheduler.cancelJob("");
    }
    */
    
    
    
    
}
