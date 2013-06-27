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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.RelativePath;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.qos.logback.core.db.dialect.MySQLDialect;

public class RealFileUtilsTest {

    public static String ROOT = "/tmp/octopus_RealFileUtilsTest_" + System.currentTimeMillis();
    
    public static Octopus octopus;
    public static Files files;
    public static FileSystem fileSystem;
    
    public static AbsolutePath testDir;
    
    @BeforeClass 
    public static void prepare() throws Exception {
    
        octopus = OctopusFactory.newOctopus(null);
        
        files = octopus.files();
        fileSystem = files.getLocalCWDFileSystem();
    
        testDir = files.newPath(fileSystem, new RelativePath(ROOT));
        files.createDirectory(testDir);
    }
    
    
    @AfterClass 
    public static void cleanup() throws Exception {
        files.delete(testDir);
        OctopusFactory.endOctopus(octopus);
    }
    
    
    @Test
    public void test_copyFromInputStream1() throws Exception { 
     
        String message = "Hello World!";
        
        AbsolutePath testFile = testDir.resolve(new RelativePath("test_copyFromInputStream1.txt"));
        
        FileUtils.copy(octopus, new ByteArrayInputStream(message.getBytes()), testFile, true);
        
        byte [] tmp = FileUtils.readAllBytes(octopus, testFile);
        
        assertNotNull(tmp);
        assertTrue(tmp.length > 0);
        assertTrue(message.equals(new String(tmp)));
        
        files.delete(testFile);        
    }
    
    @Test
    public void test_copyFromInputStream2() throws Exception { 
     
        String message = "Hello World!";
        
        AbsolutePath testFile = testDir.resolve(new RelativePath("test_copyFromInputStream2.txt"));
        
        FileUtils.copy(octopus, new ByteArrayInputStream(message.getBytes()), testFile, true);
        FileUtils.copy(octopus, new ByteArrayInputStream(message.getBytes()), testFile, true);
        
        byte [] tmp = FileUtils.readAllBytes(octopus, testFile);

        assertNotNull(tmp);
        assertTrue(tmp.length > 0);
        assertTrue(message.equals(new String(tmp)));
        
        files.delete(testFile);        
    }
 
    @Test
    public void test_copyFromInputStream3() throws Exception { 
     
        String message = "Hello World!";
        
        AbsolutePath testFile = testDir.resolve(new RelativePath("test_copyFromInputStream3.txt"));
        
        FileUtils.copy(octopus, new ByteArrayInputStream(message.getBytes()), testFile, true);
        FileUtils.copy(octopus, new ByteArrayInputStream(message.getBytes()), testFile, false);
        
        byte [] tmp = FileUtils.readAllBytes(octopus, testFile);

        assertNotNull(tmp);
        assertTrue(tmp.length > 0);
        assertTrue(new String(tmp).equals(message + message));
        
        files.delete(testFile);        
    }
 
    @Test 
    public void test_copyToOutputStream1() throws Exception { 

        String message = "Hello World!";
        
        AbsolutePath testFile = testDir.resolve(new RelativePath("test_copyToOutputStream1.txt"));
        
        FileUtils.write(octopus, testFile, message.getBytes(), true);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        FileUtils.copy(octopus, testFile, out);
        
        byte [] tmp = out.toByteArray();

        assertNotNull(tmp);
        assertTrue(tmp.length > 0);
        assertTrue(new String(tmp).equals(message));
        
        files.delete(testFile);        
    }

    @Test 
    public void test_copyToOutputStream2() throws Exception { 

        String message = "Hello World!";
        
        AbsolutePath testFile = testDir.resolve(new RelativePath("test_copyToOutputStream2.txt"));
        
        FileUtils.write(octopus, testFile, message.getBytes(), true);
        FileUtils.write(octopus, testFile, message.getBytes(), true);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        FileUtils.copy(octopus, testFile, out);
        
        byte [] tmp = out.toByteArray();

        assertNotNull(tmp);
        assertTrue(tmp.length > 0);
        assertTrue(new String(tmp).equals(message));
        
        files.delete(testFile);        
    }

    @Test 
    public void test_copyToOutputStream3() throws Exception { 

        String message = "Hello World!";
        
        AbsolutePath testFile = testDir.resolve(new RelativePath("test_copyToOutputStream3.txt"));
        
        FileUtils.write(octopus, testFile, message.getBytes(), true);
        FileUtils.write(octopus, testFile, message.getBytes(), false);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        FileUtils.copy(octopus, testFile, out);
        
        byte [] tmp = out.toByteArray();

        assertNotNull(tmp);
        assertTrue(tmp.length > 0);
        assertTrue(new String(tmp).equals(message + message));
        
        files.delete(testFile);        
    }

