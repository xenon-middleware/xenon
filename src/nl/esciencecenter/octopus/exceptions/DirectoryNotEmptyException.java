package nl.esciencecenter.octopus.exceptions;

import java.net.URI;

public class DirectoryNotEmptyException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public DirectoryNotEmptyException(String s, String adaptorName, URI uri) {
        super(s, adaptorName, uri);
    }

    public DirectoryNotEmptyException(String message, Throwable t, String adaptorName, URI uri) {
        super(message, t, adaptorName, uri);
    }

}
