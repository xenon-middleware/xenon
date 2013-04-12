package nl.esciencecenter.octopus.credentials;

import java.util.Properties;

/**
 * Credential represents a user credential uses to gain access to a resource. 
 * 
 * @author Rob van Nieuwpoort <R.vanNieuwpoort@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface Credential {
    
    /** 
     * Get the name of the adaptor attached to this Credential.
     * 
     * @return the name of the adaptor.
     */
    public String getAdaptorName();
    
    /** 
     * Get the properties used to create this Credential.  
     * 
     * @return the properties used to create this Credential. 
     */
    public Properties getProperties();
}
