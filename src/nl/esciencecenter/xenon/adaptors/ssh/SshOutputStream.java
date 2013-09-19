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

import java.io.IOException;
import java.io.OutputStream;

import nl.esciencecenter.xenon.CobaltException;


import com.jcraft.jsch.ChannelSftp;

/**
 * We have to implement a special output stream, and delegate all calls to the internal jcraft output stream, because we have to
 * disconnect the channel on a close of the stream.
 * 
 * @author rob
 * 
 */
public class SshOutputStream extends OutputStream {
    private OutputStream out;
    private SshMultiplexedSession session;
    private ChannelSftp channel;

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
    public int hashCode() {
        return out.hashCode();
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
    public boolean equals(Object obj) {
        return out.equals(obj);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        try {
            out.close();
        } finally {
            try { 
                session.releaseSftpChannel(channel);
            } catch (CobaltException e) { 
                throw new IOException("Failed to release SSH channel!", e);
            }
        } 
    }

    @Override
    public String toString() {
        return out.toString();
    }
}
