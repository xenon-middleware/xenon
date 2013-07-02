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

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import nl.esciencecenter.octopus.adaptors.GenericJobAdaptorTestParent;
import nl.esciencecenter.octopus.adaptors.JobTestConfig;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.Scheduler;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class SSHJobTestConfig extends JobTestConfig {

    private String username;
    private char [] passwd;
    
    private URI correctURI;
    private URI correctURIWithPath;
    private URI correctFSURI;
    
    private URI wrongUserURI;
    private URI wrongLocationURI;
    private URI wrongPathURI;
    
    public SSHJobTestConfig(String configfile) throws Exception { 
        
        super("ssh");
        
        if (configfile == null) { 
            configfile = System.getProperty("test.ssh.adaptor.config");
        }
            
        if (configfile == null) { 
            configfile = System.getProperty("user.home") + File.separator + "test_ssh.properties";
        }
        
        Properties p = new Properties();
        p.load(new FileInputStream(configfile));
        
        username = getPropertyOrFail(p, "test.ssh.user");
        passwd = getPropertyOrFail(p, "test.ssh.password").toCharArray();
        
        String location = getPropertyOrFail(p, "test.ssh.location");
        
        String wrongUser = getPropertyOrFail(p, "test.ssh.user.wrong");
        String wrongLocation = getPropertyOrFail(p, "test.ssh.location.wrong");
        
        correctURI = new URI("ssh://" + username + "@" + location);
        correctFSURI = new URI("sftp://" + username + "@" + location);
        correctURIWithPath = new URI("ssh://" + username + "@" + location + "/");
        wrongUserURI = new URI("ssh://" + wrongUser + "@" + location);
        wrongLocationURI = new URI("ssh://" + username + "@" + wrongLocation);
        wrongPathURI = new URI("ssh://" + username + "@" + location + "/aap/noot");
    }
    
    private String getPropertyOrFail(Properties p, String property) throws Exception { 
        
        String tmp = p.getProperty(property);
        
        if (tmp == null) { 
            throw new Exception("Failed to retrieve property " + property);
        }
        
        return tmp;
    }
    
    @Override
    public URI getCorrectURI() throws Exception {
        return correctURI;
    }
    @Override
    public URI getCorrectURIWithPath() throws Exception {
        return correctURIWithPath;
    }
    
    @Override
    public boolean supportURILocation() {
        return true;
    }
    
    @Override
    public URI getURIWrongLocation() throws Exception {
        return wrongLocationURI;
    }
    
    @Override
    public URI getURIWrongPath() throws Exception {
        return wrongPathURI;
    }

    @Override
    public boolean supportURIUser() {
        return true;
    }

    @Override
    public URI getURIWrongUser() throws Exception {
        return wrongUserURI;
    }

    @Override
    public boolean supportsCredentials() {
        return true;
    }
    
    @Override
    public Credential getDefaultCredential(Credentials credentials) throws Exception {
        return credentials.getDefaultCredential("ssh");
    }

    @Override
    public Credential getPasswordCredential(Credentials credentials) throws Exception {
        return credentials.newPasswordCredential("ssh", new Properties(), username, passwd);
    }
    
    @Override
    public Credential getInvalidCredential(Credentials credentials) throws Exception {
        return credentials.newPasswordCredential("ssh", new Properties(), username, "wrongpassword".toCharArray());
    }

    @Override
    public boolean supportNonDefaultCredential() {
        return true;
    }

    @Override
    public Credential getNonDefaultCredential(Credentials credentials) throws Exception {
        return getPasswordCredential(credentials);
    }

    @Override
    public boolean supportNullCredential() {
        return true;
    }

    @Override
    public boolean supportsClose() {
        return true;
    }

    @Override
    public Scheduler getDefaultScheduler(Jobs jobs, Credentials credentials) throws Exception {
        return jobs.newScheduler(correctURI, getDefaultCredential(credentials), getDefaultProperties());
    }

    @Override
    public FileSystem getDefaultFileSystem(Files files, Credentials credentials) throws Exception {
        return files.newFileSystem(correctFSURI, getDefaultCredential(credentials), getDefaultProperties());
    }

    @Override
    public String getInvalidQueueName() throws Exception {
        return "aap";
    }

    @Override
    public Properties getDefaultProperties() throws Exception {
        return null;
    }

    @Override
    public boolean supportsStatusAfterDone() {
        return false;
    }

    @Override
    public long getDefaultQueueWaitTimeout() {
        return 1000;
    }

    @Override
    public long getDefaultShortJobTimeout() {
        return 1000;
    }

    @Override
    public long getDefaultCancelTimeout() {
        return 1000;
    }
}
