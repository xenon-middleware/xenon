package nl.esciencecenter.octopus.jobs;

import java.net.URI;
import java.util.Properties;

public interface Scheduler {

    public String getAdaptorName();
    
    public URI getUri();

    public Properties getProperties();
}
