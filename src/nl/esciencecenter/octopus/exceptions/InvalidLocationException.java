package nl.esciencecenter.octopus.exceptions;

public class InvalidLocationException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public InvalidLocationException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public InvalidLocationException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
