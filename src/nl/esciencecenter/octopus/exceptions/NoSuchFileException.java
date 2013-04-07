package nl.esciencecenter.octopus.exceptions;

import java.net.URI;

public class NoSuchFileException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public NoSuchFileException(String s, String adaptorName, URI uri) {
        super(s, adaptorName, uri);
    }

    public NoSuchFileException(String message, Throwable t, String adaptorName, URI uri) {
        super(message, t, adaptorName, uri);
    }
}
