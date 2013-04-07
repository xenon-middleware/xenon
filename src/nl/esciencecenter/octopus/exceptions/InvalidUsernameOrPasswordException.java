package nl.esciencecenter.octopus.exceptions;

import java.net.URI;

public class InvalidUsernameOrPasswordException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public InvalidUsernameOrPasswordException(String s, String adaptorName, URI uri) {
        super(s, adaptorName, uri);
    }

    public InvalidUsernameOrPasswordException(String message, Throwable t, String adaptorName, URI uri) {
        super(message, t, adaptorName, uri);
    }
}
