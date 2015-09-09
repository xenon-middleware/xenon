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

package nl.esciencecenter.xenon.engine.files;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import nl.esciencecenter.xenon.engine.files.CopyImplementation;

import org.junit.Test;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class CopyImplementationTest {

    @Test
    public void test_getters_and_setters() {

        CopyImplementation c = new CopyImplementation("NAME", "ID", null, null);

        assertTrue("NAME".equals(c.getAdaptorName()));
        assertTrue("ID".equals(c.getUniqueID()));

        assertNull(c.getSource());
        assertNull(c.getTarget());
    }

}
