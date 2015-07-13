package nl.esciencecenter.xenon.adaptors.webdav;

import static org.junit.Assert.assertEquals;
import nl.esciencecenter.xenon.InvalidLocationException;

import org.junit.Test;

public class WebdavLocationTest {
    private int DEFAULT_PORT = 80;

    @Test
    public void parse_noPortInLocation_useDefaultPort() throws InvalidLocationException {
        // Arrange
        String url = "localhost";

        // Act
        WebdavLocation location = WebdavLocation.parse(url);

        // Assert
        assertEquals(DEFAULT_PORT, location.getPort());
    }

    @Test
    public void parse_locationWithPath_correctPath() throws InvalidLocationException {
        // Arrange
        String url = "http://domain:80/path";

        // Act
        WebdavLocation location = WebdavLocation.parse(url);

        // Assert
        assertEquals("/path", location.getPath());
    }

    @Test
    public void toString_locationWithPath_correctPath() throws InvalidLocationException {
        // Arrange
        String url = "http://domain:80/path";

        // Act
        WebdavLocation location = WebdavLocation.parse(url);

        // Assert
        assertEquals("http://domain:80/path", location.toString());
    }

    @Test
    public void toString_locationWithoutSchemeAndScheme_correctScheme() throws InvalidLocationException {
        // Arrange
        String url = "domain:80/path";
        String scheme = "http";

        // Act
        WebdavLocation location = WebdavLocation.parse(url, scheme);

        // Assert
        assertEquals("http://domain:80/path", location.toString());
    }

    @Test
    public void toString_locationConflictingSchemes_useUrlScheme() throws InvalidLocationException {
        // Arrange
        String url = "https://domain:80/path";
        String scheme = "http";

        // Act
        WebdavLocation location = WebdavLocation.parse(url, scheme);

        // Assert
        assertEquals("https://domain:80/path", location.toString());
    }
}
