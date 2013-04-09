package nl.esciencecenter.octopus.exceptions;

import java.net.URI;

public class AttributeNotSupportedException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public AttributeNotSupportedException(String s, String adaptorName, URI uri) {
        super(s, adaptorName, uri);
    }

    public AttributeNotSupportedException(String message, Throwable t, String adaptorName, URI uri) {
        super(message, t, adaptorName, uri);
    }

}
