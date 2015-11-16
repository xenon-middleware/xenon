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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.util.FileVisitResult;
import nl.esciencecenter.xenon.util.FileVisitor;
import nl.esciencecenter.xenon.util.Utils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RealFileUtilsTest {

    public static final String ROOT = "xenon_RealFileUtilsTest_" + System.currentTimeMillis();

    public static Xenon xenon;
    public static Files files;
    public static FileSystem fileSystem;

    public static Path testDir;

    @BeforeClass
    public static void prepare() throws Exception {

        xenon = XenonFactory.newXenon(null);

        files = xenon.files();
        
        Path cwd = Utils.getLocalCWD(files);
        
        fileSystem = cwd.getFileSystem();

        testDir = files.newPath(fileSystem, cwd.getRelativePath().resolve(ROOT));
        files.createDirectory(testDir);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        files.delete(testDir);
        XenonFactory.endXenon(xenon);
    }

    @Test
    public void test_copyFromInputStream1() throws Exception {

        String message = "Hello World!";

        Path testFile = Utils.resolveWithRoot(files, testDir, "test_copyFromInputStream1.txt");

        Utils.copy(files, new ByteArrayInputStream(message.getBytes()), testFile, true);

        byte[] tmp = Utils.readAllBytes(files, testFile);

        assertNotNull(tmp);
        assertTrue(tmp.length > 0);
        assertTrue(message.equals(new String(tmp)));

        files.delete(testFile);
    }

    @Test
    public void test_copyFromInputStream2() throws Exception {

        String message = "Hello World!";

        Path testFile = Utils.resolveWithRoot(files, testDir, "test_copyFromInputStream2.txt");

        Utils.copy(files, new ByteArrayInputStream(message.getBytes()), testFile, true);
        Utils.copy(files, new ByteArrayInputStream(message.getBytes()), testFile, true);

        byte[] tmp = Utils.readAllBytes(files, testFile);

        assertNotNull(tmp);
        assertTrue(tmp.length > 0);
        assertTrue(message.equals(new String(tmp)));

        files.delete(testFile);
    }

    @Test
    public void test_copyFromInputStream3() throws Exception {

        String message = "Hello World!";

        Path testFile = Utils.resolveWithRoot(files, testDir, "test_copyFromInputStream3.txt");

        Utils.copy(files, new ByteArrayInputStream(message.getBytes()), testFile, true);
        Utils.copy(files, new ByteArrayInputStream(message.getBytes()), testFile, false);

        byte[] tmp = Utils.readAllBytes(files, testFile);

        assertNotNull(tmp);
        assertTrue(tmp.length > 0);
        assertTrue(new String(tmp).equals(message + message));

        files.delete(testFile);
    }

    @Test
    public void test_copyToOutputStream1() throws Exception {

        String message = "Hello World!";

        Path testFile = Utils.resolveWithRoot(files, testDir, "test_copyToOutputStream1.txt");

        Utils.write(files, testFile, message.getBytes(), true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Utils.copy(files, testFile, out);

        byte[] tmp = out.toByteArray();

        assertNotNull(tmp);
        assertTrue(tmp.length > 0);
        assertTrue(new String(tmp).equals(message));

        files.delete(testFile);
    }

    @Test
    public void test_copyToOutputStream2() throws Exception {

        String message = "Hello World!";

        Path testFile = Utils.resolveWithRoot(files, testDir, "test_copyToOutputStream2.txt");

        Utils.write(files, testFile, message.getBytes(), true);
        Utils.write(files, testFile, message.getBytes(), true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Utils.copy(files, testFile, out);

        byte[] tmp = out.toByteArray();

        assertNotNull(tmp);
        assertTrue(tmp.length > 0);
        assertTrue(new String(tmp).equals(message));

        files.delete(testFile);
    }

    @Test
    public void test_copyToOutputStream3() throws Exception {

        String message = "Hello World!";

        Path testFile = Utils.resolveWithRoot(files, testDir, "test_copyToOutputStream3.txt");

        Utils.write(files, testFile, message.getBytes(), true);
        Utils.write(files, testFile, message.getBytes(), false);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Utils.copy(files, testFile, out);

        byte[] tmp = out.toByteArray();

        assertNotNull(tmp);
        assertTrue(tmp.length > 0);
        assertTrue(new String(tmp).equals(message + message));

        files.delete(testFile);
    }

    @Test
    public void test_newBufferedWriter1() throws Exception {

        String message = "Hello World!";

        Path testFile = Utils.resolveWithRoot(files, testDir, "test_newBufferedWriter1.txt");

        BufferedWriter bw = Utils.newBufferedWriter(files, testFile, Charset.defaultCharset(), true);
        bw.write(message);
        bw.close();

        byte[] tmp = Utils.readAllBytes(files, testFile);

        assertNotNull(tmp);
        assertTrue(tmp.length > 0);
        assertTrue(new String(tmp).equals(message));

        files.delete(testFile);
    }

    @Test
    public void test_newBufferedWriter2() throws Exception {

        String message = "Hello World!";

        Path testFile = Utils.resolveWithRoot(files, testDir, "test_newBufferedWriter2.txt");

        BufferedWriter bw = Utils.newBufferedWriter(files, testFile, Charset.defaultCharset(), true);
        bw.write(message);
        bw.close();

        bw = Utils.newBufferedWriter(files, testFile, Charset.defaultCharset(), true);
        bw.write(message);
        bw.close();

        byte[] tmp = Utils.readAllBytes(files, testFile);

        assertNotNull(tmp);
        assertTrue(tmp.length > 0);
        assertTrue(new String(tmp).equals(message));

        files.delete(testFile);
    }

    @Test
    public void test_newBufferedWriter3() throws Exception {

        String message = "Hello World!";

        Path testFile = Utils.resolveWithRoot(files, testDir, "test_newBufferedWriter3.txt");

        BufferedWriter bw = Utils.newBufferedWriter(files, testFile, Charset.defaultCharset(), true);
        bw.write(message);
        bw.close();

        bw = Utils.newBufferedWriter(files, testFile, Charset.defaultCharset(), false);
        bw.write(message);
        bw.close();

        byte[] tmp = Utils.readAllBytes(files, testFile);

        assertNotNull(tmp);
        assertTrue(tmp.length > 0);
        assertTrue(new String(tmp).equals(message + message));

        files.delete(testFile);
    }

    @Test
    public void test_newBufferedReader1() throws Exception {

        String message = "Hello World!";

        Path testFile = Utils.resolveWithRoot(files, testDir, "test_newBufferedReader1.txt");

        Utils.write(files, testFile, message.getBytes(), true);

        BufferedReader br = Utils.newBufferedReader(files, testFile, Charset.defaultCharset());

        String tmp = br.readLine();
        br.close();

        assertNotNull(tmp);
        assertTrue(tmp.equals(message));

        files.delete(testFile);
    }

    @Test
    public void test_newBufferedReader2() throws Exception {

        String message = "Hello World!\n";

        Path testFile = Utils.resolveWithRoot(files, testDir, "test_newBufferedReader2.txt");

        Utils.write(files, testFile, message.getBytes(), true);
        Utils.write(files, testFile, message.getBytes(), false);

        BufferedReader br = Utils.newBufferedReader(files, testFile, Charset.defaultCharset());

        String tmp1 = br.readLine();
        String tmp2 = br.readLine();
        br.close();

        assertNotNull(tmp1);
        assertNotNull(tmp2);

        assertTrue(tmp1.equals("Hello World!"));
        assertTrue(tmp2.equals("Hello World!"));

        files.delete(testFile);
    }

    @Test
    public void test_readAllLines1() throws Exception {

        Path testFile = Utils.resolveWithRoot(files, testDir, "test_readAllLines1.txt");

        BufferedWriter bw = Utils.newBufferedWriter(files, testFile, Charset.defaultCharset(), true);
        bw.write("line1\n");
        bw.write("line2\n");
        bw.write("line3\n");
        bw.write("line4\n");
        bw.close();

        List<String> tmp = Utils.readAllLines(files, testFile, Charset.defaultCharset());

        assertNotNull(tmp);
        assertTrue(tmp.size() == 4);
        assertTrue(tmp.get(0).equals("line1"));
        assertTrue(tmp.get(1).equals("line2"));
        assertTrue(tmp.get(2).equals("line3"));
        assertTrue(tmp.get(3).equals("line4"));

        files.delete(testFile);
    }

    @Test
    public void test_write1() throws Exception {

        Path testFile = Utils.resolveWithRoot(files, testDir, "test_write1.txt");

        LinkedList<String> tmp1 = new LinkedList<>();
        tmp1.add("line1");
        tmp1.add("line2");
        tmp1.add("line3");
        tmp1.add("line4");

        Utils.write(files, testFile, tmp1, Charset.defaultCharset(), true);

        List<String> tmp2 = Utils.readAllLines(files, testFile, Charset.defaultCharset());

        System.err.println("Read " + tmp2.size() + " lines");

        assertNotNull(tmp2);
        assertTrue(tmp2.size() == 4);
        assertTrue(tmp2.get(0).equals("line1"));
        assertTrue(tmp2.get(1).equals("line2"));
        assertTrue(tmp2.get(2).equals("line3"));
        assertTrue(tmp2.get(3).equals("line4"));

        files.delete(testFile);
    }

    @SuppressWarnings("CanBeFinal")
    class MyFileVisitor implements FileVisitor {

        final Path[] dirs;
        final Path[] files;

        MyFileVisitor(Path[] dirs, Path[] files) {
            this.dirs = dirs;
            this.files = files;
        }

        private void check(Path[] avail, Path path) throws XenonException {
            for (Path option : avail) {
                if (path.equals(option)) {
                    return;
                }
            }

            throw new XenonException("", "Unexpected path: " + path);
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, XenonException exception, Files files)
                throws XenonException {
            check(dirs, dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, FileAttributes attributes, Files files)
                throws XenonException {

            check(dirs, dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, FileAttributes attributes, Files f) throws XenonException {

            check(files, file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, XenonException exception, Files files)
                throws XenonException {
            throw new XenonException("", "Visit failed of path: " + file);
        }

    }

    @Test
    public void test_walkFileTree1() throws Exception {

        Path[] dirs = new Path[2];

        dirs[0] = Utils.resolveWithRoot(files, testDir, "test_walkFileTree1");
        dirs[1] = Utils.resolveWithRoot(files, dirs[0], "dir0");

        files.createDirectories(dirs[1]);

        Path[] tmp = new Path[10];

        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = Utils.resolveWithRoot(files, dirs[1], "file" + i);
            files.createFile(tmp[i]);
        }

        FileVisitor fv = new MyFileVisitor(dirs, tmp);
        Utils.walkFileTree(files, dirs[0], fv);

        for (int i = 0; i < 10; i++) {
            files.delete(tmp[i]);
        }

        files.delete(dirs[1]);
        files.delete(dirs[0]);
    }

    class MyFileVisitor2 implements FileVisitor {

        @Override
        public FileVisitResult postVisitDirectory(Path dir, XenonException exception, Files files)
                throws XenonException {
            throw new XenonException("", "Unexpected visit of path: " + dir);
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, FileAttributes attributes, Files files)
                throws XenonException {
            return FileVisitResult.TERMINATE;
        }

        @Override
        public FileVisitResult visitFile(Path file, FileAttributes attributes, Files files) throws XenonException {
            throw new XenonException("", "Unexpected visit of path: " + file);
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, XenonException exception, Files files)
                throws XenonException {
            throw new XenonException("", "Visit failed of path: " + file);
        }
    }

    @Test
    public void test_walkFileTree2() throws Exception {

        Path[] dirs = new Path[2];

        dirs[0] = Utils.resolveWithRoot(files, testDir, "test_walkFileTree2");
        dirs[1] = Utils.resolveWithRoot(files, dirs[0], "dir0");

        files.createDirectories(dirs[1]);

        Path[] tmp = new Path[10];

        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = Utils.resolveWithRoot(files, dirs[1], "file" + i);
            files.createFile(tmp[i]);
        }

        FileVisitor fv = new MyFileVisitor2();
        Utils.walkFileTree(files, dirs[0], fv);

        for (int i = 0; i < 10; i++) {
            files.delete(tmp[i]);
        }

        files.delete(dirs[1]);
        files.delete(dirs[0]);
    }

    class MyFileVisitor3 implements FileVisitor {

        @Override
        public FileVisitResult postVisitDirectory(Path dir, XenonException exception, Files files)
                throws XenonException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, FileAttributes attributes, Files files)
                throws XenonException {
            return FileVisitResult.SKIP_SUBTREE;
        }

        @Override
        public FileVisitResult visitFile(Path file, FileAttributes attributes, Files files) throws XenonException {
            throw new XenonException("", "Unexpected visit of path: " + file);
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, XenonException exception, Files files)
                throws XenonException {
            throw new XenonException("", "Visit failed of path: " + file);
        }
    }

    @Test
    public void test_walkFileTree3() throws Exception {

        Path[] dirs = new Path[2];

        dirs[0] = Utils.resolveWithRoot(files, testDir, "test_walkFileTree3");
        dirs[1] = Utils.resolveWithRoot(files, dirs[0], "dir0");

        files.createDirectories(dirs[1]);

        Path[] tmp = new Path[10];

        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = Utils.resolveWithRoot(files, dirs[1], "file" + i);
            files.createFile(tmp[i]);
        }

        FileVisitor fv = new MyFileVisitor3();
        Utils.walkFileTree(files, dirs[0], fv);

        for (int i = 0; i < 10; i++) {
            files.delete(tmp[i]);
        }

        files.delete(dirs[1]);
        files.delete(dirs[0]);
    }

    class MyFileVisitor4 implements FileVisitor {

        int countFiles = 0;

        @Override
        public FileVisitResult postVisitDirectory(Path dir, XenonException exception, Files files)
                throws XenonException {
            countFiles = 0;
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, FileAttributes attributes, Files files)
                throws XenonException {
            countFiles = 0;
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, FileAttributes attributes, Files files) throws XenonException {

            if (countFiles == 0) {
                countFiles = 1;
                return FileVisitResult.SKIP_SIBLINGS;
            }

            throw new XenonException("", "Unexpected visit of path: " + file);
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, XenonException exception, Files files)
                throws XenonException {
            throw new XenonException("", "Visit failed of path: " + file);
        }
    }

    @Test
    public void test_walkFileTree4() throws Exception {

        Path[] dirs = new Path[3];

        dirs[0] = Utils.resolveWithRoot(files, testDir, "test_walkFileTree4");
        dirs[1] = Utils.resolveWithRoot(files, dirs[0], "dir0");
        dirs[2] = Utils.resolveWithRoot(files, dirs[0], "dir1");

        files.createDirectories(dirs[1]);
        files.createDirectories(dirs[2]);

        Path[] tmp = new Path[10];

        for (int i = 0; i < 5; i++) {
            tmp[i] = Utils.resolveWithRoot(files, dirs[1], "file" + i);
            files.createFile(tmp[i]);
        }

        for (int i = 0; i < 5; i++) {
            tmp[5 + i] = Utils.resolveWithRoot(files, dirs[2], "file" + (5 + i));
            files.createFile(tmp[5 + i]);
        }

        FileVisitor fv = new MyFileVisitor4();
        Utils.walkFileTree(files, dirs[0], fv);

        for (int i = 0; i < 10; i++) {
            files.delete(tmp[i]);
        }

        files.delete(dirs[2]);
        files.delete(dirs[1]);
        files.delete(dirs[0]);
    }

    class MyFileVisitor5 implements FileVisitor {

        @Override
        public FileVisitResult postVisitDirectory(Path dir, XenonException exception, Files files)
                throws XenonException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, FileAttributes attributes, Files files)
                throws XenonException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, FileAttributes attributes, Files files) throws XenonException {
            return FileVisitResult.TERMINATE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, XenonException exception, Files files)
                throws XenonException {
            throw new XenonException("", "Visit failed of path: " + file);
        }
    }

    @Test
    public void test_walkFileTree5() throws Exception {

        Path[] dirs = new Path[2];

        dirs[0] = Utils.resolveWithRoot(files, testDir, "test_walkFileTree5");
        dirs[1] = Utils.resolveWithRoot(files, dirs[0], "dir0");

        files.createDirectories(dirs[1]);

        Path[] tmp = new Path[10];

        for (int i = 0; i < 10; i++) {
            tmp[i] = Utils.resolveWithRoot(files, dirs[1], "file" + i);
            files.createFile(tmp[i]);
        }

        FileVisitor fv = new MyFileVisitor5();
        Utils.walkFileTree(files, dirs[0], fv);

        for (int i = 0; i < 10; i++) {
            files.delete(tmp[i]);
        }

        files.delete(dirs[1]);
        files.delete(dirs[0]);
    }

    class MyFileVisitor6 implements FileVisitor {

        @Override
        public FileVisitResult postVisitDirectory(Path dir, XenonException exception, Files files)
                throws XenonException {
            throw new XenonException("", "Visit failed of path: " + dir);
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, FileAttributes attributes, Files files)
                throws XenonException {
            throw new XenonException("", "Visit failed of path: " + dir);
        }

        @Override
        public FileVisitResult visitFile(Path file, FileAttributes attributes, Files files) throws XenonException {
            throw new XenonException("", "Visit failed of path: " + file);
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, XenonException exception, Files files)
                throws XenonException {
            throw new XenonException("", "Visit failed of path: " + file);
        }
    }

    @Test
    public void test_walkFileTree6() throws Exception {

        Path dir = Utils.resolveWithRoot(files, testDir, "test_walkFileTree6");
        files.createDirectories(dir);

        FileVisitor fv = new MyFileVisitor6();

        try {
            Utils.walkFileTree(files, dir, fv);
            throw new Exception("test_walkFileTree6 did not throw an exception!");
        } catch (XenonException e) {
            // expected
        }

        files.delete(dir);
    }
}
