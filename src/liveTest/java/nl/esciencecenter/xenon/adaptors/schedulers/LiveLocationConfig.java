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

import static org.junit.Assume.assumeNotNull;

public class LiveLocationConfig extends SchedulerLocationConfig {

    LiveLocationConfig() {
        super(null, null, null, null);
    }

    @Override
    public String getLocation() {
        return System.getProperty("xenon.scheduler.location");
    }

    @Override
    public String getWorkdir() {
        return System.getProperty("xenon.scheduler.workdir");
    }

    @Override
    public String[] getQueueNames() {
        String queuesAsString = System.getProperty("xenon.scheduler.queues");
        // Skip when expected queues are not given
        assumeNotNull(queuesAsString);
        return queuesAsString.split(",");
    }

    @Override
    public String getDefaultQueueName() {
        String name = System.getProperty("xenon.scheduler.queues.default");
        // Skip when expected default queue is not given
        assumeNotNull(name);
        return name;
    }

    @Override
    public boolean supportsInteractive() {
        String value = System.getProperty("xenon.scheduler.supportsInteractive");
        return value != null;
    }

    @Override
    public boolean isEmbedded() {
        String value = System.getProperty("xenon.scheduler.isEmbedded");
        return value != null;
    }
}
