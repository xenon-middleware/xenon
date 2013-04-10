package nl.esciencecenter.octopus.exceptions;

public class UnsupportedOperationException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public UnsupportedOperationException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public UnsupportedOperationException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
