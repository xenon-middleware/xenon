package nl.esciencecenter.octopus.engine;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.octopus.AdaptorInfo;
import nl.esciencecenter.octopus.OctopusProperties;
import nl.esciencecenter.octopus.engine.files.FilesAdaptor;
import nl.esciencecenter.octopus.engine.jobs.JobsAdaptor;
import nl.esciencecenter.octopus.exceptions.OctopusException;

/**
 * New-style adaptor interface. Adaptors are expected to implement one or more create functions of the Octopus interface,
 * depending on which functionality they provide.
 * 
 * @author Jason Maassen
 * 
 */
public abstract class Adaptor implements AdaptorInfo {

    private final String name;
    private final String description;
    private final String[] supportedSchemes;

    protected final OctopusEngine octopusEngine;

    private final String[][] defaultProperties;

    private final OctopusProperties properties;

    protected Adaptor(OctopusEngine octopusEngine, String name, String description, String[] supportedSchemes,
            String[][] defaultProperties, OctopusProperties properties) throws OctopusException {

        super();

        this.octopusEngine = octopusEngine;

        this.name = name;
        this.description = description;
        this.supportedSchemes = supportedSchemes;

        this.defaultProperties = (defaultProperties == null ? new String[0][0] : defaultProperties);
        this.properties = processProperties(properties);
    }

    private OctopusProperties processProperties(OctopusProperties properties) throws OctopusException {

        Set<String> validSet = new HashSet<String>();

        for (int i = 0; i < defaultProperties.length; i++) {
            validSet.add(defaultProperties[i][0]);
        }

        OctopusProperties p = properties.filter("octopus.adaptors." + name);

        for (Map.Entry<Object, Object> entry : p.entrySet()) {
            if (!validSet.contains(entry.getKey())) {
                throw new OctopusException("Unknown property " + entry);
            }
        }
        
        return new OctopusProperties(defaultProperties, p);
    }

    public OctopusProperties getProperties() { 
        return properties;
    }
    
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String[] getSupportedSchemes() {
        return supportedSchemes.clone();
    }

    public boolean supports(String scheme) {

        for (String s : supportedSchemes) {
            if (s.equalsIgnoreCase(scheme)) {
                return true;
            }
        }

        return false;
    }

    public abstract FilesAdaptor filesAdaptor() throws OctopusException;

    public abstract JobsAdaptor jobsAdaptor() throws OctopusException;

    public abstract void end();
}
