package nl.esciencecenter.xenon.adaptors.gftp;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.Credential;

/**
 * Grid Proxy Credential.
 * 
 * Wrap around a proxy credential for example in "/tmp/x509up_u1000"
 * 
 * @author Piter T. de Boer
 */
public class GlobusProxyCredential implements Credential {
    private String proxyFilepath = null;

    private GlobusCredential globusCredential = null;

    private List<X509Certificate> certificates = null;

    private GlobusProxyCredentials credentialFactory = null;

    public GlobusProxyCredential(GlobusProxyCredentials globusProxyCredentials, String proxyFilepath) throws XenonException {
        this.credentialFactory = globusProxyCredentials;
        initCACertificates();
        this.proxyFilepath = proxyFilepath;
        this.globusCredential = loadProxyFile(proxyFilepath);
    }

    private GlobusCredential loadProxyFile(String proxyFile) throws XenonException {
        try {
            // update static loaded certificates: 
            GftpUtil.staticUpdateTrustedCertificates(certificates);

            return new GlobusCredential(proxyFilepath);
        } catch (GlobusCredentialException e) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }
    }

    @Override
    public String getAdaptorName() {
        return GftpAdaptor.ADAPTOR_NAME;
    }

    @Override
    public Map<String, String> getProperties() {
        return null;
    }

    public GSSCredential createGSSCredential() throws XenonException {
        try {
            return new GlobusGSSCredentialImpl(globusCredential, GSSCredential.DEFAULT_LIFETIME);
        } catch (GSSException e) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "GSSException:" + e.getMessage(), e);
        }
    }

    protected void initCACertificates() {

        this.certificates = GftpUtil.loadX509Certificates();
    }

}
