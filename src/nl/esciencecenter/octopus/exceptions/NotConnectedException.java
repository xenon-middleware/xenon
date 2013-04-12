package nl.esciencecenter.octopus.exceptions;

public class NotConnectedException extends OctopusIOException {

    private static final long serialVersionUID = 1L;

    public NotConnectedException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public NotConnectedException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
