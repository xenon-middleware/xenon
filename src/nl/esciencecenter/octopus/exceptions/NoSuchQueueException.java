package nl.esciencecenter.octopus.exceptions;

public class NoSuchQueueException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public NoSuchQueueException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public NoSuchQueueException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
