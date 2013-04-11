package nl.esciencecenter.octopus.credentials;

import nl.esciencecenter.octopus.OctopusProperties;

public interface Credential {
 
    public String getAdaptorName();
    
    public String getUniqueID();

    public OctopusProperties getProperties();
}
