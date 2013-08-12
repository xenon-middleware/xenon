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

import nl.esciencecenter.octopus.engine.OctopusProperties;

/**
 * Represents a {@link Credential} stored in a myproxy server. You need to specify a host, port, username and password to the
 * myproxy server.
 * 
 * @author rob
 */
public class ProxyCredentialImplementation extends CredentialImplementation {

    /**
     * The hostname of the server
     */
    private String host;

    /**
     * the port where the server runs
     */
    private int port;

    /**
     * Creates a {@link ProxyCredentialImplementation}.
     * 
     * @param host
     *            the hostname of the myproxy server
     * @param port
     *            the port where the myproxy server runs, -1 for the default port
     * @param username
     *            the username to use to connect to the myproxy server
     * @param password
     *            the password to use to connect to the myproxy server
     */
    public ProxyCredentialImplementation(String adaptorName, String uniqueID, OctopusProperties properties, String host,
            int port, String username, char[] password) {
        super(adaptorName, uniqueID, properties, username, password);
        this.host = host;
        this.port = port;
    }

    /**
     * Returns the host of the MyProxy server.
     * 
     * @return the host of the MyProxy server
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the port of the MyProxy server.
     * 
     * @return the port of the MyProxy server.
     */
    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "ProxyCredentialImplementation [adaptorName=" + getAdaptorName() + ", username=" + getUsername() + ", host=" + host
                + ", port=" + port + "]";
    }
}
