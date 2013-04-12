package nl.esciencecenter.octopus.credentials;

import java.util.Properties;

import nl.esciencecenter.octopus.exceptions.OctopusException;

/**
 * Credentials represents the credentials interface of Octopus.
 * 
 * This interface contains various methods for creating certificates. 
 * 
 * @author Rob van Nieuwpoort <R.vanNieuwpoort@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */

public interface Credentials {

    /**
     * Constructs a certificate Credential. 
     * 
     * A certificate Credential is created out of a <code>keyfile</code> pointing to the private key, a <code>certfile</code>
     * pointing to the certificate, a username and a password.
     * 
     * @param keyfile
     *            the private key file (for example userkey.pem)
     * @param certfile
     *            the certificate file (for example usercert.pem)
     * @param username
     *            the username
     * @param password
     *            the password or passphrase belonging to the key and certificate.
     * @returns an ID for the credential, which can be used to remove it from the credential set again.
     */
    public Credential newCertificateCredential(String scheme, Properties properties, String keyfile, String certfile,
            String username, String password) throws OctopusException;

    /**
     * Constructs a password credential. 
     * 
     * If a username is given in the URIs, it must be identical to username parameter.
     * 
     * @param username
     *            the username.
     * @param password
     *            the password.
     */
    public Credential newPasswordCredential(String scheme, Properties properties, String username, String password)
            throws OctopusException;

    /**
     * Creates a proxy credential.
     * 
     * @param host
     *            the hostname of the proxy server
     * @param port
     *            the port where the proxy server runs, -1 for the default port
     * @param username
     *            the username to use to connect to the proxy server
     * @param password
     *            the password to use to connect to the proxy server
     */
    public Credential newProxyCredential(String scheme, Properties properties, String host, int port, String username,
            String password) throws OctopusException;
}
