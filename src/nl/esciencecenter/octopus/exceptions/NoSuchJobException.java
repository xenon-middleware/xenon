package nl.esciencecenter.octopus.exceptions;

public class NoSuchJobException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public NoSuchJobException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public NoSuchJobException(String adaptorName, String message) {
        super(adaptorName, message);
    }
}
