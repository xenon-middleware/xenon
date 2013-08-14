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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URISyntaxException;
import java.util.List;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.util.Sandbox.Pair;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RealSandboxTest {

    private static Octopus octopus;
    private static Files files;
    private static FileSystem fileSystem;

    private static AbsolutePath testDir;

    private static AbsolutePath testInput1;
    private static AbsolutePath testInput2;

    private static int counter = 0;

    @BeforeClass
    public static void prepare() throws Exception {
        octopus = OctopusFactory.newOctopus(null);
        files = octopus.files();
        fileSystem = files.getLocalCWDFileSystem();
        testDir = fileSystem.getEntryPath().resolve(new RelativePath("octopus_test_" + System.currentTimeMillis()));
        files.createDirectory(testDir);

        testInput1 = testDir.resolve(new RelativePath("input1"));
        testInput2 = testDir.resolve(new RelativePath("input2"));

        files.createFile(testInput1);
        files.createFile(testInput2);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        FileUtils.recursiveDelete(files, testDir);
        OctopusFactory.endOctopus(octopus);
    }

    public static synchronized String getNextSandbox() {
        return "sandbox-" + counter++;
    }

    @Test(expected = OctopusException.class)
    public void testSandbox_WithNullOctopus() throws URISyntaxException, OctopusIOException, OctopusException {
        // throws exception
        new Sandbox(null, testDir, getNextSandbox());
    }

    @Test(expected = OctopusException.class)
    public void testSandbox_WithNullPath() throws URISyntaxException, OctopusIOException, OctopusException {
        // throws exception
        new Sandbox(octopus, null, getNextSandbox());
    }

    @Test
    public void testSandbox_WithName() throws URISyntaxException, OctopusIOException, OctopusException {
        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(octopus, testDir, name);

        AbsolutePath expectedPath = testDir.resolve(new RelativePath(name));
        assertEquals(expectedPath, sandbox.getPath());
        assertEquals(0, sandbox.getUploadFiles().size());
        assertEquals(0, sandbox.getDownloadFiles().size());
    }

    @Test
    public void testSandbox_WithoutName() throws URISyntaxException, OctopusIOException, OctopusException {

        Sandbox sandbox = new Sandbox(octopus, testDir, null);

        String sandboxPath = sandbox.getPath().getPath();
        String tmp = testDir.getPath();

        assertTrue(sandboxPath.startsWith(tmp + "/octopus_sandbox_"));
    }

    @Test
    public void testAddUploadFile_SrcAndDst() throws URISyntaxException, OctopusIOException, OctopusException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(octopus, testDir, name);

        sandbox.addUploadFile(testInput1, "input");

        List<Pair> uploadfiles = sandbox.getUploadFiles();
        AbsolutePath dst = testDir.resolve(new RelativePath(name)).resolve(new RelativePath("input"));

        assertEquals(1, uploadfiles.size());
        assertEquals(testInput1, uploadfiles.get(0).source);
        assertEquals(dst, uploadfiles.get(0).destination);
    }

    @Test
    public void testSetUploadFiles() throws URISyntaxException, OctopusIOException, OctopusException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(octopus, testDir, name);

        sandbox.setUploadFiles(testInput1, testInput2);

        List<Pair> uploadfiles = sandbox.getUploadFiles();

        AbsolutePath sb = testDir.resolve(new RelativePath(name));
        AbsolutePath dst1 = sb.resolve(new RelativePath("input1"));
        AbsolutePath dst2 = sb.resolve(new RelativePath("input2"));

        assertEquals(2, uploadfiles.size());
        assertEquals(testInput1, uploadfiles.get(0).source);
        assertEquals(dst1, uploadfiles.get(0).destination);

        assertEquals(testInput2, uploadfiles.get(1).source);
        assertEquals(dst2, uploadfiles.get(1).destination);
    }

    @Test
    public void testAddUploadFile_DstNull_DstSameFileName() throws URISyntaxException, OctopusIOException, OctopusException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(octopus, testDir, name);

        sandbox.addUploadFile(testInput1, null);

        List<Pair> uploadfiles = sandbox.getUploadFiles();

        AbsolutePath dst = testDir.resolve(new RelativePath(name)).resolve(new RelativePath("input1"));

        assertEquals(1, uploadfiles.size());
        assertEquals(testInput1, uploadfiles.get(0).source);
        assertEquals(dst, uploadfiles.get(0).destination);
    }

    @Test
    public void testAddUploadFile_SrcNull_NullPointerException() throws URISyntaxException, OctopusIOException, OctopusException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(octopus, testDir, name);

        try {
            sandbox.addUploadFile(null);
            fail("Expected an NullPointerException");
        } catch (NullPointerException e) {
            assertEquals("the source path cannot be null when adding a preStaged file", e.getMessage());
        }
    }

    @Test
    public void testAddDownloadFile() throws URISyntaxException, OctopusIOException, OctopusException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(octopus, testDir, name);

        sandbox.addDownloadFile("output1", testInput1);

        List<Pair> downfiles = sandbox.getDownloadFiles();

        AbsolutePath sb = testDir.resolve(new RelativePath(name));
        AbsolutePath src = sb.resolve(new RelativePath("output1"));

        assertEquals(1, downfiles.size());
        assertEquals(src, downfiles.get(0).source);
        assertEquals(testInput1, downfiles.get(0).destination);
    }

    @Test
    public void testAddDownloadFileSrcNull() throws URISyntaxException, OctopusIOException, OctopusException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(octopus, testDir, name);

        sandbox.addDownloadFile(null, testInput1);

        List<Pair> downfiles = sandbox.getDownloadFiles();

        AbsolutePath sb = testDir.resolve(new RelativePath(name));
        AbsolutePath src = sb.resolve(new RelativePath("input1"));

        assertEquals(1, downfiles.size());
        assertEquals(src, downfiles.get(0).source);
        assertEquals(testInput1, downfiles.get(0).destination);
    }

    @Test(expected = NullPointerException.class)
    public void testAddDownloadFileDstNull() throws URISyntaxException, OctopusIOException, OctopusException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(octopus, testDir, name);

        // throws exception
        sandbox.addDownloadFile("output", null);
    }

    @Test
    public void testUploadDelete() throws OctopusIOException, OctopusException, URISyntaxException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(octopus, testDir, name);

        sandbox.addUploadFile(testInput1, "input");
        sandbox.upload(CopyOption.REPLACE);

        AbsolutePath dstDir = testDir.resolve(new RelativePath(name));
        AbsolutePath dstFile = dstDir.resolve(new RelativePath("input"));

        assertTrue(files.exists(dstDir));
        assertTrue(files.exists(dstFile));

        sandbox.delete();

        assertFalse(files.exists(dstDir));
        assertFalse(files.exists(dstFile));
    }

    @Test
    public void testUploadDownloadDelete() throws OctopusIOException, OctopusException, URISyntaxException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(octopus, testDir, name);

        AbsolutePath download = testDir.resolve(new RelativePath("download"));

        sandbox.addUploadFile(testInput1, "input");
        sandbox.addDownloadFile("input", download);

        sandbox.upload(CopyOption.REPLACE);

        AbsolutePath dstDir = testDir.resolve(new RelativePath(name));
        AbsolutePath dstFile = dstDir.resolve(new RelativePath("input"));

        assertTrue(files.exists(dstDir));
        assertTrue(files.exists(dstFile));

        sandbox.download(CopyOption.REPLACE);
        sandbox.delete();

        assertFalse(files.exists(dstDir));
        assertFalse(files.exists(dstFile));

        assertTrue(files.exists(download));

        files.delete(download);
    }

    @Test
    public void testDoubleUploadDelete() throws OctopusIOException, OctopusException, URISyntaxException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(octopus, testDir, name);

        sandbox.addUploadFile(testInput1, "input");

        sandbox.upload(CopyOption.REPLACE);

        AbsolutePath dstDir = testDir.resolve(new RelativePath(name));
        AbsolutePath dstFile = dstDir.resolve(new RelativePath("input"));

        assertTrue(files.exists(dstDir));
        assertTrue(files.exists(dstFile));

        sandbox.upload(CopyOption.REPLACE);

        sandbox.delete();

        assertFalse(files.exists(dstDir));
        assertFalse(files.exists(dstFile));
    }

    @Test
    public void testHashCode() throws URISyntaxException, OctopusIOException, OctopusException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(octopus, testDir, name);

        final int prime = 31;
        int result = 1;
        result = prime * result + octopus.hashCode();
        result = prime * result + testDir.resolve(new RelativePath(name)).hashCode();
        result = prime * result + sandbox.getUploadFiles().hashCode();
        result = prime * result + sandbox.getDownloadFiles().hashCode();

        assertEquals(result, sandbox.hashCode());
    }

    @Test
    public void testToString() throws URISyntaxException, OctopusIOException, OctopusException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(octopus, testDir, name);

        assertTrue(sandbox.toString().startsWith("Sandbox"));

        // NOTE: very hard to test toString, as it depends on the location from where the test is started. Fortunately, we don't 
        // really care what it prints, as it is for debugging anyway ;-)
    }

    @Test
    public void testEquals_sameObject_equal() throws URISyntaxException, OctopusIOException, OctopusException {
        Sandbox sandbox = new Sandbox(octopus, testDir, getNextSandbox());
        assertEquals(sandbox, sandbox);
    }

    @Test
    public void testEquals_otherClass_notEqual() throws URISyntaxException, OctopusIOException, OctopusException {
        Sandbox sandbox = new Sandbox(octopus, testDir, getNextSandbox());
        assertFalse(sandbox.equals(42));
    }

    @Test
    public void testEquals_otherNull_notEqual() throws URISyntaxException, OctopusIOException, OctopusException {
        Sandbox sandbox = new Sandbox(octopus, testDir, getNextSandbox());
        assertFalse(sandbox.equals(null));
    }

    @Test
    public void testEquals_otherOctopus_notEqual() throws URISyntaxException, OctopusIOException, OctopusException {

        Octopus octopus2 = OctopusFactory.newOctopus(null);

        String name = getNextSandbox();

        Sandbox sandbox1 = new Sandbox(octopus, testDir, name);
        Sandbox sandbox2 = new Sandbox(octopus2, testDir, name);

        assertNotEquals(sandbox1, sandbox2);

        OctopusFactory.endOctopus(octopus2);
    }

    @Test
    public void testEquals_otherPath_notEqual() throws URISyntaxException, OctopusIOException, OctopusException {

        AbsolutePath path = fileSystem.getEntryPath().resolve(new RelativePath("octopus_test_" + System.currentTimeMillis()));

        String name = getNextSandbox();

        Sandbox sandbox1 = new Sandbox(octopus, testDir, name);
        Sandbox sandbox2 = new Sandbox(octopus, path, name);

        assertNotEquals(sandbox1, sandbox2);
    }

    @Test
    public void testEquals_otherUpload_notEqual() throws URISyntaxException, OctopusIOException, OctopusException {

        String name = getNextSandbox();

        Sandbox sandbox1 = new Sandbox(octopus, testDir, name);
        Sandbox sandbox2 = new Sandbox(octopus, testDir, name);

        sandbox1.addUploadFile(testInput1);
        assertNotEquals(sandbox1, sandbox2);
    }

    @Test
    public void testEquals_otherDownload_notEqual() throws URISyntaxException, OctopusIOException, OctopusException {

        String name = getNextSandbox();

        Sandbox sandbox1 = new Sandbox(octopus, testDir, name);
        Sandbox sandbox2 = new Sandbox(octopus, testDir, name);

        sandbox1.addDownloadFile("output", testInput1);
        assertNotEquals(sandbox1, sandbox2);
    }

    @Test
    public void testEquals_sameUpload_Equal() throws URISyntaxException, OctopusIOException, OctopusException {

        String name = getNextSandbox();

        Sandbox sandbox1 = new Sandbox(octopus, testDir, name);
        Sandbox sandbox2 = new Sandbox(octopus, testDir, name);

        sandbox1.addUploadFile(testInput1);
        sandbox2.addUploadFile(testInput1);

        assertEquals(sandbox1, sandbox2);
    }

    @Test
    public void testPair_Equals() throws URISyntaxException, OctopusIOException, OctopusException {

        AbsolutePath src = testInput1;
        AbsolutePath dst = testInput2;

        Sandbox.Pair pair = new Sandbox.Pair(src, dst);
        Sandbox.Pair pair2 = new Sandbox.Pair(src, dst);
        Sandbox.Pair pair3 = new Sandbox.Pair(src, null);
        Sandbox.Pair pair4 = new Sandbox.Pair(null, dst);
        Sandbox.Pair pair5 = new Sandbox.Pair(src, null);
        Sandbox.Pair pair6 = new Sandbox.Pair(null, dst);

        assertTrue(pair.equals(pair));
        assertTrue(pair3.equals(pair5));
        assertTrue(pair4.equals(pair6));

        assertFalse(pair.equals(null));
        assertFalse(pair.equals("AAP"));

        assertTrue(pair.equals(pair2));

        assertFalse(pair.equals(pair3));
        assertFalse(pair.equals(pair4));

        assertFalse(pair3.equals(pair));
        assertFalse(pair4.equals(pair));
    }

    @Test
    public void testPair_hashCode() throws URISyntaxException, OctopusIOException, OctopusException {

        AbsolutePath src = testInput1;
        AbsolutePath dst = testInput2;

        Sandbox.Pair pair = new Sandbox.Pair(src, dst);
        Sandbox.Pair pair2 = new Sandbox.Pair(null, null);
        Sandbox.Pair pair3 = new Sandbox.Pair(src, null);
        Sandbox.Pair pair4 = new Sandbox.Pair(null, dst);

        final int prime = 31;
        int result = 1;
        result = prime * result + dst.hashCode();
        result = prime * result + src.hashCode();

        assertTrue(pair.hashCode() == result);

        result = 1;
        result = prime * result + 0;
        result = prime * result + src.hashCode();

        assertTrue(pair3.hashCode() == result);

        result = 1;
        result = prime * result + dst.hashCode();
        result = prime * result + 0;

        assertTrue(pair4.hashCode() == result);

        result = 1;
        result = prime * result + 0;
        result = prime * result + 0;

        assertTrue(pair2.hashCode() == result);
    }

    @Test
    public void testPair_toString() throws URISyntaxException, OctopusIOException, OctopusException {

        AbsolutePath src = testInput1;
        AbsolutePath dst = testInput2;

        Sandbox.Pair pair = new Sandbox.Pair(src, dst);

        // NOTE: bogus test. We don't really care what it prints exactly, as it is for debugging anyway ;-)
        assertTrue(pair.toString().startsWith("Pair"));
    }

}
