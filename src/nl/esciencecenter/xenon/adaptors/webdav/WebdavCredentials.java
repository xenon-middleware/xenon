package nl.esciencecenter.xenon.adaptors.webdav;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.credentials.PasswordCredentialImplementation;

public class WebdavCredentials implements Credentials {
    private static int currentID = 1;
    private XenonProperties properties;
    private WebdavAdaptor adaptor;

    private static synchronized String getNewUniqueID() {
        String res = "webdav" + currentID;
        currentID++;
        return res;
    }

    public WebdavCredentials(XenonProperties properties, WebdavAdaptor webdavAdaptor) {
        this.properties = properties;

        if (webdavAdaptor == null) {
            throw new IllegalArgumentException("Adaptor can not be null!");
        }

        adaptor = webdavAdaptor;
    }

    @Override
    public Credential newCertificateCredential(String scheme, String certfile, String username, char[] password,
            Map<String, String> properties) throws XenonException {
        throw new XenonException(adaptor.getName(), "CertificateCredential not supported!");
    }

    @Override
    public Credential newPasswordCredential(String scheme, String username, char[] password, Map<String, String> properties)
            throws XenonException {
        XenonProperties xenonProperties = new XenonProperties(adaptor.getSupportedProperties(Component.CREDENTIALS), properties);
        return new PasswordCredentialImplementation(adaptor.getName(), getNewUniqueID(), xenonProperties, username, password);
    }

    @Override
    public Credential getDefaultCredential(String scheme) throws XenonException {
        return null; // TODO
    }

    @Override
    public void close(Credential credential) throws XenonException {
        // ignored
    }

    @Override
    public boolean isOpen(Credential credential) throws XenonException {
        return true;
    }

}
