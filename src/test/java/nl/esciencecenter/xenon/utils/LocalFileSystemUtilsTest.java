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

import java.io.File;

import org.junit.Test;

public class LocalFileSystemUtilsTest {

    @Test
    public void test_windowsRootNull() {
        assertFalse(LocalFileSystemUtils.isWindowsRoot(null));
    }

    @Test
    public void test_windowsRoot_correctDriveLetter() {
        assertTrue(LocalFileSystemUtils.isWindowsRoot("C:"));
    }

    @Test
    public void test_windowsRoot_invalidDriveLetter() {
        assertFalse(LocalFileSystemUtils.isWindowsRoot("1:"));
    }

    @Test
    public void test_windowsRoot_correctDriveLetterSlash() {
        assertTrue(LocalFileSystemUtils.isWindowsRoot("C:\\"));
    }

    @Test
    public void test_windowsRoot_invalidDriveLetterSlash() {
        assertFalse(LocalFileSystemUtils.isWindowsRoot("1:\\"));
    }

    @Test
    public void test_windowsRoot_noSlash() {
        assertFalse(LocalFileSystemUtils.isWindowsRoot("C:X"));
    }

    @Test
    public void test_windowsRoot_invalidString1() {
        assertFalse(LocalFileSystemUtils.isWindowsRoot("C"));
    }

    @Test
    public void test_windowsRoot_invalidString2() {
        assertFalse(LocalFileSystemUtils.isWindowsRoot("CC"));
    }

    @Test
    public void test_windowsRoot_invalidString3() {
        assertFalse(LocalFileSystemUtils.isWindowsRoot("CCC"));
    }

    @Test
    public void test_windowsRoot_invalidString4() {
        assertFalse(LocalFileSystemUtils.isWindowsRoot("CCCC"));
    }

    @Test
    public void test_linuxRootNull() {
        assertFalse(LocalFileSystemUtils.isLinuxRoot(null));
    }

    @Test
    public void test_linuxRootSlash() {
        assertTrue(LocalFileSystemUtils.isLinuxRoot("/"));
    }

    @Test
    public void test_linuxRootOther() {
        assertFalse(LocalFileSystemUtils.isLinuxRoot("C:"));
    }

    @Test
    public void test_osxRootNull() {
        assertFalse(LocalFileSystemUtils.isOSXRoot(null));
    }

    @Test
    public void test_osxRootSlash() {
        assertTrue(LocalFileSystemUtils.isOSXRoot("/"));
    }

    @Test
    public void test_osxRootOther() {
        assertFalse(LocalFileSystemUtils.isOSXRoot("C:"));
    }

    @Test
    public void test_startsWithLinuxRootNull() {
        assertFalse(LocalFileSystemUtils.startsWithLinuxRoot(null));
    }

    @Test
    public void test_startsWithLinuxRootTrue() {
        assertTrue(LocalFileSystemUtils.startsWithLinuxRoot("/usr/local"));
    }

    @Test
    public void test_startsWithLinuxRootFalse() {
        assertFalse(LocalFileSystemUtils.startsWithLinuxRoot("C:/Users"));
    }

    @Test
    public void test_startsWithWindowsRootNull() {
        assertFalse(LocalFileSystemUtils.startWithWindowsRoot(null));
    }

    @Test
    public void test_startsWithWindowsRootTrue() {
        assertTrue(LocalFileSystemUtils.startWithWindowsRoot("C:/Users"));
    }

    @Test
    public void test_startsWithWindowsRootFalse_linux() {
        assertFalse(LocalFileSystemUtils.startWithWindowsRoot("/usr/local"));
    }

    @Test
    public void test_startsWithWindowsRootFalse_tooShort() {
        assertFalse(LocalFileSystemUtils.startWithWindowsRoot("c"));
    }

    @Test
    public void test_startsWithWindowsRootFalse_noColon() {
        assertFalse(LocalFileSystemUtils.startWithWindowsRoot("C/Users"));
    }

    @Test
    public void test_startsWithWindowsRootFalse_noDriveLetter() {
        assertFalse(LocalFileSystemUtils.startWithWindowsRoot("1:/Users"));
    }

    @Test
    public void test_startsWithRootNull() {
        assertFalse(LocalFileSystemUtils.startWithRoot(null));
    }

    @Test
    public void test_startsWithRoot_linux() {
        assertTrue(LocalFileSystemUtils.startWithRoot("/usr/local"));
    }

    @Test
    public void test_startsWithRoot_windows() {
        assertTrue(LocalFileSystemUtils.startWithRoot("C:/Users"));
    }

    @Test
    public void test_startsWithRoot_noRoot() {
        assertFalse(LocalFileSystemUtils.startWithRoot("Hello World"));
    }

    @Test
    public void test_localSeparator() {
        assertEquals(File.separatorChar, LocalFileSystemUtils.getLocalSeparator());
    }
}
