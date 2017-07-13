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
package nl.esciencecenter.xenon.adaptors.schedulers.ssh;

import nl.esciencecenter.xenon.schedulers.JobHandle;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.Scheduler;

public class MockJobHandle implements JobHandle {
    private final String identifier;
    private final JobDescription desc;

    public MockJobHandle(String identifier, JobDescription desc) {
        this.identifier = identifier;
        this.desc = desc;
    }

    @Override
    public JobDescription getJobDescription() {
        return desc;
    }

    @Override
    public Scheduler getScheduler() {
        return null;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }
}