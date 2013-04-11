package nl.esciencecenter.octopus.engine.credentials;

import java.net.URI;
import java.util.UUID;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Path;

public class CredentialsEngine implements Credentials {
    private final OctopusEngine octopusEngine;

    public CredentialsEngine(OctopusEngine octopusEngine) {
        this.octopusEngine = octopusEngine;
    }

    @Override
    public Credential newCertificateCredential(Path keyfile, Path certfile, String username, String password)
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

//    @Override
//    public UUID newCertificateCredential(Path keyfile, Path certfile, String username, String password, URI... validFor)
//            throws OctopusException {
//        UUID uuid = UUID.randomUUID();
//
//        for (URI uri : validFor) {
//            Adaptor adaptor = octopusEngine.getAdaptorFor(uri.getScheme());
//            adaptor.credentialsAdaptor().newCertificateCredential(uuid, keyfile, certfile, username, password, uri);
//        }
//
//        return uuid;
//    }
//
//    @Override
//    public UUID newPasswordCredential(String username, String password, URI... validFor) throws OctopusException {
//        UUID uuid = UUID.randomUUID();
//
//        for (URI uri : validFor) {
//            Adaptor adaptor = octopusEngine.getAdaptorFor(uri.getScheme());
//
//            if (uri.getUserInfo() != null && !uri.getUserInfo().equals(username)) {
//                throw new OctopusException("CredentialsEngine", "If usernames in URIs are given, they must be identical to the username parameter.");
//            }
//
//            adaptor.credentialsAdaptor().newPasswordCredential(uuid, username, password, uri);
//        }
//
//        return uuid;
//    }
//
//    @Override
//    public UUID newProxyCredential(String host, int port, String username, String password, URI... validFor)
//            throws OctopusException {
//        UUID uuid = UUID.randomUUID();
//
//        for (URI uri : validFor) {
//            Adaptor adaptor = octopusEngine.getAdaptorFor(uri.getScheme());
//            adaptor.credentialsAdaptor().newProxyCredential(uuid, host, port, username, password, uri);
//        }
//
//        return uuid;
//    }
//
//    @Override
//    public void remove(UUID credentialID, URI... validFor) throws OctopusException {
//        if (validFor.length == 0) {
//            for (Adaptor adaptor : octopusEngine.getAdaptors()) {
//                adaptor.credentialsAdaptor().remove(credentialID, null);
//            }
//            return;
//        }
//
//        for (URI uri : validFor) {
//            Adaptor adaptor = octopusEngine.getAdaptorFor(uri.getScheme());
//            adaptor.credentialsAdaptor().remove(credentialID, uri);
//        }
//    }
}
