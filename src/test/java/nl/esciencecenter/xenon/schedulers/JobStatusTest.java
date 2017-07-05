package nl.esciencecenter.xenon.schedulers;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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

		@Override
		public boolean isInteractive() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isOnline() {
			// TODO Auto-generated method stub
			return false;
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
