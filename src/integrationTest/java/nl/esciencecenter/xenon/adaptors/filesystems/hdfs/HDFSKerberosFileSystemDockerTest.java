package nl.esciencecenter.xenon.adaptors.filesystems.hdfs;


import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.filesystems.FileSystemTestParent;
import nl.esciencecenter.xenon.adaptors.filesystems.LocationConfig;
import nl.esciencecenter.xenon.credentials.KeytabCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;
import org.junit.ClassRule;

import java.util.HashMap;
import java.util.Map;

import static nl.esciencecenter.xenon.adaptors.filesystems.hdfs.HDFSFileAdaptor.REPLACE_ON_FAILURE;

public class HDFSKerberosFileSystemDockerTest extends FileSystemTestParent {

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder().file("src/integrationTest/resources/docker-compose/hdfs-kerberos.yml")
            .waitingForService("hdfs", HealthChecks.toHaveAllPortsOpen()).build();

    @Override
    protected LocationConfig setupLocationConfig(FileSystem fileSystem) {
        return new LocationConfig() {

            @Override
            public Map.Entry<Path, Path> getSymbolicLinksToExistingFile() {
                // TODO: fix me
                throw new Error("Symlinks not yet supported on HDFS");
            }

            @Override
            public Path getExistingPath() {
                return new Path("/filesystem-test-fixture/links/file0");
            }

            @Override
            public Path getWritableTestDir() {
                return fileSystem.getWorkingDirectory();
            }

            @Override
            public Path getExpectedWorkingDirectory() {
                return new Path("/filesystem-test-fixture");
            }
        };
    }

    @Override
    public FileSystem setupFileSystem() throws XenonException {
        String location = docker.containers().container("hdfs").port(8020).inFormat("localhost:$EXTERNAL_PORT");
        System.setProperty("java.security.krb5.conf", "src/integrationTest/resources/kerberos/krb5.conf");
        Map<String, String> props = new HashMap<>();
        // this is needed for a single data-node hadoop cluster
        props.put(REPLACE_ON_FAILURE, "NEVER");
        props.put(HDFSFileAdaptor.AUTHENTICATION,"kerberos");
        props.put(HDFSFileAdaptor.DFS_NAMENODE_KERBEROS_PRINCIPAL,"hdfs/localhost@esciencecenter.nl");
        props.put(HDFSFileAdaptor.BLOCK_ACCESS_TOKEN, "true");
        props.put(HDFSFileAdaptor.TRANSFER_PROTECTION, "integrity");
//            properties.put(HDFSFileAdaptor.DATA_NODE_ADRESS, "localhost:50016");
        KeytabCredential kt = new KeytabCredential("xenon@esciencecenter.nl","src/integrationTest/resources/kerberos/xenon.keytab");
        FileSystem fs =  FileSystem.create("hdfs", location, kt, props);
        fs.setWorkingDirectory(new Path("/filesystem-test-fixture"));
        return fs;
    }
}
