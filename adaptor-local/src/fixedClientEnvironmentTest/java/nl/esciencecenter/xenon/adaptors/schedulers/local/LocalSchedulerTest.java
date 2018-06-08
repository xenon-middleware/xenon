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
package nl.esciencecenter.xenon.adaptors.schedulers.local;

import java.util.HashMap;

import org.junit.Test;

import nl.esciencecenter.xenon.InvalidCredentialException;
import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.UnknownPropertyException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.SchedulerLocationConfig;
import nl.esciencecenter.xenon.adaptors.schedulers.SchedulerTestParent;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.schedulers.Scheduler;

public class LocalSchedulerTest extends SchedulerTestParent {

    @Override
    protected SchedulerLocationConfig setupLocationConfig() {

        return new SchedulerLocationConfig("", "/tmp", new String[] { "single", "multi", "unlimited" }, "single") {

            @Override
            public boolean supportsInteractive() {
                return true;
            }

            @Override
            public boolean isEmbedded() {
                return true;
            }
        };
    }

    @Override
    public Scheduler setupScheduler(SchedulerLocationConfig config) throws XenonException {
        return Scheduler.create("local", config.getLocation());
    }

    @Test
    public void test_location_null() throws XenonException {
        Scheduler.create("local", null);
    }

    @Test
    public void test_location_empty() throws XenonException {
        Scheduler.create("local", "");
    }

    // @Test
    // public void test_location_url() throws XenonException {
    // Scheduler.create("local", "local://");
    // }

    @Test(expected = InvalidLocationException.class)
    public void test_location_wrong() throws XenonException {
        Scheduler.create("local", "foobar");
    }

    @Test
    public void test_credential_null() throws XenonException {
        Scheduler.create("local", "", null);
    }

    @Test
    public void test_credential_default() throws XenonException {
        Scheduler.create("local", "", new DefaultCredential());
    }

    @Test(expected = InvalidCredentialException.class)
    public void test_credential_wrong() throws XenonException {
        Scheduler.create("local", "", new PasswordCredential("user", "password".toCharArray()));
    }

    @Test
    public void test_properties_null() throws XenonException {
        Scheduler.create("local", "", null, null);
    }

    @Test
    public void test_properties_empty() throws XenonException {
        Scheduler.create("local", "", null, new HashMap<String, String>());
    }

    @Test(expected = UnknownPropertyException.class)
    public void test_properties_unknownProperty_throwsException() throws XenonException {
        HashMap<String, String> map = new HashMap<>();
        map.put("key", "value");
        Scheduler.create("local", "", null, map);
    }
}
