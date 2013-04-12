package nl.esciencecenter.octopus.engine;

import java.util.Map;

import nl.esciencecenter.octopus.AdaptorStatus;

public class AdaptorStatusImplementation implements AdaptorStatus {

    private final String name;
    private final String description;
    private final String [] supportedSchemes;
    private final Map<String, String> supportedProperties;
    private final Map<String, String> adaptorSpecificInformation;
    
    public AdaptorStatusImplementation(String name, String description, String[] supportedSchemes,
            Map<String, String> supportedProperties, Map<String, String> adaptorSpecificInformation) {
        
        super();
        this.name = name;
        this.description = description;
        this.supportedSchemes = supportedSchemes;
        this.supportedProperties = supportedProperties;
        this.adaptorSpecificInformation = adaptorSpecificInformation;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String[] getSupportedSchemes() {
        return supportedSchemes.clone();
    }

    @Override
    public Map<String, String> getSupportedProperties() {
        return supportedProperties;
    }

    @Override
    public Map<String, String> getAdaptorSpecificInformation() {
        return adaptorSpecificInformation;
    }
}
