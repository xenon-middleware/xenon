package nl.esciencecenter.xenon.adaptors.ssh;

import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.adaptors.generic.Location;

public class SshLocation extends Location {

    public SshLocation(String user, String host, int port) {
        super(user, host, port);
    }

    protected SshLocation(String location) throws InvalidLocationException {
        super(location);
    }

    @Override
    protected String getAdaptorName() {
        return SshAdaptor.ADAPTOR_NAME;
    }

    @Override
    protected int getDefaultPort() {
        return SshAdaptor.DEFAULT_PORT;
    }

    public static SshLocation parse(String location) throws InvalidLocationException {
        return new SshLocation(location);
    }
}
