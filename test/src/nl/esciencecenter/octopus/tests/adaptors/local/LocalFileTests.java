package nl.esciencecenter.octopus.tests.adaptors.local;

import static org.junit.Assert.*;

import java.net.URI;

import junit.framework.Assert;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Path;

public class LocalFileTests {

    @org.junit.Test
    public void test1() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);

        String tmpdir = System.getProperty("java.io.tmpdir");
        
        System.err.println("tmpdir = " + tmpdir);

        FileSystem fs = octopus.files().newFileSystem(new URI("local"), null, null);
        
        Path path = octopus.files().newPath(fs, tmpdir);

        Assert.assertTrue(octopus.files().exists(path));

        octopus.end();

    }

    @org.junit.Test
    public void test2() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);

        FileSystem fs = octopus.files().newFileSystem(new URI("local"), null, null);
        
        Path tmpDir = octopus.files().newPath(fs, System.getProperty("java.io.tmpdir"));

        System.err.println("tmpdir = " + tmpDir);

        assertTrue(octopus.files().exists(tmpDir));

        assertTrue(octopus.files().isDirectory(tmpDir));

        Path sandboxDir = octopus.files().newPath(fs, System.getProperty("java.io.tmpdir") + "/test-sandbox");

        if (octopus.files().exists(sandboxDir)) {
            System.err.println("deleting " + sandboxDir);
            octopus.files().delete(sandboxDir);
        }

        assertFalse(octopus.files().exists(sandboxDir));

        octopus.files().createDirectory(sandboxDir);

        assertTrue(octopus.files().exists(sandboxDir));

        octopus.end();

    }

}
