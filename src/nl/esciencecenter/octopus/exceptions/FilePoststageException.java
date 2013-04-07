package nl.esciencecenter.octopus.exceptions;

import java.net.URI;

public class FilePoststageException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public FilePoststageException(String s, String adaptorName, URI uri) {
        super(s, adaptorName, uri);
    }

    public FilePoststageException(String message, Throwable t, String adaptorName, URI uri) {
        super(message, t, adaptorName, uri);
    }
}
