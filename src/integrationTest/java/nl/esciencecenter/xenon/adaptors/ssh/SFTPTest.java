/*
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
package nl.esciencecenter.xenon.adaptors.ssh;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.files.AbstractFileTests;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.jcraft.jsch.ConfigRepository;

@RunWith(Parameterized.class)
public class SFTPTest extends AbstractFileTests {
    @Parameters
    public static Collection<String> getLocations() throws Exception {
        SSHFileTestConfig config = new SSHFileTestConfig(null);
        String locationString = config.getProperty("test.sftp.locations", "");
        if (locationString.trim().isEmpty()) {
            return new ArrayList<>(0);
        }

        String[] locationArray = locationString.split(",");
        Collection<String> locations = new ArrayList<>(locationArray.length);
        for (String location : locationArray) {
            String trimmedLocation = location.trim();
            if (!trimmedLocation.isEmpty()) {
                if (!trimmedLocation.contains("://")) {
                    trimmedLocation = "sftp://" + trimmedLocation;
                }
                locations.add(trimmedLocation);
            }
        }
        return locations;
    }

    private final SshLocation sshLocation;
    
    public SFTPTest(String location) throws InvalidLocationException {
        sshLocation = SshLocation.parse(location, ConfigRepository.nullConfig);
    }

    public String getTestUser() {
        // actual test user: use current user name. 
        return sshLocation.getUser();
    }

    public URI getTestLocation() throws Exception {
        return new URI(sshLocation.toString());
    }

    public Credential getCredentials() throws XenonException {
        return xenon.credentials().getDefaultCredential("ssh");
    }

    // ===
    // Sftp Specific tests here: 
    // ===
}
