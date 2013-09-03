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

import static org.mockito.Mockito.mock;

import java.net.URISyntaxException;

import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.Path;

import org.junit.Test;

public class SandboxTest {
    
    @Test(expected = OctopusException.class)
    public void testSandbox_WithNullOctopus() throws URISyntaxException, OctopusIOException, OctopusException {
        // throws exception
        new Sandbox(null, mock(Path.class), "sandbox-1");
    }

    @Test(expected = OctopusException.class)
    public void testSandbox_WithNullPath() throws URISyntaxException, OctopusIOException, OctopusException {
        // throws exception
        new Sandbox(mock(Files.class), null, "sandbox-1");
    }
}
