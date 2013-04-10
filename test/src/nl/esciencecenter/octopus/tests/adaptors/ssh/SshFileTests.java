package nl.esciencecenter.octopus.tests.adaptors.ssh;

import java.net.URI;

import junit.framework.Assert;
import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.files.Path;

public class SshFileTests {

    @org.junit.Test
    public void test1() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);

        Credentials c = octopus.credentials();
        c.newCertificateCredential(octopus.files().newPath(new URI("/home/rob/.ssh/id_rsa")), octopus.files().newPath(new URI(".ssh/id_rsa.pub")), "rob", "", new URI("ssh:localhost"));
        
        URI location = new URI("ssh://rob@localhost" + System.getProperty("java.io.tmpdir"));

        System.err.println("tmpdir = " + location);

        Path path = octopus.files().newPath(location);

        Assert.assertTrue(octopus.files().exists(path));

        octopus.end();
    }

    // test connection refused
    // test com.jcraft.jsch.JSchException: UnknownHostKey: localhost. RSA key fingerprint is a7:08:d3:10:af:df:94:85:d6:65:74:3c:a4:6d:80:48

}
