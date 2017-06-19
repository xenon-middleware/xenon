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

package nl.esciencecenter.xenon.adaptors.file.sftp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.adaptors.job.ssh.SSHJobTestConfig;
import nl.esciencecenter.xenon.files.CopyOption;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.OpenOption;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;
import nl.esciencecenter.xenon.util.Utils;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class SftpSamePathCopyTest {
 
    public static SSHJobTestConfig config;
    
    protected Xenon xenon;
    protected Files files;
    
    @BeforeClass
    public static void prepareSSHConfig() throws Exception {
        config = new SSHJobTestConfig(null);    
    }

    @Before
    public void prepare() throws Exception {
        // This is not an adaptor option, so it will throw an exception!
        //properties.put(SshAdaptor.POLLING_DELAY, "100");

        Map<String, String> properties = new HashMap<>();
        
        xenon = XenonFactory.newXenon(properties);
        files = xenon.files();
    }
    
    public Path resolve(Path root, String... path) throws XenonException {
        return files.newPath(root.getFileSystem(), root.getRelativePath().resolve(new RelativePath(path)));
    }
    
    // Depends on: newOutputStream
    private void writeData(Path testFile, byte[] data) throws Exception {

        OutputStream out = null;

        try {
            out = files.newOutputStream(testFile, OpenOption.CREATE, OpenOption.TRUNCATE, OpenOption.WRITE);

            if (data != null) {
                out.write(data);
            }
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                //ignore
            }
        }
    }
    
    @Test
    public void testCopyFileWithSamePath() throws Exception {
        
        // This test check for the bug that a copy would fail if the source and target paths are the same, regardless of the 
        // filesystem or machine they where on. So copying a local /tmp/foo.txt to ssh://somemachine/tmp/foo.txt would do nothing, 
        // since source /tmp/foo.txt matches target /tmp/foo.txt
        
        // Test file name
        String name = "xenon-" + (int) (1000000.0 * Math.random());
        
        // Create two filesystems, one local, one SSH
        FileSystem fs1 = files.newFileSystem("local", "/", null, null);
        FileSystem fs2 = files.newFileSystem("ssh", config.getCorrectLocation(), config.getDefaultCredential(), null);
        
        // Create to paths to /tmp, one local, one remote.
        Path p1 = files.newPath(fs1, new RelativePath("/tmp"));
        Path p2 = files.newPath(fs2, new RelativePath("/tmp"));
        
        // Resolve the src and target file names, and check if they exist.
        Path src = resolve(p1, name);
        assertFalse("Generated SOURCE test file already exists! " + src, files.exists(src));
        
        Path target = resolve(p2, name);
        assertFalse("Generated TARGET test file already exists! " + target, files.exists(target));
        
        byte [] sourceData = "Hello wrong".getBytes();
        
        // Write some data in the src
        writeData(src, sourceData);
        
        // Copy to the target
        files.copy(src, target, CopyOption.CREATE);
        
        // Check if target exists and contains the expected data.
        assertTrue("TARGET test file does not exist! " + target, files.exists(target));
        
        byte [] targetData = Utils.readAllBytes(files, target);

        assertTrue("SOURCE and TARGET file do not contain the same data!", Arrays.equals(sourceData, targetData));
        
        // Cleanup
        files.delete(src);
        files.delete(target);
        
        files.close(fs1);
        files.close(fs2);
    }
} 
