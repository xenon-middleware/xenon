package nl.esciencecenter.octopus.adaptors.gridengine;

import nl.esciencecenter.octopus.exceptions.OctopusException;

public class IncompatibleServerException extends OctopusException {

    public IncompatibleServerException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public IncompatibleServerException(String adaptorName, String message) {
        super(adaptorName, message);
    }

    private static final long serialVersionUID = 1L;

}
