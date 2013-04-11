package nl.esciencecenter.octopus.engine.files;

import java.net.URI;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.RelativePath;

public class FileSystemImplementation implements FileSystem {

    private final String adaptorName;
    private final String uniqueID; 
    
    private final URI uri;
    private final Credential credential;
    private final OctopusProperties properties;
    private final RelativePath entryPath;
        
    public FileSystemImplementation(String adaptorName, String uniqueID, URI uri, RelativePath entryPath, Credential credential, 
            OctopusProperties properties) {
        this.adaptorName = adaptorName;
        this.uniqueID = uniqueID;
        this.uri = uri;
        this.entryPath = entryPath;
        this.credential = credential;
        this.properties = properties;
    }

    public Credential getCredential() { 
        return credential;
    }
    
    public String getUniqueID() {
        return uniqueID;
    }
    
    @Override
    public RelativePath getEntryPath() {
        return entryPath;
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
        FileSystemImplementation other = (FileSystemImplementation) obj;
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
