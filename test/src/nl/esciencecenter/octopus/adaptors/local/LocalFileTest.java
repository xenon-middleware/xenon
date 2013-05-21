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
package nl.esciencecenter.octopus.adaptors.local;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;

import junit.framework.Assert;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.RelativePath;

public class LocalFileTest {

    @org.junit.Test
    public void test1() throws Exception {
        // Tries to create an absolute path for tmpdir.
        
        Octopus octopus = OctopusFactory.newOctopus(null);

        String tmpdir = System.getProperty("java.io.tmpdir");

        System.err.println("tmpdir = " + tmpdir);

        Files files = octopus.files();
        
        FileSystem fs = files.newFileSystem(new URI("file:///"), null, null);

        AbsolutePath path = files.newPath(fs, new RelativePath(tmpdir));

        Assert.assertTrue(files.exists(path));

        octopus.end();

    }

    @org.junit.Test
    public void test2() throws Exception {
        
        // Tries to create a test-sandbox dir in tmpdir.
        
        String tmp = System.getProperty("java.io.tmpdir");
        
        Octopus octopus = OctopusFactory.newOctopus(null);
        
        Files files = octopus.files();
        
        FileSystem fs = files.newFileSystem(new URI("file:///"), null, null);

        AbsolutePath tmpDir = files.newPath(fs, new RelativePath(tmp));

        System.err.println("tmpdir = " + tmpDir);

        assertTrue(files.exists(tmpDir));
        assertTrue(files.isDirectory(tmpDir));

        AbsolutePath sandboxDir = files.newPath(fs, new RelativePath(tmp + "/test-sandbox"));

        if (files.exists(sandboxDir)) {
            System.err.println("deleting " + sandboxDir);
            files.delete(sandboxDir);
        }

        assertFalse(files.exists(sandboxDir));

        files.createDirectory(sandboxDir);

        assertTrue(files.exists(sandboxDir));

        files.delete(sandboxDir);
        
        assertFalse(files.exists(sandboxDir));
        
        octopus.end();

    }

    @org.junit.Test
    public void test3() throws Exception {
        
        // Tries to create a sandboxdir and create a new file in this sandboxdir.        
        Octopus octopus = OctopusFactory.newOctopus(null);

        Files files = octopus.files();
        
        FileSystem fs = files.newFileSystem(new URI("file:///"), null, null);

        String tmp = System.getProperty("java.io.tmpdir");
        
        AbsolutePath tmpDir = files.newPath(fs, new RelativePath(tmp));

        System.err.println("tmpdir = " + tmpDir);

        assertTrue(files.exists(tmpDir));

        assertTrue(files.isDirectory(tmpDir));

        AbsolutePath sandboxDir = files.newPath(fs, new RelativePath(tmp + "/test-sandbox"));

        if (files.exists(sandboxDir)) {
            System.err.println("deleting " + sandboxDir);
            files.delete(sandboxDir);
        }

        assertFalse(files.exists(sandboxDir));

        files.createDirectory(sandboxDir);

        assertTrue(files.exists(sandboxDir));

        AbsolutePath testFile = files.newPath(fs, new RelativePath(tmp + "/test-sandbox/test1"));

        assertFalse(files.exists(testFile));

        files.createFile(testFile);

        assertTrue(files.exists(testFile));

        files.delete(testFile);
        
        assertFalse(files.exists(testFile));

        files.delete(sandboxDir);
        
        assertFalse(files.exists(sandboxDir));

        octopus.end();
    }
    
