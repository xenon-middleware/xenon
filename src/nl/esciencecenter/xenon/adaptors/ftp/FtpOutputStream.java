package nl.esciencecenter.xenon.adaptors.ftp;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;

/**
 * Wraps an OutputStream instance following the decorator pattern. Functionality added is sending a pending command completed
 * signal after closing the output stream.
 *
 * @author Christiaan Meijer
 *
 */
public class FtpOutputStream extends OutputStream {

    private OutputStream outputStream;
    private FTPClient ftpClient;

    public FtpOutputStream(OutputStream outputStream, FTPClient ftpClient) {
        this.outputStream = outputStream;
        this.ftpClient = ftpClient;
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        outputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        outputStream.write(b, off, len);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new FtpOutputStream(outputStream, ftpClient);
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
        ftpClient.completePendingCommand();
    }

    @Override
    public boolean equals(Object obj) {
        return outputStream.equals(obj);
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public int hashCode() {
        return outputStream.hashCode();
    }

    @Override
    public String toString() {
        return outputStream.toString();
    }
}
