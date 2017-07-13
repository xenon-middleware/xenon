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
package nl.esciencecenter.xenon.schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

public class JobStatusTest {
	
	class FakeJobHandle implements JobHandle {

		@Override
		public JobDescription getJobDescription() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Scheduler getScheduler() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getIdentifier() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	@Test
	public void test_handle() throws Exception {
		JobHandle h = new FakeJobHandle();
		JobStatus s = new JobStatus(h, "STATE", 0, null, true, false, null);
		assertEquals(h, s.getJob());
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_handleFailsNull() throws Exception {
		new JobStatus(null, "STATE", 0, null, true, false, null);
	}
	
	@Test
	public void test_state() throws Exception {
		JobHandle h = new FakeJobHandle();
		JobStatus s = new JobStatus(h, "STATE", 0, null, true, false, null);
		assertEquals("STATE", s.getState());
	}

	@Test
	public void test_exit() throws Exception {
		JobHandle h = new FakeJobHandle();
		JobStatus s = new JobStatus(h, "STATE", 42, null, true, false, null);
		assertEquals(new Integer(42), s.getExitCode());
	}

	@Test
	public void test_exception() throws Exception {
		JobHandle h = new FakeJobHandle();
		Exception e = new NullPointerException("EEP");
		JobStatus s = new JobStatus(h, "STATE", 0, e, true, false, null);
		assertEquals(e, s.getException());
	}
	
	@Test
	public void test_hasExceptionTrue() throws Exception {
		JobHandle h = new FakeJobHandle();
		Exception e = new NullPointerException("EEP");
		JobStatus s = new JobStatus(h, "STATE", 0, e, true, false, null);
		assertTrue(s.hasException());
	}

	@Test
	public void test_hasExceptionFalse() throws Exception {
		JobHandle h = new FakeJobHandle();		
		JobStatus s = new JobStatus(h, "STATE", 0, null, true, false, null);
		assertFalse(s.hasException());
	}

	@Test
	public void test_runningTrue() throws Exception {
		JobHandle h = new FakeJobHandle();
		JobStatus s = new JobStatus(h, "STATE", 0, null, true, false, null);
		assertTrue(s.isRunning());
	}

	@Test
	public void test_runningFalse() throws Exception {
		JobHandle h = new FakeJobHandle();
		JobStatus s = new JobStatus(h, "STATE", 0, null, false, false, null);
		assertFalse(s.isRunning());
	}
	
	@Test
	public void test_doneTrue() throws Exception {
		JobHandle h = new FakeJobHandle();
		JobStatus s = new JobStatus(h, "STATE", 0, null, false, true, null);
		assertTrue(s.isDone());
	}

	@Test
	public void test_doneFalse() throws Exception {
		JobHandle h = new FakeJobHandle();
		JobStatus s = new JobStatus(h, "STATE", 0, null, false, false, null);
		assertFalse(s.isRunning());
	}

	@Test
	public void test_Properties() throws Exception {
		HashMap<String, String> tmp = new HashMap<>();
		tmp.put("key", "value");

		JobHandle h = new FakeJobHandle();
		JobStatus s = new JobStatus(h, "STATE", 0, null, false, false, tmp);
		assertEquals(tmp, s.getSchedulerSpecficInformation());
	}

	@Test
	public void test_toString() throws Exception {

		HashMap<String, String> tmp = new HashMap<>();
		tmp.put("key", "value");
		
		JobHandle h = new FakeJobHandle();
		Exception e = new NullPointerException("EEP");
		JobStatus s = new JobStatus(h, "STATE", 0, e, false, false, tmp);
		
		String expected = "JobStatus [job=" + h + ", state=" + "STATE" + ", exitCode=" + 0 + ", exception=" + e
                + ", running=" + false + ", done=" + false + ", schedulerSpecificInformation=" + tmp + "]";
		
		assertEquals(expected, s.toString());		
	}
}
