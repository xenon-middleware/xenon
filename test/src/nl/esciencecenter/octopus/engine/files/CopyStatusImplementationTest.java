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

package nl.esciencecenter.octopus.engine.files;

import static org.junit.Assert.*;
import nl.esciencecenter.octopus.files.Copy;

import org.junit.Test;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class CopyStatusImplementationTest {

    @Test
    public void test_getters_and_setters() {

        Copy c = new CopyImplementation("NAME", "ID", null, null);

        CopyStatusImplementation csi = new CopyStatusImplementation(c, "STATE", true, false, 42, 64, null);

        assertTrue(c.equals(csi.getCopy()));
        assertTrue("STATE".equals(csi.getState()));
        assertTrue(csi.isRunning());
        assertFalse(csi.isDone());
        assertTrue(csi.bytesToCopy() == 42);
        assertTrue(csi.bytesCopied() == 64);
        assertFalse(csi.hasException());

        assertNull(csi.getException());

        csi = new CopyStatusImplementation(c, "STATE", true, false, 42, 64, new Exception());

        assertTrue(csi.hasException());

        Exception e = csi.getException();

        assertNotNull(e);
    }

}