    @org.junit.Test
    public void test4() throws Exception {
        
        // Tries to create a sandboxdir, create a new file in this sandboxdir, write some data to the file, reads back the data, 
        // and compares to the expected input.  
        
        Octopus octopus = OctopusFactory.newOctopus(null);

        Files files = octopus.files();
        
        FileSystem fs = files.newFileSystem(new URI("file:///"), null, null);

        String tmp = System.getProperty("java.io.tmpdir");
        
        AbsolutePath tmpDir = files.newPath(fs, new RelativePath(tmp));

        System.err.println("tmpdir = " + tmpDir);

        assertTrue(files.exists(tmpDir));

        assertTrue(files.isDirectory(tmpDir));

        AbsolutePath sandboxDir = files.newPath(fs, new RelativePath(tmp + "/test-sandbox"));

        if (files.exists(sandboxDir)) {
            System.err.println("deleting " + sandboxDir);
            files.delete(sandboxDir);
        }

        assertFalse(files.exists(sandboxDir));

        files.createDirectory(sandboxDir);

        assertTrue(files.exists(sandboxDir));

        AbsolutePath testFile = files.newPath(fs, new RelativePath(tmp + "/test-sandbox/test1"));

        assertFalse(files.exists(testFile));

        files.createFile(testFile);

        assertTrue(files.exists(testFile));
        
        OutputStream out = files.newOutputStream(testFile, OpenOption.WRITE);
        out.write("Hello world".getBytes());
        out.close();
        
        byte [] buffer = new byte[1024];
        
        InputStream in = files.newInputStream(testFile);
        int size = in.read(buffer);
        
        assertTrue(size == "Hello world".length());
        assertTrue(new String(buffer, 0, size).equals("Hello world"));
        
        files.delete(testFile);
        
        assertFalse(files.exists(testFile));

        files.delete(sandboxDir);
        
        assertFalse(files.exists(sandboxDir));

        octopus.end();
    }

    @org.junit.Test
    public void test5() throws Exception {
        
        // Tries to create a sandboxdir, creates a new file in this sandboxdir, write some data to the file, copies the file, 
        // reads back the data from the copy, and compares to the expected input.  
        
        Octopus octopus = OctopusFactory.newOctopus(null);

        Files files = octopus.files();
        
        FileSystem fs = files.newFileSystem(new URI("file:///"), null, null);

        String tmp = System.getProperty("java.io.tmpdir");
        
        AbsolutePath tmpDir = files.newPath(fs, new RelativePath(tmp));

        System.err.println("tmpdir = " + tmpDir);

        assertTrue(files.exists(tmpDir));

        assertTrue(files.isDirectory(tmpDir));

        AbsolutePath sandboxDir = files.newPath(fs, new RelativePath(tmp + "/test-sandbox"));

        if (files.exists(sandboxDir)) {
            System.err.println("deleting " + sandboxDir);
            files.delete(sandboxDir);
        }

        assertFalse(files.exists(sandboxDir));

        files.createDirectory(sandboxDir);

        assertTrue(files.exists(sandboxDir));

        AbsolutePath testFile = files.newPath(fs, new RelativePath(tmp + "/test-sandbox/test1"));

        assertFalse(files.exists(testFile));

        files.createFile(testFile);

        assertTrue(files.exists(testFile));
        
        OutputStream out = files.newOutputStream(testFile, OpenOption.WRITE);
        out.write("Hello world".getBytes());
        out.close();
        
        AbsolutePath testFile2 = files.newPath(fs, new RelativePath(tmp + "/test-sandbox/test2"));
        
        assertFalse(files.exists(testFile2));

        files.copy(testFile, testFile2);
        
        byte [] buffer = new byte[1024];
        
        InputStream in = files.newInputStream(testFile2);
        int size = in.read(buffer);
        
        assertTrue(size == "Hello world".length());
        assertTrue(new String(buffer, 0, size).equals("Hello world"));
                
        files.delete(testFile);
        assertFalse(files.exists(testFile));

        files.delete(testFile2);
        assertFalse(files.exists(testFile2));

        files.delete(sandboxDir);
        assertFalse(files.exists(sandboxDir));

        octopus.end();
    }

