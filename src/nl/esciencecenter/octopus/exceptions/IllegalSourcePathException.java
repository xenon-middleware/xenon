package nl.esciencecenter.octopus.exceptions;

public class IllegalSourcePathException extends OctopusIOException {

    private static final long serialVersionUID = 1L;

    public IllegalSourcePathException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public IllegalSourcePathException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
