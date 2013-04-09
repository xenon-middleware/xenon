package nl.esciencecenter.octopus.engine.jobs;

import java.net.URI;

import nl.esciencecenter.octopus.ImmutableTypedProperties;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.jobs.Scheduler;

public class SchedulerImplementation implements Scheduler {

	private final URI uri;
	private final ImmutableTypedProperties properties;
	private final String adaptorName;
	
	public SchedulerImplementation(URI uri, ImmutableTypedProperties properties, String adaptorName) { 
		this.uri = uri;
		this.properties = properties;
		this.adaptorName = adaptorName;
	}
	
	@Override
	public URI getUri() {
		return uri;
	}

	@Override
	public ImmutableTypedProperties getProperties() {
		return properties;
	}

	@Override
	public String getAdaptorName() {
		return adaptorName;
	}
}
