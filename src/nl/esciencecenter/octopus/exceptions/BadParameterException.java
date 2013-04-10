package nl.esciencecenter.octopus.exceptions;

public class BadParameterException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public BadParameterException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public BadParameterException(String adaptorName, String message) {
        super(adaptorName, message);
    }

}
