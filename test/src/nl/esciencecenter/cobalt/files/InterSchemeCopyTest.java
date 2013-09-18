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

package nl.esciencecenter.cobalt.files;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import nl.esciencecenter.cobalt.Cobalt;
import nl.esciencecenter.cobalt.CobaltFactory;
import nl.esciencecenter.cobalt.engine.CobaltEngine;
import nl.esciencecenter.cobalt.files.CopyOption;
import nl.esciencecenter.cobalt.files.Files;
import nl.esciencecenter.cobalt.files.Path;
import nl.esciencecenter.cobalt.util.Utils;

import org.junit.Test;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class InterSchemeCopyTest {

    private String getPropertyOrFail(Properties p, String property) throws Exception {

        String tmp = p.getProperty(property);

        if (tmp == null) {
            throw new Exception("Failed to retireve property " + property);
        }

        return tmp;
    }
    
    @Test
    public void test_copy_local_ssh() throws Exception {

        Cobalt octopus = CobaltEngine.newCobalt(null);

        String configfile = System.getProperty("test.config");
        
        if (configfile == null) {
            configfile = System.getProperty("user.home") + File.separator + "octopus.test.properties";
        }
        
        Properties p = new Properties();
        p.load(new FileInputStream(configfile));

        String user = getPropertyOrFail(p, "test.ssh.user");
        String location = getPropertyOrFail(p, "test.ssh.location");
        
        Files files = octopus.files();

        Path localCWD = Utils.getLocalCWD(files);
        Path sshCWD = files.newFileSystem("ssh", user + "@" + location, null, null).getEntryPath();

        String dirname = "octopus_test_" + System.currentTimeMillis();

        Path localDir = Utils.resolveWithRoot(files, localCWD, dirname);
        files.createDirectory(localDir);

        Path sshDir = Utils.resolveWithRoot(files, sshCWD, dirname);
        files.createDirectory(sshDir);

        // Create file locally and copy to remote        
        Path localFile = Utils.resolveWithRoot(files, localDir, "test");
        files.createFile(localFile);
        Path sshFile = Utils.resolveWithRoot(files, sshDir, "test");
        files.copy(localFile, sshFile, CopyOption.CREATE);

        assertTrue(files.exists(localFile));
        assertTrue(files.exists(sshFile));

        // Create file remotely and copy to local        
        Path localFile2 = Utils.resolveWithRoot(files, localDir, "test2");
        Path sshFile2 = Utils.resolveWithRoot(files, sshDir, "test2");
        files.createFile(sshFile2);
        files.copy(sshFile2, localFile2, CopyOption.CREATE);

        assertTrue(files.exists(localFile2));
        assertTrue(files.exists(sshFile2));

        // cleanup
        files.delete(localFile);
        files.delete(localFile2);

        files.delete(sshFile);
        files.delete(sshFile2);

        files.delete(localDir);
        files.delete(sshDir);

        CobaltFactory.endCobalt(octopus);
    }
}
