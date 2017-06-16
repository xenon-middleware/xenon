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

package nl.esciencecenter.xenon.adaptors.job.torque;

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
public class TorqueJobTestConfig extends JobTestConfig {

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

    public TorqueJobTestConfig(String configfile) throws Exception {

        super("torque", configfile);
        
        scheme = "torque";
        fileScheme = "sftp";
        
        username = getPropertyOrFail("test.torque.user");
        passwd = getPropertyOrFail("test.torque.password").toCharArray();

        String location = getPropertyOrFail("test.torque.location");

        defaultQueue = getPropertyOrFail("test.torque.default.queue");
        String queueList = getPropertyOrFail("test.torque.queues");
        queues = queueList.split("\\s*,\\s*");

        queueWaitTime = Long.parseLong(getPropertyOrFail("test.torque.queue.wait.time"));
        updateTime = Long.parseLong(getPropertyOrFail("test.torque.update.time"));

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
        return "torque";
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
        return jobs.newScheduler(scheme, correctLocation, getDefaultCredential(), getDefaultProperties());
    }

    @Override
    public Path getWorkingDir(Files files) throws Exception {
        return files.newFileSystem(fileScheme, correctLocation, getDefaultCredential(), null).getEntryPath();
    }

    @Override
    public String getInvalidQueueName() {
        return "aap";
    }

    public boolean supportsProperties() throws Exception {
        return true;
    }

    @Override
    public Map<String, String> getDefaultProperties() throws Exception {
        Map<String, String> result = new HashMap<>(2);
        result.put("xenon.adaptors.torque.poll.delay", "100");
        return result;
    }

    public Map<String, String> getUnknownProperties() throws Exception {
        Map<String, String> result = new HashMap<>(2);
        result.put("xenon.adaptors.torque.unknown.property", "some.value");
        return result;
    }

    public Map<String, String>[] getInvalidProperties() throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, String>[] result = new Map[] { new HashMap<>(2) };
        result[0].put("xenon.adaptors.torque.poll.delay", "AAP");
        return result;
    }

    public Map<String, String> getCorrectProperties() throws Exception {
        Map<String, String> result = new HashMap<>(2);
        result.put("xenon.adaptors.torque.poll.delay", "100");
        return result;
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
