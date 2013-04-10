package nl.esciencecenter.octopus.exceptions;

public class AttributeNotSupportedException extends OctopusIOException {

    private static final long serialVersionUID = 1L;

    public AttributeNotSupportedException(String adaptorName, String message, Throwable t) {
        super(adaptorName, message, t);
    }

    public AttributeNotSupportedException(String adaptorName, String message) {
        super(adaptorName, message);
    }

}
