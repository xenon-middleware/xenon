package nl.esciencecenter.octopus.engine.jobs;

import java.net.URI;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.jobs.Scheduler;

public class SchedulerImplementation implements Scheduler {

    private final String adaptorName;
    private final String uniqueID; 
    
    private final URI uri;
    private final OctopusProperties properties;
    private final String [] queueNames;
    private final Credential credential;
    
    private final boolean localStandardStreams;
    private final boolean hasDetachedJobs;
    
    public SchedulerImplementation(String adaptorName, String uniqueID, URI uri, String [] queueNames, Credential credential, 
            OctopusProperties properties, boolean localStandardStreams, boolean hasDetachedJobs) {
        
        this.adaptorName = adaptorName;
        this.uniqueID = uniqueID;
        this.uri = uri;
        this.queueNames = queueNames;
        this.properties = properties;
        this.credential = credential;
        this.localStandardStreams = localStandardStreams;
        this.hasDetachedJobs = hasDetachedJobs;
    }

    public Credential getCredential() {
        return credential;
    }
    
    public String getUniqueID() {
        return uniqueID;
    }
    
    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public OctopusProperties getProperties() {
        return properties;
    }

    @Override
    public String getAdaptorName() {
        return adaptorName;
    }
    
    @Override
    public String[] getQueueNames() {
        return queueNames.clone();
    }

    @Override
    public boolean hasLocalStandardStreams() {
        return localStandardStreams;
    }

    @Override
    public boolean hasDetachedJobs() {
        return hasDetachedJobs;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((adaptorName == null) ? 0 : adaptorName.hashCode());
        result = prime * result + ((uniqueID == null) ? 0 : uniqueID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SchedulerImplementation other = (SchedulerImplementation) obj;
        if (adaptorName == null) {
            if (other.adaptorName != null)
                return false;
        } else if (!adaptorName.equals(other.adaptorName))
            return false;
        if (uniqueID == null) {
            if (other.uniqueID != null)
                return false;
        } else if (!uniqueID.equals(other.uniqueID))
            return false;
        return true;
    }
}
