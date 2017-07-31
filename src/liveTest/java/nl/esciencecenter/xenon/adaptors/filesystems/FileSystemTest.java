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
package nl.esciencecenter.xenon.adaptors.filesystems;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.CertificateCredential;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;

public class FileSystemTest extends FileSystemTestParent {
    @Override
    protected LocationConfig setupLocationConfig(FileSystem fileSystem) {
        return new LiveLocationConfig(fileSystem);
    }

    @Override
    public FileSystem setupFileSystem() throws XenonException {
        String name = System.getProperty("xenon.filesystem");
        org.junit.Assume.assumeFalse("Ignoring filesystem test, 'xenon.filesystem' system property not set", name == null);
        String location = System.getProperty("xenon.location");
        assertNotNull("liveTest expects 'xenon.location' system property", location);
        Credential cred = buildCredential();
        Map<String, String> props = buildProperties();
        return FileSystem.create(name, location, cred, props);
    }

    private Map<String,String> buildProperties() {
        Map<String,String> properties = new HashMap<>();
        String prefix = FileAdaptor.ADAPTORS_PREFIX + System.getProperty("xenon.filesystem");
        for (String propName : System.getProperties().stringPropertyNames()) {
            if (propName.startsWith(prefix)) {
                properties.put(propName, System.getProperty(propName));
            }
        }
        return properties;
    }

    private Credential buildCredential() {
        String username = System.getProperty("xenon.username");
        assertNotNull("liveTest expects 'xenon.username' system property", username);
        String password = System.getProperty("xenon.password");
        if (password != null) {
            return new PasswordCredential(username, password.toCharArray());
        }
        String certfile = System.getProperty("xenon.certfile");
        if (certfile != null) {
            String passphrase = System.getProperty("xenon.passphrase");
            if (passphrase == null) {
                return new CertificateCredential(username, certfile, new char[0]);
            }
            return new CertificateCredential(username, certfile, passphrase.toCharArray());
        }
        return new DefaultCredential(username);
    }
}
