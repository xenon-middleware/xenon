package nl.esciencecenter.xenon.adaptors.filesystems.sftp;

import static nl.esciencecenter.xenon.adaptors.filesystems.sftp.SftpFileAdaptor.LOAD_STANDARD_KNOWN_HOSTS;
import static nl.esciencecenter.xenon.adaptors.filesystems.sftp.SftpFileAdaptor.STRICT_HOST_KEY_CHECKING;

import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.filesystems.LocationConfig;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.junit.ClassRule;

public class SftpFileSystemDockerTest extends SftpFileSystemTestParent {

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
        .file("src/integrationTest/resources/docker-compose/openssh.yml")
        .waitingForService("ssh", HealthChecks.toHaveAllPortsOpen())
        .build();

    @Override
    protected LocationConfig setupLocationConfig() {
        return new SftpLocationConfig();
    }

    @Override
    public FileSystem setupFileSystem() throws XenonException {
        String location = docker.containers().container("ssh").port(22).inFormat("$HOST:$EXTERNAL_PORT");
        PasswordCredential cred = new PasswordCredential("xenon", "javagat".toCharArray());
        Map<String, String> props = new HashMap<>();
        props.put(STRICT_HOST_KEY_CHECKING, "false");
        props.put(LOAD_STANDARD_KNOWN_HOSTS, "false");
        return FileSystem.create("sftp", location, cred, props);
    }
}
