package nl.esciencecenter.xenon.adaptors.shared.ssh;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.junit.ClassRule;
import org.junit.Test;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;

import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;

public class SshUtilDockerTest extends SshUtilTest {
	
	@ClassRule
	public static DockerComposeRule docker = DockerComposeRule.builder()
		.file("src/integrationTest/resources/docker-compose/openssh.yml")
		.waitingForService("ssh", HealthChecks.toHaveAllPortsOpen())
		.build();

	public String getLocation() {
		return docker.containers().container("ssh").port(22).inFormat("$HOST:$EXTERNAL_PORT");
	}
	
	public Credential getCorrectCredential() { 
		return new PasswordCredential("xenon", "javagat".toCharArray());
	}
	
	public Credential getInvalidUserCredential() { 
		return new PasswordCredential("aap", "javagat".toCharArray());
	}
	
	public Credential getInvalidPasswordCredential() { 
		return new PasswordCredential("xenon", "aap".toCharArray());
	}
	
	
}
