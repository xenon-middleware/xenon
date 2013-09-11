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

import nl.esciencecenter.octopus.OctopusException;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.OctopusEngine;

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
    public Credential newCertificateCredential(String scheme, String certfile, String username, char[] password,
            Map<String, String> properties) throws OctopusException {
        return octopusEngine.credentials().newCertificateCredential(targetScheme, certfile, username, password, properties);
    }

    @Override
    public Credential newPasswordCredential(String scheme, String username, char[] password, Map<String, String> properties)
            throws OctopusException {
        return octopusEngine.credentials().newPasswordCredential(targetScheme, username, password, properties);
    }

    @Override
    public Credential getDefaultCredential(String scheme) throws OctopusException {
        return octopusEngine.credentials().getDefaultCredential(targetScheme);
    }

    @Override
    public void close(Credential credential) throws OctopusException {
        throw new OctopusException("scripting", "The Scripting Adaptor does not make credentials, and thus cannot close them");
    }

    @Override
    public boolean isOpen(Credential credential) throws OctopusException {
        throw new OctopusException("scripting", "The Scripting Adaptor does not make credentials, and thus cannot check if " +
        		"they are open");
    }
}