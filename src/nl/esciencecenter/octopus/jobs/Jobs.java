package nl.esciencecenter.octopus.jobs;

import java.net.URI;
import java.util.Properties;

import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.security.Credentials;

/**
 * Main interface to the jobs package
 *
 */
public interface Jobs {

    public Scheduler newScheduler(URI location) throws OctopusException;

    public Scheduler newScheduler(Properties properties, Credentials credentials, URI location) throws OctopusException;
}
