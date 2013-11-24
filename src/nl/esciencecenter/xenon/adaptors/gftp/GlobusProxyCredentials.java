/*
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.esciencecenter.xenon.adaptors.gftp;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.engine.XenonProperties;

/**
 * Grid Proxy Credential Factory based on Globus (Proxy) Credentials. 
 * 
 * @author Piter T. de Boer
 */
public class GlobusProxyCredentials implements Credentials {

    public static final Logger logger = LoggerFactory.getLogger(GlobusProxyCredentials.class);

    public static final String supportedSchemes[] = { GftpUtil.GFTP_SCHEME,GftpUtil.GSIFTP_SCHEME, "srm", "lfc" };

    private List<X509Certificate> certificates;

    public GlobusProxyCredentials(XenonProperties properties, GftpAdaptor gftpAdaptor) {
        // static init Globus ! 
        reloadCACertificates();
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
        return "/tmp/testproxy"; // copy of user proxy, for example /tmp/x509_u666
    }

    public GlobusProxyCredential getDefaultProxyCredential() throws XenonException {
        return loadProxy(getProxyFileLocation());
    }

    public GlobusProxyCredential loadProxy(String proxyFile) throws XenonException {
        return new GlobusProxyCredential(this, proxyFile);
    }
    
    /**
     * Create new Globus (Grid) Proxy credential from user certificate credentials. 
     * @param certfile - private certificate key file, for example ~/.globus/userkey.pem 
     * @param userVOinfo - optional User and VO (+role) information, may be null. Only needed for VOMS enabled proxies. 
     * @param passphrase - Grid Certificate Passphrase. 
     * @return new created Grid Proxy Credential as Globus Credential. 
     * @throws XenonException
     */
    public GlobusProxyCredential createProxy(String certfile, String userVOinfo, char[] passphrase) throws XenonException {
        logger.error("FIXME:createProxy()");
        throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "FIXME: createProxy(): Not supported yet.");
    }

    /**
     * Load proxy certificate and return as GlobusCredential.  
     * @param proxyFilepath - path to proxy, for example /tmp/x509_u666
     * @return GlobusCredentials from proxy certificate. 
     * @throws XenonException 
     */
    public GlobusCredential loadProxyFile(String proxyFilepath) throws XenonException {
      
        // Refresh Trusted Certificates before creating new credential, more could have been added. 
        reloadCACertificates(); 
        
        try {
            
            // Update static loaded certificates: 
            GftpUtil.staticUpdateTrustedCertificates(certificates);
            // 
            return new GlobusCredential(proxyFilepath);
            
        } catch (GlobusCredentialException e) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }
    }
    
    /**
     * Reload Trusted (CA) certificates from default locations. 
     */
    protected void reloadCACertificates() {

        this.certificates = GftpUtil.loadX509Certificates();
    }


}
