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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.UUID;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.Copy;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.CopyStatus;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.RelativePath;

public class AsyncLocalFileTest {    

    String tmpDirName = System.getProperty("java.io.tmpdir");

    Octopus octopus;
    Files files;   
    FileSystem fs;
    AbsolutePath tmpDir;
    
    private void prepare() throws OctopusException, OctopusIOException, URISyntaxException {         
        octopus = OctopusFactory.newOctopus(null);
        files = octopus.files();       
        fs = files.newFileSystem(new URI("file:///"), null, null);
        tmpDir = files.newPath(fs, new RelativePath(tmpDirName));    
        assertTrue(files.exists(tmpDir));
        assertTrue(files.isDirectory(tmpDir));
    }
    
    private void cleanup() throws Exception {         
        octopus.end();        
    }
    
    private AbsolutePath prepareTestDir(String testDir) throws OctopusIOException, OctopusException { 
        
        AbsolutePath sandboxDir = files.newPath(fs, tmpDir.getRelativePath().resolve(testDir));

        if (files.exists(sandboxDir)) {
            System.err.println("deleting " + sandboxDir);
            files.delete(sandboxDir);
        }

        assertFalse(files.exists(sandboxDir));

        files.createDirectory(sandboxDir);

        assertTrue(files.exists(sandboxDir));
        
        return sandboxDir;
    }
    
    
    private void cleanupTestDir(AbsolutePath sandboxDir) throws OctopusIOException { 

        files.delete(sandboxDir);
        
        assertFalse(files.exists(sandboxDir));
    }
    
    private AbsolutePath prepareTestFile(AbsolutePath dir, String name) throws OctopusIOException, OctopusException {  
        
        AbsolutePath testFile = files.newPath(fs, dir.getRelativePath().resolve(name));

        assertFalse(files.exists(testFile));

        files.createFile(testFile);

        assertTrue(files.exists(testFile));
        
        return testFile;
    }
    
    private void cleanupTestFile(AbsolutePath file) throws OctopusIOException { 

        files.delete(file);        
        assertFalse(files.exists(file));
    }
    
    private void writeData(AbsolutePath testFile, byte [] data) throws IOException { 
        
        assertTrue(files.exists(testFile));
        
        OutputStream out = files.newOutputStream(testFile, OpenOption.WRITE, OpenOption.TRUNCATE_EXISTING);
        out.write(data);
        out.close();       
        
        long size = files.size(testFile);
        
        assertTrue(size == data.length);
    }
    
    private void checkData(AbsolutePath testFile, byte [] data) throws IOException { 
        
        assertTrue(files.exists(testFile));
        
        byte [] buffer = new byte[data.length+1];
        
        InputStream in = files.newInputStream(testFile);
        int size = in.read(buffer);
        
        assertTrue(size == data.length);
        assertTrue(Arrays.equals(data, Arrays.copyOfRange(buffer, 0, data.length)));
    }
   
    private CopyStatus waitUntilDone(Copy copy) throws Exception { 

        CopyStatus status = files.getCopyStatus(copy);
        
        int count = 0;
        
        while (!status.isDone()) { 

            System.out.println("Got status " + count + " " + status.getState());
            
            count++;
            
            if (count > 10) {
                throw new Exception("Async copy failed to complete in time!");
            }
            
            try { 
                Thread.sleep(1000);
            } catch (Exception e) { 
                // ignored
            }
            
            status = files.getCopyStatus(copy);
        }

        System.out.println("Got status " + count + " " + status.getState());
        
        if (status.hasException()) { 
            throw new Exception("Async copy failed!", status.getException());
        }
        
        return status;
    }
    
    @org.junit.Test
    public void test1() throws Exception {
        
        // Tries to create a sandboxdir, creates a new file in this sandboxdir, write some data to the file, copies the file 
        // asynchronously, reads back the data from the copy, and compares to the expected input.          
        byte [] buffer = "Hello world".getBytes();
        
        prepare();
        
        AbsolutePath sandboxDir = prepareTestDir("test_sandbox_" + UUID.randomUUID());
        AbsolutePath testFile1 = prepareTestFile(sandboxDir, "test1");
        
        writeData(testFile1, buffer);

        AbsolutePath testFile2 = files.newPath(fs, sandboxDir.getRelativePath().resolve("test2"));
        
        assertFalse(files.exists(testFile2));

        long size = files.size(testFile1);
        
        Copy copy = files.copy(testFile1, testFile2, CopyOption.ASYNCHRONOUS);
        
        CopyStatus status = waitUntilDone(copy);
        
        assertTrue(status.bytesCopied() == size);
        assertTrue(files.exists(testFile2));
        
        checkData(testFile2, buffer);

        cleanupTestFile(testFile1);
        cleanupTestFile(testFile2);
        cleanupTestDir(sandboxDir);
        cleanup();
    }

