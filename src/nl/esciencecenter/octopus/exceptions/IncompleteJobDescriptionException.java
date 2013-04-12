package nl.esciencecenter.octopus.exceptions;

public class IncompleteJobDescriptionException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public IncompleteJobDescriptionException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public IncompleteJobDescriptionException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
