package nl.esciencecenter.octopus.engine;

import java.util.ArrayList;
import java.util.Properties;

import nl.esciencecenter.octopus.AdaptorInfo;
import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.credentials.CredentialsEngineImplementation;
import nl.esciencecenter.octopus.engine.files.FilesEngine;
import nl.esciencecenter.octopus.engine.jobs.JobsEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.IllegalPropertyException;
import nl.esciencecenter.octopus.exceptions.UnknownPropertyException;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.jobs.Jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OctopusEngine implements the Octopus Interface class by redirecting all calls to {@link Adaptor}s.
 * 
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class OctopusEngine implements Octopus {

    /** The local adaptor is a special case, therefore we publish its name here. */
    public static final String LOCAL_ADAPTOR_NAME = "local";
    
    /** All our own properties start with this prefix. */
    public static final String PREFIX = "octopus.";

    /** All our own queue properties start with this prefix. */
    public static final String ADAPTORS = PREFIX + "adaptors.";

    /** All our own queue properties start with this prefix. */
    public static final String LOAD = ADAPTORS + "load";

    /** List of {NAME, DESCRIPTION, DEFAULT_VALUE} for properties. */
    private static final String[][] VALID_PROPERTIES = new String[][] {
            { LOAD, null, "List: comma separated list of the adaptors to load." }};
    
    private static final Logger logger = LoggerFactory.getLogger(OctopusEngine.class);

    /** All OctopusEngines created so far */
    private static final ArrayList<OctopusEngine> octopusEngines = new ArrayList<OctopusEngine>();

    /** 
     * Create a new Octopus using the given properties.
     * 
     * @param properties the properties used to create the Octopus. 
     * @return the newly created Octopus created. 
     * 
     * @throws UnknownPropertyException If an unknown property was passed.
     * @throws IllegalPropertyException If a known property was passed with an illegal value.
     * @throws OctopusException If the Octopus failed initialize.
     */
    public static Octopus newOctopus(Properties properties) throws OctopusException {

        OctopusEngine result = new OctopusEngine(properties);

        synchronized (octopusEngines)  {
            octopusEngines.add(result);
        }

        return result;
    }
    
    public static void closeOctopus(Octopus engine) throws OctopusException {

        OctopusEngine result = null;
        
        synchronized (octopusEngines)  {           
            for (int i=0;i<octopusEngines.size();i++) { 
                if (octopusEngines.get(i) == engine) {
                    result = octopusEngines.remove(i);
                    break;
                }
            }
        }

        if (result == null) { 
            throw new OctopusException("engine", "No such OctopusEngine");
        } 
        
        result.end();
    }

    public static void endAll() {
        synchronized (octopusEngines)  {                       
            for (OctopusEngine octopusEngine : octopusEngines) {
                octopusEngine.end();
            }
        }
    }
    
    private boolean ended = false;

    private OctopusProperties octopusProperties;

    private final FilesEngine filesEngine;

    private final JobsEngine jobsEngine;

    private final CredentialsEngineImplementation credentialsEngine;

    private final Adaptor[] adaptors;

    /**
     * Constructs a OctopusEngine.
     * 
     * @param properties the properties to use. Will NOT be copied.
     *            
     * @throws UnknownPropertyException If an unknown property was passed.
     * @throws IllegalPropertyException If a known property was passed with an illegal value.
     * @throws OctopusException If the Octopus failed initialize.
     */
    private OctopusEngine(Properties properties) throws OctopusException {
      
        octopusProperties = new OctopusProperties(VALID_PROPERTIES, properties);
        
        adaptors = AdaptorLoader.loadAdaptors(octopusProperties, this);

        filesEngine = new FilesEngine(this);

        jobsEngine = new JobsEngine(this);

        credentialsEngine = new CredentialsEngineImplementation(this);

        logger.info("Octopus engine initialized with adaptors: " + adaptors);
    }

    // ************** Octopus Interface Implementation ***************\\

    @Override
    public AdaptorInfo[] getAdaptorInfos() {
        return adaptors.clone();
    }

    @Override
    public AdaptorInfo getAdaptorInfo(String adaptorName) throws OctopusException {
        return getAdaptor(adaptorName);
    }

    /**
     * Return the adaptor that provides functionality for the given scheme.
     * 
     * @param scheme
     *            the scheme for which to get the adaptor
     * @return the adaptor
     */
    public Adaptor getAdaptorFor(String scheme) throws OctopusException {
        
        for (Adaptor adaptor : adaptors) {
            if (adaptor.supports(scheme)) {
                return adaptor;
            }
        }
        throw new OctopusException("cannot find adaptor for scheme " + scheme, null, null);
    }

    public Adaptor getAdaptor(String name) throws OctopusException {
        for (Adaptor adaptor : adaptors) {
            if (adaptor.getName().equals(name)) {
                return adaptor;
            }
        }

        throw new OctopusException("could not find adaptor named " + name, null, null);
    }

    public Adaptor[] getAdaptors() {
        return adaptors;
    }

    @Override
    public synchronized Properties getProperties() {
        return octopusProperties;
    }

    @Override
    public Files files() {
        return filesEngine;
    }

    @Override
    public Jobs jobs() {
        return jobsEngine;
    }

    @Override
    public Credentials credentials() {
        return credentialsEngine;
    }

    @Override
    public void end() {
        synchronized (this) {
            if (ended) {
                return;
            }

            ended = true;
        }

        for (Adaptor adaptor : adaptors) {
            adaptor.end();
        }
    }
}
