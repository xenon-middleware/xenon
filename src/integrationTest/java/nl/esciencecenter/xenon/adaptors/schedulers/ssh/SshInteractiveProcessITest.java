package nl.esciencecenter.xenon.adaptors.schedulers.ssh;

import static org.junit.Assert.*;

import java.io.OutputStream;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.junit.Test;

import nl.esciencecenter.xenon.adaptors.schedulers.InputWriter;
import nl.esciencecenter.xenon.adaptors.schedulers.OutputReader;
import nl.esciencecenter.xenon.adaptors.shared.ssh.SSHUtil;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobHandle;
import nl.esciencecenter.xenon.schedulers.Streams;

public abstract class SshInteractiveProcessITest {

	public abstract String getLocation();

	public abstract Credential getCorrectCredential();
	
	@Test
	public void test_run_hostname() throws Exception { 
		SshClient client = SSHUtil.createSSHClient(false, false, false, false);
		ClientSession session = SSHUtil.connect("test", client, getLocation(), getCorrectCredential(), 10*1000);
		
		JobDescription desc = new JobDescription();
		desc.setExecutable("/bin/hostname");
		
		JobHandle h = new MockJobHandle("TESTID", desc);
		
		SshInteractiveProcess p = new SshInteractiveProcess(session, h);
		
		Streams s = p.getStreams();

		assertNotNull(s.getStdin());
		assertNotNull(s.getStdout());
		assertNotNull(s.getStderr());

		// No input, so close stdin
		s.getStdin().close();
		
		OutputReader stdout = new OutputReader(s.getStdout());
		OutputReader stderr = new OutputReader(s.getStderr());
		
		stderr.waitUntilFinished();
		stdout.waitUntilFinished();
		
		String output = stdout.getResult();
		String error = stderr.getResult();
		
		assertTrue(error.isEmpty());
		assertFalse(output.isEmpty());
		assertEquals(0, p.getExitStatus());
	}


	@Test
	public void test_run_cat() throws Exception { 
		SshClient client = SSHUtil.createSSHClient(false, false, false, false);
		ClientSession session = SSHUtil.connect("test", client, getLocation(), getCorrectCredential(), 10*1000);
		
		JobDescription desc = new JobDescription();
		desc.setExecutable("/bin/cat");
		
		JobHandle h = new MockJobHandle("TESTID", desc);
		
		SshInteractiveProcess p = new SshInteractiveProcess(session, h);
		
		Streams s = p.getStreams();

		assertNotNull(s.getStdin());
		assertNotNull(s.getStdout());
		assertNotNull(s.getStderr());

		String message = "Hello World!";
		
		InputWriter stdin = new InputWriter(message, s.getStdin());
		OutputReader stdout = new OutputReader(s.getStdout());
		OutputReader stderr = new OutputReader(s.getStderr());
		
		stdin.waitUntilFinished();		
		stderr.waitUntilFinished();
		stdout.waitUntilFinished();
		
		String output = stdout.getResult();
		String error = stderr.getResult();
		
		assertTrue(error.isEmpty());
		assertEquals(message, output);
		assertEquals(0, p.getExitStatus());
	}

	
	
//	@Test
//	public void test_exitStatusBeforeFinish() throws Exception { 
//		SshClient client = SSHUtil.createSSHClient(false, false, false, false);
//		ClientSession session = SSHUtil.connect("test", client, getLocation(), getCorrectCredential(), 10*1000);
//		
//		JobDescription desc = new JobDescription();
//		desc.setExecutable("/bin/hostname");
//		
//		JobHandle h = new MockJobHandle("TESTID", desc);
//		
//		SshInteractiveProcess p = new SshInteractiveProcess(session, h);
//		
//		assertEquals(-1, p.getExitStatus());
//		
//		p.destroy();		
//	}
//
	
}
