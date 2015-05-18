package nl.esciencecenter.xenon.adaptors.ftp;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTPClient;

/**
 * Wraps an InputStream instance. Only functionality added is sending a pending command completed signal after closing the input
 * stream.
 *
 * @author Christiaan Meijer
 *
 */
public class FtpInputStream extends InputStream {
    private InputStream inputStream;
    private FTPClient ftpClient;
    private boolean completedPendingFtpCommand = false;

    public FtpInputStream(InputStream inputStream, FTPClient ftpClient) {
        this.inputStream = inputStream;
        this.ftpClient = ftpClient;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();

        // Added functionality:
        if (completedPendingFtpCommand == false) {
            ftpClient.completePendingCommand();
            completedPendingFtpCommand = true;
        }
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new FtpInputStream(inputStream, ftpClient);
    }

    @Override
    public boolean equals(Object obj) {
        return inputStream.equals(obj);
    }

    @Override
    public int hashCode() {
        return inputStream.hashCode();
    }

    @Override
    public synchronized void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return inputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    @Override
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    @Override
    public String toString() {
        return inputStream.toString();
    }

}
