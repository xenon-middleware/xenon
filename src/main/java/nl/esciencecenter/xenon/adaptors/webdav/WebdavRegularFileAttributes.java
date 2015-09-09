package nl.esciencecenter.xenon.adaptors.webdav;

import nl.esciencecenter.xenon.XenonException;

import org.apache.jackrabbit.webdav.property.DavPropertySet;

public class WebdavRegularFileAttributes extends WebdavFileAttributes {

    public WebdavRegularFileAttributes(DavPropertySet properties) throws XenonException {
        super(properties);
    }

    @Override
    public boolean isRegularFile() {
        return true;
    }

}
