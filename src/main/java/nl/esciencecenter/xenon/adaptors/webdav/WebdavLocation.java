package nl.esciencecenter.xenon.adaptors.webdav;

import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.adaptors.generic.Location;

public class WebdavLocation extends Location {

    public WebdavLocation(String user, String host, int port) {
        super(user, host, port);
    }

    protected WebdavLocation(String location) throws InvalidLocationException {
        super(location, WebdavAdaptor.ADAPTOR_SCHEME.get(0));
    }

    public WebdavLocation(String location, String scheme) throws InvalidLocationException {
        this(location);
        if (getScheme() == null) {
            //setScheme(scheme); // TODO: this method doesn't exist yet.
        }
    }

    @Override
    protected String getAdaptorName() {
        return WebdavAdaptor.ADAPTOR_NAME;
    }

    @Override
    protected int getDefaultPort() {
        return WebdavAdaptor.DEFAULT_PORT;
    }    

    @Override
    public String toString() {
        return super.toString() + getPath();
    }

    public static WebdavLocation parse(String location) throws InvalidLocationException {
        return new WebdavLocation(location);
    }

    public static WebdavLocation parse(String location, String scheme) throws InvalidLocationException {
        return new WebdavLocation(location, scheme);
    }
}
