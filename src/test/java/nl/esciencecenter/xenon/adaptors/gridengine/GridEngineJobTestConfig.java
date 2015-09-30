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

package nl.esciencecenter.xenon.adaptors.gridengine;

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
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * 
 */
public class GridEngineJobTestConfig extends JobTestConfig {

    private final String username;
    private final char[] passwd;

    private final String scheme;
    private final String fileScheme;
    private final String correctLocation;
    private final String wrongLocation;
    private final String correctLocationWrongUser;
    
    private final String defaultQueue;
    private final String[] queues;

    private final long queueWaitTime;
    private final long updateTime;

    private final String parallelEnvironment;

    public GridEngineJobTestConfig(String configfile) throws Exception {
        super("gridengine", configfile);
        
        scheme = "ge";
        fileScheme = "sftp";
        
        username = getPropertyOrFail("test.gridengine.user");
        passwd = getPropertyOrFail("test.gridengine.password").toCharArray();

        String location = getPropertyOrFail("test.gridengine.location");

        defaultQueue = getPropertyOrFail("test.gridengine.default.queue");
        String queueList = getPropertyOrFail("test.gridengine.queues");
        queues = queueList.split("\\s*,\\s*");

        queueWaitTime = Long.parseLong(getPropertyOrFail("test.gridengine.queue.wait.time"));
        updateTime = Long.parseLong(getPropertyOrFail("test.gridengine.update.time"));

        parallelEnvironment = p.getProperty("test.gridengine.parallel.environment");

        if (location == null || location.isEmpty() || location.equals("/")) { 
            correctLocation = "";
            wrongLocation = "doesnotexists.com";
            correctLocationWrongUser = "incorrect@";
        } else { 
            correctLocation = username + "@" + location;
            wrongLocation = username + "@" + "doesnotexists.com";
            correctLocationWrongUser = "incorrect@" + location;
        }
    }

    @Override
    public String getScheme() throws Exception {
        return "ge";
    }

    @Override
    public String getCorrectLocation() throws Exception {
        return correctLocation; 
    }

    @Override
    public boolean supportLocation() {
        return true;
    }

    @Override
    public String getWrongLocation() throws Exception {
        return wrongLocation;
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
        return credentials.getDefaultCredential("ge");
    }

    @Override
    public Credential getPasswordCredential(Credentials credentials) throws Exception {
        return credentials.newPasswordCredential("ge", username, passwd, new HashMap<String, String>(0));
    }

    @Override
    public Credential getInvalidCredential(Credentials credentials) throws Exception {
        return credentials.newPasswordCredential("ge", username, "wrongpassword".toCharArray(), new HashMap<String, String>(0));
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
        return jobs.newScheduler(scheme, correctLocation, getDefaultCredential(credentials), getDefaultProperties());
    }

    @Override
    public Path getWorkingDir(Files files, Credentials credentials) throws Exception {
        return files.newFileSystem(fileScheme, correctLocation, getDefaultCredential(credentials), null).getEntryPath();
    }

    @Override
    public String getInvalidQueueName() throws Exception {
        return "aap";
    }

    public boolean supportsProperties() throws Exception {
        return true;
    }

    @Override
    public Map<String, String> getDefaultProperties() throws Exception {
        Map<String, String> result = new HashMap<>(3);
        result.put("xenon.adaptors.gridengine.poll.delay", "10");
        result.put("xenon.adaptors.gridengine.accounting.grace.time", String.valueOf(getUpdateTime()));
        return result;
    }

    public Map<String, String> getUnknownProperties() throws Exception {
        Map<String, String> result = new HashMap<>(2);
        result.put("xenon.adaptors.gridengine.unknown.property", "some.value");
        return result;
    }

    public Map<String, String>[] getInvalidProperties() throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, String>[] result = new Map[] {new HashMap<>(2)};
        result[0].put("xenon.adaptors.gridengine.poll.delay", "AAP");
        return result;
    }

    public Map<String, String> getCorrectProperties() throws Exception {
        return getDefaultProperties();
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

    public String getParallelEnvironment() {
        return parallelEnvironment;
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
