package nl.esciencecenter.octopus;

import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.jobs.Jobs;

/**
 * Main interface to Octopus. Provides an access point to all packages of Octopus, as well as some utility functions.
 * 
 * @author Niels Drost
 */
public interface Octopus {

    public Properties getDefaultProperties();

    public void setDefaultProperties(Properties properties);
    
    public AdaptorInfo getAdaptorInfo(String adaptorName) throws OctopusException;
    
    public AdaptorInfo[] getAdaptorInfos();
    
    /**
     * Get a reference to the Files package interface.
     * 
     * @return a reference to the Files interface.
     */
    public Files files();
    
    /**
     * Get a reference to the Jobs package interface.
     * 
     * @return a reference to the Files package interface.
     */
    public Jobs jobs();

    /**
     * Get a reference to the Credentials package interface.
     * 
     * @return a reference to the Credentials package interface.
     */
    public Credentials credentials();

    //Future extension: clouds
    //public Clouds clouds();
    
    //Future extension: bandwidth on demand
    //public Networks networks();
    
    //public ??

    public void end() throws Exception;

}
