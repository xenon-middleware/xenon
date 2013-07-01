package nl.esciencecenter.octopus.adaptors.gridengine;

import nl.esciencecenter.octopus.exceptions.OctopusException;

public class CommandFailedException extends OctopusException {

    private static final long serialVersionUID = 1L;
    
    private int exitCode;
    private String stderr;

    public CommandFailedException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public CommandFailedException(String adaptorName, String message) {
        super(adaptorName, message);
    }

    public CommandFailedException(String adaptorName, String message, int exitCode, String stderr) {
        super(adaptorName, message);    
        this.exitCode = exitCode;
        this.stderr = stderr;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getStderr() {
        return stderr;
    }
}
