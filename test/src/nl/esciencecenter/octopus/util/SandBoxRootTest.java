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

package nl.esciencecenter.octopus.util;

import static org.junit.Assert.*;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.RelativePath;

import org.junit.Test;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class SandBoxRootTest {

    @Test
    public void test() throws OctopusException, OctopusIOException {
        
        Octopus octopus = OctopusFactory.newOctopus(null);
        Files files = octopus.files();
        FileSystem fs = files.getLocalCWDFileSystem(null);
        
        Sandbox sandbox = new Sandbox(octopus, files.newPath(fs, new RelativePath("/tmp")), null);
        
        assertEquals(sandbox.getPath().getParent().getPath(), "/tmp");
        
        OctopusFactory.endAll();
    }

}
