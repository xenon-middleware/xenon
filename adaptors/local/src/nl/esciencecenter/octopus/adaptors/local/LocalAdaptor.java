package nl.esciencecenter.octopus.adaptors.local;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.OctopusProperties;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.files.FilesAdaptor;
import nl.esciencecenter.octopus.engine.jobs.JobsAdaptor;
import nl.esciencecenter.octopus.exceptions.OctopusException;

public class LocalAdaptor extends Adaptor {

    private static final String ADAPTOR_NAME = "local";

    private static final String ADAPTOR_DESCRIPTION = "The local adaptor implements all functionality with "
            + " standard java classes such as java.lang.Process and java.nio.file.Files.";

    private static final String[] ADAPTOR_SCHEME = new String[] { "local", "file" };

    /** All our own properties start with this prefix. */
    public static final String PREFIX = OctopusEngine.ADAPTORS + "local.";

    /** All our own queue properties start with this prefix. */
    public static final String QUEUE = PREFIX + "queue.";

    /** Maximum history length for finished jobs */
    public static final String MAX_HISTORY = QUEUE + "historySize";

    /** All our multi queue properties start with this prefix. */
    public static final String MULTIQ = QUEUE + "multiq.";

    /** Maximum number of concurrent jobs in the multiq */
    public static final String MULTIQ_MAX_CONCURRENT = MULTIQ + "maxConcurrentJobs";

    /** List of {NAME, DESCRIPTION, DEFAULT_VALUE} for properties. */
    private static final String[][] VALID_PROPERTIES = new String[][] {
            { MAX_HISTORY, "1000", "Int: the maximum history length for finished jobs." },
            { MULTIQ_MAX_CONCURRENT, null, "Int: the maximum number of concurrent jobs in the multiq." } };

    private final LocalFiles localFiles;
    private final LocalJobs localJobs;
    private final LocalCredentials localCredentials;

    public LocalAdaptor(OctopusProperties properties, OctopusEngine octopusEngine) throws OctopusException {
        super(octopusEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME, VALID_PROPERTIES, properties);

        localFiles = new LocalFiles(getProperties(), this, octopusEngine);
        localJobs = new LocalJobs(getProperties(), this, octopusEngine);
        localCredentials = new LocalCredentials();
    }

    void checkURI(URI location) throws OctopusException {
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
    public Map<String, String> getSupportedProperties() {
        return new HashMap<String, String>();
    }

    @Override
    public void end() {
        localJobs.end();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public FilesAdaptor filesAdaptor() {
        return localFiles;
    }

    @Override
    public JobsAdaptor jobsAdaptor() {
        return localJobs;
    }

    @Override
    public LocalCredentials credentialsAdaptor() {
        return localCredentials;
    }
}
