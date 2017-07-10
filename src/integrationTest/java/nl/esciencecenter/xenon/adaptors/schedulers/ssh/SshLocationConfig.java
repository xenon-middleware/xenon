package nl.esciencecenter.xenon.adaptors.schedulers.ssh;

import nl.esciencecenter.xenon.adaptors.schedulers.SchedulerLocationConfig;

public class SshLocationConfig extends SchedulerLocationConfig {

	private String location;
	
	public SshLocationConfig(String location) { 
		this.location = location; 
	}
	
	@Override
	public String getLocation() {
		return location;
	}
}
