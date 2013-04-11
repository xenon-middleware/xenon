package nl.esciencecenter.octopus.exceptions;

public class FileAlreadyExistsException extends OctopusIOException {

    private static final long serialVersionUID = 1L;

    public FileAlreadyExistsException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public FileAlreadyExistsException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