    @Test 
    public void test_newBufferedWriter1() throws Exception { 

        String message = "Hello World!";
        
        AbsolutePath testFile = testDir.resolve(new RelativePath("test_newBufferedWriter1.txt"));
        
        BufferedWriter bw = FileUtils.newBufferedWriter(octopus, testFile, Charset.defaultCharset(), true);
        bw.write(message);
        bw.close();
        
        byte [] tmp = FileUtils.readAllBytes(octopus, testFile);

        assertNotNull(tmp);
        assertTrue(tmp.length > 0);
        assertTrue(new String(tmp).equals(message));
        
        files.delete(testFile);        
    } 
    
    @Test 
    public void test_newBufferedWriter2() throws Exception { 

        String message = "Hello World!";
        
        AbsolutePath testFile = testDir.resolve(new RelativePath("test_newBufferedWriter2.txt"));
        
        BufferedWriter bw = FileUtils.newBufferedWriter(octopus, testFile, Charset.defaultCharset(), true);
        bw.write(message);
        bw.close();
        
        bw = FileUtils.newBufferedWriter(octopus, testFile, Charset.defaultCharset(), true);
        bw.write(message);
        bw.close();
        
        byte [] tmp = FileUtils.readAllBytes(octopus, testFile);

        assertNotNull(tmp);
        assertTrue(tmp.length > 0);
        assertTrue(new String(tmp).equals(message));
        
        files.delete(testFile);        
    } 

    @Test 
    public void test_newBufferedWriter3() throws Exception { 

        String message = "Hello World!";
        
        AbsolutePath testFile = testDir.resolve(new RelativePath("test_newBufferedWriter3.txt"));
        
        BufferedWriter bw = FileUtils.newBufferedWriter(octopus, testFile, Charset.defaultCharset(), true);
        bw.write(message);
        bw.close();
        
        bw = FileUtils.newBufferedWriter(octopus, testFile, Charset.defaultCharset(), false);
        bw.write(message);
        bw.close();
        
        byte [] tmp = FileUtils.readAllBytes(octopus, testFile);

        assertNotNull(tmp);
        assertTrue(tmp.length > 0);
        assertTrue(new String(tmp).equals(message + message));
        
        files.delete(testFile);        
    } 
    
    @Test 
    public void test_newBufferedReader1() throws Exception { 

        String message = "Hello World!";
        
        AbsolutePath testFile = testDir.resolve(new RelativePath("test_newBufferedReader1.txt"));
        
        FileUtils.write(octopus, testFile, message.getBytes(), true);
        
        BufferedReader br = FileUtils.newBufferedReader(octopus, testFile, Charset.defaultCharset());
        
        String tmp = br.readLine();
        br.close();
        
        assertNotNull(tmp);
        assertTrue(tmp.equals(message));
        
        files.delete(testFile);        
    } 

    @Test 
    public void test_newBufferedReader2() throws Exception { 

        String message = "Hello World!\n";
        
        AbsolutePath testFile = testDir.resolve(new RelativePath("test_newBufferedReader2.txt"));
        
        FileUtils.write(octopus, testFile, message.getBytes(), true);
        FileUtils.write(octopus, testFile, message.getBytes(), false);
        
        BufferedReader br = FileUtils.newBufferedReader(octopus, testFile, Charset.defaultCharset());
        
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

        AbsolutePath testFile = testDir.resolve(new RelativePath("test_readAllLines1.txt"));
        
        BufferedWriter bw = FileUtils.newBufferedWriter(octopus, testFile, Charset.defaultCharset(), true);
        bw.write("line1\n");
        bw.write("line2\n");
        bw.write("line3\n");
        bw.write("line4\n");
        bw.close();
        
        List<String> tmp = FileUtils.readAllLines(octopus, testFile, Charset.defaultCharset());
        
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

        AbsolutePath testFile = testDir.resolve(new RelativePath("test_write1.txt"));
        
        LinkedList<String> tmp1 = new LinkedList<>();
        tmp1.add("line1");
        tmp1.add("line2");
        tmp1.add("line3");
        tmp1.add("line4");
               
        FileUtils.write(octopus, testFile, tmp1, Charset.defaultCharset(), true);
        
        List<String> tmp2 = FileUtils.readAllLines(octopus, testFile, Charset.defaultCharset());
        
        System.err.println("Read "+ tmp2.size() +  " lines");
        
        assertNotNull(tmp2);
        assertTrue(tmp2.size() == 4);
        assertTrue(tmp2.get(0).equals("line1"));
        assertTrue(tmp2.get(1).equals("line2"));
        assertTrue(tmp2.get(2).equals("line3"));
        assertTrue(tmp2.get(3).equals("line4"));
        
        files.delete(testFile);        
    }         

