package nl.esciencecenter.octopus.files;

import java.util.Set;

import nl.esciencecenter.octopus.exceptions.AttributeNotSupportedException;

/**
 * FileAttributes represents a set of attributes of a path.
 * 
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface FileAttributes {

    /**
     * Does the path refer to a directory ?
     * 
     * @return If the path refers to a directory. 
     * 
     * @throws AttributeNotSupportedException If the attribute is not supported by the adaptor.
     */
    public boolean isDirectory() throws AttributeNotSupportedException;

    /**
     * Is the path not a file, link or directory ?
     * 
     * @return If the file is a directory. 
     * 
     * @throws AttributeNotSupportedException If the attribute is not supported by the adaptor.
     */
    public boolean isOther() throws AttributeNotSupportedException;
    
    /**
     * Does the path refer to a regular file ?
     * 
     * @return If the path refers to a regular file. 
     * 
     * @throws AttributeNotSupportedException If the attribute is not supported by the adaptor.
     */
    public boolean isRegularFile() throws AttributeNotSupportedException;

    /**
     * Does the path refer to a symbolic link ?
     * 
     * @return If the path refers to a symbolic link.  
     * 
     * @throws AttributeNotSupportedException If the attribute is not supported by the adaptor.
     */
    public boolean isSymbolicLink() throws AttributeNotSupportedException;

    /**
     * Does the path refer to an executable file ?
     * 
     * @return If the path refers an executable file ?  
     * 
     * @throws AttributeNotSupportedException If the attribute is not supported by the adaptor.
     */
    public boolean isExecutable() throws AttributeNotSupportedException;

    /**
     * Does the path refer to an hidden file ?
     * 
     * @return If the path refers an hidden file ?  
     * 
     * @throws AttributeNotSupportedException If the attribute is not supported by the adaptor.
     */
    public boolean isHidden() throws AttributeNotSupportedException;
    
    /**
     * Does the path refer to an readable file ?
     * 
     * @return If the path refers an readable file ?  
     * 
     * @throws AttributeNotSupportedException If the attribute is not supported by the adaptor.
     */
    public boolean isReadable() throws AttributeNotSupportedException;

    /**
     * Does the path refer to a writable file ?
     * 
     * @return If the path refers a writable file ?  
     * 
     * @throws AttributeNotSupportedException If the attribute is not supported by the adaptor.
     */
    public boolean isWritable() throws AttributeNotSupportedException;
    
    /**
     * Get the creation time for this file.
     * 
     * @return The creation time for this file.
     * 
     * @throws AttributeNotSupportedException If the attribute is not supported by the adaptor.
     */
    public long creationTime() throws AttributeNotSupportedException;

    /**
     * Get the last access time for this file.
     * 
     * @return The last access time for this file.
     * 
     * @throws AttributeNotSupportedException If the attribute is not supported by the adaptor.
     */
    public long lastAccessTime() throws AttributeNotSupportedException;

    
    /**
     * Get the last modified time for this file.
     * 
     * @return The last modified time for this file.
     * 
     * @throws AttributeNotSupportedException If the attribute is not supported by the adaptor.
     */
    public long lastModifiedTime() throws AttributeNotSupportedException;

    /**
     * Get the size of this file.
     * 
     * @return The size of this file.
     * 
     * @throws AttributeNotSupportedException If the attribute is not supported by the adaptor.
     */
    public long size() throws AttributeNotSupportedException;

    /**
     * Get the group of this file.
     * 
     * @return The group of this file.
     * 
     * @throws AttributeNotSupportedException If the attribute is not supported by the adaptor.
     */
    public String group() throws AttributeNotSupportedException;

    /**
     * Get the owner of this file.
     * 
     * @return The owner of this file.
     * 
     * @throws AttributeNotSupportedException If the attribute is not supported by the adaptor.
     */
    public String owner() throws AttributeNotSupportedException;

    /**
     * Get the permissions of this file.
     * 
     * @return The permissions of this file.
     * 
     * @throws AttributeNotSupportedException If the attribute is not supported by the adaptor.
     */
    public Set<PosixFilePermission> permissions() throws AttributeNotSupportedException;
}
