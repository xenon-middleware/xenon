package nl.esciencecenter.octopus.exceptions;

public class DirectoryNotEmptyException extends OctopusIOException {

    private static final long serialVersionUID = 1L;

    public DirectoryNotEmptyException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public DirectoryNotEmptyException(String adaptorName, String message) {
        super(adaptorName, message);
    }

}
