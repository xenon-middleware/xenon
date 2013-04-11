package nl.esciencecenter.octopus.adaptors.local;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.jobs.Jobs;

public class LocalAdaptor extends Adaptor {

    public static final String ADAPTOR_NAME = OctopusEngine.LOCAL_ADAPTOR_NAME;

    public static final String ADAPTOR_DESCRIPTION = "The local adaptor implements all functionality with "
            + " standard java classes such as java.lang.Process and java.nio.file.Files.";

    public static final String[] ADAPTOR_SCHEME = new String[] { "local", "file" };

    /** All our own properties start with this prefix. */
    public static final String PREFIX = OctopusEngine.ADAPTORS + "local.";

    /** All our own queue properties start with this prefix. */
    public static final String QUEUE = PREFIX + "queue.";

    /** Maximum history length for finished jobs */
    public static final String MAX_HISTORY = QUEUE + "historySize";

    /** All our multi queue properties start with this prefix. */
    public static final String MULTIQ = QUEUE + "multi.";

    /** Maximum number of concurrent jobs in the multiq */
    public static final String MULTIQ_MAX_CONCURRENT = MULTIQ + "maxConcurrentJobs";

    /** List of {NAME, DESCRIPTION, DEFAULT_VALUE} for properties. */
    private static final String[][] VALID_PROPERTIES = new String[][] {
            { MAX_HISTORY, "1000", "Int: the maximum history length for finished jobs." },
            { MULTIQ_MAX_CONCURRENT, null, "Int: the maximum number of concurrent jobs in the multiq." } };

    private final LocalFiles localFiles;
    private final LocalJobs localJobs;

    public LocalAdaptor(OctopusProperties properties, OctopusEngine octopusEngine) throws OctopusException {
        super(octopusEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME, VALID_PROPERTIES, properties);

        localFiles = new LocalFiles(getProperties(), this, octopusEngine);
        localJobs = new LocalJobs(getProperties(), this, octopusEngine);
    }

    void checkURI(URI location) throws OctopusException {
        
        if (location == null) { 
            return;
        }
        
        String scheme = location.getScheme();

        if (scheme != null && !supports(scheme)) {
            throw new OctopusException(getClass().getName(), "Local adaptor does not support scheme " + scheme);
        }

        String host = location.getHost();

        if (host != null && !host.equals("localhost")) {
            throw new OctopusException(getClass().getName(), "Local adaptor only supports url with empty host or \"localhost\", not \""
                    + location.getHost() + "\"");
        }
    }
    
    @Override
    public boolean supports(String scheme) {

        if (scheme == null) { 
            return true;
        }
        
        return super.supports(scheme);
    }
    

    @Override
    public Map<String, String> getSupportedProperties() {
        return new HashMap<String, String>();
    }

    @Override
    public void end() {
        // TODO: implement!
        
        // localJobs.end();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Files filesAdaptor() {
        return localFiles;
    }

    @Override
    public Jobs jobsAdaptor() {
        return localJobs;
    }

    @Override
    public Credentials credentialsAdaptor() throws OctopusException {
        throw new OctopusException(ADAPTOR_NAME, "Adaptor does not need or understand credentials.");
    }
}
