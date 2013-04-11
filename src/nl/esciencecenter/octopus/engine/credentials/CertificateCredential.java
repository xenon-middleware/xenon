package nl.esciencecenter.octopus.engine.credentials;

import java.net.URI;

import nl.esciencecenter.octopus.engine.OctopusProperties;

/**
 * A container for security Information based upon certificates. Contexts based upon these mechanisms can be used by adaptors to
 * create further contexts containing opaque data objects, e.g. GSSAPI credentials.
 */
public class CertificateCredential extends Credential {

    /**
     * This member variables holds the URI of the keyfile of the SecurityContext
     */
    private String keyfile = null;

    private String certfile = null;

    /**
     * Constructs a {@link CertificateCredential} out of a {@link URI} pointing to the private key, a {@link URI} pointing to the
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
     */
    public CertificateCredential(String adaptorName, OctopusProperties properties, String keyfile, String certfile, String username, String password) {
        super(adaptorName, properties, username, password);
        this.keyfile = keyfile;
        this.certfile = certfile;
    }

    /**
     * Returns the location of the keyfile associated with the context.
     * 
     * @return The location of the keyfile associated with the context.
     */
    public String getKeyfile() {
        return keyfile;
    }

    public String toString() {
        return "CertificateSecurityContext(keyfile = " + keyfile + " certfile = " + certfile
                + ((username == null) ? "" : (" username = " + username));
    }

    /**
     * Returns the {@link URI} of the certificate file.
     * 
     * @return the {@link URI} of the certificate file.
     */
    public String getCertfile() {
        return certfile;
    }
}
