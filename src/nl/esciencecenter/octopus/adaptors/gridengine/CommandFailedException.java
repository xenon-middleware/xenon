package nl.esciencecenter.octopus.adaptors.gridengine;

import nl.esciencecenter.octopus.exceptions.OctopusException;

public class CommandFailedException extends OctopusException {

    private static final long serialVersionUID = 1L;

    public CommandFailedException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public CommandFailedException(String adaptorName, String message) {
        super(adaptorName, message);
    }

}
