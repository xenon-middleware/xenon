/*
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.esciencecenter.octopus.util;

import static org.junit.Assert.*;
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

    private Sandbox sampleSandbox() throws URISyntaxException, OctopusException, OctopusIOException {

        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));
        Sandbox sandbox = new Sandbox(mock(Files.class), path, "sandbox-1");
        sandbox.addUploadFile(new AbsolutePathImplementation(fs, new RelativePath("/tmp/inputfile")));
        sandbox.addDownloadFile("outputfile", new AbsolutePathImplementation(fs, new RelativePath("/tmp/outputfile")));
        return sandbox;
    }

    @Test(expected = OctopusException.class)
    public void testSandbox_WithNullOctopus() throws URISyntaxException, OctopusIOException, OctopusException {
        // throws exception
        new Sandbox(null, mock(AbsolutePath.class), "sandbox-1");
    }

    @Test(expected = OctopusException.class)
    public void testSandbox_WithNullPath() throws URISyntaxException, OctopusIOException, OctopusException {
        // throws exception
        new Sandbox(mock(Files.class), null, "sandbox-1");
    }

    @Test
    public void testSandbox_WithName() throws URISyntaxException, OctopusIOException, OctopusException {
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));

        Sandbox sandbox = new Sandbox(mock(Files.class), path, "sandbox-1");

        AbsolutePath expectedPath = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandbox-1"));
        assertEquals(expectedPath, sandbox.getPath());
        assertEquals(0, sandbox.getUploadFiles().size());
        assertEquals(0, sandbox.getDownloadFiles().size());
    }

    @Test
    public void testSandbox_WithoutName() throws URISyntaxException, OctopusIOException, OctopusException {
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));

        Sandbox sandbox = new Sandbox(mock(Files.class), path, null);

        String sandboxPath = sandbox.getPath().getPath();
        assertTrue(sandboxPath.startsWith("/tmp/octopus_sandbox_"));
    }

    @Test
    public void testAddUploadFile_SrcAndDst() throws URISyntaxException, OctopusIOException, OctopusException {
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));
        Sandbox sandbox = new Sandbox(mock(Files.class), path, "sandbox-1");

        AbsolutePath src = new AbsolutePathImplementation(fs, new RelativePath("/tmp/inputfile"));
        sandbox.addUploadFile(src, "input");

        List<Pair> uploadfiles = sandbox.getUploadFiles();
        AbsolutePath dst = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandbox-1/input"));
        assertEquals(1, uploadfiles.size());
        assertEquals(src, uploadfiles.get(0).source);
        assertEquals(dst, uploadfiles.get(0).destination);
    }

    @Test
    public void testSetUploadFiles() throws URISyntaxException, OctopusIOException, OctopusException {
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));
        Sandbox sandbox = new Sandbox(mock(Files.class), path, "sandbox-1");

        AbsolutePath src1 = new AbsolutePathImplementation(fs, new RelativePath("/tmp/input1"));
        AbsolutePath src2 = new AbsolutePathImplementation(fs, new RelativePath("/tmp/input2"));

        sandbox.setUploadFiles(src1, src2);

        List<Pair> uploadfiles = sandbox.getUploadFiles();

        AbsolutePath dst1 = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandbox-1/input1"));
        AbsolutePath dst2 = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandbox-1/input2"));

        assertEquals(2, uploadfiles.size());
        assertEquals(src1, uploadfiles.get(0).source);
        assertEquals(dst1, uploadfiles.get(0).destination);

        assertEquals(src2, uploadfiles.get(1).source);
        assertEquals(dst2, uploadfiles.get(1).destination);
    }

    @Test
    public void testAddUploadFile_DstNull_DstSameFileName() throws URISyntaxException, OctopusIOException, OctopusException {
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandboxes"));
        Sandbox sandbox = new Sandbox(mock(Files.class), path, "sandbox-1");

        AbsolutePath src = new AbsolutePathImplementation(fs, new RelativePath("/tmp/inputfile"));
        sandbox.addUploadFile(src, null);

        List<Pair> uploadfiles = sandbox.getUploadFiles();
        AbsolutePath dst = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandboxes/sandbox-1/inputfile"));
        assertEquals(1, uploadfiles.size());
        assertEquals(src, uploadfiles.get(0).source);
        assertEquals(dst, uploadfiles.get(0).destination);
    }

    @Test
    public void testAddUploadFile_SrcNull_NullPointerException() throws URISyntaxException, OctopusIOException, OctopusException {
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));
        Sandbox sandbox = new Sandbox(mock(Files.class), path, "sandbox-1");

        try {
            sandbox.addUploadFile(null);
            fail("Expected an NullPointerException");
        } catch (NullPointerException e) {
            assertEquals("the source path cannot be null when adding a preStaged file", e.getMessage());
        }
    }

    @Test
    public void testUpload_NoSandboxDir_MkdirAndCopy() throws OctopusIOException, OctopusException, URISyntaxException {
        Files files = mock(Files.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));
        Sandbox sandbox = new Sandbox(files, path, "sandbox-1");
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
        Files files = mock(Files.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));
        Sandbox sandbox = new Sandbox(files, path, "sandbox-1");
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
        Files files = mock(Files.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath root = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));
        Sandbox sandbox = new Sandbox(files, root, "sandbox-1");

        sandbox.delete();

        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandbox-1"));
        verify(files).delete(path);
    }

    @Test
    public void testAddDownloadFile_SrcAndDst() throws URISyntaxException, OctopusIOException, OctopusException {
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));
        Sandbox sandbox = new Sandbox(mock(Files.class), path, "sandbox-1");

        AbsolutePath dst = new AbsolutePathImplementation(fs, new RelativePath("/tmp/outputfile"));
        sandbox.addDownloadFile("output", dst);

        List<Pair> uploadfiles = sandbox.getDownloadFiles();
        AbsolutePath src = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandbox-1/output"));
        assertEquals(1, uploadfiles.size());
        assertEquals(src, uploadfiles.get(0).source);
        assertEquals(dst, uploadfiles.get(0).destination);
    }

    @Test
    public void testAddDownloadFile_SrcNull_SrcSameFileName() throws URISyntaxException, OctopusIOException, OctopusException {
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandboxes"));
        Sandbox sandbox = new Sandbox(mock(Files.class), path, "sandbox-1");

        AbsolutePath dst = new AbsolutePathImplementation(fs, new RelativePath("/tmp/outputfile"));
        sandbox.addDownloadFile(null, dst);

        List<Pair> uploadfiles = sandbox.getDownloadFiles();
        AbsolutePath src = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandboxes/sandbox-1/outputfile"));
        assertEquals(1, uploadfiles.size());
        assertEquals(src, uploadfiles.get(0).source);
        assertEquals(dst, uploadfiles.get(0).destination);
    }

    @Test
    public void testAddDownloadFile_DstNull_NullPointerException() throws URISyntaxException, OctopusIOException,
            OctopusException {
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));
        Sandbox sandbox = new Sandbox(mock(Files.class), path, "sandbox-1");

        try {
            sandbox.addDownloadFile("output", null);
            fail("Expected an NullPointerException");
        } catch (NullPointerException e) {
            assertEquals("the destination path cannot be null when adding a postStaged file", e.getMessage());
        }
    }

    @Test
    public void testDownload() throws OctopusIOException, URISyntaxException, OctopusException {
        Files files = mock(Files.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));
        Sandbox sandbox = new Sandbox(files, path, "sandbox-1");
        AbsolutePath dst = new AbsolutePathImplementation(fs, new RelativePath("/tmp/outputfile"));
        sandbox.addDownloadFile("output", dst);

        sandbox.download();

        AbsolutePath src = new AbsolutePathImplementation(fs, new RelativePath("/tmp/sandbox-1/output"));
        verify(files).copy(src, dst);
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
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));

        Sandbox sandbox1 = new Sandbox(mock(Files.class), path, "sandbox-1");
        Sandbox sandbox2 = new Sandbox(mock(Files.class), path, "sandbox-1");

        assertNotEquals(sandbox1, sandbox2);
    }

    @Test
    public void testEquals_otherPath_notEqual() throws URISyntaxException, OctopusIOException, OctopusException {
        Files files = mock(Files.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);

        AbsolutePath path1 = new AbsolutePathImplementation(fs, new RelativePath("/tmp1"));
        AbsolutePath path2 = new AbsolutePathImplementation(fs, new RelativePath("/tmp2"));

        Sandbox sandbox1 = new Sandbox(files, path1, "sandbox-1");
        Sandbox sandbox2 = new Sandbox(files, path2, "sandbox-2");

        assertNotEquals(sandbox1, sandbox2);
    }

    @Test
    public void testEquals_otherUpload_notEqual() throws URISyntaxException, OctopusIOException, OctopusException {
        Files files = mock(Files.class);
        
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));

        Sandbox sandbox1 = new Sandbox(files, path, "sandbox-1");
        AbsolutePath src = new AbsolutePathImplementation(fs, new RelativePath("/tmp/inputfile"));
        sandbox1.addUploadFile(src);
        Sandbox sandbox2 = new Sandbox(files, path, "sandbox-1");

        assertNotEquals(sandbox1, sandbox2);
    }

    @Test
    public void testEquals_otherDownload_notEqual() throws URISyntaxException, OctopusIOException, OctopusException {
        Files files = mock(Files.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));

        Sandbox sandbox1 = new Sandbox(files, path, "sandbox-1");
        AbsolutePath src = new AbsolutePathImplementation(fs, new RelativePath("/tmp/outputfile"));
        sandbox1.addDownloadFile("outputfile", src);
        Sandbox sandbox2 = new Sandbox(files, path, "sandbox-1");

        assertNotEquals(sandbox1, sandbox2);
    }

    @Test
    public void testEquals_sameUpload_Equal() throws URISyntaxException, OctopusIOException, OctopusException {
        Files files = mock(Files.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new RelativePath(), null, null);
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("/tmp"));

        Sandbox sandbox1 = new Sandbox(files, path, "sandbox-1");
        AbsolutePath src = new AbsolutePathImplementation(fs, new RelativePath("/tmp/inputfile"));
        sandbox1.addUploadFile(src);
        Sandbox sandbox2 = new Sandbox(files, path, "sandbox-1");
        AbsolutePath src2 = new AbsolutePathImplementation(fs, new RelativePath("/tmp/inputfile"));
        sandbox2.addUploadFile(src2);

        assertEquals(sandbox1, sandbox2);
    }
}
