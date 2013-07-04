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
package nl.esciencecenter.octopus.adaptors.gridengine;

import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;

/**
 * 
 * GridEngine credentials implementation. As GridEngine uses ssh for interacting with the scheduler, actually creates ssh
 * credentials by forwarding the request to the engine again, after replacing the scheme.
 * 
 * @author Niels Drost
 * 
 */
public class GridEngineCredentials implements Credentials {

    private final OctopusEngine octopusEngine;

    public GridEngineCredentials(OctopusEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
    }

    @Override
    public Credential newCertificateCredential(String scheme, Properties properties, String keyfile, String certfile,
            String username, char[] password) throws OctopusException {
        return octopusEngine.credentials().newCertificateCredential("ssh", properties, keyfile, certfile, username, password);
    }

    @Override
    public Credential newPasswordCredential(String scheme, Properties properties, String username, char[] password)
            throws OctopusException {
        return octopusEngine.credentials().newPasswordCredential("ssh", properties, username, password);
    }

    @Override
    public Credential newProxyCredential(String scheme, Properties properties, String host, int port, String username,
            char[] password) throws OctopusException {
        return octopusEngine.credentials().newProxyCredential("ssh", properties, host, port, username, password); 
    }

    @Override
    public Credential getDefaultCredential(String scheme) throws OctopusException {
        return octopusEngine.credentials().getDefaultCredential("ssh");
    }
}