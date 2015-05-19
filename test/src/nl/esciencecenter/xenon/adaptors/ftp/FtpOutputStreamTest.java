package nl.esciencecenter.xenon.adaptors.ftp;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.Path;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.Test;

public class FtpOutputStreamTest {
    @Test
    public void close_callTwice_completePendingCommandOnlyOnce() throws IOException {
        // Arrange
        OutputStream stream = new ByteArrayOutputStream();
        FTPClient ftpClient = mock(FTPClient.class);
        FtpOutputStream ftpOutputStream = new FtpOutputStream(stream, ftpClient, mock(Path.class), mock(FtpFiles.class));

        // Act
        ftpOutputStream.close();
        ftpOutputStream.close();

        // Assert
        verify(ftpClient).completePendingCommand();
    }

    @Test
    public void close_callOnce_fileSystemIsClosed() throws IOException, XenonException {
        // Arrange
        OutputStream stream = new ByteArrayOutputStream();
        FtpFiles ftpFiles = mock(FtpFiles.class);
        FtpOutputStream ftpInputStream = new FtpOutputStream(stream, mock(FTPClient.class), mock(Path.class), ftpFiles);

        // Act
        ftpInputStream.close();

        // Assert
        verify(ftpFiles).close(null);
    }
}
