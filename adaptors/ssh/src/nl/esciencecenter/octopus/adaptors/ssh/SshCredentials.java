package nl.esciencecenter.octopus.adaptors.ssh;

import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.credentials.CertificateCredentialImplementation;
import nl.esciencecenter.octopus.engine.credentials.PasswordCredentialImplementation;
import nl.esciencecenter.octopus.engine.credentials.ProxyCredentialImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusException;

public class SshCredentials implements Credentials {
    OctopusProperties properties;
    SshAdaptor adaptor;
    OctopusEngine octopusEngine;

    public SshCredentials(OctopusProperties properties, SshAdaptor sshAdaptor, OctopusEngine octopusEngine) {
        this.properties = properties;
        this.adaptor = sshAdaptor;
        this.octopusEngine = octopusEngine;
    }

    @Override
    public Credential newCertificateCredential(String scheme, Properties properties, String keyfile, String certfile,
            String username, String password) throws OctopusException {
        return new CertificateCredentialImplementation(adaptor.getName(), new OctopusProperties(properties), keyfile, certfile,
                username, password);
    }

    @Override
    public Credential newPasswordCredential(String scheme, Properties properties, String username, String password)
            throws OctopusException {
        return new PasswordCredentialImplementation(adaptor.getName(), new OctopusProperties(properties), username, password);
    }

    @Override
    public Credential newProxyCredential(String scheme, Properties properties, String host, int port, String username,
            String password) throws OctopusException {
        return new ProxyCredentialImplementation(adaptor.getName(), new OctopusProperties(properties), host, port, username,
                password);
    }
}
