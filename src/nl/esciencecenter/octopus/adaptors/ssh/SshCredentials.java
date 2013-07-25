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

import nl.esciencecenter.octopus.OctopusPropertyDescription.Level;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.credentials.CertificateCredentialImplementation;
import nl.esciencecenter.octopus.engine.credentials.PasswordCredentialImplementation;
import nl.esciencecenter.octopus.engine.credentials.ProxyCredentialImplementation;
import nl.esciencecenter.octopus.exceptions.InvalidCredentialException;
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
    public Credential newCertificateCredential(String scheme, Map<String,String> properties, String keyfile, String certfile,
            String username, char[] password) throws OctopusException {
        
        OctopusProperties p = new OctopusProperties(adaptor.getSupportedProperties(Level.CREDENTIALS), properties);
        
        return new CertificateCredentialImplementation(adaptor.getName(), getNewUniqueID(), p, keyfile, certfile, username, 
                password);
    }

    @Override
    public Credential newPasswordCredential(String scheme, Map<String,String> properties, String username, char[] password)
            throws OctopusException {
        
        OctopusProperties p = new OctopusProperties(adaptor.getSupportedProperties(Level.CREDENTIALS), properties);
        
        return new PasswordCredentialImplementation(adaptor.getName(), getNewUniqueID(), p, username, password);
    }

    @Override
    public Credential newProxyCredential(String scheme, Map<String,String> properties, String host, int port, String username,
            char[] password) throws OctopusException {
        
        OctopusProperties p = new OctopusProperties(adaptor.getSupportedProperties(Level.CREDENTIALS), properties);
        
        return new ProxyCredentialImplementation(adaptor.getName(), getNewUniqueID(), p, host, port, username, password);
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

        File keyFile = new File(userHome + File.separator + ".ssh" + File.separator + "id_dsa");
        File certFile = new File(userHome + File.separator + ".ssh" + File.separator + "id_dsa.pub");

        if (keyFile.exists() && certFile.exists()) {
            // logger.info("Using default credential: "+ keyFile.getPath());
            return new CertificateCredentialImplementation(adaptor.getName(), getNewUniqueID(),
                    properties, keyFile.getPath(), certFile.getPath(), user, null);
        }

        File keyFile2 = new File(userHome + File.separator + ".ssh" + File.separator + "id_rsa");
        File certFile2 = new File(userHome + File.separator + ".ssh" + File.separator + "id_rsa.pub");

        if (keyFile2.exists() && certFile2.exists()) {
            // logger.info("Using default credential: "+ keyFile2.getPath());
            return new CertificateCredentialImplementation(adaptor.getName(), getNewUniqueID(),
                    properties, keyFile2.getPath(), certFile2.getPath(), user, null);
        }

        throw new InvalidCredentialException(SshAdaptor.ADAPTOR_NAME, "Cannot create a default credential for ssh, tried "
                + keyFile.getPath() + " and " + keyFile2.getPath());
    }
}
