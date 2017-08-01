package nl.esciencecenter.xenon.filesystems;

import nl.esciencecenter.xenon.XenonException;


public class CopyCancelledException extends XenonException {
    public CopyCancelledException(String adaptorName, String s) {
        super(adaptorName,s);
    }

}
