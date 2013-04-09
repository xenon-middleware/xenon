package nl.esciencecenter.octopus.adaptors.ssh;

import java.net.URI;
import java.util.UUID;

import nl.esciencecenter.octopus.ImmutableTypedProperties;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.credentials.CredentialsAdaptor;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Path;

public class SshCredentials extends CredentialsAdaptor {
    private ImmutableTypedProperties properties;
    private SshAdaptor sshAdaptor;
    private OctopusEngine octopusEngine;

    public SshCredentials(ImmutableTypedProperties properties, SshAdaptor sshAdaptor, OctopusEngine octopusEngine)
            throws OctopusException {
        this.properties = properties;
        this.sshAdaptor = sshAdaptor;
        this.octopusEngine = octopusEngine;
    }

    @Override
    public void newCertificateCredential(UUID uuid, Path keyfile, Path certfile, String username, String password, URI validFor)
            throws OctopusException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public UUID newPasswordCredential(UUID uuid, String username, String password, URI validFor) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UUID newProxyCredential(UUID uuid, String host, int port, String username, String password, URI validFor)
            throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void remove(UUID credentialID, URI validFor) throws OctopusException {
        // TODO Auto-generated method stub
        
    }
}
