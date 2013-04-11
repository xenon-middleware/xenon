package nl.esciencecenter.octopus.jobs;

import java.net.URI;
import java.util.Properties;

public interface Scheduler {

    public String getAdaptorName();
    
    public String getUniqueID();

    public URI getUri();

    public Properties getProperties();
}
