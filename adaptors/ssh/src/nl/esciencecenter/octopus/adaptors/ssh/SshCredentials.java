package nl.esciencecenter.octopus.adaptors.ssh;

import java.net.URI;
import java.util.ArrayList;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.octopus.OctopusProperties;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.credentials.CertificateCredential;
import nl.esciencecenter.octopus.engine.credentials.Credential;
import nl.esciencecenter.octopus.engine.credentials.CredentialSet;
import nl.esciencecenter.octopus.engine.credentials.CredentialsAdaptor;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Path;

public class SshCredentials extends CredentialsAdaptor {
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
    public void newCertificateCredential(UUID uuid, Path keyfile, Path certfile, String username, String password, URI validFor)
            throws OctopusException {
        CertificateCredential c = new CertificateCredential(keyfile.toUri(), certfile.toUri(), username, password);
        credentialSet.add(c);
        logger.debug("added credential to the set: " + c);
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
    
    protected CredentialSet getCredentialSet() {
        return credentialSet;
    }
    
    Credential getCredentialFor(URI uri) {
        CredentialSet credentialSet = getCredentialSet();
        Credential[] credentials = credentialSet.getAll();
        
        // TODO check everything here. (we will probably not need this anyway)
        for(Credential credential: credentials) {
            logger.debug("cred = " + credential + ", user = " + credential.getUsername() + ", uri user = " + uri.getUserInfo());
            
            if(credential.getUsername().equals(uri.getUserInfo())) {
                return credential;
            }
        }
        
        return null;
    }
}
