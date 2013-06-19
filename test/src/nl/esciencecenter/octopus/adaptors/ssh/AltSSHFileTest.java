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

package nl.esciencecenter.octopus.adaptors.ssh;

import java.net.URI;
import java.util.Properties;

import nl.esciencecenter.octopus.adaptors.AbstractFileTest;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.files.FileSystem;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class AltSSHFileTest extends AbstractFileTest {

    @Override
    public URI getCorrectURI() throws Exception {
        return new URI("ssh://test@localhost/");
    }

    @Override
    public URI getURIWrongUser() throws Exception {
        return new URI("ssh://aap@localhost/");
    }

    @Override
    public URI getURIWrongLocation() throws Exception {
        return new URI("ssh://test@aap/");
    }

    @Override
    public URI getURIWrongPath() throws Exception {
        return new URI("ssh://test@localhost/aap/noot");
    }

    @Override
    public URI getCorrectURIWithPath() throws Exception {
        return new URI("ssh://test@localhost/");
    }
    
    @Override
    public boolean supportURIUser() {
        return true;
    }

    @Override
    public boolean supportURILocation() {
        return true;
    }

    @Override
    public Credential getDefaultCredential() throws Exception {
        // Note: default credential is null for local adaptor!
        return octopus.credentials().getDefaultCredential("ssh");
    }

    @Override
    public Credential getNonDefaultCredential() throws Exception {
        throw new Exception("SSH adaptor does not support non-default credentials (FIXME)!");
    }

    @Override
    public boolean supportNonDefaultCredential() {
        // FIXME!
        return false;
    }

    @Override
    public boolean supportNullCredential() {
        return true;
    }
    
    @Override
    public Properties getCorrectProperties() throws Exception {
        throw new Exception("SSH adaptor does not support correct properties (FIXME?)!");
    }

    @Override
    public Properties getIncorrectProperties() throws Exception {
        throw new Exception("Local adaptor does not support incorrect properties (FIXME?)!");
    }

    @Override
    public boolean supportProperties() {
        return false;
    }

    @Override
    public FileSystem getTestFileSystem() throws Exception {
        return files.newFileSystem(new URI("sftp://test@localhost"), getDefaultCredential(), null);
    }

    @Override
    public void closeTestFileSystem(FileSystem fs) throws Exception {
        files.close(fs);
    }

    @Override
    public boolean supportsClose() {
        return true;
    }

    @Override
    public boolean supportsLocalCWDFileSystem() {
        return false;
    }

    @Override
    public boolean supportsLocalHomeFileSystem() {
        return false;
    }
}
