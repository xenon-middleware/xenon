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

package nl.esciencecenter.xenon.files;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import nl.esciencecenter.xenon.Cobalt;
import nl.esciencecenter.xenon.CobaltException;
import nl.esciencecenter.xenon.engine.CobaltEngine;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.util.Utils;

import org.junit.Test;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class InterSchemeMoveTest {
    
    private String getPropertyOrFail(Properties p, String property) throws Exception {

        String tmp = p.getProperty(property);

        if (tmp == null) {
            throw new Exception("Failed to retireve property " + property);
        }

        return tmp;
    }
    
    @Test(expected = CobaltException.class)
    public void test_move() throws Exception {

        String configfile = System.getProperty("test.config");
        
        if (configfile == null) {
            configfile = System.getProperty("user.home") + File.separator + "octopus.test.properties";
        }
        
        Properties p = new Properties();
        p.load(new FileInputStream(configfile));

        String user = getPropertyOrFail(p, "test.ssh.user");
        String location = getPropertyOrFail(p, "test.ssh.location");
        
        Cobalt octopus = CobaltEngine.newCobalt(null);

        Files files = octopus.files();

        FileSystem fs1 = Utils.getLocalCWD(files).getFileSystem();
        FileSystem fs2 = files.newFileSystem("ssh", user + "@" + location, null, null);

        Path file1 = Utils.resolveWithEntryPath(files, fs1, "test");
        Path file2 = Utils.resolveWithEntryPath(files, fs2, "test");

        files.move(file1, file2);
    }
}
