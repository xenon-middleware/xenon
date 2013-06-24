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

import nl.esciencecenter.octopus.adaptors.AbstractJobTestParent;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.jobs.Scheduler;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class AltSSHJobsTest extends AbstractJobTestParent {

    @Override
    public String getAdaptorName() {
        return "SSH";
    }
    
    @Override
    public URI getValidURI() throws Exception {
        return new URI("ssh://test@localhost/");
    }

    @Override
    public URI getInvalidLocationURI() throws Exception {
        return new URI("ssh://test@aap/");
    }

    @Override
    public URI getInvalidPathURI() throws Exception {
        return new URI("ssh://test@localhost/aap/noot");
    }
    
//    @Override
//    public URI getURIWrongUser() throws Exception {
//        return new URI("ssh://aap@localhost/");
//    }

    @Override
    public Credential getDefaultCredential() throws Exception {
        return octopus.credentials().getDefaultCredential("ssh");
    }

    @Override
    public Credential getPasswordCredential() throws Exception {
        return octopus.credentials().newPasswordCredential("ssh", new Properties(), "test", "rT127Vim".toCharArray());
    }
        
    @Override
    public boolean supportsCredentials() throws Exception {
        return true;
    }
    
    @Override
    public Credential getInvalidCredential() throws Exception {
        return octopus.credentials().newPasswordCredential("ssh", new Properties(), "test", "aap".toCharArray());
    }

    @Override
    public boolean supportsProperties() throws Exception {
        return false;
    }

    @Override
    public Properties getDefaultProperties() throws Exception {
        return new Properties();
    }

    @Override
    public Properties getUnknownProperties() throws Exception {
        Properties tmp = new Properties();
        tmp.put("ssh.queue.multi.aap", "42");
        return tmp;
    }

    @Override
    public Properties [] getInvalidProperties() throws Exception {

        Properties [] tmp = new Properties[4]; 
        
        tmp[0] = new Properties();
        tmp[0].put("ssh.queue.multi.maxConcurrentJobs", "0");
        
        tmp[1] = new Properties();
        tmp[1].put("ssh.queue.historySize", "-2");
        
        tmp[2] = new Properties();
        tmp[2].put("ssh.queue.pollingDelay", "1");
       
        tmp[3] = new Properties();
        tmp[3].put("ssh.queue.pollingDelay", "100000");
        
        return tmp;
    }

    @Override
    public boolean supportsClose() throws Exception {
        return true;
    }

    @Override
    public Scheduler getDefaultScheduler() throws Exception {
        return jobs.newScheduler(getValidURI(), getDefaultCredential(), getDefaultProperties());
    }

    @Override
    public FileSystem getDefaultFileSystem() throws Exception {
        return files.newFileSystem(new URI("ssh://test@localhost/"), getDefaultCredential(), getDefaultProperties());
    }

    @Override
    public String getInvalidQueueName() throws Exception {
        return "aap";
    }
}
