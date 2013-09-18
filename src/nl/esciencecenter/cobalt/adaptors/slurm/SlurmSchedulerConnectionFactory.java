package nl.esciencecenter.cobalt.adaptors.slurm;

import nl.esciencecenter.cobalt.CobaltException;
import nl.esciencecenter.cobalt.adaptors.scripting.SchedulerConnection;
import nl.esciencecenter.cobalt.adaptors.scripting.SchedulerConnectionFactory;
import nl.esciencecenter.cobalt.adaptors.scripting.ScriptingAdaptor;
import nl.esciencecenter.cobalt.credentials.Credential;
import nl.esciencecenter.cobalt.engine.CobaltEngine;
import nl.esciencecenter.cobalt.engine.CobaltProperties;

public class SlurmSchedulerConnectionFactory implements SchedulerConnectionFactory {

    @Override
    public SchedulerConnection newSchedulerConnection(ScriptingAdaptor adaptor, String scheme, String location,
            Credential credential, CobaltProperties properties, CobaltEngine engine) throws CobaltException { 
        return new SlurmSchedulerConnection(adaptor, location, credential, properties, engine);
    }
}
