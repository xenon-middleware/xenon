package nl.esciencecenter.octopus.files;

import java.net.URI;
import java.util.Properties;

public interface FileSystem {
    
    public String getAdaptorName();
    
    public URI getUri();

    public Properties getProperties();
    
    public RelativePath getEntryPath();

}
