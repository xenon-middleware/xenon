package nl.esciencecenter.xenon.adaptors.slurm;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.scripting.SchedulerConnection;
import nl.esciencecenter.xenon.adaptors.scripting.SchedulerConnectionFactory;
import nl.esciencecenter.xenon.adaptors.scripting.ScriptingAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonProperties;

public class SlurmSchedulerConnectionFactory implements SchedulerConnectionFactory {

    @Override
    public SchedulerConnection newSchedulerConnection(ScriptingAdaptor adaptor, String scheme, String location,
            Credential credential, XenonProperties properties, XenonEngine engine) throws XenonException { 
        return new SlurmSchedulerConnection(adaptor, location, credential, properties, engine);
    }
}
