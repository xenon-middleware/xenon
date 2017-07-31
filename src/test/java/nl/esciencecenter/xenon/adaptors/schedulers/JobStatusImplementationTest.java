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

import nl.esciencecenter.xenon.schedulers.JobStatus;

public class JobStatusImplementationTest {
	
	@Test
	public void test_handle() throws Exception {
		String id = "JOB-42";
		JobStatus s = new JobStatusImplementation(id, "STATE", 0, null, true, false, null);
		assertEquals(id, s.getJobIdentifier());
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_handleFailsNull() throws Exception {
		new JobStatusImplementation(null, "STATE", 0, null, true, false, null);
	}
	
	@Test
	public void test_state() throws Exception {
		String id = "JOB-42";
		JobStatus s = new JobStatusImplementation(id, "STATE", 0, null, true, false, null);
		assertEquals("STATE", s.getState());
	}

	@Test
	public void test_exit() throws Exception {
		String id = "JOB-42";
		JobStatus s = new JobStatusImplementation(id, "STATE", 42, null, true, false, null);
		assertEquals(new Integer(42), s.getExitCode());
	}

	@Test
	public void test_exception() throws Exception {
		String id = "JOB-42";
		Exception e = new NullPointerException("EEP");
		JobStatus s = new JobStatusImplementation(id, "STATE", 0, e, true, false, null);
		assertEquals(e, s.getException());
	}
	
	@Test
	public void test_hasExceptionTrue() throws Exception {
		String id = "JOB-42";
		Exception e = new NullPointerException("EEP");
		JobStatus s = new JobStatusImplementation(id, "STATE", 0, e, true, false, null);
		assertTrue(s.hasException());
	}

	@Test
	public void test_hasExceptionFalse() throws Exception {
		String id = "JOB-42";		
		JobStatus s = new JobStatusImplementation(id, "STATE", 0, null, true, false, null);
		assertFalse(s.hasException());
	}

	@Test
	public void test_runningTrue() throws Exception {
		String id = "JOB-42";
		JobStatus s = new JobStatusImplementation(id, "STATE", 0, null, true, false, null);
		assertTrue(s.isRunning());
	}

	@Test
	public void test_runningFalse() throws Exception {
		String id = "JOB-42";
		JobStatus s = new JobStatusImplementation(id, "STATE", 0, null, false, false, null);
		assertFalse(s.isRunning());
	}
	
	@Test
	public void test_doneTrue() throws Exception {
		String id = "JOB-42";
		JobStatus s = new JobStatusImplementation(id, "STATE", 0, null, false, true, null);
		assertTrue(s.isDone());
	}

	@Test
	public void test_doneFalse() throws Exception {
		String id = "JOB-42";
		JobStatus s = new JobStatusImplementation(id, "STATE", 0, null, false, false, null);
		assertFalse(s.isRunning());
	}

	@Test
	public void test_Properties() throws Exception {
		HashMap<String, String> tmp = new HashMap<>();
		tmp.put("key", "value");

		String id = "JOB-42";
		JobStatus s = new JobStatusImplementation(id, "STATE", 0, null, false, false, tmp);
		assertEquals(tmp, s.getSchedulerSpecficInformation());
	}

	@Test
	public void test_toString() throws Exception {

		HashMap<String, String> tmp = new HashMap<>();
		tmp.put("key", "value");
		
		String id = "JOB-42";
		Exception e = new NullPointerException("EEP");
		JobStatus s = new JobStatusImplementation(id, "STATE", 0, e, false, false, tmp);
		
		String expected = "JobStatus [jobIdentifier=" + id + ", state=" + "STATE" + ", exitCode=" + 0 + ", exception=" + e
                + ", running=" + false + ", done=" + false + ", schedulerSpecificInformation=" + tmp + "]";
		
		assertEquals(expected, s.toString());		
	}

	@Test
	public void test_hashcode() {
		JobStatus a = new JobStatusImplementation("JOB-42", "STATE", 0, null, true, false, null);
		JobStatus b = new JobStatusImplementation("JOB-42", "STATE", 0, null, true, false, null);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void test_equals() {
		JobStatus a = new JobStatusImplementation("JOB-42", "STATE", 0, null, true, false, null);
		JobStatus b = new JobStatusImplementation("JOB-42", "STATE", 0, null, true, false, null);
		assertTrue(a.equals(b));
	}
}
