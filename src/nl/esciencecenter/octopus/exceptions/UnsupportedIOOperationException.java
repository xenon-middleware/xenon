package nl.esciencecenter.octopus.exceptions;

public class UnsupportedIOOperationException extends OctopusIOException {

    private static final long serialVersionUID = 1L;

    public UnsupportedIOOperationException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public UnsupportedIOOperationException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