    @org.junit.Test
    public void test2() throws Exception {
        
        // Tries to create a sandboxdir, creates a new file in this sandboxdir, write some data to the file, copies the file, 
        // reads back the data from the copy, and compares to the expected input.          
        byte [] buffer = "Hello world".getBytes();
        
        prepare();
        
        AbsolutePath sandboxDir = prepareTestDir("test_sandbox_" + UUID.randomUUID());
        AbsolutePath testFile1 = prepareTestFile(sandboxDir, "test1");
        
        writeData(testFile1, buffer);

        AbsolutePath testFile2 = files.newPath(fs, sandboxDir.getRelativePath().resolve("test2"));
        
        assertFalse(files.exists(testFile2));

        long size = files.size(testFile1);

        Copy copy = files.copy(testFile1, testFile2, CopyOption.CREATE, CopyOption.ASYNCHRONOUS);
        
        CopyStatus status = waitUntilDone(copy);
        
        assertTrue(status.bytesCopied() == size);
        assertTrue(files.exists(testFile2));
        
        checkData(testFile2, buffer);

        cleanupTestFile(testFile1);
        cleanupTestFile(testFile2);
        cleanupTestDir(sandboxDir);
        cleanup();
    }
    
    @org.junit.Test
    public void test3() throws Exception {
        
        // Tries to create a sandboxdir, creates two files in this sandboxdir, write some data to both files, appends one file to  
        // the other, reads back the data from the files, and compares to the expected input.  
        
        byte [] buffer1 = "Hello world".getBytes();
        byte [] buffer2 = "Goodbye world".getBytes();
        byte [] buffer3 = "Goodbye worldHello world".getBytes();
        
        prepare();
        
        AbsolutePath sandboxDir = prepareTestDir("test_sandbox_" + UUID.randomUUID());
        AbsolutePath testFile1 = prepareTestFile(sandboxDir, "test1");
        AbsolutePath testFile2 = prepareTestFile(sandboxDir, "test2");
        
        writeData(testFile1, buffer1);
        writeData(testFile2, buffer2);
        
        long size1 = files.size(testFile1);
        long size2 = files.size(testFile2);
        
        Copy copy = files.copy(testFile1, testFile2, CopyOption.APPEND, CopyOption.ASYNCHRONOUS);
        
        CopyStatus status = waitUntilDone(copy);
        
        assertTrue(status.bytesCopied() == size1);
        assertTrue(files.size(testFile2) == (size1+size2));
        
        checkData(testFile2, buffer3);
        
        cleanupTestFile(testFile1);
        cleanupTestFile(testFile2);
        cleanupTestDir(sandboxDir);
        cleanup();
    }
    
    @org.junit.Test
    public void test4() throws Exception {
        
        // Tries to create a sandboxdir, creates two files in this sandboxdir, write data to one file, writes a subset of the data
        // to another file, uses resumeCopy to copy the rest from one file to another, reads back the data from the second file, 
        // compares to the expected input.  
        
        byte [] buffer1 = "01234567890123456789".getBytes();
        byte [] buffer2 = "0123456789".getBytes();
        
        prepare();
        
        AbsolutePath sandboxDir = prepareTestDir("test_sandbox_" + UUID.randomUUID());
        AbsolutePath testFile1 = prepareTestFile(sandboxDir, "test1");
        AbsolutePath testFile2 = prepareTestFile(sandboxDir, "test2");
        
        writeData(testFile1, buffer1);
        writeData(testFile2, buffer2);
        
        long size1 = files.size(testFile1);
        long size2 = files.size(testFile2);
        
        Copy copy = files.copy(testFile1, testFile2, CopyOption.RESUME, CopyOption.ASYNCHRONOUS);
        
        CopyStatus status = waitUntilDone(copy);
        
        assertTrue(status.bytesCopied() == (size1-size2));
        
        checkData(testFile2, buffer1);

        cleanupTestFile(testFile1);
        cleanupTestFile(testFile2);
        cleanupTestDir(sandboxDir);
        cleanup();
    }

}
