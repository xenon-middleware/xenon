package nl.esciencecenter.octopus.util;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.engine.files.AbsolutePathImplementation;
import nl.esciencecenter.octopus.engine.files.FileSystemImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.util.Sandbox.Pair;

import org.junit.Test;

public class SandboxTest {

    @Test
    public void testSandbox() throws URISyntaxException, OctopusIOException, OctopusException {
        Octopus octopus = mock(Octopus.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));

        Sandbox sandbox = new Sandbox(octopus, path, "sandbox-1");

        AbsolutePath expectedPath = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandbox-1"));
        assertThat(sandbox.getPath(), is(expectedPath));
        assertThat(sandbox.getUploadFiles().size(), is(0));
        assertThat(sandbox.getDownloadFiles().size(), is(0));
    }

    @Test
    public void testSetUploadFiles() {
        fail("Not yet implemented");
    }

    @Test
    public void testAddUploadFileAbsolutePath() {
        fail("Not yet implemented");
    }

    @Test
    public void testAddUploadFileAbsolutePathString() throws URISyntaxException, OctopusIOException, OctopusException {
        Octopus octopus = mock(Octopus.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));
        Sandbox sandbox = new Sandbox(octopus, path, "sandbox-1");

        AbsolutePath src = new AbsolutePathImplementation(fs, new RelativePath("/tmp/inputfile"));
        sandbox.addUploadFile(src, "input");

        List<Pair> uploadfiles = sandbox.getUploadFiles();
        AbsolutePath dst = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandbox-1/input"));
        assertThat(uploadfiles.size(), is(1));
        assertThat(uploadfiles.get(0).source, is(src));
        assertThat(uploadfiles.get(0).destination, is(dst));
    }

    @Test
    public void testGetDownloadFiles() {
        fail("Not yet implemented");
    }

    @Test
    public void testAddDownloadFile() {
        fail("Not yet implemented");
    }

    @Test
    public void testUpload() throws OctopusIOException, OctopusException, URISyntaxException {
        Octopus octopus = mock(Octopus.class);
        Files files = mock(Files.class);
        when(octopus.files()).thenReturn(files);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));
        Sandbox sandbox = new Sandbox(octopus, path, "sandbox-1");
        AbsolutePath src = new AbsolutePathImplementation(fs, new RelativePath("/tmp/inputfile"));
        sandbox.addUploadFile(src, "input");

        sandbox.upload();

        AbsolutePath sandboxDir = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandbox-1"));
        verify(files).createDirectory(sandboxDir);
        AbsolutePath dst = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandbox-1/input"));
        verify(files).copy(src, dst);
    }

    @Test
    public void testDownload() {
        fail("Not yet implemented");
    }

    @Test
    public void testWipe() {
        fail("Not yet implemented");
    }

    @Test
    public void testDelete() {
        fail("Not yet implemented");
    }

}
