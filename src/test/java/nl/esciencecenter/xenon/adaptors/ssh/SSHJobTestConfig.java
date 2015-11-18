/**
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
package nl.esciencecenter.xenon.adaptors.ssh;

import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.adaptors.JobTestConfig;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.jobs.Jobs;
import nl.esciencecenter.xenon.jobs.Scheduler;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class SSHJobTestConfig extends JobTestConfig {

    private final String username;
    private final char[] passwd;

    private final static String scheme = "ssh";
    
    private final String correctLocation;
    private final String correctLocationWrongUser;
    private final String wrongLocation;

    public SSHJobTestConfig(String configfile) throws Exception {
        super("ssh", configfile);

        String location = getPropertyOrFail("test.ssh.location");
           
        username = getPropertyOrFail("test.ssh.user");
        passwd = getPropertyOrFail("test.ssh.password").toCharArray();

        correctLocation = username + "@" + location;
        correctLocationWrongUser =  "incorrect@" + location;
        wrongLocation = username + "@doesnotexist.com";
    }

    @Override
    public boolean supportLocation() {
        return true;
    }

    @Override
    public boolean supportUserInUri() {
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
        return credentials.newPasswordCredential("ssh", username, passwd, new HashMap<String, String>(0));
    }

    @Override
    public Credential getInvalidCredential(Credentials credentials) throws Exception {
        return credentials.newPasswordCredential("ssh", username, "wrongpassword".toCharArray(), new HashMap<String, String>(0));
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
    public String getInvalidQueueName() {
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

    @Override
    public boolean targetIsWindows() {
        return false;
    }
}
