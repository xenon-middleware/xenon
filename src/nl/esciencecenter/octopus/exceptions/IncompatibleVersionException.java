package nl.esciencecenter.octopus.exceptions;

public class IncompatibleVersionException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public IncompatibleVersionException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public IncompatibleVersionException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
