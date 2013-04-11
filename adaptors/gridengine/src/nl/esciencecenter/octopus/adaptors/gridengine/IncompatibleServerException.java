package nl.esciencecenter.octopus.adaptors.gridengine;

import java.net.URI;

import nl.esciencecenter.octopus.exceptions.OctopusException;

public class IncompatibleServerException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public IncompatibleServerException(String message, Exception error) {
        super(message, error);
    }

    public IncompatibleServerException(String s, String adaptorName, URI uri) {
        super(s, adaptorName, uri);
    }

    public IncompatibleServerException(String message, Throwable t, String adaptorName, URI uri) {
        super(message, t, adaptorName, uri);
    }

    public IncompatibleServerException(String s) {
        super(s);
    }

}
