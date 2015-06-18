package nl.esciencecenter.xenon.adaptors.webdav;

import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.adaptors.generic.Location;

public class WebdavLocation extends Location {

    public WebdavLocation(String user, String host, int port) {
        super(user, host, port);
    }

    protected WebdavLocation(String location) throws InvalidLocationException {
        super(location);
    }

    @Override
    protected String getAdaptorName() {
        return WebdavAdaptor.ADAPTOR_NAME;
    }

    @Override
    protected int getDefaultPort() {
        return WebdavAdaptor.DEFAULT_PORT;
    }

    public static WebdavLocation parse(String location) throws InvalidLocationException {
        return new WebdavLocation(location);
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return super.toString() + getPath();
    }

}
