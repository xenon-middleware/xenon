package nl.esciencecenter.octopus.adaptors.local;

import java.net.URI;
import java.util.UUID;

import nl.esciencecenter.octopus.engine.credentials.CredentialsAdaptor;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Path;

public class LocalCredentials extends CredentialsAdaptor {

    @Override
    public void newCertificateCredential(UUID uuid, Path keyfile, Path certfile, String username, String password, URI validFor)
            throws OctopusException {
        throw new OctopusException(getClass().getName(), "The local adaptor does not need or support credentials");
    }

    @Override
    public UUID newPasswordCredential(UUID uuid, String username, String password, URI validFor) throws OctopusException {
        throw new OctopusException(getClass().getName(), "The local adaptor does not need or support credentials");
    }

    @Override
    public UUID newProxyCredential(UUID uuid, String host, int port, String username, String password, URI validFor)
            throws OctopusException {
        throw new OctopusException(getClass().getName(), "The local adaptor does not need or support credentials");
    }

    @Override
    public void remove(UUID credentialID, URI validFor) throws OctopusException {
        // We don have credentials, so we should ignore this.
    }
}
