package nl.esciencecenter.octopus.adaptors.ssh;

import java.io.IOException;
import java.io.InputStream;

import com.jcraft.jsch.ChannelSftp;

/**
 * We have to implement a special input stream, and delegate all calls to the internal jcraft input stream, because we have to
 * disconnect the channel on a close of the stream.
 * 
 * @author rob
 * 
 */
public class SshInputStream extends InputStream {
    private InputStream in;
    private ChannelSftp channel;

    public SshInputStream(InputStream in, ChannelSftp channel) {
        this.in = in;
        this.channel = channel;
    }

    @Override
    public int read() throws IOException {
        return in.read();
    }

    @Override
    public int hashCode() {
        return in.hashCode();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return in.read(b);
    }

    @Override
    public boolean equals(Object obj) {
        return in.equals(obj);
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
        try {
            in.close();
        } finally {
            channel.disconnect();
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
