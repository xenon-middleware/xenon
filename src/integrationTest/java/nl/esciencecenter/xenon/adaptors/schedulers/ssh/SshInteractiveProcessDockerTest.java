package nl.esciencecenter.xenon.adaptors.schedulers.ssh;

import org.junit.ClassRule;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;

import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;

public class SshInteractiveProcessDockerTest extends SshInteractiveProcessITest {
	
	@ClassRule
	public static DockerComposeRule docker = DockerComposeRule.builder()
		.file("src/integrationTest/resources/docker-compose/openssh.yml")
		.waitingForService("ssh", HealthChecks.toHaveAllPortsOpen())
		.build();

	public String getLocation() {
		return docker.containers().container("ssh").port(22).inFormat("$HOST:$EXTERNAL_PORT");
	}

	@Override
	public Credential getCorrectCredential() {
		return new PasswordCredential("xenon", "javagat".toCharArray());
	}
	
}
