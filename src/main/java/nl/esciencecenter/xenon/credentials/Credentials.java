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

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;

/**
 * Credentials represents the credentials interface of Xenon.
 * 
 * This interface contains various methods for creating and closing 
 * credentials. Make sure to always close Credential instances by calling 
 * {@code close()} when you no longer need them, otherwise their associated 
 * resources remain allocated.
 * 
 * @version 1.0
 * @since 1.0
 */
public interface Credentials {

    /**
     * Constructs a certificate Credential.
     * 
     * A certificate credential is created out of a certificate file 
     * (containing the certificate), a user name, and (optionally) a password 
     * needed to access the credential. Make sure to always close Credential
     * instances by calling close() when you no longer need them, otherwise 
     * their associated resources remain allocated.
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
     * @throws nl.esciencecenter.xenon.UnknownPropertyException
     *             If an unknown property was passed.
     * @throws nl.esciencecenter.xenon.InvalidPropertyException
     *             If a known property was passed with an illegal value.
     * @throws CertificateNotFoundException
     *             If the certificate file could not be found.
     * @throws XenonException
     *             If the Credential could not be created.
     */
    Credential newCertificateCredential(String scheme, String certfile, String username, char[] password,
            Map<String, String> properties) throws XenonException;

    /**
     * Constructs a password credential out of a {@code username} and a 
     * {@code password}. Make sure to always close Credential instances 
     * by calling close() when you no longer need them, otherwise their 
     * associated resources remain allocated.
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
     * @throws nl.esciencecenter.xenon.UnknownPropertyException
     *             If an unknown property was passed.
     * @throws nl.esciencecenter.xenon.InvalidPropertyException
     *             If a known property was passed with an illegal value.
     * @throws XenonException
     *             If the Credential could not be created.
     */
    Credential newPasswordCredential(String scheme, String username, char[] password, Map<String, String> properties)
            throws XenonException;
    
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
     * @throws nl.esciencecenter.xenon.UnknownPropertyException
     *             If an unknown property was passed.
     * @throws nl.esciencecenter.xenon.InvalidPropertyException
     *             If a known property was passed with an illegal value.
     * @throws nl.esciencecenter.xenon.InvalidCredentialException
     *             If a valid credential cannot be created.
     * @throws XenonException
     *             If the Credential could not be created.
     */
    Credential getDefaultCredential(String scheme) throws XenonException;

    /**
     * Close a Credential
     * 
     * @param credential
     *            the Credential to close.
     * 
     * @throws XenonException
     *             If the Credential failed to close.
     */
    void close(Credential credential) throws XenonException;
    
    /**
     * Test if a Credential is open.
     * 
     * @param credential
     *            the Credential to test.
     * @return whether the credential is open.
     * 
     * @throws XenonException
     *             If the test failed.
     */
    boolean isOpen(Credential credential) throws XenonException;    
}
