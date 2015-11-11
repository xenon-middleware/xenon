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

package nl.esciencecenter.xenon.adaptors.ssh;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.Buffer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    
    private SftpATTRS createEmptySftpATTRS() throws Exception {        
        Method method = SftpATTRS.class.getDeclaredMethod("getATTR", Buffer.class);
        method.setAccessible(true);
        return (SftpATTRS) method.invoke(null, new Buffer(new byte[4]));
    }

    private void setFlags(SftpATTRS obj, int flags) throws Exception {
        Method method = SftpATTRS.class.getDeclaredMethod("setFLAGS", int.class);
        method.setAccessible(true);
        method.invoke(obj, flags);
    }
    
    @org.junit.Test
    public void testEqualsIdentity() throws Exception {       
        SftpATTRS s1 = createEmptySftpATTRS();
        assertTrue(SshUtil.equals(s1, s1));        
    }
    
    @org.junit.Test
    public void testEqualsToNull1() throws Exception {       
        SftpATTRS s1 = createEmptySftpATTRS();
        assertFalse(SshUtil.equals(s1, null));        
    }

    @org.junit.Test
    public void testEqualsToNull2() throws Exception {       
        SftpATTRS s1 = createEmptySftpATTRS();
        assertFalse(SshUtil.equals(null, s1));        
    }
    
    @org.junit.Test
    public void testEqualsNullNull() throws Exception {       
        assertTrue(SshUtil.equals(null, null));        
    }

    @org.junit.Test
    public void testEquals() throws Exception {       
        SftpATTRS s1 = createEmptySftpATTRS();
        SftpATTRS s2 = createEmptySftpATTRS();
        assertTrue(SshUtil.equals(s1, s2));        
    }
    
    @org.junit.Test
    public void testEqualsAtime() throws Exception {       
        SftpATTRS s1 = createEmptySftpATTRS();
        SftpATTRS s2 = createEmptySftpATTRS();
        
        s1.setACMODTIME(42, 42);
        s2.setACMODTIME(22, 22);
        
        assertFalse(SshUtil.equals(s1, s2));        
    }

    @org.junit.Test
    public void testEqualsMtime() throws Exception {       
        SftpATTRS s1 = createEmptySftpATTRS();
        SftpATTRS s2 = createEmptySftpATTRS();
        
        s1.setACMODTIME(42, 42);
        s2.setACMODTIME(42, 22);
        
        assertFalse(SshUtil.equals(s1, s2));        
    }
    
    @org.junit.Test
    public void testEqualsGID() throws Exception {       
        SftpATTRS s1 = createEmptySftpATTRS();
        SftpATTRS s2 = createEmptySftpATTRS();
        
        s1.setUIDGID(42, 42);
        s2.setUIDGID(42, 22);
        
        assertFalse(SshUtil.equals(s1, s2));        
    }
    
    @org.junit.Test
    public void testEqualsUID() throws Exception {       
        SftpATTRS s1 = createEmptySftpATTRS();
        SftpATTRS s2 = createEmptySftpATTRS();
        
        s1.setUIDGID(42, 42);
        s2.setUIDGID(22, 22);
        
        assertFalse(SshUtil.equals(s1, s2));        
    }

    @org.junit.Test
    public void testEqualsPermissions() throws Exception {       
        SftpATTRS s1 = createEmptySftpATTRS();
        SftpATTRS s2 = createEmptySftpATTRS();
        
        s1.setPERMISSIONS(42);
        s2.setPERMISSIONS(0);
        
        assertFalse(SshUtil.equals(s1, s2));        
    }

    @org.junit.Test
    public void testEqualsSize() throws Exception {       
        SftpATTRS s1 = createEmptySftpATTRS();
        SftpATTRS s2 = createEmptySftpATTRS();
        
        s1.setSIZE(42);
        s2.setSIZE(0);
        
        assertFalse(SshUtil.equals(s1, s2));        
    }
    
    @org.junit.Test
    public void testEqualsFlags() throws Exception {       
        SftpATTRS s1 = createEmptySftpATTRS();
        SftpATTRS s2 = createEmptySftpATTRS();
        
        setFlags(s1, 42);
        
        assertFalse(SshUtil.equals(s1, s2));        
    }

    
}
