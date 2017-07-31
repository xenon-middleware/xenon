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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.esciencecenter.xenon.filesystems.PosixFilePermission;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class PosixFilePermissionTest {
    
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
