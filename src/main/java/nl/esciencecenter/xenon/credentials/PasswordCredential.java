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

import java.util.Arrays;

import nl.esciencecenter.xenon.credentials.Credential;

/**
 * A Credential consisting of a username + password combination.
 * 
 */
public class PasswordCredential implements Credential {

	/**
	 *  The user name associated with the credential.
	 */
    private final String username;

    /**
     * Must be char array for security!! (Strings end up in the constant pool, etc.)
     */
    private final char[] password;
    
    public PasswordCredential(String username, char[] password) {

    	this.username = username;
        
        if (password != null) {
            this.password = Arrays.copyOf(password, password.length);
        } else {
            this.password = null;
        }
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
	public String getUsername() {
		return username;
	}
	
	@Override
    public String toString() {
        return "PasswordCredential [username=" + getUsername() + "]";
    }
}
