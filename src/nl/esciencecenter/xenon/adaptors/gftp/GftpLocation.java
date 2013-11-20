package nl.esciencecenter.xenon.adaptors.gftp;

import nl.esciencecenter.xenon.InvalidLocationException;

public class GftpLocation {

    private final String hostname;

    private final int port;

    GftpLocation(String host, int port) {
        this.hostname = host;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return hostname + ":" + port;
    }

    public static GftpLocation parse(String location) throws InvalidLocationException {

        // Parse the location        
        String host = null;
        int port = GftpAdaptor.DEFAULT_PORT;

        if (location == null) {
            throw new InvalidLocationException(GftpAdaptor.ADAPTOR_NAME, "Failed to parse GFTP location: (null)");
        }

        String tmpLocation = location;

        int index = tmpLocation.indexOf(':');

        if (index == 0) {
            // There must be a host before the :
            throw new InvalidLocationException(GftpAdaptor.ADAPTOR_NAME, "Failed to parse GFTP location! Missing host: "
                    + location);
        }

        if (index > 0) {

            if (tmpLocation.length() < index + 1) {
                throw new InvalidLocationException(GftpAdaptor.ADAPTOR_NAME, "Failed to parse GFTP location! Invalid port: "
                        + location);
            }

            String portAsString = tmpLocation.substring(index + 1);

            if (portAsString.length() == 0) {
                throw new InvalidLocationException(GftpAdaptor.ADAPTOR_NAME, "Failed to parse GFTP location! Missing port: "
                        + location);
            }

            try {
                port = Integer.valueOf(portAsString);
            } catch (NumberFormatException e) {
                throw new InvalidLocationException(GftpAdaptor.ADAPTOR_NAME, "Failed to parse GFTP location! Invalid port: "
                        + location, e);
            }

            tmpLocation = tmpLocation.substring(0, index);
        }

        host = tmpLocation;

        if (host.isEmpty()) {
            throw new InvalidLocationException(GftpAdaptor.ADAPTOR_NAME, "Failed to parse GFTP location! Missing host: "
                    + location);
        }

        if (port <= 0) {
            port = GftpAdaptor.DEFAULT_PORT;
        }

        return new GftpLocation(host, port);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        GftpLocation other = (GftpLocation) obj;

        return this.toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

}
