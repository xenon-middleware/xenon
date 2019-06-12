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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.schedulers.JobDescription;

public class SshInteractiveProcessTest {

    @Test(expected = IllegalArgumentException.class)
    public void test_createFailsSessionNull() throws XenonException {

        JobDescription desc = new JobDescription();
        desc.setWorkingDirectory("workdir");
        desc.setExecutable("exec");

        HashMap<String, String> env = new HashMap<>();
        env.put("key1", "value1");
        env.put("key2", "value2");
        desc.setEnvironment(env);
        desc.setArguments(new String[] { "a", "b", "c" });

        new SshInteractiveProcess(null, desc, "JOB-42", 10000L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_createFailsJobNull() throws XenonException {
        MockClientSession session = new MockClientSession(false);
        new SshInteractiveProcess(session, null, "JOB-42", 10000L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_createFailsIDNull() throws XenonException {

        JobDescription desc = new JobDescription();
        desc.setWorkingDirectory("workdir");
        desc.setExecutable("exec");

        HashMap<String, String> env = new HashMap<>();
        env.put("key1", "value1");
        env.put("key2", "value2");
        desc.setEnvironment(env);
        desc.setArguments(new String[] { "a", "b", "c" });

        new SshInteractiveProcess(null, desc, null, 10000L);
    }

    @Test(expected = XenonException.class)
    public void test_createFails() throws XenonException {

        JobDescription desc = new JobDescription();
        desc.setWorkingDirectory("workdir");
        desc.setExecutable("exec");

        HashMap<String, String> env = new HashMap<>();
        env.put("key1", "value1");
        env.put("key2", "value2");
        desc.setEnvironment(env);
        desc.setArguments(new String[] { "a", "b", "c" });

        MockClientSession session = new MockClientSession(false, true);
        new SshInteractiveProcess(session, desc, "JOB-42", 10000L);
    }

    @Test
    public void test_create() throws XenonException {
        JobDescription desc = new JobDescription();
        desc.setWorkingDirectory("workdir");
        desc.setExecutable("exec");

        HashMap<String, String> env = new HashMap<>();
        env.put("key1", "value1");
        env.put("key2", "value2");
        desc.setEnvironment(env);
        desc.setArguments(new String[] { "a", "b", "c" });

        MockClientSession session = new MockClientSession(false);
        SshInteractiveProcess p = new SshInteractiveProcess(session, desc, "JOB-42", 10000L);

        MockChannelExec e = (MockChannelExec) session.exec;

        assertNotNull(e);
        assertEquals("cd 'workdir' && exec 'a' 'b' 'c'", e.command);
        assertEquals(env, e.env);
        assertEquals("JOB-42", p.getStreams().getJobIdentifier());
    }

    @Test
    public void test_create2() throws XenonException {

        JobDescription desc = new JobDescription();
        desc.setExecutable("exec");

        HashMap<String, String> env = new HashMap<>();
        env.put("key1", "value1");
        env.put("key2", "value2");
        desc.setEnvironment(env);
        desc.setArguments(new String[] { "a", "b", "c" });

        MockClientSession session = new MockClientSession(false);
        new SshInteractiveProcess(session, desc, "JOB-42", 10000L);

        MockChannelExec e = (MockChannelExec) session.exec;

        assertNotNull(e);
        assertEquals("exec 'a' 'b' 'c'", e.command);
        assertEquals(env, e.env);
    }

    @Test
    public void test_isDoneFalse() throws XenonException {

        JobDescription desc = new JobDescription();
        desc.setExecutable("exec");

        HashMap<String, String> env = new HashMap<>();
        env.put("key1", "value1");
        env.put("key2", "value2");
        desc.setEnvironment(env);
        desc.setArguments(new String[] { "a", "b", "c" });

        MockClientSession session = new MockClientSession(false);
        SshInteractiveProcess p = new SshInteractiveProcess(session, desc, "JOB-42", 10000L);

        assertFalse(p.isDone());
    }

    @Test
    public void test_isDoneTrue() throws XenonException, IOException {
        JobDescription desc = new JobDescription();
        desc.setExecutable("exec");

        HashMap<String, String> env = new HashMap<>();
        env.put("key1", "value1");
        env.put("key2", "value2");
        desc.setEnvironment(env);
        desc.setArguments(new String[] { "a", "b", "c" });

        MockClientSession session = new MockClientSession(false);
        SshInteractiveProcess p = new SshInteractiveProcess(session, desc, "JOB-42", 10000L);

        session.exec.close();

        assertTrue(p.isDone());
    }

    @Test
    public void test_isDoubleDoneTrue() throws XenonException, IOException {
        JobDescription desc = new JobDescription();
        desc.setExecutable("exec");

        HashMap<String, String> env = new HashMap<>();
        env.put("key1", "value1");
        env.put("key2", "value2");
        desc.setEnvironment(env);
        desc.setArguments(new String[] { "a", "b", "c" });

        MockClientSession session = new MockClientSession(false);
        SshInteractiveProcess p = new SshInteractiveProcess(session, desc, "JOB-42", 10000L);

        assertNotNull(session.exec);

        MockChannelExec e = (MockChannelExec) session.exec;
        e.close();

        p.isDone();

        assertTrue(p.isDone());
    }

    @Test
    public void test_destroy() throws XenonException {
        JobDescription desc = new JobDescription();
        desc.setExecutable("exec");

        HashMap<String, String> env = new HashMap<>();
        env.put("key1", "value1");
        env.put("key2", "value2");
        desc.setEnvironment(env);
        desc.setArguments(new String[] { "a", "b", "c" });

        MockClientSession session = new MockClientSession(false);
        SshInteractiveProcess p = new SshInteractiveProcess(session, desc, "JOB-42", 10000L);
        assertFalse(p.isDone());

        p.destroy();

        assertTrue(p.isDone());
    }

    @Test
    public void test_destroyAfterDone() throws XenonException, IOException {
        JobDescription desc = new JobDescription();
        desc.setExecutable("exec");

        HashMap<String, String> env = new HashMap<>();
        env.put("key1", "value1");
        env.put("key2", "value2");
        desc.setEnvironment(env);
        desc.setArguments(new String[] { "a", "b", "c" });

        MockClientSession session = new MockClientSession(false);
        SshInteractiveProcess p = new SshInteractiveProcess(session, desc, "JOB-42", 10000L);

        assertNotNull(session.exec);
        MockChannelExec e = (MockChannelExec) session.exec;
        e.close();

        p.isDone();

        p.destroy();

        assertTrue(e.isClosed());
    }

    @Test
    public void test_forCoverage() throws XenonException {
        JobDescription desc = new JobDescription();
        desc.setExecutable("exec");

        HashMap<String, String> env = new HashMap<>();
        env.put("key1", "value1");
        env.put("key2", "value2");
        desc.setEnvironment(env);
        desc.setArguments(new String[] { "a", "b", "c" });

        MockClientSession session = new MockClientSession(false);
        SshInteractiveProcess p = new SshInteractiveProcess(session, desc, "JOB-42", 10000L);

        assertNotNull(session.exec);
        MockChannelExec e = (MockChannelExec) session.exec;
        e.closeThrows = true;

        p.isDone();
        p.destroy();
        p.getExitStatus();
    }

}
