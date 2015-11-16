package nl.esciencecenter.xenon.adaptors.ftp;

import java.io.IOException;
import java.io.OutputStream;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.Path;

import org.apache.commons.net.ftp.FTPClient;

/**
 * Wraps an OutputStream instance. Only functionality added is sending a pending command completed signal after closing the output
 * stream.
 *
 * @author Christiaan Meijer
 *
 */
public class FtpOutputStream extends OutputStream {

    private final OutputStream outputStream;
    private final FTPClient ftpClient;
    private boolean completedPendingFtpCommand = false;
    private final Path path;
    private final FtpFiles ftpFiles;

    public FtpOutputStream(OutputStream outputStream, FTPClient ftpClient, Path path, FtpFiles ftpFiles) {
        this.outputStream = outputStream;
        this.path = path;
        this.ftpClient = ftpClient;
        this.ftpFiles = ftpFiles;
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
    public void close() throws IOException {
        outputStream.close();

        // Added functionality:
        if (!completedPendingFtpCommand) {
            ftpClient.completePendingCommand();
            completedPendingFtpCommand = true;
            try {
                ftpFiles.close(path.getFileSystem());
            } catch (XenonException e) {
                throw new IOException("Could not close file system for ftp output stream", e);
            }
        }
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public String toString() {
        return outputStream.toString();
    }
}
