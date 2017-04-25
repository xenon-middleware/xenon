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
import java.io.InputStream;

import nl.esciencecenter.xenon.XenonException;

import com.jcraft.jsch.ChannelSftp;

/**
 * We have to implement a special input stream, and delegate all calls to the internal jcraft input stream, because we have to
 * disconnect the channel on a close of the stream.
 * 
 */
public class SshInputStream extends InputStream {
    private final InputStream in;
    private final SshMultiplexedSession session;
    private final ChannelSftp channel;

    public SshInputStream(InputStream in, SshMultiplexedSession session, ChannelSftp channel) {
        this.in = in;
        this.session = session;
        this.channel = channel;
    }

    @Override
    public int read() throws IOException {
        return in.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return in.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    @Override
    public String toString() {
        return in.toString();
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public void close() throws IOException {
        IOException tmp = null;
        
        try {
            // First attempt to close the in stream.
            in.close();
        } catch (IOException e) {
            tmp = new IOException("Failed to close the SSH input stream!", e);
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
    public void mark(int readlimit) {
        in.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        in.reset();
    }

    @Override
    public boolean markSupported() {
        return in.markSupported();
    }
}
