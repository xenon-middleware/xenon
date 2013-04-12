package nl.esciencecenter.octopus;

import java.util.Map;

public interface AdaptorStatus {

    /**
     * Get the schemes this adaptor supports for creating objects. Does not imply it supports all schemes for all Octopus
     * functions.
     * 
     * @return the schemes this adaptor supports
     */
    public String[] getSupportedSchemes();

    /**
     * Name of the adaptor
     * 
     * @return the name of the adaptor
     */
    public String getName();

    public String getDescription();

    /**
     * Returns a map containing supported properties, and a small description for each.
     * 
     * @return map containing supported properties, and a small description for each.
     */
    public Map<String, String> getSupportedProperties();
    
    public Map<String, String> getAdaptorSpecificInformation();

}