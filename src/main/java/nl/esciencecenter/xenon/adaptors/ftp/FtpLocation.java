package nl.esciencecenter.xenon.adaptors.ftp;

import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.adaptors.generic.Location;

public class FtpLocation extends Location {

    public FtpLocation(String user, String host, int port) {
        super(user, host, port);
    }

    protected FtpLocation(String location) throws InvalidLocationException {
        super(location, FtpAdaptor.ADAPTOR_SCHEME.get(0));
    }

    @Override
    protected String getAdaptorName() {
        return FtpAdaptor.ADAPTOR_NAME;
    }

    @Override
    protected int getDefaultPort() {
        return FtpAdaptor.DEFAULT_PORT;
    }

    public static FtpLocation parse(String location) throws InvalidLocationException {
        return new FtpLocation(location);
    }
}
