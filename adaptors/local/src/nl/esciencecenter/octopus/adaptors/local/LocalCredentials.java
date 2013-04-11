package nl.esciencecenter.octopus.adaptors.local;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.exceptions.OctopusException;

public class LocalCredentials implements Credentials {

    @Override
    public Credential newCertificateCredential(String keyfile, String certfile, String username, String password)
            throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Credential newPasswordCredential(String username, String password) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Credential newProxyCredential(String host, int port, String username, String password) throws OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

}
