package nl.esciencecenter.xenon.adaptors.gftp;


import org.globus.common.CoGProperties;
import org.globus.gsi.GlobusCredential;
import org.globus.tools.proxy.DefaultGridProxyModel;
import org.globus.tools.proxy.GridProxyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Static Globus Configuration. 
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
    
    public static CoGProperties getStaticCoGProperties()
    {
        // initialize defaults from Globus Proxy Model 
        GridProxyModel staticModel = staticGetModel(false);
        CoGProperties props = staticModel.getProperties(); 
        
        String defaultUserCertFile = props.getUserCertFile(); 
        String defaultUserKeyFile = props.getUserKeyFile();
        String defaultProxyFilename = props.getProxyFile();
        int defaultLifetime = props.getProxyLifeTime(); 
     
        logger.info("default user certFile = {}",defaultUserCertFile); 
        logger.info("default user keyFile  = {}",defaultUserKeyFile); 
        logger.info("default user proxy file   = {}",defaultProxyFilename); 
        logger.info("default proxy lifetime     = {}",defaultLifetime); 
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
    
    protected static GridProxyModel staticGetModel()
    {
        return staticGetModel(false);
    }

    protected static GridProxyModel staticGetModel(boolean usePKCS11Device)
    {
        GridProxyModel staticModel;
        
        if (usePKCS11Device)
        {
            try
            {
                // Do We Need: PKCS11 ???
                Class<?> iClass = Class.forName(PKCS11_MODEL);
                staticModel = (GridProxyModel) iClass.newInstance();
            }
            catch (Exception e)
            {
                staticModel = new DefaultGridProxyModel();
            }
        }
        else
        {
            staticModel = new DefaultGridProxyModel();
        }
        return staticModel;
    }
    
    
    public static GlobusCredential createCredential(String userCert,String userKey,char passphrase[]) throws Exception
    {
        GridProxyModel staticModel = staticGetModel();
        GlobusCredential credential;
        
        CoGProperties props = getStaticCoGProperties();  
        
        int defaultLifetime = props.getProxyLifeTime(); 
        
        // --- 
        // Must update static properties *before* using static create proxy methods from Globus! 
        // ---
        props.setUserCertFile(userCert);
        props.setUserKeyFile(userKey);
        props.setProxyLifeTime(defaultLifetime);

        credential = staticModel.createProxy(new String(passphrase)); 
        
        return credential; 
    }
}
