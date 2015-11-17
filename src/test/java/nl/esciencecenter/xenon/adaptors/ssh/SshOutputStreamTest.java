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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import nl.esciencecenter.xenon.XenonException;

import org.junit.Test;

import com.jcraft.jsch.ChannelSftp;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class SshOutputStreamTest {

    static class DummySession extends SshMultiplexedSession {

        private boolean released = false;
        
        DummySession() {
            super();
        }

        protected void releaseSftpChannel(ChannelSftp channel) throws XenonException {
            if (!released) { 
                released = true;
            } else { 
                throw new XenonException("test", "Channel released twice!");
            }
        }
    }

    @Test
    public void testWrite() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DummySession s = new DummySession();
        SshOutputStream t = new SshOutputStream(out, s, null);
        
        t.write(42);
        t.write(43);
        
        byte [] data = out.toByteArray();
        
        assertEquals(data[0], 42);
        assertEquals(data[1], 43);
    }

    @Test
    public void testWriteArray() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DummySession s = new DummySession();
        SshOutputStream t = new SshOutputStream(out, s, null);
        
        byte [] tmp = new byte[] { 42, 43 };
        
        t.write(tmp);
        
        byte [] data = out.toByteArray();
        
        assertEquals(data[0], 42);
        assertEquals(data[1], 43);
    }

    @Test
    public void testWriteArray2() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DummySession s = new DummySession();
        SshOutputStream t = new SshOutputStream(out, s, null);
        
        byte [] tmp = new byte[] { 42, 43 };
        
        t.write(tmp, 1, 1);
        
        byte [] data = out.toByteArray();
        
        assertEquals(data[0], 43);
    }

    @Test
    public void testFlush() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DummySession s = new DummySession();
        SshOutputStream t = new SshOutputStream(out, s, null);
        
        t.write(42);
        t.flush();
        
        byte [] data = out.toByteArray();
        
        assertEquals(data[0], 42);
    }

    @Test
    public void testClose() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DummySession s = new DummySession();
        SshOutputStream t = new SshOutputStream(out, s, null);
        t.close();
        assertTrue(s.released);
    }
    
    @Test(expected=IOException.class)
    public void testClose2() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DummySession s = new DummySession();
        SshOutputStream t = new SshOutputStream(out, s, null);
        t.close();
        // Should throw exception
        t.close();
    }

    @Test
    public void testToString() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DummySession s = new DummySession();
        SshOutputStream t = new SshOutputStream(out, s, null);
        assertEquals(t.toString(), out.toString());
    }
}
