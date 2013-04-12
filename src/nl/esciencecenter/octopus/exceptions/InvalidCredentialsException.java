package nl.esciencecenter.octopus.exceptions;

public class InvalidCredentialsException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public InvalidCredentialsException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public InvalidCredentialsException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
