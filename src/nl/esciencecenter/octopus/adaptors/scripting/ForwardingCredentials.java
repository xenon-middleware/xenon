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
package nl.esciencecenter.octopus.adaptors.scripting;

import java.util.Map;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;

/**
 * 
 * Credentials implementation which forwards all requests to another adaptor by replacing the scheme with the given target, and
 * forwarding the request to the engine again.
 * 
 * @author Niels Drost
 * 
 */
public class ForwardingCredentials implements Credentials {

    private final OctopusEngine octopusEngine;
    private final String targetScheme;

    public ForwardingCredentials(OctopusEngine octopusEngine, String targetScheme) {
        this.octopusEngine = octopusEngine;
        this.targetScheme = targetScheme;
    }

    @Override
    public Credential newCertificateCredential(String scheme, Map<String, String> properties, String certfile, String username,
            char[] password) throws OctopusException {
        return octopusEngine.credentials().newCertificateCredential(targetScheme, properties, certfile, username, password);
    }

    @Override
    public Credential newPasswordCredential(String scheme, Map<String, String> properties, String username, char[] password)
            throws OctopusException {
        return octopusEngine.credentials().newPasswordCredential(targetScheme, properties, username, password);
    }

    @Override
    public Credential newProxyCredential(String scheme, Map<String, String> properties, String host, int port, String username,
            char[] password) throws OctopusException {
        return octopusEngine.credentials().newProxyCredential(targetScheme, properties, host, port, username, password);
    }

    @Override
    public Credential getDefaultCredential(String scheme) throws OctopusException {
        return octopusEngine.credentials().getDefaultCredential(targetScheme);
    }
}