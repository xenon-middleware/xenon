package nl.esciencecenter.xenon.adaptors.job;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.jobs.Scheduler;
import nl.esciencecenter.xenon.jobs.SchedulerAdaptorDescription;

public abstract class SchedulerAdaptor {

	public static final String ADAPTORS_PREFIX = "xenon.adaptors.scheduler.";
	
	private static int currentID = 1;
	
	private final SchedulerAdaptorDescription adaptorDescription;
	
	protected SchedulerAdaptor(String name, String description, String [] locations, XenonPropertyDescription [] properties, 
			boolean isOnline, boolean supportsBatch, boolean supportsInteractive) {
		adaptorDescription = new SchedulerAdaptorDescription(name, description, locations, properties, 
				isOnline, supportsBatch, supportsInteractive);
	}
	
    protected synchronized String getNewUniqueID() {
        String res = adaptorDescription.getName() + "." + currentID;
        currentID++;
        return res;
    }

	public String getName() { 
		return adaptorDescription.getName();
	}
	
	public SchedulerAdaptorDescription getAdaptorDescription() { 
		return adaptorDescription;
	}
	   
	public abstract Scheduler createScheduler(String location, Credential credential, Map<String,String> properties) throws XenonException;
	
}
