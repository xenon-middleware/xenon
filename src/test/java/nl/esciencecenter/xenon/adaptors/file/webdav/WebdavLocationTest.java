/**
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.esciencecenter.xenon.adaptors.file.webdav;

import static org.junit.Assert.assertEquals;
import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.adaptors.file.webdav.WebdavLocation;

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
/*
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
*/
}
