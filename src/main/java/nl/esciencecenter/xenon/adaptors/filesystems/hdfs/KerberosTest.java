package nl.esciencecenter.xenon.adaptors.filesystems.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;

public class KerberosTest {
    private static String username = "hdfs-user";
    private static char[] password = "hadoop".toCharArray();
    public static LoginContext kinit() throws LoginException {
        LoginContext lc = new LoginContext(KerberosTest.class.getSimpleName(), new CallbackHandler() {
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for(Callback c : callbacks){
                    if(c instanceof NameCallback)
                        ((NameCallback) c).setName(username);
                    if(c instanceof PasswordCallback)
                        ((PasswordCallback) c).setPassword(password);
                }
            }});
        lc.login();
        return lc;
    }

    public static void main(String[] argv){
        System.setProperty("java.security.krb5.conf", "krb.conf");
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://one.hdp:8020");
        conf.set("hadoop.security.authentication", "kerberos");
        UserGroupInformation.setConfiguration(conf);
        try {
            LoginContext lc = kinit();
            UserGroupInformation.loginUserFromSubject(lc.getSubject());
        } catch (Exception e){

        }


    }

}
