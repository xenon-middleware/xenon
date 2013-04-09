package nl.esciencecenter.octopus.jobs;

import java.net.URI;

import nl.esciencecenter.octopus.OctopusProperties;

public interface Scheduler {

    public URI getUri();

    public OctopusProperties getProperties();

    public String getAdaptorName();
}
