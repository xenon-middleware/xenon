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

import static org.junit.Assert.*;

import org.junit.Test;

public class CopyOptionTest {

    @Test
    public void testContains_empty_doesNotContainIt() {
        CopyOption[] options = new CopyOption[0];
        assertFalse(CopyOption.contains(options, CopyOption.CREATE));
    }

    @Test
    public void testContains_filled_doesContainIt() {
        CopyOption[] options = new CopyOption[] { CopyOption.ASYNCHRONOUS, CopyOption.CREATE };
        assertTrue(CopyOption.contains(options, CopyOption.CREATE));
    }

    @Test
    public void testContains_filled_doesNotContainIt() {
        CopyOption[] options = new CopyOption[] { CopyOption.REPLACE };
        assertFalse(CopyOption.contains(options, CopyOption.CREATE));
    }

    @Test
    public void testContains_optionsNull_doesNotContainIt() {
        assertFalse(CopyOption.contains(null, CopyOption.CREATE));
    }

    @Test
    public void testContains_optionNull_doesNotContainIt() {
        CopyOption[] options = new CopyOption[] { CopyOption.ASYNCHRONOUS, CopyOption.CREATE };
        assertFalse(CopyOption.contains(options, null));
    }

    @Test
    public void testContains_optionsFilledNull_doesNotContainIt() {
        CopyOption[] options = new CopyOption[] { null };
        assertFalse(CopyOption.contains(options, CopyOption.CREATE));
    }

}
