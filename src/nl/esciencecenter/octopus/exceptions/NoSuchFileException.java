package nl.esciencecenter.octopus.exceptions;

public class NoSuchFileException extends OctopusIOException {

    private static final long serialVersionUID = 1L;

    public NoSuchFileException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public NoSuchFileException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
