package nl.esciencecenter.octopus.exceptions;

public class PermissionDeniedException extends OctopusIOException {

    private static final long serialVersionUID = 1L;

    public PermissionDeniedException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public PermissionDeniedException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
