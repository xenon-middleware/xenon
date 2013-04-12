package nl.esciencecenter.octopus.files;

/**
 * Path with its associated Attributes.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface PathAttributesPair {

    /** 
     * Get the AbsolutePath in this PathAttributesPair.
     * 
     * @return the AbsolutePath.
     */
    public AbsolutePath path();

    /** 
     * Get the FileAttributes in this PathAttributesPair.
     * 
     * @return the FileAttributes.
     */
    public FileAttributes attributes();
}
