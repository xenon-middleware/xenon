package nl.esciencecenter.octopus.engine.jobs;

import java.net.URI;

import nl.esciencecenter.octopus.ImmutableTypedProperties;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.security.Credentials;

public interface JobsAdaptor {
    
    public Scheduler newScheduler(ImmutableTypedProperties properties, Credentials credentials, URI location)
            throws OctopusException;

}
