package nl.esciencecenter.octopus.adaptors.local;

import java.net.URI;

import nl.esciencecenter.octopus.ImmutableTypedProperties;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.jobs.JobsAdaptor;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.security.Credentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalJobs implements JobsAdaptor {

    private static final Logger logger = LoggerFactory.getLogger(LocalFiles.class);

    private final OctopusEngine octopusEngine;
    private final LocalAdaptor localAdaptor;
    private final ImmutableTypedProperties properties;

    private final LocalScheduler scheduler;

    public LocalJobs(ImmutableTypedProperties properties, LocalAdaptor localAdaptor, OctopusEngine octopusEngine)
            throws OctopusException {
        this.octopusEngine = octopusEngine;
        this.localAdaptor = localAdaptor;
        this.properties = properties;

        this.scheduler = new LocalScheduler(properties, octopusEngine);
    }

    @Override
    public Scheduler newScheduler(ImmutableTypedProperties properties, Credentials credentials, URI location)
            throws OctopusException {
        localAdaptor.checkURI(location);

        if (location.getPath() != null && location.getPath().length() > 0) {
            throw new OctopusException("Non-empty path in a local scheduler uri is not allowed", "local", location);
        }

        return scheduler;
    }

    public void end() {
        scheduler.end();
    }

}