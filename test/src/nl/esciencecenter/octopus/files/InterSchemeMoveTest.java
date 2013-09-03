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

import java.net.URI;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.Util;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.util.FileUtils;

import org.junit.Test;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class InterSchemeMoveTest {

    @Test(expected = OctopusIOException.class)
    public void test_move() throws Exception {

        Octopus octopus = OctopusEngine.newOctopus(null);

        Files files = octopus.files();

        FileSystem fs1 = FileUtils.getLocalCWD(files).getFileSystem();
        FileSystem fs2 = files.newFileSystem("ssh", "test@localhost", null, null);

        Path file1 = Util.resolve(files, fs1, "test");
        Path file2 = Util.resolve(files, fs2, "test");

        files.move(file1, file2);
    }
}
