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
package nl.esciencecenter.xenon.engine.credentials;

import nl.esciencecenter.xenon.engine.CobaltProperties;

/**
 * A container for security Information based upon certificates. Contexts based upon these mechanisms can be used by adaptors to
 * create further contexts containing opaque data objects, e.g. GSSAPI credentials.
 */
public class CertificateCredentialImplementation extends CredentialImplementation {

    /**
     * This member variables holds the URI of the keyfile of the SecurityContext
     */
    private String certfile = null;

    /**
     * Constructs a {@link CertificateCredentialImplementation} out of a {@link URI} pointing to the private key, a {@link URI}
     * pointing to the certificate, a username and a password.
     * 
     * @param certfile
     *            the certificate file (for example usercert.pem)
     * @param username
     *            the username
     * @param password
     *            the password or passphrase belonging to the key and certificate.
     */
    public CertificateCredentialImplementation(String adaptorName, String uniqueID, CobaltProperties properties,
            String certfile, String username, char[] password) {

        super(adaptorName, uniqueID, properties, username, password);
        this.certfile = certfile;
    }

    /**
     * Returns the {@link URI} of the certificate file.
     * 
     * @return the {@link URI} of the certificate file.
     */
    public String getCertfile() {
        return certfile;
    }

    @Override
    public String toString() {
        return "CertificateCredentialImplementation [adaptorName=" + getAdaptorName() + ", username=" + getUsername() 
                + ", certfile=" + certfile + "]";
    }
}
