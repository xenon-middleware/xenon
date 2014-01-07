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
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.omg.PortableInterceptor.USER_EXCEPTION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.UnknownPropertyException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.engine.PropertyTypeException;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.XenonPropertyDescriptionImplementation;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;

/**
 * Grid Proxy Credential Factory based on Globus (Proxy) Credentials.
 * 
 * @author Piter T. de Boer
 */
public class GlobusProxyCredentials implements Credentials {

    public static final Logger logger = LoggerFactory.getLogger(GlobusProxyCredentials.class);

    public static final String PROPERTY_PREFIX = "xenon.globus.";

    /**
     * Location of 'userkey.pem' file. Ussualy it is ~/.globus/userkey.pem.
     */
    public static final String PROPERTY_USER_KEY_FILE = PROPERTY_PREFIX + "user.userkey";

    /**
     * Location of 'usercert.pem' file. Ussualy it is ~/.globus/usercert.pem.
     */
    public static final String PROPERTY_USER_CERT_FILE = PROPERTY_PREFIX + "user.usercert";

    /**
     * Location of already created user proxy file (if not at default location).
     */
    public static final String PROPERTY_USER_X509_PROXY = PROPERTY_PREFIX + "user.x509proxy";

    /**
     * Location of user accepted (CA) certificates if not in ~/.globus/certificates.
     */
    public static final String PROPERTY_USER_CERTIFICATES_DIR = PROPERTY_PREFIX + "user.certificates";

    public static final ImmutableArray<XenonPropertyDescription> GLOBUS_PROPERTIES = new ImmutableArray<XenonPropertyDescription>(

    new XenonPropertyDescriptionImplementation(PROPERTY_USER_KEY_FILE, Type.STRING, EnumSet.of(Component.CREDENTIALS), null,
            "Location of User Key file or userkey.pem if not at ~/.globus/userkey.pem"),

    new XenonPropertyDescriptionImplementation(PROPERTY_USER_CERT_FILE, Type.STRING, EnumSet.of(Component.CREDENTIALS), null,
            "Location of user Certificate file or 'usercert.pem' file if not at ~/.globus/usercert.pem"),

    new XenonPropertyDescriptionImplementation(PROPERTY_USER_X509_PROXY, Type.STRING, EnumSet.of(Component.CREDENTIALS), null,
            "Location of user X509 proxy file if not at default location /tmp/x509_{userid}"),

    new XenonPropertyDescriptionImplementation(PROPERTY_USER_CERTIFICATES_DIR, Type.STRING, EnumSet.of(Component.CREDENTIALS),
            null, "Location of user certificates if not at default location ~/.globus/certificates"));

    // --- 
    // Instance
    // ---

    protected List<X509Certificate> certificates;
    protected String userKeyfile;
    protected String userCertfile;
    protected String userProxyfile;
    protected String userCertificatesDir;

    public GlobusProxyCredentials(XenonProperties properties, GftpAdaptor gftpAdaptor) {

        updateProperties(properties);
        // static init Globus ! 
        reloadCACertificates();

    }

    protected void updateProperties(XenonProperties properties) {
        userKeyfile = getPropertyOrNull(properties, PROPERTY_USER_KEY_FILE);
        userCertfile = getPropertyOrNull(properties, PROPERTY_USER_CERT_FILE);
        userProxyfile = getPropertyOrNull(properties, PROPERTY_USER_X509_PROXY);
        userCertificatesDir = getPropertyOrNull(properties, PROPERTY_USER_CERTIFICATES_DIR);

    }

    protected String getPropertyOrNull(XenonProperties properties, String propName) {
        String val = null;

        try {
            val = properties.getStringProperty(propName);
        } catch (UnknownPropertyException | PropertyTypeException e) {
            logger.warn("Unknown property {}", propName);
        }

        return val;
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

        if (this.userProxyfile != null) {
            return userProxyfile;
        } else {
            // static configuration. 
            return GlobusUtil.getStaticCoGProperties().getProxyFile();
        }
    }

    public GlobusProxyCredential getDefaultProxyCredential() throws XenonException {
        return loadProxy(getProxyFileLocation());
    }

    public GlobusProxyCredential loadProxy(String proxyFile) throws XenonException {
        return new GlobusProxyCredential(this, proxyFile);
    }

    /**
     * Create new Globus (Grid) Proxy credential from user certificate credentials.
     * 
     * @param certfile
     *            - directory containing user certificate files: 'usercert.key' and 'userkey.pem', for example: ~/.globus/.
     * @param userVOinfo
     *            - optional User and VO (+role) information, may be null. Only needed for VOMS enabled proxies.
     * @param passphrase
     *            - User Grid Certificate Passphrase.
     * @return new created Grid Proxy Credential as Globus Credential.
     * @throws XenonException
     */
    public GlobusProxyCredential createProxy(String certdirectory, String userVOinfo, char[] passphrase) throws XenonException {
        logger.error("FIXME:createProxy()");
        throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "FIXME: createProxy(): Not supported yet.");
    }

    /**
     * Load proxy certificate and return as GlobusCredential.
     * 
     * @param proxyFilepath
     *            - path to proxy, for example /tmp/x509_u666
     * @return GlobusCredentials from proxy certificate.
     * @throws XenonException
     */
    public GlobusCredential loadGlobusProxyFile(String proxyFilepath) throws XenonException {

        // Refresh Trusted Certificates before creating new credential, more could have been added manually 
        // by user. 
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

        this.certificates = GftpUtil.loadX509Certificates(new String[] { userCertificatesDir });
    }

}
