package nl.esciencecenter.octopus.adaptors.ssh;

import java.io.IOException;
import java.io.OutputStream;

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
    private ChannelSftp channel;

    public SshOutputStream(OutputStream out, ChannelSftp channel) {
        this.out = out;
        this.channel = channel;
    }

    public void write(int b) throws IOException {
        out.write(b);
    }

    public int hashCode() {
        return out.hashCode();
    }

    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    public boolean equals(Object obj) {
        return out.equals(obj);
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void close() throws IOException {
        try {
            out.close();
        } finally {
            channel.disconnect();
        }
    }

    public String toString() {
        return out.toString();
    }
}
