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
import nl.esciencecenter.octopus.exceptions.OctopusException;
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
    public Credential getDefaultCredential() throws OctopusException {
        return null;
    }

    @Override
    public Credential getPasswordCredential() throws OctopusException {
        return null;
    }
    
    @Override
    public Credential getInvalidCredential() throws OctopusException {
        return null;
    }

    @Override
    public boolean supportsProperties() {
        return true;
    }

    @Override
    public Properties getDefaultProperties() {
        return new Properties();
    }

    @Override
    public Properties getUnknownProperties() throws Exception {
        Properties tmp = new Properties();
        tmp.put("local.queue.multi.aap", "42");
        return tmp;
    }

    @Override
    public Properties [] getInvalidProperties() throws Exception {

        Properties [] tmp = new Properties[4]; 
        
        tmp[0] = new Properties();
        tmp[0].put("local.queue.multi.maxConcurrentJobs", "0");
        
        tmp[1] = new Properties();
        tmp[1].put("local.queue.historySize", "-2");
        
        tmp[2] = new Properties();
        tmp[2].put("local.queue.pollingDelay", "1");
       
        tmp[3] = new Properties();
        tmp[3].put("local.queue.pollingDelay", "100000");
        
        return tmp;
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

    @Override
    public String getAdaptorName() {
        return "local";
    }
}
