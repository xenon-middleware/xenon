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
package nl.esciencecenter.xenon.adaptors.job.slurm;

import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.adaptors.JobTestConfig;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.jobs.Jobs;
import nl.esciencecenter.xenon.jobs.Scheduler;

/**
 * 
 */
public class SlurmJobTestConfig extends JobTestConfig {

    private final String username;
    private final char[] passwd;

    private final static String scheme = "slurm";
    private final String correctLocation;
    private final String wrongLocation;
    private final String correctLocationWrongUser;
    
    private final String defaultQueue;
    private final String[] queues;

    private final long queueWaitTime;
    private final long updateTime;

    public SlurmJobTestConfig(String configfile) throws Exception {

        super("slurm", configfile);

        username = getPropertyOrFail("test.slurm.user");
        passwd = getPropertyOrFail("test.slurm.password").toCharArray();

        String location = getPropertyOrFail("test.slurm.location");

        defaultQueue = getPropertyOrFail("test.slurm.default.queue");
        String queueList = getPropertyOrFail("test.slurm.queues");
        queues = queueList.split("\\s*,\\s*");

        queueWaitTime = Long.parseLong(getPropertyOrFail("test.slurm.queue.wait.time"));
        updateTime = Long.parseLong(getPropertyOrFail("test.slurm.update.time"));

        if (location == null || location.isEmpty() || location.equals("/")) { 
            correctLocation = "";
            wrongLocation = "doesnotexists.com";
            correctLocationWrongUser = "incorrect@/";
        } else { 
            correctLocation = username + "@" + location;
            wrongLocation = username + "@" + "doesnotexists.com";
            correctLocationWrongUser = "incorrect@" + location;
        }
    }

    @Override
    public String getAdaptorName() {
        return "slurm";
    }

    @Override
    public Map<String, String> getUnknownProperties() throws Exception {
        Map<String, String> properties = new HashMap<>(2);
        properties.put("some.key", "some value");

        return properties;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String>[] getInvalidProperties() throws Exception {
        return new Map[0];
    }

    @Override
    public Map<String, String> getCorrectProperties() throws Exception {
        return new HashMap<>(0);
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
    public boolean supportsProperties() throws Exception {
        return true;
    }

    @Override
    public Credential getDefaultCredential() throws Exception {
        return new DefaultCredential();
    }

    @Override
    public Credential getPasswordCredential() throws Exception {
        return new PasswordCredential(username, passwd);
    }

    @Override
    public Credential getInvalidCredential() throws Exception {
        return new PasswordCredential(username, "wrongpassword".toCharArray());
    }

    @Override
    public boolean supportNonDefaultCredential() {
        return false;
    }

    @Override
    public Credential getNonDefaultCredential() throws Exception {
        return getPasswordCredential();
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
    public Scheduler getDefaultScheduler(Jobs jobs) throws Exception {
        return jobs.newScheduler("slurm", correctLocation, getDefaultCredential(), getDefaultProperties());
    }

    @Override
    public Path getWorkingDir(Files files) throws Exception {
        return files.newFileSystem("sftp", correctLocation, getDefaultCredential(), null).getEntryPath();
    }

    @Override
    public String getInvalidQueueName() {
        return "aap";
    }

    @Override
    public Map<String, String> getDefaultProperties() throws Exception {
        return new HashMap<>(0);
    }

    @Override
    public boolean supportsStatusAfterDone() {
        return true;
    }

    @Override
    public long getQueueWaitTime() {
        return queueWaitTime;
    }

    @Override
    public long getUpdateTime() {
        return updateTime;
    }

    @Override
    public boolean supportsParallelJobs() {
        return true;
    }

    @Override
    public boolean supportsEnvironmentVariables() {
        return true;
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
        return true;
    }
    
    @Override
    public boolean targetIsWindows() {
        return false;
    }
}
