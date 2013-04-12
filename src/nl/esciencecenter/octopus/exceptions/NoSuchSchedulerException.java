package nl.esciencecenter.octopus.exceptions;

public class NoSuchSchedulerException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public NoSuchSchedulerException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public NoSuchSchedulerException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
