package nl.esciencecenter.octopus.exceptions;

public class EndOfFileException extends OctopusIOException {

    private static final long serialVersionUID = 1L;

    public EndOfFileException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public EndOfFileException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
