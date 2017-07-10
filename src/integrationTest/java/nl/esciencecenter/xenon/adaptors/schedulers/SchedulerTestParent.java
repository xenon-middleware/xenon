package nl.esciencecenter.xenon.adaptors.schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assume.assumeTrue;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.schedulers.Scheduler;
import nl.esciencecenter.xenon.schedulers.SchedulerAdaptorDescription;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    	assertEquals(locationConfig.getLocation(), scheduler.getLocation());
    }
    
    @Test
    public void test_unknownJobStatus() throws XenonException {
   	
    	

    }

    
    

}
