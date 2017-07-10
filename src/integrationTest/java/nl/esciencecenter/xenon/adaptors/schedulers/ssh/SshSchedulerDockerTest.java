package nl.esciencecenter.xenon.adaptors.schedulers.ssh;

import static nl.esciencecenter.xenon.adaptors.schedulers.ssh.SshSchedulerAdaptor.LOAD_STANDARD_KNOWN_HOSTS;
import static nl.esciencecenter.xenon.adaptors.schedulers.ssh.SshSchedulerAdaptor.STRICT_HOST_KEY_CHECKING;

import java.util.HashMap;
import java.util.Map;

import org.junit.ClassRule;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.SchedulerLocationConfig;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.schedulers.Scheduler;


public class SshSchedulerDockerTest extends SshSchedulerTestParent {
	
	@ClassRule
	public static DockerComposeRule docker = DockerComposeRule.builder()
		.file("src/integrationTest/resources/docker-compose/openssh.yml")
		.waitingForService("ssh", HealthChecks.toHaveAllPortsOpen())
		.build();

	@Override
	protected SchedulerLocationConfig setupLocationConfig() {
		return new SshLocationConfig(docker.containers().container("ssh").port(22).inFormat("$HOST:$EXTERNAL_PORT"));
	}

	@Override
	public Scheduler setupScheduler() throws XenonException {
		String location = docker.containers().container("ssh").port(22).inFormat("$HOST:$EXTERNAL_PORT");
		PasswordCredential cred = new PasswordCredential("xenon", "javagat".toCharArray());
		Map<String, String> props = new HashMap<>();
		props.put(STRICT_HOST_KEY_CHECKING, "false");
		props.put(LOAD_STANDARD_KNOWN_HOSTS, "false");
		return Scheduler.create("ssh", location, cred, props);
	}
	
	
	
	
}
