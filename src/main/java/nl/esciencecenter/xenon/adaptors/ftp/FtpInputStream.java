package nl.esciencecenter.xenon.adaptors.ftp;

import java.io.IOException;
import java.io.InputStream;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.Path;

import org.apache.commons.net.ftp.FTPClient;

/**
 * Wraps an InputStream instance. Only functionality added is sending a pending command completed signal after closing the input
 * stream.
 *
 * @author Christiaan Meijer
 *
 */
@SuppressWarnings("CanBeFinal")
public class FtpInputStream extends InputStream {
    private final InputStream inputStream;
    private final FTPClient ftpClient;
    private boolean completedPendingFtpCommand = false;
    private final Path path;
    private final FtpFiles ftpFiles;

    public FtpInputStream(InputStream inputStream, FTPClient ftpClient, Path path, FtpFiles ftpFiles) {
        this.inputStream = inputStream;
        this.ftpClient = ftpClient;
        this.path = path;
        this.ftpFiles = ftpFiles;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();

        // Added functionality:
        if (!completedPendingFtpCommand) {
            ftpClient.completePendingCommand();
            completedPendingFtpCommand = true;
            try {
                ftpFiles.close(path.getFileSystem());
            } catch (XenonException e) {
                throw new IOException("Could not close file system for ftp input stream", e);
            }
        }
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
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
