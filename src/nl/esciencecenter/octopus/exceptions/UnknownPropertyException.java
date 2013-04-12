package nl.esciencecenter.octopus.exceptions;

public class UnknownPropertyException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public UnknownPropertyException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public UnknownPropertyException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
