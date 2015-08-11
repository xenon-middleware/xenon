package nl.esciencecenter.xenon.adaptors.generic;

import nl.esciencecenter.xenon.InvalidLocationException;

public abstract class Location {

    private final String user;
    private final String host;
    private final int port;

    public Location(String user, String host, int port) {
        this.user = user;
        this.host = host;
        this.port = port;
    }

    protected Location(String location) throws InvalidLocationException {
        // Parse the location
        String newUser = null;
        String newHost;
        int newPort = -1;

        if (location == null) {
            throw new InvalidLocationException(getAdaptorName(), "Failed to parse " + getAdaptorName() + " location: (null)");
        }

        String tmpLocation = location;

        int index = tmpLocation.indexOf('@');

        if (index == 0) {
            // There must be a user before the @, or no @ at all.
            throw new InvalidLocationException(getAdaptorName(), "Failed to parse " + getAdaptorName()
                    + " location! Invalid user: " + location);
        }

        if (index > 0) {
            newUser = tmpLocation.substring(0, index);
            tmpLocation = tmpLocation.substring(index + 1);
        }

        index = tmpLocation.indexOf(':');

        if (index == 0) {
            // There must be a host before the :
            throw new InvalidLocationException(getAdaptorName(), "Failed to parse " + getAdaptorName()
                    + " location! Missing host: " + location);
        }

        if (index > 0) {

            if (tmpLocation.length() < index + 1) {
                throw new InvalidLocationException(getAdaptorName(), "Failed to parse " + getAdaptorName()
                        + " location! Invalid port: " + location);
            }

            String portAsString = tmpLocation.substring(index + 1);

            if (portAsString.length() == 0) {
                throw new InvalidLocationException(getAdaptorName(), "Failed to parse " + getAdaptorName()
                        + " location! Missing port: " + location);
            }

            try {
                newPort = Integer.valueOf(portAsString);
            } catch (NumberFormatException e) {
                throw new InvalidLocationException(getAdaptorName(), "Failed to parse " + getAdaptorName()
                        + " location! Invalid port: " + location, e);
            }

            tmpLocation = tmpLocation.substring(0, index);
        }

        newHost = tmpLocation;

        if (newHost.isEmpty()) {
            throw new InvalidLocationException(getAdaptorName(), "Failed to parse " + getAdaptorName()
                    + " location! Missing host: " + location);
        }

        this.user = newUser;
        this.host = newHost;
        this.port = newPort;
    }

    public String getUser() {
        return user;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        if (port > 0) {
            return port;
        } else {
            return getDefaultPort();
        }
    }

    /** Whether the default port was used because none was provided. */
    public boolean usesDefaultPort() {
        return port <= 0;
    }

    protected abstract String getAdaptorName();

    protected abstract int getDefaultPort();

    @Override
    public String toString() {
        if (user != null) {
            return user + "@" + host + ":" + port;
        } else {
            return host + ":" + port;
        }
    }

}
