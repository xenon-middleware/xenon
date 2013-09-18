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
package nl.esciencecenter.cobalt.engine.credentials;

import java.util.Arrays;
import java.util.Map;

import nl.esciencecenter.cobalt.credentials.Credential;
import nl.esciencecenter.cobalt.engine.CobaltProperties;

/**
 * A container for security Information.
 * 
 */
public abstract class CredentialImplementation implements Credential {

    private final String uniqueID;

    /** the user name to use for this context */
    private final String username;

    /**
     * Must be char array for security!! (Strings end up in the constant pool, etc.)
     */
    private final char[] password;

    private final String adaptorName;

    private final CobaltProperties properties;

    protected CredentialImplementation(String adaptorName, String uniqueID, CobaltProperties properties, String username,
            char[] password) {

        this.adaptorName = adaptorName;
        this.username = username;
        this.uniqueID = uniqueID;

        if (password != null) {
            this.password = Arrays.copyOf(password, password.length);
        } else {
            this.password = null;
        }

        if (properties == null) {
            this.properties = new CobaltProperties();
        } else {
            this.properties = properties;
        }
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

        if (password == null) {
            return new char[0];
        }

        return password.clone();
    }

    @Override
    public Map<String, String> getProperties() {
        return properties.toMap();
    }

    @Override
    public String getAdaptorName() {
        return adaptorName;
    }
}
