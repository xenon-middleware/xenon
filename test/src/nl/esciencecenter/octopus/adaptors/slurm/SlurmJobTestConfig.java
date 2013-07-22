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

package nl.esciencecenter.octopus.adaptors.slurm;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Properties;

import nl.esciencecenter.octopus.adaptors.JobTestConfig;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.Scheduler;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * 
 */
public class SlurmJobTestConfig extends JobTestConfig {


    private String username;
    private char[] passwd;

    private URI correctURI;
    private URI correctURIWithPath;
    private URI correctFSURI;

    private URI wrongUserURI;
    private URI wrongLocationURI;
    private URI wrongPathURI;

    private String defaultQueue;
    private String[] queues;

    public SlurmJobTestConfig(String configfile) throws Exception {

        super("slurm");

        if (configfile == null) {
            configfile = System.getProperty("test.slurm.adaptor.config");
        }

        if (configfile == null) {
            configfile = System.getProperty("user.home") + File.separator + "test_slurm.properties";
        }

        Properties p = new Properties();
        p.load(new FileInputStream(configfile));

        username = getPropertyOrFail(p, "test.slurm.user");
        passwd = getPropertyOrFail(p, "test.slurm.password").toCharArray();

        String location = getPropertyOrFail(p, "test.slurm.location");

        String wrongUser = getPropertyOrFail(p, "test.slurm.user.wrong");
        String wrongLocation = getPropertyOrFail(p, "test.slurm.location.wrong");

        defaultQueue = getPropertyOrFail(p, "test.slurm.default.queue");
        String queueList = getPropertyOrFail(p, "test.slurm.queues");
        queues = queueList.split("\\s*,\\s*");

        correctURI = new URI("slurm://" + username + "@" + location);
        correctFSURI = new URI("sftp://" + username + "@" + location);
        correctURIWithPath = new URI("slurm://" + username + "@" + location + "/");
        wrongUserURI = new URI("slurm://" + wrongUser + "@" + location);
        wrongLocationURI = new URI("slurm://" + username + "@" + wrongLocation);
        wrongPathURI = new URI("slurm://" + username + "@" + location + "/aap/noot");
    }

    private String getPropertyOrFail(Properties p, String property) throws Exception {

        String tmp = p.getProperty(property);

        if (tmp == null) {
            throw new Exception("Failed to retrieve property " + property);
        }

        return tmp;
    }
    
    @Override
    public String getAdaptorName() {
        return "slurm";
    }

    @Override
    public Properties getUnknownProperties() throws Exception {
        Properties properties = new Properties();
        
        properties.put("some.key",  "some value");
        
        return properties;
    }

    @Override
    public Properties[] getInvalidProperties() throws Exception {
        return new Properties[0];
    }

    @Override
    public Properties getCorrectProperties() throws Exception {
        return new Properties();
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
    public boolean supportsProperties() throws Exception {
        return true;
    }

    @Override
    public Credential getDefaultCredential(Credentials credentials) throws Exception {
        return credentials.getDefaultCredential("slurm");
    }

    @Override
    public Credential getPasswordCredential(Credentials credentials) throws Exception {
        return credentials.newPasswordCredential("slurm", new Properties(), username, passwd);
    }

    @Override
    public Credential getInvalidCredential(Credentials credentials) throws Exception {
        return credentials.newPasswordCredential("slurm", new Properties(), username, "wrongpassword".toCharArray());
    }

    @Override
    public boolean supportNonDefaultCredential() {
        return false;
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

    public String getDefaultQueueName() {
        return defaultQueue;
    }

    public String[] getQueueNames() {
        return queues;
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
        Properties result = new Properties();
        //result.put("octopus.adaptors.slurm.poll.delay", "100");
        return result;
    }

    @Override
    public boolean supportsStatusAfterDone() {
        return true;
    }

    @Override
    public long getDefaultQueueWaitTimeout() {
        return 5 * 60000;
    }

    @Override
    public long getDefaultShortJobTimeout() {
        return 120000;
    }

    @Override
    public long getDefaultCancelTimeout() {
        return 120000;
    }

    @Override
    public boolean supportsParallelJobs() {
        return true;
    }
    
    @Override
    public boolean supportsEnvironmentVariables() {
        return true;
    }
}
