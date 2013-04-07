package nl.esciencecenter.octopus;

import java.util.Properties;

import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.security.Credentials;

public class OctopusFactory {
    
    private OctopusFactory() {
        //DO NOT USE
    }

    /**
     * Constructs a Octopus instance.
     * 
     * @param credentials
     *            the credentials to use. Will NOT be copied, may be null.
     * @param properties
     *            the properties to use. Will NOT be copied, may be null.
     * @return a new Octopus instance.
     * @throws OctopusException
     *             in case the engine fails to initialize.
     */
    public static Octopus newOctopus(Properties properties, Credentials credentials) throws OctopusException {
        return OctopusEngine.newEngine(properties, credentials);
    }

    public void end() {
        OctopusEngine.endEngines();
    }
}
