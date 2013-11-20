package nl.esciencecenter.xenon.adaptors.gftp;

import java.io.File;
import java.lang.reflect.Field;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import nl.esciencecenter.xenon.adaptors.ssh.SshUtil;
import nl.esciencecenter.xenon.files.PosixFilePermission;
import nl.esciencecenter.xenon.files.RelativePath;

import org.globus.common.CoGProperties;
import org.globus.ftp.FeatureList;
import org.globus.gsi.TrustedCertificates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various Globus and Other GridFTP util methods.
 * 
 * @author Piter T. de Boer
 */
public class GftpUtil {

    public static final Logger logger = LoggerFactory.getLogger(GftpUtil.class);

    public static final String GSIFTP_SCHEME = "gsiftp"; 

    public static final String GFTP_SCHEME = "gftp"; 
    
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
     * Default system wide grid certificates directory.
     */
    public static final String DEFAULT_SYSTEM_CERTIFICATES_DIR = "/etc/grid-security/certificates";

    /**
     * User subdirectory '~/.globus' for globus configurations.
     */
    public static final String GLOBUSRC = ".globus";

    public static void staticInit() {
        initCogProperties();
    }

    public static void initCogProperties() {
        CoGProperties props = CoGProperties.getDefault();

        String val = props.getProperty(COG_ENFORCE_SIGNING_POLICY);

        if ((val == null) || (val.equals(""))) {
            props.setProperty(COG_ENFORCE_SIGNING_POLICY, "false");
        }
    }

    public synchronized static List<X509Certificate> loadX509Certificates() {
        String dirs[] = new String[] { "/etc/grid-security/certificates" };
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
            defXCerts = defCerts.getCertificates();

            if (defXCerts != null) {
                for (X509Certificate cert : defXCerts) {
                    logger.debug(" + loaded default grid certificate: {}", cert.getSubjectDN());
                    allCerts.add(cert);
                }
            }
        } catch (NullPointerException e) {
            // Bug in Globus!
            logger.warn("Globus NullPointer bug: TrustedCertificates.getDefault(): NullPointerException:");
        }

        logger.info(" + Got {} default certificates", allCerts.size());

        for (String certPath : caCertificateDirs) {
            //Global.infoPrintf(this," + Checking extra certificates from:%s\n",certPath);

            // check path: avoid errors in eclipse: 
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
                logger.warn("***Warning: skipping non-existing certificate directory:{}", certPath);
            }
        }

        return allCerts;
    }

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
                logger.info(" > updating trusted certificate: {}", xcert.getSubjectX500Principal());
            }
        }

    }

    public static boolean isXDir(String dirName) {
        
        if (dirName.compareTo(".") == 0) // Current Dir
        {
            return true;
        }

        if (dirName.compareTo("..") == 0) // Parent Dir
        {
            return true;
        }

        return false;
    }

    /**
     * Convert GridFTP Time String "YYYYMMDDhhmmss" to millis since Epoch.
     */
    public static long timeStringToMillis(String val) {
        // FTP date value is in YYYYMMDDhhmmss
        int YYYY = Integer.valueOf(val.substring(0, 4));
        int MM = Integer.valueOf(val.substring(4, 6));
        int DD = Integer.valueOf(val.substring(6, 8));
        int hh = Integer.valueOf(val.substring(8, 10));
        int mm = Integer.valueOf(val.substring(10, 12));
        int ss = Integer.valueOf(val.substring(12, 14));

        // GMT TIMEZONE:
        TimeZone gmtTZ = TimeZone.getTimeZone("GMT-0:00");
        Calendar cal = new GregorianCalendar(gmtTZ);

        // O-be-1-kenobi: month nr in GregorianCalendar is zero-based
        cal.set(YYYY, MM - 1, DD, hh, mm, ss);

        // TimeZone localTZ=Calendar.getInstance().getTimeZone();
        // cal.setTimeZone(localTZ);

        return cal.getTimeInMillis();
    }

    public static String basename(String filepath) {
        return new RelativePath(filepath).getFileNameAsString();
    }

    public static String dirname(String filepath) {
        return new RelativePath(filepath).getParent().getAbsolutePath();
    }

    public static Set<PosixFilePermission> unixModeToPosixFilePermissions(int mode) {

        // use SshUtil. 
        return SshUtil.bitsToPermissions(mode);
    }

    public static String toString(FeatureList features) {
        
        String str = "FeatureList:[";
        boolean first = true;

        // Use reflection to check String Constants as FeatureList doesn't support them:
        Field[] fields = FeatureList.class.getFields();
        for (Field field : fields) {
            // skip private parts 
            if (field.isAccessible() == false) {
                //continue; 
            }

            String name = field.getName();
            if (features.contains(name)) {
                if (first) {
                    str += name;
                    first = false;
                } else {
                    str += "," + name;
                }
            }
        }

        return str + "]";
    }
}
