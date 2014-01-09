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

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.Credential;

import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

/**
 * Grid Proxy Credential.
 * 
 * Wrap around a proxy credential for example in "/tmp/x509up_u1000"
 * 
 * @author Piter T. de Boer
 */
public class GlobusProxyCredential implements Credential {

    private GlobusCredential globusCredential = null;

    private GlobusProxyCredentials credentialFactory = null;

    public GlobusProxyCredential(GlobusProxyCredentials globusProxyCredentials, String proxyFilepath) throws XenonException {
        this.credentialFactory = globusProxyCredentials;
        this.globusCredential = credentialFactory.loadGlobusProxyFile(proxyFilepath);
    }

    public GlobusProxyCredential(GlobusProxyCredentials globusProxyCredentials, GlobusCredential globusCredential)
            throws XenonException {
        this.credentialFactory = globusProxyCredentials;
        this.globusCredential = globusCredential;
    }

    @Override
    public String getAdaptorName() {
        return GftpAdaptor.ADAPTOR_NAME;
    }

    @Override
    public Map<String, String> getProperties() {
        // todo: return proxy properties like timeleft, user subject, etc. 
        return null;
    }

    /**
     * Convert Globus Credential to GSS Credential.
     * 
     * @return new GSS Credential created from this GlobusCredential.
     * @throws XenonException
     */
    public GSSCredential createGSSCredential() throws XenonException {
        try {
            return new GlobusGSSCredentialImpl(globusCredential, GSSCredential.DEFAULT_LIFETIME);
        } catch (GSSException e) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "GSSException:" + e.getMessage(), e);
        }
    }

    /**
     * @return User Subject String of the proxy.
     */
    public String getUserSubject() {
        return this.globusCredential.getSubject();
    }

    /**
     * Return Issuer of the proxy credential. The "issuer" of a <strong>proxy</strong> credential is the owner of the certificate itself, not the
     * CA.
     * 
     * @return Issuer Principle
     */
    public String getIssuer() {
        return this.globusCredential.getIssuer();
    }

    /**
     * @return proxy lifetime left in seconds.
     */
    public long getTimeLeftInSeconds() {
        return this.globusCredential.getTimeLeft();
    }

    /**
     * Return actual GlobusCredential object. 
     * @return actual GlobusCredential object.
     */
    public GlobusCredential getGlobusCredential() {
        return this.globusCredential;
    }

}
