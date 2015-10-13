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


import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.ssh.SSHJobTestConfig;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.util.Utils;

import org.junit.Test;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class ITInterSchemeMoveTest {
    @Test(expected = XenonException.class)
    public void test_move() throws Exception {
        SSHJobTestConfig config = new SSHJobTestConfig(null);

        String user = config.getPropertyOrFail("test.ssh.user");
        String location = config.getPropertyOrFail("test.ssh.location");
        
        Xenon xenon = XenonEngine.newXenon(null);

        Files files = xenon.files();

        FileSystem fs1 = Utils.getLocalCWD(files).getFileSystem();
        FileSystem fs2 = files.newFileSystem("ssh", user + "@" + location, null, null);

        Path file1 = Utils.resolveWithEntryPath(files, fs1, "test");
        Path file2 = Utils.resolveWithEntryPath(files, fs2, "test");

        files.move(file1, file2);
    }
}
