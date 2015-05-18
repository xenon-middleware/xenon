package nl.esciencecenter.xenon.adaptors.ftp;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.Test;

public class FtpOutputStreamTest {
    @Test
    public void close_callTwice_completePendingCommandOnlyOnce() throws IOException {
        // Arrange
        OutputStream stream = new ByteArrayOutputStream();
        FTPClient ftpClient = mock(FTPClient.class);
        FtpOutputStream ftpOutputStream = new FtpOutputStream(stream, ftpClient);

        // Act
        ftpOutputStream.close();
        ftpOutputStream.close();

        // Assert
        verify(ftpClient).completePendingCommand();
    }
}
