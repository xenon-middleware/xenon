package nl.esciencecenter.xenon.adaptors.gftp;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.engine.XenonProperties;

/**
 * Grid Proxy Credential Factory.
 * 
 * @author Piter T. de Boer
 */
public class GlobusProxyCredentials implements Credentials {

    public static final Logger logger = LoggerFactory.getLogger(GlobusProxyCredentials.class);

    public static final String supportedSchemes[] = { GftpUtil.GFTP_SCHEME,GftpUtil.GSIFTP_SCHEME, "srm", "lfc" };

    public GlobusProxyCredentials(XenonProperties properties, GftpAdaptor gftpAdaptor) {

    }

    @Override
    public GlobusProxyCredential newCertificateCredential(String scheme, String certfile, String userinfo, char[] password,
            Map<String, String> properties) throws XenonException {

        if (password != null) {
            return createProxy(certfile, userinfo, password);
        } else {
            return loadProxy(certfile);
        }
    }

    @Override
    public GridSshCredential
            newPasswordCredential(String scheme, String username, char[] password, Map<String, String> properties)
                    throws XenonException {
        throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "newPasswordCredential(): Not supported");
    }

    @Override
    public Credential getDefaultCredential(String scheme) throws XenonException {
        return getDefaultProxyCredential();
    }

    @Override
    public void close(Credential credential) throws XenonException {
        // nothing to close; 
    }

    @Override
    public boolean isOpen(Credential credential) throws XenonException {
        return false;
    }

    public String getProxyFileLocation() {
        logger.error("FIXME:getProxyFileLocation()");
        return null;
    }

    public GlobusProxyCredential getDefaultProxyCredential() throws XenonException {
        return loadProxy(getProxyFileLocation());
    }

    public GlobusProxyCredential loadProxy(String proxyFile) throws XenonException {
        return new GlobusProxyCredential(this, proxyFile);
    }

    public GlobusProxyCredential createProxy(String certfile, String userinfo, char[] password) throws XenonException {

        throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "createProxy(): Not supported yet.");
    }

}
