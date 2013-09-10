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
package nl.esciencecenter.octopus.credentials;

import java.util.Map;

import nl.esciencecenter.octopus.exceptions.OctopusException;

/**
 * Credentials represents the credentials interface of Octopus.
 * 
 * This interface contains various methods for creating and closing credentials.
 * 
 * @author Rob van Nieuwpoort <R.vanNieuwpoort@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface Credentials {

    /**
     * Constructs a certificate Credential.
     * 
     * A certificate Credential is created out of a certificate file (containing the certificate), a user name, and (optionally)
     * a password needed to access the credential.
     *
     * @param scheme
     *          the scheme for which to create a credential.                
     * @param certfile
     *          the certificate file (for example userkey.pem or id_dsa).
     * @param username
     *          the user name.
     * @param password
     *          the password or pass phrase belonging to the certificate.
     * @param properties
     *            (optional) properties used to configure the credential.
     * 
     * @return the Credential.
     * 
     * @throws UnknownPropertyException
     *             If an unknown property was passed.
     * @throws InvalidPropertyException
     *             If a known property was passed with an illegal value.
     * @throws OctopusException
     *             If the <code>Credential<code> could not be created.
     */
    Credential newCertificateCredential(String scheme, String certfile, String username, char[] password,
            Map<String, String> properties) throws OctopusException;

    /**
     * Constructs a password credential.
     * 
     * A password Credential consists of a user name and a password.
     * 
     * @param scheme
     *          the scheme for which to create a credential.                
     * @param username
     *          the user name.
     * @param password
     *          the password.
     * @param properties
     *            (optional) properties used to configure the credential.
     * 
     * @return the Credential.
     * 
     * @throws UnknownPropertyException
     *             If an unknown property was passed.
     * @throws InvalidPropertyException
     *             If a known property was passed with an illegal value.
     * @throws OctopusException
     *             If the <code>Credential<code> could not be created.
     */
    Credential newPasswordCredential(String scheme, String username, char[] password, Map<String, String> properties)
            throws OctopusException;

    /**
     * Creates a default credential for the given scheme.
     * 
     * It depends on the scheme if a default credential can be created. 
     * 
     * @param scheme
     *          the scheme for which to create a certificate.                
     * 
     * @return the Credential.
     * 
     * @throws UnknownPropertyException
     *             If an unknown property was passed.
     * @throws InvalidPropertyException
     *             If a known property was passed with an illegal value.
     * @throws OctopusException
     *             If the <code>Credential<code> could not be created.
     */
    Credential getDefaultCredential(String scheme) throws OctopusException;

    /**
     * Close a Credential
     * 
     * @param credential
     *            the Credential to close.
     * 
     * @throws OctopusException
     *             If the Credential failed to close.
     */
    void close(Credential credential) throws OctopusException;
    
    /**
     * Test if a Credential is open.
     * 
     * @param credential
     *            the Credential to test.
     * 
     * @throws OctopusException
     *             If the test failed.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    boolean isOpen(Credential credential) throws OctopusException;    
}
