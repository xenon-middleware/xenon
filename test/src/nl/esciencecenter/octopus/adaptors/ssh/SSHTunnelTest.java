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

package nl.esciencecenter.octopus.adaptors.ssh;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Properties;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;

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

        String configfile = System.getProperty("test.config");
        
        if (configfile == null) {
            configfile = System.getProperty("user.home") + File.separator + "octopus.test.properties";
        }
        
        Properties p = new Properties();
        p.load(new FileInputStream(configfile));

        String gateway = getPropertyOrFail(p, "test.ssh.gateway");
        String location = getPropertyOrFail(p, "test.ssh.location");
        
        Octopus octopus = OctopusFactory.newOctopus(null);
        Files files = octopus.files();
        Credentials credentials = octopus.credentials();

        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("octopus.adaptors.ssh.gateway", gateway);

        // Will thrown an exception if the tunnel fails ?
        FileSystem filesystem = files.newFileSystem("ssh", location, credentials.getDefaultCredential("sftp"), properties);

        files.close(filesystem);
        OctopusFactory.endOctopus(octopus);
    }

}
