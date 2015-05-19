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

    private OutputStream outputStream;
    private FTPClient ftpClient;
    private boolean completedPendingFtpCommand = false;
    private Path path;
    private FtpFiles ftpFiles;

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
    protected Object clone() throws CloneNotSupportedException {
        return new FtpOutputStream(outputStream, ftpClient, path, ftpFiles);
    }

    @Override
    public void close() throws IOException {
        outputStream.close();

        // Added functionality:
        if (completedPendingFtpCommand == false) {
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
