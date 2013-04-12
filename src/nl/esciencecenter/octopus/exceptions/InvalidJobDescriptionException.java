package nl.esciencecenter.octopus.exceptions;

public class InvalidJobDescriptionException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public InvalidJobDescriptionException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public InvalidJobDescriptionException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
