package nl.esciencecenter.octopus.exceptions;

import java.net.URI;

public class CommandNotFoundException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public CommandNotFoundException(String s, String adaptorName, URI uri) {
        super(s, adaptorName, uri);
    }

    public CommandNotFoundException(String message, Throwable t, String adaptorName, URI uri) {
        super(message, t, adaptorName, uri);
    }
}
