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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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

import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Grid Proxy Credential Factory based on Globus (Proxy) Credentials. <br>
 * Typically a GlobusProxyCrential object is linked to one user account and (proxy) credential.
 * 
 * @author Piter T. de Boer
 */
public class GlobusProxyCredentials implements Credentials {

    public static final Logger logger = LoggerFactory.getLogger(GlobusProxyCredentials.class);

    public static final String PROPERTY_PREFIX = "xenon.globus.";

    /**
     * Location of 'userkey.pem' file. Usually it is ~/.globus/userkey.pem.
     */
    public static final String PROPERTY_USER_KEY_FILE = PROPERTY_PREFIX + "user.userkey";

    /**
     * Location of 'usercert.pem' file. Usually it is ~/.globus/usercert.pem.
     */
    public static final String PROPERTY_USER_CERT_FILE = PROPERTY_PREFIX + "user.usercert";

    /**
     * Location of already created user proxy file (if not at default location).
     */
    public static final String PROPERTY_USER_X509_PROXY = PROPERTY_PREFIX + "user.x509proxy";

    /**
     * Default proxy life time in hours when creating a new proxy. Typically between 1 and 24 hours.
     */
    public static final String PROPERTY_USER_PROXY_LIFETIME_HOURS = PROPERTY_PREFIX + "user.proxy.lifeTimeInHours";

    public static final String USERKEY_PEM = "userkey.pem";

    public static final String USERCERT_PEM = "usercert.pem";

    public static final ImmutableArray<XenonPropertyDescription> GLOBUS_CREDENTIAL_PROPERTIES = new ImmutableArray<XenonPropertyDescription>(

    new XenonPropertyDescriptionImplementation(PROPERTY_USER_KEY_FILE, Type.STRING, EnumSet.of(Component.CREDENTIALS), null,
            "Location of User Key file or 'userkey.pem' file. Default location is: ~/.globus/userkey.pem"),

    new XenonPropertyDescriptionImplementation(PROPERTY_USER_CERT_FILE, Type.STRING, EnumSet.of(Component.CREDENTIALS), null,
            "Location of user Certificate file or 'usercert.pem' file. Default location is: ~/.globus/usercert.pem"),

    new XenonPropertyDescriptionImplementation(PROPERTY_USER_X509_PROXY, Type.STRING, EnumSet.of(Component.CREDENTIALS), null,
            "Location of user X509 proxy file. Default location is: /tmp/x509_<userid>"),

    new XenonPropertyDescriptionImplementation(PROPERTY_USER_PROXY_LIFETIME_HOURS, Type.STRING,
            EnumSet.of(Component.CREDENTIALS), null, "Default Lifetime of create Grid Proxy in hours. "));

    public static XenonPropertyDescription[] getSupportedProperties() {
        return GLOBUS_CREDENTIAL_PROPERTIES.asArray();
    }

    // --- 
    // Instance
    // ---

    protected List<X509Certificate> certificates;
    protected String userKeyfile;
    protected String userCertfile;
    protected String userProxyfile;
    protected String userCertificatesDir;
    int proxyLifeTimeInHours = 24;

    public GlobusProxyCredentials(XenonProperties properties, GftpAdaptor gftpAdaptor) {

        // Optional properties, if none defined Globus defaults will be used. 
        updateDefaultProperties(properties);
    }

    protected void updateDefaultProperties(XenonProperties properties) {
        // Defaults: 
        userKeyfile = getPropertyOrDefault(properties, PROPERTY_USER_KEY_FILE, null);
        userCertfile = getPropertyOrDefault(properties, PROPERTY_USER_CERT_FILE, null);
        userProxyfile = getPropertyOrDefault(properties, PROPERTY_USER_X509_PROXY, null);
        proxyLifeTimeInHours = parseInt(getPropertyOrDefault(properties, PROPERTY_USER_PROXY_LIFETIME_HOURS, "24"), 24);
    }

    /**
     * Create Properties object containing current configured properties.
     * 
     * @return Properties Map of current credential Properties.
     */
    protected Map<String, String> createDefaultCredentialProperties() {

        return createCredentialProperties(userCertfile, userKeyfile, userProxyfile);
    }

    protected Map<String, String> createCredentialProperties(String certFile, String keyFile, String proxyFile) {

        Map<String, String> props = new Hashtable<String, String>();
        if (certFile != null) {
            props.put(PROPERTY_USER_KEY_FILE, certFile);
        }
        if (keyFile != null) {
            props.put(PROPERTY_USER_CERT_FILE, keyFile);
        }
        if (proxyFile != null) {
            props.put(PROPERTY_USER_X509_PROXY, proxyFile);
        }
        return props;
    }

    /**
     * Return property value or default value, if property is not defined.
     */
    protected String getPropertyOrDefault(XenonProperties properties, String propName, String defaultValue) {
        String val = null;

        try {
            val = properties.getStringProperty(propName);
        } catch (UnknownPropertyException | PropertyTypeException e) {
            logger.warn("Unknown property {}", propName);
        }

        if (val != null) {
            return val;
        }

        return defaultValue;
    }

    protected String getPropertyOrDefault(Map<String, String> properties, String propName, String defaultValue) {

        String val = properties.get(propName);

        if (val != null) {
            return val;
        } else {
            return defaultValue;
        }
    }

    protected int parseInt(String strVal, int defaultValue) {

        if (strVal == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(strVal);
        } catch (Exception e) {
            logger.warn("Couldn't parse integer value:'{}'", strVal);
        }

        return defaultValue;
    }

