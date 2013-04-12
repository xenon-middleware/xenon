package nl.esciencecenter.octopus.exceptions;

public class InvalidCloseException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public InvalidCloseException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public InvalidCloseException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
