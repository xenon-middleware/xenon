package nl.esciencecenter.xenon.adaptors.filesystems.hdfs;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.filesystems.FileSystemTestParent;
import nl.esciencecenter.xenon.adaptors.filesystems.LocationConfig;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;
import org.junit.ClassRule;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by atze on 25-8-17.
 */
public class HDFSFileSystemDockerTest extends FileSystemTestParent {

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder().file("src/integrationTest/resources/docker-compose/minio.yml")
            .waitingForService("minio", HealthChecks.toHaveAllPortsOpen()).saveLogsTo("/var/tmp/bla").build();

    @Override
    protected LocationConfig setupLocationConfig(FileSystem fileSystem) {
        return new LocationConfig() {

            @Override
            public Map.Entry<Path, Path> getSymbolicLinksToExistingFile() {
                throw new Error("Symlinks not supported on S3");
            }

            @Override
            public Path getExistingPath() {
                return new Path("links/file0");
            }

            @Override
            public Path getWritableTestDir() {
                return fileSystem.getWorkingDirectory();
            }

            @Override
            public Path getExpectedWorkingDirectory() {
                return new Path("/");
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
