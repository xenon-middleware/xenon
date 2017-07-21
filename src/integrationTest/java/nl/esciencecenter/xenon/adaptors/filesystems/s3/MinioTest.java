package nl.esciencecenter.xenon.adaptors.filesystems.s3;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.filesystems.LocationConfig;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;
import org.junit.ClassRule;

import java.util.HashMap;
import java.util.Map;

import static nl.esciencecenter.xenon.adaptors.filesystems.sftp.SftpFileAdaptor.LOAD_STANDARD_KNOWN_HOSTS;
import static nl.esciencecenter.xenon.adaptors.filesystems.sftp.SftpFileAdaptor.STRICT_HOST_KEY_CHECKING;

/**
 * Created by atze on 13-7-17.
 */
public class MinioTest extends TestParent {



    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file("src/integrationTest/resources/docker-compose/minio.yml")
            .waitingForService("minio", HealthChecks.toHaveAllPortsOpen()).saveLogsTo("/var/tmp/bla")
            .build();

    @Override
    protected LocationConfig setupLocationConfig(FileSystem fileSystem) {
        return new LocationConfig() {
            @Override
            public Path getExistingPath() {
                return new Path("links/file0");
            }

            @Override
            public Map.Entry<Path, Path> getSymbolicLinksToExistingFile() {
                return null;
            }
        };
    }

    @Override
    public FileSystem setupFileSystem() throws XenonException {
        String location = docker.containers().container("minio").port(9000).inFormat("http://localhost:$EXTERNAL_PORT/filesystem-test-fixture");
        PasswordCredential cred = new PasswordCredential("xenon", "javagat01".toCharArray());
        Map<String, String> props = new HashMap<>();
        return FileSystem.create("s3", location, cred, props);
    }
}
