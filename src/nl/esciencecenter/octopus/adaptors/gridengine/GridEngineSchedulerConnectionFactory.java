package nl.esciencecenter.octopus.adaptors.gridengine;

import java.net.URI;
import java.util.Properties;

import nl.esciencecenter.octopus.adaptors.scripting.SchedulerConnection;
import nl.esciencecenter.octopus.adaptors.scripting.SchedulerConnectionFactory;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

public class GridEngineSchedulerConnectionFactory implements SchedulerConnectionFactory {

    @Override
    public SchedulerConnection newSchedulerConnection(URI location, Credential credential, Properties properties,
            OctopusEngine engine) throws OctopusIOException, OctopusException {
        return new GridEngineSchedulerConnection(location, credential, properties, engine);
    }

}
