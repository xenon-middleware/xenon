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

import static nl.esciencecenter.xenon.adaptors.Utils.buildCredential;
import static nl.esciencecenter.xenon.adaptors.Utils.buildProperties;
import static org.junit.Assume.assumeFalse;

import java.util.Map;

import org.junit.BeforeClass;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.schedulers.Scheduler;

public class SchedulerTest extends SchedulerTestParent {
    @BeforeClass
    static public void skipIfNotRequested() {
        String name = System.getProperty("xenon.scheduler");
        assumeFalse("Ignoring scheduler test, 'xenon.scheduler' system property not set", name == null);
    }

    @Override
    protected SchedulerLocationConfig setupLocationConfig() {
        return new LiveLocationConfig();
    }

    @Override
    public Scheduler setupScheduler(SchedulerLocationConfig config) throws XenonException {
        String name = System.getProperty("xenon.scheduler");
        // String location = System.getProperty("xenon.scheduler.location");
        Credential cred = buildCredential();
        Map<String, String> props = buildProperties(SchedulerAdaptor.ADAPTORS_PREFIX + System.getProperty("xenon.scheduler"));
        return Scheduler.create(name, config.getLocation(), cred, props);
    }
}
