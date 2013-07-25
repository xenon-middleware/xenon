package nl.esciencecenter.octopus.adaptors.slurm;

import java.net.URI;

import nl.esciencecenter.octopus.adaptors.scripting.SchedulerConnection;
import nl.esciencecenter.octopus.adaptors.scripting.SchedulerConnectionFactory;
import nl.esciencecenter.octopus.adaptors.scripting.ScriptingAdaptor;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

public class SlurmSchedulerConnectionFactory implements SchedulerConnectionFactory {

    @Override
    public SchedulerConnection newSchedulerConnection(ScriptingAdaptor adaptor, URI location, Credential credential, 
            OctopusProperties properties, OctopusEngine engine) throws OctopusIOException, OctopusException {
        return new SlurmSchedulerConnection(adaptor, location, credential, properties, engine);
    }

}
