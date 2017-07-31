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
package nl.esciencecenter.xenon.adaptors.shared.ssh;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.junit.Test;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;

public abstract class SshUtilTest {
	
	public abstract String getLocation();

	public abstract Credential getCorrectCredential();
	
	@Test
	public void test_connect() throws Exception { 
		SshClient client = SSHUtil.createSSHClient(false, false, false, false);
		ClientSession session = SSHUtil.connect("test", client, getLocation(), getCorrectCredential(), 10*1000);
		session.close();
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_connect_FailsNullCredential() throws Exception { 
		SshClient client = SSHUtil.createSSHClient(false, false, false, false);
		SSHUtil.connect("test", client, getLocation(), null, 10*1000);
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_connect_FailsInvalidTimeout() throws Exception { 
		SshClient client = SSHUtil.createSSHClient(false, false, false, false);
		SSHUtil.connect("test", client, getLocation(), getCorrectCredential(), -1);
	}
	
	@Test(expected=XenonException.class)
	public void test_connect_FailsUsernameNull() throws Exception { 
		SshClient client = SSHUtil.createSSHClient(false, false, false, false);
		SSHUtil.connect("test", client, getLocation(), new PasswordCredential(null, "foobar".toCharArray()), 10*1000);
	}
	
	

}
