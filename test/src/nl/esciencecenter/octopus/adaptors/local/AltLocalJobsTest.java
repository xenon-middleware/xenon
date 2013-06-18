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
import java.net.URISyntaxException;
import java.util.Properties;

import nl.esciencecenter.octopus.adaptors.AbstractJobTest;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.jobs.Scheduler;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class AltLocalJobsTest extends AbstractJobTest {

    @Override
    public URI getValidURI() throws URISyntaxException {
        return new URI("local:///");
    }

    @Override
    public URI getInvalidLocationURI() throws URISyntaxException {
        return new URI("local://hutsefluts/");
    }

    @Override
    public URI getInvalidPathURI() throws URISyntaxException {
        return new URI("local:///hutsefluts/");
    }
    
    @Override
    public boolean supportsCredentials() {
        return false;
    }

    @Override
    public Credential getDefaultCredential() {
        return null;
    }

    @Override
    public Credential getInvalidCredential() {
        return null;
    }

    @Override
    public boolean supportsProperties() {
        return false;
    }

    @Override
    public Properties getDefaultProperties() {
        return new Properties();
    }

    @Override
    public Properties getUnknownProperties() {
        return null;
    }   

    @Override
    public Properties getInvalidProperties() {
        return null;
    }

    @Override
    public boolean supportsClose() throws Exception {
        return false;
    }

    @Override
    public Scheduler getDefaultScheduler() throws Exception {
        return jobs.getLocalScheduler();
    }

    @Override
    public String getInvalidQueueName() throws Exception {
        return "aap";
    }

    @Override
    public FileSystem getDefaultFileSystem() throws Exception {
        return files.getLocalCWDFileSystem();
    }
}
