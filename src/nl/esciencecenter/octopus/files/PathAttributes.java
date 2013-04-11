package nl.esciencecenter.octopus.files;

/**
 * Path with its associated Attributes
 * 
 * @author Niels Drost
 * 
 */
public interface PathAttributes {

    public AbsolutePath path();

    public FileAttributes attributes();
}
