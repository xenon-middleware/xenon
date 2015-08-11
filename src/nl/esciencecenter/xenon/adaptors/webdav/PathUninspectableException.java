package nl.esciencecenter.xenon.adaptors.webdav;

import nl.esciencecenter.xenon.XenonException;

public class PathUninspectableException extends XenonException {
    private static final long serialVersionUID = 1L;

    public PathUninspectableException(String adaptorName, String message) {
        super(adaptorName, message);
    }

    public PathUninspectableException(String adaptorName, String message, Throwable nested) {
        super(adaptorName, message, nested);
    }

}
