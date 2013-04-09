package nl.esciencecenter.octopus.exceptions;

import java.net.URI;

public class CredentialExpiredException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public CredentialExpiredException(String s, String adaptorName, URI uri) {
        super(s, adaptorName, uri);
    }

    public CredentialExpiredException(String message, Throwable t, String adaptorName, URI uri) {
        super(message, t, adaptorName, uri);
    }

}
