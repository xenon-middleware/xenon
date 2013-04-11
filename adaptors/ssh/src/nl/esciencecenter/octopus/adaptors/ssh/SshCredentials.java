package nl.esciencecenter.octopus.adaptors.ssh;

import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.credentials.CredentialSet;
import nl.esciencecenter.octopus.exceptions.OctopusException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshCredentials implements Credentials {
    private static final Logger logger = LoggerFactory.getLogger(SshFiles.class);

    private OctopusProperties properties;
    private SshAdaptor sshAdaptor;
    private OctopusEngine octopusEngine;

    private CredentialSet credentialSet = new CredentialSet();
    
    public SshCredentials(OctopusProperties properties, SshAdaptor sshAdaptor, OctopusEngine octopusEngine)
            throws OctopusException {
        this.properties = properties;
        this.sshAdaptor = sshAdaptor;
        this.octopusEngine = octopusEngine;
    }

    @Override
    public nl.esciencecenter.octopus.credentials.Credential newCertificateCredential(String keyfile, String certfile,
            String username, String password) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public nl.esciencecenter.octopus.credentials.Credential newPasswordCredential(String username, String password)
            throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public nl.esciencecenter.octopus.credentials.Credential newProxyCredential(String host, int port, String username,
            String password) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }
}
