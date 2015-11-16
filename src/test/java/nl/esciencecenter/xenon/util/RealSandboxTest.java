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

package nl.esciencecenter.xenon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.List;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.files.CopyOption;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.util.Sandbox;
import nl.esciencecenter.xenon.util.Utils;
import nl.esciencecenter.xenon.util.Sandbox.Pair;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RealSandboxTest {

    private static Xenon xenon;
    private static Files files;
    private static FileSystem fileSystem;

    private static Path testDir;

    private static Path testInput1;
    private static Path testInput2;

    private static int counter = 0;

    @BeforeClass
    public static void prepare() throws Exception {
        xenon = XenonFactory.newXenon(null);
        files = xenon.files();
        
        Path cwd = Utils.getLocalCWD(files);
        
        fileSystem = cwd.getFileSystem();
        testDir = Utils.resolveWithRoot(files, cwd, "xenon_test_" + System.currentTimeMillis());
        files.createDirectory(testDir);

        testInput1 = Utils.resolveWithRoot(files, testDir, "input1");
        testInput2 = Utils.resolveWithRoot(files, testDir, "input2");

        files.createFile(testInput1);
        files.createFile(testInput2);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        Utils.recursiveDelete(files, testDir);
        XenonFactory.endXenon(xenon);
    }

    public static synchronized String getNextSandbox() {
        return "sandbox-" + counter++;
    }

    @Test(expected = XenonException.class)
    public void testSandbox_WithNullXenon() throws URISyntaxException, XenonException {
        // throws exception
        new Sandbox(null, testDir, getNextSandbox());
    }

    @Test(expected = XenonException.class)
    public void testSandbox_WithNullPath() throws URISyntaxException, XenonException {
        // throws exception
        new Sandbox(files, null, getNextSandbox());
    }

    @Test
    public void testSandbox_WithName() throws URISyntaxException, XenonException {
        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(files, testDir, name);

        Path expectedPath = Utils.resolveWithRoot(files, testDir, name);
        assertEquals(expectedPath, sandbox.getPath());
        assertEquals(0, sandbox.getUploadFiles().size());
        assertEquals(0, sandbox.getDownloadFiles().size());
    }

    @Test
    public void testSandbox_WithoutName() throws URISyntaxException, XenonException {

        Sandbox sandbox = new Sandbox(files, testDir, null);

        String sandboxPath = sandbox.getPath().getRelativePath().getAbsolutePath();
        String tmp = testDir.getRelativePath().getAbsolutePath();
        
        assertTrue(sandboxPath.startsWith(tmp + testDir.getRelativePath().getSeparator() + "xenon_sandbox_"));
    }

    @Test
    public void testAddUploadFile_SrcAndDst() throws URISyntaxException, XenonException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(files, testDir, name);

        sandbox.addUploadFile(testInput1, "input");

        List<Pair> uploadfiles = sandbox.getUploadFiles();
        Path dst = Utils.resolveWithRoot(files, testDir, name, "input");

        assertEquals(1, uploadfiles.size());
        assertEquals(testInput1, uploadfiles.get(0).getSource());
        assertEquals(dst, uploadfiles.get(0).getDestination());
    }

    @Test
    public void testSetUploadFiles() throws URISyntaxException, XenonException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(files, testDir, name);

        sandbox.setUploadFiles(testInput1, testInput2);

        List<Pair> uploadfiles = sandbox.getUploadFiles();

        Path sb = Utils.resolveWithRoot(files, testDir, name);
        Path dst1 = Utils.resolveWithRoot(files, sb, "input1");
        Path dst2 = Utils.resolveWithRoot(files, sb, "input2");

        assertEquals(2, uploadfiles.size());
        assertEquals(testInput1, uploadfiles.get(0).getSource());
        assertEquals(dst1, uploadfiles.get(0).getDestination());

        assertEquals(testInput2, uploadfiles.get(1).getSource());
        assertEquals(dst2, uploadfiles.get(1).getDestination());
    }

    @Test
    public void testAddUploadFile_DstNull_DstSameFileName() throws URISyntaxException, XenonException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(files, testDir, name);

        sandbox.addUploadFile(testInput1, null);

        List<Pair> uploadfiles = sandbox.getUploadFiles();

        Path dst = Utils.resolveWithRoot(files, testDir, name, "input1");

        assertEquals(1, uploadfiles.size());
        assertEquals(testInput1, uploadfiles.get(0).getSource());
        assertEquals(dst, uploadfiles.get(0).getDestination());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddUploadFile_SrcNull_NullPointerException() throws URISyntaxException, XenonException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(files, testDir, name);

        sandbox.addUploadFile(null);
    }

    @Test
    public void testAddDownloadFile() throws URISyntaxException, XenonException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(files, testDir, name);

        sandbox.addDownloadFile("output1", testInput1);

        List<Pair> downfiles = sandbox.getDownloadFiles();

        Path src = Utils.resolveWithRoot(files, testDir, name, "output1");

        assertEquals(1, downfiles.size());
        assertEquals(src, downfiles.get(0).getSource());
        assertEquals(testInput1, downfiles.get(0).getDestination());
    }

    @Test
    public void testAddDownloadFileSrcNull() throws URISyntaxException, XenonException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(files, testDir, name);

        sandbox.addDownloadFile(null, testInput1);

        List<Pair> downfiles = sandbox.getDownloadFiles();

        Path src = Utils.resolveWithRoot(files, testDir, name, "input1");
        
        assertEquals(1, downfiles.size());
        assertEquals(src, downfiles.get(0).getSource());
        assertEquals(testInput1, downfiles.get(0).getDestination());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDownloadFileDstNull() throws URISyntaxException, XenonException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(files, testDir, name);

        // throws exception
        sandbox.addDownloadFile("output", null);
    }

    @Test
    public void testUploadDelete() throws XenonException, URISyntaxException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(files, testDir, name);

        sandbox.addUploadFile(testInput1, "input");
        sandbox.upload(CopyOption.REPLACE);

        Path dstDir = Utils.resolveWithRoot(files, testDir, name);
        Path dstFile = Utils.resolveWithRoot(files, dstDir, "input");

        assertTrue(files.exists(dstDir));
        assertTrue(files.exists(dstFile));

        sandbox.delete();

        assertFalse(files.exists(dstDir));
        assertFalse(files.exists(dstFile));
    }

    @Test
    public void testUploadDownloadDelete() throws XenonException, URISyntaxException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(files, testDir, name);

        Path download = Utils.resolveWithRoot(files, testDir, "download");

        sandbox.addUploadFile(testInput1, "input");
        sandbox.addDownloadFile("input", download);

        sandbox.upload(CopyOption.REPLACE);

        Path dstDir = Utils.resolveWithRoot(files, testDir, name);
        Path dstFile = Utils.resolveWithRoot(files, dstDir, "input");

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
    public void testDoubleUploadDelete() throws XenonException, URISyntaxException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(files, testDir, name);

        sandbox.addUploadFile(testInput1, "input");

        sandbox.upload(CopyOption.REPLACE);

        Path dstDir = Utils.resolveWithRoot(files, testDir, name);
        Path dstFile = Utils.resolveWithRoot(files, dstDir, "input");

        assertTrue(files.exists(dstDir));
        assertTrue(files.exists(dstFile));

        sandbox.upload(CopyOption.REPLACE);

        sandbox.delete();

        assertFalse(files.exists(dstDir));
        assertFalse(files.exists(dstFile));
    }

    @Test
    public void testHashCode() throws URISyntaxException, XenonException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(files, testDir, name);

        final int prime = 31;
        int result = 1;
        result = prime * result + files.hashCode();
        result = prime * result + Utils.resolveWithRoot(files, testDir, name).hashCode();
        result = prime * result + sandbox.getUploadFiles().hashCode();
        result = prime * result + sandbox.getDownloadFiles().hashCode();

        assertEquals(result, sandbox.hashCode());
    }

    @Test
    public void testToString() throws URISyntaxException, XenonException {

        String name = getNextSandbox();
        Sandbox sandbox = new Sandbox(files, testDir, name);

        assertTrue(sandbox.toString().startsWith("Sandbox"));

        // NOTE: very hard to test toString, as it depends on the location from where the test is started. Fortunately, we don't 
        // really care what it prints, as it is for debugging anyway ;-)
    }

    @Test
    public void testEquals_sameObject_equal() throws URISyntaxException, XenonException {
        Sandbox sandbox = new Sandbox(files, testDir, getNextSandbox());
        assertEquals(sandbox, sandbox);
    }

    @Test
    public void testEquals_otherClass_notEqual() throws URISyntaxException, XenonException {
        Sandbox sandbox = new Sandbox(files, testDir, getNextSandbox());
        assertFalse(sandbox.equals(42));
    }

    @Test
    public void testEquals_otherNull_notEqual() throws URISyntaxException, XenonException {
        Sandbox sandbox = new Sandbox(files, testDir, getNextSandbox());
        assertFalse(sandbox.equals(null));
    }

    @Test
    public void testEquals_otherXenon_notEqual() throws URISyntaxException, XenonException {

        Xenon xenon2 = XenonFactory.newXenon(null);

        String name = getNextSandbox();

        Sandbox sandbox1 = new Sandbox(files, testDir, name);
        Sandbox sandbox2 = new Sandbox(xenon2.files(), testDir, name);

        assertNotEquals(sandbox1, sandbox2);

        XenonFactory.endXenon(xenon2);
    }

    @Test
    public void testEquals_otherPath_notEqual() throws URISyntaxException, XenonException {

        Path path = Utils.resolveWithEntryPath(files, fileSystem, "xenon_test_" + System.currentTimeMillis());

        String name = getNextSandbox();

        Sandbox sandbox1 = new Sandbox(files, testDir, name);
        Sandbox sandbox2 = new Sandbox(files, path, name);

        assertNotEquals(sandbox1, sandbox2);
    }

    @Test
    public void testEquals_otherUpload_notEqual() throws URISyntaxException, XenonException {

        String name = getNextSandbox();

        Sandbox sandbox1 = new Sandbox(files, testDir, name);
        Sandbox sandbox2 = new Sandbox(files, testDir, name);

        sandbox1.addUploadFile(testInput1);
        assertNotEquals(sandbox1, sandbox2);
    }

    @Test
    public void testEquals_otherDownload_notEqual() throws URISyntaxException, XenonException {

        String name = getNextSandbox();

        Sandbox sandbox1 = new Sandbox(files, testDir, name);
        Sandbox sandbox2 = new Sandbox(files, testDir, name);

        sandbox1.addDownloadFile("output", testInput1);
        assertNotEquals(sandbox1, sandbox2);
    }

    @Test
    public void testEquals_sameUpload_Equal() throws URISyntaxException, XenonException {

        String name = getNextSandbox();

        Sandbox sandbox1 = new Sandbox(files, testDir, name);
        Sandbox sandbox2 = new Sandbox(files, testDir, name);

        sandbox1.addUploadFile(testInput1);
        sandbox2.addUploadFile(testInput1);

        assertEquals(sandbox1, sandbox2);
    }

    @Test
    public void testPair_Equals() throws URISyntaxException, XenonException {

        Path src = testInput1;
        Path dst = testInput2;

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
    public void testPair_hashCode() throws URISyntaxException, XenonException {

        Path src = testInput1;
        Path dst = testInput2;

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
        //noinspection PointlessArithmeticExpression
        result = prime * result + 0;
        result = prime * result + src.hashCode();

        assertTrue(pair3.hashCode() == result);

        result = 1;
        result = prime * result + dst.hashCode();
        //noinspection PointlessArithmeticExpression
        result = prime * result + 0;

        assertTrue(pair4.hashCode() == result);

        result = 1;
        //noinspection PointlessArithmeticExpression
        result = prime * result + 0;
        //noinspection PointlessArithmeticExpression
        result = prime * result + 0;

        assertTrue(pair2.hashCode() == result);
    }

    @Test
    public void testPair_toString() throws URISyntaxException, XenonException {

        Path src = testInput1;
        Path dst = testInput2;

        Sandbox.Pair pair = new Sandbox.Pair(src, dst);

        // NOTE: bogus test. We don't really care what it prints exactly, as it is for debugging anyway ;-)
        assertTrue(pair.toString().startsWith("Pair"));
    }

}
