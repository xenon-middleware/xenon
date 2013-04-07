package nl.esciencecenter.octopus.exceptions;

import java.net.URI;

public class FilePrestageException extends OctopusException {
    
    private static final long serialVersionUID = 1L;

    public FilePrestageException(String s, String adaptorName, URI uri) {
        super(s, adaptorName, uri);
    }

    public FilePrestageException(String message, Throwable t, String adaptorName, URI uri) {
        super(message, t, adaptorName, uri);
    }
}
