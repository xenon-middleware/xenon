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
package nl.esciencecenter.xenon.credentials;

import java.util.HashMap;

/**
 * A {@link Credential} consisting of a collection of Credentials each uniquely identified by a String (typically a host name or host alias).
 *
 * A default Credential can be set that will be returned if by <code>get</code> if a key is not found.
 */
public class CredentialMap implements Credential {

    private final HashMap<String, UserCredential> map = new HashMap<>();

    private UserCredential defaultCredential;

    /**
     * Create a new CredentialMap using <code>null</code> as the default credential.
     */
    public CredentialMap() {
        this(null);
    }

    /**
     * Creates a new <code>CredentialMap</code> and set the default credential to <code>defaultCredential</code>.
     *
     * @param defaultCredential
     *            the default credential to return by <code>get</code> if a key is not found.
     */
    public CredentialMap(UserCredential defaultCredential) {
        this.defaultCredential = defaultCredential;
    }

    /**
     * Add a {@link Credential} to the CredentialMap using <code>key</code> as a unique key.
     *
     * If the <code>key</code> already exists in the CredentialMap, the stored {@link UserCredential} will be replaced by <code>credential</code>.
     *
     * @param key
     *            the unique key used to store the credential.
     * @param credential
     *            the UserCredential to store.
     */
    public void put(String key, UserCredential credential) {

        if (key == null) {
            throw new IllegalArgumentException("Key may not be null");
        }

        map.put(key, credential);
    }

    /**
     * Check if the <code>key</code> is stored in this CredentialMap.
     *
     * @param key
     *            the key to check.
     * @return if <code>key</code> is stored in this CredentialMap
     */
    public boolean containsCredential(String key) {
        return map.containsKey(key);
    }

    /**
     * Retrieve the {@link UserCredential} stored using the <code>key</code>.
     *
     * If the key is not found in the map, the default credential is returned (if provided when the CredentialMap was created) or <code>null</code> if no
     * default is set.
     *
     * @param key
     *            the key of the {@link UserCredential} to retrieve.
     * @return the {@link UserCredential} stored using <code>key</code> or the default credential if the key is not found.
     */
    public UserCredential get(String key) {

        if (map.containsKey(key)) {
            return map.get(key);
        }

        return defaultCredential;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((defaultCredential == null) ? 0 : defaultCredential.hashCode());
        result = prime * result + ((map == null) ? 0 : map.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        CredentialMap other = (CredentialMap) obj;

        if (defaultCredential == null) {
            if (other.defaultCredential != null)
                return false;

        } else if (!defaultCredential.equals(other.defaultCredential))
            return false;

        if (!map.equals(other.map))
            return false;

        return true;
    }

}
