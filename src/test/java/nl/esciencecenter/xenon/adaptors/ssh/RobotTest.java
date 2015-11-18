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
package nl.esciencecenter.xenon.adaptors.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class RobotTest {

    @org.junit.Test
    public void testPrompt1() throws Exception {
        Robot r = new Robot(true);
        assertTrue(r.promptYesNo(null));
    }

    @org.junit.Test
    public void testPrompt2() throws Exception {
        Robot r = new Robot(false);
        assertFalse(r.promptYesNo(null));
    }

    @org.junit.Test
    public void testPromptPassword() throws Exception {
        Robot r = new Robot(false);
        assertFalse(r.promptPassword(null));
    }

    @org.junit.Test
    public void testPromptPassphrase() throws Exception {
        Robot r = new Robot(false);
        assertFalse(r.promptPassphrase(null));
    }

    @org.junit.Test
    public void testGetPassword() throws Exception {
        Robot r = new Robot(false);
        assertEquals(r.getPassword(), null);
    }

    @org.junit.Test
    public void testGetPassphrase() throws Exception {
        Robot r = new Robot(false);
        assertEquals(r.getPassphrase(), null);
    }
}
