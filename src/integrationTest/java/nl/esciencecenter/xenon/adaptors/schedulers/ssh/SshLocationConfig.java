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
package nl.esciencecenter.xenon.adaptors.schedulers.ssh;

import nl.esciencecenter.xenon.adaptors.schedulers.SchedulerLocationConfig;

public class SshLocationConfig extends SchedulerLocationConfig {

    private String location;

    public SshLocationConfig(String location) {
        this.location = location;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public String[] getQueueNames() {
        return new String [] { "single", "multi", "unlimited" } ;
    }

    @Override
    public String getDefaultQueueName() {
        return "single";
    }

    @Override
    public boolean supportsInteractive() {
        return true;
    }

    @Override
    public boolean isEmbedded() {
        return true;
    }
}
