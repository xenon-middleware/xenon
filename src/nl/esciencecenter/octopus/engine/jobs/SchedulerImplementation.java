package nl.esciencecenter.octopus.engine.jobs;

import java.net.URI;

import nl.esciencecenter.octopus.ImmutableTypedProperties;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.security.Credentials;

public class SchedulerImplementation implements Scheduler {

	private final URI uri;
	private final ImmutableTypedProperties properties;
	private final Credentials credentials;
	private final String adaptorName;
	
	public SchedulerImplementation(URI uri, ImmutableTypedProperties properties, Credentials credentials, String adaptorName) { 
		this.uri = uri;
		this.properties = properties;
		this.credentials = credentials;
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
	public Credentials getCredentials() {
		return credentials;
	}

	@Override
	public String getAdaptorName() {
		return adaptorName;
	}

}
