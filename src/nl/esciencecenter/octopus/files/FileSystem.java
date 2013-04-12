package nl.esciencecenter.octopus.files;

import java.net.URI;
import java.util.Properties;

/**
 * FileSystem represent a (possibly remote) file system that can be used to access data.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface FileSystem {
    
    /** 
     * Get the name of the adaptor attached to this FileSystem.
     * 
     * @return the name of the adaptor.
     */
    public String getAdaptorName();
    
    /**
     * Get the URI representing the location of the FileSystem.
     *  
     * @return the location of the FileSystem.
     */
    public URI getUri();

    /** 
     * Get the properties used to create this FileSystem.  
     * 
     * @return the properties used to create this FileSystem. 
     */
    public Properties getProperties();

    /** 
     * Get the entry path of this file system.
     * 
     * The entry path is the initial path when the FileSystem is first accessed, for example <code>"/home/username"</code>. 
     *  
     * @return the entry path of this file system.
     */
    public RelativePath getEntryPath();
}
