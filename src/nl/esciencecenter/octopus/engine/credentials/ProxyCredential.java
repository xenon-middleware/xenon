package nl.esciencecenter.octopus.engine.credentials;

import nl.esciencecenter.octopus.engine.OctopusProperties;

/**
 * This context is used if you have a credential stored in a myproxy server. You need to specify a host, port, username and
 * password to the myproxy server.
 * 
 * @author rob
 */
public class ProxyCredential extends Credential {

    /**
     * The hostname of the server
     */
    protected String host;

    /**
     * the port where the server runs
     */
    protected int port;

    /**
     * Creates a {@link ProxyCredential}.
     * 
     * @param host
     *            the hostname of the myproxy server
     * @param port
     *            the port where the myproxy server runs, -1 for the default port
     * @param username
     *            the username to use to connect to the myproxy server
     * @param password
     *            the password to use to connect to the myproxy server
     */
    public ProxyCredential(String adaptorName, OctopusProperties properties, String host, int port, String username, String password) {
            super(adaptorName, properties, username, password);
        this.host = host;
        this.port = port;
    }
    
    /**
     * Returns the host of the MyProxy server.
     * 
     * @return the host of the MyProxy server
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the port of the MyProxy server.
     * 
     * @return the port of the MyProxy server.
     */
    public int getPort() {
        return port;
    }

    public String toString() {
        return "MyProxyServerCredentialSecurityContext(host = " + host + " port = " + port
                + ((username == null) ? "" : (" username = " + username));
    }
}
