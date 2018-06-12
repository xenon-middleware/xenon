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

public class LocalFileSystemUtilsTestWindows {

    @Rule
    public final ProvideSystemProperty p2 = new ProvideSystemProperty("os.name", "Windows7");

    @Test
    public void test_osWindows() {
        // System.setProperty("os.name", "Windows7");
        assertFalse(LocalFileSystemUtils.isLinux());
        assertTrue(LocalFileSystemUtils.isWindows());
        assertFalse(LocalFileSystemUtils.isOSX());
    }

    @Test
    public void test_isLocalRootWindowsTrue() {
        // System.setProperty("os.name", "Windows7");
        assertFalse(LocalFileSystemUtils.isLocalRoot("/"));
    }

    @Test
    public void test_isLocalRootWindowsFalse() {
        // System.setProperty("os.name", "Windows7");
        assertTrue(LocalFileSystemUtils.isLocalRoot("C:"));
    }

    @Test
    public void test_getLocalRoot_windows_null() throws XenonException {
        // System.setProperty("os.name", "Windows7");
        String tmp = LocalFileSystemUtils.getLocalRoot(null);
        assertEquals("C:", tmp);
    }

    @Test
    public void test_getLocalRoot_windows_empty() throws XenonException {
        // System.setProperty("os.name", "Windows7");
        String tmp = LocalFileSystemUtils.getLocalRoot("");
        assertEquals("C:", tmp);
    }

    @Test
    public void test_getLocalRoot_windows_simple() throws XenonException {
        // System.setProperty("os.name", "Windows7");
        String tmp = LocalFileSystemUtils.getLocalRoot("C:/Users");
        assertEquals("C:", tmp);
    }

    @Test
    public void test_getLocalRoot_windows_leadingSlash() throws XenonException {
        // System.setProperty("os.name", "Windows7");
        String tmp = LocalFileSystemUtils.getLocalRoot("/C:/Users");
        assertEquals("C:", tmp);
    }

    @Test(expected = InvalidLocationException.class)
    public void test_getLocalRoot_windows_tooShort() throws XenonException {
        // System.setProperty("os.name", "Windows7");
        LocalFileSystemUtils.getLocalRoot("C");
    }

    @Test(expected = InvalidLocationException.class)
    public void test_getLocalRoot_windows_noColon() throws XenonException {
        // System.setProperty("os.name", "Windows7");
        LocalFileSystemUtils.getLocalRoot("C/Users");
    }

    @Test(expected = InvalidLocationException.class)
    public void test_getLocalRoot_windows_noDrive() throws XenonException {
        // System.setProperty("os.name", "Windows7");
        LocalFileSystemUtils.getLocalRoot("1:/Users");
    }
}
