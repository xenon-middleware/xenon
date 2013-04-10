package nl.esciencecenter.octopus.credentials;

import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Path;

/**
 * @author Rob van Nieuwpoort
 * 
 *         Creates new credentials, and store them in the credential set. All credentials can be limited in scope with the
 *         validFor parameter. This parameter specifies the set of URI for which this credential is valid. This can be used to
 *         restrict credentials to a host, port, scheme, username, etc...
 */
public interface Credentials {

    /**
     * Constructs a certificate Credential out of a {@link Path} pointing to the private key, a {@link Path} pointing to the
     * certificate, a username and a password.
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
    public Credential newCertificateCredential(Path keyfile, Path certfile, String username, String password)
            throws OctopusException;

    /**
     * Constructs a password credential. If a username is given in the URIs, it must be identical to username parameter.
     * 
     * @param username
     *            the username.
     * @param password
     *            the password.
     */
    public Credential newPasswordCredential(String username, String password) throws OctopusException;

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
    public Credential newProxyCredential(String host, int port, String username, String password)
            throws OctopusException;

}
