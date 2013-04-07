package nl.esciencecenter.octopus.exceptions;

import java.net.URI;

public class DirectoryIteratorException extends DeployRuntimeException {

    private static final long serialVersionUID = 1L;

    public DirectoryIteratorException(String s, String adaptorName, URI uri) {
        super(s, adaptorName, uri);
    }

    public DirectoryIteratorException(String message, Throwable t, String adaptorName, URI uri) {
        super(message, t, adaptorName, uri);
    }
}
