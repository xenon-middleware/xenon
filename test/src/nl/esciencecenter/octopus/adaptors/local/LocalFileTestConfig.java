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
import java.util.Map;

import nl.esciencecenter.octopus.adaptors.FileTestConfig;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class LocalFileTestConfig extends FileTestConfig {

    private final URI correctURI;
    private final URI correctURIWithPath;
    private final URI wrongPathURI;
    private final URI wrongLocationURI;

    public LocalFileTestConfig() throws Exception {
        super("local");

        correctURI = new URI("file:///");
        correctURIWithPath = new URI("file:////");
        wrongPathURI = new URI("file:///aap/noot/mies/");
        wrongLocationURI = new URI("file://machine/");
    }

    @Override
    public FileSystem getTestFileSystem(Files files, Credentials credentials) throws Exception {
        return files.getLocalCWDFileSystem();
    }

    @Override
    public void closeTestFileSystem(Files files, FileSystem fs) throws Exception {
        // ignore
    }

    @Override
    public URI getCorrectURI() throws Exception {
        return correctURI;
    }

    @Override
    public URI getCorrectURIWithPath() throws Exception {
        return correctURI;
    }

    @Override
    public URI getURIWrongPath() throws Exception {
        return wrongPathURI;
    }

    public boolean supportURILocation() {
        return true;
    }

    public URI getURIWrongLocation() throws Exception {
        return wrongLocationURI;
    }

    @Override
    public Credential getDefaultCredential(Credentials c) throws Exception {
        return null;
    }

    @Override
    public Map<String,String> getDefaultProperties() throws Exception {
        return null;
    }

    public boolean supportNullCredential() {
        return true;
    }
}