    class MyFileVisitor implements FileVisitor {

        AbsolutePath [] dirs;
        AbsolutePath [] files;
        
        MyFileVisitor(AbsolutePath [] dirs, AbsolutePath [] files) { 
            this.dirs = dirs;
            this.files = files;
        }
        
        private void check(AbsolutePath [] avail, AbsolutePath path) throws OctopusIOException { 
            
            for (int i=0;i<avail.length;i++) { 
                if (path.equals(avail[i])) { 
                    return;
                }
            }
            
            throw new OctopusIOException("", "Unexpected path: " + path.getPath()); 
        }
        
        @Override
        public FileVisitResult postVisitDirectory(AbsolutePath dir, OctopusIOException exception, Octopus octopus) 
                throws OctopusIOException {
            check(dirs, dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(AbsolutePath dir, FileAttributes attributes, Octopus octopus)
                throws OctopusIOException {
            
            check(dirs, dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(AbsolutePath file, FileAttributes attributes, Octopus octopus) throws OctopusIOException {

            check(files, file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(AbsolutePath file, OctopusIOException exception, Octopus octopus)
                throws OctopusIOException {
            throw new OctopusIOException("", "Visit failed of path: " + file.getPath());
        } 
        
    }
    
    @Test 
    public void test_walkFileTree1() throws Exception { 
    
        AbsolutePath [] dirs = new AbsolutePath[2];
        
        dirs[0] = testDir.resolve(new RelativePath("test_walkFileTree1"));
        dirs[1] = dirs[0].resolve(new RelativePath("dir0"));
        
        files.createDirectories(dirs[1]);
        
        AbsolutePath [] tmp = new AbsolutePath[10];
        
        for (int i=0;i<tmp.length;i++) { 
            tmp[i] = dirs[1].resolve(new RelativePath("file" + i));
            files.createFile(tmp[i]);
        }
        
        FileVisitor fv = new MyFileVisitor(dirs, tmp);
        FileUtils.walkFileTree(octopus, dirs[0], fv);

        for (int i=0;i<10;i++) { 
            files.delete(tmp[i]);
        }
        
        files.delete(dirs[1]);
        files.delete(dirs[0]);
    } 

    class MyFileVisitor2 implements FileVisitor {

        @Override
        public FileVisitResult postVisitDirectory(AbsolutePath dir, OctopusIOException exception, Octopus octopus) 
                throws OctopusIOException {
            throw new OctopusIOException("", "Unexpected visit of path: " + dir.getPath());
        }

        @Override
        public FileVisitResult preVisitDirectory(AbsolutePath dir, FileAttributes attributes, Octopus octopus)
                throws OctopusIOException {
            return FileVisitResult.TERMINATE;
        }

        @Override
        public FileVisitResult visitFile(AbsolutePath file, FileAttributes attributes, Octopus octopus) throws OctopusIOException {
            throw new OctopusIOException("", "Unexpected visit of path: " + file.getPath());
        }

        @Override
        public FileVisitResult visitFileFailed(AbsolutePath file, OctopusIOException exception, Octopus octopus)
                throws OctopusIOException {
            throw new OctopusIOException("", "Visit failed of path: " + file.getPath());
        }         
    }
    
    @Test 
    public void test_walkFileTree2() throws Exception { 
    
        AbsolutePath [] dirs = new AbsolutePath[2];
        
        dirs[0] = testDir.resolve(new RelativePath("test_walkFileTree2"));
        dirs[1] = dirs[0].resolve(new RelativePath("dir0"));
        
        files.createDirectories(dirs[1]);
        
        AbsolutePath [] tmp = new AbsolutePath[10];
        
        for (int i=0;i<tmp.length;i++) { 
            tmp[i] = dirs[1].resolve(new RelativePath("file" + i));
            files.createFile(tmp[i]);
        }
        
        FileVisitor fv = new MyFileVisitor2();
        FileUtils.walkFileTree(octopus, dirs[0], fv);

        for (int i=0;i<10;i++) { 
            files.delete(tmp[i]);
        }
        
        files.delete(dirs[1]);
        files.delete(dirs[0]);
    } 

    class MyFileVisitor3 implements FileVisitor {

        @Override
        public FileVisitResult postVisitDirectory(AbsolutePath dir, OctopusIOException exception, Octopus octopus) 
                throws OctopusIOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(AbsolutePath dir, FileAttributes attributes, Octopus octopus)
                throws OctopusIOException {
            return FileVisitResult.SKIP_SUBTREE;
        }

        @Override
        public FileVisitResult visitFile(AbsolutePath file, FileAttributes attributes, Octopus octopus) throws OctopusIOException {
            throw new OctopusIOException("", "Unexpected visit of path: " + file.getPath());
        }

        @Override
        public FileVisitResult visitFileFailed(AbsolutePath file, OctopusIOException exception, Octopus octopus)
                throws OctopusIOException {
            throw new OctopusIOException("", "Visit failed of path: " + file.getPath());
        }         
    }
    
    @Test 
    public void test_walkFileTree3() throws Exception { 
    
        AbsolutePath [] dirs = new AbsolutePath[2];
        
        dirs[0] = testDir.resolve(new RelativePath("test_walkFileTree3"));
        dirs[1] = dirs[0].resolve(new RelativePath("dir0"));
        
        files.createDirectories(dirs[1]);
        
        AbsolutePath [] tmp = new AbsolutePath[10];
        
        for (int i=0;i<tmp.length;i++) { 
            tmp[i] = dirs[1].resolve(new RelativePath("file" + i));
            files.createFile(tmp[i]);
        }
        
        FileVisitor fv = new MyFileVisitor3();
        FileUtils.walkFileTree(octopus, dirs[0], fv);

        for (int i=0;i<10;i++) { 
            files.delete(tmp[i]);
        }
        
        files.delete(dirs[1]);
        files.delete(dirs[0]);
    } 

    class MyFileVisitor4 implements FileVisitor {

        int countFiles = 0;
        
        @Override
        public FileVisitResult postVisitDirectory(AbsolutePath dir, OctopusIOException exception, Octopus octopus) 
                throws OctopusIOException {
            countFiles = 0;
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(AbsolutePath dir, FileAttributes attributes, Octopus octopus)
                throws OctopusIOException {
            countFiles = 0;
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(AbsolutePath file, FileAttributes attributes, Octopus octopus) throws OctopusIOException {
            
            if (countFiles == 0) {
                countFiles = 1;
                return FileVisitResult.SKIP_SIBLINGS;
            } 
            
            throw new OctopusIOException("", "Unexpected visit of path: " + file.getPath());
        }

        @Override
        public FileVisitResult visitFileFailed(AbsolutePath file, OctopusIOException exception, Octopus octopus)
                throws OctopusIOException {
            throw new OctopusIOException("", "Visit failed of path: " + file.getPath());
        }         
    }
    
    @Test 
    public void test_walkFileTree4() throws Exception { 
    
        AbsolutePath [] dirs = new AbsolutePath[3];
        
        dirs[0] = testDir.resolve(new RelativePath("test_walkFileTree4"));
        dirs[1] = dirs[0].resolve(new RelativePath("dir0"));
        dirs[2] = dirs[0].resolve(new RelativePath("dir1"));
        
        files.createDirectories(dirs[1]);
        files.createDirectories(dirs[2]);
        
        AbsolutePath [] tmp = new AbsolutePath[10];
        
        for (int i=0;i<5;i++) { 
            tmp[i] = dirs[1].resolve(new RelativePath("file" + i));
            files.createFile(tmp[i]);
        }
        
        for (int i=0;i<5;i++) { 
            tmp[5+i] = dirs[2].resolve(new RelativePath("file" + (5+i)));
            files.createFile(tmp[5+i]);
        }
        
        FileVisitor fv = new MyFileVisitor4();
        FileUtils.walkFileTree(octopus, dirs[0], fv);

        for (int i=0;i<10;i++) { 
            files.delete(tmp[i]);
        }
        
        files.delete(dirs[2]);
        files.delete(dirs[1]);
        files.delete(dirs[0]);
    } 

    class MyFileVisitor5 implements FileVisitor {

        @Override
        public FileVisitResult postVisitDirectory(AbsolutePath dir, OctopusIOException exception, Octopus octopus) 
                throws OctopusIOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(AbsolutePath dir, FileAttributes attributes, Octopus octopus)
                throws OctopusIOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(AbsolutePath file, FileAttributes attributes, Octopus octopus) throws OctopusIOException {
            return FileVisitResult.TERMINATE;
        }

        @Override
        public FileVisitResult visitFileFailed(AbsolutePath file, OctopusIOException exception, Octopus octopus)
                throws OctopusIOException {
            throw new OctopusIOException("", "Visit failed of path: " + file.getPath());
        }         
    }
    
    @Test 
    public void test_walkFileTree5() throws Exception { 
    
        AbsolutePath [] dirs = new AbsolutePath[2];
        
        dirs[0] = testDir.resolve(new RelativePath("test_walkFileTree5"));
        dirs[1] = dirs[0].resolve(new RelativePath("dir0"));
        
        files.createDirectories(dirs[1]);
        
        AbsolutePath [] tmp = new AbsolutePath[10];
        
        for (int i=0;i<10;i++) { 
            tmp[i] = dirs[1].resolve(new RelativePath("file" + i));
            files.createFile(tmp[i]);
        }
        
        FileVisitor fv = new MyFileVisitor5();
        FileUtils.walkFileTree(octopus, dirs[0], fv);

        for (int i=0;i<10;i++) { 
            files.delete(tmp[i]);
        }
        
        files.delete(dirs[1]);
        files.delete(dirs[0]);
    } 
    
    class MyFileVisitor6 implements FileVisitor {

        @Override
        public FileVisitResult postVisitDirectory(AbsolutePath dir, OctopusIOException exception, Octopus octopus) 
                throws OctopusIOException {
            throw new OctopusIOException("", "Visit failed of path: " + dir.getPath());
        }

        @Override
        public FileVisitResult preVisitDirectory(AbsolutePath dir, FileAttributes attributes, Octopus octopus)
                throws OctopusIOException {
            throw new OctopusIOException("", "Visit failed of path: " + dir.getPath());
        }

        @Override
        public FileVisitResult visitFile(AbsolutePath file, FileAttributes attributes, Octopus octopus) throws OctopusIOException {
            throw new OctopusIOException("", "Visit failed of path: " + file.getPath());
        }

        @Override
        public FileVisitResult visitFileFailed(AbsolutePath file, OctopusIOException exception, Octopus octopus)
                throws OctopusIOException {
            throw new OctopusIOException("", "Visit failed of path: " + file.getPath());
        }         
    }
    
    @Test 
    public void test_walkFileTree6() throws Exception { 
    
        AbsolutePath dir = testDir.resolve(new RelativePath("test_walkFileTree6"));
        files.createDirectories(dir);
        
        FileVisitor fv = new MyFileVisitor6();
        
        try { 
            FileUtils.walkFileTree(octopus, dir, fv);
            throw new Exception("test_walkFileTree6 did not throw an exception!");
        } catch (OctopusIOException e) { 
            // expected
        }
        
        files.delete(dir);
    } 
    

    
    
        /**
         * Walks a file tree.
         */
    //    public static AbsolutePath walkFileTree(Octopus octopus, AbsolutePath start, FileVisitor visitor) throws OctopusIOException {
    
        /**
         * Walks a file tree.
         */
      //  public static AbsolutePath walkFileTree(Octopus octopus, AbsolutePath start, boolean followLinks, int maxDepth,
        //        FileVisitor visitor) throws OctopusIOException {
  
        // Walk a file tree.
        //private static FileVisitResult walk(Octopus octopus, AbsolutePath path, FileAttributes attributes, boolean followLinks,
                //int maxDepth, FileVisitor visitor) throws OctopusIOException {

        /**
         * Recursively copies directories, files and symbolic links from source to target.
         *
         * @param octopus
         * @param source
         * @param target
         * @param options
         *
         * @throws OctopusIOException
         * @throws UnsupportedOperationException Thrown when CopyOption.REPLACE_EXISTING and CopyOption.IGNORE_EXISTING are used together.
         */
       // public static void recursiveCopy(Octopus octopus, AbsolutePath source, AbsolutePath target, CopyOption... options)
      //          throws OctopusIOException, UnsupportedOperationException {

        /**
         * Recursively removes all directories, files and symbolic links in path.
         *
         * @param octopus
         * @param path
         * @throws OctopusIOException
         */
      //  public static void recursiveDelete(Octopus octopus, AbsolutePath path) throws OctopusIOException {
//
    //}


}
