package nl.esciencecenter.xenon.adaptors.filesystems.hdfs;

import nl.esciencecenter.xenon.InvalidPropertyException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.FileAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.credentials.KeytabCredential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
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

    /** The name of this adaptor */
    public static final String ADAPTOR_NAME = "hdfs";

    /** The default SSH port */
    protected static final int DEFAULT_PORT = 21;

    /** A description of this adaptor */
    private static final String ADAPTOR_DESCRIPTION = "Adaptor for the Apache Hadoop file system";

    /** The locations supported by this adaptor */
    private static final String [] ADAPTOR_LOCATIONS = new String [] { "hdfs://host[:port]" };

    /** All our own properties start with this prefix. */
    public static final String PREFIX = FileAdaptor.ADAPTORS_PREFIX + "hdfs.";

    public static final String HADOOP_SETTINGS_FILE = PREFIX + "hadoopSettingsFile";
    /** Hadoop property: dfs.client.block.write.replace-datanode-on-failure.policy  */
//    public static final String REPLACE_ON_FAILURE = PREFIX + "replaceOnFailure";
//
//    public static final String AUTHENTICATION = PREFIX + "authentication";
//
//    public static final String DFS_NAMENODE_KERBEROS_PRINCIPAL = PREFIX + "dfs.namenode.kerberos.principal";
//
//    public static final String BLOCK_ACCESS_TOKEN = PREFIX + "dfs.block.access.token.enable";
//
//    public static final String TRANSFER_PROTECTION = PREFIX + "dfs.data.transfer.protection";

    /** The buffer size to use when copying data. */
    public static final String BUFFER_SIZE = PREFIX + "bufferSize";



    protected static final XenonPropertyDescription[] VALID_PROPERTIES = new XenonPropertyDescription[] {
            new XenonPropertyDescription(BUFFER_SIZE, XenonPropertyDescription.Type.SIZE, "64K", "The buffer size to use when copying files (in bytes)."),
            new XenonPropertyDescription(HADOOP_SETTINGS_FILE, XenonPropertyDescription.Type.STRING, "", "The path to the file with the hadoop settings, i.e. \"/home/xenon/core-site.xml\".")
    };

    public HDFSFileAdaptor() {
        super("hdfs", ADAPTOR_DESCRIPTION, ADAPTOR_LOCATIONS, VALID_PROPERTIES);
    }


    @Override
    public FileSystem createFileSystem(String location, Credential credential, Map<String, String> properties) throws XenonException {
        Configuration conf = new Configuration(true);


        conf.set("fs.defaultFS", location);
        properties = properties == null ? new HashMap<>() : properties;
        XenonProperties prop = new XenonProperties(VALID_PROPERTIES,properties);
        String file = prop.getStringProperty(HADOOP_SETTINGS_FILE);
        try {
            org.apache.hadoop.fs.Path p = new org.apache.hadoop.fs.Path(file);
            conf.addResource(p);
        } catch (IllegalArgumentException e){

        }

        String authent = conf.get("hadoop.security.authentication");

        try {
            UserGroupInformation.reset();
            if (authent.equals("kerberos")) {
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
                    PasswordCredential pw = (PasswordCredential) credential;
                    LoginContext lc = new LoginContext("hdfs-kerb", new CallbackHandler() {
                        @Override
                        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                            for (Callback c : callbacks) {
                                if (c instanceof NameCallback)
                                    ((NameCallback) c).setName(pw.getUsername());
                                if (c instanceof PasswordCallback)
                                    ((PasswordCallback) c).setPassword(pw.getPassword());
                            }

                        }});
                    lc.login();
                    UserGroupInformation.loginUserFromSubject(lc.getSubject());
                }
            }
        }catch (IOException | LoginException e) {
                throw new XenonException("hdfs", "Error when logging in to hdfs", e);
        }

        XenonProperties xp = new XenonProperties(VALID_PROPERTIES, properties);
        int bufferSize = (int) xp.getSizeProperty(BUFFER_SIZE);

        if (bufferSize <= 0 || bufferSize >= Integer.MAX_VALUE) {
            throw new InvalidPropertyException(ADAPTOR_NAME,
                    "Invalid value for " + BUFFER_SIZE + ": " + bufferSize + " (must be between 1 and " + Integer.MAX_VALUE + ")");
        }
        conf.set("dfs.client.block.write.replace-datanode-on-failure.policy", "NEVER");
        try {
            org.apache.hadoop.fs.FileSystem fs = org.apache.hadoop.fs.FileSystem.get(conf);
            return new HDFSFileSystem(getNewUniqueID(),location, fs,bufferSize, xp);
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