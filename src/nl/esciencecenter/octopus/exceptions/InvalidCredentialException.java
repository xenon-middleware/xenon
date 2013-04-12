package nl.esciencecenter.octopus.exceptions;

public class InvalidCredentialException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public InvalidCredentialException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public InvalidCredentialException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
