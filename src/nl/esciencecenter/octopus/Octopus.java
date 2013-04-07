package nl.esciencecenter.octopus;

import java.util.Properties;

import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.security.Credentials;

/**
 * Main interface to Octopus. Provides an acces point to all packages of Octopus, as well as some utility functions.
 * 
 * @author Niels Drost
 */
public interface Octopus {

    public Credentials getDefaultCredentials();

    public void setDefaultCredentials(Credentials credentials);

    public Properties getDefaultProperties();

    public void setDefaultProperties(Properties properties);
    
    AdaptorInfo getAdaptorInfo(String adaptorName) throws OctopusException;
    
    public AdaptorInfo[] getAdaptorInfos();
    
    /**
     * Get a reference to the Files package interface.
     * 
     * @return a reference to the Files interface.
     */
    public Files files();
    
    /**
     * Get a reference to the Jobs package
     * 
     * @return a reference to the Files package interface.
     */
    public Jobs jobs();
    

    //Future extension: clouds
    //public Clouds clouds();
    
    //Future extension: bandwith on demand
    //public Networks networks();
    
    //public ??

    public void end() throws Exception;

}
