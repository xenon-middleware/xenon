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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class OpenOptionTest {

    @Test
    public void testContains_empty_doesNotContainIt() {
        OpenOption[] options = new OpenOption[0];
        assertFalse(OpenOption.CREATE.occursIn(options));
    }

    @Test
    public void testContains_filled_doesContainIt() {
        OpenOption[] options = new OpenOption[] { OpenOption.APPEND, OpenOption.CREATE };
        assertTrue(OpenOption.CREATE.occursIn(options));
    }

    @Test
    public void testContains_filled_doesNotContainIt() {
        OpenOption[] options = new OpenOption[] { OpenOption.OPEN };
        assertFalse(OpenOption.CREATE.occursIn(options));
    }

    @Test
    public void testContains_optionsFilledNull_doesNotContainIt() {
        OpenOption[] options = new OpenOption[] { null };
        assertFalse(OpenOption.CREATE.occursIn(options));
    }

    @Test
    public void testContains_valueOf() {
        OpenOption option = OpenOption.valueOf("CREATE");
        assertTrue(option.equals(OpenOption.CREATE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testContains_valueOf_fails() {
        OpenOption.valueOf("AAP");
    }

}
