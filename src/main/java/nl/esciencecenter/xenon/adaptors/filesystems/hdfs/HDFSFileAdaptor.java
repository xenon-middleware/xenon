package nl.esciencecenter.xenon.adaptors.filesystems.hdfs;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.FileAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.credentials.KeytabCredential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.log4j.spi.LoggerFactory;
import org.slf4j.Logger;


import javax.security.auth.callback.*;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by atze on 3-7-17.
 */
public class HDFSFileAdaptor extends FileAdaptor{

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(HDFSFileAdaptor.class);

    /** The default SSH port */
    protected static final int DEFAULT_PORT = 21;

    /** A description of this adaptor */
    private static final String ADAPTOR_DESCRIPTION = "Adaptor for the Apache Hadoop file system";

    /** The locations supported by this adaptor */
    private static final String [] ADAPTOR_LOCATIONS = new String [] { "hdfs://host[:port]" };

    /** All our own properties start with this prefix. */
    public static final String PREFIX = FileAdaptor.ADAPTORS_PREFIX + "hdfs.";

    /** Hadoop property: dfs.client.block.write.replace-datanode-on-failure.policy  */
    public static final String REPLACE_ON_FAILURE = PREFIX + "replaceOnFailure";

    public static final String AUTHENTICATION = PREFIX + "authentication";

    protected static final XenonPropertyDescription[] VALID_PROPERTIES = new XenonPropertyDescription[] {
            new XenonPropertyDescription(REPLACE_ON_FAILURE, XenonPropertyDescription.Type.STRING, "DEFAULT", "Corresponds to Hadoop property: dfs.client.block.write.replace-datanode-on-failure.policy "),
            new XenonPropertyDescription(AUTHENTICATION, XenonPropertyDescription.Type.STRING, "simple", "Corresponds to Hadoop property hadoop.security.authentication, possible values: simple and kerberos(default)")
    };

    public HDFSFileAdaptor() {
        super("hdfs", ADAPTOR_DESCRIPTION, ADAPTOR_LOCATIONS, VALID_PROPERTIES);
    }


    @Override
    public FileSystem createFileSystem(String location, Credential credential, Map<String, String> properties) throws XenonException {
        Configuration conf = new Configuration(false);
        conf.set("fs.defaultFS", location);
        properties = properties == null ? new HashMap<>() : properties;
        if(properties.containsKey(REPLACE_ON_FAILURE)){
            conf.set("dfs.client.block.write.replace-datanode-on-failure.policy",properties.get(REPLACE_ON_FAILURE));
        }
        if(properties.containsKey(AUTHENTICATION)){
            conf.set("hadoop.security.authentication", properties.get(AUTHENTICATION));
        }
        try {
            if (properties.get(AUTHENTICATION).equals("kerberos")) {
                UserGroupInformation.setConfiguration(conf);
                if (credential instanceof DefaultCredential) {


                    UserGroupInformation.loginUserFromSubject(null);

                }
                if (credential instanceof KeytabCredential) {
                    KeytabCredential kt = (KeytabCredential) credential;
                    UserGroupInformation.loginUserFromKeytab(kt.getUsername(),
                            kt.getKeytabFile());
                }
                if(credential instanceof PasswordCredential){
                    // set JAAS to request password
                    javax.security.auth.login.Configuration.setConfiguration(new javax.security.auth.login.Configuration() {
                        @Override
                        public AppConfigurationEntry[] getAppConfigurationEntry(String s) {

                            if(s.equals("hdfs-kerb")){
                                HashMap map = new HashMap<String,String>();
                                map.put("client", "TRUE");
                                return new AppConfigurationEntry[] {
                                        new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule", AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, map)
                                };
                            }
                            return null;
                        }
                    });

                    LoginContext lc = new LoginContext("JaasSample", new CallbackHandler() {
                        @Override
                        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                            for (Callback c : callbacks) {
                                if (c instanceof NameCallback)
                                    ((NameCallback) c).setName("admin/admin@esciencecenter.nl");
                                if (c instanceof PasswordCallback)
                                    ((PasswordCallback) c).setPassword("javagat".toCharArray());
                            }

                        }});
                    UserGroupInformation.loginUserFromSubject(lc.getSubject());
                }
            }
        }catch (IOException | LoginException e) {
                throw new XenonException("hdfs", "Error when logging in to hdfs", e);
        }

        XenonProperties xp = new XenonProperties(VALID_PROPERTIES, properties);
        try {
            org.apache.hadoop.fs.FileSystem fs = org.apache.hadoop.fs.FileSystem.get(conf);
            return new HDFSFileSystem(getNewUniqueID(),location, fs,xp);
        } catch(IOException e){
            throw new XenonException("hdfs", "Failed to create HDFS connection: " + e.getMessage());
        }

    }


    @Override
    public boolean canReadSymboliclinks() {
        return false;
    }

    @Override
    public boolean canCreateSymboliclinks() {
        return false;
    }
}