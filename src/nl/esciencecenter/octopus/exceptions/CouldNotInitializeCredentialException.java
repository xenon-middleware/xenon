package nl.esciencecenter.octopus.exceptions;

import java.net.URI;

public class CouldNotInitializeCredentialException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public CouldNotInitializeCredentialException(String s, String adaptorName, URI uri) {
        super(s, adaptorName, uri);
    }

    public CouldNotInitializeCredentialException(String message, Throwable t, String adaptorName, URI uri) {
        super(message, t, adaptorName, uri);
    }

}
