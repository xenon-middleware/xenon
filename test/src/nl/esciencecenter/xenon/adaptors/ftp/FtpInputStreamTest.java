package nl.esciencecenter.xenon.adaptors.ftp;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.Path;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.Test;

public class FtpInputStreamTest {
    @Test
    public void close_callTwice_completePendingCommandOnlyOnce() throws IOException {
        // Arrange
        InputStream stream = new ByteArrayInputStream(new byte[4]);
        FTPClient ftpClient = mock(FTPClient.class);
        FtpInputStream ftpInputStream = new FtpInputStream(stream, ftpClient, mock(Path.class), mock(FtpFiles.class));

        // Act
        ftpInputStream.close();
        ftpInputStream.close();

        // Assert
        verify(ftpClient).completePendingCommand();
    }

    @Test
    public void close_callOnce_fileSystemIsClosed() throws IOException, XenonException {
        // Arrange
        InputStream stream = new ByteArrayInputStream(new byte[4]);
        FtpFiles ftpFiles = mock(FtpFiles.class);
        FtpInputStream ftpInputStream = new FtpInputStream(stream, mock(FTPClient.class), mock(Path.class), ftpFiles);

        // Act
        ftpInputStream.close();

        // Assert
        verify(ftpFiles).close(null);
    }
}
