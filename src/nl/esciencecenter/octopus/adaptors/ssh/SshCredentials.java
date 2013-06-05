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
package nl.esciencecenter.octopus.adaptors.ssh;

import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.credentials.CertificateCredentialImplementation;
import nl.esciencecenter.octopus.engine.credentials.PasswordCredentialImplementation;
import nl.esciencecenter.octopus.engine.credentials.ProxyCredentialImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusException;

public class SshCredentials implements Credentials {

    private static int currentID = 1;

    private static synchronized String getNewUniqueID() {
        String res = "ssh" + currentID;
        currentID++;
        return res;
    }

    OctopusProperties properties;
    SshAdaptor adaptor;
    OctopusEngine octopusEngine;

    public SshCredentials(OctopusProperties properties, SshAdaptor sshAdaptor, OctopusEngine octopusEngine) {
        this.properties = properties;
        this.adaptor = sshAdaptor;
        this.octopusEngine = octopusEngine;
    }

    @Override
    public Credential newCertificateCredential(String scheme, Properties properties, String keyfile, String certfile,
            String username, char [] password) throws OctopusException {
        return new CertificateCredentialImplementation(adaptor.getName(), getNewUniqueID(), 
                new OctopusProperties(properties), keyfile, certfile, username, password);
    }

    @Override
    public Credential newPasswordCredential(String scheme, Properties properties, String username, char [] password)
            throws OctopusException {
        return new PasswordCredentialImplementation(adaptor.getName(), getNewUniqueID(),
                new OctopusProperties(properties), username, password);
    }

    @Override
    public Credential newProxyCredential(String scheme, Properties properties, String host, int port, String username,
            char [] password) throws OctopusException {
        return new ProxyCredentialImplementation(adaptor.getName(), getNewUniqueID(),
                new OctopusProperties(properties), host, port, username, password);
    }

    @Override
    public Credential getDefaultCredential(String scheme) throws OctopusException {
        return adaptor.getDefaultCredential();
    }
}
