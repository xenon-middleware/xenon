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
package nl.esciencecenter.xenon.adaptors.shared.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.filesystems.Path;

public class LocalUtilTest {

    @Test
    public void test_constructor() {
        new LocalUtil();
    }

    @Test(expected=XenonException.class)
    public void test_getLocalRoot_fails() throws XenonException {
        LocalUtil.getRelativePath("/usr/local", "C:/Users");
    }

    @Test
    public void test_getLocalRoot() throws XenonException {
        Path tmp = LocalUtil.getRelativePath("/usr/local", "/usr");
        assertEquals("/local", tmp.toString());
    }

    @Test
    public void test_getLocalRoot_nothingLeft() throws XenonException {
        Path tmp = LocalUtil.getRelativePath("/usr/local", "/usr/local");
        assertTrue(tmp.toString().isEmpty());
    }

    @Test
    public void test_javaPath_() throws XenonException {
        Path tmp = LocalUtil.getRelativePath("/usr/local", "/usr/local");
        assertTrue(tmp.toString().isEmpty());
    }
}
