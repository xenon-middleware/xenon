/**
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

import static org.junit.Assert.assertTrue;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.adaptors.ssh.SSHJobTestConfig;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.util.Utils;

import org.junit.Test;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class ITInterSchemeCopyTest {

    @Test
    public void test_copy_local_ssh() throws Exception {
        Xenon xenon = XenonEngine.newXenon(null);
        SSHJobTestConfig config = new SSHJobTestConfig(null);

        String user = config.getPropertyOrFail("test.ssh.user");
        String location = config.getPropertyOrFail("test.ssh.location");
        
        Files files = xenon.files();

        Path localCWD = Utils.getLocalCWD(files);
        Path sshCWD = files.newFileSystem("ssh", user + "@" + location, null, null).getEntryPath();

        String dirname = "xenon_test_" + System.currentTimeMillis();

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

        XenonFactory.endXenon(xenon);
    }
}
