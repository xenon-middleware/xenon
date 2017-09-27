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
package nl.esciencecenter.xenon.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;

import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.XenonException;

public class LocalFileSystemUtilsTestLinux {

    @Rule
    public final ProvideSystemProperty p1 = new ProvideSystemProperty("os.name", "Linux");

    @Test
    public void test_osLinux() {
        // System.setProperty("os.name", "Linux");
        assertTrue(LocalFileSystemUtils.isLinux());
        assertFalse(LocalFileSystemUtils.isWindows());
        assertFalse(LocalFileSystemUtils.isOSX());
    }

    @Test
    public void test_getLocalRoot_linux_null() throws XenonException {
        // System.setProperty("os.name", "Linux");
        String tmp = LocalFileSystemUtils.getLocalRoot(null);
        assertEquals("/", tmp);
    }

    @Test
    public void test_getLocalRoot_linux_empty() throws XenonException {
        // System.setProperty("os.name", "Linux");
        String tmp = LocalFileSystemUtils.getLocalRoot("");
        assertEquals("/", tmp);
    }

    @Test
    public void test_getLocalRoot_linux_simple() throws XenonException {
        // System.setProperty("os.name", "Linux");
        String tmp = LocalFileSystemUtils.getLocalRoot("/");
        assertEquals("/", tmp);
    }

    @Test
    public void test_getLocalRoot_linux() throws XenonException {
        // System.setProperty("os.name", "Linux");
        String tmp = LocalFileSystemUtils.getLocalRoot("/usr/local");
        assertEquals("/", tmp);
    }

    @Test(expected = InvalidLocationException.class)
    public void test_getLocalRoot_linux_wrong() throws XenonException {
        // System.setProperty("os.name", "Linux");
        LocalFileSystemUtils.getLocalRoot("Hello World");
    }

    @Test
    public void test_isLocalRootLinuxTrue() {
        // System.setProperty("os.name", "Linux");
        assertTrue(LocalFileSystemUtils.isLocalRoot("/"));
    }

    @Test
    public void test_isLocalRootLinuxFalse() {
        // System.setProperty("os.name", "Linux");
        assertFalse(LocalFileSystemUtils.isLocalRoot("C:"));
    }

}
