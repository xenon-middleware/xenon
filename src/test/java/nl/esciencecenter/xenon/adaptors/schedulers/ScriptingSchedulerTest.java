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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.schedulers.NoSuchQueueException;
import nl.esciencecenter.xenon.schedulers.QueueStatus;

public class ScriptingSchedulerTest {

    @Test
    public void test_translateError() throws XenonException {

        MockScriptingScheduler ss = new MockScriptingScheduler();

        MockScheduler s = new MockScheduler(false, "some error", 1);
        RemoteCommandRunner r = new RemoteCommandRunner(s, "this is input", "/bin/foobar", new String[] { "p1", "p2" });

        try {
            ss.translateError(r, "this is input", "/bin/foobar", new String[] { "p1", "p2" });
        } catch (XenonException e) {
            String message = "TEST adaptor: could not run command \"/bin/foobar\" with stdin \"this is input\" arguments \"[p1, p2]\" using scheduler \"local\". Exit code = 1 Output: Hello stdout Error output: Hello stderr";
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void test_getQueueStatusses_noSuchQueue() throws XenonException {

        MockScriptingScheduler ss = new MockScriptingScheduler();

        Map<String, Map<String, String>> map = new HashMap<>();

        QueueStatus[] s = ss.getQueueStatusses(map, "queue");

        assertTrue(s[0].hasException());
        assertTrue(s[0].getException() instanceof NoSuchQueueException);

    }

}
