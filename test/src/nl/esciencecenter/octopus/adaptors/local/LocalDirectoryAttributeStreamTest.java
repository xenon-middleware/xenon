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

import java.nio.file.DirectoryIteratorException;
import java.util.NoSuchElementException;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.files.AbsolutePathImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.RelativePath;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class LocalDirectoryAttributeStreamTest {

    private static final String TEST_DIR = "octopus_test_" + System.currentTimeMillis();
    
    @org.junit.BeforeClass
    public static void prepareClass() throws OctopusIOException, OctopusException { 

        Octopus octopus = OctopusFactory.newOctopus(null);
        
        Files files = octopus.files();
        FileSystem fs = files.getLocalCWDFileSystem();
        AbsolutePath root = fs.getEntryPath();
        AbsolutePath testDir = root.resolve(new RelativePath(TEST_DIR));
        files.createDirectory(testDir);
        
        AbsolutePath file0 = testDir.resolve(new RelativePath("file0"));
        AbsolutePath file1 = testDir.resolve(new RelativePath("file2"));
        AbsolutePath file2 = testDir.resolve(new RelativePath("file3"));
        
        files.createFile(file0);
        files.createFile(file1);
        files.createFile(file2);
        
        OctopusFactory.endOctopus(octopus);
    }

    @org.junit.AfterClass
    public static void cleanupClass() throws OctopusException, OctopusIOException { 
        
        Octopus octopus = OctopusFactory.newOctopus(null);
        
        Files files = octopus.files();
        FileSystem fs = files.getLocalCWDFileSystem();
        AbsolutePath root = fs.getEntryPath();
        AbsolutePath testDir = root.resolve(new RelativePath(TEST_DIR));
        AbsolutePath file0 = testDir.resolve(new RelativePath("file0"));
        AbsolutePath file1 = testDir.resolve(new RelativePath("file2"));
        AbsolutePath file2 = testDir.resolve(new RelativePath("file3"));

        AbsolutePath dir0 = testDir.resolve(new RelativePath("dir0"));
        AbsolutePath file4 = dir0.resolve(new RelativePath("file4"));
        
        if (files.exists(testDir)) {
            
            if (files.exists(file4)) { 
                files.delete(file4);
            }

            if (files.exists(dir0)) { 
                files.delete(dir0);
            }
            
            files.delete(file0);
            files.delete(file1);
            files.delete(file2);
            files.delete(testDir);
        }
        
        OctopusFactory.endOctopus(octopus);
    }
    
    private OctopusEngine octopus;   
    private FileSystem fs;
    private AbsolutePath root; 
    private AbsolutePath testDir;
    
    private LocalAdaptor localAdaptor;
    private LocalFiles localFiles;
    
    
    class AllTrue implements DirectoryStream.Filter { 
        @Override
        public boolean accept(AbsolutePath entry) {
            return true;
        }
    }

    class AllFalse implements DirectoryStream.Filter { 
        @Override
        public boolean accept(AbsolutePath entry) {
            return false;
        }
    }
    
    @org.junit.Before
    public void prepareTest() throws OctopusIOException, OctopusException { 

        octopus = new OctopusEngine(null);
        localAdaptor = new LocalAdaptor(null, octopus);
        localFiles = new LocalFiles(new OctopusProperties(), localAdaptor, octopus);
        fs = localFiles.getLocalCWDFileSystem();
        root = fs.getEntryPath();
        testDir = root.resolve(new RelativePath(TEST_DIR));    
    } 

    @org.junit.After
    public void cleanupTest() throws OctopusIOException, OctopusException { 
        octopus.end();
    } 
    
    @org.junit.Test(expected = OctopusIOException.class)
    public void test_nonexistant_dir() throws Exception {
        AbsolutePath path = new AbsolutePathImplementation(fs, new RelativePath("aap"));
        new LocalDirectoryAttributeStream(localFiles, new LocalDirectoryStream(path, new AllTrue())); 
    }      
    
    @org.junit.Test
    public void test_ok_allTrue() throws Exception {
        
        LocalDirectoryAttributeStream stream = 
                new LocalDirectoryAttributeStream(localFiles, new LocalDirectoryStream(testDir, new AllTrue()));
        
        while (stream.hasNext()) { 
            stream.next();
        }

        stream.close();
    }

    @org.junit.Test
    public void test_ok_allTrue2() throws Exception {
        
        LocalDirectoryAttributeStream stream = 
                new LocalDirectoryAttributeStream(localFiles, new LocalDirectoryStream(testDir, new AllTrue()));
        
        for (int i=0;i<10;i++) { 
            if (!stream.hasNext()) { 
                throw new Exception("Failed to call hasNext in test_ok_allTrue!");
            }
        }
        
        while (stream.hasNext()) { 
            stream.next();
        }

        stream.close();
    }
    
    @org.junit.Test
    public void test_ok_allFalse() throws Exception {
        
        LocalDirectoryAttributeStream stream = 
                new LocalDirectoryAttributeStream(localFiles, new LocalDirectoryStream(testDir, new AllFalse()));
        
        while (stream.hasNext()) { 
            stream.next();
        }

        stream.close();
    }

    @org.junit.Test(expected = NoSuchElementException.class)
    public void test_ok_allFalse2() throws Exception {

        LocalDirectoryAttributeStream stream = 
                new LocalDirectoryAttributeStream(localFiles, new LocalDirectoryStream(testDir, new AllFalse()));
        
        stream.next();
    }
    
    @org.junit.Test(expected = NoSuchElementException.class)
    public void test_fails_next() throws Exception {

        LocalDirectoryAttributeStream stream = 
                new LocalDirectoryAttributeStream(localFiles, new LocalDirectoryStream(testDir, new AllTrue()));
        
        while (true) { 
            stream.next();
        }            
    }

    @org.junit.Test(expected = DirectoryIteratorException.class)
    public void test_remove_file_halfway_allTrue() throws Exception {

        AbsolutePath dir0 = testDir.resolve(new RelativePath("dir0"));
        localFiles.createDirectory(dir0);
    
        AbsolutePath file4 = dir0.resolve(new RelativePath("file4"));
        localFiles.createFile(file4);
        
        if (!localFiles.exists(file4)) { 
            throw new Exception("Failed to create file4!"); 
        }
        
        LocalDirectoryAttributeStream stream = 
                new LocalDirectoryAttributeStream(localFiles, new LocalDirectoryStream(dir0, new AllTrue()));
            
        while (stream.hasNext()) { 
            localFiles.delete(file4);
            stream.next();
        }

        stream.close();
    }
    
    @org.junit.Test(expected = UnsupportedOperationException.class)
    public void test_fails_remove() throws Exception {
        
        LocalDirectoryAttributeStream stream = 
                new LocalDirectoryAttributeStream(localFiles, new LocalDirectoryStream(testDir, new AllTrue()));
        
        stream.remove();
    }

    @org.junit.Test
    public void test_ok_double_close() throws Exception {
        LocalDirectoryAttributeStream stream = 
                new LocalDirectoryAttributeStream(localFiles, new LocalDirectoryStream(testDir, new AllTrue()));
        
        stream.close();
        stream.close();        
    }
}
