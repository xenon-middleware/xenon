package nl.esciencecenter.octopus.jobs;

import java.net.URI;

import nl.esciencecenter.octopus.engine.OctopusProperties;

public interface Scheduler {

    public String getAdaptorName();
    
    public String getUniqueID();

    public URI getUri();

    public OctopusProperties getProperties();
}
