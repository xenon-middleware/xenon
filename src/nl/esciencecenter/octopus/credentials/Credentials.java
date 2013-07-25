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
    public Credential newCertificateCredential(String scheme, Map<String,String> properties, String keyfile, String certfile,
            String username, char[] password) throws OctopusException;

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
    public Credential newPasswordCredential(String scheme, Map<String,String> properties, String username, char[] password)
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
    public Credential newProxyCredential(String scheme, Map<String,String> properties, String host, int port, String username,
            char[] password) throws OctopusException;

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
    public Credential getDefaultCredential(String scheme) throws OctopusException;
}
