/*
 * Created on Jul 29, 2005
 */
package nl.esciencecenter.octopus.security;

/**
 * This context is used if you have a credential stored in a myproxy server. You
 * need to specify a host, port, username and password to the myproxy server.
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
     *            the port where the myproxy server runs, -1 for the default
     *            port
     * @param username
     *            the username to use to connect to the myproxy server
     * @param password
     *            the password to use to connect to the myproxy server
     */
    public ProxyCredential(String host, int port, String username, String password) {
        super(username, password);
        this.host = host;
        this.port = port;
    }

    public Object clone() throws CloneNotSupportedException {
        return new ProxyCredential(host, port, username, password);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ProxyCredential)) {
            return false;
        }

        ProxyCredential other = (ProxyCredential) obj;

        return other.username.equals(username) && other.password.equals(password) && other.host.equals(host)
                && (other.port == port);
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
     * Sets the host of the MyProxy server.
     * 
     * @param host
     *            the new host of the MyProxy server
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the port of the MyProxy server.
     * 
     * @return the port of the MyProxy server.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port of the MyProxy server.
     * 
     * @param port
     *            the new port of the MyProxy server
     */
    public void setPort(int port) {
        this.port = port;
    }

    public int hashCode() {
        return host.hashCode();
    }

    public String toString() {
        return "MyProxyServerCredentialSecurityContext(host = " + host + " port = " + port
                + ((username == null) ? "" : (" username = " + username))
                + ((dataObjects == null) ? "" : (" userdata = " + dataObjects)) + ")";
    }
}