    @org.junit.Test
    public void test6() throws Exception {
        
        // Tries to create a sandboxdir, creates two files in this sandboxdir, write some data to both files, appends one file to  
        // the other, reads back the data from the files, and compares to the expected input.  
        
        Octopus octopus = OctopusFactory.newOctopus(null);

        Files files = octopus.files();
        
        FileSystem fs = files.newFileSystem(new URI("file:///"), null, null);

        String tmp = System.getProperty("java.io.tmpdir");
        
        AbsolutePath tmpDir = files.newPath(fs, new RelativePath(tmp));

        System.err.println("tmpdir = " + tmpDir);

        assertTrue(files.exists(tmpDir));

        assertTrue(files.isDirectory(tmpDir));

        AbsolutePath sandboxDir = files.newPath(fs, new RelativePath(tmp + "/test-sandbox"));

        if (files.exists(sandboxDir)) {
            System.err.println("deleting " + sandboxDir);
            files.delete(sandboxDir);
        }

        assertFalse(files.exists(sandboxDir));

        files.createDirectory(sandboxDir);

        assertTrue(files.exists(sandboxDir));

        AbsolutePath testFile = files.newPath(fs, new RelativePath(tmp + "/test-sandbox/test1"));

        assertFalse(files.exists(testFile));

        files.createFile(testFile);

        assertTrue(files.exists(testFile));
        
        OutputStream out = files.newOutputStream(testFile, OpenOption.WRITE);
        out.write("Hello world".getBytes());
        out.close();
        
        AbsolutePath testFile2 = files.newPath(fs, new RelativePath(tmp + "/test-sandbox/test2"));
        
        assertFalse(files.exists(testFile2));

        files.createFile(testFile2);

        assertTrue(files.exists(testFile2));
        
        out = files.newOutputStream(testFile2, OpenOption.WRITE);
        out.write("Goodbye world".getBytes());
        out.close();
        
        files.append(testFile, testFile2);
        
        byte [] buffer = new byte[1024];
        
        InputStream in = files.newInputStream(testFile2);
        int size = in.read(buffer);
        
        assertTrue(size == "Goodbye worldHello world".length());
        assertTrue(new String(buffer, 0, size).equals("Goodbye worldHello world"));
        
        files.delete(testFile);
        assertFalse(files.exists(testFile));

        files.delete(testFile2);
        assertFalse(files.exists(testFile2));
        
        files.delete(sandboxDir);
        
        assertFalse(files.exists(sandboxDir));

        octopus.end();
    }
    
    @org.junit.Test
    public void test7() throws Exception {
        
        // Tries to create a sandboxdir, creates two files in this sandboxdir, write data to one file, writes a subset of the data
        // to another file, uses resumeCopy to copy the rest from one file to another, reads back the data from the second file, 
        // compares to the expected input.  
        
        byte [] data = "01234567890123456789".getBytes();
        
        Octopus octopus = OctopusFactory.newOctopus(null);

        Files files = octopus.files();
        
        FileSystem fs = files.newFileSystem(new URI("file:///"), null, null);

        String tmp = System.getProperty("java.io.tmpdir");
        
        AbsolutePath tmpDir = files.newPath(fs, new RelativePath(tmp));

        System.err.println("tmpdir = " + tmpDir);

        assertTrue(files.exists(tmpDir));

        assertTrue(files.isDirectory(tmpDir));

        AbsolutePath sandboxDir = files.newPath(fs, new RelativePath(tmp + "/test-sandbox"));

        if (files.exists(sandboxDir)) {
            System.err.println("deleting " + sandboxDir);
            files.delete(sandboxDir);
        }

        assertFalse(files.exists(sandboxDir));

        files.createDirectory(sandboxDir);

        assertTrue(files.exists(sandboxDir));

        AbsolutePath testFile = files.newPath(fs, new RelativePath(tmp + "/test-sandbox/test1"));

        assertFalse(files.exists(testFile));

        files.createFile(testFile);

        assertTrue(files.exists(testFile));
        
        OutputStream out = files.newOutputStream(testFile, OpenOption.WRITE);
        out.write(data);
        out.close();
        
        AbsolutePath testFile2 = files.newPath(fs, new RelativePath(tmp + "/test-sandbox/test2"));
        
        assertFalse(files.exists(testFile2));

        files.createFile(testFile2);

        assertTrue(files.exists(testFile2));
        
        out = files.newOutputStream(testFile2, OpenOption.WRITE);
        out.write(data, 0, 10);
        out.close();
        
        files.resumeCopy(testFile, testFile2, true);
        
        byte [] buffer = new byte[1024];
        
        InputStream in = files.newInputStream(testFile2);
        int size = in.read(buffer);
        
        assertTrue(size == data.length);
        assertTrue(Arrays.equals(Arrays.copyOfRange(buffer, 0, size), data));
        
        files.delete(testFile);
        assertFalse(files.exists(testFile));

        files.delete(testFile2);
        assertFalse(files.exists(testFile2));
        
        files.delete(sandboxDir);
        
        assertFalse(files.exists(sandboxDir));

        octopus.end();
    }
    
}
