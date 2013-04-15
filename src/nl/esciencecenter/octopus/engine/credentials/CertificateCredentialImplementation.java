/*
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.esciencecenter.octopus.engine.credentials;

import java.net.URI;

import nl.esciencecenter.octopus.engine.OctopusProperties;

/**
 * A container for security Information based upon certificates. Contexts based upon these mechanisms can be used by adaptors to
 * create further contexts containing opaque data objects, e.g. GSSAPI credentials.
 */
public class CertificateCredentialImplementation extends CredentialImplementation {

    /**
     * This member variables holds the URI of the keyfile of the SecurityContext
     */
    private String keyfile = null;

    private String certfile = null;

    /**
     * Constructs a {@link CertificateCredentialImplementation} out of a {@link URI} pointing to the private key, a {@link URI}
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
     */
    public CertificateCredentialImplementation(String adaptorName, OctopusProperties properties, String keyfile, String certfile,
            String username, String password) {
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
