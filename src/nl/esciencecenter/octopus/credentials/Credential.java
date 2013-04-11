package nl.esciencecenter.octopus.credentials;

import java.util.Properties;

public interface Credential {
 
    public String getAdaptorName();
    
    public Properties getProperties();
}
