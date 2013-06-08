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

package nl.esciencecenter.octopus.adaptors.local;

import java.net.URI;
import java.util.Properties;

import nl.esciencecenter.octopus.adaptors.AbstractFileTest;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.files.FileSystem;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class AltLocalFileTest extends AbstractFileTest {

    @Override
    public URI getCorrectURI() throws Exception {
        return new URI("file:///");
    }

    @Override
    public URI getURIWrongUser() throws Exception {
        throw new Exception("Local adaptor does not support URI with user!");
    }

    @Override
    public URI getURIWrongLocation() throws Exception {
        throw new Exception("Local adaptor does not support URI with location!");
    }

    @Override
    public URI getURIWrongPath() throws Exception {
        return new URI("file://aap/noot");
    }

    @Override
    public boolean supportURIUser() {
        return false;
    }

    @Override
    public boolean supportURILocation() {
        return false;
    }

    @Override
    public Credential getDefaultCredential() throws Exception {
        // Note: default credential is null for local adaptor!
        return null;
    }

    @Override
    public Credential getNonDefaultCredential() throws Exception {
        throw new Exception("Local adaptor does not support non-default credentials!");
    }

    @Override
    public boolean supportNonDefaultCredential() {
        return false;
    }

    @Override
    public boolean supportNullCredential() {
        return true;
    }
    
    @Override
    public Properties getCorrectProperties() throws Exception {
        throw new Exception("Local adaptor does not support correct properties!");
    }

    @Override
    public Properties getIncorrectProperties() throws Exception {
        throw new Exception("Local adaptor does not support incorrect properties!");
    }

    @Override
    public boolean supportProperties() {
        return false;
    }

    @Override
    public FileSystem getTestFileSystem() throws Exception {
        return files.getLocalCWDFileSystem();
    }

    @Override
    public void closeTestFileSystem(FileSystem fs) throws Exception {
        // ignored
    }

    @Override
    public boolean supportsClose() {
        return false;
    }

}
