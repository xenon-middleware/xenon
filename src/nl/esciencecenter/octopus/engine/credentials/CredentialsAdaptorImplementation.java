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
import java.util.UUID;

import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.AbsolutePath;

public abstract class CredentialsAdaptorImplementation {
    /**
     * Constructs a certificate Credential out of a {@link AbsolutePath} pointing to the private key, a {@link AbsolutePath}
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
    public abstract void newCertificateCredential(UUID uuid, AbsolutePath keyfile, AbsolutePath certfile, String username,
            String password, URI validFor) throws OctopusException;

    /**
     * Constructs a password credential. If a username is given in the URIs, it must be identical to username parameter.
     * 
     * @param username
     *            the username.
     * @param password
     *            the password.
     */
    public abstract UUID newPasswordCredential(UUID uuid, String username, String password, URI validFor) throws OctopusException;

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
    public abstract UUID newProxyCredential(UUID uuid, String host, int port, String username, String password, URI validFor)
            throws OctopusException;

    /**
     * Removes credentials from the credential set.
     * 
     * @param credentialID
     * @param validFor
     *            remove from given URIs, or from all if null is passed in.
     */
    public abstract void remove(UUID credentialID, URI validFor) throws OctopusException;
}
