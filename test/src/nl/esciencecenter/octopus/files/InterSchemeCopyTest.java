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

package nl.esciencecenter.octopus.files;

import static org.junit.Assert.assertTrue;

import java.net.URI;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.engine.OctopusEngine;

import org.junit.Test;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class InterSchemeCopyTest {

    @Test
    public void test_copy_local_ssh() throws Exception {

        Octopus octopus = OctopusEngine.newOctopus(null);

        Files files = octopus.files();

        FileSystem localFS = files.getLocalCWDFileSystem();
        FileSystem sshFS = files.newFileSystem(new URI("ssh://test@localhost"), null, null);

        String dirname = "octopus_test_" + System.currentTimeMillis();
        
        AbsolutePath localDir = localFS.getEntryPath().resolve(new RelativePath(dirname));
        files.createDirectory(localDir);
        
        AbsolutePath sshDir = sshFS.getEntryPath().resolve(new RelativePath(dirname));
        files.createDirectory(sshDir);
        
        // Create file locally and copy to remote        
        AbsolutePath localFile = localDir.resolve(new RelativePath("test"));
        files.createFile(localFile);
        AbsolutePath sshFile = sshDir.resolve(new RelativePath("test")); 
        files.copy(localFile, sshFile, CopyOption.CREATE);

        assertTrue(files.exists(localFile));
        assertTrue(files.exists(sshFile));
        
        // Create file remotely and copy to local        
        AbsolutePath localFile2 = localDir.resolve(new RelativePath("test2"));
        AbsolutePath sshFile2 = sshDir.resolve(new RelativePath("test2")); 
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
        
        OctopusFactory.endOctopus(octopus);
    }
}
