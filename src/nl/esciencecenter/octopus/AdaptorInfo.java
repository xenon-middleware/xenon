package nl.esciencecenter.octopus;

import java.util.Map;

public interface AdaptorInfo {

    /**
     * Get the schemes this adaptor supports for creating objects. Does not imply it supports all schemes for all Octopus
     * functions.
     * 
     * @return the schemes this adaptor supports
     */
    public abstract String[] getSupportedSchemes();

    /**
     * Name of the adaptor
     * 
     * @return the name of the adaptor
     */
    public abstract String getName();

    public abstract String getDescription();

    /**
     * Returns a map containing supported properties, and a small description for each.
     * 
     * @return map containing supported properties, and a small description for each.
     */
    public abstract Map<String, String> getSupportedProperties();

}