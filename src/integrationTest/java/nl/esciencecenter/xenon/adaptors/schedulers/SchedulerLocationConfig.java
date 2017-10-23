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
package nl.esciencecenter.xenon.adaptors.schedulers;

public abstract class SchedulerLocationConfig {

    protected String location;
    protected String workdir;

    protected String[] queueNames;
    protected String defaultQueue;

    public SchedulerLocationConfig(String location, String workdir, String[] queueNames, String defaultQueue) {
        this.location = location;
        this.workdir = workdir;
        this.queueNames = queueNames;
        this.defaultQueue = defaultQueue;
    }

    public String getWorkdir() {
        return workdir;
    }

    public String getLocation() {
        return location;
    }

    public String[] getQueueNames() {
        return queueNames;
    }

    public String getDefaultQueueName() {
        return defaultQueue;
    }

    public long getMaxWaitUntilRunning() {
        return 10 * 1000;
    }

    public long getMaxWaintUntilDone() {
        return 60 * 1000;
    }

    public boolean supportsBatch() {
        return true;
    }

    public boolean supportsInteractive() {
        return false;
    }

    public boolean isEmbedded() {
        return false;
    }

    // public abstract Map.Entry<Path,Path> getSymbolicLinksToExistingFile();
}