    @Override
    public GlobusProxyCredential newCertificateCredential(String scheme, String userCredentialsDir, String userinfo,
            char[] password, Map<String, String> properties) throws XenonException {

        String proxyFile;
        String keyfile;
        String certfile;

        // Use values from either given properties or factory defaults: 
        keyfile = getPropertyOrDefault(properties, GlobusProxyCredentials.PROPERTY_USER_KEY_FILE, userKeyfile);
        certfile = getPropertyOrDefault(properties, GlobusProxyCredentials.PROPERTY_USER_CERT_FILE, userCertfile);
        proxyFile = getPropertyOrDefault(properties, GlobusProxyCredentials.PROPERTY_USER_X509_PROXY, userProxyfile);
        int lifeTime = parseInt(
                getPropertyOrDefault(properties, GlobusProxyCredentials.PROPERTY_USER_PROXY_LIFETIME_HOURS, null),
                proxyLifeTimeInHours);

        // Parent '.globus' directory contains 'userkey.pem' and 'usercert.pem' directory: 
        if (userCredentialsDir != null) {
            // use 'userkey.pem' and 'usercert.pem'  from specified user credentials directory: 
            keyfile = userCredentialsDir + "/" + GlobusProxyCredentials.USERKEY_PEM;
            certfile = userCredentialsDir + "/" + GlobusProxyCredentials.USERCERT_PEM;
        }

        // Feature: load already created proxy: 
        if ((keyfile == null) && (certfile == null) && (proxyFile != null)) {
            return this.loadProxy(proxyFile);
        } else {
            return createProxy(certfile, keyfile, userinfo, password, proxyFile, lifeTime);
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

        // Delete proxy and make it invalid
        if (credential instanceof GlobusProxyCredential) {
            ((GlobusProxyCredential) credential).invalidate(true);
        } else {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "close():unrecognized credential:" + credential);
        }
    }

    @Override
    public boolean isOpen(Credential credential) throws XenonException {
        // Check wether proxy if valid 
        if (credential instanceof GlobusProxyCredential) {
            return ((GlobusProxyCredential) credential).isValid();
        } else {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, "isOpen():unrecognized credential:" + credential);
        }
    }

    public String getProxyFileLocation() {

        if (this.userProxyfile != null) {
            return userProxyfile;
        } else {
            // static CoG configuration. 
            return GlobusUtil.getStaticCoGProperties().getProxyFile();
        }
    }

    public GlobusProxyCredential getDefaultProxyCredential() throws XenonException {
        return loadProxy(getProxyFileLocation());
    }

    public GlobusProxyCredential loadProxy(String proxyFile) throws XenonException {
        // only proxy file is known as property here:
        Map<String, String> proxyProps = new Hashtable<String, String>();
        proxyProps.put(PROPERTY_USER_X509_PROXY, proxyFile); // 
        return new GlobusProxyCredential(this, proxyFile, proxyProps);
    }

    /**
     * Create new Globus (Grid) Proxy credential from user certificate credentials.
     * 
     * @param userCertFile
     *            - user Certificate file for example 'usercert.pem'
     * @param userKeyFile
     *            - user Key file for example 'userkey.pem'
     * @param userVOinfo
     *            - optional User and VO (+role) information, may be null. Only needed for VO enabled proxy credentials.
     * @param passphrase
     *            - User Grid Certificate Passphrase.
     * @param userProxyLocation
     *            - optional location of the proxy file, if null the default location will be used.
     * @return new created Grid Proxy Credential as GlobusProxyCredential.
     * @throws XenonException
     */
    public GlobusProxyCredential createProxy(String certFile, String keyFile, String userVOinfo, char[] passphrase,
            String proxyFile, int lifeTime) throws XenonException {

        if (keyFile == null) {
            throw new NullPointerException("Error: user keyFile==null. At least the userKeyFile must be given as argument. ");
        }

        logger.info("user certFile   = {}", certFile);
        logger.info("user keyFile    = {}", keyFile);
        logger.info("user proxy file = {}", proxyFile);
        logger.info("proxy lifetime  = {}H", lifeTime);

        try {
            // Must reload static configured certificates before calling globus !   
            reloadCACertificates();
            // Create Globus proxy certificate using the given user certificate, key and passphrase (if needed). Note that this 
            // method expects a proxy lifetime in seconds, not hours!
            GlobusCredential cred = GlobusUtil.createCredential(certFile, keyFile, passphrase, proxyFile, lifeTime * 3600, true);

            // Remember the properties used to create this credential.  
            Map<String, String> props = createCredentialProperties(certFile, keyFile, proxyFile);
            
            return new GlobusProxyCredential(this, cred, props);
        } catch (Exception e) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }

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

        try {
            
            // Update static loaded trusted certificates.
            reloadCACertificates();
            GlobusUtil.staticUpdateTrustedCertificates(certificates);
            return new GlobusCredential(proxyFilepath);

        } catch (GlobusCredentialException e) {
            throw new XenonException(GftpAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }
    }

    /**
     * Reload Trusted (CA) certificates from default locations.
     */
    protected void reloadCACertificates() {

        this.certificates = GlobusUtil.loadX509Certificates(new String[] { userCertificatesDir });
    }

    /**
     * Remove proxy file.
     * 
     * @param globusProxyCredential
     *            proxy credential to remove.
     */
    public boolean delete(GlobusProxyCredential globusProxyCredential) {

        String proxyPath = globusProxyCredential.getProxyFilePath();
        if (proxyPath != null) {

            return Util.destroy(proxyPath);
        }
        else
        {
            logger.warn("No proxy file to delete.");
        }
        return false; 
    }

}
