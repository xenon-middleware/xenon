package nl.esciencecenter.octopus.adaptors.local;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.ImmutableTypedProperties;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;

public class LocalAdaptor implements Adaptor {
    
    public static final String LOCAL_MULTIQ_MAX_JOBS = "local.multiq.max.concurrent.jobs";
    public static final String LOCAL_Q_HISTORY_SIZE = "local.q.history.size";
    
    public static final int DEFAULT_LOCAL_Q_HISTORY_SIZE = 1000;

    private final OctopusEngine octopusEngine;

    private final LocalFiles filesAdaptor;
    
    private final LocalJobs jobsAdaptor;

    private final LocalCredentials credentialsAdaptor;
    
    public LocalAdaptor(ImmutableTypedProperties properties, OctopusEngine octopusEngine) throws OctopusException {
        this.octopusEngine = octopusEngine;
        this.filesAdaptor = new LocalFiles(properties, this, octopusEngine);
        this.jobsAdaptor = new LocalJobs(properties, this, octopusEngine);
        this.credentialsAdaptor = new LocalCredentials();
    }

    @Override
    public String[] getSupportedSchemes() {
        return new String[] { "local", "file", ""};
    }

    @Override
    public boolean supports(String scheme) {
        for (String string : getSupportedSchemes()) {
            if (string.equalsIgnoreCase(scheme)) {
                return true;
            }
        }
        if (scheme == null) {
            return true;
        }
        
        return false;
    }
    
    void checkURI(URI location) throws OctopusException {
        if (!supports(location.getScheme())) {
            throw new OctopusException("Local adaptor does not support scheme " + location.getScheme(), "local", location);
        }

        String host = location.getHost();

        if (host != null && !host.equals("localhost")) {
            throw new OctopusException("Local adaptor only supports url with empty host or \"localhost\", not \""
                    + location.getHost() + "\"", "local", location);
        }
    }


    @Override
    public String getName() {
        return "local";
    }

    @Override
    public String getDescription() {
        return "The Local adaptor implements all functionality with standard java classes such "
                + "as java.lang.Process and java.nio.file.Files.";
    }

    @Override
    public Map<String, String> getSupportedProperties() {
        return new HashMap<String, String>();
    }

    
    @Override
    public LocalFiles filesAdaptor() {
        return filesAdaptor;
    }
    
    @Override
    public LocalJobs jobsAdaptor() {
        return jobsAdaptor;
    }

    @Override
    public LocalCredentials credentialsAdaptor() {
        return credentialsAdaptor;
    }

    @Override
    public void end() {
        jobsAdaptor.end();
    }
    
    @Override
    public String toString() {
        return getName();
    }

}
