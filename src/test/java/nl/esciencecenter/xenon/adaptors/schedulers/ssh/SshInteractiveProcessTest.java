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
import nl.esciencecenter.xenon.adaptors.schedulers.JobImplementation;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.Scheduler;
import nl.esciencecenter.xenon.schedulers.MockScheduler;

public class SshInteractiveProcessTest {

	@Test(expected = IllegalArgumentException.class)
	public void test_createFailsSessionNull() throws XenonException {
		
		Scheduler s = new MockScheduler("ID0", "TEST", "MEM", true, true, false, null);
		
		JobDescription desc = new JobDescription();
		desc.setWorkingDirectory("workdir");
		desc.setExecutable("exec");
		
		HashMap<String,String> env = new HashMap<>();
		env.put("key1", "value1");
		env.put("key2", "value2");
		desc.setEnvironment(env);
		desc.setArguments(new String [] { "a", "b", "c" });
		
		JobImplementation j = new JobImplementation(s, "aap", desc);

		new SshInteractiveProcess(null, j);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test_createFailsJobNull() throws XenonException {
		MockClientSession session = new MockClientSession(false);
		new SshInteractiveProcess(session, null);
	}
	
	@Test(expected=XenonException.class)
	public void test_createFails() throws XenonException {
		Scheduler s = new MockScheduler("ID0", "TEST", "MEM", true, true, false, null);
		
		JobDescription desc = new JobDescription();
		desc.setWorkingDirectory("workdir");
		desc.setExecutable("exec");
		
		HashMap<String,String> env = new HashMap<>();
		env.put("key1", "value1");
		env.put("key2", "value2");
		desc.setEnvironment(env);
		desc.setArguments(new String [] { "a", "b", "c" });
		
		JobImplementation j = new JobImplementation(s, "aap", desc);
		
		MockClientSession session = new MockClientSession(false, true);
		new SshInteractiveProcess(session, j);
	}
	
	@Test
	public void test_create() throws XenonException {
		Scheduler s = new MockScheduler("ID0", "TEST", "MEM", true, true, false, null);
		
		JobDescription desc = new JobDescription();
		desc.setWorkingDirectory("workdir");
		desc.setExecutable("exec");
		
		HashMap<String,String> env = new HashMap<>();
		env.put("key1", "value1");
		env.put("key2", "value2");
		desc.setEnvironment(env);
		desc.setArguments(new String [] { "a", "b", "c" });
		
		JobImplementation j = new JobImplementation(s, "aap", desc);
		
		MockClientSession session = new MockClientSession(false);
		SshInteractiveProcess p = new SshInteractiveProcess(session, j);
		
		MockChannelExec e = (MockChannelExec) session.exec;
		
		assertNotNull(e);
		assertEquals("cd 'workdir' && exec 'a' 'b' 'c'", e.command);
		assertEquals(env, e.env);
		assertEquals(j, p.getStreams().getJob());
	}
	
	@Test
	public void test_create2() throws XenonException {
		Scheduler s = new MockScheduler("ID0", "TEST", "MEM", true, true, false, null);
		
		JobDescription desc = new JobDescription();
		desc.setExecutable("exec");
		
		HashMap<String,String> env = new HashMap<>();
		env.put("key1", "value1");
		env.put("key2", "value2");
		desc.setEnvironment(env);
		desc.setArguments(new String [] { "a", "b", "c" });
		
		JobImplementation j = new JobImplementation(s, "aap", desc);
		
		MockClientSession session = new MockClientSession(false);
		new SshInteractiveProcess(session, j);
		
		MockChannelExec e = (MockChannelExec) session.exec;
		
		assertNotNull(e);
		assertEquals("exec 'a' 'b' 'c'", e.command);
		assertEquals(env, e.env);
	}

	@Test
	public void test_isDoneFalse() throws XenonException {
		Scheduler s = new MockScheduler("ID0", "TEST", "MEM", true, true, false, null);
		
		JobDescription desc = new JobDescription();
		desc.setExecutable("exec");
		
		HashMap<String,String> env = new HashMap<>();
		env.put("key1", "value1");
		env.put("key2", "value2");
		desc.setEnvironment(env);
		desc.setArguments(new String [] { "a", "b", "c" });
		
		JobImplementation j = new JobImplementation(s, "aap", desc);
		
		MockClientSession session = new MockClientSession(false);
		SshInteractiveProcess p = new SshInteractiveProcess(session, j);
		
		assertFalse(p.isDone());
	}
	
	
	@Test
	public void test_isDoneTrue() throws XenonException {
		Scheduler s = new MockScheduler("ID0", "TEST", "MEM", true, true, false, null);
		
		JobDescription desc = new JobDescription();
		desc.setExecutable("exec");
		
		HashMap<String,String> env = new HashMap<>();
		env.put("key1", "value1");
		env.put("key2", "value2");
		desc.setEnvironment(env);
		desc.setArguments(new String [] { "a", "b", "c" });
		
		JobImplementation j = new JobImplementation(s, "aap", desc);
		
		MockClientSession session = new MockClientSession(false);
		SshInteractiveProcess p = new SshInteractiveProcess(session, j);
		
		MockChannelExec e = (MockChannelExec) session.exec;
		e.closed = true;

		assertTrue(p.isDone());
	}
	
	@Test
	public void test_isDoubleDoneTrue() throws XenonException {
		Scheduler s = new MockScheduler("ID0", "TEST", "MEM", true, true, false, null);
		
		JobDescription desc = new JobDescription();
		desc.setExecutable("exec");
		
		HashMap<String,String> env = new HashMap<>();
		env.put("key1", "value1");
		env.put("key2", "value2");
		desc.setEnvironment(env);
		desc.setArguments(new String [] { "a", "b", "c" });
		
		JobImplementation j = new JobImplementation(s, "aap", desc);
		
		MockClientSession session = new MockClientSession(false);
		SshInteractiveProcess p = new SshInteractiveProcess(session, j);
		
		assertNotNull(session.exec);
		
		MockChannelExec e = (MockChannelExec) session.exec;
		e.closed = true;

		p.isDone();
		
		assertTrue(p.isDone());
	}
	
	@Test
	public void test_destroy() throws XenonException {
		Scheduler s = new MockScheduler("ID0", "TEST", "MEM", true, true, false, null);
		
		JobDescription desc = new JobDescription();
		desc.setExecutable("exec");
		
		HashMap<String,String> env = new HashMap<>();
		env.put("key1", "value1");
		env.put("key2", "value2");
		desc.setEnvironment(env);
		desc.setArguments(new String [] { "a", "b", "c" });
		
		JobImplementation j = new JobImplementation(s, "aap", desc);
		
		MockClientSession session = new MockClientSession(false);
		SshInteractiveProcess p = new SshInteractiveProcess(session, j);
		p.destroy();
		

		assertNotNull(session.exec);
		MockChannelExec e = (MockChannelExec) session.exec;
		assertTrue(e.gotClose);
	}

	@Test
	public void test_destroyAfterDone() throws XenonException {
		Scheduler s = new MockScheduler("ID0", "TEST", "MEM", true, true, false, null);
		
		JobDescription desc = new JobDescription();
		desc.setExecutable("exec");
		
		HashMap<String,String> env = new HashMap<>();
		env.put("key1", "value1");
		env.put("key2", "value2");
		desc.setEnvironment(env);
		desc.setArguments(new String [] { "a", "b", "c" });
		
		JobImplementation j = new JobImplementation(s, "aap", desc);
		
		MockClientSession session = new MockClientSession(false);
		SshInteractiveProcess p = new SshInteractiveProcess(session, j);
		
		assertNotNull(session.exec);
		MockChannelExec e = (MockChannelExec) session.exec;
		e.closed = true;

		p.isDone();

		p.destroy();

		assertTrue(e.gotClose);
	}

	@Test
	public void test_forCoverage() throws XenonException {
		Scheduler s = new MockScheduler("ID0", "TEST", "MEM", true, true, false, null);
		
		JobDescription desc = new JobDescription();
		desc.setExecutable("exec");
		
		HashMap<String,String> env = new HashMap<>();
		env.put("key1", "value1");
		env.put("key2", "value2");
		desc.setEnvironment(env);
		desc.setArguments(new String [] { "a", "b", "c" });
		
		JobImplementation j = new JobImplementation(s, "aap", desc);
		
		MockClientSession session = new MockClientSession(false);
		SshInteractiveProcess p = new SshInteractiveProcess(session, j);
		
		assertNotNull(session.exec);
		MockChannelExec e = (MockChannelExec) session.exec;
		e.closeThrows = true;

		p.isDone();
		p.destroy();
		p.getExitStatus();
	}
	

	
	
	
	
}
