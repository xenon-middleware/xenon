package nl.esciencecenter.xenon.adaptors.gftp;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.globus.common.CoGProperties;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.TrustedCertificates;
import org.globus.tools.proxy.DefaultGridProxyModel;
import org.globus.tools.proxy.GridProxyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static Globus Configuration.<br>
 * GftpAdaptor uses Globus (CoG) defaults from this class if not specified by Adaptor Properties.
 * 
 * @author Piter T. de Boer
 */
public class GlobusUtil {

    public static final Logger logger = LoggerFactory.getLogger(GlobusUtil.class);

    /**
     * In cog-jglobus 1.4 this string isn't defined. Define it here to stay 1.4 compatible. This property is defined in CoG
     * jGlobus 1.7 and higher.
     */
    public static final String COG_ENFORCE_SIGNING_POLICY = "java.security.gsi.signing.policy";

    /**
     * PKCS11 Model property (not used).
     */
    public static final String PKCS11_MODEL = "org.globus.tools.proxy.PKCS11GridProxyModel";

    /**
     * User sub-directory '~/.globus' for globus configurations.
     */
    public static final String GLOBUSRC = ".globus";

    /**
     * Default user (grid) public certificate file for example in the "~/.globus" directory.
     */
    public static final String USERCERTPEM = "usercert.pem";

    /**
     * Default user (grid) private certificate key file, for example in the "~/.globus/" directory.
     */
    public static final String USERKEYPEM = "userkey.pem";

    public static void staticInit() {
        // Some Globus properties must be defined Globally 
        initCogProperties();
    }

    public static CoGProperties getStaticCoGProperties() {
        // initialize defaults from Globus Proxy Model 
        GridProxyModel staticModel = staticGetModel(false);
        CoGProperties props = staticModel.getProperties();

        String defaultUserCertFile = props.getUserCertFile();
        String defaultUserKeyFile = props.getUserKeyFile();
        String defaultProxyFilename = props.getProxyFile();
        int defaultLifetimeInHours = props.getProxyLifeTime();

        logger.info("default user certFile   = {}", defaultUserCertFile);
        logger.info("default user keyFile    = {}", defaultUserKeyFile);
        logger.info("default user proxy file = {}", defaultProxyFilename);
        logger.info("default proxy lifetime  = {}H", defaultLifetimeInHours);
        // Update (Global) Properties ?
        return props;
    }

    public static void initCogProperties() {
        // update static properties: 
        CoGProperties props = CoGProperties.getDefault();

        String val = props.getProperty(COG_ENFORCE_SIGNING_POLICY);

        if ((val == null) || (val.equals(""))) {
            props.setProperty(COG_ENFORCE_SIGNING_POLICY, "false");
        }
    }

    protected static GridProxyModel staticGetModel() {
        return staticGetModel(false);
    }

    /**
     * Todo: support for external PKCS11 device.
     */
    protected static GridProxyModel staticGetModel(boolean usePKCS11Device) {
        GridProxyModel staticModel;

        if (usePKCS11Device) {
            try {
                // Do We Need: PKCS11 ???
                Class<?> iClass = Class.forName(PKCS11_MODEL);
                staticModel = (GridProxyModel) iClass.newInstance();
            } catch (Exception e) {
                staticModel = new DefaultGridProxyModel();
            }
        } else {
            staticModel = new DefaultGridProxyModel();
        }
        return staticModel;
    }

    public static GlobusCredential createCredential(String userCert, String userKey, char passphrase[]) throws Exception {
        return createCredential(userCert, userKey, passphrase, null, -1);
    }

    /**
     * Static method to create a Globus Proxy Credential.
     * 
     * @param userCert
     *            - location of usercert.pem file, default is 'usercert.pem' from ~/.globus
     * @param userKey
     *            - location of userkey.pem file, default is 'userkey.pem' from ~/.globus
     * @param passphrase
     *            - actual passphrase
     * @param userProxyLocation
     *            - optional location to save proxy file to
     * @return actual globus proxy credential if proxy creation is successful.
     * @throws Exception
     */
    public static GlobusCredential createCredential(String userCert, String userKey, char passphrase[], String userProxyLocation,
            int lifeTime) throws Exception {
        GridProxyModel staticModel = staticGetModel();
        GlobusCredential credential;

        CoGProperties props = getStaticCoGProperties();

        if (passphrase == null) {
            throw new NullPointerException("Can't create proxy without passphrase. passphrase==null!");
        }

        // --- 
        // Must update static properties *before* using static create proxy methods from Globus! 
        // ---

        if (userCert != null) {
            props.setUserCertFile(userCert);
        } // else use default 'usercert.pem' location 

        if (userKey != null) {
            props.setUserKeyFile(userKey);
        } // else use default 'userkey.pem' location. 

        if (userProxyLocation != null) {
            props.setProxyFile(userProxyLocation);
        } //else use /tmp/x509_<userid>  

        if (lifeTime > 0) {
            props.setProxyLifeTime(lifeTime);
        } // default is 24 hours. 

        credential = staticModel.createProxy(new String(passphrase));

        return credential;
    }

