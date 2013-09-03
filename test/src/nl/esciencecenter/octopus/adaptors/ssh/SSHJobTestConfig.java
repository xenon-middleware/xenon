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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import nl.esciencecenter.octopus.adaptors.JobTestConfig;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.Scheduler;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class SSHJobTestConfig extends JobTestConfig {

    private String username;
    private char[] passwd;

    private String scheme = "ssh";
    
    private String correctLocation;
    private String correctLocationWrongUser;
    private String wrongLocation;

    public SSHJobTestConfig(String configfile) throws Exception {

        super("ssh");

        if (configfile == null) {
            configfile = System.getProperty("test.config");
        }

        if (configfile == null) {
            configfile = System.getProperty("user.home") + File.separator + "octopus.test.properties";
        }

        Properties p = new Properties();
        p.load(new FileInputStream(configfile));

        username = getPropertyOrFail(p, "test.ssh.user");
        passwd = getPropertyOrFail(p, "test.ssh.password").toCharArray();

        String location = getPropertyOrFail(p, "test.ssh.location");

        String wrongUser = getPropertyOrFail(p, "test.ssh.user.wrong");
        String wrongLoc = getPropertyOrFail(p, "test.ssh.location.wrong");

        correctLocation = username + "@" + location;
        correctLocationWrongUser =  wrongUser + "@" + location;
        wrongLocation = username + "@" + wrongLoc;
    }

    private String getPropertyOrFail(Properties p, String property) throws Exception {

        String tmp = p.getProperty(property);

        if (tmp == null) {
            throw new Exception("Failed to retrieve property " + property);
        }

        return tmp;
    }

    @Override
    public boolean supportLocation() {
        return true;
    }

    @Override
    public boolean supportUser() {
        return true;
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
        return credentials.newPasswordCredential("ssh", username, passwd, new HashMap<String, String>());
    }

    @Override
    public Credential getInvalidCredential(Credentials credentials) throws Exception {
        return credentials.newPasswordCredential("ssh", username, "wrongpassword".toCharArray(), new HashMap<String, String>());
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
        return jobs.newScheduler("ssh", correctLocation, getDefaultCredential(credentials), getDefaultProperties());
    }

    @Override
    public Path getWorkingDir(Files files, Credentials credentials) throws Exception {
        return files.newFileSystem("sftp", correctLocation, getDefaultCredential(credentials), getDefaultProperties()).getEntryPath();
    }

    @Override
    public String getInvalidQueueName() throws Exception {
        return "aap";
    }

    @Override
    public Map<String, String> getDefaultProperties() throws Exception {
        return null;
    }

    @Override
    public boolean supportsStatusAfterDone() {
        return false;
    }

    @Override
    public long getQueueWaitTime() {
        return 5000;
    }

    @Override
    public long getUpdateTime() {
        return 5000;
    }

    @Override
    public boolean supportsEnvironmentVariables() {
        return false;
    }

    @Override
    public boolean supportsParallelJobs() {
        return false;
    }

    @Override
    public String getScheme() throws Exception {
        return scheme;
    }

    @Override
    public String getCorrectLocation() throws Exception {
        return correctLocation;
    }

    @Override
    public String getWrongLocation() throws Exception {
        return wrongLocation;
    }

    @Override
    public String getCorrectLocationWithUser() throws Exception {
        return correctLocation;
    }

    @Override
    public String getCorrectLocationWithWrongUser() throws Exception {
        return correctLocationWrongUser;
    }

    @Override
    public boolean supportsNullLocation() {
        return false;
    }
}
