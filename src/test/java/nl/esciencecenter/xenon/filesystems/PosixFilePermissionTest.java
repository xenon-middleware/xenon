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
package nl.esciencecenter.xenon.filesystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class PosixFilePermissionTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConvertToOctalNull() {
        PosixFilePermission.convertToOctal(null);
    }

    @Test
    public void testConvertToOctalNone() {
        String result = PosixFilePermission.convertToOctal(new HashSet<PosixFilePermission>());
        assertEquals("0", result);
    }

    @Test
    public void testConvertToOctalAll() {
        Set<PosixFilePermission> tmp = new HashSet<PosixFilePermission>();

        tmp.add(PosixFilePermission.GROUP_READ);
        tmp.add(PosixFilePermission.GROUP_WRITE);
        tmp.add(PosixFilePermission.GROUP_EXECUTE);
        tmp.add(PosixFilePermission.OWNER_READ);
        tmp.add(PosixFilePermission.OWNER_WRITE);
        tmp.add(PosixFilePermission.OWNER_EXECUTE);
        tmp.add(PosixFilePermission.OTHERS_READ);
        tmp.add(PosixFilePermission.OTHERS_WRITE);
        tmp.add(PosixFilePermission.OTHERS_EXECUTE);

        String result = PosixFilePermission.convertToOctal(tmp);
        assertEquals("777", result);
    }

    @Test
    public void testConvertFromOctalZero() {

        Set<PosixFilePermission> tmp = new HashSet<PosixFilePermission>();
        Set<PosixFilePermission> result = PosixFilePermission.convertFromOctal("0000");
        assertEquals(tmp, result);
    }

    @Test
    public void testConvertFromOctalAll() {

        Set<PosixFilePermission> tmp = new HashSet<PosixFilePermission>();

        tmp.add(PosixFilePermission.GROUP_READ);
        tmp.add(PosixFilePermission.GROUP_WRITE);
        tmp.add(PosixFilePermission.GROUP_EXECUTE);
        tmp.add(PosixFilePermission.OWNER_READ);
        tmp.add(PosixFilePermission.OWNER_WRITE);
        tmp.add(PosixFilePermission.OWNER_EXECUTE);
        tmp.add(PosixFilePermission.OTHERS_READ);
        tmp.add(PosixFilePermission.OTHERS_WRITE);
        tmp.add(PosixFilePermission.OTHERS_EXECUTE);

        Set<PosixFilePermission> result = PosixFilePermission.convertFromOctal("0777");
        assertEquals(tmp, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertFromOctalNull() {
        PosixFilePermission.convertFromOctal(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertFromOctalWrongSize() {
        PosixFilePermission.convertFromOctal("0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertFromOctalWrongData() {
        PosixFilePermission.convertFromOctal("ABCD");
    }

    @Test
    public void testContainsAgainstNull() {
        assertFalse(PosixFilePermission.contains(PosixFilePermission.GROUP_EXECUTE, (PosixFilePermission[]) null));
    }

    @Test
    public void testContainsAgainstEmpty() {
        assertFalse(PosixFilePermission.contains(PosixFilePermission.GROUP_EXECUTE, new PosixFilePermission[0]));
    }

    @Test
    public void testContainsNull() {
        assertFalse(PosixFilePermission.contains(null, PosixFilePermission.GROUP_EXECUTE));
    }

    @Test
    public void testDoesNotContain() {
        assertFalse(PosixFilePermission.contains(PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.GROUP_READ, PosixFilePermission.OTHERS_WRITE));
    }

    @Test
    public void testDoesContain() {
        assertTrue(PosixFilePermission.contains(PosixFilePermission.OTHERS_WRITE, PosixFilePermission.GROUP_READ, PosixFilePermission.OTHERS_WRITE));
    }
}
