package nl.esciencecenter.octopus;

import java.util.Properties;

import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;

public class OctopusFactory {

    /**
     * Private constructor as OctopusFactory should never be created. 
     */
    private OctopusFactory() {
        //DO NOT USE
    }

    /**
     * Constructs a Octopus instance. 
     * 
     * Properties may be provided that will be passed to the engine and the adaptors. Note that an {@link OctopusException} will 
     * be thrown if properties contains unknown keys.    
     * 
     * @param properties
     *            the properties to use. 

     * @return a new Octopus instance.
     * 
     * @throws OctopusException
     *             in case the engine fails to initialize.
     */
    public static Octopus newOctopus(Properties properties) throws OctopusException {
        return OctopusEngine.newEngine(properties);
    }

    /**
     * 
     */
    public static void end() {
        OctopusEngine.endEngines();
    }
}
