package nl.esciencecenter.xenon.adaptors.ftp;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.Test;

public class FtpInputStreamTest {
    @Test
    public void close_callTwice_completePendingCommandOnlyOnce() throws IOException {
        // Arrange
        InputStream stream = new ByteArrayInputStream(new byte[4]);
        FTPClient ftpClient = mock(FTPClient.class);
        FtpInputStream ftpInputStream = new FtpInputStream(stream, ftpClient);

        // Act
        ftpInputStream.close();
        ftpInputStream.close();

        // Assert
        verify(ftpClient).completePendingCommand();
    }
}
