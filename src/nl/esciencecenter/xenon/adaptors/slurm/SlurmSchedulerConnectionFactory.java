package nl.esciencecenter.xenon.adaptors.slurm;

import nl.esciencecenter.xenon.CobaltException;
import nl.esciencecenter.xenon.adaptors.scripting.SchedulerConnection;
import nl.esciencecenter.xenon.adaptors.scripting.SchedulerConnectionFactory;
import nl.esciencecenter.xenon.adaptors.scripting.ScriptingAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.CobaltEngine;
import nl.esciencecenter.xenon.engine.CobaltProperties;

public class SlurmSchedulerConnectionFactory implements SchedulerConnectionFactory {

    @Override
    public SchedulerConnection newSchedulerConnection(ScriptingAdaptor adaptor, String scheme, String location,
            Credential credential, CobaltProperties properties, CobaltEngine engine) throws CobaltException { 
        return new SlurmSchedulerConnection(adaptor, location, credential, properties, engine);
    }
}
