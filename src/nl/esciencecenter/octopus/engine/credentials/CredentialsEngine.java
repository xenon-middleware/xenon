package nl.esciencecenter.octopus.engine.credentials;

import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;

public class CredentialsEngine implements Credentials {
    private final OctopusEngine octopusEngine;

    public CredentialsEngine(OctopusEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
    }

    @Override
    public Credential newCertificateCredential(String scheme, Properties properties, String keyfile, String certfile, String username, String password)
            throws OctopusException {
        Adaptor adaptor = octopusEngine.getAdaptorFor(scheme);
        return adaptor.credentialsAdaptor().newCertificateCredential(scheme, properties, keyfile, certfile, username, password);
    }

    @Override
    public Credential newPasswordCredential(String scheme, Properties properties, String username, String password) throws OctopusException {
        Adaptor adaptor = octopusEngine.getAdaptorFor(scheme);
        return adaptor.credentialsAdaptor().newPasswordCredential(scheme, properties, username, password);
    }

    @Override
    public Credential newProxyCredential(String scheme, Properties properties, String host, int port, String username, String password) throws OctopusException {
        Adaptor adaptor = octopusEngine.getAdaptorFor(scheme);
        return adaptor.credentialsAdaptor().newProxyCredential(scheme, properties, host, port, username, password);
    }
}
