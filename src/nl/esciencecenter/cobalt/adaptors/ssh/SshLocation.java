package nl.esciencecenter.cobalt.adaptors.ssh;

import nl.esciencecenter.cobalt.InvalidLocationException;

public class SshLocation {

    private final String user;
    private final String host;
    private final int port;

    public SshLocation(String user, String host, int port) {
        this.user = user;
        this.host = host;
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() { 
        if (user != null) { 
            return user + "@" + host + ":" + port;
        } else { 
            return host + ":" + port;
        }
    }
    
    public static SshLocation parse(String location) throws InvalidLocationException {

        // Parse the location        
        String user = null;
        String host = null;
        int port = SshAdaptor.DEFAULT_PORT;

        if (location == null) { 
            throw new InvalidLocationException(SshAdaptor.ADAPTOR_NAME, "Failed to parse SSH location: (null)");
        }

        String tmpLocation = location;
        
        int index = tmpLocation.indexOf('@');

        if (index == 0) {
            // There must be a user before the @, or no @ at all.
            throw new InvalidLocationException(SshAdaptor.ADAPTOR_NAME, "Failed to parse SSH location! Invalid user: "
                    + location);
        }

        if (index > 0) {
            user = tmpLocation.substring(0, index);
            tmpLocation = tmpLocation.substring(index + 1);
        }

        index = tmpLocation.indexOf(':');

        if (index == 0) {
            // There must be a host before the :
            throw new InvalidLocationException(SshAdaptor.ADAPTOR_NAME, "Failed to parse SSH location! Missing host: " 
                    + location);
        }

        if (index > 0) {

            if (tmpLocation.length() < index + 1) {
                throw new InvalidLocationException(SshAdaptor.ADAPTOR_NAME, "Failed to parse SSH location! Invalid port: "
                        + location);
            }

            String portAsString = tmpLocation.substring(index + 1);

            if (portAsString.length() == 0) {
                throw new InvalidLocationException(SshAdaptor.ADAPTOR_NAME, "Failed to parse SSH location! Missing port: "
                        + location);
            }

            try {
                port = Integer.valueOf(portAsString);
            } catch (NumberFormatException e) {
                throw new InvalidLocationException(SshAdaptor.ADAPTOR_NAME, "Failed to parse SSH location! Invalid port: "
                        + location, e);
            }

            tmpLocation = tmpLocation.substring(0, index);
        }

        host = tmpLocation;

        if (host.isEmpty()) { 
            throw new InvalidLocationException(SshAdaptor.ADAPTOR_NAME, "Failed to parse SSH location! Missing host: " 
                    + location);
        }
        
        if (port <= 0) {
            port = SshAdaptor.DEFAULT_PORT;
        }

        return new SshLocation(user, host, port);
    }
}
