package nl.esciencecenter.octopus.adaptors.local;

import static org.junit.Assert.*;

import java.net.URI;

import junit.framework.Assert;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.files.Path;

public class LocalFileTests {

    @org.junit.Test
    public void test1() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);

        URI location = new URI(System.getProperty("java.io.tmpdir"));

        System.err.println("tmpdir = " + location);

        Path path = octopus.files().newPath(location);

        Assert.assertTrue(octopus.files().exists(path));

        octopus.end();

    }

    @org.junit.Test
    public void test2() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);

        Path tmpDir = octopus.files().newPath(new URI(System.getProperty("java.io.tmpdir")));

        System.err.println("tmpdir = " + tmpDir);

        assertTrue(octopus.files().exists(tmpDir));

        assertTrue(octopus.files().isDirectory(tmpDir));

        Path sandboxDir = octopus.files().newPath(new URI(System.getProperty("java.io.tmpdir") + "/test-sandbox"));

        if (octopus.files().exists(sandboxDir)) {
            System.err.println("deleting " + sandboxDir);
            octopus.files().delete(sandboxDir);
        }

        assertFalse(octopus.files().exists(sandboxDir));

        octopus.files().createDirectory(sandboxDir, null);

        assertTrue(octopus.files().exists(sandboxDir));

        octopus.end();

    }

}
