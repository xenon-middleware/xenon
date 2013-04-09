package nl.esciencecenter.octopus.exceptions;

import java.net.URI;

public class DeployRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String adaptorName;

    private final URI uri;

    public DeployRuntimeException(String s, String adaptorName, URI uri) {
        super(s);
        this.adaptorName = adaptorName;
        this.uri = uri;
    }

    public DeployRuntimeException(String message, Throwable t, String adaptorName, URI uri) {
        super(message, t);
        this.adaptorName = adaptorName;
        this.uri = uri;
    }

    @Override
    public String getMessage() {
        String result = super.getMessage();
        if (adaptorName != null) {
            result = adaptorName + " adaptor: " + result;
        }

        if (uri != null) {
            result = result + " (uri = " + uri + ")";
        }

        return result;
    }
}
