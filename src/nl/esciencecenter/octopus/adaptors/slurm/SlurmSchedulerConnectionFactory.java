package nl.esciencecenter.octopus.adaptors.slurm;

import java.net.URI;
import java.util.Properties;

import nl.esciencecenter.octopus.adaptors.scripting.SchedulerConnection;
import nl.esciencecenter.octopus.adaptors.scripting.SchedulerConnectionFactory;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

public class SlurmSchedulerConnectionFactory implements SchedulerConnectionFactory {

    @Override
    public SchedulerConnection newSchedulerConnection(URI location, Credential credential, Properties properties,
            OctopusEngine engine) throws OctopusIOException, OctopusException {
        return new SlurmSchedulerConnection(location, credential, properties, engine);
    }

}
