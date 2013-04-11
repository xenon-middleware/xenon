package nl.esciencecenter.octopus.tests.adaptors.ssh;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import junit.framework.Assert;
import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.RelativePath;

public class SshFileTests {

    @org.junit.Test
    public void test1() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);

        Credentials c = octopus.credentials();

        String username = System.getProperty("user.name");

        Credential credential = c.newCertificateCredential("/home/" + username + "/.ssh/id_rsa", ".ssh/id_rsa.pub", username, "");

        FileSystem fileSystem = octopus.files().newFileSystem(new URI("ssh://" + username + "@localhost"), credential, null);

        AbsolutePath path = octopus.files().newPath(fileSystem, new RelativePath(System.getProperty("java.io.tmpdir")));

        Assert.assertTrue(octopus.files().exists(path));

        octopus.end();
    }

    @org.junit.Test
    public void test2() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);
        Credentials c = octopus.credentials();
        String username = System.getProperty("user.name");
        Credential credential = c.newCertificateCredential("/home/" + username + "/.ssh/id_rsa", "/home/" + username + "/.ssh/id_rsa.pub", username, "");
       
        FileSystem fileSystem = octopus.files().newFileSystem(new URI("ssh://" + username + "@localhost"), credential, null);

        AbsolutePath path = octopus.files().newPath(fileSystem, new RelativePath(".bashrc"));

        InputStream in = octopus.files().newInputStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        while (true) {
            String line = br.readLine();
            if (line == null)
                break;
            System.err.println(line);
        }

        octopus.end();
    }

    // test connection refused
    // test com.jcraft.jsch.JSchException: UnknownHostKey: localhost. RSA key fingerprint is a7:08:d3:10:af:df:94:85:d6:65:74:3c:a4:6d:80:48
}
