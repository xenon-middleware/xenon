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

package nl.esciencecenter.octopus.adaptors.ssh;

import static org.junit.Assert.*;

import java.util.EnumSet;
import java.util.Set;

import nl.esciencecenter.octopus.files.PosixFilePermission;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class SSHUtilTest {
    
    @org.junit.Test
    public void testConstructor() throws Exception {
        // Dummy test for coverage
        new SshUtil();
    }
    
    @org.junit.Test
    public void testAllBits() throws Exception {

        Set<PosixFilePermission> tmp = EnumSet.allOf(PosixFilePermission.class);
        
        int bits = SshUtil.permissionsToBits(tmp);
        
        Set<PosixFilePermission> tmp2 = SshUtil.bitsToPermissions(bits);
        
        assertEquals(tmp, tmp2);
        
        assertTrue(SshUtil.isExecutable(bits));
        assertTrue(SshUtil.isReadable(bits));
        assertTrue(SshUtil.isWritable(bits));
    }
        
    @org.junit.Test
    public void testNoBits() throws Exception {

        Set<PosixFilePermission> tmp = EnumSet.noneOf(PosixFilePermission.class);
        
        int bits = SshUtil.permissionsToBits(tmp);
        
        Set<PosixFilePermission> tmp2 = SshUtil.bitsToPermissions(bits);
        
        assertEquals(tmp, tmp2);
        
        assertFalse(SshUtil.isExecutable(bits));
        assertFalse(SshUtil.isReadable(bits));
        assertFalse(SshUtil.isWritable(bits));
    }
}