    /**
     * Default system wide grid certificates directory.
     */
    public static final String DEFAULT_SYSTEM_CERTIFICATES_DIR = "/etc/grid-security/certificates";

    /** 
     * Load certificates from list of locations. If directory doesn't exist, the location will be skipped. 
     * @param customDirs - List of directories to look for X509 Certificates.   
     * @return loaded X509 Certificates from specified directories. 
     */
    public static List<X509Certificate> loadX509Certificates(String[] customDirs) {

        int n = 0;
        if (customDirs != null) {
            n = customDirs.length;
        }

        // default dir: 
        String dirs[] = new String[1 + n];
        // add custom dirs (if specified):
        dirs[0] = DEFAULT_SYSTEM_CERTIFICATES_DIR;
        int index = 1;
        for (int i = 0; i < n; i++) {
            if (customDirs[i] != null) {
                dirs[index++] = customDirs[i]; // filter existing here ? 
            }
        }
        return loadCertificates(dirs);
    }

    public static List<X509Certificate> loadCertificates(String caCertificateDirs[]) {
        if (caCertificateDirs == null) {
            return null; // null in null out
        }

        List<X509Certificate> allCerts = new ArrayList<X509Certificate>();

        // Default globus certificates. 
        try {
            TrustedCertificates defCerts = null;
            X509Certificate[] defXCerts = null;
            defCerts = TrustedCertificates.getDefault();
            if (defCerts != null) {
                defXCerts = defCerts.getCertificates();
            }

            if (defXCerts != null) {
                for (X509Certificate cert : defXCerts) {
                    logger.debug(" + loaded default grid certificate: {}", cert.getSubjectDN());
                    allCerts.add(cert);
                }
            }
        } catch (NullPointerException e) {
            // (old) Bug in Globus!
            logger.warn("Globus NullPointer bug: TrustedCertificates.getDefault(): NullPointerException:");
        }

        logger.info(" + Got {} default certificates", allCerts.size());

        for (String certPath : caCertificateDirs) {

            if ((certPath == null) || certPath == "") {
                continue;
            }

            File file = new File(certPath);
            if (file.exists()) {
                logger.debug(" +++ Loading Extra Certificates from: {} +++", certPath);

                TrustedCertificates extraCerts = TrustedCertificates.load(certPath);
                X509Certificate extraXCertsArr[] = extraCerts.getCertificates();

                if ((extraXCertsArr == null) || (extraXCertsArr.length <= 0)) {
                    logger.debug(" - No certificates found in: {}", certPath);
                }

                for (X509Certificate cert : extraXCertsArr) {
                    logger.debug(" + loaded extra certificate: {}", cert.getSubjectDN());
                    allCerts.add(cert);
                }
            } else {
                logger.debug("skipping non-existing certificate directory:{}", certPath);
            }
        }

        return allCerts;
    }

    /**
     * Update static loaded Trusted Certificates used by Globus.
     * 
     * @param certs
     *            - Trusted certificates needed by Globus.
     */
    public static void staticUpdateTrustedCertificates(List<X509Certificate> certs) {
        
        X509Certificate[] newXCerts = new X509Certificate[certs.size()];
        newXCerts = certs.toArray(newXCerts);
        TrustedCertificates trustedCertificates = new TrustedCertificates(newXCerts);
        TrustedCertificates.setDefaultTrustedCertificates(trustedCertificates);

        // Debug: print out actual certificates. 
        TrustedCertificates tcerts = TrustedCertificates.getDefault();
        if (certs != null) {
            X509Certificate[] xcerts = tcerts.getCertificates();

            for (X509Certificate xcert : xcerts) {
                logger.info(" > updating Trusted Certificate: {}", xcert.getSubjectX500Principal());
            }
        }

    }
}
