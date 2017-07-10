package nl.esciencecenter.xenon.schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

public class QueueStatusTest {
	
	@Test
	public void test_scheduler() throws Exception {
		TestScheduler s = new TestScheduler("ID", "TEST", "MEM", true, true, true, null);
		QueueStatus stat = new QueueStatus(s, "Q", null, null);
		assertEquals(s,  stat.getScheduler());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test_schedulerFailsNull() throws Exception {
		new QueueStatus(null, "Q", null, null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_queueNameFailsNull() throws Exception {
		TestScheduler s = new TestScheduler("ID", "TEST", "MEM", true, true, true, null);
		new QueueStatus(s, null, null, null);
	}
	
	@Test
	public void test_queue() throws Exception {
		TestScheduler s = new TestScheduler("ID", "TEST", "MEM", true, true, true, null);
		QueueStatus stat = new QueueStatus(s, "Q", null, null);
		assertEquals("Q",  stat.getQueueName());
	}
	
	@Test
	public void test_exeption() throws Exception {
		TestScheduler s = new TestScheduler("ID", "TEST", "MEM", true, true, true, null);
		Exception e = new NullPointerException("aap");
		QueueStatus stat = new QueueStatus(s, "Q", e, null);
		assertEquals(e,  stat.getException());
	}
	
	@Test
	public void test_info() throws Exception {
		HashMap<String, String> tmp = new HashMap<>();
		tmp.put("key", "value");
		
		TestScheduler s = new TestScheduler("ID", "TEST", "MEM", true, true, true, null);
		Exception e = new NullPointerException("aap");
		QueueStatus stat = new QueueStatus(s, "Q", e, tmp);
		assertEquals(tmp,  stat.getSchedulerSpecficInformation());
	}
	
	@Test
	public void test_hasExeptionTrue() throws Exception {
		TestScheduler s = new TestScheduler("ID", "TEST", "MEM", true, true, true, null);
		Exception e = new NullPointerException("aap");
		QueueStatus stat = new QueueStatus(s, "Q", e, null);
		assertTrue(stat.hasException());
	}
	
	@Test
	public void test_hasExeptionFalse() throws Exception {
		TestScheduler s = new TestScheduler("ID", "TEST", "MEM", true, true, true, null);
		QueueStatus stat = new QueueStatus(s, "Q", null, null);
		assertFalse(stat.hasException());
	}
	
	@Test
	public void test_toString() throws Exception {
		HashMap<String, String> tmp = new HashMap<>();
		tmp.put("key", "value");
		TestScheduler s = new TestScheduler("ID", "TEST", "MEM", true, true, true, null);
		Exception e = new NullPointerException("aap");
		QueueStatus stat = new QueueStatus(s, "Q", e, tmp);
		
		String expected = "QueueStatus [scheduler=" + s + ", queueName=" + "Q" + ", exception=" + e
			     + ", schedulerSpecificInformation=" + tmp + "]";
 		
		assertEquals(expected, stat.toString());
	}
	
	

}
