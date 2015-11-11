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

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.NoSuchPathException;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.SftpException;

import static org.junit.Assert.assertEquals;
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

    
    @org.junit.Test
    public void testsftpExceptionToXenonException1() throws Exception {
        XenonException e = SshUtil.sftpExceptionToXenonException(new SftpException(ChannelSftp.SSH_FX_OK, "test"));
        assertTrue(e instanceof XenonException);
        assertEquals(e.getMessage(), "ssh adaptor: test");
    }
    
    public void testsftpExceptionToXenonException2() throws Exception {
        XenonException e = SshUtil.sftpExceptionToXenonException(new SftpException(ChannelSftp.SSH_FX_EOF, "test"));
        assertTrue(e instanceof EndOfFileException);
    }
    
    public void testsftpExceptionToXenonException3() throws Exception {
        XenonException e = SshUtil.sftpExceptionToXenonException(new SftpException(ChannelSftp.SSH_FX_NO_SUCH_FILE, "test"));
        assertTrue(e instanceof NoSuchPathException);
    }
    
    public void testsftpExceptionToXenonException4() throws Exception {
        XenonException e = SshUtil.sftpExceptionToXenonException(new SftpException(ChannelSftp.SSH_FX_PERMISSION_DENIED, "test"));
        assertTrue(e instanceof PermissionDeniedException);
    }
    
    public void testsftpExceptionToXenonException5() throws Exception {
        XenonException e = SshUtil.sftpExceptionToXenonException(new SftpException(ChannelSftp.SSH_FX_FAILURE, "test"));
        assertTrue(e instanceof XenonException);
        assertEquals(e.getMessage(), "SSH gave an unknown error");
    }
    
    public void testsftpExceptionToXenonException6() throws Exception {
        XenonException e = SshUtil.sftpExceptionToXenonException(new SftpException(ChannelSftp.SSH_FX_NO_CONNECTION, "test"));
        assertTrue(e instanceof XenonException);
        assertEquals(e.getMessage(), "SSH received a malformed message");
    }
    
    public void testsftpExceptionToXenonException7() throws Exception {
        XenonException e = SshUtil.sftpExceptionToXenonException(new SftpException(ChannelSftp.SSH_FX_CONNECTION_LOST, "test"));
        assertTrue(e instanceof ConnectionLostException);
    }

    public void testsftpExceptionToXenonException8() throws Exception {
        XenonException e = SshUtil.sftpExceptionToXenonException(new SftpException(ChannelSftp.SSH_FX_OP_UNSUPPORTED, "test"));
        assertTrue(e instanceof UnsupportedIOOperationException);
    }
    
    public void testsftpExceptionToXenonException9() throws Exception {
        XenonException e = SshUtil.sftpExceptionToXenonException(new SftpException(42, "test"));
        assertTrue(e instanceof XenonException);
        assertEquals(e.getMessage(), "Unknown SSH exception");
    }
}
