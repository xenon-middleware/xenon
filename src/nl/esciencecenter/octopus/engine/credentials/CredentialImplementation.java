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
 * A container for security Information.
 * 
 */
public abstract class CredentialImplementation implements nl.esciencecenter.octopus.credentials.Credential {

    private static int currentID = 1;

    private static synchronized String getNewUniqueID() {
        String res = "ssh" + currentID;
        currentID++;
        return res;
    }

    private final String uniqueID;

    /** the user name to use for this context */
    protected final String username;

    /**
     * This member variables holds the passphrase of the SecurityContext
     */

    /**
     * Must be char array for security!! (String end up in the constant pool, etc.)
     */
    protected final char[] password;

    protected final String adaptorName;

    protected final OctopusProperties properties;

    protected CredentialImplementation(String adaptorName, OctopusProperties properties, String username, String password) {
        this.adaptorName = adaptorName;
        this.properties = properties;
        this.username = username;
        this.password = password.toCharArray();
        uniqueID = getNewUniqueID();
    }

    public String getUniqueID() {
        return uniqueID;
    }

    /**
     * Returns the user name.
     * 
     * @return the user name
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the password.
     * 
     * @return the password
     */
    public char[] getPassword() {
        return password;
    }

    @Override
    public OctopusProperties getProperties() {
        return properties;
    }

    @Override
    public String getAdaptorName() {
        return adaptorName;
    }
}
