/**
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
package nl.esciencecenter.xenon.credentials;

/**
 * A container for security Information based upon certificates.
 */
public class CertificateCredential extends PasswordCredential {

    /**
     * This member variables holds the URI of the file containing the certificate.
     */
    private String certfile = null;

    /**
     * Constructs a {@link CertificateCredential} out of a username, a certificate file containing a private key, and an optional passphrase.
     *
     * @param username
     *          the username for this certificate
     * @param certfile
     *          the certification file
     * @param passphrase
     *          the optional passphrase needed to decrypt for this certificate
     */
    public CertificateCredential(String username, String certfile, char[] passphrase) {
        super(username, passphrase);
        this.certfile = certfile;
    }

    /**
     * Returns the certificate file.
     *
     * @return the certificate file.
     */
    public String getCertificateFile() {
        return certfile;
    }

    @Override
    public String toString() {
        return "CertificateCredential [username=" + getUsername() + ", certfile=" + certfile + "]";
    }
}
