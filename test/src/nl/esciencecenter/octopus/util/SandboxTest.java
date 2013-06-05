package nl.esciencecenter.octopus.util;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.net.URISyntaxException;
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
    public void testSandbox_WithName() throws URISyntaxException, OctopusIOException, OctopusException {
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
    public void testSandbox_WithoutName() throws URISyntaxException, OctopusIOException, OctopusException {
        Octopus octopus = mock(Octopus.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));

        Sandbox sandbox = new Sandbox(octopus, path, null);

        String sandboxPath = sandbox.getPath().getPath();
        assertTrue(sandboxPath.startsWith("/tmp/octopus_sandbox_"));
    }

    @Test
    public void testAddUploadFile_SrcAndDst() throws URISyntaxException, OctopusIOException, OctopusException {
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
    public void testAddUploadFile_DstNull_DstSameFileName() throws URISyntaxException, OctopusIOException, OctopusException {
        Octopus octopus = mock(Octopus.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandboxes"));
        Sandbox sandbox = new Sandbox(octopus, path, "sandbox-1");

        AbsolutePath src = new AbsolutePathImplementation(fs, new RelativePath("/tmp/inputfile"));
        sandbox.addUploadFile(src, null);

        List<Pair> uploadfiles = sandbox.getUploadFiles();
        AbsolutePath dst = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandboxes/sandbox-1/inputfile"));
        assertThat(uploadfiles.size(), is(1));
        assertThat(uploadfiles.get(0).source, is(src));
        assertThat(uploadfiles.get(0).destination, is(dst));
    }

    @Test
    public void testAddUploadFile_SrcNull_NullPointerException() throws URISyntaxException, OctopusIOException, OctopusException {
        Octopus octopus = mock(Octopus.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));
        Sandbox sandbox = new Sandbox(octopus, path, "sandbox-1");

        try {
            sandbox.addUploadFile(null);
            fail("Expected an NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), is("the source path cannot be null when adding a preStaged file"));
        }
    }

    @Test
    public void testUpload_NoSandboxDir_MkdirAndCopy() throws OctopusIOException, OctopusException, URISyntaxException {
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
    public void testUpload_SandboxDirExists_Copy() throws OctopusIOException, OctopusException, URISyntaxException {
        Octopus octopus = mock(Octopus.class);
        Files files = mock(Files.class);
        when(octopus.files()).thenReturn(files);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));
        Sandbox sandbox = new Sandbox(octopus, path, "sandbox-1");
        AbsolutePath sandboxDir = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandbox-1"));
        when(files.exists(sandboxDir)).thenReturn(true);
        AbsolutePath src = new AbsolutePathImplementation(fs, new RelativePath("/tmp/inputfile"));
        sandbox.addUploadFile(src, "input");

        sandbox.upload();

        verify(files, never()).createDirectory(sandboxDir);
        AbsolutePath dst = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandbox-1/input"));
        verify(files).copy(src, dst);
    }

    @Test
    public void testDelete() throws URISyntaxException, OctopusIOException, OctopusException {
        Octopus octopus = mock(Octopus.class);
        Files files = mock(Files.class);
        when(octopus.files()).thenReturn(files);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath root = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));
        Sandbox sandbox = new Sandbox(octopus, root, "sandbox-1");

        sandbox.delete();

        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandbox-1"));
        verify(files).delete(path);
    }

    @Test
    public void testAddDownloadFile_SrcAndDst() throws URISyntaxException, OctopusIOException, OctopusException {
        Octopus octopus = mock(Octopus.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));
        Sandbox sandbox = new Sandbox(octopus, path, "sandbox-1");

        AbsolutePath dst = new AbsolutePathImplementation(fs, new RelativePath("/tmp/outputfile"));
        sandbox.addDownloadFile("output", dst);

        List<Pair> uploadfiles = sandbox.getDownloadFiles();
        AbsolutePath src = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandbox-1/output"));
        assertThat(uploadfiles.size(), is(1));
        assertThat(uploadfiles.get(0).source, is(src));
        assertThat(uploadfiles.get(0).destination, is(dst));
    }

    @Test
    public void testAddDownloadFile_SrcNull_SrcSameFileName() throws URISyntaxException, OctopusIOException, OctopusException {
        Octopus octopus = mock(Octopus.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandboxes"));
        Sandbox sandbox = new Sandbox(octopus, path, "sandbox-1");

        AbsolutePath dst = new AbsolutePathImplementation(fs, new RelativePath("/tmp/outputfile"));
        sandbox.addDownloadFile(null, dst);

        List<Pair> uploadfiles = sandbox.getDownloadFiles();
        AbsolutePath src = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandboxes/sandbox-1/outputfile"));
        assertThat(uploadfiles.size(), is(1));
        assertThat(uploadfiles.get(0).source, is(src));
        assertThat(uploadfiles.get(0).destination, is(dst));
    }

    @Test
    public void testAddDownloadFile_DstNull_NullPointerException() throws URISyntaxException, OctopusIOException,
            OctopusException {
        Octopus octopus = mock(Octopus.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));
        Sandbox sandbox = new Sandbox(octopus, path, "sandbox-1");

        try {
            sandbox.addDownloadFile("output", null);
            fail("Expected an NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), is("the destination path cannot be null when adding a postStaged file"));
        }
    }

    @Test
    public void testDownload() throws OctopusIOException, URISyntaxException, OctopusException {
        Octopus octopus = mock(Octopus.class);
        Files files = mock(Files.class);
        when(octopus.files()).thenReturn(files);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));
        Sandbox sandbox = new Sandbox(octopus, path, "sandbox-1");
        AbsolutePath dst = new AbsolutePathImplementation(fs, new RelativePath("/tmp/outputfile"));
        sandbox.addDownloadFile("output", dst);

        sandbox.download();

        AbsolutePath src = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandbox-1/output"));
        verify(files).copy(src, dst);
    }

    private Sandbox sampleSandbox() throws URISyntaxException, OctopusException, OctopusIOException {
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));
        // Use null for octopus so hashcode is the same every time
        Sandbox sandbox = new Sandbox(null, path, "sandbox-1");
        sandbox.addUploadFile(new AbsolutePathImplementation(fs, new RelativePath("/tmp/inputfile")));
        sandbox.addDownloadFile("outputfile", new AbsolutePathImplementation(fs, new RelativePath("/tmp/outputfile")));
        return sandbox;
    }

    @Test
    public void testHashCode() throws URISyntaxException, OctopusIOException, OctopusException {
        Sandbox sandbox = sampleSandbox();

        assertEquals(47434737, sandbox.hashCode());
    }

    @Test
    public void testToString() throws URISyntaxException, OctopusIOException, OctopusException {
        Sandbox sandbox = sampleSandbox();

        String expected =
                "Sandbox [octopus=null, path=FileSystemImplementation [adaptorName=local, uri=file:///, entryPath=RelativePath [element=[], seperator=/], properties=null]RelativePath [element=[tmp, sandbox-1], seperator=/], uploadFiles=[Pair [source=FileSystemImplementation [adaptorName=local, uri=file:///, entryPath=RelativePath [element=[], seperator=/], properties=null]RelativePath [element=[tmp, inputfile], seperator=/], destination=FileSystemImplementation [adaptorName=local, uri=file:///, entryPath=RelativePath [element=[], seperator=/], properties=null]RelativePath [element=[tmp, sandbox-1, inputfile], seperator=/]]], downloadFiles=[Pair [source=FileSystemImplementation [adaptorName=local, uri=file:///, entryPath=RelativePath [element=[], seperator=/], properties=null]RelativePath [element=[tmp, sandbox-1, outputfile], seperator=/], destination=FileSystemImplementation [adaptorName=local, uri=file:///, entryPath=RelativePath [element=[], seperator=/], properties=null]RelativePath [element=[tmp, outputfile], seperator=/]]]]";
        assertEquals(expected, sandbox.toString());
    }

    @Test
    public void testEquals_sameObject_equal() throws URISyntaxException, OctopusIOException, OctopusException {
        Sandbox sandbox = sampleSandbox();

        assertEquals(sandbox, sandbox);
    }

    @Test
    public void testEquals_otherClass_notEqual() throws URISyntaxException, OctopusIOException, OctopusException {
        Sandbox sandbox = sampleSandbox();

        assertFalse(sandbox.equals(42));
    }

    @Test
    public void testEquals_otherNull_notEqual() throws URISyntaxException, OctopusIOException, OctopusException {
        Sandbox sandbox = sampleSandbox();

        assertFalse(sandbox.equals(null));
    }

    @Test
    public void testEquals_otherOctopus_notEqual() throws URISyntaxException, OctopusIOException, OctopusException {
        Octopus octopus = mock(Octopus.class);
        Octopus octopus2 = mock(Octopus.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));

        Sandbox sandbox1 = new Sandbox(octopus, path, "sandbox-1");
        Sandbox sandbox2 = new Sandbox(octopus2, path, "sandbox-1");

        assertThat(sandbox1, is(not(sandbox2)));
    }

    @Test
    public void testEquals_thisOctopusIsNull_notEqual() throws URISyntaxException, OctopusIOException, OctopusException {
        Octopus octopus = mock(Octopus.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));

        Sandbox sandbox1 = new Sandbox(null, path, "sandbox-1");
        Sandbox sandbox2 = new Sandbox(octopus, path, "sandbox-1");

        assertThat(sandbox1, is(not(sandbox2)));
    }

    @Test
    public void testEquals_otherOctopusIsNull_notEqual() throws URISyntaxException, OctopusIOException, OctopusException {
        Octopus octopus = mock(Octopus.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));

        Sandbox sandbox1 = new Sandbox(octopus, path, "sandbox-1");
        Sandbox sandbox2 = new Sandbox(null, path, "sandbox-1");

        assertThat(sandbox1, is(not(sandbox2)));
    }

    @Test
    public void testEquals_bothOctopusNull_Equal() throws URISyntaxException, OctopusIOException, OctopusException {
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));

        Sandbox sandbox1 = new Sandbox(null, path, "sandbox-1");
        Sandbox sandbox2 = new Sandbox(null, path, "sandbox-1");

        assertThat(sandbox1, is(sandbox2));
    }

    @Test
    public void testEquals_otherPath_notEqual() throws URISyntaxException, OctopusIOException, OctopusException {
        Octopus octopus = mock(Octopus.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));

        Sandbox sandbox1 = new Sandbox(octopus, path, "sandbox-1");
        Sandbox sandbox2 = new Sandbox(octopus, path, "sandbox-2");

        assertThat(sandbox1, is(not(sandbox2)));
    }

    @Test
    public void testEquals_otherUpload_notEqual() throws URISyntaxException, OctopusIOException, OctopusException {
        Octopus octopus = mock(Octopus.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));

        Sandbox sandbox1 = new Sandbox(octopus, path, "sandbox-1");
        AbsolutePath src = new AbsolutePathImplementation(fs, new RelativePath("/tmp/inputfile"));
        sandbox1.addUploadFile(src);
        Sandbox sandbox2 = new Sandbox(octopus, path, "sandbox-1");

        assertThat(sandbox1, is(not(sandbox2)));
    }

    @Test
    public void testEquals_otherDownload_notEqual() throws URISyntaxException, OctopusIOException, OctopusException {
        Octopus octopus = mock(Octopus.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));

        Sandbox sandbox1 = new Sandbox(octopus, path, "sandbox-1");
        AbsolutePath src = new AbsolutePathImplementation(fs, new RelativePath("/tmp/outputfile"));
        sandbox1.addDownloadFile("outputfile", src);
        Sandbox sandbox2 = new Sandbox(octopus, path, "sandbox-1");

        assertThat(sandbox1, is(not(sandbox2)));
    }

    @Test
    public void testEquals_sameUpload_Equal() throws URISyntaxException, OctopusIOException, OctopusException {
        Octopus octopus = mock(Octopus.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));

        Sandbox sandbox1 = new Sandbox(octopus, path, "sandbox-1");
        AbsolutePath src = new AbsolutePathImplementation(fs, new RelativePath("/tmp/inputfile"));
        sandbox1.addUploadFile(src);
        Sandbox sandbox2 = new Sandbox(octopus, path, "sandbox-1");
        AbsolutePath src2 = new AbsolutePathImplementation(fs, new RelativePath("/tmp/inputfile"));
        sandbox2.addUploadFile(src2);

        assertThat(sandbox1, is(sandbox2));
    }

}
