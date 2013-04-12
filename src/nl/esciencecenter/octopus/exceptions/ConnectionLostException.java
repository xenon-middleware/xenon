package nl.esciencecenter.octopus.exceptions;

public class ConnectionLostException extends OctopusIOException {

    private static final long serialVersionUID = 1L;

    public ConnectionLostException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public ConnectionLostException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
