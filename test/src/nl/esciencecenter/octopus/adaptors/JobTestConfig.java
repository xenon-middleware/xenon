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

package nl.esciencecenter.octopus.adaptors;

import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.Scheduler;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public abstract class JobTestConfig extends GenericTestConfig {
  
    protected JobTestConfig(String adaptorName) { 
        super(adaptorName);
    }
    
    public boolean supportsClose() throws Exception { 
        return false;
    }

    public String[] getQueueNames() {
        return new String [] { "single", "multi", "unlimited" };
    }
    
    public String getDefaultQueueName() {
        return "single";
    }

    public abstract boolean supportsStatusAfterDone(); 
    
    public abstract Scheduler getDefaultScheduler(Jobs jobs, Credentials credentials) throws Exception;
    public abstract FileSystem getDefaultFileSystem(Files files, Credentials credentials) throws Exception;
    public abstract String getInvalidQueueName() throws Exception;

    public abstract long getDefaultQueueWaitTimeout();
    public abstract long getDefaultShortJobTimeout();
    public abstract long getDefaultCancelTimeout();

}
