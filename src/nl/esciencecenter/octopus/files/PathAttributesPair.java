package nl.esciencecenter.octopus.files;

/**
 * Path with its associated Attributes
 * 
 * @author Niels Drost
 * 
 */
public interface PathAttributesPair {

    public AbsolutePath path();

    public FileAttributes attributes();
}
