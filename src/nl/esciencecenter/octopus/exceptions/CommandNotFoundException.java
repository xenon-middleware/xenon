package nl.esciencecenter.octopus.exceptions;

public class CommandNotFoundException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public CommandNotFoundException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public CommandNotFoundException(String adaptorName, String message) {
        super(adaptorName, message);
    }

}
