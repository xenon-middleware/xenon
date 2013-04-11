package nl.esciencecenter.octopus.adaptors.local;

import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.exceptions.OctopusException;

public class LocalCredentials implements Credentials {

    @Override
    public Credential newCertificateCredential(String scheme, Properties properties, String keyfile, String certfile,
            String username, String password) throws OctopusException {
        throw new OctopusException(scheme, "The local adaptor does not need or understand credentials");
    }

    @Override
    public Credential newPasswordCredential(String scheme, Properties properties, String username, String password)
            throws OctopusException {
        throw new OctopusException(scheme, "The local adaptor does not need or understand credentials");
    }

    @Override
    public Credential newProxyCredential(String scheme, Properties properties, String host, int port, String username,
            String password) throws OctopusException {
        throw new OctopusException(scheme, "The local adaptor does not need or understand credentials");
    }
}
