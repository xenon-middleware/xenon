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

import java.util.Map;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.OctopusRuntimeException;
import nl.esciencecenter.octopus.files.FileSystem;

public class CredentialsEngineImplementation implements Credentials {
    private final OctopusEngine octopusEngine;

    public CredentialsEngineImplementation(OctopusEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
    }

    @Override
    public Credential newCertificateCredential(String scheme, Map<String,String> properties, String certfile, String username, 
            char[] password) throws OctopusException {
        Adaptor adaptor = octopusEngine.getAdaptorFor(scheme);
        return adaptor.credentialsAdaptor().newCertificateCredential(scheme, properties, certfile, username, password);
    }

    @Override
    public Credential newPasswordCredential(String scheme, Map<String,String> properties, String username, char[] password)
            throws OctopusException {
        Adaptor adaptor = octopusEngine.getAdaptorFor(scheme);
        return adaptor.credentialsAdaptor().newPasswordCredential(scheme, properties, username, password);
    }

    @Override
    public Credential newProxyCredential(String scheme, Map<String,String> properties, String host, int port, String username,
            char[] password) throws OctopusException {
        Adaptor adaptor = octopusEngine.getAdaptorFor(scheme);
        return adaptor.credentialsAdaptor().newProxyCredential(scheme, properties, host, port, username, password);
    }

    @Override
    public Credential getDefaultCredential(String scheme) throws OctopusException {
        Adaptor adaptor = octopusEngine.getAdaptorFor(scheme);
        return adaptor.credentialsAdaptor().getDefaultCredential(scheme);
    }

    @Override
    public void close(Credential credential) throws OctopusException {
        getCredentialsAdaptor(credential).close(credential);
    }
    
    private Credentials getCredentialsAdaptor(Credential credential) {
        try {
            Adaptor adaptor = octopusEngine.getAdaptor(credential.getAdaptorName());
            return adaptor.credentialsAdaptor();
        } catch (OctopusException e) {
            // This is a case that should never occur, the adaptor was already created, it cannot dissapear suddenly.
            // Therefore, we make this a runtime exception.
            throw new OctopusRuntimeException("CredentialEngine", "Could not find adaptor named " + credential.getAdaptorName(), e);
        }
    }
}
