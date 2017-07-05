package nl.esciencecenter.xenon.schedulers;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;

import org.junit.Test;

import nl.esciencecenter.xenon.InvalidAdaptorException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.schedulers.local.LocalSchedulerAdaptor;

public class SchedulerTest {

	  @Test
	  public void test_create() throws Exception {
		  Scheduler s = Scheduler.create("local");
		  assertEquals("local", s.getAdaptorName());
	  }
	  
	  @Test(expected=InvalidAdaptorException.class)
	  public void test_createFailsNull() throws Exception {
		  Scheduler.create(null);
	  }

	  @Test(expected=InvalidAdaptorException.class)
	  public void test_createFailsEmpty() throws Exception {
		  Scheduler.create("");
	  }

	  @Test(expected=InvalidAdaptorException.class)
	  public void test_createFailsUnknown() throws Exception {
		  Scheduler.create("aap");
	  }

	  @Test
	  public void test_names() throws XenonException {
		  String [] tmp = Scheduler.getAdaptorNames();
		  String [] expected = new String [] { "local", "ssh", "gridengine", "slurm", "torque" };
		  assertTrue(Arrays.equals(expected, tmp));
	  }

	  @Test
	  public void test_adaptorDescription() throws XenonException {
		  
		  SchedulerAdaptorDescription d = Scheduler.getAdaptorDescription("local");
		  
		  LocalSchedulerAdaptor l = new LocalSchedulerAdaptor();
		  
		  assertEquals("local", l.getName());
		  assertTrue(d.isOnline());
		  assertTrue(d.supportsBatch());
		  assertTrue(d.supportsInteractive());
		  assertEquals(LocalSchedulerAdaptor.ADAPTOR_DESCRIPTION, d.getDescription());
		  assertArrayEquals(LocalSchedulerAdaptor.ADAPTOR_LOCATIONS, d.getSupportedLocations());
		  assertArrayEquals(LocalSchedulerAdaptor.VALID_PROPERTIES, d.getSupportedProperties());
	  }

	  @Test(expected=InvalidAdaptorException.class)
	  public void test_adaptorDescriptionFailsNull() throws XenonException {
		  Scheduler.getAdaptorDescription(null);
	  }

	  @Test(expected=InvalidAdaptorException.class)
	  public void test_adaptorDescriptionFailsEmpty() throws XenonException {
		  Scheduler.getAdaptorDescription("");
	  }

	  @Test(expected=InvalidAdaptorException.class)
	  public void test_adaptorDescriptionFailsUnknown() throws XenonException {
		  Scheduler.getAdaptorDescription("aap");
	  }
	  
	  @Test
	  public void test_adaptorDescriptions() throws XenonException {
		  
		  String [] names =  Scheduler.getAdaptorNames();
		  SchedulerAdaptorDescription [] desc = Scheduler.getAdaptorDescriptions();
		  
		  assertEquals(names.length, desc.length);
		  
		  for (int i=0;i<names.length;i++) { 
			  assertEquals(Scheduler.getAdaptorDescription(names[i]), desc[i]);
		  }
	  }
	  
	  @Test(expected=IllegalArgumentException.class)
	  public void test_createSchedulerFailsIDNull() throws Exception {
		  new TestScheduler(null, "TEST", "MEM", true, true, true, null);
	  }

	  @Test(expected=IllegalArgumentException.class)
	  public void test_createSchedulerFailsNameNull() throws Exception {
		  new TestScheduler("0", null, "MEM", true, true, true, null);
	  }

	  @Test(expected=IllegalArgumentException.class)
	  public void test_createSchedulerFailsLocationNull() throws Exception {
		  new TestScheduler("0", "TEST", null, true, true, true, null);
	  }
	  
	  @Test
	  public void test_getLocation() throws Exception {
		  Scheduler s = new TestScheduler("0", "TEST", "MEM", true, true, true, null);
		  assertEquals("MEM",s.getLocation());
	  }

	  @Test
	  public void test_isOnlineTrue() throws Exception {
		  Scheduler s = new TestScheduler("0", "TEST", "MEM", true, true, true, null);
		  assertTrue(s.isOnline());
	  }

	  @Test
	  public void test_isOnlineFalse() throws Exception {
		  Scheduler s = new TestScheduler("0", "TEST", "MEM", false, true, true, null);
		  assertFalse(s.isOnline());
	  }
	  
	  @Test
	  public void test_supportsBathTrue() throws Exception {
		  Scheduler s = new TestScheduler("0", "TEST", "MEM", true, true, true, null);
		  assertTrue(s.supportsBatch());
	  }

	  @Test
	  public void test_supportsBathFalse() throws Exception {
		  Scheduler s = new TestScheduler("0", "TEST", "MEM", true, false, true, null);
		  assertFalse(s.supportsBatch());
	  }

	  @Test
	  public void test_supportsInteractiveTrue() throws Exception {
		  Scheduler s = new TestScheduler("0", "TEST", "MEM", true, true, true, null);
		  assertTrue(s.supportsInteractive());
	  }

	  @Test
	  public void test_supportsInteractiveFalse() throws Exception {
		  Scheduler s = new TestScheduler("0", "TEST", "MEM", true, true, false, null);
		  assertFalse(s.supportsInteractive());
	  }

	  @Test
	  public void test_equalsTrueSelf() throws Exception {
		  Scheduler s = new TestScheduler("ID0", "TEST", "MEM", true, true, false, null);
		  assertTrue(s.equals(s));
	  }

	  @Test
	  public void test_equalsTrueSameID() throws Exception {
		  Scheduler s = new TestScheduler("ID0", "TEST", "MEM", true, true, false, null);
		  Scheduler s2 = new TestScheduler("ID0", "TEST", "MEM", true, true, false, null);
		  assertTrue(s.equals(s2));
	  }

	  @Test
	  public void test_equalsFalseNull() throws Exception {
		  Scheduler s = new TestScheduler("ID0", "TEST", "MEM", true, true, false, null);
		  assertFalse(s.equals(null));
	  }

	  @Test
	  public void test_equalsFalseWrongType() throws Exception {
		  Scheduler s = new TestScheduler("ID0", "TEST", "MEM", true, true, false, null);
		  assertFalse(s.equals("hello"));
	  }

	  @Test
	  public void test_properties() throws Exception {
		  HashMap<String,String> p = new HashMap<>(); 
		  p.put("aap", "noot");
		  
		  XenonPropertyDescription d = new XenonPropertyDescription("aap", Type.STRING, "empty", "test");
		  XenonProperties prop = new XenonProperties(new XenonPropertyDescription [] { d }, p);
		  
		  Scheduler s = new TestScheduler("ID0", "TEST", "MEM", true, true, true, prop);
		  assertEquals(p, s.getProperties());
	  }

	  @Test
	  public void test_hashcode() throws Exception {
		  Scheduler s = new TestScheduler("ID0", "TEST", "MEM", true, true, false, null);
		  assertEquals("ID0".hashCode(), s.hashCode());
	  }
	  
	  
}
