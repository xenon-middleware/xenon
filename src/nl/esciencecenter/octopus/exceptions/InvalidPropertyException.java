package nl.esciencecenter.octopus.exceptions;

public class InvalidPropertyException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public InvalidPropertyException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public InvalidPropertyException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
