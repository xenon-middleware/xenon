package nl.esciencecenter.octopus.exceptions;

import java.io.IOException;
import java.net.URI;

public class OctopusException extends IOException {

    private static final long serialVersionUID = 1L;

    private final String adaptorName;

    private final URI uri;

    public OctopusException(String s) {
        super(s);
        this.adaptorName = null;
        this.uri = null;
    }

    public OctopusException(String s, String adaptorName, URI uri) {
        super(s);
        this.adaptorName = adaptorName;
        this.uri = uri;
    }

    public OctopusException(String message, Throwable t, String adaptorName, URI uri) {
        super(message, t);
        this.adaptorName = adaptorName;
        this.uri = uri;
    }

    public OctopusException(String message, Exception error) {
        super(message, error);
        this.adaptorName = null;
        this.uri = null;
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
