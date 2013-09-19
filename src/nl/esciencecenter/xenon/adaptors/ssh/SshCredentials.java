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
package nl.esciencecenter.xenon.adaptors.ssh;

import java.io.File;
import java.util.Map;

import nl.esciencecenter.xenon.CobaltException;
import nl.esciencecenter.xenon.InvalidCredentialException;
import nl.esciencecenter.xenon.CobaltPropertyDescription.Component;
import nl.esciencecenter.xenon.credentials.CertificateNotFoundException;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.engine.CobaltProperties;
import nl.esciencecenter.xenon.engine.credentials.CertificateCredentialImplementation;
import nl.esciencecenter.xenon.engine.credentials.PasswordCredentialImplementation;

public class SshCredentials implements Credentials {

    private static int currentID = 1;

    private static synchronized String getNewUniqueID() {
        String res = "ssh" + currentID;
        currentID++;
        return res;
    }

    private final CobaltProperties properties;
    private final SshAdaptor adaptor;

    public SshCredentials(CobaltProperties properties, SshAdaptor sshAdaptor) {
        this.properties = properties;

        if (sshAdaptor == null) {
            throw new IllegalArgumentException("Adaptor can not be null!");
        }

        this.adaptor = sshAdaptor;
    }

    @Override
    public Credential newCertificateCredential(String scheme, String certfile, String username, char[] password,
            Map<String, String> properties) throws CobaltException {

        CobaltProperties p = new CobaltProperties(adaptor.getSupportedProperties(Component.CREDENTIALS), properties);

        if (!new File(certfile).exists()) { 
            throw new CertificateNotFoundException(SshAdaptor.ADAPTOR_NAME, "Certificate file not found: " + certfile);
        }
        
        return new CertificateCredentialImplementation(adaptor.getName(), getNewUniqueID(), p, certfile, username, password);
    }

    @Override
    public Credential newPasswordCredential(String scheme, String username, char[] password, Map<String, String> properties)
            throws CobaltException {

        CobaltProperties p = new CobaltProperties(adaptor.getSupportedProperties(Component.CREDENTIALS), properties);

        return new PasswordCredentialImplementation(adaptor.getName(), getNewUniqueID(), p, username, password);
    }

    @Override
    public Credential getDefaultCredential(String scheme) throws CobaltException {

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
    public void close(Credential credential) throws CobaltException {
        // Nothing to do here.
    }

    @Override
    public boolean isOpen(Credential credential) throws CobaltException {
        // Defaults to false
        return false;
    }
}
