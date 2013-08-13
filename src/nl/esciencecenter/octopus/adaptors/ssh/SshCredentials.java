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

import java.io.File;
import java.util.Map;

import nl.esciencecenter.octopus.OctopusPropertyDescription.Component;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.credentials.CertificateCredentialImplementation;
import nl.esciencecenter.octopus.engine.credentials.PasswordCredentialImplementation;
import nl.esciencecenter.octopus.exceptions.InvalidCredentialException;
import nl.esciencecenter.octopus.exceptions.OctopusException;

public class SshCredentials implements Credentials {

    private static int currentID = 1;

    private static synchronized String getNewUniqueID() {
        String res = "ssh" + currentID;
        currentID++;
        return res;
    }

    private final OctopusProperties properties;
    private final SshAdaptor adaptor;

    public SshCredentials(OctopusProperties properties, SshAdaptor sshAdaptor) {
        this.properties = properties;

        if (sshAdaptor == null) {
            throw new IllegalArgumentException("Adaptor can not be null!");
        }

        this.adaptor = sshAdaptor;
    }

    @Override
    public Credential newCertificateCredential(String scheme, String certfile, String username, char[] password,
            Map<String, String> properties) throws OctopusException {

        OctopusProperties p = new OctopusProperties(adaptor.getSupportedProperties(Component.CREDENTIALS), properties);

        return new CertificateCredentialImplementation(adaptor.getName(), getNewUniqueID(), p, certfile, username, password);
    }

    @Override
    public Credential newPasswordCredential(String scheme, String username, char[] password, Map<String, String> properties)
            throws OctopusException {

        OctopusProperties p = new OctopusProperties(adaptor.getSupportedProperties(Component.CREDENTIALS), properties);

        return new PasswordCredentialImplementation(adaptor.getName(), getNewUniqueID(), p, username, password);
    }

    @Override
    public Credential getDefaultCredential(String scheme) throws OctopusException {

        String userHome = System.getProperty("user.home");

        if (userHome == null) {
            throw new InvalidCredentialException(SshAdaptor.ADAPTOR_NAME, "Cannot get user home directory.");
        }

        String user = System.getProperty("user.name");

        if (user == null) {
            throw new InvalidCredentialException(SshAdaptor.ADAPTOR_NAME, "Cannot get user name.");
        }

        File certFile = new File(userHome + File.separator + ".ssh" + File.separator + "id_dsa");

        if (certFile.exists()) {
            return new CertificateCredentialImplementation(adaptor.getName(), getNewUniqueID(), properties, certFile.getPath(),
                    user, null);
        }

        File certFile2 = new File(userHome + File.separator + ".ssh" + File.separator + "id_rsa");

        if (certFile2.exists()) {
            return new CertificateCredentialImplementation(adaptor.getName(), getNewUniqueID(), properties, certFile2.getPath(),
                    user, null);
        }

        throw new InvalidCredentialException(SshAdaptor.ADAPTOR_NAME, "Cannot create a default credential for ssh, tried "
                + certFile.getPath() + " and " + certFile2.getPath());
    }

    @Override
    public void close(Credential credential) throws OctopusException {
        // TODO: implement
    }
}
