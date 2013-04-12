package nl.esciencecenter.octopus.exceptions;

public class IllegalJobDescriptionException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public IllegalJobDescriptionException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public IllegalJobDescriptionException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
