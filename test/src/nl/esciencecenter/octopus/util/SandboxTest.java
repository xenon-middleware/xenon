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

import nl.esciencecenter.octopus.engine.files.PathImplementation;
import nl.esciencecenter.octopus.engine.files.FileSystemImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.Pathname;
import nl.esciencecenter.octopus.util.Sandbox.Pair;

import org.junit.Test;

public class SandboxTest {

    /*
    private Sandbox sampleSandbox() throws URISyntaxException, OctopusException, OctopusIOException {

        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new Pathname(), null, null);
        Path path = new PathImplementation(fs, new Pathname("/tmp"));
        Sandbox sandbox = new Sandbox(mock(Files.class), path, "sandbox-1");
        sandbox.addUploadFile(new PathImplementation(fs, new Pathname("/tmp/inputfile")));
        sandbox.addDownloadFile("outputfile", new PathImplementation(fs, new Pathname("/tmp/outputfile")));
        return sandbox;
    }
    */
    
    @Test(expected = OctopusException.class)
    public void testSandbox_WithNullOctopus() throws URISyntaxException, OctopusIOException, OctopusException {
        // throws exception
        new Sandbox(null, mock(Path.class), "sandbox-1");
    }

    @Test(expected = OctopusException.class)
    public void testSandbox_WithNullPath() throws URISyntaxException, OctopusIOException, OctopusException {
        // throws exception
        new Sandbox(mock(Files.class), null, "sandbox-1");
    }
    /*
    @Test
    public void testSandbox_WithName() throws URISyntaxException, OctopusIOException, OctopusException {
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new Pathname(), null, null);
        Path path = new PathImplementation(fs, new Pathname("/tmp"));

        Sandbox sandbox = new Sandbox(mock(Files.class), path, "sandbox-1");

        Path expectedPath = new PathImplementation(fs, new Pathname("/tmp/sandbox-1"));
        assertEquals(expectedPath, sandbox.getPath());
        assertEquals(0, sandbox.getUploadFiles().size());
        assertEquals(0, sandbox.getDownloadFiles().size());
    }

    @Test
    public void testSandbox_WithoutName() throws URISyntaxException, OctopusIOException, OctopusException {
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new Pathname(), null, null);
        Path path = new PathImplementation(fs, new Pathname("/tmp"));

        Sandbox sandbox = new Sandbox(mock(Files.class), path, null);

        String sandboxPath = sandbox.getPath().getPathname().getAbsolutePath();
        assertTrue(sandboxPath.startsWith("/tmp/octopus_sandbox_"));
    }

    @Test
    public void testAddUploadFile_SrcAndDst() throws URISyntaxException, OctopusIOException, OctopusException {
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new Pathname(), null, null);
        Path path = new PathImplementation(fs, new Pathname("/tmp"));
        Sandbox sandbox = new Sandbox(mock(Files.class), path, "sandbox-1");

        Path src = new PathImplementation(fs, new Pathname("/tmp/inputfile"));
        sandbox.addUploadFile(src, "input");

        List<Pair> uploadfiles = sandbox.getUploadFiles();
        Path dst = new PathImplementation(fs, new Pathname("/tmp/sandbox-1/input"));
        assertEquals(1, uploadfiles.size());
        assertEquals(src, uploadfiles.get(0).getSource());
        assertEquals(dst, uploadfiles.get(0).getDestination());
    }

    @Test
    public void testSetUploadFiles() throws URISyntaxException, OctopusIOException, OctopusException {
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new Pathname(), null, null);
        Path path = new PathImplementation(fs, new Pathname("/tmp"));
        Sandbox sandbox = new Sandbox(mock(Files.class), path, "sandbox-1");

        Path src1 = new PathImplementation(fs, new Pathname("/tmp/input1"));
        Path src2 = new PathImplementation(fs, new Pathname("/tmp/input2"));

        sandbox.setUploadFiles(src1, src2);

        List<Pair> uploadfiles = sandbox.getUploadFiles();

        Path dst1 = new PathImplementation(fs, new Pathname("/tmp/sandbox-1/input1"));
        Path dst2 = new PathImplementation(fs, new Pathname("/tmp/sandbox-1/input2"));

        assertEquals(2, uploadfiles.size());
        assertEquals(src1, uploadfiles.get(0).getSource());
        assertEquals(dst1, uploadfiles.get(0).getDestination());

        assertEquals(src2, uploadfiles.get(1).getSource());
        assertEquals(dst2, uploadfiles.get(1).getDestination());
    }

    @Test
    public void testAddUploadFile_DstNull_DstSameFileName() throws URISyntaxException, OctopusIOException, OctopusException {
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new Pathname(), null, null);
        Path path = new PathImplementation(fs, new Pathname("/tmp/sandboxes"));
        Sandbox sandbox = new Sandbox(mock(Files.class), path, "sandbox-1");

        Path src = new PathImplementation(fs, new Pathname("/tmp/inputfile"));
        sandbox.addUploadFile(src, null);

        List<Pair> uploadfiles = sandbox.getUploadFiles();
        Path dst = new PathImplementation(fs, new Pathname("/tmp/sandboxes/sandbox-1/inputfile"));
        assertEquals(1, uploadfiles.size());
        assertEquals(src, uploadfiles.get(0).getSource());
        assertEquals(dst, uploadfiles.get(0).getDestination());
    }

    @Test
    public void testAddUploadFile_SrcNull_NullPointerException() throws URISyntaxException, OctopusIOException, OctopusException {
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new Pathname(), null, null);
        Path path = new PathImplementation(fs, new Pathname("/tmp"));
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
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new Pathname(), null, null);
        Path path = new PathImplementation(fs, new Pathname("/tmp"));
        Sandbox sandbox = new Sandbox(files, path, "sandbox-1");
        Path src = new PathImplementation(fs, new Pathname("/tmp/inputfile"));
        sandbox.addUploadFile(src, "input");

        sandbox.upload();

        Path sandboxDir = new PathImplementation(fs, new Pathname("/tmp/sandbox-1"));
        verify(files).createDirectory(sandboxDir);
        Path dst = new PathImplementation(fs, new Pathname("/tmp/sandbox-1/input"));
        verify(files).copy(src, dst);
    }

    @Test
    public void testUpload_SandboxDirExists_Copy() throws OctopusIOException, OctopusException, URISyntaxException {
        Files files = mock(Files.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new Pathname(), null, null);
        Path path = new PathImplementation(fs, new Pathname("/tmp"));
        Sandbox sandbox = new Sandbox(files, path, "sandbox-1");
        Path sandboxDir = new PathImplementation(fs, new Pathname("/tmp/sandbox-1"));
        when(files.exists(sandboxDir)).thenReturn(true);
        Path src = new PathImplementation(fs, new Pathname("/tmp/inputfile"));
        sandbox.addUploadFile(src, "input");

        sandbox.upload();

        verify(files, never()).createDirectory(sandboxDir);
        Path dst = new PathImplementation(fs, new Pathname("/tmp/sandbox-1/input"));
        verify(files).copy(src, dst);
    }

    @Test
    public void testDelete() throws URISyntaxException, OctopusIOException, OctopusException {
        Files files = mock(Files.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new Pathname(), null, null);
        Path root = new PathImplementation(fs, new Pathname("/tmp"));
        Sandbox sandbox = new Sandbox(files, root, "sandbox-1");

        sandbox.delete();

        Path path = new PathImplementation(fs, new Pathname("/tmp/sandbox-1"));
        verify(files).delete(path);
    }

    @Test
    public void testAddDownloadFile_SrcAndDst() throws URISyntaxException, OctopusIOException, OctopusException {
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new Pathname(), null, null);
        Path path = new PathImplementation(fs, new Pathname("/tmp"));
        Sandbox sandbox = new Sandbox(mock(Files.class), path, "sandbox-1");

        Path dst = new PathImplementation(fs, new Pathname("/tmp/outputfile"));
        sandbox.addDownloadFile("output", dst);

        List<Pair> uploadfiles = sandbox.getDownloadFiles();
        Path src = new PathImplementation(fs, new Pathname("/tmp/sandbox-1/output"));
        assertEquals(1, uploadfiles.size());
        assertEquals(src, uploadfiles.get(0).getSource());
        assertEquals(dst, uploadfiles.get(0).getDestination());
    }

    @Test
    public void testAddDownloadFile_SrcNull_SrcSameFileName() throws URISyntaxException, OctopusIOException, OctopusException {
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new Pathname(), null, null);
        Path path = new PathImplementation(fs, new Pathname("/tmp/sandboxes"));
        Sandbox sandbox = new Sandbox(mock(Files.class), path, "sandbox-1");

        Path dst = new PathImplementation(fs, new Pathname("/tmp/outputfile"));
        sandbox.addDownloadFile(null, dst);

        List<Pair> uploadfiles = sandbox.getDownloadFiles();
        Path src = new PathImplementation(fs, new Pathname("/tmp/sandboxes/sandbox-1/outputfile"));
        assertEquals(1, uploadfiles.size());
        assertEquals(src, uploadfiles.get(0).getSource());
        assertEquals(dst, uploadfiles.get(0).getDestination());
    }

    @Test
    public void testAddDownloadFile_DstNull_NullPointerException() throws URISyntaxException, OctopusIOException,
            OctopusException {
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new Pathname(), null, null);
        Path path = new PathImplementation(fs, new Pathname("/tmp"));
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
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new Pathname(), null, null);
        Pathname tmpDir = new Pathname("/tmp");
        Path path = new PathImplementation(fs, tmpDir);
        Sandbox sandbox = new Sandbox(files, path, "sandbox-1");
        Path dst = new PathImplementation(fs, new Pathname("/tmp/outputfile"));
        
        when(files.newPath(path.getFileSystem(), path.getPathname())).thenReturn(dst);
        
        sandbox.addDownloadFile("output", dst);

        sandbox.download();

        Path src = new PathImplementation(fs, new Pathname("/tmp/sandbox-1/output"));
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
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new Pathname(), null, null);
        Path path = new PathImplementation(fs, new Pathname("/tmp"));

        Sandbox sandbox1 = new Sandbox(mock(Files.class), path, "sandbox-1");
        Sandbox sandbox2 = new Sandbox(mock(Files.class), path, "sandbox-1");

        assertNotEquals(sandbox1, sandbox2);
    }

    @Test
    public void testEquals_otherPath_notEqual() throws URISyntaxException, OctopusIOException, OctopusException {
        Files files = mock(Files.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new Pathname(), null, null);

        Path path1 = new PathImplementation(fs, new Pathname("/tmp1"));
        Path path2 = new PathImplementation(fs, new Pathname("/tmp2"));

        Sandbox sandbox1 = new Sandbox(files, path1, "sandbox-1");
        Sandbox sandbox2 = new Sandbox(files, path2, "sandbox-2");

        assertNotEquals(sandbox1, sandbox2);
    }

    @Test
    public void testEquals_otherUpload_notEqual() throws URISyntaxException, OctopusIOException, OctopusException {
        Files files = mock(Files.class);
        
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new Pathname(), null, null);
        Path path = new PathImplementation(fs, new Pathname("/tmp"));

        Sandbox sandbox1 = new Sandbox(files, path, "sandbox-1");
        Path src = new PathImplementation(fs, new Pathname("/tmp/inputfile"));
        sandbox1.addUploadFile(src);
        Sandbox sandbox2 = new Sandbox(files, path, "sandbox-1");

        assertNotEquals(sandbox1, sandbox2);
    }

    @Test
    public void testEquals_otherDownload_notEqual() throws URISyntaxException, OctopusIOException, OctopusException {
        Files files = mock(Files.class);
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new Pathname(), null, null);
        Path path = new PathImplementation(fs, new Pathname("/tmp"));

        Sandbox sandbox1 = new Sandbox(files, path, "sandbox-1");
        Path src = new PathImplementation(fs, new Pathname("/tmp/outputfile"));
        sandbox1.addDownloadFile("outputfile", src);
        Sandbox sandbox2 = new Sandbox(files, path, "sandbox-1");

        assertNotEquals(sandbox1, sandbox2);
    }

    @Test
    public void testEquals_sameUpload_Equal() throws URISyntaxException, OctopusIOException, OctopusException {
        Files files = mock(Files.class);              
        FileSystem fs = new FileSystemImplementation("local", "local-0", new URI("file:///"), new Pathname(), null, null);
        Path path = new PathImplementation(fs, new Pathname("/tmp"));
        Path path2 = new PathImplementation(fs, new Pathname("/tmp/sandbox-1"));
        when(files.newPath(fs, path2.getPathname())).thenReturn(path2);
        
        Sandbox sandbox1 = new Sandbox(files, path, "sandbox-1");
        Path src = new PathImplementation(fs, new Pathname("/tmp/inputfile"));
        sandbox1.addUploadFile(src);
        
        Sandbox sandbox2 = new Sandbox(files, path, "sandbox-1");
        Path src2 = new PathImplementation(fs, new Pathname("/tmp/inputfile"));
        sandbox2.addUploadFile(src2);

        assertEquals(sandbox1, sandbox2);
    }
    */
}
