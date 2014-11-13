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

import java.util.Hashtable;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.Credential;

import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

/**
 * Grid Proxy Credential. Wraps around a proxy credential for example loaded from "/tmp/x509up_u1000" or a created
 * GlobusCredential.
 * 
 * @author Piter T. de Boer
 */
public class GlobusProxyCredential implements Credential {

    private GlobusCredential globusCredential = null;

    private GlobusProxyCredentials credentialFactory = null;

    private Map<String, String> properties = new Hashtable<String, String>();

    public GlobusProxyCredential(GlobusProxyCredentials globusProxyCredentials, String proxyFilepath, Map<String, String> props)
            throws XenonException {
        this.updateProperties(props, true);
        this.credentialFactory = globusProxyCredentials;
        this.globusCredential = credentialFactory.loadGlobusProxyFile(proxyFilepath);
        setProxyFilePath(proxyFilepath);
    }

    public GlobusProxyCredential(GlobusProxyCredentials globusProxyCredentials, GlobusCredential globusCredential,
            Map<String, String> props) throws XenonException {
        this.updateProperties(props, true);
        this.credentialFactory = globusProxyCredentials;
        this.globusCredential = globusCredential;
    }

    @Override
    public String getAdaptorName() {
        return GftpAdaptor.ADAPTOR_NAME;
    }

    /**
     * Update Credential Properties.
     * 
     * @param newProps
     *            - new properties to add to this one.
     * @param clearPrevious
     *            - clear previous stored properties.
     */
    protected void updateProperties(Map<String, String> newProps, boolean clearPrevious) {
        if (clearPrevious) {
            properties.clear();
        }
        properties.putAll(newProps);
    }

    protected void setProxyFilePath(String path) {
        if (path == null) {
            // clear property. 
            this.properties.remove(GlobusProxyCredentials.PROPERTY_USER_KEY_FILE);
        } else {
            properties.put(GlobusProxyCredentials.PROPERTY_USER_KEY_FILE, path);
        }
    }

    /**
     * If the proxy has been saved to a file, or loaded from a file, this method will return that path.
     * 
     * @return path to actual proxy file, if loaded from or save to a file. Null otherwise.
     */
    public String getProxyFilePath() {
        return properties.get(GlobusProxyCredentials.PROPERTY_USER_KEY_FILE);
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
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
     * Return Issuer of the proxy credential. The "issuer" of a <strong>proxy</strong> credential is the owner of the certificate
     * itself, not the CA.
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
     * 
     * @return actual GlobusCredential object.
     */
    public GlobusCredential getGlobusCredential() {
        return this.globusCredential;
    }

    public GlobusProxyCredentials getGlobusProxyCredentials() {
        return this.credentialFactory;
    }

    public void invalidate(boolean deleteProxy) {

        if (deleteProxy) {
            credentialFactory.delete(this);
        }

        this.globusCredential = null;
    }

    public boolean isValid() {
        if (globusCredential == null) {
            return false;
        }

        if (this.getTimeLeftInSeconds() > 0) {
            return true;
        }

        return false;
    }

}
