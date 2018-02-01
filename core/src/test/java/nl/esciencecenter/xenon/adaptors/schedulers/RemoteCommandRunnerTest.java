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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.esciencecenter.xenon.XenonException;

public class RemoteCommandRunnerTest {

    @Test(expected = XenonException.class)
    public void test_fails() throws XenonException {
        MockScheduler s = new MockScheduler(true, null, 1);
        RemoteCommandRunner r = new RemoteCommandRunner(s, "this is input", "/bin/foobar", new String[] { "p1", "p2" });
    }

    @Test
    public void test_success() throws XenonException {
        MockScheduler s = new MockScheduler(false, null, 0);
        RemoteCommandRunner r = new RemoteCommandRunner(s, "this is input", "/bin/foobar", new String[] { "p1", "p2" });
        assertTrue(r.success());
    }

    @Test
    public void test_output_on_stderr() throws XenonException {
        MockScheduler s = new MockScheduler(false, "some error", 0);
        RemoteCommandRunner r = new RemoteCommandRunner(s, "this is input", "/bin/foobar", new String[] { "p1", "p2" });
        assertFalse(r.success());
    }

    @Test
    public void test_exit_code() throws XenonException {
        MockScheduler s = new MockScheduler(false, null, 1);
        RemoteCommandRunner r = new RemoteCommandRunner(s, "this is input", "/bin/foobar", new String[] { "p1", "p2" });
        assertFalse(r.success());
    }
}
