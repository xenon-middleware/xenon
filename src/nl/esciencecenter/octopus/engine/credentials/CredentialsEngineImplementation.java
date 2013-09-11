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

import nl.esciencecenter.octopus.OctopusException;
import nl.esciencecenter.octopus.OctopusRuntimeException;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;

public class CredentialsEngineImplementation implements Credentials {
    private final OctopusEngine octopusEngine;

    public CredentialsEngineImplementation(OctopusEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
    }

    @Override
    public Credential newCertificateCredential(String scheme, String certfile, String username, char[] password,
            Map<String, String> properties) throws OctopusException {
        Adaptor adaptor = octopusEngine.getAdaptorFor(scheme);
        return adaptor.credentialsAdaptor().newCertificateCredential(scheme, certfile, username, password, properties);
    }

    @Override
    public Credential newPasswordCredential(String scheme, String username, char[] password, Map<String, String> properties)
            throws OctopusException {
        Adaptor adaptor = octopusEngine.getAdaptorFor(scheme);
        return adaptor.credentialsAdaptor().newPasswordCredential(scheme, username, password, properties);
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

    @Override
    public boolean isOpen(Credential credential) throws OctopusException {
        return getCredentialsAdaptor(credential).isOpen(credential);
    }

    private Credentials getCredentialsAdaptor(Credential credential) {
        try {
            Adaptor adaptor = octopusEngine.getAdaptor(credential.getAdaptorName());
            return adaptor.credentialsAdaptor();
        } catch (OctopusException e) {
            // This is a case that should never occur, the adaptor was already created, it cannot disappear suddenly.
            // Therefore, we make this a runtime exception.
            throw new OctopusRuntimeException("CredentialEngine", "Could not find adaptor named " + credential.getAdaptorName(),
                    e);
        }
    }

}
