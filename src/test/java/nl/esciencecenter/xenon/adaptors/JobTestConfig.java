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

package nl.esciencecenter.xenon.adaptors;

import java.io.FileNotFoundException;
import java.io.IOException;

import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.jobs.Jobs;
import nl.esciencecenter.xenon.jobs.Scheduler;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public abstract class JobTestConfig extends GenericTestConfig {

    protected JobTestConfig(String adaptorName, String configfile) throws FileNotFoundException, IOException {
        super(adaptorName, configfile);
    }

    public boolean supportsClose() {
        return false;
    }

    public String[] getQueueNames() {
        return new String[] { "single", "multi", "unlimited" };
    }

    public String getDefaultQueueName() {
        return "single";
    }

    public boolean supportsEnvironmentVariables() {
        return true;
    }

    public abstract boolean supportsStatusAfterDone();

    public abstract Scheduler getDefaultScheduler(Jobs jobs, Credentials credentials) throws Exception;

    public abstract Path getWorkingDir(Files files, Credentials credentials) throws Exception;

    public abstract String getInvalidQueueName();

    public abstract long getQueueWaitTime();

    public abstract long getUpdateTime();

    public long getJobTimeout(long extra_seconds) {
        return getQueueWaitTime() + getUpdateTime() + 1000*extra_seconds;
    }

    public long getPollDelay() {
        return (getQueueWaitTime() + getUpdateTime()) / 10;
    }

    public abstract boolean supportsParallelJobs();
    
    public abstract boolean supportsNullLocation();

    public abstract boolean targetIsWindows();
    
}
