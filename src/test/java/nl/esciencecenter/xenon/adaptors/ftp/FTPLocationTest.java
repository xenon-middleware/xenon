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