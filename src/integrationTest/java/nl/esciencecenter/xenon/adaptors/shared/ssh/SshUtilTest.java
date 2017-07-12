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
