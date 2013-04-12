package nl.esciencecenter.octopus.exceptions;

public class NoSuchOctopusException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public NoSuchOctopusException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public NoSuchOctopusException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
