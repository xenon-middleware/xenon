package nl.esciencecenter.octopus.engine.jobs;

import java.net.URI;
import java.util.Properties;

import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.security.Credentials;

public class JobsEngine implements Jobs {

    private final OctopusEngine octopusEngine;

    public JobsEngine(OctopusEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
    }

    @Override
    public Scheduler newScheduler(URI location) throws OctopusException {
        return newScheduler(null, null, location);
    }

    @Override
    public Scheduler newScheduler(Properties properties, Credentials credentials, URI location) throws OctopusException {
        Adaptor adaptor = octopusEngine.getAdaptorFor(location.getScheme());

        return adaptor.jobsAdaptor().newScheduler(octopusEngine.getCombinedProperties(properties),
                octopusEngine.getCombinedCredentials(credentials), location);
    }

}