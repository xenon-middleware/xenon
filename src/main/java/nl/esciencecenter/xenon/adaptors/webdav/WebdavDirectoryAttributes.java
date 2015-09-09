package nl.esciencecenter.xenon.adaptors.webdav;

import nl.esciencecenter.xenon.XenonException;

import org.apache.jackrabbit.webdav.property.DavPropertySet;

public class WebdavDirectoryAttributes extends WebdavFileAttributes {

    public WebdavDirectoryAttributes(DavPropertySet properties) throws XenonException {
        super(properties);
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

}
