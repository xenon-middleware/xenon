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

     private GlobusProxyCredentials credentialFactory = null;

    public GlobusProxyCredential(GlobusProxyCredentials globusProxyCredentials, String proxyFilepath) throws XenonException {
        this.credentialFactory = globusProxyCredentials;
        this.proxyFilepath = proxyFilepath;
        this.globusCredential = credentialFactory.loadGlobusProxyFile(proxyFilepath);
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



}
