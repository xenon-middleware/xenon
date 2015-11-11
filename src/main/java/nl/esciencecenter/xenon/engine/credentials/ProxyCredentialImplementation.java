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
package nl.esciencecenter.xenon.engine.credentials;

import nl.esciencecenter.xenon.engine.XenonProperties;

/**
 * A container for security Information based upon certificates. Contexts based upon these mechanisms can be used by adaptors to
 * create further contexts containing opaque data objects, e.g. GSSAPI credentials.
 */
public class ProxyCredentialImplementation extends CredentialImplementation {

    /**
     * Constructs a {@link ProxyCredentialImplementation}. Complete API unclear at the moment.
     * 
     * @param adaptorName
     *            the name of the adaptor creating this credential  
     * @param uniqueID
     *            a unique ID for this credential
     * @param properties
     *            properties to take into account when creating this credential. 
     */
    public ProxyCredentialImplementation(String adaptorName, String uniqueID, XenonProperties properties) {
        super(adaptorName, uniqueID, properties, null, null);
    }
    
    @Override
    public String toString() {
        return "ProxyCredentialImplementation [adaptorName=" + getAdaptorName() + ", username=" + getUsername() + "]";
    }
}
