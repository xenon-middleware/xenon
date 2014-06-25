package nl.esciencecenter.xenon.adaptors.gftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.globus.gsi.CertUtil;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.X509ExtensionSet;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.proxy.ext.ProxyCertInfo;
import org.globus.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JGlobus based Init Proxy Class. <br>
 * Use <code>GSIConstants.GSI_2_PROXY</code> as ProxyType to create legacy Globus Proxies. This class doesn't need static Globus
 * properties to be defined.
 * 
 * @author Piter T. de Boer
 */
public class ProxyInit {

    private static final Logger logger = LoggerFactory.getLogger(ProxyInit.class);

    // ================
    // instance fields 
    // ================

    private PrivateKey userKey = null;

    protected X509Certificate[] userCertificates;

    protected int bits = 512;

    protected int lifetime = 3600 * 12;

    protected ProxyCertInfo proxyCertInfo = null;

    /**
     * Default type is "old" Globus Legacy proxy.
     */
    protected int proxyType = GSIConstants.GSI_2_PROXY;

    protected GlobusCredential proxy = null;

    /**
     * Non static Globus certificate utility class.
     */
    protected CertUtil certUtil;

    public ProxyInit() {
        // performs static initialization.
        certUtil = new CertUtil();
    }

    public X509Certificate getCertificate() {
        return this.userCertificates[0];
    }

    /**
     * @param bits
     *            - Size of proxy certificate in bits.
     */
    public void setBits(int bits) {
        this.bits = bits;
    }

    /**
     * @return size of proxy certificate in bits.
     */
    public int getBits() {
        return bits;
    }

    /**
     * @param lifetime
     *            - lifetime of the to be created proxy in seconds.
     */
    public void setLifetime(int lifetimeInSeconds) {
        this.lifetime = lifetimeInSeconds;
    }

    /**
     * @return lifetime of the proxy to be created in seconds.
     */
    public int getLifetime() {
        return lifetime;
    }

    /**
     * Set proxy type to GSI 2 "legacy" proxies. Needed for old legacy Grid proxies.
     */
    public void setProxyTypeToGSI2Legacy() {
        setProxyType(GSIConstants.GSI_2_PROXY);
    }

    /**
     * Specify Globus Proxy type, for example <code>GSIConstants.GSI_2_PROXY</code> for 'legacy' globus proxies.
     * 
     * @see org.globus.gsi.GSIConstants
     * @param proxyType
     *            the Globus Proxy Type to create.
     */
    public void setProxyType(int proxyType) {
        this.proxyType = proxyType;
    }

    /**
     * @return Globus proxy type, for example <code>GSIConstants.GSI_2_PROXY</code> for legacy globus proxies.
     * @see org.globus.gsi.GSIConstants
     */
    public int getProxyType() {
        return proxyType;
    }

    /**
     * Specify extra certificate extensions, for example VO attributes to be added after a valid proxy has been created. For
     * example to create a VOMS enabled Proxy.
     */
    public void setProxyCertInfo(ProxyCertInfo proxyCertInfo) {
        this.proxyCertInfo = proxyCertInfo;
    }

    /**
     * Factory method to create globus proxy credentials.
     * 
     * @param certFile
     *            - user public certificate file, for example "~/.globus/usercert.pem"
     * @param keyFile
     *            - user private key file, for example "~/.globus/userkey.pem"
     * @param passphrase
     *            - passphrase to decode private key file
     * @param verify
     *            verify user key and certificate file.
     * @param proxyFile
     *            - optional path of file to save new created proxy to.
     * @return Created GlobusCredential object.
     * @throws Exception
     */
    public GlobusCredential createProxy(String certFile, String keyFile, char passphrase[], boolean verify, String proxyFile)
            throws Exception {

        logger.debug("createProxy(): Using cert file : {}", certFile);
        logger.debug("createProxy(): Using key file  : {}", keyFile);
        logger.debug("createProxy(): Number of bits  : {}", bits);
        logger.debug("createProxy(): Saving proxy to : {}", proxyFile);
        logger.debug("createProxy(): Option verify   : {}", verify);

        loadUserCertificates(certFile);
        loadUserKey(keyFile, passphrase);

        if (verify) {
            verify();
        }

        logger.info("Creating proxy, please wait...");

        create();

        logger.info("Your proxy is valid until: {}.", proxy.getCertificateChain()[0].getNotAfter());

        if (proxyFile == null) {
            logger.info("No proxy file specified. Not saving proxy file.\n");
        } else {
            saveTo(proxyFile);
        }

        return proxy;
    }

    private void saveTo(String proxyFile) throws IOException {

        logger.debug("Saving proxy to: {}\n", proxyFile);

        OutputStream out = null;

        try {
            File file = Util.createFile(proxyFile);
            // set read only permissions
            if (!Util.setOwnerAccessOnly(proxyFile)) {
                logger.warn("Warning: Please check file permissions for your proxy file:{}!", proxyFile);
            }
            out = new FileOutputStream(file);
            // write the contents
            proxy.save(out);
        } catch (IOException e) {
            logger.error("Failed to save proxy to file: {}!", proxyFile);
            throw e;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    ; // ignore
                }
            }
        }
    }

    /**
     * Verify public and private key pair whether they match.
     * 
     * @throws Exception
     *             if public key and private key do no match.Can be user as assertion.
     */
    public void verify() throws Exception {
        RSAPublicKey pkey = (RSAPublicKey) getCertificate().getPublicKey();
        RSAPrivateKey prkey = (RSAPrivateKey) userKey;

        if (!pkey.getModulus().equals(prkey.getModulus())) {
            throw new Exception("Certificate and private key specified do not match!");
        }
    }

    /**
     * Load user certificate file, for example "~/.globus/usercert.pem".
     * 
     * @param arg
     *            - path to user certificate file.
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public void loadUserCertificates(String arg) throws IOException, GeneralSecurityException {
        userCertificates = CertUtil.loadCertificates(arg);
    }

    /**
     * Load user key file and decode it using the passphrase.
     * 
     * @param file
     *            - path to private user key file, for example "~/.globus/userkey.pem".
     * @param passphrase
     *            - passphrase needed to decode the private key.
     * @throws Exception
     */
    public void loadUserKey(String file, char passphrase[]) throws Exception {
        userKey = GlobusUtil.getPrivateKey(file, passphrase);
    }

    /** 
     * Create the proxy. 
     * 
     * @throws GeneralSecurityException
     */
    public void create() throws GeneralSecurityException {
        BouncyCastleCertProcessingFactory factory = BouncyCastleCertProcessingFactory.getDefault();

        // No Extensions for legacy proxies:
        // Extensions can be used for example to create VOMS enabled proxies.
        X509ExtensionSet extSet = null;

        proxy = factory.createCredential(userCertificates, userKey, bits, lifetime, proxyType, extSet);
    }

}
