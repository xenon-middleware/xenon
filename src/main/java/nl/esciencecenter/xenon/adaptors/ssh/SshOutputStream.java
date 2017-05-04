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

import java.io.IOException;
import java.io.OutputStream;

import nl.esciencecenter.xenon.XenonException;


import com.jcraft.jsch.ChannelSftp;

/**
 * We have to implement a special output stream, and delegate all calls to the internal jcraft output stream, because we have to
 * disconnect the channel on a close of the stream.
 * 
 */
public class SshOutputStream extends OutputStream {
    private final OutputStream out;
    private final SshMultiplexedSession session;
    private final ChannelSftp channel;

    public SshOutputStream(OutputStream out, SshMultiplexedSession session, ChannelSftp channel) {
        this.out = out;
        this.session = session;
        this.channel = channel;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        
        IOException tmp = null;
        
        try {
            // First attempt to close the in stream.
            out.close();
        } catch (IOException e) {
            tmp = new IOException("Failed to close the SSH output stream!", e);
        } 
        
        try { 
            // Next, attempt to release the channel, even if in failed to close. 
            session.releaseSftpChannel(channel);
        } catch (XenonException e) { 
            if (tmp == null) {  
                tmp = new IOException("Failed to release SSH channel!", e);
            }
        }
        
        if (tmp != null) { 
            // throw the first exception we encountered, if any
            throw tmp;
        }
    }

    @Override
    public String toString() {
        return out.toString();
    }
}
