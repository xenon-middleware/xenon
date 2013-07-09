package nl.esciencecenter.octopus.adaptors.scripting;

import java.net.URI;
import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

public interface SchedulerConnectionFactory {

    public SchedulerConnection newSchedulerConnection(URI location, Credential credential, Properties properties,
            OctopusEngine engine) throws OctopusIOException, OctopusException;
}
