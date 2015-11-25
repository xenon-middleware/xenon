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
package nl.esciencecenter.xenon.adaptors.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;

import nl.esciencecenter.xenon.XenonException;

import com.jcraft.jsch.ChannelSftp;

/**
 * 
 */
public class SshInputStreamTest {

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
    
    private static SshInputStream createSshInputStream(byte [] data) {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        DummySession s = new DummySession();
        return new SshInputStream(in, s, null);
    }
    
    private static SshInputStream createSshInputStream() {
        byte [] data = new byte[] { 42, 43 };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        DummySession s = new DummySession();
        return new SshInputStream(in, s, null);
    }
   
    @Test
    public void testRead() throws Exception {
        SshInputStream t = createSshInputStream();
        assertEquals(t.read(), 42);
        assertEquals(t.read(), 43);
    }

    @Test
    public void testReadArray1() throws Exception {
        byte [] data = new byte[] { 42, 43 };
        SshInputStream t = createSshInputStream(data);
        byte [] toRead = new byte[2];        
        t.read(toRead);
        assertEquals(toRead[0], 42);
        assertEquals(toRead[1], 43);
    }
    
    @Test
    public void testReadArray2() throws Exception {
        SshInputStream t = createSshInputStream();

        byte [] toRead = new byte[1];
        
        t.read(toRead);
        assertEquals(toRead[0], 42);
        
        t.read(toRead);
        assertEquals(toRead[0], 43);
    }

    @Test
    public void testReadArrayOffset() throws Exception {
        SshInputStream t = createSshInputStream();

        byte [] toRead = new byte[2];
        toRead[0] = 2;
        
        t.read(toRead, 1, 1);
        
        assertEquals(toRead[0], 2);
        assertEquals(toRead[1], 42);
    }
      
    @Test
    public void testSkip1() throws Exception {
        SshInputStream t = createSshInputStream();
        t.skip(1);
        byte [] toRead = new byte[1];
        t.read(toRead);
        assertEquals(toRead[0], 43);
    }
   
    @Test
    public void testSkip2() throws Exception {
        SshInputStream t = createSshInputStream();
        t.skip(0);
        byte [] toRead = new byte[1];
        t.read(toRead);
        assertEquals(toRead[0], 42);
    }

    @Test
    public void testAvailable() throws Exception {
        SshInputStream t = createSshInputStream();
        assertEquals(t.available(), 2);
    }
    
    @Test
    public void testMarkSupported() throws Exception {
        byte [] data = new byte[] { 42, 43 };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        DummySession s = new DummySession();        
        SshInputStream t = new SshInputStream(in, s, null);

        assertEquals(t.markSupported(), in.markSupported());
    }
    
    @Test
    public void testMark() throws Exception {
        SshInputStream t = createSshInputStream();
        t.mark(5);
        
        byte [] toRead = new byte[1];
        t.read(toRead);
        assertEquals(toRead[0], 42);
        
        t.reset();
        t.read(toRead);
        assertEquals(toRead[0], 42);
        
        t.read(toRead);
        assertEquals(toRead[0], 43);        
    }

    @Test
    public void testClose() throws Exception {
        byte [] data = new byte[] { 42, 43 };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        DummySession s = new DummySession();        
        SshInputStream t = new SshInputStream(in, s, null);
        t.close();
        assertTrue(s.released);
    }
    
    @Test(expected=IOException.class)
    public void testClose2() throws Exception {
        byte [] data = new byte[] { 42, 43 };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        DummySession s = new DummySession();        
        SshInputStream t = new SshInputStream(in, s, null);
        t.close();
        // Should throw exception
        t.close();
    }
    
    @Test
    public void testToString() throws Exception {
        byte [] data = new byte[] { 42, 43 };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        DummySession s = new DummySession();        
        SshInputStream t = new SshInputStream(in, s, null);
        
        assertEquals(t.toString(), in.toString());
    }
}
