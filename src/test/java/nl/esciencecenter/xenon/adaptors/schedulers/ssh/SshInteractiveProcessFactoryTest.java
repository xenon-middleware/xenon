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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.SchedulerClosedException;
import nl.esciencecenter.xenon.schedulers.JobDescription;

public class SshInteractiveProcessFactoryTest {


    @Test(expected=IllegalArgumentException.class)
    public void test_creatNullFails() throws XenonException {
        new SshInteractiveProcessFactory(null);
    }

    @Test
    public void test_isOpen() throws XenonException {
        MockClientSession session = new MockClientSession(false);
        SshInteractiveProcessFactory p = new SshInteractiveProcessFactory(session);
        assertTrue(p.isOpen());
    }

    @Test
    public void test_close() throws XenonException {
        MockClientSession session = new MockClientSession(false);
        SshInteractiveProcessFactory p = new SshInteractiveProcessFactory(session);
        p.close();
        assertFalse(p.isOpen());
    }

    @Test(expected=SchedulerClosedException.class)
    public void test_doublecloseFails() throws XenonException {
        MockClientSession session = new MockClientSession(false);
        SshInteractiveProcessFactory p = new SshInteractiveProcessFactory(session);
        p.close();
        p.close();
    }

    @Test(expected=XenonException.class)
    public void test_closeSessionFails() throws XenonException {
        MockClientSession session = new MockClientSession(true);
        SshInteractiveProcessFactory p = new SshInteractiveProcessFactory(session);
        p.close();
    }

    @Test(expected=SchedulerClosedException.class)
    public void test_createProcessFailsClosed() throws XenonException {
        MockClientSession session = new MockClientSession(false);
        SshInteractiveProcessFactory p = new SshInteractiveProcessFactory(session);
        p.close();

        JobDescription desc = new JobDescription();
        desc.setWorkingDirectory("workdir");
        desc.setExecutable("exec");

        p.createInteractiveProcess(desc, "JOB-42");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_createProcessFailsNullDescription() throws XenonException {
        MockClientSession session = new MockClientSession(false);
        SshInteractiveProcessFactory p = new SshInteractiveProcessFactory(session);
        p.createInteractiveProcess(null, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_createProcessFailsNullID() throws XenonException {
        MockClientSession session = new MockClientSession(false);
        SshInteractiveProcessFactory p = new SshInteractiveProcessFactory(session);
        JobDescription desc = new JobDescription();
        desc.setWorkingDirectory("workdir");
        desc.setExecutable("exec");
        p.createInteractiveProcess(desc, null);
    }

    @Test
    public void test_createProcess() throws XenonException {
        MockClientSession session = new MockClientSession(false);
        SshInteractiveProcessFactory p = new SshInteractiveProcessFactory(session);

        JobDescription desc = new JobDescription();
        desc.setWorkingDirectory("workdir");
        desc.setExecutable("exec");

        HashMap<String,String> env = new HashMap<>();
        env.put("key1", "value1");
        env.put("key2", "value2");
        desc.setEnvironment(env);
        desc.setArguments(new String [] { "a", "b", "c" });
        p.createInteractiveProcess(desc, "JOB-42");

        MockChannelExec e = (MockChannelExec) session.exec;

        assertNotNull(e);
        assertEquals("cd 'workdir' && exec 'a' 'b' 'c'", e.command);
        assertEquals(env, e.env);
    }

}
