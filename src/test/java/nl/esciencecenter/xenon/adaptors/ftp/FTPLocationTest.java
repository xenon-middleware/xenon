package nl.esciencecenter.xenon.adaptors.ftp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FTPLocationTest {

    private static final int DEFAULT_PORT = 21;

    @Test
    public void test_parse_hostOnly() throws Exception {
        // Arrange
        String url = "host";

        // Act
        FtpLocation location = FtpLocation.parse(url);

        // Assert
        assertEquals(DEFAULT_PORT, location.getPort());
    }
}