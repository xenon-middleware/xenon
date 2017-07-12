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
package nl.esciencecenter.xenon.filesystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.Set;

import nl.esciencecenter.xenon.adaptors.filesystems.PosixFileUtils;
import nl.esciencecenter.xenon.filesystems.PosixFilePermission;

/**
 * 
 */
public class PosixFileUtilsTest {

    @org.junit.Test
    public void testConstructor() throws Exception {
        // Dummy test for coverage
        new PosixFileUtils();
    }

    @org.junit.Test
    public void testAllBits() throws Exception {

        Set<PosixFilePermission> tmp = EnumSet.allOf(PosixFilePermission.class);

        int bits = PosixFileUtils.permissionsToBits(tmp);

        Set<PosixFilePermission> tmp2 = PosixFileUtils.bitsToPermissions(bits);

        assertEquals(tmp, tmp2);

        assertTrue(PosixFileUtils.isExecutable(bits));
        assertTrue(PosixFileUtils.isReadable(bits));
        assertTrue(PosixFileUtils.isWritable(bits));
    }

    @org.junit.Test
    public void testNoBits() throws Exception {

        Set<PosixFilePermission> tmp = EnumSet.noneOf(PosixFilePermission.class);

        int bits = PosixFileUtils.permissionsToBits(tmp);

        Set<PosixFilePermission> tmp2 = PosixFileUtils.bitsToPermissions(bits);

        assertEquals(tmp, tmp2);

        assertFalse(PosixFileUtils.isExecutable(bits));
        assertFalse(PosixFileUtils.isReadable(bits));
        assertFalse(PosixFileUtils.isWritable(bits));
    }
}
