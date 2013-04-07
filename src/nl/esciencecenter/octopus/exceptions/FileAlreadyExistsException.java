package nl.esciencecenter.octopus.exceptions;

import java.net.URI;

public class FileAlreadyExistsException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public FileAlreadyExistsException(String s, String adaptorName, URI uri) {
        super(s, adaptorName, uri);
    }

    public FileAlreadyExistsException(String message, Throwable t, String adaptorName, URI uri) {
        super(message, t, adaptorName, uri);
    }
}
