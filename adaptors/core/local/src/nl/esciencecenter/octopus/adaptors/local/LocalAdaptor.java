package nl.esciencecenter.octopus.adaptors.local;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.ImmutableTypedProperties;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.files.FilesAdaptor;
import nl.esciencecenter.octopus.engine.jobs.JobsAdaptor;
import nl.esciencecenter.octopus.exceptions.OctopusException;

public class LocalAdaptor extends Adaptor {
	
	private static final String ADAPTOR_NAME = "local";
	
	private static final String ADAPTOR_DESCRIPTION = "The local adaptor implements all functionality with " +
			" standard java classes such as java.lang.Process and java.nio.file.Files." ;
    
	private static final String [] ADAPTOR_SCHEME = new String [] { "local", "file" };
	
    protected static final String LOCAL_MULTIQ_MAX_JOBS = "local.multiq.maxConcurrentJobs";
    protected static final String LOCAL_Q_HISTORY_SIZE = "local.queue.historySize";
    
    protected static final int DEFAULT_LOCAL_Q_HISTORY_SIZE = 1000;

    private final LocalFiles localFiles;
    private final LocalJobs localJobs;
    
    public LocalAdaptor(ImmutableTypedProperties properties, OctopusEngine octopusEngine) throws OctopusException {
    	super(octopusEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME);
    			
    	localFiles = new LocalFiles(properties, this, octopusEngine);
    	localJobs = new LocalJobs(properties, this, octopusEngine);
    }
    
    void checkURI(URI location) throws OctopusException {

    	if (!supports(location.getScheme())) {
            throw new OctopusException("Local adaptor does not support scheme " + location.getScheme(), ADAPTOR_NAME, location);
        }
        
        String host = location.getHost();

        if (host != null && !host.equals("localhost")) {
            throw new OctopusException("Local adaptor only supports url with empty host or \"localhost\", not \""
                    + location.getHost() + "\"", ADAPTOR_NAME, location);
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
}
