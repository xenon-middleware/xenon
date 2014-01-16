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

package nl.esciencecenter.xenon.adaptors.ssh;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Properties;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class SSHTunnelTest {

    private String getPropertyOrFail(Properties p, String property) throws Exception {

        String tmp = p.getProperty(property);

        if (tmp == null) {
            throw new Exception("Failed to retireve property " + property);
        }

        return tmp;
    }
    
    @org.junit.Test
    public void test_sshViaTunnel() throws Exception {

        Properties p = System.getProperties();
        
        String configfile = System.getProperty("test.config");
        
        if (configfile == null) {
            configfile = System.getProperty("user.home") + File.separator + "xenon.test.properties";
        }
        
        if (new File(configfile).exists()) {
            p = new Properties();
            p.load(new FileInputStream(configfile));    
        }
        
        String gateway = getPropertyOrFail(p, "test.ssh.gateway");
        String location = getPropertyOrFail(p, "test.ssh.location");
        
        Xenon xenon = XenonFactory.newXenon(null);
        Files files = xenon.files();
        Credentials credentials = xenon.credentials();

        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("xenon.adaptors.ssh.gateway", gateway);
        properties.put("xenon.adaptors.ssh.strictHostKeyChecking", "false");
        
//        System.out.println("gateway = " + gateway);
//        System.out.println("location = " + location);
//        System.out.println("credential = " + credentials.getDefaultCredential("sftp"));
//        
        // Will thrown an exception if the tunnel fails ?
        FileSystem filesystem = files.newFileSystem("ssh", location, credentials.getDefaultCredential("sftp"), properties);

        files.close(filesystem);
        XenonFactory.endXenon(xenon);
    }

    public static void main(String [] args) throws Exception { 
        new SSHTunnelTest().test_sshViaTunnel();
    }
    
    
}
