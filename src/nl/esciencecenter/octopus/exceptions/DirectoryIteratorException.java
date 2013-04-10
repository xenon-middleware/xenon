package nl.esciencecenter.octopus.exceptions;

public class DirectoryIteratorException extends OctopusRuntimeException {

    private static final long serialVersionUID = 1L;

    public DirectoryIteratorException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public DirectoryIteratorException(String adaptorName, String message) {
        super(adaptorName, message);
    }

}
