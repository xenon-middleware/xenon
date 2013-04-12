package nl.esciencecenter.octopus.exceptions;

public class IllegalPropertyException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public IllegalPropertyException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public IllegalPropertyException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
